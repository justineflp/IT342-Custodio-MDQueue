package edu.cit.custodio.mdqueue.feature.queueentry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueEntryResponse {

    private Long id;
    private Long queueId;
    private String queueName;
    private Long clinicId;
    private String clinicName;
    private Long patientId;
    private String patientName;
    private int queueNumber;
    private String status;
    private String checkInTime;
    private String servedTime;
    private String completedTime;
    private long positionInQueue;
    private long peopleAhead;
}
