package com.strathmore.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime; // Import LocalDateTime
import java.util.Optional;
import java.util.Random; // Import Random

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService; // Inject EmailService

    public User registerNewUser(User user) throws Exception {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new Exception("Email already exists: " + user.getEmail());
        }
        if (user.getPassword() == null || !user.getPassword().equals(user.getPasswordRepeat())) {
            throw new Exception("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setVerified(false); // Ensure user starts as not verified

        // Generate verification code
        String code = String.format("%06d", new Random().nextInt(999999));
        user.setVerificationCode(code);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1)); // Code expires in 1 hour

        User savedUser = userRepository.save(user);

        // Send email AFTER saving the user
        emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getFullname(), code);

        return savedUser;
    }

    public boolean verifyUser(String email, String code) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!user.isVerified() &&
                    user.getVerificationCode() != null &&
                    user.getVerificationCode().equals(code) &&
                    user.getVerificationCodeExpiresAt() != null &&
                    user.getVerificationCodeExpiresAt().isAfter(LocalDateTime.now()))
            {
                user.setVerified(true);
                user.setVerificationCode(null); // Clear code after verification
                user.setVerificationCodeExpiresAt(null);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }
    public void changeUserPassword(User user, String currentPassword, String newPassword, String newPasswordRepeat) throws Exception {
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new Exception("Incorrect current password.");
        }
        // Validate new password
        if (newPassword == null || newPassword.length() < 6) { // Add length check if desired
            throw new Exception("New password must be at least 6 characters long.");
        }
        if (!newPassword.equals(newPasswordRepeat)) {
            throw new Exception("New passwords do not match.");
        }

        // Hash and update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}