package com.ecommerce.order.service;

import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.client.UserClient;
import com.ecommerce.order.dto.*;
import com.ecommerce.order.exception.ResourceNotFoundException;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                         ORDER SERVICE                                     ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  THIS IS THE MOST IMPORTANT CLASS FOR YOUR INTERVIEW!                    ║
 * ║                                                                           ║
 * ║  It demonstrates:                                                         ║
 * ║  1. Inter-service communication using Feign                              ║
 * ║  2. Business logic orchestration                                         ║
 * ║  3. Transaction management                                                ║
 * ║  4. Error handling across services                                        ║
 * ║                                                                           ║
 * ║  INTERVIEW TIP:                                                           ║
 * ║  "When an order is placed, the Order Service acts as an orchestrator.    ║
 * ║   It verifies the user exists, validates product availability, creates   ║
 * ║   the order, and updates stock levels - all while maintaining data       ║
 * ║   consistency through proper transaction handling."                       ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */
@Service
@RequiredArgsConstructor
@Slf4j  // Lombok: Creates a logger named 'log'
public class OrderService {

    private final OrderRepository orderRepository;
    
    /*
     * FEIGN CLIENTS - For calling other services
     * 
     * These are INJECTED by Spring (constructor injection)
     * Feign creates the implementations automatically!
     */
    private final ProductClient productClient;  // Calls Product Service
    private final UserClient userClient;        // Calls User Service

    // ═══════════════════════════════════════════════════════════════════════
    // CREATE ORDER - The main orchestration method
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Create a new order
     * 
     * FLOW:
     * 1. Verify user exists (call User Service)
     * 2. For each item:
     *    a. Get product details (call Product Service)
     *    b. Validate stock is available
     * 3. Create order entity
     * 4. Update stock for all products (call Product Service)
     * 5. Save order
     * 6. Return order with all details
     * 
     * TRANSACTION:
     * If anything fails after stock update, we have a problem!
     * In a real system, you'd use:
     * - Saga pattern (compensating transactions)
     * - Or 2-phase commit
     * - Or messaging (eventual consistency)
     */
    @Transactional
    public OrderDto createOrder(OrderRequest request) {
        log.info("Creating order for user: {}", request.getUserId());
        
        // ─────────────────────────────────────────────────────────────────────
        // STEP 1: Verify user exists
        // ─────────────────────────────────────────────────────────────────────
        UserDto user = userClient.getUser(request.getUserId());
        log.info("User verified: {}", user.getEmail());
        
        // ─────────────────────────────────────────────────────────────────────
        // STEP 2: Validate all products and calculate total
        // ─────────────────────────────────────────────────────────────────────
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // Create the order entity
        Order order = Order.builder()
                .userId(request.getUserId())
                .shippingAddress(request.getShippingAddress() != null 
                        ? request.getShippingAddress() 
                        : user.getAddress())
                .status(OrderStatus.PENDING)
                .build();
        
        // Process each order item
        for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            // Call Product Service to get product details
            ProductDto product = productClient.getProduct(itemRequest.getProductId());
            
            // Validate stock
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException(
                    "Insufficient stock for product: " + product.getName() + 
                    ". Available: " + product.getStockQuantity() + 
                    ", Requested: " + itemRequest.getQuantity()
                );
            }
            
            // Calculate subtotal
            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            
            // Create order item
            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .build();
            
            order.addItem(orderItem);
            totalAmount = totalAmount.add(subtotal);
        }
        
        order.setTotalAmount(totalAmount);
        
        // ─────────────────────────────────────────────────────────────────────
        // STEP 3: Update stock for all products
        // ─────────────────────────────────────────────────────────────────────
        for (OrderItem item : order.getItems()) {
            StockUpdateRequest stockUpdate = new StockUpdateRequest(-item.getQuantity());
            productClient.updateStock(item.getProductId(), stockUpdate);
            log.info("Stock updated for product {}: -{}", item.getProductId(), item.getQuantity());
        }
        
        // ─────────────────────────────────────────────────────────────────────
        // STEP 4: Save order
        // ─────────────────────────────────────────────────────────────────────
        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {}", savedOrder.getId());
        
        return mapToDto(savedOrder, user);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // READ OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════

    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(order -> {
                    try {
                        UserDto user = userClient.getUser(order.getUserId());
                        return mapToDto(order, user);
                    } catch (Exception e) {
                        // If user service is unavailable, return order without user details
                        return mapToDto(order, null);
                    }
                })
                .collect(Collectors.toList());
    }

    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forOrder(id));
        
        UserDto user = null;
        try {
            user = userClient.getUser(order.getUserId());
        } catch (Exception e) {
            log.warn("Could not fetch user details for user ID: {}", order.getUserId());
        }
        
        return mapToDto(order, user);
    }

    public List<OrderDto> getOrdersByUserId(Long userId) {
        // Verify user exists first
        UserDto user = userClient.getUser(userId);
        
        return orderRepository.findByUserId(userId).stream()
                .map(order -> mapToDto(order, user))
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UPDATE ORDER STATUS
    // ═══════════════════════════════════════════════════════════════════════

    @Transactional
    public OrderDto updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forOrder(id));
        
        // Validate status transition (simple validation)
        if (order.getStatus() == OrderStatus.DELIVERED || 
            order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException(
                "Cannot change status of a " + order.getStatus() + " order"
            );
        }
        
        // If cancelling, restore stock
        if (newStatus == OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                StockUpdateRequest stockUpdate = new StockUpdateRequest(item.getQuantity());
                productClient.updateStock(item.getProductId(), stockUpdate);
                log.info("Stock restored for product {}: +{}", item.getProductId(), item.getQuantity());
            }
        }
        
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        
        UserDto user = null;
        try {
            user = userClient.getUser(order.getUserId());
        } catch (Exception e) {
            log.warn("Could not fetch user details");
        }
        
        return mapToDto(updatedOrder, user);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MAPPING
    // ═══════════════════════════════════════════════════════════════════════

    private OrderDto mapToDto(Order order, UserDto user) {
        List<OrderDto.OrderItemDto> itemDtos = order.getItems().stream()
                .map(item -> OrderDto.OrderItemDto.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());
        
        return OrderDto.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .userEmail(user != null ? user.getEmail() : null)
                .userName(user != null ? user.getFirstName() + " " + user.getLastName() : null)
                .items(itemDtos)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
