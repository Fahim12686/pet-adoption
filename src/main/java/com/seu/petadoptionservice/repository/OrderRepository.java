package com.seu.petadoptionservice.repository;

import com.seu.petadoptionservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser_IdOrderByCreatedAtDesc(Long userId);
}
