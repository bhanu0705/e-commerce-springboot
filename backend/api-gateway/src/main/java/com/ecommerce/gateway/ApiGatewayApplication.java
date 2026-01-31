package com.ecommerce.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                      API GATEWAY - MAIN APPLICATION                       ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  THE GATEWAY PATTERN:                                                     ║
 * ║                                                                           ║
 * ║  Without Gateway:                                                         ║
 * ║    Frontend → Product Service (8081)                                      ║
 * ║    Frontend → Order Service (8082)                                        ║
 * ║    Frontend → User Service (8083)                                         ║
 * ║    (Frontend needs to know ALL service URLs!)                             ║
 * ║                                                                           ║
 * ║  With Gateway:                                                            ║
 * ║    Frontend → Gateway (8080) → Routes to correct service                  ║
 * ║    (Frontend only knows ONE URL!)                                         ║
 * ║                                                                           ║
 * ║  INTERVIEW TIP:                                                           ║
 * ║  "The API Gateway is the single entry point for all client requests.      ║
 * ║   It provides routing, load balancing, and cross-cutting concerns like    ║
 * ║   authentication. This decouples clients from knowing internal service    ║
 * ║   locations and allows us to change internal architecture freely."        ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */

@SpringBootApplication

/*
 * @EnableDiscoveryClient - Tells Spring to register with Eureka
 *   - This service will show up in Eureka dashboard
 *   - It can also DISCOVER other services by name
 *   - Example: Route to "product-service" → Eureka tells Gateway the URL
 */
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
        
        // After startup:
        // - Gateway runs on port 8080
        // - Registers itself with Eureka
        // - Starts routing requests based on application.yml config
    }
}
