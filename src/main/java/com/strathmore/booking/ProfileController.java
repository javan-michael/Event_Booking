package com.strathmore.booking;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    @GetMapping("/profile")
    public String showProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            model.addAttribute("user", customUserDetails.getUser());
        } else {
            model.addAttribute("user", null); // Should not happen if user is logged in
        }
        return "profile";
    }

}