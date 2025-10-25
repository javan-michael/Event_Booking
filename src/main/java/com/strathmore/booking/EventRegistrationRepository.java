package com.strathmore.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    // Find registrations for a specific user and event
    Optional<EventRegistration> findByUserAndEvent(User user, Event event);

    // Find all upcoming registrations for a user
    @Query("SELECT er FROM EventRegistration er JOIN er.event e WHERE er.user = :user AND e.eventDate >= :today ORDER BY e.eventDate ASC, e.eventTime ASC")
    List<EventRegistration> findUpcomingRegistrationsByUser(User user, LocalDate today);

}