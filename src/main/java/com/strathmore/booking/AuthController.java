package com.strathmore.booking;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/signin")
    public String showSigninForm() {
        return "signin";
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        // Now uses User from the same package
        model.addAttribute("user", new User());
        return "signup";
    }

}