package edu.cit.custodio.mdqueue.feature.payment;

import edu.cit.custodio.mdqueue.feature.appointment.Appointment;
import edu.cit.custodio.mdqueue.feature.appointment.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    public Payment processSandboxPayment(Long appointmentId, String paymentMethodId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        // Use the dynamically set amountDue from the appointment, fallback to 50.00 if unset
        BigDecimal amount = appointment.getAmountDue() != null ? appointment.getAmountDue() : new BigDecimal("50.00");

        // Determine status and provider
        String provider = "Sandbox Gateway";
        Payment.Status status = Payment.Status.SUCCESS;
        
        if ("GCASH".equalsIgnoreCase(paymentMethodId)) {
            provider = "GCash";
        } else if ("CARD".equalsIgnoreCase(paymentMethodId)) {
            provider = "Credit/Debit Card";
        } else if ("PAY_IN_PERSON".equalsIgnoreCase(paymentMethodId)) {
            provider = "Pay in Person";
            status = Payment.Status.PENDING; // Pay in person is pending until clinic checkout
        } else if (paymentMethodId != null && paymentMethodId.startsWith("tok_fail")) {
            status = Payment.Status.FAILED;
        }

        Payment payment = Payment.builder()
                .appointment(appointment)
                .amount(amount)
                .currency("PHP")
                .provider(provider)
                .providerPaymentId(UUID.randomUUID().toString())
                .status(status)
                .paidAt(status == Payment.Status.SUCCESS ? LocalDateTime.now() : null)
                .build();

        return paymentRepository.save(payment);
    }

    public Payment getPaymentDetails(Long appointmentId) {
        return paymentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("No payment found for this appointment"));
    }
}
