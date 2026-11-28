
package com.cituccs.sims.controller;

import com.cituccs.sims.config.AuthInterceptor;
import com.cituccs.sims.entity.BookingCancellation;
import com.cituccs.sims.service.BookingCancellationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/cancellations")
public class AdminCancellationController {

    private final BookingCancellationService cancellationService;

    public AdminCancellationController(
            BookingCancellationService cancellationService) {
        this.cancellationService = cancellationService;
    }

    @GetMapping
    public List<CancellationResponse> getAllCancellations() {
        return cancellationService.getAllCancellations()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/pending")
    public List<CancellationResponse> getPendingCancellations() {
        return cancellationService.getPendingCancellations()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PutMapping("/{cancellationId}/approve")
    public CancellationResponse approveCancellation(
            @PathVariable Long cancellationId,
            HttpServletRequest request) {

        Long adminId = getAuthenticatedUserId(request);

        BookingCancellation cancellation =
                cancellationService.approveCancellation(
                        cancellationId,
                        adminId
                );

        return toResponse(cancellation);
    }

    @PutMapping("/{cancellationId}/reject")
    public CancellationResponse rejectCancellation(
            @PathVariable Long cancellationId,
            HttpServletRequest request) {

        Long adminId = getAuthenticatedUserId(request);

        BookingCancellation cancellation =
                cancellationService.rejectCancellation(
                        cancellationId,
                        adminId
                );

        return toResponse(cancellation);
    }

    private Long getAuthenticatedUserId(HttpServletRequest request) {
        Object userId = request.getAttribute(
                AuthInterceptor.USER_ID_ATTRIBUTE
        );

        if (!(userId instanceof Long)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Please sign in."
            );
        }

        return (Long) userId;
    }

    private CancellationResponse toResponse(
            BookingCancellation cancellation) {

        return new CancellationResponse(
                cancellation.getCancellationId(),
                cancellation.getBooking().getBookingId(),
                cancellation.getBooking().getBookingReference(),
                cancellation.getRequestedBy().getName(),
                cancellation.getReason(),
                cancellation.getCancellationStatus(),
                cancellation.getRefundAmount(),
                cancellation.getRequestedAt(),
                cancellation.getProcessedAt()
        );
    }

    public record CancellationResponse(
            Long cancellationId,
            Long bookingId,
            String bookingReference,
            String requestedBy,
            String reason,
            String cancellationStatus,
            BigDecimal refundAmount,
            LocalDateTime requestedAt,
            LocalDateTime processedAt
    ) {
    }
}
