package com.cituccs.sims.service;

import com.cituccs.sims.entity.Booking;
import com.cituccs.sims.entity.BookingEquipment;
import com.cituccs.sims.entity.Court;
import com.cituccs.sims.entity.Equipment;
import com.cituccs.sims.entity.FacilityHours;
import com.cituccs.sims.entity.TimeSlot;
import com.cituccs.sims.entity.User;
import com.cituccs.sims.repository.BookingEquipmentRepository;
import com.cituccs.sims.repository.BookingRepository;
import com.cituccs.sims.repository.CourtRepository;
import com.cituccs.sims.repository.EquipmentRepository;
import com.cituccs.sims.repository.FacilityHoursRepository;
import com.cituccs.sims.repository.TimeSlotRepository;
import com.cituccs.sims.repository.UserRepository;
import com.cituccs.sims.web.PlayerBookingModels.BookingCreated;
import com.cituccs.sims.web.PlayerBookingModels.BookingOptions;
import com.cituccs.sims.web.PlayerBookingModels.BookingRequest;
import com.cituccs.sims.web.PlayerBookingModels.CourtInfo;
import com.cituccs.sims.web.PlayerBookingModels.EquipmentLine;
import com.cituccs.sims.web.PlayerBookingModels.EquipmentOption;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlayerBookingService {

    public static final ZoneId ZONE = ZoneId.of("Asia/Manila");
    private static final int MAX_HOURS = 3;
    private static final int HOLD_MINUTES = 30;
    private static final int CANCEL_CUTOFF_HOURS = 24;
    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");
    private static final String REFERENCE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final FacilityHoursRepository facilityHoursRepository;
    private final BookingRepository bookingRepository;
    private final BookingEquipmentRepository bookingEquipmentRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();

    public PlayerBookingService(CourtRepository courtRepository,
                                TimeSlotRepository timeSlotRepository,
                                FacilityHoursRepository facilityHoursRepository,
                                BookingRepository bookingRepository,
                                BookingEquipmentRepository bookingEquipmentRepository,
                                EquipmentRepository equipmentRepository,
                                UserRepository userRepository) {
        this.courtRepository = courtRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.facilityHoursRepository = facilityHoursRepository;
        this.bookingRepository = bookingRepository;
        this.bookingEquipmentRepository = bookingEquipmentRepository;
        this.equipmentRepository = equipmentRepository;
        this.userRepository = userRepository;
    }

    // ------------------------------------------------------------------
    // 1) Options for the Booking Details screen
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public BookingOptions getOptions(Long courtId, String dateText, String startText, int requestedHours) {
        LocalDate date = parseDate(dateText);
        LocalTime start = parseTime(startText);
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Court not found."));
        requireActiveCourt(court);

        LocalDateTime now = LocalDateTime.now(ZONE);
        List<TimeSlot> usable = findUsableSlots(court, date, start, now);

        int hours = Math.max(1, Math.min(requestedHours, usable.size()));
        List<TimeSlot> chosen = usable.subList(0, hours);
        LocalTime windowStart = chosen.get(0).getStartTime();
        LocalTime windowEnd = chosen.get(hours - 1).getEndTime();

        List<EquipmentOption> options = new ArrayList<>();
        for (Equipment e : equipmentRepository.findByEquipmentStatusOrderByEquipmentIdAsc("ACTIVE")) {
            options.add(new EquipmentOption(
                    e.getEquipmentId(), e.getEquipmentName(), e.getDescription(), e.getRentalFee(),
                    availableQuantity(e, date, windowStart, windowEnd, now)));
        }

        return new BookingOptions(
                new CourtInfo(court.getCourtId(), court.getCourtName(), court.getDescription(), court.getHourlyRate()),
                date.toString(), windowStart.format(HHMM), windowEnd.format(HHMM),
                hours, usable.size(),
                money(court.getHourlyRate().multiply(BigDecimal.valueOf(hours))),
                options);
    }

    // ------------------------------------------------------------------
    // 2) Create a booking (PENDING, held for 30 minutes)
    // ------------------------------------------------------------------

    @Transactional
    public BookingCreated createBooking(Long userId, BookingRequest request) {
        if (request == null || request.courtId() == null || request.date() == null
                || request.startTime() == null || request.hours() == null) {
            throw bad("Please choose a court, date, start time and number of hours.");
        }
        int hours = request.hours();
        if (hours < 1 || hours > MAX_HOURS) {
            throw bad("You can book 1 to " + MAX_HOURS + " hours.");
        }
        LocalDate date = parseDate(request.date());
        LocalTime start = parseTime(request.startTime());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in again."));

        // Lock the court row first, so the overlap check below is safe.
        Court court = courtRepository.findByIdForUpdate(request.courtId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Court not found."));
        requireActiveCourt(court);

        LocalDateTime now = LocalDateTime.now(ZONE);
        List<TimeSlot> usable = findUsableSlots(court, date, start, now);
        if (usable.size() < hours) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Those hours are no longer all available. Please pick another time.");
        }
        List<TimeSlot> chosen = usable.subList(0, hours);
        LocalTime windowStart = chosen.get(0).getStartTime();
        LocalTime windowEnd = chosen.get(hours - 1).getEndTime();

        // Merge equipment lines and ignore zero quantities.
        Map<Long, Integer> wanted = new LinkedHashMap<>();
        if (request.equipment() != null) {
            for (EquipmentLine line : request.equipment()) {
                if (line == null || line.equipmentId() == null || line.quantity() == null) {
                    throw bad("Equipment details are not valid.");
                }
                if (line.quantity() < 0) {
                    throw bad("Equipment quantity cannot be negative.");
                }
                if (line.quantity() > 0) {
                    wanted.merge(line.equipmentId(), line.quantity(), Integer::sum);
                }
            }
        }

        // Check stock (rows locked) and calculate the equipment price on the server.
        BigDecimal equipmentAmount = BigDecimal.ZERO;
        List<BookingEquipment> equipmentRows = new ArrayList<>();
        Booking booking = new Booking();

        if (!wanted.isEmpty()) {
            List<Equipment> locked = equipmentRepository.findAllByIdForUpdate(wanted.keySet());
            if (locked.size() != wanted.size()) {
                throw bad("One of the equipment items does not exist.");
            }
            for (Equipment e : locked) {
                int qty = wanted.get(e.getEquipmentId());
                if (!"ACTIVE".equals(e.getEquipmentStatus())) {
                    throw bad(e.getEquipmentName() + " is not available for rent.");
                }
                int available = availableQuantity(e, date, windowStart, windowEnd, now);
                if (qty > available) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Only " + available + " " + e.getEquipmentName() + " left for your hours.");
                }
                BigDecimal subtotal = money(e.getRentalFee().multiply(BigDecimal.valueOf(qty)));
                equipmentAmount = equipmentAmount.add(subtotal);

                BookingEquipment row = new BookingEquipment();
                row.setBooking(booking);
                row.setEquipment(e);
                row.setQuantity(qty);
                row.setUnitFee(e.getRentalFee());
                row.setSubtotal(subtotal);
                equipmentRows.add(row);
            }
        }

        BigDecimal courtAmount = money(court.getHourlyRate().multiply(BigDecimal.valueOf(hours)));
        BigDecimal total = courtAmount.add(equipmentAmount);

        booking.setBookingReference(newReference());
        booking.setUser(user);
        booking.setCourt(court);
        booking.setTimeSlot(chosen.get(0));
        booking.setBookingDate(date);
        booking.setStartTime(windowStart);
        booking.setEndTime(windowEnd);
        booking.setHours(hours);
        booking.setBookingStatus("PENDING");
        booking.setExpiresAt(now.plusMinutes(HOLD_MINUTES));
        booking.setCourtAmount(courtAmount);
        booking.setEquipmentAmount(equipmentAmount);
        booking.setTotalAmount(total);
        booking.setCancellationDeadline(date.atTime(windowStart).minusHours(CANCEL_CUTOFF_HOURS));

        Booking saved = bookingRepository.save(booking);
        bookingEquipmentRepository.saveAll(equipmentRows);

        return new BookingCreated(
                saved.getBookingId(), saved.getBookingReference(), saved.getBookingStatus(),
                saved.getExpiresAt().toString(), date.toString(),
                windowStart.format(HHMM), windowEnd.format(HHMM), hours,
                courtAmount, equipmentAmount, total);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    // Hours in a row (max 3) that can be booked from the start time.
    // A slot is usable if it is inside the opening hours, not in the past,
    // follows the previous slot with no gap, and has no active booking on it.
    private List<TimeSlot> findUsableSlots(Court court, LocalDate date, LocalTime start, LocalDateTime now) {
        List<TimeSlot> slots = timeSlotRepository.findByActiveTrueOrderByStartTimeAsc();
        int index = -1;
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).getStartTime().equals(start)) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            throw bad("That start time is not a valid hour.");
        }

        FacilityHours open = facilityHoursFor(date);
        if (open == null || !open.isOpen() || open.getOpeningTime() == null || open.getClosingTime() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The facility is closed on that day.");
        }

        List<TimeSlot> usable = new ArrayList<>();
        for (int i = 0; i < MAX_HOURS && index + i < slots.size(); i++) {
            TimeSlot slot = slots.get(index + i);
            if (i > 0 && !slot.getStartTime().equals(usable.get(i - 1).getEndTime())) break;
            if (slot.getStartTime().isBefore(open.getOpeningTime())
                    || slot.getEndTime().isAfter(open.getClosingTime())) break;
            if (date.atTime(slot.getStartTime()).isBefore(now)) break;
            if (bookingRepository.countActiveOverlapping(
                    court.getCourtId(), date, slot.getStartTime(), slot.getEndTime(), now) > 0) break;
            usable.add(slot);
        }

        if (usable.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "That hour is no longer available. Please pick another time.");
        }
        return usable;
    }

    private FacilityHours facilityHoursFor(LocalDate date) {
        String prefix = date.getDayOfWeek().name().substring(0, 3).toLowerCase();
        for (FacilityHours f : facilityHoursRepository.findAll()) {
            if (f.getDayOfWeek() != null && f.getDayOfWeek().trim().toLowerCase().startsWith(prefix)) {
                return f;
            }
        }
        return null;
    }

    // total pieces - pieces used by active bookings that overlap the window
    private int availableQuantity(Equipment e, LocalDate date, LocalTime start, LocalTime end, LocalDateTime now) {
        Long used = bookingEquipmentRepository.sumBookedQuantity(e.getEquipmentId(), date, start, end, now);
        int total = e.getTotalQuantity() == null ? 0 : e.getTotalQuantity();
        return Math.max(0, total - (used == null ? 0 : used.intValue()));
    }

    private void requireActiveCourt(Court court) {
        if (!"ACTIVE".equals(court.getCourtStatus())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Court not found.");
        }
    }

    private String newReference() {
        String day = LocalDate.now(ZONE).format(DateTimeFormatter.BASIC_ISO_DATE);
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder code = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                code.append(REFERENCE_CHARS.charAt(random.nextInt(REFERENCE_CHARS.length())));
            }
            String reference = "PB-" + day + "-" + code;
            if (!bookingRepository.existsByBookingReference(reference)) {
                return reference;
            }
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not create a booking reference.");
    }

    private LocalDate parseDate(String text) {
        try {
            LocalDate date = LocalDate.parse(text);
            if (date.isBefore(LocalDate.now(ZONE))) {
                throw bad("You cannot book a date in the past.");
            }
            return date;
        } catch (DateTimeParseException e) {
            throw bad("The date is not valid.");
        }
    }

    private LocalTime parseTime(String text) {
        try {
            return LocalTime.parse(text);
        } catch (DateTimeParseException e) {
            throw bad("The start time is not valid.");
        }
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
