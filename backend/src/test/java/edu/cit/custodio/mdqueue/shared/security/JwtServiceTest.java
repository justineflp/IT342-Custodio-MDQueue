package edu.cit.custodio.mdqueue.shared.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtService – token generation, extraction, and validation.
 */
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);

        userDetails = User.withUsername("test@example.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();
    }

    @Test
    @DisplayName("Generate token returns non-null, non-empty string")
    void generateToken_returnsValidToken() {
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Extract username from token matches original")
    void extractUsername_returnsCorrectUsername() {
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);

        assertEquals("test@example.com", username);
    }

    @Test
    @DisplayName("Valid token passes validation check")
    void isTokenValid_withValidToken_returnsTrue() {
        String token = jwtService.generateToken(userDetails);
        boolean valid = jwtService.isTokenValid(token, userDetails);

        assertTrue(valid);
    }

    @Test
    @DisplayName("Token with wrong user fails validation")
    void isTokenValid_withDifferentUser_returnsFalse() {
        String token = jwtService.generateToken(userDetails);

        UserDetails otherUser = User.withUsername("other@example.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        boolean valid = jwtService.isTokenValid(token, otherUser);

        assertFalse(valid);
    }

    @Test
    @DisplayName("Expired token fails validation")
    void isTokenValid_withExpiredToken_returnsFalse() {
        // Set a very short expiration
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L);

        String token = jwtService.generateToken(userDetails);

        // Reset expiration for validation (so the service itself doesn't error)
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);

        assertThrows(Exception.class, () -> jwtService.isTokenValid(token, userDetails));
    }

    @Test
    @DisplayName("Two tokens for the same user are different (unique issuedAt)")
    void generateToken_twiceForSameUser_returnsDifferentTokens() throws InterruptedException {
        String token1 = jwtService.generateToken(userDetails);
        Thread.sleep(1100); // JWT iat has seconds precision, so we need >1s gap
        String token2 = jwtService.generateToken(userDetails);

        assertNotEquals(token1, token2);
    }
}
