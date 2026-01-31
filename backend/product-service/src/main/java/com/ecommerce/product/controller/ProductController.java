package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductDto;
import com.ecommerce.product.dto.StockUpdateRequest;
import com.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                         PRODUCT CONTROLLER                                ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  THE CONTROLLER LAYER:                                                    ║
 * ║  - Handles HTTP requests (GET, POST, PUT, DELETE)                        ║
 * ║  - Delegates to Service for business logic                               ║
 * ║  - Returns appropriate HTTP responses                                     ║
 * ║                                                                           ║
 * ║  REST API DESIGN:                                                         ║
 * ║  ┌────────────┬───────────────────┬─────────────────────────────────────┐ ║
 * ║  │ HTTP Verb  │ Endpoint          │ Action                              │ ║
 * ║  ├────────────┼───────────────────┼─────────────────────────────────────┤ ║
 * ║  │ GET        │ /api/products     │ Get all products                    │ ║
 * ║  │ GET        │ /api/products/1   │ Get product with id=1               │ ║
 * ║  │ POST       │ /api/products     │ Create new product                  │ ║
 * ║  │ PUT        │ /api/products/1   │ Update product with id=1            │ ║
 * ║  │ DELETE     │ /api/products/1   │ Delete product with id=1            │ ║
 * ║  │ PATCH      │ /api/products/1/stock │ Update only stock quantity      │ ║
 * ║  └────────────┴───────────────────┴─────────────────────────────────────┘ ║
 * ║                                                                           ║
 * ║  INTERVIEW TIP:                                                           ║
 * ║  "I follow RESTful conventions: nouns for endpoints, HTTP verbs for       ║
 * ║   actions. The controller is kept thin - it only handles HTTP concerns.   ║
 * ║   All business logic is in the service layer for reusability."            ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */

/*
 * @RestController = @Controller + @ResponseBody
 *   - @Controller: This class handles web requests
 *   - @ResponseBody: Return values are serialized to JSON (not view names)
 * 
 * @RequestMapping("/api/products")
 *   - All methods in this class are under /api/products
 *   - Method annotations add to this base path
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    // Injected by Spring (constructor injection via @RequiredArgsConstructor)
    private final ProductService productService;

    // ═══════════════════════════════════════════════════════════════════════
    // GET ENDPOINTS (Read operations)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * GET /api/products
     * Retrieve all products
     * 
     * @GetMapping - Handle GET requests to /api/products
     * Returns: 200 OK with list of products
     */
    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<ProductDto> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
        // ResponseEntity.ok(body) = status 200 + body
    }

    /**
     * GET /api/products/1
     * Retrieve single product by ID
     * 
     * @PathVariable - Extracts {id} from URL
     * Example: GET /api/products/5 → id = 5
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        ProductDto product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * GET /api/products/search?keyword=phone
     * Search products by name
     * 
     * @RequestParam - Extracts query parameter from URL
     * Example: /api/products/search?keyword=phone → keyword = "phone"
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductDto>> searchProducts(
            @RequestParam String keyword) {
        List<ProductDto> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/category/Electronics
     * Get products by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDto>> getProductsByCategory(
            @PathVariable String category) {
        List<ProductDto> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // POST ENDPOINTS (Create operations)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * POST /api/products
     * Create a new product
     * 
     * @RequestBody - Deserializes JSON body to ProductDto object
     * @Valid - Triggers validation annotations on ProductDto
     * 
     * Request Body Example:
     * {
     *   "name": "iPhone 15",
     *   "description": "Latest iPhone",
     *   "price": 999.99,
     *   "stockQuantity": 100,
     *   "category": "Electronics"
     * }
     * 
     * Returns: 201 Created with created product
     */
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(
            @Valid @RequestBody ProductDto productDto) {
        ProductDto createdProduct = productService.createProduct(productDto);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
        // Status 201 (Created) is more correct than 200 for POST
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PUT/PATCH ENDPOINTS (Update operations)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * PUT /api/products/1
     * Update entire product
     * 
     * PUT vs PATCH:
     * - PUT: Replace the entire resource (all fields required)
     * - PATCH: Partial update (only some fields)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDto productDto) {
        ProductDto updatedProduct = productService.updateProduct(id, productDto);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * PATCH /api/products/1/stock
     * Update only stock quantity
     * 
     * This endpoint is called by Order Service when:
     * - Order placed → decrease stock (negative quantity)
     * - Order cancelled → increase stock (positive quantity)
     * 
     * Request Body:
     * { "quantityChange": -5 }  // Reduce stock by 5
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductDto> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        ProductDto updatedProduct = productService.updateStock(id, request);
        return ResponseEntity.ok(updatedProduct);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DELETE ENDPOINTS (Delete operations)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * DELETE /api/products/1
     * Delete a product
     * 
     * Returns: 204 No Content (success but no body to return)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
        // Status 204 = "I did it successfully, nothing to show you"
    }
}
