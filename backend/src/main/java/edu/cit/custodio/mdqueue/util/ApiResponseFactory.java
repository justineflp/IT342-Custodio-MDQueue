package edu.cit.custodio.mdqueue.util;

import edu.cit.custodio.mdqueue.dto.ApiResponse;

import java.util.Map;

/**
 * Singleton factory for creating standardized {@link ApiResponse} objects.
 * <p>
 * <b>Singleton Design Pattern</b>: This class uses eager initialization to ensure
 * exactly one instance exists throughout the application's lifecycle. The private
 * constructor prevents external instantiation, and the static {@code getInstance()}
 * method provides the single global access point.
 * </p>
 * <p>
 * This factory centralizes API response construction, replacing scattered
 * {@code new HashMap<>()} calls in exception handlers and controllers with
 * a consistent, type-safe response format.
 * </p>
 */
public class ApiResponseFactory {

    // Eager initialization — thread-safe, created at class loading time
    private static final ApiResponseFactory INSTANCE = new ApiResponseFactory();

    /**
     * Private constructor prevents external instantiation.
     * This is a key characteristic of the Singleton pattern.
     */
    private ApiResponseFactory() {
        // Singleton: no external instantiation allowed
    }

    /**
     * Returns the single instance of ApiResponseFactory.
     *
     * @return the singleton instance
     */
    public static ApiResponseFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Creates a success response with data and message.
     *
     * @param data    the response payload
     * @param message a human-readable success message
     * @param <T>     the type of the data payload
     * @return a standardized success ApiResponse
     */
    public <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Creates a success response with data, message, and custom status code.
     *
     * @param data    the response payload
     * @param message a human-readable success message
     * @param status  the HTTP status code
     * @param <T>     the type of the data payload
     * @return a standardized success ApiResponse
     */
    public <T> ApiResponse<T> success(T data, String message, int status) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(status)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Creates an error response with status code and message.
     *
     * @param status  the HTTP status code
     * @param message a human-readable error message
     * @param <T>     the type parameter (typically Void or Object for errors)
     * @return a standardized error ApiResponse
     */
    public <T> ApiResponse<T> error(int status, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .data(null)
                .build();
    }

    /**
     * Creates a validation error response with field-level errors.
     *
     * @param errors a map of field names to error messages
     * @return a standardized validation error ApiResponse
     */
    public ApiResponse<Map<String, String>> validationError(Map<String, String> errors) {
        return ApiResponse.<Map<String, String>>builder()
                .success(false)
                .status(400)
                .message("Please fill out all required fields correctly")
                .data(errors)
                .build();
    }
}
