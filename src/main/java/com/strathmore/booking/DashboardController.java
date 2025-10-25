package com.strathmore.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private EventRepository eventRepository;

    // Need UserRepository to link events to creator (admin)
    // Need EventRegistrationRepository later for user view

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            User currentUser = customUserDetails.getUser();
            List<Event> events;

            if (customUserDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                // Admin: Fetch events created by this admin (Need user_id in Event entity later)
                // For now, fetch all events as a placeholder
                events = eventRepository.findAll(); // Replace with findByCreatedBy(currentUser) later
                model.addAttribute("viewType", "admin");
            } else {
                // User: Fetch events they are registered for (Need registration logic later)
                // For now, show an empty list
                events = Collections.emptyList(); // Replace with findRegisteredEventsByUserId(currentUser.getId()) later
                model.addAttribute("viewType", "user");
            }
            model.addAttribute("events", events);

        } else {
            model.addAttribute("events", Collections.emptyList());
            model.addAttribute("viewType", "none"); // Or handle error
        }

        return "dashboard";
    }
}