package com.cituccs.sims.repository;

import com.cituccs.sims.entity.Court;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {

    // Locks the court row while a booking is being created, so two players
    // cannot book the same court and hour at the same moment.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Court c where c.courtId = :id")
    Optional<Court> findByIdForUpdate(@Param("id") Long id);
}
