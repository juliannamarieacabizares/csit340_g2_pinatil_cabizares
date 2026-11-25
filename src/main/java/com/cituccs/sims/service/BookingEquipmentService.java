
package com.cituccs.sims.service;

import com.cituccs.sims.entity.BookingEquipment;
import com.cituccs.sims.repository.BookingEquipmentRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookingEquipmentService {

    private final BookingEquipmentRepository bookingEquipmentRepository;

    public BookingEquipmentService(BookingEquipmentRepository bookingEquipmentRepository) {
        this.bookingEquipmentRepository = bookingEquipmentRepository;
    }

    public List<BookingEquipment> getAllBookingEquipment() {
        return bookingEquipmentRepository.findAll();
    }

    public BookingEquipment getBookingEquipmentById(Long id) {
        return bookingEquipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking equipment not found"));
    }

    public BookingEquipment saveBookingEquipment(BookingEquipment bookingEquipment) {
        return bookingEquipmentRepository.save(bookingEquipment);
    }

    public void deleteBookingEquipment(Long id) {
        bookingEquipmentRepository.deleteById(id);
    }
}
