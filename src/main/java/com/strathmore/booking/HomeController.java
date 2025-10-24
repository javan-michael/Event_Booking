package com.strathmore.booking;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // Tells Spring this is a web controller
public class HomeController {

    // This function "listens" for requests to the homepage ("/")
    @GetMapping("/")
    public String home(Model model) {

        // Your index.html loops over "featured_events".
        // We must send an empty list (or null) so the page doesn't crash.
        // We will add real data here later.
        model.addAttribute("featured_events", null);

        // This tells Spring Boot to return the file named "index.html"
        // Thymeleaf will now automatically wrap it with "layout.html"
        return "index";
    }

}