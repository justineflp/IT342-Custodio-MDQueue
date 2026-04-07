package edu.cit.custodio.mdqueue.service;

import edu.cit.custodio.mdqueue.dto.AuthResponse;
import edu.cit.custodio.mdqueue.dto.LoginRequest;
import edu.cit.custodio.mdqueue.dto.RegisterRequest;
import edu.cit.custodio.mdqueue.entity.User;
import edu.cit.custodio.mdqueue.exception.InvalidCredentialsException;
import edu.cit.custodio.mdqueue.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Service handling authentication operations (login and registration).
 * <p>
 * Uses the <b>Builder Pattern</b> via the centralized {@link #buildAuthResponse} method
 * to construct {@link AuthResponse} objects, eliminating code duplication and ensuring
 * consistent response structure across all auth operations.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        User user = userService.register(request);
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_USER")
                .build();
        String token = jwtService.generateToken(userDetails);

        // Builder Pattern: centralized response construction
        return buildAuthResponse(user, token, "Account created successfully");
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase().trim(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByEmail(userDetails.getUsername());
            String token = jwtService.generateToken(userDetails);

            // Builder Pattern: centralized response construction
            return buildAuthResponse(user, token, "Login successful");
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    /**
     * Builder Pattern: Centralized method for constructing AuthResponse objects.
     * <p>
     * This method uses the Builder pattern (via Lombok's @Builder on AuthResponse)
     * to create consistent, well-structured auth responses. By centralizing the
     * construction logic, we eliminate duplication between register() and login()
     * and ensure any future fields are added in a single place.
     * </p>
     *
     * @param user    the authenticated user entity
     * @param token   the generated JWT token
     * @param message a human-readable status message
     * @return a fully constructed AuthResponse
     */
    private AuthResponse buildAuthResponse(User user, String token, String message) {
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .message(message)
                .build();
    }
}
