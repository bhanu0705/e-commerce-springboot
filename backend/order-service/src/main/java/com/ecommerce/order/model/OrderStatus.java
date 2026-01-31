package com.ecommerce.order.model;

/**
 * Order Status Enum - Represents the lifecycle of an order
 * 
 * Flow: PENDING → CONFIRMED → SHIPPED → DELIVERED
 *       or any state → CANCELLED
 */
public enum OrderStatus {
    PENDING,    // Order placed, awaiting confirmation
    CONFIRMED,  // Order confirmed, payment received
    SHIPPED,    // Order shipped
    DELIVERED,  // Order delivered to customer
    CANCELLED   // Order cancelled
}
