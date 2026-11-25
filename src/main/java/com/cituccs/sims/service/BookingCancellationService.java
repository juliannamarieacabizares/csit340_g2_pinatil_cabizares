
package com.cituccs.sims.service;

import com.cituccs.sims.entity.BookingCancellation;
import com.cituccs.sims.repository.BookingCancellationRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookingCancellationService {

    private final BookingCancellationRepository bookingCancellationRepository;

    public BookingCancellationService(BookingCancellationRepository bookingCancellationRepository) {
        this.bookingCancellationRepository = bookingCancellationRepository;
    }

    public List<BookingCancellation> getAllCancellations() {
        return bookingCancellationRepository.findAll();
    }

    public BookingCancellation getCancellationById(Long id) {
        return bookingCancellationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cancellation not found"));
    }

    public BookingCancellation saveCancellation(BookingCancellation cancellation) {
        return bookingCancellationRepository.save(cancellation);
    }

    public void deleteCancellation(Long id) {
        bookingCancellationRepository.deleteById(id);
    }
}
