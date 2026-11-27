package com.cituccs.sims.controller;

import com.cituccs.sims.config.AuthInterceptor;
import com.cituccs.sims.service.PlayerPaymentService;
import com.cituccs.sims.web.PlayerBookingModels.BookingDetails;
import com.cituccs.sims.web.PlayerBookingModels.PaymentRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

// Payment screen APIs. A player can only open and pay their own bookings.
@RestController
@RequestMapping("/api/player/bookings")
public class PlayerPaymentController {

    private final PlayerPaymentService paymentService;

    public PlayerPaymentController(PlayerPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Booking summary, hold countdown, latest payment status and the GCash account
    @GetMapping("/{bookingId}")
    public BookingDetails details(
            @SessionAttribute(AuthInterceptor.USER_ID_ATTRIBUTE) Long userId,
            @PathVariable Long bookingId) {
        return paymentService.getDetails(userId, bookingId);
    }

    // Sends the GCash reference. The amount is always the booking total (full payment).
    @PostMapping("/{bookingId}/payment")
    public BookingDetails submitPayment(
            @SessionAttribute(AuthInterceptor.USER_ID_ATTRIBUTE) Long userId,
            @PathVariable Long bookingId,
            @RequestBody PaymentRequest request) {
        return paymentService.submitReference(userId, bookingId,
                request == null ? null : request.referenceNumber());
    }
}
