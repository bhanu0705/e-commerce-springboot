package com.ecommerce.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                    DISCOVERY SERVER - MAIN APPLICATION                    ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  This is the entry point for our Eureka Discovery Server.                ║
 * ║                                                                           ║
 * ║  HOW IT WORKS:                                                            ║
 * ║  1. When this starts, it creates a Eureka Server at port 8761            ║
 * ║  2. Other services (product, order, user) will register here             ║
 * ║  3. You can see all registered services at: http://localhost:8761        ║
 * ║                                                                           ║
 * ║  INTERVIEW TIP:                                                           ║
 * ║  "Eureka implements the Service Discovery pattern. Instead of hardcoding ║
 * ║   service URLs, services register themselves and discover each other     ║
 * ║   by name. This enables dynamic scaling and failover."                   ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */

/*
 * @SpringBootApplication - This is a COMBINATION of 3 annotations:
 *   1. @Configuration  - This class can define @Bean methods
 *   2. @EnableAutoConfiguration - Spring Boot auto-configures based on dependencies
 *   3. @ComponentScan - Automatically finds @Component, @Service, @Controller classes
 * 
 * Think of it as: "This is a Spring Boot app, set everything up automatically"
 */
@SpringBootApplication

/*
 * @EnableEurekaServer - This ONE annotation does all the magic!
 *   - Starts a Eureka Server
 *   - Creates a web dashboard at /
 *   - Creates REST endpoints for service registration
 *   - Handles heartbeats from registered services
 * 
 * Without this annotation, this would just be a regular Spring Boot app.
 * With it, it becomes a full-fledged service registry!
 */
@EnableEurekaServer
public class DiscoveryServerApplication {

    /*
     * main() - The entry point of any Java application
     * 
     * SpringApplication.run() does:
     *   1. Creates the Spring ApplicationContext (the container for all beans)
     *   2. Starts the embedded Tomcat server
     *   3. Scans for components and creates them
     *   4. Opens port 8761 (configured in application.yml)
     */
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
        
        // After startup, you'll see in console:
        // "Started DiscoveryServerApplication in X seconds"
        // Then visit http://localhost:8761 to see the dashboard!
    }
}
