package edu.cit.custodio.mdqueue.event;

import edu.cit.custodio.mdqueue.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Observer Pattern: Publisher (Subject) that broadcasts authentication events.
 * <p>
 * This class acts as the <b>Subject/Publisher</b> in the Observer pattern.
 * It uses Spring's {@link ApplicationEventPublisher} to decouple the act of
 * authentication from any side-effects (logging, analytics, welcome emails).
 * </p>
 * <p>
 * When a user logs in or registers, this publisher fires an {@link AuthEvent}.
 * Any number of listeners (Observers) can subscribe to these events without
 * the publisher knowing about them — achieving true loose coupling.
 * </p>
 *
 * @see AuthEvent
 * @see AuthEventListener
 */
@Component
@RequiredArgsConstructor
public class AuthEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Publishes a LOGIN event for the given user.
     *
     * @param user the user who logged in
     */
    public void publishLoginEvent(User user) {
        AuthEvent event = new AuthEvent(
                this,
                user.getEmail(),
                user.getFullName(),
                AuthEvent.EventType.LOGIN
        );
        eventPublisher.publishEvent(event);
    }

    /**
     * Publishes a REGISTER event for the given user.
     *
     * @param user the newly registered user
     */
    public void publishRegisterEvent(User user) {
        AuthEvent event = new AuthEvent(
                this,
                user.getEmail(),
                user.getFullName(),
                AuthEvent.EventType.REGISTER
        );
        eventPublisher.publishEvent(event);
    }
}
