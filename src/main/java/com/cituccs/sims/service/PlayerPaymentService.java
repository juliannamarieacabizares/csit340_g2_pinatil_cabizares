package com.cituccs.sims.service;

import com.cituccs.sims.entity.Booking;
import com.cituccs.sims.entity.BookingEquipment;
import com.cituccs.sims.entity.Payment;
import com.cituccs.sims.repository.BookingEquipmentRepository;
import com.cituccs.sims.repository.BookingRepository;
import com.cituccs.sims.repository.PaymentRepository;
import com.cituccs.sims.web.PlayerBookingModels.BookingDetails;
import com.cituccs.sims.web.PlayerBookingModels.GcashInfo;
import com.cituccs.sims.web.PlayerBookingModels.GearLine;
import com.cituccs.sims.web.PlayerBookingModels.PaymentInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Payment screen: show one of the player's bookings and take the GCash reference.
@Service
public class PlayerPaymentService {

    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

    private final BookingRepository bookingRepository;
    private final BookingEquipmentRepository bookingEquipmentRepository;
    private final PaymentRepository paymentRepository;
    private final String gcashAccountName;
    private final String gcashNumber;

    // The GCash account can be changed in application.properties with
    // app.gcash.account-name and app.gcash.number. These are placeholder defaults.
    public PlayerPaymentService(BookingRepository bookingRepository,
                                BookingEquipmentRepository bookingEquipmentRepository,
                                PaymentRepository paymentRepository,
                                @Value("${app.gcash.account-name:Smash Point Pickleball Center}") String gcashAccountName,
                                @Value("${app.gcash.number:0917 123 4567}") String gcashNumber) {
        this.bookingRepository = bookingRepository;
        this.bookingEquipmentRepository = bookingEquipmentRepository;
        this.paymentRepository = paymentRepository;
        this.gcashAccountName = gcashAccountName;
        this.gcashNumber = gcashNumber;
    }

    // ------------------------------------------------------------------
    // 1) Booking details for the Payment screen
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public BookingDetails getDetails(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(PlayerPaymentService::notFound);
        requireOwner(booking, userId);
        return toDetails(booking);
    }

    // ------------------------------------------------------------------
    // 2) Submit the GCash reference (full amount, set by the server)
    // ------------------------------------------------------------------

    @Transactional
    public BookingDetails submitReference(Long userId, Long bookingId, String referenceText) {
        // Lock the booking row: a double click cannot create two payments.
        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(PlayerPaymentService::notFound);
        requireOwner(booking, userId);

        if (!"PENDING".equals(booking.getBookingStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This booking is " + booking.getBookingStatus().toLowerCase() + ", so it cannot take a payment.");
        }

        LocalDateTime now = LocalDateTime.now(PlayerBookingService.ZONE);
        if (booking.getExpiresAt() != null && !booking.getExpiresAt().isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The 30-minute hold ran out. Please book the court again.");
        }

        Payment latest = latestPayment(bookingId);
        if (latest != null && !"REJECTED".equals(latest.getPaymentStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A payment reference was already sent for this booking.");
        }

        String reference = cleanReference(referenceText);
        if (paymentRepository.existsByTransactionReferenceAndPaymentStatusNot(reference, "REJECTED")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This GCash reference was already used. Please check the number.");
        }

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentMethod("GCASH");
        payment.setPaymentStatus("SUBMITTED");
        payment.setAmount(booking.getTotalAmount()); // full amount, always from the server
        payment.setTransactionReference(reference);
        paymentRepository.save(payment);

        // The admin still has to check the payment. Keep the court held until then,
        // otherwise another player could book the same hour.
        booking.setExpiresAt(null);
        bookingRepository.save(booking);

        return toDetails(booking);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private BookingDetails toDetails(Booking booking) {
        LocalDateTime now = LocalDateTime.now(PlayerBookingService.ZONE);

        boolean pending = "PENDING".equals(booking.getBookingStatus());
        LocalDateTime expiresAt = booking.getExpiresAt();
        boolean expired = pending && expiresAt != null && !expiresAt.isAfter(now);
        long secondsLeft = (pending && expiresAt != null && !expired)
                ? Math.max(0, Duration.between(now, expiresAt).getSeconds())
                : 0;

        // Older rows have no start/end time: fall back to their single time slot.
        LocalTime start = booking.getStartTime() != null ? booking.getStartTime() : booking.getTimeSlot().getStartTime();
        LocalTime end = booking.getEndTime() != null ? booking.getEndTime() : booking.getTimeSlot().getEndTime();
        int hours = booking.getHours() != null ? booking.getHours() : 1;

        List<GearLine> gear = new ArrayList<>();
        for (BookingEquipment row : bookingEquipmentRepository.findByBookingBookingId(booking.getBookingId())) {
            gear.add(new GearLine(row.getEquipment().getEquipmentName(), row.getQuantity(), row.getSubtotal()));
        }

        Payment latest = latestPayment(booking.getBookingId());
        PaymentInfo payment = latest == null ? null : new PaymentInfo(
                latest.getPaymentStatus(), latest.getPaymentMethod(), latest.getTransactionReference(),
                latest.getCreatedAt() == null ? null : latest.getCreatedAt().toString());

        return new BookingDetails(
                booking.getBookingId(), booking.getBookingReference(), booking.getBookingStatus(),
                expired, secondsLeft,
                booking.getBookingDate().toString(), start.format(HHMM), end.format(HHMM), hours,
                booking.getCourt().getCourtName(), booking.getCourt().getDescription(),
                gear,
                booking.getCourtAmount(), booking.getEquipmentAmount(), booking.getTotalAmount(),
                payment, new GcashInfo(gcashAccountName, gcashNumber));
    }

    private Payment latestPayment(Long bookingId) {
        List<Payment> payments = paymentRepository.findByBookingBookingIdOrderByPaymentIdDesc(bookingId);
        return payments.isEmpty() ? null : payments.get(0);
    }

    // A player can only see and pay their own bookings.
    // We answer "not found" for other people's bookings, so ids cannot be guessed.
    private void requireOwner(Booking booking, Long userId) {
        if (booking.getUser() == null || !booking.getUser().getId().equals(userId)) {
            throw notFound();
        }
    }

    // GCash reference numbers are digits (usually 13). Spaces and dashes are allowed when typing.
    private String cleanReference(String text) {
        String digits = text == null ? "" : text.replaceAll("[\\s-]", "");
        if (!digits.matches("\\d{10,20}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Please enter the GCash reference number (10 to 20 digits).");
        }
        return digits;
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found.");
    }
}
