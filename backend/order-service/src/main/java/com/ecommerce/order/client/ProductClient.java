package com.ecommerce.order.client;

import com.ecommerce.order.dto.ProductDto;
import com.ecommerce.order.dto.StockUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                          PRODUCT CLIENT (Feign)                           ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  THIS IS THE MAGIC OF FEIGN!                                              ║
 * ║                                                                           ║
 * ║  You write: Just an interface with method signatures                      ║
 * ║  Feign generates: Complete HTTP client implementation!                    ║
 * ║                                                                           ║
 * ║  HOW IT WORKS:                                                            ║
 * ║  ┌────────────────────────────────────────────────────────────────────┐   ║
 * ║  │ Your Code                      What Feign Does                     │   ║
 * ║  ├────────────────────────────────────────────────────────────────────┤   ║
 * ║  │ productClient.getProduct(5)    1. Asks Eureka: Where is            │   ║
 * ║  │                                   "product-service"?               │   ║
 * ║  │                                2. Gets: http://192.168.1.5:8081    │   ║
 * ║  │                                3. Makes HTTP GET to:               │   ║
 * ║  │                                   http://192.168.1.5:8081/api/     │   ║
 * ║  │                                   products/5                       │   ║
 * ║  │                                4. Deserializes JSON to ProductDto  │   ║
 * ║  └────────────────────────────────────────────────────────────────────┘   ║
 * ║                                                                           ║
 * ║  INTERVIEW TIP:                                                           ║
 * ║  "Feign provides a declarative way to create HTTP clients. Combined      ║
 * ║   with Eureka, it uses service names instead of URLs, enabling dynamic   ║
 * ║   service discovery and load balancing across multiple instances."       ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */

/*
 * @FeignClient Configuration:
 * 
 * name = "product-service"
 *   - This is the name registered in Eureka
 *   - Feign asks Eureka: "Where is product-service?"
 *   - Gets the actual URL dynamically
 * 
 * If product-service has multiple instances, Feign automatically
 * load-balances between them!
 */
@FeignClient(name = "product-service")
public interface ProductClient {

    /**
     * Get product by ID
     * 
     * Equivalent to: GET http://product-service/api/products/{id}
     * 
     * The @PathVariable works just like in controllers
     */
    @GetMapping("/api/products/{id}")
    ProductDto getProduct(@PathVariable("id") Long id);

    /**
     * Update product stock
     * 
     * Called when order is placed to decrease stock
     * 
     * Equivalent to: PATCH http://product-service/api/products/{id}/stock
     * Body: { "quantityChange": -2 }
     */
    @PutMapping("/api/products/{id}/stock")
    ProductDto updateStock(@PathVariable("id") Long id, @RequestBody StockUpdateRequest request);
}
