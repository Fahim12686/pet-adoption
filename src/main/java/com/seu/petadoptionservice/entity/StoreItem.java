package com.seu.petadoptionservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "store_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private String imageUrl;

    @Column(nullable = false)
    private String category; // FOOD, TOYS, ACCESSORIES, GROOMING, BEDS

    @Column(nullable = false)
    @Builder.Default
    private Integer stockQty = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
