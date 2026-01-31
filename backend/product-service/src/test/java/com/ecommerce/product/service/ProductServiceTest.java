package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductDto;
import com.ecommerce.product.dto.StockUpdateRequest;
import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
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
import static org.mockito.Mockito.*;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                    PRODUCT SERVICE UNIT TESTS                             ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  Unit tests focus on testing the SERVICE layer in ISOLATION              ║
 * ║                                                                           ║
 * ║  Key testing concepts demonstrated:                                       ║
 * ║  - @Mock: Create mock objects to replace real dependencies               ║
 * ║  - @InjectMocks: Inject mocks into the class under test                  ║
 * ║  - when().thenReturn(): Define mock behavior                             ║
 * ║  - verify(): Check that methods were called                              ║
 * ║  - AssertJ: Fluent assertions for readable tests                         ║
 * ║                                                                           ║
 * ║  INTERVIEW TIP:                                                           ║
 * ║  "I use Mockito for unit tests to isolate the service layer.             ║
 * ║   This lets me test business logic without needing a real database."     ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */
@ExtendWith(MockitoExtension.class)  // Enable Mockito annotations
class ProductServiceTest {

    @Mock  // Create a mock ProductRepository
    private ProductRepository productRepository;

    @InjectMocks  // Inject the mock into ProductService
    private ProductService productService;

    private Product testProduct;
    private ProductDto testProductDto;

    @BeforeEach
    void setUp() {
        // Create test data before each test
        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .category("Electronics")
                .build();

        testProductDto = ProductDto.builder()
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .category("Electronics")
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GET ALL PRODUCTS TESTS
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Get All Products")
    class GetAllProductsTests {

        @Test
        @DisplayName("Should return all products")
        void getAllProducts_ReturnsAllProducts() {
            // Arrange: Set up mock behavior
            when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct));

            // Act: Call the method under test
            List<ProductDto> result = productService.getAllProducts();

            // Assert: Verify the results
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Test Product");
            verify(productRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no products exist")
        void getAllProducts_WhenNoProducts_ReturnsEmptyList() {
            when(productRepository.findAll()).thenReturn(Arrays.asList());

            List<ProductDto> result = productService.getAllProducts();

            assertThat(result).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GET PRODUCT BY ID TESTS
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Get Product By ID")
    class GetProductByIdTests {

        @Test
        @DisplayName("Should return product when found")
        void getProductById_WhenProductExists_ReturnsProduct() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            ProductDto result = productService.getProductById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Test Product");
            assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void getProductById_WhenProductDoesNotExist_ThrowsException() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CREATE PRODUCT TESTS
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Create Product")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully")
        void createProduct_WithValidData_ReturnsCreatedProduct() {
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            ProductDto result = productService.createProduct(testProductDto);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Test Product");
            verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("Should set default stock quantity when not provided")
        void createProduct_WithoutStockQuantity_SetsDefaultValue() {
            ProductDto dtoWithoutStock = ProductDto.builder()
                    .name("Product Without Stock")
                    .price(new BigDecimal("50.00"))
                    .build();

            Product savedProduct = Product.builder()
                    .id(2L)
                    .name("Product Without Stock")
                    .price(new BigDecimal("50.00"))
                    .stockQuantity(0)
                    .build();

            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

            ProductDto result = productService.createProduct(dtoWithoutStock);

            assertThat(result.getStockQuantity()).isEqualTo(0);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UPDATE PRODUCT TESTS
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Update Product")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product successfully")
        void updateProduct_WhenProductExists_ReturnsUpdatedProduct() {
            ProductDto updateDto = ProductDto.builder()
                    .name("Updated Product")
                    .price(new BigDecimal("149.99"))
                    .stockQuantity(50)
                    .build();

            Product updatedProduct = Product.builder()
                    .id(1L)
                    .name("Updated Product")
                    .price(new BigDecimal("149.99"))
                    .stockQuantity(50)
                    .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

            ProductDto result = productService.updateProduct(1L, updateDto);

            assertThat(result.getName()).isEqualTo("Updated Product");
            assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("149.99"));
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent product")
        void updateProduct_WhenProductDoesNotExist_ThrowsException() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.updateProduct(999L, testProductDto))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UPDATE STOCK TESTS
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Update Stock")
    class UpdateStockTests {

        @Test
        @DisplayName("Should increase stock successfully")
        void updateStock_WithPositiveChange_IncreasesStock() {
            StockUpdateRequest request = new StockUpdateRequest(10);

            Product productWithUpdatedStock = Product.builder()
                    .id(1L)
                    .name("Test Product")
                    .stockQuantity(110)  // 100 + 10
                    .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(productWithUpdatedStock);

            ProductDto result = productService.updateStock(1L, request);

            assertThat(result.getStockQuantity()).isEqualTo(110);
        }

        @Test
        @DisplayName("Should decrease stock successfully")
        void updateStock_WithNegativeChange_DecreasesStock() {
            StockUpdateRequest request = new StockUpdateRequest(-20);

            Product productWithUpdatedStock = Product.builder()
                    .id(1L)
                    .name("Test Product")
                    .stockQuantity(80)  // 100 - 20
                    .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(productWithUpdatedStock);

            ProductDto result = productService.updateStock(1L, request);

            assertThat(result.getStockQuantity()).isEqualTo(80);
        }

        @Test
        @DisplayName("Should throw exception when stock goes negative")
        void updateStock_WhenResultingStockNegative_ThrowsException() {
            StockUpdateRequest request = new StockUpdateRequest(-150);  // More than available

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            assertThatThrownBy(() -> productService.updateStock(1L, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Insufficient stock");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DELETE PRODUCT TESTS
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Delete Product")
    class DeleteProductTests {

        @Test
        @DisplayName("Should delete product successfully")
        void deleteProduct_WhenProductExists_DeletesSuccessfully() {
            when(productRepository.existsById(1L)).thenReturn(true);
            doNothing().when(productRepository).deleteById(1L);

            productService.deleteProduct(1L);

            verify(productRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent product")
        void deleteProduct_WhenProductDoesNotExist_ThrowsException() {
            when(productRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> productService.deleteProduct(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
