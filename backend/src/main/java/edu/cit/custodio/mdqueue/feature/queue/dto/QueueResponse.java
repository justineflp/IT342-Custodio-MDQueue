package edu.cit.custodio.mdqueue.feature.queue.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueResponse {

    private Long id;
    private Long clinicId;
    private String clinicName;
    private String name;
    private String status;
    private int currentNumber;
    private long waitingCount;
    private String createdAt;
}
