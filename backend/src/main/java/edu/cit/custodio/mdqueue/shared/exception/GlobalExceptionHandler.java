package edu.cit.custodio.mdqueue.shared.exception;

import edu.cit.custodio.mdqueue.feature.auth.exception.InvalidCredentialsException;

import edu.cit.custodio.mdqueue.feature.auth.exception.DuplicateEmailException;

import edu.cit.custodio.mdqueue.shared.dto.ApiResponse;
import edu.cit.custodio.mdqueue.shared.util.ApiResponseFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST API.
 * <p>
 * Refactored to use the <b>Singleton Pattern</b> via {@link ApiResponseFactory#getInstance()}.
 * All error responses are now constructed through the single factory instance,
 * replacing the previous scattered {@code new HashMap<>()} response construction.
 * This ensures consistent error response formatting across the entire API.
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Singleton Pattern: obtain the single factory instance
    private final ApiResponseFactory responseFactory = ApiResponseFactory.getInstance();

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateEmail(DuplicateEmailException ex) {
        ApiResponse<Void> response = responseFactory.error(
                HttpStatus.CONFLICT.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
        ApiResponse<Void> response = responseFactory.error(
                HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        ApiResponse<Void> response = responseFactory.error(
                HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // Singleton Pattern: use factory for consistent validation error responses
        ApiResponse<Map<String, String>> response = responseFactory.validationError(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}

