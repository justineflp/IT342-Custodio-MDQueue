package edu.cit.custodio.mdqueue.feature.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    public void sendWelcomeEmail(String toEmail, String fullName) {
        log.info("==========================================");
        log.info("DUMMY EMAIL SENT:");
        log.info("To: {}", toEmail);
        log.info("Subject: Welcome to MDQueue!");
        log.info("Body: Hello {}, welcome to the MDQueue Medical Appointment System.", fullName);
        log.info("==========================================");
    }

    public void sendAppointmentConfirmation(String toEmail, String doctorName, String datetime) {
        log.info("==========================================");
        log.info("DUMMY EMAIL SENT:");
        log.info("To: {}", toEmail);
        log.info("Subject: Appointment Confirmed");
        log.info("Body: Your appointment with {} at {} has been confirmed.", doctorName, datetime);
        log.info("==========================================");
    }

    public void sendPaymentReceipt(String toEmail, String amount) {
        log.info("==========================================");
        log.info("DUMMY EMAIL SENT:");
        log.info("To: {}", toEmail);
        log.info("Subject: Payment Receipt");
        log.info("Body: Thank you for your payment of ${}.", amount);
        log.info("==========================================");
    }
}
