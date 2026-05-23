package edu.cit.custodio.mdqueue.feature.appointment;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void getAppointmentDetails_ShouldReturnOk_WhenAppointmentExists() throws Exception {
        // Arrange
        AppointmentResponse response = AppointmentResponse.builder()
                .id(1L)
                .patientId(2L)
                .patientName("John Patient")
                .doctorId(3L)
                .doctorName("Dr. Jane")
                .appointmentDatetime(LocalDateTime.of(2026, 6, 1, 10, 0))
                .reason("Regular Checkup")
                .status("PENDING")
                .build();

        Mockito.when(appointmentService.getAppointmentDetails(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/appointments/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Appointment details retrieved"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.patientName").value("John Patient"))
                .andExpect(jsonPath("$.data.doctorName").value("Dr. Jane"))
                .andExpect(jsonPath("$.data.reason").value("Regular Checkup"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }
}
