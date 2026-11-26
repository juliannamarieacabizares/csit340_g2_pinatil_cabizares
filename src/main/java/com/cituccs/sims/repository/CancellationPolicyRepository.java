
package com.cituccs.sims.repository;

import com.cituccs.sims.entity.CancellationPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CancellationPolicyRepository extends JpaRepository<CancellationPolicy, Long> {

    Optional<CancellationPolicy> findFirstByActiveTrueAndHoursBeforeBookingLessThanEqualOrderByHoursBeforeBookingDesc(
            Integer hoursBeforeBooking
    );
}
