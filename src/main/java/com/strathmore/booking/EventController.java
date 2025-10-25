package com.strathmore.booking;

import org.springframework.beans.factory.annotation.Autowired;
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
                              RedirectAttributes redirectAttributes) {

        String imagePath = null;
        if (!file.isEmpty()) {
            try {
                // Ensure upload directory exists
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Create a unique filename
                String originalFilename = file.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                Path filePath = uploadPath.resolve(uniqueFileName);

                // Save the file
                Files.copy(file.getInputStream(), filePath);

                // Store relative path for the entity
                imagePath = "uploads/events/" + uniqueFileName;

            } catch (IOException e) {
                // Handle file saving error
                e.printStackTrace(); // Log the error
                redirectAttributes.addFlashAttribute("msg_type", "danger");
                redirectAttributes.addFlashAttribute("msg", "Error uploading image: " + e.getMessage());
                return "redirect:/create-event"; // Stay on the create page
            }
        }

        event.setImagePath(imagePath);

        try {
            eventRepository.save(event);
            redirectAttributes.addFlashAttribute("msg_type", "success");
            redirectAttributes.addFlashAttribute("msg", "Event created successfully!");
            return "redirect:/dashboard"; // Redirect to dashboard after success
        } catch (Exception e) {
            // Handle database saving error
            e.printStackTrace(); // Log the error
            redirectAttributes.addFlashAttribute("msg_type", "danger");
            redirectAttributes.addFlashAttribute("msg", "Error saving event: " + e.getMessage());
            return "redirect:/create-event"; // Stay on the create page
        }
    }
}