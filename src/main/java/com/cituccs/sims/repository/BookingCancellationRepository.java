
package com.cituccs.sims.repository;

import com.cituccs.sims.entity.BookingCancellation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingCancellationRepository extends JpaRepository<BookingCancellation, Long> {
}
