package com.cituccs.sims.repository;

import com.cituccs.sims.entity.Booking;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByBookingReference(String bookingReference);

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

    // Locks one booking while a payment reference is being submitted,
    // so a double click cannot create two payments.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.bookingId = :id")
    Optional<Booking> findByIdForUpdate(@Param("id") Long id);

    // "Active" = CONFIRMED, or PENDING and the 30-minute hold has not expired.
    // Old rows without start/end time fall back to their single time slot.
    // Overlap rule: existing.start < new.end AND existing.end > new.start

    @Query("""
            select count(b) from Booking b
            where b.court.courtId = :courtId
              and b.bookingDate = :date
              and (b.bookingStatus = 'CONFIRMED'
                   or (b.bookingStatus = 'PENDING' and (b.expiresAt is null or b.expiresAt > :now)))
              and coalesce(b.startTime, b.timeSlot.startTime) < :endTime
              and coalesce(b.endTime, b.timeSlot.endTime) > :startTime
            """)
    long countActiveOverlapping(@Param("courtId") Long courtId,
                                @Param("date") LocalDate date,
                                @Param("startTime") LocalTime startTime,
                                @Param("endTime") LocalTime endTime,
                                @Param("now") LocalDateTime now);

    // One row per active booking on a date: [courtId, startTime, endTime, bookingStatus]
    @Query("""
            select b.court.courtId,
                   coalesce(b.startTime, b.timeSlot.startTime),
                   coalesce(b.endTime, b.timeSlot.endTime),
                   b.bookingStatus
            from Booking b
            where b.bookingDate = :date
              and (b.bookingStatus = 'CONFIRMED'
                   or (b.bookingStatus = 'PENDING' and (b.expiresAt is null or b.expiresAt > :now)))
            """)
    List<Object[]> findActiveWindows(@Param("date") LocalDate date,
                                     @Param("now") LocalDateTime now);
}
