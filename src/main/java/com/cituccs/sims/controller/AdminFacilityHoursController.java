
package com.cituccs.sims.controller;

import com.cituccs.sims.entity.FacilityHours;
import com.cituccs.sims.service.FacilityHoursService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/facility-hours")
public class AdminFacilityHoursController {

    private final FacilityHoursService facilityHoursService;

    private static final Set<String> VALID_DAYS = Set.of(
            "MONDAY", "TUESDAY", "WEDNESDAY",
            "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
    );

    public AdminFacilityHoursController(FacilityHoursService facilityHoursService) {
        this.facilityHoursService = facilityHoursService;
    }

    @GetMapping
    public List<FacilityHours> getAllFacilityHours() {
        return facilityHoursService.getAllFacilityHours();
    }

    @PostMapping
    public ResponseEntity<FacilityHours> createFacilityHours(
            @RequestBody FacilityHoursRequest request) {

        validateFacilityHours(request);

        FacilityHours hours = new FacilityHours();
        hours.setDayOfWeek(request.dayOfWeek().toUpperCase());
        hours.setOpen(request.open());

        if (request.open()) {
            hours.setOpeningTime(request.openingTime());
            hours.setClosingTime(request.closingTime());
        }

        FacilityHours savedHours = facilityHoursService.saveFacilityHours(hours);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedHours);
    }

    @PutMapping("/{id}")
    public FacilityHours updateFacilityHours(
            @PathVariable Long id,
            @RequestBody FacilityHoursRequest request) {

        validateFacilityHours(request);

        FacilityHours hours = facilityHoursService.getFacilityHoursById(id);
        hours.setDayOfWeek(request.dayOfWeek().toUpperCase());
        hours.setOpen(request.open());

        if (request.open()) {
            hours.setOpeningTime(request.openingTime());
            hours.setClosingTime(request.closingTime());
        } else {
            hours.setOpeningTime(null);
            hours.setClosingTime(null);
        }

        return facilityHoursService.saveFacilityHours(hours);
    }

    private void validateFacilityHours(FacilityHoursRequest request) {
        if (request.dayOfWeek() == null ||
                !VALID_DAYS.contains(request.dayOfWeek().toUpperCase())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A valid day of the week is required."
            );
        }

        if (request.open() &&
                (request.openingTime() == null || request.closingTime() == null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Opening and closing times are required."
            );
        }

        if (request.open() &&
                !request.closingTime().isAfter(request.openingTime())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Closing time must be later than opening time."
            );
        }
    }

    public record FacilityHoursRequest(
            String dayOfWeek,
            LocalTime openingTime,
            LocalTime closingTime,
            boolean open
    ) {
    }
}
