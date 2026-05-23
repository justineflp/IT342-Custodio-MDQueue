package edu.cit.custodio.mdqueue.feature.queueentry;

import edu.cit.custodio.mdqueue.shared.adapter.ApiResponseAdapter;
import edu.cit.custodio.mdqueue.shared.dto.ApiResponse;
import edu.cit.custodio.mdqueue.feature.queueentry.dto.QueueEntryResponse;
import edu.cit.custodio.mdqueue.feature.user.User;
import edu.cit.custodio.mdqueue.feature.queueentry.QueueEntryService;
import edu.cit.custodio.mdqueue.feature.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queue-entries")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class QueueEntryController {

    private final QueueEntryService queueEntryService;
    private final UserService userService;

    @PostMapping("/join/{queueId}")
    public ResponseEntity<ApiResponse<QueueEntryResponse>> joinQueue(
            @PathVariable Long queueId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User patient = userService.findByEmail(userDetails.getUsername());
        QueueEntryResponse response = queueEntryService.joinQueue(queueId, patient);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseAdapter.toSuccessResponse(response, "Joined queue successfully", HttpStatus.CREATED.value()));
    }

    @GetMapping("/queue/{queueId}")
    public ResponseEntity<ApiResponse<List<QueueEntryResponse>>> getByQueue(@PathVariable Long queueId) {
        List<QueueEntryResponse> entries = queueEntryService.getEntriesByQueue(queueId);
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(entries, "Queue entries retrieved"));
    }

    @GetMapping("/queue/{queueId}/waiting")
    public ResponseEntity<ApiResponse<List<QueueEntryResponse>>> getWaiting(@PathVariable Long queueId) {
        List<QueueEntryResponse> entries = queueEntryService.getWaitingEntries(queueId);
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(entries, "Waiting entries retrieved"));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<QueueEntryResponse>>> getMyEntries(
            @AuthenticationPrincipal UserDetails userDetails) {
        User patient = userService.findByEmail(userDetails.getUsername());
        List<QueueEntryResponse> entries = queueEntryService.getMyEntries(patient);
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(entries, "Your queue entries retrieved"));
    }

    @GetMapping("/my/active")
    public ResponseEntity<ApiResponse<List<QueueEntryResponse>>> getMyActiveEntries(
            @AuthenticationPrincipal UserDetails userDetails) {
        User patient = userService.findByEmail(userDetails.getUsername());
        List<QueueEntryResponse> entries = queueEntryService.getMyActiveEntries(patient);
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(entries, "Your active entries retrieved"));
    }

    @PatchMapping("/serve-next/{queueId}")
    public ResponseEntity<ApiResponse<QueueEntryResponse>> serveNext(
            @PathVariable Long queueId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User doctor = userService.findByEmail(userDetails.getUsername());
        QueueEntryResponse response = queueEntryService.serveNext(queueId, doctor);
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(response, "Now serving patient"));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<QueueEntryResponse>> complete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User doctor = userService.findByEmail(userDetails.getUsername());
        QueueEntryResponse response = queueEntryService.completeEntry(id, doctor);
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(response, "Entry completed"));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<QueueEntryResponse>> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        QueueEntryResponse response = queueEntryService.cancelEntry(id, user);
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(response, "Entry cancelled"));
    }
}
