package com.ecommerce.product.repository;

import com.ecommerce.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                         PRODUCT REPOSITORY                                ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  THE MAGIC OF SPRING DATA JPA:                                            ║
 * ║                                                                           ║
 * ║  You write: Just an INTERFACE with method signatures                      ║
 * ║  Spring generates: Complete implementation with SQL queries!              ║
 * ║                                                                           ║
 * ║  JpaRepository<Product, Long>                                             ║
 * ║    ↑           ↑       ↑                                                  ║
 * ║    │           │       └── Type of the ID field (Long)                    ║
 * ║    │           └── Entity class (Product)                                 ║
 * ║    └── Spring Data interface (provides CRUD methods)                      ║
 * ║                                                                           ║
 * ║  METHODS YOU GET FOR FREE:                                                ║
 * ║  - save(entity)          → INSERT or UPDATE                               ║
 * ║  - findById(id)          → SELECT by primary key                          ║
 * ║  - findAll()             → SELECT all rows                                ║
 * ║  - deleteById(id)        → DELETE by primary key                          ║
 * ║  - count()               → COUNT rows                                     ║
 * ║  - existsById(id)        → Check if exists                                ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */

/*
 * @Repository - Tells Spring this is a data access component
 *   - Spring creates an implementation automatically
 *   - Also translates database exceptions to Spring exceptions
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /*
     * ═══════════════════════════════════════════════════════════════════════
     * QUERY DERIVATION: Spring creates SQL from method names!
     * ═══════════════════════════════════════════════════════════════════════
     * 
     * You write: findByCategory(String category)
     * Spring generates: SELECT * FROM products WHERE category = ?
     * 
     * How it works:
     * - findBy  → SELECT ... WHERE
     * - Category → category column
     * - String category → the ? parameter
     */
    
    // Find all products in a category
    List<Product> findByCategory(String category);

    /*
     * More examples of query derivation:
     * 
     * findByNameContaining("phone")
     * → SELECT * FROM products WHERE name LIKE '%phone%'
     * 
     * findByPriceLessThan(100)
     * → SELECT * FROM products WHERE price < 100
     * 
     * findByStockQuantityGreaterThan(0)
     * → SELECT * FROM products WHERE stock_quantity > 0
     */
    
    // Find products with name containing a keyword (case-insensitive search)
    List<Product> findByNameContainingIgnoreCase(String keyword);

    // Find products in a price range
    List<Product> findByPriceBetween(java.math.BigDecimal minPrice, 
                                      java.math.BigDecimal maxPrice);

    // Find products that are in stock
    List<Product> findByStockQuantityGreaterThan(Integer minStock);

    /*
     * You can also write custom queries using @Query annotation
     * if the method name gets too complex:
     * 
     * @Query("SELECT p FROM Product p WHERE p.price < :maxPrice AND p.stockQuantity > 0")
     * List<Product> findAffordableInStockProducts(@Param("maxPrice") BigDecimal maxPrice);
     */
}
