package edu.cit.custodio.mdqueue.feature.clinic;

import edu.cit.custodio.mdqueue.shared.adapter.ApiResponseAdapter;
import edu.cit.custodio.mdqueue.shared.dto.ApiResponse;
import edu.cit.custodio.mdqueue.feature.clinic.dto.ClinicRequest;
import edu.cit.custodio.mdqueue.feature.clinic.dto.ClinicResponse;
import edu.cit.custodio.mdqueue.feature.user.User;
import edu.cit.custodio.mdqueue.feature.clinic.ClinicService;
import edu.cit.custodio.mdqueue.feature.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ClinicController {

    private final ClinicService clinicService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClinicResponse>> create(
            @Valid @RequestBody ClinicRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User owner = userService.findByEmail(userDetails.getUsername());
        ClinicResponse response = clinicService.create(request, owner);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseAdapter.toSuccessResponse(response, "Clinic created successfully", HttpStatus.CREATED.value()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClinicResponse>>> getAll(
            @RequestParam(required = false) String search) {
        List<ClinicResponse> clinics = (search != null && !search.isBlank())
                ? clinicService.search(search)
                : clinicService.getAll();
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(clinics, "Clinics retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClinicResponse>> getById(@PathVariable Long id) {
        ClinicResponse response = clinicService.getById(id);
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(response, "Clinic retrieved"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClinicResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ClinicRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User owner = userService.findByEmail(userDetails.getUsername());
        ClinicResponse response = clinicService.update(id, request, owner);
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(response, "Clinic updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User owner = userService.findByEmail(userDetails.getUsername());
        clinicService.delete(id, owner);
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(null, "Clinic deleted"));
    }

    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<ClinicResponse>>> getMyClinics(
            @AuthenticationPrincipal UserDetails userDetails) {
        User owner = userService.findByEmail(userDetails.getUsername());
        List<ClinicResponse> clinics = clinicService.getByOwner(owner);
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(clinics, "Your clinics retrieved"));
    }
}
