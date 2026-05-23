package edu.cit.custodio.mdqueue.feature.clinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicRequest {

    @NotBlank(message = "Clinic name is required")
    @Size(max = 150)
    private String name;

    @Size(max = 500)
    private String address;

    @Size(max = 20)
    private String phoneNumber;

    @Size(max = 1000)
    private String description;

    private String openingTime; // "08:00"

    private String closingTime; // "17:00"
}
