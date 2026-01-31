package com.ecommerce.order.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                            ORDER ENTITY                                   ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  An order contains:                                                       ║
 * ║  - User ID (who placed the order)                                        ║
 * ║  - List of order items (products and quantities)                         ║
 * ║  - Total amount                                                          ║
 * ║  - Order status (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)      ║
 * ║                                                                           ║
 * ║  NOTE: We store userId, not the full User object                         ║
 * ║        This is the "database-per-service" pattern                        ║
 * ║        Each service owns its own data, references others by ID           ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orders")  // "order" is a reserved SQL keyword
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * We store the USER ID, not the User object
     * Why? Because User data is in a DIFFERENT DATABASE (User Service)
     * 
     * When we need user details, we call User Service via Feign
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /*
     * ONE-TO-MANY relationship with OrderItem
     * 
     * CascadeType.ALL: When we save/delete Order, items are also saved/deleted
     * orphanRemoval: If we remove an item from the list, delete it from DB
     * 
     * @OneToMany - One order can have MANY items
     * mappedBy = "order" - The OrderItem entity has a field called "order"
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /*
     * @Enumerated - Store enum as string in database
     * EnumType.STRING: Stores "PENDING", "CONFIRMED" etc.
     * EnumType.ORDINAL: Stores 0, 1, 2 etc. (fragile if enum order changes!)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "shipping_address")
    private String shippingAddress;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Helper method to add items to order
     * Maintains bidirectional relationship
     */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);  // Set the back-reference
    }
}
