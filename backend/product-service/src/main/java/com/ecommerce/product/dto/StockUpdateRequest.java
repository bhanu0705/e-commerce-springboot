package com.ecommerce.product.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                          STOCK UPDATE REQUEST                             ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  A focused DTO for stock updates only.                                    ║
 * ║                                                                           ║
 * ║  WHY A SEPARATE DTO?                                                      ║
 * ║  - More specific than using full ProductDto                               ║
 * ║  - Clearer API: PATCH /products/{id}/stock                                ║
 * ║  - Quantity can be negative (to decrease stock on order)                  ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateRequest {

    /*
     * Change in quantity:
     * - Positive: Add to stock (restock)
     * - Negative: Remove from stock (order placed)
     * 
     * Example: quantityChange = -2 means "reduce stock by 2"
     */
    @NotNull(message = "Quantity change is required")
    private Integer quantityChange;
}
