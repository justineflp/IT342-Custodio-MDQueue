package edu.cit.custodio.mdqueue.feature.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.custodio.mdqueue.feature.queue.dto.QueueRequest;
import edu.cit.custodio.mdqueue.feature.queue.dto.QueueResponse;
import edu.cit.custodio.mdqueue.feature.user.User;
import edu.cit.custodio.mdqueue.feature.user.UserService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QueueController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class QueueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QueueService queueService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "doctor@example.com", roles = "DOCTOR")
    void create_ShouldReturnCreated_WhenAuthorized() throws Exception {
        // Arrange
        QueueRequest request = QueueRequest.builder()
                .name("Morning Consult")
                .build();

        User doctor = User.builder()
                .id(1L)
                .email("doctor@example.com")
                .fullName("Dr. John")
                .role(User.Role.DOCTOR)
                .build();

        QueueResponse response = QueueResponse.builder()
                .id(1L)
                .clinicId(10L)
                .clinicName("Health First Clinic")
                .name("Morning Consult")
                .status("ACTIVE")
                .build();

        Mockito.when(userService.findByEmail("doctor@example.com")).thenReturn(doctor);
        Mockito.when(queueService.create(eq(10L), any(QueueRequest.class), any(User.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/queues/clinic/10")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Queue created"))
                .andExpect(jsonPath("$.data.name").value("Morning Consult"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void getByClinic_ShouldReturnOk_WhenPublicAccessed() throws Exception {
        // Arrange
        QueueResponse response = QueueResponse.builder()
                .id(1L)
                .clinicId(10L)
                .clinicName("Health First Clinic")
                .name("Morning Consult")
                .status("ACTIVE")
                .build();

        Mockito.when(queueService.getByClinic(10L)).thenReturn(Collections.singletonList(response));

        // Act & Assert
        mockMvc.perform(get("/api/queues/clinic/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Queues retrieved"))
                .andExpect(jsonPath("$.data[0].name").value("Morning Consult"));
    }

    @Test
    @WithMockUser(username = "doctor@example.com", roles = "DOCTOR")
    void updateStatus_ShouldReturnOk_WhenAuthorized() throws Exception {
        // Arrange
        Map<String, String> body = new HashMap<>();
        body.put("status", "COMPLETED");

        User doctor = User.builder()
                .id(1L)
                .email("doctor@example.com")
                .fullName("Dr. John")
                .role(User.Role.DOCTOR)
                .build();

        QueueResponse response = QueueResponse.builder()
                .id(1L)
                .clinicId(10L)
                .clinicName("Health First Clinic")
                .name("Morning Consult")
                .status("COMPLETED")
                .build();

        Mockito.when(userService.findByEmail("doctor@example.com")).thenReturn(doctor);
        Mockito.when(queueService.updateStatus(eq(1L), eq("COMPLETED"), any(User.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(patch("/api/queues/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Queue status updated"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }
}
