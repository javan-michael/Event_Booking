package com.strathmore.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findTop3ByEventDateGreaterThanEqualOrderByEventDateAscEventTimeAsc(LocalDate today);

    Page<Event> findByEventDateGreaterThanEqual(LocalDate today, Pageable pageable);

    Page<Event> findByEventDateGreaterThanEqualAndTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            LocalDate today, String title, String description, Pageable pageable);

    // New method for admin dashboard
    List<Event> findByCreatedByOrderByEventDateDesc(User user);

}