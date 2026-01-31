package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderDto;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.exception.ResourceNotFoundException;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Order Controller Integration Tests
 */
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private OrderDto testOrderDto;

    @BeforeEach
    void setUp() {
        testOrderDto = OrderDto.builder()
                .id(1L)
                .userId(1L)
                .userName("John Doe")
                .userEmail("john@example.com")
                .totalAmount(new BigDecimal("199.98"))
                .status(OrderStatus.PENDING)
                .shippingAddress("123 Main St")
                .items(Arrays.asList(
                        OrderDto.OrderItemDto.builder()
                                .id(1L)
                                .productId(1L)
                                .productName("Test Product")
                                .quantity(2)
                                .unitPrice(new BigDecimal("99.99"))
                                .subtotal(new BigDecimal("199.98"))
                                .build()
                ))
                .build();
    }

    @Nested
    @DisplayName("GET /api/orders")
    class GetAllOrdersTests {

        @Test
        @DisplayName("Returns 200 and list of orders")
        void getAllOrders_ReturnsOrderList() throws Exception {
            when(orderService.getAllOrders()).thenReturn(Arrays.asList(testOrderDto));

            mockMvc.perform(get("/api/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", is(1)))
                    .andExpect(jsonPath("$[0].status", is("PENDING")));
        }
    }

    @Nested
    @DisplayName("GET /api/orders/{id}")
    class GetOrderByIdTests {

        @Test
        @DisplayName("Returns 200 and order when found")
        void getOrderById_WhenExists_ReturnsOrder() throws Exception {
            when(orderService.getOrderById(1L)).thenReturn(testOrderDto);

            mockMvc.perform(get("/api/orders/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.userName", is("John Doe")))
                    .andExpect(jsonPath("$.items", hasSize(1)));
        }

        @Test
        @DisplayName("Returns 404 when order not found")
        void getOrderById_WhenNotFound_Returns404() throws Exception {
            when(orderService.getOrderById(999L))
                    .thenThrow(new ResourceNotFoundException("Order not found"));

            mockMvc.perform(get("/api/orders/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/orders")
    class CreateOrderTests {

        @Test
        @DisplayName("Returns 201 when order created successfully")
        void createOrder_WithValidData_Returns201() throws Exception {
            OrderRequest request = OrderRequest.builder()
                    .userId(1L)
                    .shippingAddress("123 Main St")
                    .items(Arrays.asList(
                            OrderRequest.OrderItemRequest.builder()
                                    .productId(1L)
                                    .quantity(2)
                                    .build()
                    ))
                    .build();

            when(orderService.createOrder(any(OrderRequest.class))).thenReturn(testOrderDto);

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.totalAmount", is(199.98)));
        }

        @Test
        @DisplayName("Returns 400 when userId is missing")
        void createOrder_WithoutUserId_Returns400() throws Exception {
            OrderRequest invalidRequest = OrderRequest.builder()
                    .items(Arrays.asList(
                            OrderRequest.OrderItemRequest.builder()
                                    .productId(1L)
                                    .quantity(1)
                                    .build()
                    ))
                    .build();

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Returns 400 when items are empty")
        void createOrder_WithoutItems_Returns400() throws Exception {
            OrderRequest invalidRequest = OrderRequest.builder()
                    .userId(1L)
                    .build();

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Returns 400 when quantity is zero")
        void createOrder_WithZeroQuantity_Returns400() throws Exception {
            OrderRequest invalidRequest = OrderRequest.builder()
                    .userId(1L)
                    .items(Arrays.asList(
                            OrderRequest.OrderItemRequest.builder()
                                    .productId(1L)
                                    .quantity(0)  // Invalid
                                    .build()
                    ))
                    .build();

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/orders/{id}/status")
    class UpdateOrderStatusTests {

        @Test
        @DisplayName("Returns 200 when status updated successfully")
        void updateStatus_WithValidStatus_Returns200() throws Exception {
            OrderDto confirmedOrder = OrderDto.builder()
                    .id(1L)
                    .status(OrderStatus.CONFIRMED)
                    .build();

            when(orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED))
                    .thenReturn(confirmedOrder);

            mockMvc.perform(put("/api/orders/1/status")
                            .param("status", "CONFIRMED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("CONFIRMED")));
        }

        @Test
        @DisplayName("Returns 400 when status transition is invalid")
        void updateStatus_WithInvalidTransition_Returns400() throws Exception {
            when(orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED))
                    .thenThrow(new IllegalArgumentException("Cannot change status"));

            mockMvc.perform(put("/api/orders/1/status")
                            .param("status", "CONFIRMED"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/orders/user/{userId}")
    class GetOrdersByUserIdTests {

        @Test
        @DisplayName("Returns 200 and orders for user")
        void getOrdersByUserId_ReturnsUserOrders() throws Exception {
            when(orderService.getOrdersByUserId(1L))
                    .thenReturn(Arrays.asList(testOrderDto));

            mockMvc.perform(get("/api/orders/user/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].userId", is(1)));
        }
    }
}
