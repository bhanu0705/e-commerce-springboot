package com.ecommerce.order.client;

import com.ecommerce.order.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign Client for User Service
 * 
 * Used to verify user exists before placing an order
 */
@FeignClient(name = "user-service")
public interface UserClient {

    /**
     * Get user by ID
     * 
     * If user doesn't exist, Product Service returns 404
     * Feign throws FeignException which we handle in OrderService
     */
    @GetMapping("/api/users/{id}")
    UserDto getUser(@PathVariable("id") Long id);
}
