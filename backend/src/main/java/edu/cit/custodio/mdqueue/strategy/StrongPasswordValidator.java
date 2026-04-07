package edu.cit.custodio.mdqueue.strategy;

import org.springframework.stereotype.Component;

/**
 * Strategy Pattern: Strong password validation strategy.
 * <p>
 * This is an alternative <b>Concrete Strategy</b> that enforces stricter password
 * rules including minimum length, uppercase letters, digits, and special characters.
 * </p>
 * <p>
 * To activate this strategy as the default, add {@code @Primary} to this class
 * and remove it from {@link BasicPasswordValidator}. Both strategies implement
 * the same {@link PasswordValidationStrategy} interface, so they are fully
 * interchangeable — this is the core benefit of the Strategy pattern.
 * </p>
 */
@Component
public class StrongPasswordValidator implements PasswordValidationStrategy {

    private static final int MIN_LENGTH = 8;

    /**
     * Validates that the password meets strong security requirements:
     * <ul>
     *   <li>At least 8 characters long</li>
     *   <li>At least one uppercase letter</li>
     *   <li>At least one digit</li>
     *   <li>At least one special character (!@#$%^&*)</li>
     * </ul>
     *
     * @param password the password to validate
     * @throws IllegalArgumentException if any requirement is not met
     */
    @Override
    public void validate(String password) throws IllegalArgumentException {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must be at least " + MIN_LENGTH + " characters long");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException(
                    "Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException(
                    "Password must contain at least one digit");
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new IllegalArgumentException(
                    "Password must contain at least one special character");
        }
    }
}
