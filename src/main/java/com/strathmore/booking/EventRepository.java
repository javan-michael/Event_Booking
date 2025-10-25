package com.strathmore.booking;

import org.springframework.data.domain.Page; // For pagination
import org.springframework.data.domain.Pageable; // For pagination info (page size, number)
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository // Marks this as a Spring Data repository
public interface EventRepository extends JpaRepository<Event, Long> { // Manages Event entities with Long IDs


    List<Event> findTop3ByEventDateGreaterThanEqualOrderByEventDateAscEventTimeAsc(LocalDate today);


    Page<Event> findByEventDateGreaterThanEqual(LocalDate today, Pageable pageable);


    Page<Event> findByEventDateGreaterThanEqualAndTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            LocalDate today, String title, String description, Pageable pageable);


}