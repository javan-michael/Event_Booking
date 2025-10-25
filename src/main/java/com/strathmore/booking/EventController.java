package com.strathmore.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional; // Add Optional
import java.util.UUID;

@Controller
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/events/";

    @GetMapping("/create-event")
    public String showCreateEventForm(Model model) {
        model.addAttribute("event", new Event());
        return "create-event";
    }

    @PostMapping("/create-event")
    public String createEvent(@ModelAttribute Event event,
                              @RequestParam("eventImageFile") MultipartFile file,
                              RedirectAttributes redirectAttributes,
                              Authentication authentication) {

        if (!(authentication.getPrincipal() instanceof CustomUserDetails customUserDetails)) {
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "Could not identify logged-in user.");
            return "redirect:/create-event";
        }
        User currentUser = customUserDetails.getUser();
        event.setCreatedBy(currentUser);

        String imagePath = handleImageUpload(file, redirectAttributes);
        if (imagePath == null && !file.isEmpty()) { // Check if upload failed
            return "redirect:/create-event";
        }
        event.setImagePath(imagePath);

        try {
            eventRepository.save(event);
            redirectAttributes.addFlashAttribute("msg_type", "success");
            redirectAttributes.addFlashAttribute("msg", "Event created successfully!");
            return "redirect:/dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "Error saving event: " + e.getMessage());
            return "redirect:/create-event";
        }
    }

    @GetMapping("/edit-event")
    public String showEditEventForm(@RequestParam Long id, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        Optional<Event> eventOpt = eventRepository.findById(id);
        if (eventOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "Event not found.");
            return "redirect:/dashboard";
        }
        Event event = eventOpt.get();

        // Security Check: Ensure the logged-in user created this event
        if (!isUserEventCreator(authentication, event)) {
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "You do not have permission to edit this event.");
            return "redirect:/dashboard";
        }

        model.addAttribute("event", event);
        return "edit-event";
    }

    @PostMapping("/edit-event")
    public String editEvent(@ModelAttribute Event eventFormData, // Receives form data (including ID)
                            @RequestParam("eventImageFile") MultipartFile file,
                            RedirectAttributes redirectAttributes,
                            Authentication authentication) {

        Optional<Event> existingEventOpt = eventRepository.findById(eventFormData.getId());
        if (existingEventOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "Event not found.");
            return "redirect:/dashboard";
        }
        Event existingEvent = existingEventOpt.get();

        // Security Check
        if (!isUserEventCreator(authentication, existingEvent)) {
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "You do not have permission to edit this event.");
            return "redirect:/dashboard";
        }

        // Handle image upload only if a new file is provided
        String newImagePath = null;
        if (!file.isEmpty()) {
            newImagePath = handleImageUpload(file, redirectAttributes);
            if (newImagePath == null) { // Upload failed
                // Add ID back for redirect URL
                redirectAttributes.addAttribute("id", eventFormData.getId());
                return "redirect:/edit-event";
            }
            // Optionally delete old image file here if needed
            existingEvent.setImagePath(newImagePath); // Update path only if new image uploaded
        } // else keep existingEvent.getImagePath()

        // Update fields from form data
        existingEvent.setTitle(eventFormData.getTitle());
        existingEvent.setDescription(eventFormData.getDescription());
        existingEvent.setEventDate(eventFormData.getEventDate());
        existingEvent.setEventTime(eventFormData.getEventTime());
        existingEvent.setLocation(eventFormData.getLocation());
        // createdBy should not change

        try {
            eventRepository.save(existingEvent); // Save updated event
            redirectAttributes.addFlashAttribute("msg_type", "success");
            redirectAttributes.addFlashAttribute("msg", "Event updated successfully!");
            return "redirect:/dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "Error updating event: " + e.getMessage());
            redirectAttributes.addAttribute("id", eventFormData.getId()); // Add ID for redirect
            return "redirect:/edit-event";
        }
    }

    @PostMapping("/delete-event")
    public String deleteEvent(@RequestParam Long id, // Receive ID from hidden input or path variable
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {

        Optional<Event> eventOpt = eventRepository.findById(id);
        if (eventOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "Event not found.");
            return "redirect:/dashboard";
        }
        Event eventToDelete = eventOpt.get();

        // Security Check
        if (!isUserEventCreator(authentication, eventToDelete)) {
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "You do not have permission to delete this event.");
            return "redirect:/dashboard";
        }

        try {
            // Consider deleting related registrations first if needed
            // registrationRepository.deleteByEvent(eventToDelete);
            eventRepository.deleteById(id);
            // Optionally delete image file here
            redirectAttributes.addFlashAttribute("msg_type", "success");
            redirectAttributes.addFlashAttribute("msg", "Event deleted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "Error deleting event. Check dependencies (e.g., registrations).");
        }

        return "redirect:/dashboard";
    }


    // Helper method for image upload logic
    private String handleImageUpload(MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            return null; // No file uploaded or empty file
        }
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath);
            return "uploads/events/" + uniqueFileName; // Return relative path
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "Error uploading image: " + e.getMessage());
            return null; // Indicate failure
        }
    }

    // Helper for security check
    private boolean isUserEventCreator(Authentication authentication, Event event) {
        if (event.getCreatedBy() == null || !(authentication.getPrincipal() instanceof CustomUserDetails customUserDetails)) {
            return false;
        }
        User currentUser = customUserDetails.getUser();
        // Check if the event's creator ID matches the current user's ID
        // Also allow if the current user is an admin (in case admins can edit any event)
        return event.getCreatedBy().getId().equals(currentUser.getId()) ||
                customUserDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}