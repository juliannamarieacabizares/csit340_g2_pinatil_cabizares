
package com.cituccs.sims.controller;

import com.cituccs.sims.entity.Court;
import com.cituccs.sims.service.CourtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/courts")
public class AdminCourtController {

    private final CourtService courtService;

    private static final Set<String> VALID_STATUSES =
            Set.of("ACTIVE", "INACTIVE", "MAINTENANCE");

    public AdminCourtController(CourtService courtService) {
        this.courtService = courtService;
    }

    @GetMapping
    public List<Court> getAllCourts() {
        return courtService.getAllCourts();
    }

    @GetMapping("/{id}")
    public Court getCourtById(@PathVariable Long id) {
        return courtService.getCourtById(id);
    }

    @PostMapping
    public ResponseEntity<Court> createCourt(@RequestBody CourtRequest request) {
        validateCourt(request);

        Court court = new Court();
        court.setCourtName(request.courtName().trim());
        court.setDescription(request.description());
        court.setHourlyRate(request.hourlyRate());
        court.setCourtStatus("ACTIVE");

        Court savedCourt = courtService.saveCourt(court);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedCourt);
    }

    @PutMapping("/{id}")
    public Court updateCourt(
            @PathVariable Long id,
            @RequestBody CourtRequest request) {

        validateCourt(request);

        Court court = courtService.getCourtById(id);

        court.setCourtName(request.courtName().trim());
        court.setDescription(request.description());
        court.setHourlyRate(request.hourlyRate());

        return courtService.saveCourt(court);
    }

    @PatchMapping("/{id}/status")
    public Court updateCourtStatus(
            @PathVariable Long id,
            @RequestBody StatusRequest request) {

        if (request.status() == null ||
                !VALID_STATUSES.contains(request.status().toUpperCase())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid court status."
            );
        }

        Court court = courtService.getCourtById(id);
        court.setCourtStatus(request.status().toUpperCase());

        return courtService.saveCourt(court);
    }

    private void validateCourt(CourtRequest request) {
        if (request.courtName() == null ||
                request.courtName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Court name is required."
            );
        }

        if (request.hourlyRate() == null ||
                request.hourlyRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Hourly rate must be greater than zero."
            );
        }
    }

    public record CourtRequest(
            String courtName,
            String description,
            BigDecimal hourlyRate
    ) {
    }

    public record StatusRequest(String status) {
    }
}
