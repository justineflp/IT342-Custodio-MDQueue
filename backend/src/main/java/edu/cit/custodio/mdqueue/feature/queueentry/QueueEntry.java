package edu.cit.custodio.mdqueue.feature.queueentry;

import edu.cit.custodio.mdqueue.feature.user.User;

import edu.cit.custodio.mdqueue.feature.queue.QueueEntity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "queue_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueEntry {

    public enum Status {
        WAITING, SERVING, COMPLETED, CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "queue_id", nullable = false)
    private QueueEntity queue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @Column(name = "queue_number", nullable = false)
    private Integer queueNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.WAITING;

    @Column(name = "check_in_time", nullable = false, updatable = false)
    private LocalDateTime checkInTime;

    @Column(name = "served_time")
    private LocalDateTime servedTime;

    @Column(name = "completed_time")
    private LocalDateTime completedTime;

    @PrePersist
    protected void onCreate() {
        checkInTime = LocalDateTime.now();
    }
}
