package edu.cit.custodio.mdqueue.feature.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication response DTO.
 * <p>
 * Uses the <b>Builder Design Pattern</b> (via Lombok {@code @Builder}) to provide
 * a fluent, readable API for constructing response objects with multiple optional fields.
 * This avoids telescoping constructors and ensures immutability after construction.
 * </p>
 *
 * @see edu.cit.custodio.mdqueue.feature.auth.AuthService#buildAuthResponse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String type;
    private Long userId;
    private String email;
    private String fullName;
    private String role;
    private String message;
}
