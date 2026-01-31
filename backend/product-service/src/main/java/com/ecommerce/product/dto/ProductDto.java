package com.ecommerce.product.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                        PRODUCT DTO (Data Transfer Object)                 ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  ENTITY vs DTO:                                                           ║
 * ║                                                                           ║
 * ║  ENTITY (Product.java):                                                   ║
 * ║  - Maps to database table                                                 ║
 * ║  - Contains ALL fields, including internal ones                           ║
 * ║  - Should NOT be exposed directly to API                                  ║
 * ║                                                                           ║
 * ║  DTO (ProductDto.java):                                                   ║
 * ║  - What we SEND/RECEIVE through the API                                   ║
 * ║  - Can hide sensitive fields (like internal IDs, passwords)               ║
 * ║  - Can combine data from multiple entities                                ║
 * ║  - Contains VALIDATION annotations                                        ║
 * ║                                                                           ║
 * ║  INTERVIEW TIP:                                                           ║
 * ║  "We use DTOs to decouple our internal domain model from the API          ║
 * ║   contract. This allows us to change the database schema without          ║
 * ║   breaking API clients, and vice versa."                                  ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {

    private Long id;  // For responses (not required in create requests)

    /*
     * VALIDATION ANNOTATIONS:
     * These are checked when @Valid is used on a controller method parameter
     * 
     * @NotBlank - Cannot be null, empty, or just whitespace
     * @Size     - String length constraints
     */
    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    /*
     * @NotNull  - Cannot be null (but can be 0)
     * @Positive - Must be greater than 0
     * @DecimalMin - Minimum decimal value (as string for precision)
     */
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stockQuantity;

    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;

    // Read-only fields (included in responses, ignored in requests)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
