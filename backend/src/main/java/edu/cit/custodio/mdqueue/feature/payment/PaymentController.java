package edu.cit.custodio.mdqueue.feature.payment;

import edu.cit.custodio.mdqueue.shared.adapter.ApiResponseAdapter;
import edu.cit.custodio.mdqueue.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{appointmentId}/process")
    public ResponseEntity<ApiResponse<Payment>> processPayment(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, String> payload) {
        
        String paymentMethodId = payload.get("paymentMethodId");
        Payment payment = paymentService.processSandboxPayment(appointmentId, paymentMethodId);
        
        if (payment.getStatus() == Payment.Status.SUCCESS || payment.getStatus() == Payment.Status.PENDING) {
            return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(payment, "Payment processed successfully"));
        } else {
            return ResponseEntity.badRequest().body(ApiResponseAdapter.toErrorResponse(400, "Payment failed"));
        }
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<ApiResponse<Payment>> getPayment(@PathVariable Long appointmentId) {
        try {
            Payment payment = paymentService.getPaymentDetails(appointmentId);
            return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(payment, "Payment retrieved"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponseAdapter.toSuccessResponse(null, "No payment found yet"));
        }
    }
}
