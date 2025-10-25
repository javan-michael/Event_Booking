package com.strathmore.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication; // Add
import org.springframework.security.core.context.SecurityContextHolder; // Add
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private EventRepository eventRepository;
    @Autowired // Add registration repo
    private EventRegistrationRepository registrationRepository;

    private static final int FEATURED_EVENTS_COUNT = 3;
    private static final int EVENTS_PER_PAGE = 9;

    @GetMapping("/")
    public String home(Model model) {
        LocalDate today = LocalDate.now();
        List<Event> featuredEvents = eventRepository.findTop3ByEventDateGreaterThanEqualOrderByEventDateAscEventTimeAsc(today);
        model.addAttribute("featured_events", featuredEvents);
        return "index";
    }

    @GetMapping("/events")
    public String events(Model model,
                         @RequestParam(required = false, defaultValue = "") String search,
                         @RequestParam(required = false, defaultValue = "1") int page) {

        LocalDate today = LocalDate.now();
        Pageable pageable = PageRequest.of(page - 1, EVENTS_PER_PAGE);
        Page<Event> eventPage;

        if (search == null || search.trim().isEmpty()) {
            eventPage = eventRepository.findByEventDateGreaterThanEqual(today, pageable);
        } else {
            eventPage = eventRepository.findByEventDateGreaterThanEqualAndTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    today, search, search, pageable);
        }

        model.addAttribute("events", eventPage.getContent());
        model.addAttribute("search_term", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", eventPage.getTotalPages());

        return "events";
    }

    @GetMapping("/event-details")
    public String eventDetails(@RequestParam Long id, Model model) {
        Optional<Event> optionalEvent = eventRepository.findById(id);
        boolean isRegistered = false; // Default to not registered

        if (optionalEvent.isPresent()) {
            Event event = optionalEvent.get();
            model.addAttribute("event", event);

            // Check registration status if user is logged in
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
                User currentUser = customUserDetails.getUser();
                isRegistered = registrationRepository.findByUserAndEvent(currentUser, event).isPresent();
            }
        } else {
            model.addAttribute("event", null);
        }

        model.addAttribute("isRegistered", isRegistered); // Pass status to template
        return "event-details";
    }
}