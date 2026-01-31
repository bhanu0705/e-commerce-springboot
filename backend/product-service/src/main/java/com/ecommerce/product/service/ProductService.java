package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductDto;
import com.ecommerce.product.dto.StockUpdateRequest;
import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                           PRODUCT SERVICE                                 ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  THE SERVICE LAYER:                                                       ║
 * ║  - Contains ALL business logic                                            ║
 * ║  - Called by Controller (never directly accesses Repository)             ║
 * ║  - Handles transactions                                                   ║
 * ║  - Converts between Entity and DTO                                        ║
 * ║                                                                           ║
 * ║  RULE: Controller should be "thin" (just handle HTTP)                    ║
 * ║        Service should be "fat" (all the logic here)                      ║
 * ║                                                                           ║
 * ║  WHY?                                                                     ║
 * ║  - Service can be reused (called from multiple controllers)              ║
 * ║  - Easy to unit test (no HTTP involved)                                   ║
 * ║  - Business rules in one place                                           ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */

/*
 * @Service - Tells Spring this is a service component
 *   - Spring will create ONE instance (singleton) and manage it
 *   - Can be injected into Controllers using @Autowired
 * 
 * @RequiredArgsConstructor (Lombok) - Creates constructor for final fields
 *   - Spring uses constructor injection (recommended over @Autowired on fields)
 *   - Makes testing easier (can pass mock repository in constructor)
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    /*
     * DEPENDENCY INJECTION:
     * - We don't create ProductRepository with "new"
     * - Spring creates it and "injects" it here
     * - final = required, must be provided
     */
    private final ProductRepository productRepository;

    // ═══════════════════════════════════════════════════════════════════════
    // READ OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Get all products
     * 
     * Stream API explanation:
     * productRepository.findAll()    → Returns List<Product>
     *   .stream()                    → Convert to Stream for processing
     *   .map(this::mapToDto)         → Convert each Product to ProductDto
     *   .collect(Collectors.toList()) → Collect back to List
     */
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get product by ID
     * 
     * Optional explained:
     * - findById() returns Optional<Product>, not Product
     * - Optional can be empty (if not found) or contain a value
     * - orElseThrow() returns the value or throws exception if empty
     */
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forProduct(id));
        return mapToDto(product);
    }

    /**
     * Search products by name
     */
    public List<ProductDto> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get products by category
     */
    public List<ProductDto> getProductsByCategory(String category) {
        return productRepository.findByCategory(category)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // WRITE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Create a new product
     * 
     * @Transactional - Wraps this method in a database transaction
     *   - If any error occurs, ALL changes are rolled back
     *   - Ensures data consistency
     */
    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        // Convert DTO to Entity
        Product product = mapToEntity(productDto);
        
        // Save to database (INSERT query)
        Product savedProduct = productRepository.save(product);
        
        // Convert back to DTO and return
        return mapToDto(savedProduct);
    }

    /**
     * Update an existing product
     */
    @Transactional
    public ProductDto updateProduct(Long id, ProductDto productDto) {
        // First, check if product exists
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forProduct(id));
        
        // Update fields
        existingProduct.setName(productDto.getName());
        existingProduct.setDescription(productDto.getDescription());
        existingProduct.setPrice(productDto.getPrice());
        existingProduct.setStockQuantity(productDto.getStockQuantity());
        existingProduct.setCategory(productDto.getCategory());
        
        // Save (UPDATE query because entity already has an ID)
        Product updatedProduct = productRepository.save(existingProduct);
        
        return mapToDto(updatedProduct);
    }

    /**
     * Update stock quantity
     * This is called by Order Service when an order is placed
     */
    @Transactional
    public ProductDto updateStock(Long id, StockUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forProduct(id));
        
        // Calculate new quantity
        int newQuantity = product.getStockQuantity() + request.getQuantityChange();
        
        // Validate (can't have negative stock)
        if (newQuantity < 0) {
            throw new IllegalArgumentException(
                "Insufficient stock. Available: " + product.getStockQuantity() + 
                ", Requested: " + Math.abs(request.getQuantityChange())
            );
        }
        
        product.setStockQuantity(newQuantity);
        Product updatedProduct = productRepository.save(product);
        
        return mapToDto(updatedProduct);
    }

    /**
     * Delete a product
     */
    @Transactional
    public void deleteProduct(Long id) {
        // Check if exists before deleting (to give proper error message)
        if (!productRepository.existsById(id)) {
            throw ResourceNotFoundException.forProduct(id);
        }
        productRepository.deleteById(id);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MAPPING METHODS (Entity <-> DTO conversion)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Convert Entity to DTO
     * 
     * In larger projects, you'd use a library like MapStruct
     * for automatic mapping. Here we do it manually for clarity.
     */
    private ProductDto mapToDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .category(product.getCategory())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    /**
     * Convert DTO to Entity
     */
    private Product mapToEntity(ProductDto dto) {
        return Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 0)
                .category(dto.getCategory())
                .build();
        // Note: We don't set ID here because it's auto-generated
    }
}
