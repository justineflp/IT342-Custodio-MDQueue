package edu.cit.custodio.mdqueue.feature.auth;

import edu.cit.custodio.mdqueue.feature.user.UserService;

import edu.cit.custodio.mdqueue.feature.auth.dto.AuthResponse;
import edu.cit.custodio.mdqueue.feature.auth.dto.LoginRequest;
import edu.cit.custodio.mdqueue.feature.auth.dto.RegisterRequest;
import edu.cit.custodio.mdqueue.feature.user.User;
import edu.cit.custodio.mdqueue.feature.auth.event.AuthEventPublisher;
import edu.cit.custodio.mdqueue.feature.auth.exception.InvalidCredentialsException;
import edu.cit.custodio.mdqueue.shared.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import edu.cit.custodio.mdqueue.feature.auth.dto.GoogleLoginRequest;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

/**
 * Service handling authentication operations (login and registration).
 * <p>
 * Design Patterns used:
 * <ul>
 *   <li><b>Builder Pattern</b> — via the centralized {@link #buildAuthResponse} method</li>
 *   <li><b>Observer Pattern</b> — via {@link AuthEventPublisher} to publish auth events,
 *       decoupling side-effects (logging, notifications) from the core auth logic</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuthEventPublisher authEventPublisher; // Observer Pattern: event publisher

    public AuthResponse register(RegisterRequest request) {
        User user = userService.register(request);
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_USER")
                .build();
        String token = jwtService.generateToken(userDetails);

        // Observer Pattern: publish registration event (decoupled side-effects)
        authEventPublisher.publishRegisterEvent(user);

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

            // Observer Pattern: publish login event (decoupled side-effects)
            authEventPublisher.publishLoginEvent(user);

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
                .role(user.getRole().name())
                .message(message)
                .build();
    }

    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        String googleToken = request.getToken();
        String email;
        String fullName;
        boolean isMock = false;

        if (googleToken != null && googleToken.startsWith("mock_google_token_")) {
            // Simulation Bypass Mode for Dev/Demo
            email = googleToken.substring("mock_google_token_".length()).toLowerCase().trim();
            if (email.isBlank()) {
                email = "demo.google.user@example.com";
            }
            fullName = "Demo Google User";
            isMock = true;
        } else {
            // Real OAuth: verify the token using Google tokeninfo API
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> payload;
            try {
                payload = restTemplate.getForObject("https://oauth2.googleapis.com/tokeninfo?id_token={token}", Map.class, googleToken);
            } catch (Exception e) {
                throw new InvalidCredentialsException("Invalid Google token verification failed");
            }

            if (payload == null || !payload.containsKey("email")) {
                throw new InvalidCredentialsException("Invalid Google token payload");
            }

            email = ((String) payload.get("email")).toLowerCase().trim();
            fullName = (String) payload.get("name");
            if (fullName == null || fullName.isBlank()) {
                fullName = (String) payload.get("given_name");
                if (fullName == null || fullName.isBlank()) {
                    fullName = "Google User";
                }
            }
        }

        // Retrieve or register user
        User user;
        boolean isNewUser = false;
        if (userService.existsByEmail(email)) {
            user = userService.findByEmail(email);
        } else {
            user = userService.registerGoogleUser(email, fullName);
            isNewUser = true;
        }

        // Generate application security details
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_USER")
                .build();
        String token = jwtService.generateToken(userDetails);

        String modeSuffix = isMock ? " (Simulated)" : "";
        if (isNewUser) {
            authEventPublisher.publishRegisterEvent(user);
            return buildAuthResponse(user, token, "Google account" + modeSuffix + " registered and logged in successfully");
        } else {
            authEventPublisher.publishLoginEvent(user);
            return buildAuthResponse(user, token, "Google login" + modeSuffix + " successful");
        }
    }
}
