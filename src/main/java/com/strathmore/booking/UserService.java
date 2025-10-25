package com.strathmore.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Add Value
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID; // Add UUID

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;

    @Value("${app.base-url:http://localhost:8088}") // Read base URL from properties
    private String appBaseUrl;


    public User registerNewUser(User user) throws Exception {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new Exception("Email already exists: " + user.getEmail());
        }
        if (user.getPassword() == null || !user.getPassword().equals(user.getPasswordRepeat())) {
            throw new Exception("Passwords do not match");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setVerified(false);
        String code = String.format("%06d", new Random().nextInt(999999));
        user.setVerificationCode(code);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));
        User savedUser = userRepository.save(user);
        emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getFullname(), code);
        return savedUser;
    }

    public boolean verifyUser(String email, String code) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!user.isVerified() && user.getVerificationCode() != null &&
                    user.getVerificationCode().equals(code) &&
                    user.getVerificationCodeExpiresAt() != null &&
                    user.getVerificationCodeExpiresAt().isAfter(LocalDateTime.now()))
            {
                user.setVerified(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiresAt(null);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    public void initiatePasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiresAt(LocalDateTime.now().plusHours(1)); // Expires in 1 hour
            userRepository.save(user);

            String resetLink = appBaseUrl + "/reset-password?token=" + token;
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullname(), resetLink);
        }
        // No exception thrown if user not found to prevent email enumeration
    }

    public void completePasswordReset(String token, String newPassword, String newPasswordRepeat) throws Exception {
        if (newPassword == null || newPassword.length() < 6) {
            throw new Exception("New password must be at least 6 characters long.");
        }
        if (!newPassword.equals(newPasswordRepeat)) {
            throw new Exception("New passwords do not match.");
        }

        Optional<User> userOpt = userRepository.findByResetTokenAndResetTokenExpiresAtAfter(token, LocalDateTime.now());
        if (userOpt.isEmpty()) {
            throw new Exception("Invalid or expired password reset token.");
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null); // Invalidate token
        user.setResetTokenExpiresAt(null);
        userRepository.save(user);
    }

    public void changeUserPassword(User user, String currentPassword, String newPassword, String newPasswordRepeat) throws Exception {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new Exception("Incorrect current password.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new Exception("New password must be at least 6 characters long.");
        }
        if (!newPassword.equals(newPasswordRepeat)) {
            throw new Exception("New passwords do not match.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}