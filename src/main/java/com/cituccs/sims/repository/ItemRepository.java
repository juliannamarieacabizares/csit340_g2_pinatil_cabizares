package com.cituccs.sims.repository;

import com.cituccs.sims.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            String name, String category, Pageable pageable);
}