package com.ecommerce.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                    ORDER SERVICE - MAIN APPLICATION                       ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  THIS IS THE CORE SERVICE FOR DEMONSTRATING MICROSERVICES                ║
 * ║                                                                           ║
 * ║  What makes this special:                                                 ║
 * ║  1. It CALLS other services (Product, User)                              ║
 * ║  2. Uses Feign for declarative HTTP clients                              ║
 * ║  3. Demonstrates service-to-service communication                        ║
 * ║                                                                           ║
 * ║  ORDER FLOW:                                                              ║
 * ║  1. User places order → Order Service receives request                   ║
 * ║  2. Order Service calls User Service → Verify user exists                ║
 * ║  3. Order Service calls Product Service → Verify products, get prices    ║
 * ║  4. Order Service calls Product Service → Decrease stock                 ║
 * ║  5. Order Service saves order to database                                ║
 * ║  6. Returns order confirmation                                           ║
 * ║                                                                           ║
 * ║  INTERVIEW TIP:                                                           ║
 * ║  "The Order Service orchestrates the order placement flow by calling     ║
 * ║   multiple services. I use synchronous communication with Feign here,    ║
 * ║   but for better resilience, we could use async messaging with RabbitMQ  ║
 * ║   or Kafka for some steps."                                               ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */

@SpringBootApplication
@EnableDiscoveryClient

/*
 * @EnableFeignClients - ESSENTIAL for Feign to work!
 * 
 * This annotation:
 * - Scans for interfaces annotated with @FeignClient
 * - Creates implementations for them
 * - Registers them as Spring beans
 * 
 * Without this, your @FeignClient interfaces won't work!
 */
@EnableFeignClients
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
