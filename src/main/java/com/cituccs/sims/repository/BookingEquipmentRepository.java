package com.cituccs.sims.repository;

import com.cituccs.sims.entity.BookingEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingEquipmentRepository extends JpaRepository<BookingEquipment, Long> {

    List<BookingEquipment> findByBookingBookingId(Long bookingId);

    // How many pieces of one equipment are already used by active bookings
    // that overlap the given time window (CONFIRMED, or PENDING with a live hold).
    @Query("""
            select coalesce(sum(be.quantity), 0L) from BookingEquipment be
            where be.equipment.equipmentId = :equipmentId
              and be.booking.bookingDate = :date
              and (be.booking.bookingStatus = 'CONFIRMED'
                   or (be.booking.bookingStatus = 'PENDING'
                       and (be.booking.expiresAt is null or be.booking.expiresAt > :now)))
              and coalesce(be.booking.startTime, be.booking.timeSlot.startTime) < :endTime
              and coalesce(be.booking.endTime, be.booking.timeSlot.endTime) > :startTime
            """)
    Long sumBookedQuantity(@Param("equipmentId") Long equipmentId,
                           @Param("date") LocalDate date,
                           @Param("startTime") LocalTime startTime,
                           @Param("endTime") LocalTime endTime,
                           @Param("now") LocalDateTime now);
}
