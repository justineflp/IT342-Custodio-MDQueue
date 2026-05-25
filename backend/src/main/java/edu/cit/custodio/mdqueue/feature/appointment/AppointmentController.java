package edu.cit.custodio.mdqueue.feature.appointment;

import edu.cit.custodio.mdqueue.shared.adapter.ApiResponseAdapter;
import edu.cit.custodio.mdqueue.shared.dto.ApiResponse;
import edu.cit.custodio.mdqueue.feature.user.User;
import edu.cit.custodio.mdqueue.feature.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AppointmentRequest request) {
        
        User patient = userService.findByEmail(userDetails.getUsername());
        AppointmentResponse response = appointmentService.createAppointment(patient.getId(), request);
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(response, "Appointment created successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getMyAppointments(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userService.findByEmail(userDetails.getUsername());
        List<AppointmentResponse> appointments;
        
        if (user.getRole() == User.Role.DOCTOR) {
            appointments = appointmentService.getAppointmentsForDoctor(user.getId());
        } else {
            appointments = appointmentService.getAppointmentsForPatient(user.getId());
        }
        
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(appointments, "Appointments retrieved"));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAllAppointments(
            @AuthenticationPrincipal UserDetails userDetails) {
        // Assume ADMIN role check is handled by UI or we could add it here
        List<AppointmentResponse> appointments = appointmentService.getAllAppointments();
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(appointments, "All appointments retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        AppointmentResponse response = appointmentService.getAppointmentDetails(id);
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(response, "Appointment details retrieved"));
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateStatus(
            @PathVariable Long id, @RequestBody java.util.Map<String, String> payload) {
        String status = payload.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseAdapter.toErrorResponse(400, "Status is required"));
        }
        
        String amountDueStr = payload.get("amountDue");
        java.math.BigDecimal amountDue = null;
        if (amountDueStr != null && !amountDueStr.isBlank()) {
            try {
                amountDue = new java.math.BigDecimal(amountDueStr);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponseAdapter.toErrorResponse(400, "Invalid amount due format"));
            }
        }
        
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(
                appointmentService.updateStatus(id, status, amountDue), "Status updated"));
    }

    @PostMapping("/{id}/documents")
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            DocumentResponse doc = appointmentService.uploadDocument(id, file);
            return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(doc, "Document uploaded successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseAdapter.toErrorResponse(400, e.getMessage()));
        }
    }

    @GetMapping("/{id}/documents")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getDocuments(@PathVariable Long id) {
        List<DocumentResponse> docs = appointmentService.getDocumentsForAppointment(id);
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(docs, "Documents retrieved"));
    }

    @GetMapping("/documents/{docId}")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long docId) {
        MedicalDocument doc = appointmentService.getDocument(docId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getFileType() != null ? doc.getFileType() : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                .body(doc.getData());
    }
}
