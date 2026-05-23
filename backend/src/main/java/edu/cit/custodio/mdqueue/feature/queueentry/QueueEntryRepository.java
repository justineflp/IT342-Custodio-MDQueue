package edu.cit.custodio.mdqueue.feature.queueentry;

import edu.cit.custodio.mdqueue.feature.queueentry.QueueEntry;
import edu.cit.custodio.mdqueue.feature.queue.QueueEntity;
import edu.cit.custodio.mdqueue.feature.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QueueEntryRepository extends JpaRepository<QueueEntry, Long> {

    List<QueueEntry> findByQueueOrderByQueueNumberAsc(QueueEntity queue);

    List<QueueEntry> findByQueueAndStatusOrderByQueueNumberAsc(QueueEntity queue, QueueEntry.Status status);

    List<QueueEntry> findByPatientAndStatusIn(User patient, List<QueueEntry.Status> statuses);

    Optional<QueueEntry> findByQueueAndPatientAndStatusIn(QueueEntity queue, User patient, List<QueueEntry.Status> statuses);

    long countByQueueAndStatus(QueueEntity queue, QueueEntry.Status status);

    List<QueueEntry> findByPatientOrderByCheckInTimeDesc(User patient);
}
