package com.cituccs.sims.web;

import java.math.BigDecimal;
import java.util.List;

// Request and response shapes for the player booking APIs.
public final class PlayerBookingModels {

    private PlayerBookingModels() {
    }

    // ---- Requests ----

    public record EquipmentLine(Long equipmentId, Integer quantity) {
    }

    // date = "2026-12-05", startTime = "11:00"
    public record BookingRequest(Long courtId, String date, String startTime,
                                 Integer hours, List<EquipmentLine> equipment) {
    }

    // ---- Responses ----

    public record CourtInfo(Long courtId, String courtName, String description, BigDecimal hourlyRate) {
    }

    // available = pieces left for the chosen hours
    public record EquipmentOption(Long equipmentId, String name, String description,
                                  BigDecimal rentalFee, int available) {
    }

    public record BookingOptions(CourtInfo court, String date, String startTime, String endTime,
                                 int hours, int maxHours, BigDecimal courtFee,
                                 List<EquipmentOption> equipment) {
    }

    public record BookingCreated(Long bookingId, String bookingReference, String status,
                                 String expiresAt, String date, String startTime, String endTime,
                                 int hours, BigDecimal courtAmount, BigDecimal equipmentAmount,
                                 BigDecimal totalAmount) {
    }

    // ---- Payment screen ----

    // referenceNumber = the GCash reference, spaces allowed ("1234 567 890 123")
    public record PaymentRequest(String referenceNumber) {
    }

    public record GearLine(String name, int quantity, BigDecimal subtotal) {
    }

    // Latest payment of the booking (null when nothing was submitted yet)
    public record PaymentInfo(String status, String method, String reference, String submittedAt) {
    }

    public record GcashInfo(String accountName, String number) {
    }

    // expired = a PENDING booking whose 30-minute hold ran out without a payment
    // secondsLeft = seconds left in the hold (0 when there is no live countdown)
    public record BookingDetails(Long bookingId, String bookingReference, String status,
                                 boolean expired, long secondsLeft,
                                 String date, String startTime, String endTime, int hours,
                                 String courtName, String courtDescription,
                                 List<GearLine> gear,
                                 BigDecimal courtAmount, BigDecimal equipmentAmount, BigDecimal totalAmount,
                                 PaymentInfo payment, GcashInfo gcash) {
    }
}
