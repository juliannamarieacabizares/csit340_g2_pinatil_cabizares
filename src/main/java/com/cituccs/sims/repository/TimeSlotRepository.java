package com.cituccs.sims.repository;

import com.cituccs.sims.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    List<TimeSlot> findByActiveTrueOrderByStartTimeAsc();
}
