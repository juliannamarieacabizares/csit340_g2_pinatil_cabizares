package com.cituccs.sims.controller;

import com.cituccs.sims.config.AuthInterceptor;
import com.cituccs.sims.service.PlayerBookingService;
import com.cituccs.sims.web.PlayerBookingModels.BookingCreated;
import com.cituccs.sims.web.PlayerBookingModels.BookingOptions;
import com.cituccs.sims.web.PlayerBookingModels.BookingRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

// Player booking APIs. The AuthInterceptor already requires a logged-in user
// for every /api/** path, so the session always has the user id here.
@RestController
@RequestMapping("/api/player")
public class PlayerBookingController {

    private final PlayerBookingService bookingService;

    public PlayerBookingController(PlayerBookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Court details, real end time, max hours, court fee and equipment stock
    // for one court, date, start time and number of hours.
    @GetMapping("/booking-options")
    public BookingOptions bookingOptions(
            @RequestParam Long courtId,
            @RequestParam String date,
            @RequestParam String startTime,
            @RequestParam(defaultValue = "1") int hours) {
        return bookingService.getOptions(courtId, date, startTime, hours);
    }

    // Creates a PENDING booking that holds the court for 30 minutes.
    @PostMapping("/bookings")
    public ResponseEntity<BookingCreated> createBooking(
            @SessionAttribute(AuthInterceptor.USER_ID_ATTRIBUTE) Long userId,
            @RequestBody BookingRequest request) {
        BookingCreated created = bookingService.createBooking(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
