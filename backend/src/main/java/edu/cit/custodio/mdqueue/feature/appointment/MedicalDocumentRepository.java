package edu.cit.custodio.mdqueue.feature.appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalDocumentRepository extends JpaRepository<MedicalDocument, Long> {
    List<MedicalDocument> findByAppointmentIdOrderByUploadedAtDesc(Long appointmentId);
}
