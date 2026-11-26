
package com.cituccs.sims.repository;

import com.cituccs.sims.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByCourt_CourtIdAndTimeSlot_SlotIdAndBookingDateAndBookingStatusIn(
            Long courtId,
            Long slotId,
            LocalDate bookingDate,
            java.util.Collection<String> bookingStatuses
    );

    List<Booking> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<Booking> findByBookingIdAndUser_Id(
            Long bookingId,
            Long userId
    );
}
