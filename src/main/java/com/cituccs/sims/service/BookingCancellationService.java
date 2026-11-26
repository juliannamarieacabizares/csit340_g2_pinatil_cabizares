
package com.cituccs.sims.service;

import com.cituccs.sims.entity.*;
import com.cituccs.sims.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class BookingCancellationService {

    private final BookingCancellationRepository bookingCancellationRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CancellationPolicyRepository cancellationPolicyRepository;

    private static final ZoneId PHILIPPINES_ZONE =
            ZoneId.of("Asia/Manila");

    public BookingCancellationService(
            BookingCancellationRepository bookingCancellationRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository,
            CancellationPolicyRepository cancellationPolicyRepository) {

        this.bookingCancellationRepository = bookingCancellationRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.cancellationPolicyRepository = cancellationPolicyRepository;
    }

    public List<BookingCancellation> getAllCancellations() {
        return bookingCancellationRepository.findAll();
    }

    public BookingCancellation getCancellationById(Long id) {
        return bookingCancellationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cancellation request not found."
                ));
    }

    public List<BookingCancellation> getPendingCancellations() {
        return bookingCancellationRepository
                .findByCancellationStatusOrderByRequestedAtAsc("PENDING");
    }

    public List<BookingCancellation> getCancellationsByUser(Long userId) {
        return bookingCancellationRepository
                .findByRequestedBy_IdOrderByRequestedAtDesc(userId);
    }

    public BookingCancellation saveCancellation(
            BookingCancellation cancellation) {
        return bookingCancellationRepository.save(cancellation);
    }

    public void deleteCancellation(Long id) {
        bookingCancellationRepository.deleteById(id);
    }

    @Transactional
    public BookingCancellation requestCancellation(
            Long userId,
            Long bookingId,
            String reason) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User account not found."
                ));

        Booking booking = bookingRepository
                .findByBookingIdAndUser_Id(bookingId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Booking not found."
                ));

        if (!List.of("PENDING", "CONFIRMED")
                .contains(booking.getBookingStatus())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This booking cannot be cancelled."
            );
        }

        if (bookingCancellationRepository
                .existsByBooking_BookingId(bookingId)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A cancellation request already exists for this booking."
            );
        }

        if (reason == null || reason.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please provide a cancellation reason."
            );
        }

        LocalDateTime now = LocalDateTime.now(PHILIPPINES_ZONE);

        LocalDateTime bookingStart = LocalDateTime.of(
                booking.getBookingDate(),
                booking.getTimeSlot().getStartTime()
        );

        if (!bookingStart.isAfter(now)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A booking that has already started cannot be cancelled."
            );
        }

        long hoursBeforeBooking =
                Duration.between(now, bookingStart).toHours();

        CancellationPolicy policy = cancellationPolicyRepository
                .findFirstByActiveTrueAndHoursBeforeBookingLessThanEqualOrderByHoursBeforeBookingDesc(
                        Math.toIntExact(hoursBeforeBooking)
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "No applicable cancellation policy was found."
                ));

        BigDecimal refundAmount = booking.getTotalAmount()
                .multiply(policy.getRefundPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        booking.setCancellationPolicy(policy);
        booking.setRefundPercentage(policy.getRefundPercentage());
        booking.setCancellationDeadline(bookingStart.minusHours(24));

        bookingRepository.save(booking);

        BookingCancellation cancellation = new BookingCancellation();

        cancellation.setBooking(booking);
        cancellation.setRequestedBy(user);
        cancellation.setReason(reason.trim());
        cancellation.setCancellationStatus("PENDING");
        cancellation.setRefundAmount(refundAmount);

        return bookingCancellationRepository.save(cancellation);
    }

    @Transactional
    public BookingCancellation approveCancellation(
            Long cancellationId,
            Long adminId) {

        BookingCancellation cancellation =
                getCancellationById(cancellationId);

        if (!"PENDING".equals(cancellation.getCancellationStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This cancellation request has already been processed."
            );
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Admin account not found."
                ));

        Booking booking = cancellation.getBooking();

        if (!List.of("PENDING", "CONFIRMED")
                .contains(booking.getBookingStatus())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This booking can no longer be cancelled."
            );
        }

        booking.setBookingStatus("CANCELLED");

        bookingRepository.save(booking);

        cancellation.setCancellationStatus("APPROVED");
        cancellation.setProcessedBy(admin);
        cancellation.setProcessedAt(
                LocalDateTime.now(PHILIPPINES_ZONE)
        );

        return bookingCancellationRepository.save(cancellation);
    }

    @Transactional
    public BookingCancellation rejectCancellation(
            Long cancellationId,
            Long adminId) {

        BookingCancellation cancellation =
                getCancellationById(cancellationId);

        if (!"PENDING".equals(cancellation.getCancellationStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This cancellation request has already been processed."
            );
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Admin account not found."
                ));

        Booking booking = cancellation.getBooking();

        booking.setCancellationPolicy(null);
        booking.setRefundPercentage(null);
        booking.setCancellationDeadline(null);

        bookingRepository.save(booking);

        cancellation.setCancellationStatus("REJECTED");
        cancellation.setProcessedBy(admin);
        cancellation.setProcessedAt(
                LocalDateTime.now(PHILIPPINES_ZONE)
        );

        return bookingCancellationRepository.save(cancellation);
    }
}
