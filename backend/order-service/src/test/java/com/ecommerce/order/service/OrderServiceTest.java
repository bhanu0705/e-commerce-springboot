package com.ecommerce.order.service;

import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.client.UserClient;
import com.ecommerce.order.dto.*;
import com.ecommerce.order.exception.ResourceNotFoundException;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                    ORDER SERVICE UNIT TESTS                               ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  MOST IMPORTANT TESTS FOR INTERVIEW!                                      ║
 * ║                                                                           ║
 * ║  Demonstrates testing with MOCKED FEIGN CLIENTS                          ║
 * ║  - We mock ProductClient and UserClient                                  ║
 * ║  - Tests the orchestration logic in OrderService                         ║
 * ║  - Shows how to handle Feign exceptions in tests                         ║
 * ║                                                                           ║
 * ║  INTERVIEW TIP:                                                           ║
 * ║  "For testing inter-service communication, I mock the Feign clients.     ║
 * ║   This lets me test the orchestration logic without needing the other    ║
 * ║   services to be running. I also test error scenarios like when a        ║
 * ║   dependent service is unavailable."                                      ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock  // Mock the Feign client for Product Service
    private ProductClient productClient;

    @Mock  // Mock the Feign client for User Service
    private UserClient userClient;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private OrderItem testOrderItem;
    private ProductDto testProduct;
    private UserDto testUser;

    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = UserDto.builder()
                .id(1L)
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .address("123 Main St")
                .build();

        // Set up test product
        testProduct = ProductDto.builder()
                .id(1L)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .category("Electronics")
                .build();

        // Set up test order item
        testOrderItem = OrderItem.builder()
                .id(1L)
                .productId(1L)
                .productName("Test Product")
                .quantity(2)
                .unitPrice(new BigDecimal("99.99"))
                .subtotal(new BigDecimal("199.98"))
                .build();

        // Set up test order
        testOrder = Order.builder()
                .id(1L)
                .userId(1L)
                .totalAmount(new BigDecimal("199.98"))
                .status(OrderStatus.PENDING)
                .shippingAddress("123 Main St")
                .items(Arrays.asList(testOrderItem))
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CREATE ORDER TESTS - Tests inter-service communication
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Create Order")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order successfully with valid data")
        void createOrder_WithValidData_ReturnsCreatedOrder() {
            // Arrange
            OrderRequest.OrderItemRequest itemRequest = OrderRequest.OrderItemRequest.builder()
                    .productId(1L)
                    .quantity(2)
                    .build();

            OrderRequest request = OrderRequest.builder()
                    .userId(1L)
                    .shippingAddress("123 Main St")
                    .items(Arrays.asList(itemRequest))
                    .build();

            // Mock Feign client calls
            when(userClient.getUser(1L)).thenReturn(testUser);
            when(productClient.getProduct(1L)).thenReturn(testProduct);
            when(productClient.updateStock(eq(1L), any(StockUpdateRequest.class)))
                    .thenReturn(testProduct);
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

            // Act
            OrderDto result = orderService.createOrder(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("199.98"));

            // Verify Feign clients were called
            verify(userClient, times(1)).getUser(1L);
            verify(productClient, times(1)).getProduct(1L);
            verify(productClient, times(1)).updateStock(eq(1L), any(StockUpdateRequest.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void createOrder_WhenUserNotFound_ThrowsException() {
            OrderRequest request = OrderRequest.builder()
                    .userId(999L)
                    .items(Arrays.asList(
                            OrderRequest.OrderItemRequest.builder()
                                    .productId(1L)
                                    .quantity(1)
                                    .build()
                    ))
                    .build();

            // Mock Feign client to throw exception (simulating 404 from User Service)
            when(userClient.getUser(999L))
                    .thenThrow(mock(FeignException.NotFound.class));

            assertThatThrownBy(() -> orderService.createOrder(request))
                    .isInstanceOf(FeignException.NotFound.class);

            // Verify product service was never called
            verify(productClient, never()).getProduct(anyLong());
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void createOrder_WhenProductNotFound_ThrowsException() {
            OrderRequest request = OrderRequest.builder()
                    .userId(1L)
                    .items(Arrays.asList(
                            OrderRequest.OrderItemRequest.builder()
                                    .productId(999L)
                                    .quantity(1)
                                    .build()
                    ))
                    .build();

            when(userClient.getUser(1L)).thenReturn(testUser);
            when(productClient.getProduct(999L))
                    .thenThrow(mock(FeignException.NotFound.class));

            assertThatThrownBy(() -> orderService.createOrder(request))
                    .isInstanceOf(FeignException.NotFound.class);
        }

        @Test
        @DisplayName("Should throw exception when insufficient stock")
        void createOrder_WhenInsufficientStock_ThrowsException() {
            ProductDto lowStockProduct = ProductDto.builder()
                    .id(1L)
                    .name("Low Stock Product")
                    .price(new BigDecimal("99.99"))
                    .stockQuantity(5)  // Only 5 in stock
                    .build();

            OrderRequest request = OrderRequest.builder()
                    .userId(1L)
                    .items(Arrays.asList(
                            OrderRequest.OrderItemRequest.builder()
                                    .productId(1L)
                                    .quantity(10)  // Requesting 10
                                    .build()
                    ))
                    .build();

            when(userClient.getUser(1L)).thenReturn(testUser);
            when(productClient.getProduct(1L)).thenReturn(lowStockProduct);

            assertThatThrownBy(() -> orderService.createOrder(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Insufficient stock");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GET ORDER TESTS
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Get Order By ID")
    class GetOrderByIdTests {

        @Test
        @DisplayName("Should return order when found")
        void getOrderById_WhenExists_ReturnsOrder() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(userClient.getUser(1L)).thenReturn(testUser);

            OrderDto result = orderService.getOrderById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUserEmail()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("Should throw exception when order not found")
        void getOrderById_WhenNotFound_ThrowsException() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getOrderById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should return order without user details when user service fails")
        void getOrderById_WhenUserServiceFails_ReturnsOrderWithoutUserDetails() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(userClient.getUser(1L))
                    .thenThrow(mock(FeignException.ServiceUnavailable.class));

            OrderDto result = orderService.getOrderById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUserEmail()).isNull();  // User details not available
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UPDATE ORDER STATUS TESTS
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Update Order Status")
    class UpdateOrderStatusTests {

        @Test
        @DisplayName("Should update status to CONFIRMED")
        void updateOrderStatus_ToConfirmed_UpdatesSuccessfully() {
            Order pendingOrder = Order.builder()
                    .id(1L)
                    .userId(1L)
                    .status(OrderStatus.PENDING)
                    .items(Arrays.asList())
                    .totalAmount(new BigDecimal("100.00"))
                    .build();

            Order confirmedOrder = Order.builder()
                    .id(1L)
                    .userId(1L)
                    .status(OrderStatus.CONFIRMED)
                    .items(Arrays.asList())
                    .totalAmount(new BigDecimal("100.00"))
                    .build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(confirmedOrder);
            when(userClient.getUser(1L)).thenReturn(testUser);

            OrderDto result = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

            assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Should restore stock when order is cancelled")
        void updateOrderStatus_ToCancelled_RestoresStock() {
            Order orderWithItems = Order.builder()
                    .id(1L)
                    .userId(1L)
                    .status(OrderStatus.PENDING)
                    .items(Arrays.asList(testOrderItem))
                    .totalAmount(new BigDecimal("199.98"))
                    .build();
            
            // Need to set the order reference on items
            testOrderItem.setOrder(orderWithItems);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(orderWithItems));
            when(productClient.updateStock(eq(1L), any(StockUpdateRequest.class)))
                    .thenReturn(testProduct);
            when(orderRepository.save(any(Order.class))).thenReturn(orderWithItems);
            when(userClient.getUser(1L)).thenReturn(testUser);

            orderService.updateOrderStatus(1L, OrderStatus.CANCELLED);

            // Verify stock was restored (positive quantity change)
            verify(productClient).updateStock(eq(1L), argThat(request -> 
                    request.getQuantityChange() > 0
            ));
        }

        @Test
        @DisplayName("Should throw exception when trying to modify delivered order")
        void updateOrderStatus_WhenDelivered_ThrowsException() {
            Order deliveredOrder = Order.builder()
                    .id(1L)
                    .status(OrderStatus.DELIVERED)
                    .build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(deliveredOrder));

            assertThatThrownBy(() -> 
                    orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot change status");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GET ORDERS BY USER ID TESTS
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Get Orders By User ID")
    class GetOrdersByUserIdTests {

        @Test
        @DisplayName("Should return orders for user")
        void getOrdersByUserId_ReturnsUserOrders() {
            when(userClient.getUser(1L)).thenReturn(testUser);
            when(orderRepository.findByUserId(1L)).thenReturn(Arrays.asList(testOrder));

            List<OrderDto> result = orderService.getOrdersByUserId(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void getOrdersByUserId_WhenUserNotFound_ThrowsException() {
            when(userClient.getUser(999L))
                    .thenThrow(mock(FeignException.NotFound.class));

            assertThatThrownBy(() -> orderService.getOrdersByUserId(999L))
                    .isInstanceOf(FeignException.NotFound.class);
        }
    }
}
