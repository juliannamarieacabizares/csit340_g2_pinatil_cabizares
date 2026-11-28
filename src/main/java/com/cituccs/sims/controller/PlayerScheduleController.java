package com.cituccs.sims.controller;

import com.cituccs.sims.entity.Court;
import com.cituccs.sims.entity.FacilityHours;
import com.cituccs.sims.entity.TimeSlot;
import com.cituccs.sims.repository.BookingRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/player")
public class PlayerScheduleController {

    @PersistenceContext
    private EntityManager em;

    private final BookingRepository bookingRepository;

    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");
    private static final ZoneId ZONE = ZoneId.of("Asia/Manila");

    public PlayerScheduleController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // One active booking on a date (a CONFIRMED booking, or a PENDING one with a live hold).
    private record Window(LocalTime start, LocalTime end, boolean confirmed) {
    }

    // 1) List of active courts
    @GetMapping("/courts")
    public List<Map<String, Object>> courts() {
        List<Court> courts = em.createQuery(
                "select c from Court c where c.courtStatus = 'ACTIVE' order by c.courtId",
                Court.class).getResultList();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Court c : courts) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("courtId", c.getCourtId());
            m.put("courtName", c.getCourtName());
            m.put("description", c.getDescription());
            m.put("hourlyRate", c.getHourlyRate());
            result.add(m);
        }
        return result;
    }

    // 2) Full grid: all courts, all hours, for one date
    @GetMapping("/schedule-all")
    public Map<String, Object> scheduleAll(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<Court> courts = em.createQuery(
                "select c from Court c where c.courtStatus = 'ACTIVE' order by c.courtId",
                Court.class).getResultList();

        // facility hours for this weekday
        String dayPrefix = date.getDayOfWeek().name().substring(0, 3).toLowerCase();
        FacilityHours hours = null;
        for (FacilityHours f : em.createQuery("select f from FacilityHours f", FacilityHours.class).getResultList()) {
            if (f.getDayOfWeek() != null && f.getDayOfWeek().trim().toLowerCase().startsWith(dayPrefix)) {
                hours = f;
                break;
            }
        }
        boolean dayClosed = hours == null || !hours.isOpen()
                || hours.getOpeningTime() == null || hours.getClosingTime() == null;

        LocalDateTime now = LocalDateTime.now(ZONE);

        // Active bookings on this date, grouped by court.
        // A booking can cover 1 to 3 hours, so we compare time ranges (overlap rule).
        Map<Long, List<Window>> windowsByCourt = new HashMap<>();
        for (Object[] row : bookingRepository.findActiveWindows(date, now)) {
            Long courtId = (Long) row[0];
            LocalTime start = (LocalTime) row[1];
            LocalTime end = (LocalTime) row[2];
            boolean confirmed = "CONFIRMED".equals(row[3]);
            windowsByCourt.computeIfAbsent(courtId, k -> new ArrayList<>()).add(new Window(start, end, confirmed));
        }

        List<Map<String, Object>> courtList = new ArrayList<>();
        for (Court c : courts) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("courtId", c.getCourtId());
            m.put("courtName", c.getCourtName());
            m.put("description", c.getDescription());
            m.put("hourlyRate", c.getHourlyRate());
            courtList.add(m);
        }

        List<TimeSlot> slots = em.createQuery(
                "select t from TimeSlot t where t.active = true order by t.startTime",
                TimeSlot.class).getResultList();

        List<Map<String, Object>> slotList = new ArrayList<>();

        for (TimeSlot t : slots) {
            boolean outside = dayClosed
                    || t.getStartTime().isBefore(hours.getOpeningTime())
                    || t.getEndTime().isAfter(hours.getClosingTime());
            boolean past = date.atTime(t.getStartTime()).isBefore(now);

            List<String> statuses = new ArrayList<>();
            for (Court c : courts) {
                String status;
                if (outside) status = "CLOSED";
                else if (past) status = "PAST";
                else status = bookingStatusFor(windowsByCourt.get(c.getCourtId()), t);
                statuses.add(status);
            }

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("slotId", t.getSlotId());
            m.put("startTime", t.getStartTime().format(HHMM));
            m.put("endTime", t.getEndTime().format(HHMM));
            m.put("statuses", statuses); // same order as the courts list
            slotList.add(m);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("date", date.toString());
        response.put("courts", courtList);
        response.put("slots", slotList);
        return response;
    }

    // AVAILABLE, PENDING_HOLD or RESERVED for one slot on one court.
    // Overlap rule: existing.start < slot.end AND existing.end > slot.start
    private static String bookingStatusFor(List<Window> windows, TimeSlot slot) {
        if (windows == null) return "AVAILABLE";
        boolean hold = false;
        for (Window w : windows) {
            if (w.start().isBefore(slot.getEndTime()) && w.end().isAfter(slot.getStartTime())) {
                if (w.confirmed()) return "RESERVED";
                hold = true;
            }
        }
        return hold ? "PENDING_HOLD" : "AVAILABLE";
    }
}
