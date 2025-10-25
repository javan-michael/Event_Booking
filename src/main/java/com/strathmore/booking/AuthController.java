package com.strathmore.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam; // Added RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional; // Added Optional

@Controller
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired // Need repository for resend logic
    private UserRepository userRepository;
    @Autowired // Need email service for resend
    private EmailService emailService;


    @GetMapping("/signin")
    public String showSigninForm() {
        return "signin";
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(@ModelAttribute("user") User user,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (user.getPassword() == null || !user.getPassword().equals(user.getPasswordRepeat())) {
            Map<String, String> errors = new HashMap<>(); errors.put("passwordMatch", "Passwords do not match");
            redirectAttributes.addFlashAttribute("user", user); redirectAttributes.addFlashAttribute("errors", errors);
            redirectAttributes.addFlashAttribute("msg_type", "danger"); redirectAttributes.addFlashAttribute("msg", "Please fix errors.");
            return "redirect:/signup";
        }
        if (result.hasErrors()){
            redirectAttributes.addFlashAttribute("user", user); redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", result);
            redirectAttributes.addFlashAttribute("msg_type", "danger"); redirectAttributes.addFlashAttribute("msg", "Validation errors occurred.");
            return "redirect:/signup";
        }
        try {
            User registeredUser = userService.registerNewUser(user);
            redirectAttributes.addFlashAttribute("msg_type", "info");
            redirectAttributes.addFlashAttribute("msg", "Registration successful! Check your email for the verification code.");
            redirectAttributes.addFlashAttribute("email", registeredUser.getEmail()); // Send email to verify page
            return "redirect:/verify"; // Redirect to verify page
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>(); errors.put("registrationError", e.getMessage());
            redirectAttributes.addFlashAttribute("user", user); redirectAttributes.addFlashAttribute("errors", errors);
            redirectAttributes.addFlashAttribute("msg_type", "danger"); redirectAttributes.addFlashAttribute("msg", "Registration failed.");
            return "redirect:/signup";
        }
    }

    @GetMapping("/verify")
    public String showVerifyForm(@RequestParam(required = false) String email,
                                 @RequestParam(required = false) Boolean resend, // Check for resend flag
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        String targetEmail = (String) model.getAttribute("email"); // Check flash attribute first
        if (targetEmail == null && email != null) {
            targetEmail = email; // Use email from query param if no flash attribute
        }

        if (targetEmail == null) {
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "Email address is missing for verification.");
            return "redirect:/signup"; // Or signin?
        }

        // Handle resend request
        if (Boolean.TRUE.equals(resend)) {
            Optional<User> userOpt = userRepository.findByEmail(targetEmail);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (!user.isVerified() && user.getVerificationCode() != null && user.getVerificationCodeExpiresAt() != null) {
                    // Resend existing valid code
                    emailService.sendVerificationEmail(user.getEmail(), user.getFullname(), user.getVerificationCode());
                    redirectAttributes.addFlashAttribute("msg_type", "info");
                    redirectAttributes.addFlashAttribute("msg", "Verification code resent to your email.");
                } else if (!user.isVerified()) {
                    // Generate and send a new code if old one invalid/missing (optional)
                    // userService.generateAndSendNewCode(user); // Need to implement this in UserService
                    redirectAttributes.addFlashAttribute("msg_type", "info");
                    redirectAttributes.addFlashAttribute("msg", "Could not resend code. Please try signing up again if needed."); // Placeholder
                }
            } else {
                redirectAttributes.addFlashAttribute("msg_type", "danger");
                redirectAttributes.addFlashAttribute("msg", "User not found for resending code.");
            }
            redirectAttributes.addFlashAttribute("email", targetEmail);
            return "redirect:/verify"; // Redirect back to verify GET to show message
        }


        model.addAttribute("email", targetEmail); // Ensure email is in the model for the form
        return "verify";
    }

    @PostMapping("/verify")
    public String processVerify(@RequestParam String email,
                                @RequestParam String code,
                                RedirectAttributes redirectAttributes) {
        boolean verified = userService.verifyUser(email, code);

        if (verified) {
            redirectAttributes.addFlashAttribute("msg_type", "success");
            redirectAttributes.addFlashAttribute("msg", "Account verified successfully! You can now sign in.");
            return "redirect:/signin";
        } else {
            redirectAttributes.addFlashAttribute("email", email); // Send email back
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "Invalid or expired verification code.");
            return "redirect:/verify"; // Redirect back to verify GET
        }
    }
}