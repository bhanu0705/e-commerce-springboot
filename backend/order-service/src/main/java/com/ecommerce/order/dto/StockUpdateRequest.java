package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stock Update Request - Matches Product Service's expected format
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateRequest {
    private Integer quantityChange;
}
