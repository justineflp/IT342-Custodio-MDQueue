package edu.cit.custodio.mdqueue.feature.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicResponse {

    private Long id;
    private String name;
    private String address;
    private String phoneNumber;
    private String description;
    private String openingTime;
    private String closingTime;
    private Long ownerId;
    private String ownerName;
    private int activeQueues;
}
