package com.seu.petadoptionservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String species;

    private String breed;
    private Integer age;
    private String gender;

    @Column(length = 1000)
    private String description;

    private String imageUrl;

    @Column(nullable = false)
    private BigDecimal adoptionFee;

    @Column(nullable = false)
    private String status; // AVAILABLE, PENDING, ADOPTED, FOSTERED
}