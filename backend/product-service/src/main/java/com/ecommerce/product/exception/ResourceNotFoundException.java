package com.ecommerce.product.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                    RESOURCE NOT FOUND EXCEPTION                           ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  Custom exception for when a requested resource doesn't exist.            ║
 * ║                                                                           ║
 * ║  WHY CUSTOM EXCEPTIONS?                                                   ║
 * ║  - More meaningful than generic exceptions                                ║
 * ║  - Can be mapped to specific HTTP status codes                           ║
 * ║  - Cleaner error handling in service layer                               ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */

/*
 * @ResponseStatus - Automatically returns this HTTP status when thrown
 * 
 * Common HTTP Status codes:
 * - 200 OK           → Success
 * - 201 Created      → Resource created
 * - 400 Bad Request  → Invalid input
 * - 404 Not Found    → Resource doesn't exist
 * - 500 Server Error → Something went wrong
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    /*
     * Convenient static factory method for common use case
     * Usage: throw ResourceNotFoundException.forProduct(productId);
     */
    public static ResourceNotFoundException forProduct(Long productId) {
        return new ResourceNotFoundException("Product not found with id: " + productId);
    }
}
