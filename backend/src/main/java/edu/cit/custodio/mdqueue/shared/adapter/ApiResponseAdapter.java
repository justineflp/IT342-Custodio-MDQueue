package edu.cit.custodio.mdqueue.shared.adapter;

import edu.cit.custodio.mdqueue.shared.dto.ApiResponse;

/**
 * Adapter Pattern: Converts domain-specific response objects into standardized
 * {@link ApiResponse} wrappers.
 * <p>
 * This adapter serves as a bridge between the domain layer (which produces
 * domain-specific DTOs like {@code AuthResponse}) and the API contract (which
 * requires a uniform {@code ApiResponse<T>} structure). It adapts incompatible
 * response interfaces into a consistent format that API consumers can rely on.
 * </p>
 * <p>
 * <b>Adapter Design Pattern</b>: The key difference from the Singleton factory is intent.
 * While {@code ApiResponseFactory} creates new response objects, {@code ApiResponseAdapter}
 * <em>converts existing objects</em> from one format to another — the defining
 * characteristic of the Adapter pattern.
 * </p>
 *
 * @see edu.cit.custodio.mdqueue.shared.dto.ApiResponse
 */
public class ApiResponseAdapter {

    /**
     * Adapts any domain object into a standardized success {@link ApiResponse}.
     *
     * @param data    the domain-specific response object to adapt
     * @param message a human-readable message
     * @param <T>     the type of the domain object
     * @return a standardized ApiResponse wrapping the original data
     */
    public static <T> ApiResponse<T> toSuccessResponse(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Adapts any domain object into a standardized success {@link ApiResponse}
     * with a custom HTTP status code.
     *
     * @param data    the domain-specific response object to adapt
     * @param message a human-readable message
     * @param status  the HTTP status code
     * @param <T>     the type of the domain object
     * @return a standardized ApiResponse wrapping the original data
     */
    public static <T> ApiResponse<T> toSuccessResponse(T data, String message, int status) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(status)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Adapts an error condition into a standardized error {@link ApiResponse}.
     *
     * @param status  the HTTP status code
     * @param message a human-readable error message
     * @param <T>     the type parameter (typically Void for errors)
     * @return a standardized error ApiResponse
     */
    public static <T> ApiResponse<T> toErrorResponse(int status, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .data(null)
                .build();
    }
}
