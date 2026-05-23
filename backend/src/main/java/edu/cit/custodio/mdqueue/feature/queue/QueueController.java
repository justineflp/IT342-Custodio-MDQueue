package edu.cit.custodio.mdqueue.feature.queue;

import edu.cit.custodio.mdqueue.shared.adapter.ApiResponseAdapter;
import edu.cit.custodio.mdqueue.shared.dto.ApiResponse;
import edu.cit.custodio.mdqueue.feature.queue.dto.QueueRequest;
import edu.cit.custodio.mdqueue.feature.queue.dto.QueueResponse;
import edu.cit.custodio.mdqueue.feature.user.User;
import edu.cit.custodio.mdqueue.feature.queue.QueueService;
import edu.cit.custodio.mdqueue.feature.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/queues")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class QueueController {

    private final QueueService queueService;
    private final UserService userService;

    @PostMapping("/clinic/{clinicId}")
    public ResponseEntity<ApiResponse<QueueResponse>> create(
            @PathVariable Long clinicId,
            @Valid @RequestBody QueueRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User owner = userService.findByEmail(userDetails.getUsername());
        QueueResponse response = queueService.create(clinicId, request, owner);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseAdapter.toSuccessResponse(response, "Queue created", HttpStatus.CREATED.value()));
    }

    @GetMapping("/clinic/{clinicId}")
    public ResponseEntity<ApiResponse<List<QueueResponse>>> getByClinic(@PathVariable Long clinicId) {
        List<QueueResponse> queues = queueService.getByClinic(clinicId);
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(queues, "Queues retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QueueResponse>> getById(@PathVariable Long id) {
        QueueResponse response = queueService.getById(id);
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(response, "Queue retrieved"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<QueueResponse>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        User owner = userService.findByEmail(userDetails.getUsername());
        QueueResponse response = queueService.updateStatus(id, body.get("status"), owner);
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(response, "Queue status updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User owner = userService.findByEmail(userDetails.getUsername());
        queueService.delete(id, owner);
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(null, "Queue deleted"));
    }
}
