package com.ecommerce.product.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                     GLOBAL EXCEPTION HANDLER                              ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  Catches exceptions from ALL controllers and returns proper JSON errors  ║
 * ║                                                                           ║
 * ║  WITHOUT THIS:                                                            ║
 * ║  - Stack traces exposed to users (security risk!)                         ║
 * ║  - Inconsistent error format                                              ║
 * ║                                                                           ║
 * ║  WITH THIS:                                                               ║
 * ║  - Clean JSON error responses                                             ║
 * ║  - Consistent format across all endpoints                                 ║
 * ║  - Proper HTTP status codes                                               ║
 * ║                                                                           ║
 * ║  INTERVIEW TIP:                                                           ║
 * ║  "I use @RestControllerAdvice for centralized exception handling.         ║
 * ║   This ensures consistent error responses across all endpoints without    ║
 * ║   duplicating try-catch blocks in every controller."                      ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */

/*
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 *   - @ControllerAdvice: Applies to all controllers
 *   - @ResponseBody: Returns JSON instead of view names
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle ResourceNotFoundException
     * Example output:
     * {
     *   "timestamp": "2024-01-15T10:30:00",
     *   "status": 404,
     *   "error": "Not Found",
     *   "message": "Product not found with id: 999"
     * }
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(
            ResourceNotFoundException ex) {
        
        Map<String, Object> error = createErrorResponse(
            HttpStatus.NOT_FOUND, 
            ex.getMessage()
        );
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle validation errors from @Valid
     * Triggered when request body fails DTO validation
     * 
     * Example output:
     * {
     *   "timestamp": "2024-01-15T10:30:00",
     *   "status": 400,
     *   "error": "Validation Failed",
     *   "message": "Invalid input provided",
     *   "fieldErrors": {
     *     "name": "Product name is required",
     *     "price": "Price must be positive"
     *   }
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex) {
        
        Map<String, Object> error = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Validation failed"
        );
        
        // Collect all field errors
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        error.put("fieldErrors", fieldErrors);
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle IllegalArgumentException (business logic validation errors)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex) {
        
        Map<String, Object> error = createErrorResponse(
            HttpStatus.BAD_REQUEST, 
            ex.getMessage()
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Catch-all for any unhandled exceptions
     * Prevents stack traces from being exposed
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex) {
        
        Map<String, Object> error = createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred: " + ex.getMessage()
        );
        
        // Log the actual exception for debugging
        ex.printStackTrace();
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Helper method to create consistent error response structure
     */
    private Map<String, Object> createErrorResponse(HttpStatus status, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", status.value());
        error.put("error", status.getReasonPhrase());
        error.put("message", message);
        return error;
    }
}
