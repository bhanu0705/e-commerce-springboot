package com.ecommerce.order.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Order Item Entity - Represents a single product in an order
 * 
 * One Order has MANY OrderItems
 * Each OrderItem references ONE product (by ID)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * MANY-TO-ONE relationship with Order
     * 
     * @ManyToOne: Many items belong to ONE order
     * @JoinColumn: This entity has the foreign key column
     * 
     * @JsonIgnore: Prevent infinite recursion when serializing to JSON
     *   Order → items → Order → items → ... (infinite loop!)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore  // Prevent infinite JSON recursion
    private Order order;

    // Reference to product (stored as ID, not the full object)
    @Column(name = "product_id", nullable = false)
    private Long productId;

    // We denormalize product name for convenience
    // (so we don't have to call Product Service just to display order)
    @Column(name = "product_name")
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    // Price at time of order (products prices can change later)
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    // Calculated field: quantity * unitPrice
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
}
