package com.seu.petadoptionservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private AdoptionApplication application;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 4)
    private String cardLast4;

    @Column(nullable = false, unique = true)
    private String transactionId;

    @Column(nullable = false)
    private String status; // SUCCESS, FAILED

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime paidAt;
}