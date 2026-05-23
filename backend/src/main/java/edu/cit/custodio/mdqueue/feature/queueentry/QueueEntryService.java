package edu.cit.custodio.mdqueue.feature.queueentry;

import edu.cit.custodio.mdqueue.feature.queue.QueueService;

import edu.cit.custodio.mdqueue.feature.queueentry.dto.QueueEntryResponse;
import edu.cit.custodio.mdqueue.feature.queueentry.QueueEntry;
import edu.cit.custodio.mdqueue.feature.queue.QueueEntity;
import edu.cit.custodio.mdqueue.feature.user.User;
import edu.cit.custodio.mdqueue.feature.queueentry.QueueEntryRepository;
import edu.cit.custodio.mdqueue.feature.queue.QueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QueueEntryService {

    private final QueueEntryRepository queueEntryRepository;
    private final QueueRepository queueRepository;
    private final QueueService queueService;

    @Transactional
    public QueueEntryResponse joinQueue(Long queueId, User patient) {
        QueueEntity queue = queueService.getEntityById(queueId);

        if (queue.getStatus() != QueueEntity.Status.OPEN) {
            throw new IllegalArgumentException("This queue is not currently open");
        }

        // Check if already in this queue
        var existing = queueEntryRepository.findByQueueAndPatientAndStatusIn(
                queue, patient, List.of(QueueEntry.Status.WAITING, QueueEntry.Status.SERVING));
        if (existing.isPresent()) {
            throw new IllegalArgumentException("You are already in this queue");
        }

        int number = queue.nextNumber();
        queueRepository.save(queue);

        QueueEntry entry = QueueEntry.builder()
                .queue(queue)
                .patient(patient)
                .queueNumber(number)
                .status(QueueEntry.Status.WAITING)
                .build();
        entry = queueEntryRepository.save(entry);
        return toResponse(entry);
    }

    public List<QueueEntryResponse> getEntriesByQueue(Long queueId) {
        QueueEntity queue = queueService.getEntityById(queueId);
        return queueEntryRepository.findByQueueOrderByQueueNumberAsc(queue).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<QueueEntryResponse> getWaitingEntries(Long queueId) {
        QueueEntity queue = queueService.getEntityById(queueId);
        return queueEntryRepository.findByQueueAndStatusOrderByQueueNumberAsc(queue, QueueEntry.Status.WAITING)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<QueueEntryResponse> getMyEntries(User patient) {
        return queueEntryRepository.findByPatientOrderByCheckInTimeDesc(patient).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<QueueEntryResponse> getMyActiveEntries(User patient) {
        return queueEntryRepository.findByPatientAndStatusIn(
                        patient, List.of(QueueEntry.Status.WAITING, QueueEntry.Status.SERVING))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public QueueEntryResponse serveNext(Long queueId, User doctor) {
        QueueEntity queue = queueService.getEntityById(queueId);
        if (!queue.getClinic().getOwner().getId().equals(doctor.getId())) {
            throw new IllegalArgumentException("Only the clinic owner can serve patients");
        }

        List<QueueEntry> waiting = queueEntryRepository.findByQueueAndStatusOrderByQueueNumberAsc(
                queue, QueueEntry.Status.WAITING);
        if (waiting.isEmpty()) {
            throw new IllegalArgumentException("No patients waiting in queue");
        }

        QueueEntry entry = waiting.get(0);
        entry.setStatus(QueueEntry.Status.SERVING);
        entry.setServedTime(LocalDateTime.now());
        entry = queueEntryRepository.save(entry);
        return toResponse(entry);
    }

    @Transactional
    public QueueEntryResponse completeEntry(Long entryId, User doctor) {
        QueueEntry entry = queueEntryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Queue entry not found"));
        if (!entry.getQueue().getClinic().getOwner().getId().equals(doctor.getId())) {
            throw new IllegalArgumentException("Only the clinic owner can complete entries");
        }
        entry.setStatus(QueueEntry.Status.COMPLETED);
        entry.setCompletedTime(LocalDateTime.now());
        entry = queueEntryRepository.save(entry);
        return toResponse(entry);
    }

    @Transactional
    public QueueEntryResponse cancelEntry(Long entryId, User user) {
        QueueEntry entry = queueEntryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Queue entry not found"));
        // Patients can cancel their own; owners can cancel any
        boolean isPatient = entry.getPatient().getId().equals(user.getId());
        boolean isOwner = entry.getQueue().getClinic().getOwner().getId().equals(user.getId());
        if (!isPatient && !isOwner) {
            throw new IllegalArgumentException("You cannot cancel this entry");
        }
        entry.setStatus(QueueEntry.Status.CANCELLED);
        entry.setCompletedTime(LocalDateTime.now());
        entry = queueEntryRepository.save(entry);
        return toResponse(entry);
    }

    private QueueEntryResponse toResponse(QueueEntry entry) {
        // Calculate position: count WAITING entries with lower queue number
        long peopleAhead = 0;
        if (entry.getStatus() == QueueEntry.Status.WAITING) {
            List<QueueEntry> waiting = queueEntryRepository.findByQueueAndStatusOrderByQueueNumberAsc(
                    entry.getQueue(), QueueEntry.Status.WAITING);
            for (QueueEntry w : waiting) {
                if (w.getQueueNumber() < entry.getQueueNumber()) {
                    peopleAhead++;
                }
            }
        }

        return QueueEntryResponse.builder()
                .id(entry.getId())
                .queueId(entry.getQueue().getId())
                .queueName(entry.getQueue().getName())
                .clinicId(entry.getQueue().getClinic().getId())
                .clinicName(entry.getQueue().getClinic().getName())
                .patientId(entry.getPatient().getId())
                .patientName(entry.getPatient().getFullName())
                .queueNumber(entry.getQueueNumber())
                .status(entry.getStatus().name())
                .checkInTime(entry.getCheckInTime() != null ? entry.getCheckInTime().toString() : null)
                .servedTime(entry.getServedTime() != null ? entry.getServedTime().toString() : null)
                .completedTime(entry.getCompletedTime() != null ? entry.getCompletedTime().toString() : null)
                .positionInQueue(peopleAhead + 1)
                .peopleAhead(peopleAhead)
                .build();
    }
}
