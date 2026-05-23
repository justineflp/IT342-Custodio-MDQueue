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

        // Simulate a fixed price consultation fee
        BigDecimal amount = new BigDecimal("50.00");

        // Simulate sandbox logic (if paymentMethodId starts with "tok_fail", simulate failure)
        boolean isSuccess = paymentMethodId != null && !paymentMethodId.startsWith("tok_fail");

        Payment payment = Payment.builder()
                .appointment(appointment)
                .amount(amount)
                .currency("USD")
                .provider("Sandbox Gateway")
                .providerPaymentId(UUID.randomUUID().toString())
                .status(isSuccess ? Payment.Status.SUCCESS : Payment.Status.FAILED)
                .paidAt(isSuccess ? LocalDateTime.now() : null)
                .build();

        if (isSuccess) {
            // Automatically confirm appointment if payment succeeds
            appointment.setStatus(Appointment.Status.CONFIRMED);
            appointmentRepository.save(appointment);
        }

        return paymentRepository.save(payment);
    }

    public Payment getPaymentDetails(Long appointmentId) {
        return paymentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("No payment found for this appointment"));
    }
}
