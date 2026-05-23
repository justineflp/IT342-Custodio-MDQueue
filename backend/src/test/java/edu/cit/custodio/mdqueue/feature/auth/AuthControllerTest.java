package edu.cit.custodio.mdqueue.feature.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.custodio.mdqueue.feature.auth.dto.AuthResponse;
import edu.cit.custodio.mdqueue.feature.auth.dto.LoginRequest;
import edu.cit.custodio.mdqueue.feature.auth.dto.RegisterRequest;
import edu.cit.custodio.mdqueue.shared.config.SecurityConfig;
import edu.cit.custodio.mdqueue.shared.security.CustomUserDetailsService;
import edu.cit.custodio.mdqueue.shared.security.JwtAuthenticationFilter;
import edu.cit.custodio.mdqueue.shared.security.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void register_ShouldReturnCreated_WhenRequestIsValid() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .fullName("John Doe")
                .email("john.doe@example.com")
                .password("SecurePass123!")
                .confirmPassword("SecurePass123!")
                .role("PATIENT")
                .build();

        AuthResponse expectedResponse = AuthResponse.builder()
                .userId(1L)
                .email("john.doe@example.com")
                .fullName("John Doe")
                .role("PATIENT")
                .message("Account created successfully")
                .build();

        Mockito.when(authService.register(any(RegisterRequest.class))).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Account created successfully"))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("John Doe"));
    }

    @Test
    void register_ShouldReturnBadRequest_WhenPasswordsDoNotMatch() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .fullName("John Doe")
                .email("john.doe@example.com")
                .password("SecurePass123!")
                .confirmPassword("DifferentPass123!")
                .role("PATIENT")
                .build();

        Mockito.when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("Password and confirm password do not match"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ShouldReturnOk_WhenCredentialsAreValid() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("john.doe@example.com")
                .password("SecurePass123!")
                .build();

        AuthResponse expectedResponse = AuthResponse.builder()
                .token("mock-jwt-token")
                .type("Bearer")
                .userId(1L)
                .email("john.doe@example.com")
                .fullName("John Doe")
                .message("Login successful")
                .build();

        Mockito.when(authService.login(any(LoginRequest.class))).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.data.type").value("Bearer"));
    }
}
