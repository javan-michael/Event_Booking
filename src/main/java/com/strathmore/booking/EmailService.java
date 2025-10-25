package com.strathmore.booking;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.properties.mail.from:noreply@example.com}")
    private String fromAddress;

    private String fromName = "StrathEventique Team";

    public void sendVerificationEmail(String toEmail, String fullname, String code) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Activate Your StrathEventique Account");
            String text = String.format(
                    "Hello %s,\n\nYour activation code is: %s\n\nRegards,\n%s",
                    fullname, code, fromName
            );
            helper.setText(text);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.err.println("Error sending verification email: " + e.getMessage());
        }
    }

    public void sendPasswordResetEmail(String toEmail, String fullname, String resetLink) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request for StrathEventique");
            // Basic text version, can use HTML later
            String text = String.format(
                    "Hello %s,\n\nWe received a request to reset your password. If you did not make this request, please ignore this email.\n\n" +
                            "Click the link below to reset your password (link expires in 1 hour):\n%s\n\nRegards,\n%s",
                    fullname, resetLink, fromName
            );
            helper.setText(text, false); // Set HTML flag to false for plain text
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.err.println("Error sending password reset email: " + e.getMessage());
        }
    }
}