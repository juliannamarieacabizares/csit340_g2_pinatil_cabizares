
package com.cituccs.sims.controller;

import com.cituccs.sims.entity.Equipment;
import com.cituccs.sims.service.EquipmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/equipment")
public class AdminEquipmentController {

    private final EquipmentService equipmentService;

    private static final Set<String> VALID_STATUSES =
            Set.of("ACTIVE", "INACTIVE", "MAINTENANCE");

    public AdminEquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping
    public List<Equipment> getAllEquipment() {
        return equipmentService.getAllEquipment();
    }

    @GetMapping("/{id}")
    public Equipment getEquipmentById(@PathVariable Long id) {
        return equipmentService.getEquipmentById(id);
    }

    @PostMapping
    public ResponseEntity<Equipment> createEquipment(
            @RequestBody EquipmentRequest request) {

        validateEquipment(request);

        Equipment equipment = new Equipment();
        equipment.setEquipmentName(request.equipmentName().trim());
        equipment.setDescription(request.description());
        equipment.setTotalQuantity(request.totalQuantity());
        equipment.setRentalFee(request.rentalFee());
        equipment.setEquipmentStatus("ACTIVE");

        Equipment savedEquipment = equipmentService.saveEquipment(equipment);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedEquipment);
    }

    @PutMapping("/{id}")
    public Equipment updateEquipment(
            @PathVariable Long id,
            @RequestBody EquipmentRequest request) {

        validateEquipment(request);

        Equipment equipment = equipmentService.getEquipmentById(id);

        equipment.setEquipmentName(request.equipmentName().trim());
        equipment.setDescription(request.description());
        equipment.setTotalQuantity(request.totalQuantity());
        equipment.setRentalFee(request.rentalFee());

        return equipmentService.saveEquipment(equipment);
    }

    @PatchMapping("/{id}/status")
    public Equipment updateEquipmentStatus(
            @PathVariable Long id,
            @RequestBody StatusRequest request) {

        if (request.status() == null ||
                !VALID_STATUSES.contains(request.status().toUpperCase())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid equipment status."
            );
        }

        Equipment equipment = equipmentService.getEquipmentById(id);
        equipment.setEquipmentStatus(request.status().toUpperCase());

        return equipmentService.saveEquipment(equipment);
    }

    private void validateEquipment(EquipmentRequest request) {
        if (request.equipmentName() == null ||
                request.equipmentName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Equipment name is required."
            );
        }

        if (request.totalQuantity() == null ||
                request.totalQuantity() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity cannot be negative."
            );
        }

        if (request.rentalFee() == null ||
                request.rentalFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Rental fee cannot be negative."
            );
        }
    }

    public record EquipmentRequest(
            String equipmentName,
            String description,
            Integer totalQuantity,
            BigDecimal rentalFee
    ) {
    }

    public record StatusRequest(String status) {
    }
}
