package com.strathmore.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime; // Add import
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    // Add method to find by valid token
    Optional<User> findByResetTokenAndResetTokenExpiresAtAfter(String token, LocalDateTime now);

}