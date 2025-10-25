package com.strathmore.booking;

import org.springframework.beans.factory.annotation.Autowired; // Add Autowired
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping; // Add PostMapping
import org.springframework.web.bind.annotation.RequestParam; // Add RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Add RedirectAttributes

@Controller
public class ProfileController {

    @Autowired // Inject UserService
    private UserService userService;

    @GetMapping("/profile")
    public String showProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            model.addAttribute("user", customUserDetails.getUser());
        } else {
            model.addAttribute("user", null);
        }
        return "profile";
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm() {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String processChangePassword(@RequestParam String currentPassword,
                                        @RequestParam String newPassword,
                                        @RequestParam String newPasswordRepeat,
                                        Authentication authentication, // Get current user
                                        RedirectAttributes redirectAttributes) {

        if (!(authentication.getPrincipal() instanceof CustomUserDetails customUserDetails)) {
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "Could not identify user.");
            return "redirect:/change-password";
        }
        User currentUser = customUserDetails.getUser();

        try {
            userService.changeUserPassword(currentUser, currentPassword, newPassword, newPasswordRepeat);
            redirectAttributes.addFlashAttribute("msg_type", "success");
            redirectAttributes.addFlashAttribute("msg", "Password changed successfully.");
            return "redirect:/profile"; // Redirect back to profile on success
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "Error: " + e.getMessage());
            return "redirect:/change-password"; // Stay on page on error
        }
    }
}