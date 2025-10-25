package com.strathmore.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Import Value
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper; // Import MimeMessageHelper
import jakarta.mail.internet.MimeMessage; // Import MimeMessage
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Read the 'from' address from application.properties
    @Value("${spring.mail.properties.mail.from:noreply@example.com}") // Default if not set
    private String fromAddress;

    private String fromName = "StrathEventique Team"; // Define the desired sender name

    public void sendVerificationEmail(String toEmail, String fullname, String code) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom(fromAddress, fromName); // Set both address and name
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
}