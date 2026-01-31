package com.ecommerce.order.exception;

import feign.FeignException;
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
 * ║              GLOBAL EXCEPTION HANDLER (Order Service)                     ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  SPECIAL: Handles FeignException                                          ║
 * ║                                                                           ║
 * ║  When we call Product/User service and it fails, Feign throws:           ║
 * ║  - FeignException.NotFound (404)  → Product/User doesn't exist           ║
 * ║  - FeignException.BadRequest (400) → Invalid data sent                   ║
 * ║  - FeignException (other) → Service unavailable                          ║
 * ║                                                                           ║
 * ║  We catch these and return meaningful error messages to the client       ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return createErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Handle Feign exceptions (errors from other services)
     * 
     * When Product Service or User Service returns an error,
     * Feign throws a FeignException with the HTTP status code
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, Object>> handleFeignException(FeignException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.status());
        String message;
        
        // Provide user-friendly messages based on status
        if (status == HttpStatus.NOT_FOUND) {
            message = "Resource not found in external service";
            // Extract more details from the exception if available
            if (ex.getMessage().contains("product-service")) {
                message = "Product not found";
            } else if (ex.getMessage().contains("user-service")) {
                message = "User not found";
            }
        } else if (status == HttpStatus.SERVICE_UNAVAILABLE || ex.status() == -1) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "External service is currently unavailable. Please try again later.";
        } else {
            message = "Error communicating with external service";
        }
        
        return createErrorResponse(status, message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Validation Failed");
        
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        error.put("fieldErrors", fieldErrors);
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        // Log the actual exception for debugging
        ex.printStackTrace();
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", status.value());
        error.put("error", status.getReasonPhrase());
        error.put("message", message);
        return new ResponseEntity<>(error, status);
    }
}
