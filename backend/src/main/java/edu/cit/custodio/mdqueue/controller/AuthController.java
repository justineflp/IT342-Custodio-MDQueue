package edu.cit.custodio.mdqueue.controller;

import edu.cit.custodio.mdqueue.adapter.ApiResponseAdapter;
import edu.cit.custodio.mdqueue.dto.ApiResponse;
import edu.cit.custodio.mdqueue.dto.AuthResponse;
import edu.cit.custodio.mdqueue.dto.LoginRequest;
import edu.cit.custodio.mdqueue.dto.RegisterRequest;
import edu.cit.custodio.mdqueue.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 * <p>
 * Uses the <b>Adapter Pattern</b> via {@link ApiResponseAdapter} to convert
 * domain-specific {@link AuthResponse} objects into the standardized
 * {@link ApiResponse} wrapper format, ensuring a consistent API contract.
 * </p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        // Adapter Pattern: convert domain AuthResponse → standardized ApiResponse
        ApiResponse<AuthResponse> response = ApiResponseAdapter.toSuccessResponse(
                authResponse, authResponse.getMessage(), HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        // Adapter Pattern: convert domain AuthResponse → standardized ApiResponse
        ApiResponse<AuthResponse> response = ApiResponseAdapter.toSuccessResponse(
                authResponse, authResponse.getMessage());
        return ResponseEntity.ok(response);
    }
}

