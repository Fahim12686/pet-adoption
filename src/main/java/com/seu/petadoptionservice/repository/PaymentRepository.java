package com.seu.petadoptionservice.repository;

import com.seu.petadoptionservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByApplicationId(Long applicationId);
}