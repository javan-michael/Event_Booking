package com.strathmore.booking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
// We'll create UserDto later
// import com.strathmore.booking.dto.UserDto;

@Controller
public class AuthController {

    // Show the signin page
    @GetMapping("/signin")
    public String showSigninForm() {
        return "signin"; // Return signin.html
    }

    // Show the signup page
    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        // We need to send an empty object for the form to bind to
        // model.addAttribute("userDto", new UserDto()); // We'll uncomment this later
        return "signup"; // Return signup.html
    }

    // We will add POST methods for signup/verification later
}