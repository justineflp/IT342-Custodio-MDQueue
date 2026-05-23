package edu.cit.custodio.mdqueue.feature.appointment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AppointmentResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private LocalDateTime appointmentDatetime;
    private String reason;
    private String notes;
    private String status;
    private LocalDateTime createdAt;
}
