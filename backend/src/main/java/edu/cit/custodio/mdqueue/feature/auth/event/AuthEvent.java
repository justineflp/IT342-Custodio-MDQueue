package edu.cit.custodio.mdqueue.feature.auth.event;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Observer Pattern: Custom application event representing an authentication action.
 * <p>
 * This class is the <b>Event/Notification</b> in the Observer pattern. It carries
 * the data payload that is published by the Subject ({@link AuthEventPublisher})
 * and received by Observers ({@link AuthEventListener}).
 * </p>
 * <p>
 * By extending Spring's {@link ApplicationEvent}, this event integrates with
 * Spring's built-in event infrastructure, enabling loose coupling between
 * the authentication logic and its side-effects (logging, notifications, etc.).
 * </p>
 */
public class AuthEvent extends ApplicationEvent {

    /**
     * Enum representing the type of authentication event.
     */
    public enum EventType {
        LOGIN,
        REGISTER
    }

    private final String email;
    private final String fullName;
    private final EventType eventType;
    private final LocalDateTime eventTimestamp;

    /**
     * Creates a new AuthEvent.
     *
     * @param source    the object that published the event
     * @param email     the user's email
     * @param fullName  the user's full name
     * @param eventType the type of auth event (LOGIN or REGISTER)
     */
    public AuthEvent(Object source, String email, String fullName, EventType eventType) {
        super(source);
        this.email = email;
        this.fullName = fullName;
        this.eventType = eventType;
        this.eventTimestamp = LocalDateTime.now();
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public EventType getEventType() {
        return eventType;
    }

    public LocalDateTime getEventTimestamp() {
        return eventTimestamp;
    }
}
