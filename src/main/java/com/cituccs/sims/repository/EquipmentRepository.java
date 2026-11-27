package com.cituccs.sims.repository;

import com.cituccs.sims.entity.Equipment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findByEquipmentStatusOrderByEquipmentIdAsc(String equipmentStatus);

    // Locks the rows while a booking is being created, so two players
    // cannot take the last piece of equipment at the same moment.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Equipment e where e.equipmentId in :ids order by e.equipmentId")
    List<Equipment> findAllByIdForUpdate(@Param("ids") Collection<Long> ids);
}
