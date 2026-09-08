package com.seu.petadoptionservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_item_id", nullable = false)
    private StoreItem storeItem;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice; // Snapshot price at time of purchase

    /** Convenience accessor for templates: avoids ambiguous BigDecimal.valueOf overload resolution in SpEL. */
    public BigDecimal getLineTotal() {
        return unitPrice.multiply(new BigDecimal(quantity));
    }
}
