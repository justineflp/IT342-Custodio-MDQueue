package edu.cit.custodio.mdqueue.feature.appointment;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentRequest {
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Appointment date and time are required")
    @Future(message = "Appointment date must be in the future")
    private LocalDateTime appointmentDatetime;

    @NotBlank(message = "Reason for appointment is required")
    private String reason;
}
