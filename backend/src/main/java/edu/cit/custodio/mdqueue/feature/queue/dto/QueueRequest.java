package edu.cit.custodio.mdqueue.feature.queue.dto;

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
public class QueueRequest {

    @NotBlank(message = "Queue name is required")
    @Size(max = 150)
    private String name;
}
