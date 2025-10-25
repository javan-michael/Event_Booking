package com.strathmore.booking;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime; // Import LocalDateTime

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'user'")
    private String role = "user";

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean verified = false;

    @Transient
    private String passwordRepeat;

    // New fields for verification
    private String verificationCode;
    private LocalDateTime verificationCodeExpiresAt;

}