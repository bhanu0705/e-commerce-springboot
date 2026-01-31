package com.ecommerce.order.repository;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Find all orders for a specific user
    List<Order> findByUserId(Long userId);
    
    // Find orders by status
    List<Order> findByStatus(OrderStatus status);
    
    // Find orders for a user with specific status
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);
}
