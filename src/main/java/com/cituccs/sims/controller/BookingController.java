
package com.cituccs.sims.controller;

import com.cituccs.sims.config.AuthInterceptor;
import com.cituccs.sims.entity.Booking;
import com.cituccs.sims.entity.BookingCancellation;
import com.cituccs.sims.entity.Court;
import com.cituccs.sims.entity.TimeSlot;
import com.cituccs.sims.repository.CourtRepository;
import com.cituccs.sims.repository.TimeSlotRepository;
import com.cituccs.sims.service.BookingService;
import com.cituccs.sims.service.BookingCancellationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final BookingCancellationService cancellationService;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;

    public BookingController(
            BookingService bookingService,
            BookingCancellationService cancellationService,
            CourtRepository courtRepository,
            TimeSlotRepository timeSlotRepository) {

        this.bookingService = bookingService;
        this.cancellationService = cancellationService;
        this.courtRepository = courtRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    @GetMapping("/court")
    public Court getActiveCourt() {
        List<Court> courts =
                courtRepository.findByCourtStatusIgnoreCase("ACTIVE");

        if (courts.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No active court is available."
            );
        }

        if (courts.size() > 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Multiple active courts found."
            );
        }

        return courts.get(0);
    }

    @GetMapping("/time-slots")
    public List<TimeSlot> getActiveTimeSlots() {
        return timeSlotRepository.findByActiveTrueOrderByStartTimeAsc();
    }

    @GetMapping("/availability")
    public List<BookingService.AvailableTimeSlot> getAvailability(
            @RequestParam LocalDate date) {

        return bookingService.getAvailableTimeSlots(date);
    }

    @GetMapping("/my-bookings")
    public List<BookingResponse> getMyBookings(
            HttpServletRequest httpRequest) {

        Long userId = getAuthenticatedUserId(httpRequest);

        return bookingService.getBookingsByUser(userId)
                .stream()
                .map(this::toBookingResponse)
                .toList();
    }

    @GetMapping("/my-cancellations")
    public List<CancellationResponse> getMyCancellations(
            HttpServletRequest httpRequest) {

        Long userId = getAuthenticatedUserId(httpRequest);

        return cancellationService.getCancellationsByUser(userId)
                .stream()
                .map(this::toCancellationResponse)
                .toList();
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @RequestBody BookingRequest request,
            HttpServletRequest httpRequest) {

        Long userId = getAuthenticatedUserId(httpRequest);

        Booking booking = bookingService.createBooking(
                userId,
                request.bookingDate(),
                request.slotId()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toBookingResponse(booking));
    }

    @PostMapping("/{bookingId}/cancellation")
    public ResponseEntity<CancellationResponse> requestCancellation(
            @PathVariable Long bookingId,
            @RequestBody CancellationRequest request,
            HttpServletRequest httpRequest) {

        Long userId = getAuthenticatedUserId(httpRequest);

        BookingCancellation cancellation =
                cancellationService.requestCancellation(
                        userId,
                        bookingId,
                        request.reason()
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toCancellationResponse(cancellation));
    }

    private Long getAuthenticatedUserId(HttpServletRequest httpRequest) {

        Object userIdAttribute = httpRequest.getAttribute(
                AuthInterceptor.USER_ID_ATTRIBUTE
        );

        if (!(userIdAttribute instanceof Long userId)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please sign in."
            );
        }

        return userId;
    }

    private BookingResponse toBookingResponse(Booking booking) {

        return new BookingResponse(
                booking.getBookingId(),
                booking.getBookingReference(),
                booking.getBookingDate(),
                booking.getBookingStatus(),
                booking.getCourt().getCourtName(),
                booking.getTimeSlot().getSlotName(),
                booking.getTimeSlot().getStartTime(),
                booking.getTimeSlot().getEndTime(),
                booking.getCourtAmount(),
                booking.getEquipmentAmount(),
                booking.getTotalAmount(),
                booking.getCreatedAt()
        );
    }

    private CancellationResponse toCancellationResponse(
            BookingCancellation cancellation) {

        return new CancellationResponse(
                cancellation.getCancellationId(),
                cancellation.getBooking().getBookingId(),
                cancellation.getBooking().getBookingReference(),
                cancellation.getCancellationStatus(),
                cancellation.getReason(),
                cancellation.getRefundAmount(),
                cancellation.getRequestedAt()
        );
    }

    public record BookingRequest(
            LocalDate bookingDate,
            Long slotId
    ) {
    }

    public record CancellationRequest(
            String reason
    ) {
    }

    public record BookingResponse(
            Long bookingId,
            String bookingReference,
            LocalDate bookingDate,
            String bookingStatus,
            String courtName,
            String slotName,
            LocalTime startTime,
            LocalTime endTime,
            BigDecimal courtAmount,
            BigDecimal equipmentAmount,
            BigDecimal totalAmount,
            LocalDateTime createdAt
    ) {
    }

    public record CancellationResponse(
            Long cancellationId,
            Long bookingId,
            String bookingReference,
            String cancellationStatus,
            String reason,
            BigDecimal refundAmount,
            LocalDateTime requestedAt
    ) {
    }
}
