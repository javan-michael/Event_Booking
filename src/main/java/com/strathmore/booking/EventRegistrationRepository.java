package com.strathmore.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    Optional<EventRegistration> findByUserAndEvent(User user, Event event);

    @Query("SELECT er FROM EventRegistration er JOIN er.event e WHERE er.user = :user AND e.eventDate >= :today ORDER BY e.eventDate ASC, e.eventTime ASC")
    List<EventRegistration> findUpcomingRegistrationsByUser(User user, LocalDate today);

    // Add method to find all registrations for an event, ordered by user name
    List<EventRegistration> findByEventOrderByUser_FullnameAsc(Event event);

}