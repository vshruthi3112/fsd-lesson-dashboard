package com.lessondashboard.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler - Production-quality centralized exception handling.
 *
 * Responsibilities:
 * - Catch all exceptions and return structured JSON errors
 * - Log exceptions at the appropriate level (WARN for client errors, ERROR for server errors)
 * - Never expose internal details (stack traces, SQL, internal paths) to the client
 * - Provide field-level validation error details for 400 responses
 *
 * Response shape (consistent across all error types):
 * {
 *   "status": 400,
 *   "message": "User-friendly error message",
 *   "timestamp": "2024-01-15T10:30:00",
 *   "errors": ["field: message", ...] // Only for validation errors
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors (e.g., @NotBlank, @Min, @Size failures).
     * Returns 400 Bad Request with field-level messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        List<String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        String message = "Validation failed for " + fieldErrors.size() + " field(s)";

        logger.warn("Validation failed: {}", fieldErrors);

        Map<String, Object> body = buildErrorResponse(HttpStatus.BAD_REQUEST, message);
        body.put("errors", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handle ResponseStatusException (e.g., 404 Not Found, 401 Unauthorized, 409 Conflict).
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(
            ResponseStatusException ex) {

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        // Log at appropriate level based on status code category
        if (status.is4xxClientError()) {
            logger.warn("Client error {}: {}", status.value(), ex.getReason());
        } else {
            logger.error("Server error {}: {}", status.value(), ex.getReason());
        }

        return ResponseEntity.status(status)
                .body(buildErrorResponse(status, ex.getReason()));
    }

    /**
     * Handle malformed JSON request bodies.
     * Returns 400 Bad Request with a safe message (no internal parse details).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedJson(
            HttpMessageNotReadableException ex) {

        logger.warn("Malformed request body: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST,
                        "Invalid request body. Please ensure JSON is well-formed."));
    }

    /**
     * Handle type mismatch (e.g., string ID where number expected).
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        String message = String.format("Invalid value for parameter '%s': expected a %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "valid value");

        logger.warn("Type mismatch on parameter '{}': {}", ex.getName(), ex.getValue());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST, message));
    }

    /**
     * Handle unsupported HTTP methods (e.g., PATCH on an endpoint that only supports PUT).
     * Returns 405 Method Not Allowed.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {

        String message = "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint";

        logger.warn("Method not allowed: {} (supported: {})", ex.getMethod(), ex.getSupportedHttpMethods());

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, message));
    }

    /**
     * Handle requests to non-existent endpoints.
     * Returns 404 Not Found.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(
            NoResourceFoundException ex) {

        logger.warn("Resource not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(HttpStatus.NOT_FOUND, "The requested resource was not found"));
    }

    /**
     * Catch-all for unexpected exceptions.
     * Returns 500 Internal Server Error with a SAFE message.
     *
     * IMPORTANT: Never expose internal details to the client.
     * The actual error is logged server-side for debugging.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        // Log the full error for debugging — this stays server-side only
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        // Return a safe, generic message to the client
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred. Please try again later."));
    }

    /**
     * Build a structured error response body.
     * Consistent shape for all error types.
     */
    private Map<String, Object> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now().toString());
        return body;
    }
}
