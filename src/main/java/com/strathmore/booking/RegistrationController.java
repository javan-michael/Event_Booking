package com.strathmore.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class RegistrationController {

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserRepository userRepository; // Needed if using User object directly
    @Autowired
    private EventRegistrationRepository registrationRepository;

    @GetMapping("/register-for-event")
    public String registerForEvent(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof CustomUserDetails customUserDetails)) {
            return "redirect:/signin"; // Should not happen if security configured correctly
        }
        User currentUser = customUserDetails.getUser();

        Optional<Event> eventOpt = eventRepository.findById(id);
        if (eventOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "Event not found.");
            return "redirect:/events";
        }
        Event event = eventOpt.get();

        // Check if already registered
        Optional<EventRegistration> existingReg = registrationRepository.findByUserAndEvent(currentUser, event);

        if (existingReg.isPresent()) {
            redirectAttributes.addFlashAttribute("msg_type", "info");
            redirectAttributes.addFlashAttribute("msg", "You are already registered for this event.");
        } else {
            EventRegistration newRegistration = new EventRegistration();
            newRegistration.setUser(currentUser);
            newRegistration.setEvent(event);
            registrationRepository.save(newRegistration);
            redirectAttributes.addFlashAttribute("msg_type", "success");
            redirectAttributes.addFlashAttribute("msg", "Successfully registered for the event!");
        }

        return "redirect:/event-details?id=" + id;
    }

    @GetMapping("/cancel-registration")
    public String cancelRegistration(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof CustomUserDetails customUserDetails)) {
            return "redirect:/signin";
        }
        User currentUser = customUserDetails.getUser();

        Optional<Event> eventOpt = eventRepository.findById(id);
        if (eventOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "Event not found.");
            return "redirect:/dashboard"; // Redirect to dashboard if event unknown
        }
        Event event = eventOpt.get();

        Optional<EventRegistration> existingReg = registrationRepository.findByUserAndEvent(currentUser, event);

        if (existingReg.isPresent()) {
            registrationRepository.delete(existingReg.get());
            redirectAttributes.addFlashAttribute("msg_type", "success");
            redirectAttributes.addFlashAttribute("msg", "Your registration has been cancelled.");
        } else {
            redirectAttributes.addFlashAttribute("msg_type", "warning");
            redirectAttributes.addFlashAttribute("msg", "You were not registered for this event.");
        }

        // Redirect back to dashboard or event details? PHP went to dashboard.
        return "redirect:/dashboard";
        // Or: return "redirect:/event-details?id=" + id;
    }
}