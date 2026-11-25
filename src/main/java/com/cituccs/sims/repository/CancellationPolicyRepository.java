
package com.cituccs.sims.repository;

import com.cituccs.sims.entity.CancellationPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CancellationPolicyRepository extends JpaRepository<CancellationPolicy, Long> {
}
