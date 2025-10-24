package com.strathmore.booking.controller;

// Import necessary classes
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam; // Needed for search and page
import java.util.ArrayList; // For sending empty lists

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        // We'll fetch real featured events later
        model.addAttribute("featured_events", null);
        return "index";
    }

    // Handles requests like "/events" or "/events?page=2&search=workshop"
    @GetMapping("/events")
    public String events(Model model,
                         // @RequestParam gets values from the URL
                         @RequestParam(required = false, defaultValue = "") String search,
                         @RequestParam(required = false, defaultValue = "1") int page) {

        // --- Temporary Data ---
        // Right now, we don't have the database code to fetch events.
        // So, we send empty/default values to prevent Thymeleaf errors.
        model.addAttribute("events", new ArrayList<>()); // Send an empty list
        model.addAttribute("search_term", search);      // Send back the search term
        model.addAttribute("currentPage", page);        // Send the current page number
        model.addAttribute("totalPages", 1);          // Assume only 1 page for now
        // --- End Temporary Data ---

        return "events"; // Return the events.html template
    }
}