package edu.cit.custodio.mdqueue.feature.auth;

import edu.cit.custodio.mdqueue.feature.auth.dto.AuthResponse;
import edu.cit.custodio.mdqueue.feature.auth.dto.LoginRequest;
import edu.cit.custodio.mdqueue.feature.auth.dto.RegisterRequest;
import edu.cit.custodio.mdqueue.feature.auth.exception.InvalidCredentialsException;
import edu.cit.custodio.mdqueue.feature.user.User;
import edu.cit.custodio.mdqueue.feature.user.UserService;
import edu.cit.custodio.mdqueue.shared.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

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

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .message("Account created successfully")
                .build();
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

            return AuthResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .userId(user.getId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .message("Login successful")
                    .build();
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }
}
