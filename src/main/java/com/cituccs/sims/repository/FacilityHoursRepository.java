package com.cituccs.sims.repository;

import com.cituccs.sims.entity.FacilityHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacilityHoursRepository extends JpaRepository<FacilityHours, Long> {

    Optional<FacilityHours> findByDayOfWeekIgnoreCase(String dayOfWeek);
}