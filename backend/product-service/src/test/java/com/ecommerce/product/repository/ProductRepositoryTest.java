package com.ecommerce.product.repository;

import com.ecommerce.product.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                    PRODUCT REPOSITORY TESTS                               ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  @DataJpaTest - Loads only JPA components (Repository, EntityManager)    ║
 * ║                                                                           ║
 * ║  Tests the custom query methods in ProductRepository                     ║
 * ║  Uses H2 in-memory database for fast, isolated tests                     ║
 * ║                                                                           ║
 * ║  INTERVIEW TIP:                                                           ║
 * ║  "I use @DataJpaTest for repository tests. It's faster than loading      ║
 * ║   the full context and automatically rolls back after each test."        ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */
@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;  // For setting up test data

    @Autowired
    private ProductRepository productRepository;

    private Product electronicsProduct;
    private Product clothingProduct;

    @BeforeEach
    void setUp() {
        // Create test products
        electronicsProduct = Product.builder()
                .name("Laptop")
                .description("Gaming laptop")
                .price(new BigDecimal("999.99"))
                .stockQuantity(50)
                .category("Electronics")
                .build();

        clothingProduct = Product.builder()
                .name("T-Shirt")
                .description("Cotton t-shirt")
                .price(new BigDecimal("29.99"))
                .stockQuantity(100)
                .category("Clothing")
                .build();

        // Persist test data
        entityManager.persist(electronicsProduct);
        entityManager.persist(clothingProduct);
        entityManager.flush();
    }

    @Test
    @DisplayName("Find by category returns matching products")
    void findByCategory_ReturnsMatchingProducts() {
        List<Product> electronics = productRepository.findByCategory("Electronics");

        assertThat(electronics).hasSize(1);
        assertThat(electronics.get(0).getName()).isEqualTo("Laptop");
    }

    @Test
    @DisplayName("Find by category returns empty when no match")
    void findByCategory_WhenNoMatch_ReturnsEmpty() {
        List<Product> books = productRepository.findByCategory("Books");

        assertThat(books).isEmpty();
    }

    @Test
    @DisplayName("Find by name containing ignores case")
    void findByNameContainingIgnoreCase_IgnoresCase() {
        List<Product> results = productRepository.findByNameContainingIgnoreCase("LAPTOP");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Laptop");
    }

    @Test
    @DisplayName("Find by name containing finds partial matches")
    void findByNameContainingIgnoreCase_FindsPartialMatch() {
        List<Product> results = productRepository.findByNameContainingIgnoreCase("top");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Laptop");
    }

    @Test
    @DisplayName("Find by price between returns products in range")
    void findByPriceBetween_ReturnsProductsInRange() {
        List<Product> results = productRepository.findByPriceBetween(
                new BigDecimal("20.00"), 
                new BigDecimal("50.00")
        );

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("T-Shirt");
    }

    @Test
    @DisplayName("Find by stock quantity greater than returns matching products")
    void findByStockQuantityGreaterThan_ReturnsMatchingProducts() {
        List<Product> results = productRepository.findByStockQuantityGreaterThan(75);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("T-Shirt");
    }
}
