package edu.cit.custodio.mdqueue.feature.appointment;

import edu.cit.custodio.mdqueue.feature.user.User;
import edu.cit.custodio.mdqueue.feature.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final MedicalDocumentRepository documentRepository;

    @Transactional
    public AppointmentResponse createAppointment(Long patientId, AppointmentRequest request) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        if (doctor.getRole() != User.Role.DOCTOR) {
            throw new IllegalArgumentException("Selected user is not a doctor");
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDatetime(request.getAppointmentDatetime())
                .reason(request.getReason())
                .status(Appointment.Status.PENDING)
                .build();

        appointment = appointmentRepository.save(appointment);
        return mapToResponse(appointment);
    }

    public List<AppointmentResponse> getAppointmentsForPatient(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByAppointmentDatetimeDesc(patientId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<AppointmentResponse> getAppointmentsForDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorIdOrderByAppointmentDatetimeDesc(doctorId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "appointmentDatetime"))
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponse updateStatus(Long appointmentId, String status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        
        try {
            Appointment.Status newStatus = Appointment.Status.valueOf(status.toUpperCase());
            appointment.setStatus(newStatus);
            return mapToResponse(appointmentRepository.save(appointment));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getFullName())
                .doctorId(appointment.getDoctor().getId())
                .doctorName(appointment.getDoctor().getFullName())
                .appointmentDatetime(appointment.getAppointmentDatetime())
                .reason(appointment.getReason())
                .notes(appointment.getNotes())
                .status(appointment.getStatus().name())
                .createdAt(appointment.getCreatedAt())
                .build();
    }

    @Transactional
    public DocumentResponse uploadDocument(Long appointmentId, MultipartFile file) throws IOException {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        MedicalDocument doc = MedicalDocument.builder()
                .appointment(appointment)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .data(file.getBytes())
                .build();

        doc = documentRepository.save(doc);

        return DocumentResponse.builder()
                .id(doc.getId())
                .fileName(doc.getFileName())
                .fileType(doc.getFileType())
                .uploadedAt(doc.getUploadedAt())
                .build();
    }

    public List<DocumentResponse> getDocumentsForAppointment(Long appointmentId) {
        return documentRepository.findByAppointmentIdOrderByUploadedAtDesc(appointmentId).stream()
                .map(doc -> DocumentResponse.builder()
                        .id(doc.getId())
                        .fileName(doc.getFileName())
                        .fileType(doc.getFileType())
                        .uploadedAt(doc.getUploadedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public MedicalDocument getDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
    }
}
