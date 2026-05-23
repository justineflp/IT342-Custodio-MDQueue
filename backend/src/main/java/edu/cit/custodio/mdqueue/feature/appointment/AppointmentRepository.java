package edu.cit.custodio.mdqueue.feature.appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientIdOrderByAppointmentDatetimeDesc(Long patientId);
    List<Appointment> findByDoctorIdOrderByAppointmentDatetimeDesc(Long doctorId);
}
