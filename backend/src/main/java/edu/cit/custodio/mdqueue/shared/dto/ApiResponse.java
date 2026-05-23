package edu.cit.custodio.mdqueue.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standardized API response wrapper.
 * <p>
 * Part of the <b>Singleton Pattern</b> implementation: instances of this class
 * are created exclusively through {@link edu.cit.custodio.mdqueue.shared.util.ApiResponseFactory},
 * which is a Singleton, ensuring all API responses follow a consistent structure.
 * </p>
 *
 * @param <T> the type of the response data payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private int status;
    private String message;
    private T data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
