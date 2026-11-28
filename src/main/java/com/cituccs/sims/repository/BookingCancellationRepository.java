
package com.cituccs.sims.repository;

import com.cituccs.sims.entity.BookingCancellation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingCancellationRepository extends JpaRepository<BookingCancellation, Long> {

    boolean existsByBooking_BookingId(Long bookingId);

    List<BookingCancellation> findByCancellationStatusOrderByRequestedAtAsc(
            String cancellationStatus
    );

    List<BookingCancellation> findByRequestedBy_IdOrderByRequestedAtDesc(
            Long userId
    );
}
