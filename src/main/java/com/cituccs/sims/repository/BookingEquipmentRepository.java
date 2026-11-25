
package com.cituccs.sims.repository;

import com.cituccs.sims.entity.BookingEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingEquipmentRepository extends JpaRepository<BookingEquipment, Long> {
}
