package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductDto;
import com.ecommerce.product.dto.StockUpdateRequest;
import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.service.ProductService;
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
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                 PRODUCT CONTROLLER INTEGRATION TESTS                      ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  These tests verify the REST API endpoints                               ║
 * ║                                                                           ║
 * ║  @WebMvcTest - Loads only the web layer (not full app context)           ║
 * ║  MockMvc - Simulates HTTP requests to the controller                     ║
 * ║                                                                           ║
 * ║  What we test:                                                            ║
 * ║  - HTTP status codes (200, 201, 400, 404)                                ║
 * ║  - Response body content                                                  ║
 * ║  - Request validation                                                     ║
 * ║  - Error handling                                                         ║
 * ║                                                                           ║
 * ║  INTERVIEW TIP:                                                           ║
 * ║  "I use @WebMvcTest for controller tests as it loads only the web        ║
 * ║   layer, making tests fast. I mock the service layer to focus on         ║
 * ║   testing HTTP behavior and validation."                                  ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;  // Simulates HTTP requests

    @Autowired
    private ObjectMapper objectMapper;  // Converts objects to/from JSON

    @MockBean  // Mocks the service layer
    private ProductService productService;

    private ProductDto testProductDto;

    @BeforeEach
    void setUp() {
        testProductDto = ProductDto.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .category("Electronics")
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GET ENDPOINTS
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/products")
    class GetAllProductsTests {

        @Test
        @DisplayName("Returns 200 and list of products")
        void getAllProducts_ReturnsProductList() throws Exception {
            when(productService.getAllProducts()).thenReturn(Arrays.asList(testProductDto));

            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name", is("Test Product")))
                    .andExpect(jsonPath("$[0].price", is(99.99)));
        }

        @Test
        @DisplayName("Returns 200 and empty list when no products")
        void getAllProducts_WhenEmpty_ReturnsEmptyList() throws Exception {
            when(productService.getAllProducts()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/products/{id}")
    class GetProductByIdTests {

        @Test
        @DisplayName("Returns 200 and product when found")
        void getProductById_WhenExists_ReturnsProduct() throws Exception {
            when(productService.getProductById(1L)).thenReturn(testProductDto);

            mockMvc.perform(get("/api/products/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Test Product")));
        }

        @Test
        @DisplayName("Returns 404 when product not found")
        void getProductById_WhenNotFound_Returns404() throws Exception {
            when(productService.getProductById(999L))
                    .thenThrow(new ResourceNotFoundException("Product not found"));

            mockMvc.perform(get("/api/products/999"))
                    .andExpect(status().isNotFound());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // POST ENDPOINT
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /api/products")
    class CreateProductTests {

        @Test
        @DisplayName("Returns 201 when product created successfully")
        void createProduct_WithValidData_Returns201() throws Exception {
            ProductDto inputDto = ProductDto.builder()
                    .name("New Product")
                    .price(new BigDecimal("49.99"))
                    .stockQuantity(50)
                    .build();

            when(productService.createProduct(any(ProductDto.class))).thenReturn(testProductDto);

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is("Test Product")));
        }

        @Test
        @DisplayName("Returns 400 when name is missing")
        void createProduct_WithoutName_Returns400() throws Exception {
            ProductDto invalidDto = ProductDto.builder()
                    .price(new BigDecimal("49.99"))
                    .build();

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Returns 400 when price is negative")
        void createProduct_WithNegativePrice_Returns400() throws Exception {
            ProductDto invalidDto = ProductDto.builder()
                    .name("Product")
                    .price(new BigDecimal("-10.00"))
                    .build();

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PUT ENDPOINT
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /api/products/{id}")
    class UpdateProductTests {

        @Test
        @DisplayName("Returns 200 when product updated successfully")
        void updateProduct_WithValidData_Returns200() throws Exception {
            ProductDto updateDto = ProductDto.builder()
                    .name("Updated Product")
                    .price(new BigDecimal("129.99"))
                    .stockQuantity(75)
                    .build();

            when(productService.updateProduct(eq(1L), any(ProductDto.class))).thenReturn(updateDto);

            mockMvc.perform(put("/api/products/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Updated Product")));
        }

        @Test
        @DisplayName("Returns 404 when updating non-existent product")
        void updateProduct_WhenNotFound_Returns404() throws Exception {
            when(productService.updateProduct(eq(999L), any(ProductDto.class)))
                    .thenThrow(new ResourceNotFoundException("Product not found"));

            mockMvc.perform(put("/api/products/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testProductDto)))
                    .andExpect(status().isNotFound());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PATCH STOCK ENDPOINT
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PATCH /api/products/{id}/stock")
    class UpdateStockTests {

        @Test
        @DisplayName("Returns 200 when stock updated successfully")
        void updateStock_WithValidChange_Returns200() throws Exception {
            StockUpdateRequest request = new StockUpdateRequest(10);
            ProductDto updatedProduct = ProductDto.builder()
                    .id(1L)
                    .name("Test Product")
                    .stockQuantity(110)
                    .build();

            when(productService.updateStock(eq(1L), any(StockUpdateRequest.class)))
                    .thenReturn(updatedProduct);

            mockMvc.perform(patch("/api/products/1/stock")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.stockQuantity", is(110)));
        }

        @Test
        @DisplayName("Returns 400 when stock change causes negative stock")
        void updateStock_WhenInsufficientStock_Returns400() throws Exception {
            StockUpdateRequest request = new StockUpdateRequest(-200);

            when(productService.updateStock(eq(1L), any(StockUpdateRequest.class)))
                    .thenThrow(new IllegalArgumentException("Insufficient stock"));

            mockMvc.perform(patch("/api/products/1/stock")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DELETE ENDPOINT
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DELETE /api/products/{id}")
    class DeleteProductTests {

        @Test
        @DisplayName("Returns 204 when product deleted successfully")
        void deleteProduct_WhenExists_Returns204() throws Exception {
            doNothing().when(productService).deleteProduct(1L);

            mockMvc.perform(delete("/api/products/1"))
                    .andExpect(status().isNoContent());

            verify(productService, times(1)).deleteProduct(1L);
        }

        @Test
        @DisplayName("Returns 404 when product not found")
        void deleteProduct_WhenNotFound_Returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Product not found"))
                    .when(productService).deleteProduct(999L);

            mockMvc.perform(delete("/api/products/999"))
                    .andExpect(status().isNotFound());
        }
    }
}
