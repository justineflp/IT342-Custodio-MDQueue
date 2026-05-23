package edu.cit.custodio.mdqueue.feature.user;

import edu.cit.custodio.mdqueue.feature.auth.dto.RegisterRequest;
import edu.cit.custodio.mdqueue.feature.user.User;
import edu.cit.custodio.mdqueue.feature.auth.exception.DuplicateEmailException;
import edu.cit.custodio.mdqueue.feature.user.UserRepository;
import edu.cit.custodio.mdqueue.feature.auth.strategy.PasswordValidationStrategy;
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
 * Currently, {@link edu.cit.custodio.mdqueue.feature.auth.strategy.BasicPasswordValidator} is
 * the default (marked as {@code @Primary}), but it can be replaced with
 * {@link edu.cit.custodio.mdqueue.feature.auth.strategy.StrongPasswordValidator} by changing
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

        // Parse role — default to PATIENT if not specified
        User.Role role = User.Role.PATIENT;
        if (request.getRole() != null && !request.getRole().isBlank()) {
            try {
                role = User.Role.valueOf(request.getRole().toUpperCase().trim());
            } catch (IllegalArgumentException ignored) {
                // Invalid role defaults to PATIENT
            }
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase().trim())
                .phoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .isApproved(role != User.Role.DOCTOR)
                .build();

        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new edu.cit.custodio.mdqueue.feature.auth.exception.InvalidCredentialsException("Invalid email or password"));
    }

    public java.util.List<User> getDoctors() {
        return userRepository.findByRoleAndIsApprovedTrue(User.Role.DOCTOR);
    }

    public java.util.List<User> getAllDoctors() {
        return userRepository.findByRole(User.Role.DOCTOR);
    }

    @Transactional
    public User approveDoctor(Long doctorId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        if (doctor.getRole() != User.Role.DOCTOR) {
            throw new IllegalArgumentException("User is not a doctor");
        }
        doctor.setApproved(true);
        return userRepository.save(doctor);
    }

    @Transactional
    public User updateSpecialty(Long doctorId, String specialty) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        if (doctor.getRole() != User.Role.DOCTOR) {
            throw new IllegalArgumentException("User is not a doctor");
        }
        doctor.setSpecialty(specialty);
        return userRepository.save(doctor);
    }
}

