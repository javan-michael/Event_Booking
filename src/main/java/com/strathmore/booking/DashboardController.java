package com.strathmore.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors; // Add this import

@Controller
public class DashboardController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired // Inject registration repository
    private EventRegistrationRepository registrationRepository;

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            User currentUser = customUserDetails.getUser();

            if (customUserDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                // Admin: Fetch events created by this admin
                List<Event> createdEvents = eventRepository.findByCreatedByOrderByEventDateDesc(currentUser);
                model.addAttribute("events", createdEvents);
                model.addAttribute("viewType", "admin");
            } else {
                // User: Fetch events they are registered for (upcoming only)
                LocalDate today = LocalDate.now();
                List<EventRegistration> registrations = registrationRepository.findUpcomingRegistrationsByUser(currentUser, today);
                // Extract just the Event objects from the registrations
                List<Event> registeredEvents = registrations.stream()
                        .map(EventRegistration::getEvent)
                        .collect(Collectors.toList());
                model.addAttribute("events", registeredEvents);
                model.addAttribute("viewType", "user");
            }
        } else {
            model.addAttribute("events", Collections.emptyList());
            model.addAttribute("viewType", "none");
        }

        return "dashboard";
    }
}