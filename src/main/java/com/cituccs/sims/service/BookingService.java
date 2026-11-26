
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final FacilityHoursRepository facilityHoursRepository;

    private static final List<String> BLOCKING_STATUSES =
            List.of("PENDING", "CONFIRMED");

    private static final ZoneId PHILIPPINES_ZONE =
            ZoneId.of("Asia/Manila");

    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            CourtRepository courtRepository,
            TimeSlotRepository timeSlotRepository,
            FacilityHoursRepository facilityHoursRepository) {

        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.courtRepository = courtRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.facilityHoursRepository = facilityHoursRepository;
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByUser(Long userId) {
        return bookingRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    public Booking saveBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }

    public record AvailableTimeSlot(
            Long slotId,
            String slotName,
            java.time.LocalTime startTime,
            java.time.LocalTime endTime,
            boolean available
    ) {
    }

    private Court getActiveCourt() {
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
                    "Multiple active courts found. Please check the court settings."
            );
        }

        return courts.get(0);
    }

    private FacilityHours getFacilityHours(LocalDate date) {
        String day = date.getDayOfWeek().name();

        FacilityHours hours = facilityHoursRepository
                .findByDayOfWeekIgnoreCase(day)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Facility operating hours are not configured."
                ));

        if (!hours.isOpen()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The facility is closed on this date."
            );
        }

        return hours;
    }

    private void validateTimeSlot(
            TimeSlot slot,
            FacilityHours hours) {

        if (!slot.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This time slot is inactive."
            );
        }

        if (slot.getStartTime().isBefore(hours.getOpeningTime()) ||
                slot.getEndTime().isAfter(hours.getClosingTime())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This time slot is outside facility operating hours."
            );
        }
    }

    public List<AvailableTimeSlot> getAvailableTimeSlots(LocalDate date) {

        if (date == null ||
                date.isBefore(LocalDate.now(PHILIPPINES_ZONE))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please select a valid booking date."
            );
        }

        Court court = getActiveCourt();
        FacilityHours hours = getFacilityHours(date);

        return timeSlotRepository.findByActiveTrueOrderByStartTimeAsc()
                .stream()
                .filter(slot ->
                        !slot.getStartTime().isBefore(hours.getOpeningTime()) &&
                        !slot.getEndTime().isAfter(hours.getClosingTime()))
                .map(slot -> {
                    boolean available =
                            !bookingRepository
                                    .existsByCourt_CourtIdAndTimeSlot_SlotIdAndBookingDateAndBookingStatusIn(
                                            court.getCourtId(),
                                            slot.getSlotId(),
                                            date,
                                            BLOCKING_STATUSES
                                    );

                    return new AvailableTimeSlot(
                            slot.getSlotId(),
                            slot.getSlotName(),
                            slot.getStartTime(),
                            slot.getEndTime(),
                            available
                    );
                })
                .toList();
    }

    @Transactional
    public Booking createBooking(
            Long userId,
            LocalDate bookingDate,
            Long slotId) {

        if (bookingDate == null ||
                bookingDate.isBefore(LocalDate.now(PHILIPPINES_ZONE))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please select a valid booking date."
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User account not found."
                ));

        Court court = getActiveCourt();

        FacilityHours hours = getFacilityHours(bookingDate);

        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Time slot not found."
                ));

        validateTimeSlot(slot, hours);

        boolean alreadyBooked =
                bookingRepository
                        .existsByCourt_CourtIdAndTimeSlot_SlotIdAndBookingDateAndBookingStatusIn(
                                court.getCourtId(),
                                slot.getSlotId(),
                                bookingDate,
                                BLOCKING_STATUSES
                        );

        if (alreadyBooked) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This time slot has already been booked."
            );
        }

        long minutes = Duration.between(
                slot.getStartTime(),
                slot.getEndTime()
        ).toMinutes();

        BigDecimal courtAmount = court.getHourlyRate()
                .multiply(BigDecimal.valueOf(minutes))
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        Booking booking = new Booking();

        booking.setBookingReference(
                "PB-" + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase()
        );

        booking.setUser(user);
        booking.setCourt(court);
        booking.setTimeSlot(slot);
        booking.setBookingDate(bookingDate);
        booking.setBookingStatus("PENDING");
        booking.setCourtAmount(courtAmount);
        booking.setEquipmentAmount(BigDecimal.ZERO);
        booking.setTotalAmount(courtAmount);

        return bookingRepository.save(booking);
    }
}
