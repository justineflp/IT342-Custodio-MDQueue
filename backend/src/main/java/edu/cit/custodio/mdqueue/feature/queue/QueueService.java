package edu.cit.custodio.mdqueue.feature.queue;

import edu.cit.custodio.mdqueue.feature.clinic.ClinicService;

import edu.cit.custodio.mdqueue.feature.queue.dto.QueueRequest;
import edu.cit.custodio.mdqueue.feature.queue.dto.QueueResponse;
import edu.cit.custodio.mdqueue.feature.clinic.Clinic;
import edu.cit.custodio.mdqueue.feature.queue.QueueEntity;
import edu.cit.custodio.mdqueue.feature.user.User;
import edu.cit.custodio.mdqueue.feature.queueentry.QueueEntryRepository;
import edu.cit.custodio.mdqueue.feature.queue.QueueRepository;
import edu.cit.custodio.mdqueue.feature.queueentry.QueueEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueRepository queueRepository;
    private final QueueEntryRepository queueEntryRepository;
    private final ClinicService clinicService;

    @Transactional
    public QueueResponse create(Long clinicId, QueueRequest request, User owner) {
        Clinic clinic = clinicService.getEntityById(clinicId);
        if (!clinic.getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("Only the clinic owner can create queues");
        }
        QueueEntity queue = QueueEntity.builder()
                .clinic(clinic)
                .name(request.getName())
                .status(QueueEntity.Status.OPEN)
                .currentNumber(0)
                .build();
        queue = queueRepository.save(queue);
        return toResponse(queue);
    }

    public List<QueueResponse> getByClinic(Long clinicId) {
        return queueRepository.findByClinicId(clinicId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public QueueResponse getById(Long id) {
        QueueEntity queue = getEntityById(id);
        return toResponse(queue);
    }

    public QueueEntity getEntityById(Long id) {
        return queueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Queue not found"));
    }

    @Transactional
    public QueueResponse updateStatus(Long id, String status, User owner) {
        QueueEntity queue = getEntityById(id);
        if (!queue.getClinic().getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("Only the clinic owner can update queue status");
        }
        queue.setStatus(QueueEntity.Status.valueOf(status.toUpperCase()));
        queue = queueRepository.save(queue);
        return toResponse(queue);
    }

    @Transactional
    public void delete(Long id, User owner) {
        QueueEntity queue = getEntityById(id);
        if (!queue.getClinic().getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("Only the clinic owner can delete queues");
        }
        queueRepository.delete(queue);
    }

    private QueueResponse toResponse(QueueEntity queue) {
        long waitingCount = queueEntryRepository.countByQueueAndStatus(queue, QueueEntry.Status.WAITING);
        return QueueResponse.builder()
                .id(queue.getId())
                .clinicId(queue.getClinic().getId())
                .clinicName(queue.getClinic().getName())
                .name(queue.getName())
                .status(queue.getStatus().name())
                .currentNumber(queue.getCurrentNumber() != null ? queue.getCurrentNumber() : 0)
                .waitingCount(waitingCount)
                .createdAt(queue.getCreatedAt() != null ? queue.getCreatedAt().toString() : null)
                .build();
    }
}
