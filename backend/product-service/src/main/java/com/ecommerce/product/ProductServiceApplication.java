package com.ecommerce.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                    PRODUCT SERVICE - MAIN APPLICATION                     ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  RESPONSIBILITY:                                                          ║
 * ║  - Manage product catalog (CRUD operations)                               ║
 * ║  - Handle product inventory/stock                                         ║
 * ║  - Provide product information to other services                          ║
 * ║                                                                           ║
 * ║  LAYERED ARCHITECTURE (Clean Code Pattern):                               ║
 * ║  ┌─────────────────┐                                                      ║
 * ║  │   Controller    │  ← Handles HTTP requests, returns responses          ║
 * ║  ├─────────────────┤                                                      ║
 * ║  │    Service      │  ← Business logic lives here                         ║
 * ║  ├─────────────────┤                                                      ║
 * ║  │   Repository    │  ← Database operations                               ║
 * ║  ├─────────────────┤                                                      ║
 * ║  │     Entity      │  ← Maps to database table                            ║
 * ║  └─────────────────┘                                                      ║
 * ║                                                                           ║
 * ║  WHY LAYERS?                                                              ║
 * ║  - Separation of concerns (each layer has ONE job)                        ║
 * ║  - Easy to test (mock one layer, test another)                            ║
 * ║  - Easy to change (swap database without touching controller)             ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */
@SpringBootApplication
@EnableDiscoveryClient  // Register with Eureka
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
