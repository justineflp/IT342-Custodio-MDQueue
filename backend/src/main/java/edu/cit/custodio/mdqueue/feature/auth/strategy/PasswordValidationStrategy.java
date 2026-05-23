package edu.cit.custodio.mdqueue.feature.auth.strategy;

/**
 * Strategy Pattern: Interface defining the contract for password validation algorithms.
 * <p>
 * This is the <b>Strategy Interface</b> in the Strategy design pattern. It declares
 * a common method {@link #validate(String)} that all concrete password validation
 * strategies must implement. The context ({@link edu.cit.custodio.mdqueue.feature.user.UserService})
 * holds a reference to this interface and delegates validation to whichever concrete
 * strategy is injected — without knowing which specific implementation is being used.
 * </p>
 * <p>
 * This design allows password validation rules to be changed by swapping the
 * injected bean, without modifying any service code. New strategies can be added
 * by simply creating a new class that implements this interface.
 * </p>
 *
 * @see BasicPasswordValidator
 * @see StrongPasswordValidator
 */
public interface PasswordValidationStrategy {

    /**
     * Validates the given password against the strategy's rules.
     *
     * @param password the password to validate
     * @throws IllegalArgumentException if the password does not meet the requirements
     */
    void validate(String password) throws IllegalArgumentException;
}
