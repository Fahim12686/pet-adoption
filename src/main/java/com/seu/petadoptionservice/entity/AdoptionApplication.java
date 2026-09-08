package com.seu.petadoptionservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "adoption_applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdoptionApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(length = 1000)
    private String reason;

    @Column(nullable = false)
    private String status; // PENDING, APPROVED, REJECTED, COMPLETED

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime appliedAt;
}