package edu.cit.custodio.mdqueue.feature.queue;

import edu.cit.custodio.mdqueue.feature.clinic.Clinic;
import edu.cit.custodio.mdqueue.feature.queue.QueueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueueRepository extends JpaRepository<QueueEntity, Long> {

    List<QueueEntity> findByClinic(Clinic clinic);

    List<QueueEntity> findByClinicAndStatus(Clinic clinic, QueueEntity.Status status);

    List<QueueEntity> findByClinicId(Long clinicId);
}
