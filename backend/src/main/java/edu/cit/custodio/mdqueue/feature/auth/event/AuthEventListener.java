package edu.cit.custodio.mdqueue.feature.auth.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observer Pattern: Listener (Observer) that reacts to authentication events.
 * <p>
 * This class acts as an <b>Observer/Subscriber</b> in the Observer pattern.
 * It listens for {@link AuthEvent} instances published by the
 * {@link AuthEventPublisher} and reacts accordingly — without any direct
 * coupling to the authentication service.
 * </p>
 * <p>
 * Benefits of this approach:
 * <ul>
 *   <li>New side-effects can be added by creating new listener classes</li>
 *   <li>Existing listeners can be modified without touching AuthService</li>
 *   <li>Listeners can be individually enabled/disabled via Spring profiles</li>
 *   <li>The authentication flow remains focused on its core responsibility</li>
 * </ul>
 * </p>
 *
 * @see AuthEvent
 * @see AuthEventPublisher
 */
@Component
public class AuthEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuthEventListener.class);

    /**
     * Handles authentication events by logging the activity.
     * <p>
     * This method is automatically invoked by Spring's event system whenever
     * an {@link AuthEvent} is published. Additional listeners (e.g., for sending
     * welcome emails, updating analytics) can be added as separate methods or
     * classes without modifying this one.
     * </p>
     *
     * @param event the authentication event
     */
    @EventListener
    public void onAuthEvent(AuthEvent event) {
        switch (event.getEventType()) {
            case LOGIN:
                log.info("[AUTH EVENT] User logged in - Email: {}, Name: {}, Time: {}",
                        event.getEmail(), event.getFullName(), event.getEventTimestamp());
                break;
            case REGISTER:
                log.info("[AUTH EVENT] New user registered - Email: {}, Name: {}, Time: {}",
                        event.getEmail(), event.getFullName(), event.getEventTimestamp());
                // Future: send welcome email, create default preferences, etc.
                break;
            default:
                log.warn("[AUTH EVENT] Unknown event type: {}", event.getEventType());
        }
    }
}
