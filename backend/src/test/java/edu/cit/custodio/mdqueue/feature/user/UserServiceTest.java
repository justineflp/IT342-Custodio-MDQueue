package edu.cit.custodio.mdqueue.feature.user;

import edu.cit.custodio.mdqueue.feature.auth.dto.RegisterRequest;
import edu.cit.custodio.mdqueue.feature.auth.exception.DuplicateEmailException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;

    private RegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = RegisterRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .phoneNumber("1234567890")
                .password("password123")
                .confirmPassword("password123")
                .build();
    }

    @Test
    @DisplayName("Register with valid data saves user correctly")
    void register_validData_savesUser() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        User result = userService.register(validRequest);

        assertNotNull(result);
        assertEquals("John Doe", result.getFullName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("encoded_password", result.getPassword());
        assertEquals("1234567890", result.getPhoneNumber());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Register with duplicate email throws DuplicateEmailException")
    void register_duplicateEmail_throwsException() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        DuplicateEmailException ex = assertThrows(
                DuplicateEmailException.class,
                () -> userService.register(validRequest)
        );

        assertEquals("An account with this email already exists", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Register with mismatched passwords throws IllegalArgumentException")
    void register_mismatchedPasswords_throwsException() {
        validRequest.setConfirmPassword("different");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(validRequest)
        );

        assertEquals("Password and confirm password do not match", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Register normalizes email to lowercase and trims whitespace")
    void register_normalizesEmail() {
        validRequest.setEmail("  John@Example.COM  ");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.register(validRequest);

        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    @DisplayName("Register with null phone number saves null")
    void register_nullPhone_savesNull() {
        validRequest.setPhoneNumber(null);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.register(validRequest);

        assertNull(result.getPhoneNumber());
    }
}
