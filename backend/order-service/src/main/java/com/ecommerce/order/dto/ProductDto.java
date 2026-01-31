package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product DTO - Matches the response from Product Service
 * 
 * NOTE: This is a COPY of ProductDto from Product Service
 * In a larger project, you might:
 * - Create a shared library with common DTOs
 * - Or just duplicate (simpler, but requires keeping in sync)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
