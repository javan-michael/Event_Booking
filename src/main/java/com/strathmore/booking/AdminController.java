package com.strathmore.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin") // Prefix all routes in this controller with /admin
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll(); // Consider sorting or pagination for many users
        model.addAttribute("users", users);
        return "admin-users";
    }

    @PostMapping("/change-role")
    public String changeUserRole(@RequestParam Long userId,
                                 @RequestParam String newRole,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails customUserDetails)) {
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "Could not identify current admin user.");
            return "redirect:/admin/users";
        }
        User currentAdmin = customUserDetails.getUser();

        // Prevent admin from demoting themselves
        if (currentAdmin.getId().equals(userId) && !"admin".equalsIgnoreCase(newRole)) {
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "You cannot demote your own admin account.");
            return "redirect:/admin/users";
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User userToChange = userOpt.get();
            // Basic validation for role value
            if ("admin".equalsIgnoreCase(newRole) || "user".equalsIgnoreCase(newRole)) {
                userToChange.setRole(newRole.toLowerCase());
                userRepository.save(userToChange);
                redirectAttributes.addFlashAttribute("msg_type", "success");
                redirectAttributes.addFlashAttribute("msg", "User role updated successfully.");
            } else {
                redirectAttributes.addFlashAttribute("msg_type", "danger");
                redirectAttributes.addFlashAttribute("msg", "Invalid role specified.");
            }
        } else {
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "User not found.");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/delete-user")
    public String deleteUser(@RequestParam Long userId,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails customUserDetails)) {
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "Could not identify current admin user.");
            return "redirect:/admin/users";
        }
        User currentAdmin = customUserDetails.getUser();

        // Prevent admin from deleting themselves
        if (currentAdmin.getId().equals(userId)) {
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "You cannot delete your own admin account.");
            return "redirect:/admin/users";
        }

        if (userRepository.existsById(userId)) {
            // Consider implications: Delete registrations? Orphaned events?
            // Simple delete for now:
            try {
                userRepository.deleteById(userId);
                redirectAttributes.addFlashAttribute("msg_type", "success");
                redirectAttributes.addFlashAttribute("msg", "User deleted successfully.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("msg_type", "danger");
                redirectAttributes.addFlashAttribute("msg", "Error deleting user. Check dependencies (e.g., event registrations).");
            }

        } else {
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "User not found.");
        }
        return "redirect:/admin/users";
    }
}