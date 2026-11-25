
package com.cituccs.sims.controller;

import com.cituccs.sims.entity.TimeSlot;
import com.cituccs.sims.service.TimeSlotService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/time-slots")
public class AdminTimeSlotController {

    private final TimeSlotService timeSlotService;

    public AdminTimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @GetMapping
    public List<TimeSlot> getAllTimeSlots() {
        return timeSlotService.getAllTimeSlots();
    }

    @GetMapping("/{id}")
    public TimeSlot getTimeSlotById(@PathVariable Long id) {
        return timeSlotService.getTimeSlotById(id);
    }

    @PostMapping
    public ResponseEntity<TimeSlot> createTimeSlot(
            @RequestBody TimeSlotRequest request) {

        validateTimeSlot(request);

        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setSlotName(request.slotName().trim());
        timeSlot.setStartTime(request.startTime());
        timeSlot.setEndTime(request.endTime());
        timeSlot.setActive(true);

        TimeSlot savedTimeSlot = timeSlotService.saveTimeSlot(timeSlot);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedTimeSlot);
    }

    @PutMapping("/{id}")
    public TimeSlot updateTimeSlot(
            @PathVariable Long id,
            @RequestBody TimeSlotRequest request) {

        validateTimeSlot(request);

        TimeSlot timeSlot = timeSlotService.getTimeSlotById(id);
        timeSlot.setSlotName(request.slotName().trim());
        timeSlot.setStartTime(request.startTime());
        timeSlot.setEndTime(request.endTime());

        return timeSlotService.saveTimeSlot(timeSlot);
    }

    @PatchMapping("/{id}/status")
    public TimeSlot updateTimeSlotStatus(
            @PathVariable Long id,
            @RequestBody StatusRequest request) {

        if (request.active() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Active status is required."
            );
        }

        TimeSlot timeSlot = timeSlotService.getTimeSlotById(id);
        timeSlot.setActive(request.active());

        return timeSlotService.saveTimeSlot(timeSlot);
    }

    private void validateTimeSlot(TimeSlotRequest request) {
        if (request.slotName() == null || request.slotName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Time slot name is required."
            );
        }

        if (request.startTime() == null || request.endTime() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start time and end time are required."
            );
        }

        if (!request.endTime().isAfter(request.startTime())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End time must be later than start time."
            );
        }
    }

    public record TimeSlotRequest(
            String slotName,
            LocalTime startTime,
            LocalTime endTime
    ) {
    }

    public record StatusRequest(Boolean active) {
    }
}
