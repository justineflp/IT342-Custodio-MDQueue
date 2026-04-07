package edu.cit.custodio.mdqueue.controller;

import edu.cit.custodio.mdqueue.dto.ApiResponse;
import edu.cit.custodio.mdqueue.util.ApiResponseFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller for general authenticated endpoints.
 * <p>
 * Uses the <b>Singleton Pattern</b> via {@link ApiResponseFactory#getInstance()}
 * to construct all API responses through the single factory instance.
 * </p>
 */
@RestController
@RequestMapping("/api")
public class HomeController {

    // Singleton Pattern: single factory instance for response construction
    private final ApiResponseFactory responseFactory = ApiResponseFactory.getInstance();

    @GetMapping("/home")
    public ResponseEntity<ApiResponse<Map<String, Object>>> home(
            @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> data = Map.of(
                "user", userDetails != null ? userDetails.getUsername() : "anonymous",
                "authenticated", userDetails != null
        );
        ApiResponse<Map<String, Object>> response = responseFactory.success(data, "Welcome to MDQueue");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            ApiResponse<Map<String, Object>> errorResponse = responseFactory.error(401, "Unauthorized");
            return ResponseEntity.status(401).body(errorResponse);
        }
        Map<String, Object> data = Map.of("email", userDetails.getUsername());
        ApiResponse<Map<String, Object>> response = responseFactory.success(data, "Welcome to your dashboard");
        return ResponseEntity.ok(response);
    }
}

