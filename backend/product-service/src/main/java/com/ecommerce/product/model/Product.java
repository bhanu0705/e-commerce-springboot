package com.ecommerce.product.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                            PRODUCT ENTITY                                 ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  WHAT IS AN ENTITY?                                                       ║
 * ║  - A Java class that maps to a DATABASE TABLE                             ║
 * ║  - Each field becomes a COLUMN in the table                               ║
 * ║  - Each instance (object) is a ROW in the table                           ║
 * ║                                                                           ║
 * ║  JPA (Java Persistence API):                                              ║
 * ║  - Standard for ORM (Object-Relational Mapping) in Java                   ║
 * ║  - Hibernate is the implementation we use                                 ║
 * ║  - Write Java objects, JPA handles SQL automatically!                     ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */

/*
 * LOMBOK ANNOTATIONS (reduce boilerplate code):
 * 
 * @Data           - Generates: getters, setters, toString(), equals(), hashCode()
 * @NoArgsConstructor - Generates: public Product() {} (required by JPA)
 * @AllArgsConstructor - Generates: public Product(all fields...) {}  
 * @Builder        - Generates: Product.builder().name("x").price(10).build()
 * 
 * Without Lombok, you'd write 50+ lines of boring getter/setter code!
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

/*
 * JPA ANNOTATIONS:
 * 
 * @Entity - "This class represents a database table"
 * @Table  - Customize table name (optional, defaults to class name)
 */
@Entity
@Table(name = "products")
public class Product {

    /*
     * @Id - "This field is the PRIMARY KEY"
     * @GeneratedValue - "Database auto-generates this value"
     *   - IDENTITY strategy: Uses database's auto-increment
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * @Column - Customize column properties
     *   - nullable = false → This column cannot be NULL (required field)
     *   - length = 100 → Maximum 100 characters
     */
    @Column(nullable = false, length = 100)
    private String name;

    /*
     * For longer text, we can use @Column with length
     * Or use @Lob for very large text
     */
    @Column(length = 500)
    private String description;

    /*
     * BigDecimal for money!
     * - Never use double/float for currency (precision issues)
     * - BigDecimal handles exact decimal arithmetic
     * 
     * precision = 10: Total digits (including decimal)
     * scale = 2: Digits after decimal point
     * Example: 99999999.99 (10 digits total, 2 after decimal)
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /*
     * Simple integer for stock quantity
     * Default value = 0 (if not specified)
     */
    @Column(nullable = false)
    @Builder.Default  // Lombok: Use this default when using builder()
    private Integer stockQuantity = 0;

    /*
     * Product category for filtering
     */
    @Column(length = 50)
    private String category;

    /*
     * Audit fields: Track when records are created/modified
     * 
     * @PrePersist - Called automatically BEFORE saving a new entity
     * See onCreate() method below
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /*
     * LIFECYCLE CALLBACKS:
     * JPA calls these methods automatically at certain points
     */
    
    @PrePersist  // Called before INSERT
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate  // Called before UPDATE
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
