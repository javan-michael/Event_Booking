package com.strathmore.booking;

import org.springframework.beans.factory.annotation.Autowired; // For injecting the repository
import org.springframework.data.domain.Page; // For pagination result
import org.springframework.data.domain.PageRequest; // For creating pagination requests
import org.springframework.data.domain.Pageable; // For pagination info
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

import java.time.LocalDate;
import java.util.Optional; // For handling event not found

@Controller
public class HomeController {

    // Spring Boot automatically creates an instance of EventRepository and injects it here
    @Autowired
    private EventRepository eventRepository;

    private static final int FEATURED_EVENTS_COUNT = 3;
    private static final int EVENTS_PER_PAGE = 9; // Matches your events.php

    @GetMapping("/")
    public String home(Model model) {
        LocalDate today = LocalDate.now();
        // Use the repository to find the top 3 upcoming events
        List<Event> featuredEvents = eventRepository.findTop3ByEventDateGreaterThanEqualOrderByEventDateAscEventTimeAsc(today);
        model.addAttribute("featured_events", featuredEvents);
        return "index";
    }

    @GetMapping("/events")
    public String events(Model model,
                         @RequestParam(required = false, defaultValue = "") String search,
                         // Spring Boot automatically adjusts page numbers (0-based internally)
                         @RequestParam(required = false, defaultValue = "1") int page) {

        LocalDate today = LocalDate.now();
        // Create a Pageable object (page numbers are 0-based in Spring Data JPA)
        Pageable pageable = PageRequest.of(page - 1, EVENTS_PER_PAGE);
        Page<Event> eventPage; // This object holds the events for the current page AND pagination info

        if (search == null || search.trim().isEmpty()) {
            // No search term: Find all future events
            eventPage = eventRepository.findByEventDateGreaterThanEqual(today, pageable);
        } else {
            // Search term provided: Find matching future events
            eventPage = eventRepository.findByEventDateGreaterThanEqualAndTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    today, search, search, pageable);
        }

        model.addAttribute("events", eventPage.getContent()); // Get the list of events for the page
        model.addAttribute("search_term", search);
        model.addAttribute("currentPage", page); // Send the 1-based page number to Thymeleaf
        model.addAttribute("totalPages", eventPage.getTotalPages()); // Get total pages from the Page object

        return "events";
    }

    @GetMapping("/event-details")
    public String eventDetails(@RequestParam Long id, Model model) {
        // Use the repository to find the event by ID
        Optional<Event> optionalEvent = eventRepository.findById(id);

        if (optionalEvent.isPresent()) {
            Event event = optionalEvent.get();
            model.addAttribute("event", event);
            // We'll add logic to check registration status later
            // For now, assume not registered
            model.addAttribute("isRegistered", false);
        } else {
            model.addAttribute("event", null); // Event not found
        }
        return "event-details";
    }
}