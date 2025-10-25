package com.strathmore.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;

    @GetMapping("/signin")
    public String showSigninForm() { return "signin"; }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(@ModelAttribute("user") User user, BindingResult result, RedirectAttributes redirectAttributes) {
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
            redirectAttributes.addFlashAttribute("email", registeredUser.getEmail());
            return "redirect:/verify";
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>(); errors.put("registrationError", e.getMessage());
            redirectAttributes.addFlashAttribute("user", user); redirectAttributes.addFlashAttribute("errors", errors);
            redirectAttributes.addFlashAttribute("msg_type", "danger"); redirectAttributes.addFlashAttribute("msg", "Registration failed.");
            return "redirect:/signup";
        }
    }

    @GetMapping("/verify")
    public String showVerifyForm(@RequestParam(required = false) String email, @RequestParam(required = false) Boolean resend, Model model, RedirectAttributes redirectAttributes) {
        String targetEmail = (String) model.getAttribute("email");
        if (targetEmail == null && email != null) { targetEmail = email; }
        if (targetEmail == null) {
            redirectAttributes.addFlashAttribute("msg_type", "danger"); redirectAttributes.addFlashAttribute("msg", "Email address is missing for verification.");
            return "redirect:/signup";
        }
        if (Boolean.TRUE.equals(resend)) {
            Optional<User> userOpt = userRepository.findByEmail(targetEmail);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (!user.isVerified() && user.getVerificationCode() != null && user.getVerificationCodeExpiresAt() != null) {
                    emailService.sendVerificationEmail(user.getEmail(), user.getFullname(), user.getVerificationCode());
                    redirectAttributes.addFlashAttribute("msg_type", "info"); redirectAttributes.addFlashAttribute("msg", "Verification code resent to your email.");
                } else if (!user.isVerified()) {
                    redirectAttributes.addFlashAttribute("msg_type", "info"); redirectAttributes.addFlashAttribute("msg", "Could not resend code. Please try signing up again if needed.");
                }
            } else {
                redirectAttributes.addFlashAttribute("msg_type", "danger"); redirectAttributes.addFlashAttribute("msg", "User not found for resending code.");
            }
            redirectAttributes.addFlashAttribute("email", targetEmail);
            return "redirect:/verify";
        }
        model.addAttribute("email", targetEmail);
        return "verify";
    }

    @PostMapping("/verify")
    public String processVerify(@RequestParam String email, @RequestParam String code, RedirectAttributes redirectAttributes) {
        boolean verified = userService.verifyUser(email, code);
        if (verified) {
            redirectAttributes.addFlashAttribute("msg_type", "success");
            redirectAttributes.addFlashAttribute("msg", "Account verified successfully! You can now sign in.");
            return "redirect:/signin";
        } else {
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "Invalid or expired verification code.");
            return "redirect:/verify";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
        userService.initiatePasswordReset(email);
        // Always show success to prevent email enumeration
        redirectAttributes.addFlashAttribute("msg_type", "success");
        redirectAttributes.addFlashAttribute("msg", "If an account with that email exists, a password reset link has been sent.");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam(required = false) String token, Model model, RedirectAttributes redirectAttributes) {
        if (token == null || token.isEmpty()) {
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "Invalid password reset request.");
            return "redirect:/signin";
        }
        // Optional: Check if token is valid here before showing form
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token,
                                       @RequestParam String newPassword,
                                       @RequestParam String newPasswordRepeat,
                                       RedirectAttributes redirectAttributes) {
        try {
            userService.completePasswordReset(token, newPassword, newPasswordRepeat);
            redirectAttributes.addFlashAttribute("msg_type", "success");
            redirectAttributes.addFlashAttribute("msg", "Password reset successfully. You can now sign in.");
            return "redirect:/signin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("token", token); // Send token back
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "Error: " + e.getMessage());
            return "redirect:/reset-password"; // Redirect back to GET
        }
    }
}