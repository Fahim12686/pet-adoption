package com.seu.petadoptionservice.repository;

import com.seu.petadoptionservice.entity.AdoptionApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdoptionApplicationRepository extends JpaRepository<AdoptionApplication, Long> {
    List<AdoptionApplication> findByUserId(Long userId);
}