package edu.cit.custodio.mdqueue.service;

import edu.cit.custodio.mdqueue.dto.RegisterRequest;
import edu.cit.custodio.mdqueue.entity.User;
import edu.cit.custodio.mdqueue.exception.DuplicateEmailException;
import edu.cit.custodio.mdqueue.repository.UserRepository;
import edu.cit.custodio.mdqueue.strategy.PasswordValidationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service handling user operations.
 * <p>
 * Uses the <b>Strategy Pattern</b> for password validation. The
 * {@link PasswordValidationStrategy} is injected via constructor injection,
 * allowing the validation algorithm to be swapped without modifying this class.
 * Currently, {@link edu.cit.custodio.mdqueue.strategy.BasicPasswordValidator} is
 * the default (marked as {@code @Primary}), but it can be replaced with
 * {@link edu.cit.custodio.mdqueue.strategy.StrongPasswordValidator} by changing
 * the {@code @Primary} annotation.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidationStrategy passwordValidator; // Strategy Pattern

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("An account with this email already exists");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password do not match");
        }

        // Strategy Pattern: delegate password validation to the injected strategy
        // The active strategy (Basic or Strong) is determined by Spring's @Primary annotation
        passwordValidator.validate(request.getPassword());

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase().trim())
                .phoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null)
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new edu.cit.custodio.mdqueue.exception.InvalidCredentialsException("Invalid email or password"));
    }
}

