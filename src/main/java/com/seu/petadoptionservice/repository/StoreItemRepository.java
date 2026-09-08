package com.seu.petadoptionservice.repository;

import com.seu.petadoptionservice.entity.StoreItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreItemRepository extends JpaRepository<StoreItem, Long> {
    List<StoreItem> findByActiveTrue();
    List<StoreItem> findByCategoryAndActiveTrue(String category);
    List<StoreItem> findAllByOrderByIdAsc();
    List<StoreItem> findByActiveTrueOrderByIdAsc();
}
