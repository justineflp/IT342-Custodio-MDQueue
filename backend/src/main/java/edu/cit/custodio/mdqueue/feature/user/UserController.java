package edu.cit.custodio.mdqueue.feature.user;

import edu.cit.custodio.mdqueue.shared.adapter.ApiResponseAdapter;
import edu.cit.custodio.mdqueue.shared.dto.ApiResponse;
import edu.cit.custodio.mdqueue.feature.user.User;
import edu.cit.custodio.mdqueue.feature.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        Map<String, Object> profile = Map.of(
                "id", user.getId(),
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "phoneNumber", user.getPhoneNumber() != null ? user.getPhoneNumber() : "",
                "role", user.getRole().name(),
                "specialty", user.getSpecialty() != null ? user.getSpecialty() : "General Practice"
        );
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(profile, "Profile retrieved"));
    }

    @GetMapping("/doctors")
    public ResponseEntity<ApiResponse<java.util.List<Map<String, Object>>>> getDoctors() {
        java.util.List<Map<String, Object>> doctors = userService.getDoctors().stream()
                .map(user -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", user.getId());
                    map.put("fullName", user.getFullName());
                    map.put("specialty", user.getSpecialty() != null ? user.getSpecialty() : "General Practice");
                    map.put("initials", getInitials(user.getFullName()));
                    map.put("color", "#3b82f6");
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(doctors, "Doctors retrieved"));
    }
    
    @GetMapping("/admin/doctors")
    public ResponseEntity<ApiResponse<java.util.List<Map<String, Object>>>> getAllDoctors() {
        java.util.List<Map<String, Object>> doctors = userService.getAllDoctors().stream()
                .map(user -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", user.getId());
                    map.put("fullName", user.getFullName());
                    map.put("email", user.getEmail());
                    map.put("isApproved", user.isApproved());
                    map.put("specialty", user.getSpecialty() != null ? user.getSpecialty() : "General Practice");
                    map.put("initials", getInitials(user.getFullName()));
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(doctors, "All doctors retrieved"));
    }

    @PatchMapping("/admin/doctors/{id}/approve")
    public ResponseEntity<ApiResponse<String>> approveDoctor(@PathVariable Long id) {
        userService.approveDoctor(id);
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse("Approved", "Doctor approved successfully"));
    }

    @PatchMapping("/me/specialty")
    public ResponseEntity<ApiResponse<String>> updateSpecialty(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> payload) {
        String specialty = payload.get("specialty");
        if (specialty == null || specialty.trim().isEmpty()) {
            throw new IllegalArgumentException("Specialty is required");
        }
        User user = userService.findByEmail(userDetails.getUsername());
        userService.updateSpecialty(user.getId(), specialty.trim());
        return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse("Updated", "Specialty updated successfully"));
    }
    
    private String getInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "DR";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
