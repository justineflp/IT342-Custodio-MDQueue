package edu.cit.custodio.mdqueue.feature.auth.strategy;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Strategy Pattern: Basic password validation strategy.
 * <p>
 * This is a <b>Concrete Strategy</b> that implements the simplest password
 * validation rule — checking minimum length (8 characters). It is marked
 * as {@code @Primary}, making it the default strategy injected by Spring
 * when no specific qualifier is provided.
 * </p>
 * <p>
 * To switch to a stricter policy, either remove {@code @Primary} from this class and
 * add it to {@link StrongPasswordValidator}, or use Spring profiles to control
 * which strategy is active.
 * </p>
 */
@Component
@Primary
public class BasicPasswordValidator implements PasswordValidationStrategy {

    private static final int MIN_LENGTH = 8;

    /**
     * Validates that the password meets the basic minimum length requirement.
     *
     * @param password the password to validate
     * @throws IllegalArgumentException if the password is shorter than 8 characters
     */
    @Override
    public void validate(String password) throws IllegalArgumentException {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must be at least " + MIN_LENGTH + " characters long");
        }
    }
}
