package com.meetingops.interfaces.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Global exception handler for REST API endpoints.
 *
 * <p>Provides consistent error responses across all controllers,
 * including validation errors, entity-not-found errors, and
 * unexpected server errors.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors from {@code @Valid} annotations.
     *
     * @param ex the validation exception
     * @return a 400 Bad Request response with field-level errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(final MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", error.getDefaultMessage()
                ))
                .toList();

        return ResponseEntity.badRequest().body(Map.of(
                "error", "VALIDATION_ERROR",
                "message", "Request validation failed",
                "details", errors,
                "timestamp", Instant.now().toString()
        ));
    }

    /**
     * Handles entity-not-found errors.
     *
     * @param ex the no-such-element exception
     * @return a 404 Not Found response
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(final NoSuchElementException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "NOT_FOUND",
                "message", ex.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    /**
     * Handles unexpected server errors.
     *
     * @param ex the runtime exception
     * @return a 500 Internal Server Error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(final Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "INTERNAL_ERROR",
                "message", "An unexpected error occurred",
                "timestamp", Instant.now().toString()
        ));
    }
}
