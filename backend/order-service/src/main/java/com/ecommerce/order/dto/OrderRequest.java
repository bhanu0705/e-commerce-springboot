package com.ecommerce.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                     ORDER REQUEST (Create Order)                          ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  This is what the frontend sends when placing an order                   ║
 * ║                                                                           ║
 * ║  Example Request:                                                         ║
 * ║  POST /api/orders                                                         ║
 * ║  {                                                                        ║
 * ║    "userId": 1,                                                           ║
 * ║    "shippingAddress": "123 Main St, City",                               ║
 * ║    "items": [                                                             ║
 * ║      { "productId": 1, "quantity": 2 },                                  ║
 * ║      { "productId": 3, "quantity": 1 }                                   ║
 * ║    ]                                                                      ║
 * ║  }                                                                        ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    private String shippingAddress;

    @NotNull(message = "Order must have at least one item")
    @Valid  // Validates nested OrderItemRequest objects too
    private List<OrderItemRequest> items;

    /**
     * Nested class for order items in the request
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemRequest {
        
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }
}
