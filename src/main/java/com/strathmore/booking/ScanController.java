package com.strathmore.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class ScanController {

    @Autowired
    private EventRegistrationRepository registrationRepository;

    // Define a simple class to hold scan result data
    public static class ScanResult {
        String status; // "success", "warning", "error"
        String message;
        String user;
        String event;
        LocalDateTime time;

        // Constructor for simple messages
        public ScanResult(String status, String message) {
            this.status = status;
            this.message = message;
        }

        // Constructor for detailed messages
        public ScanResult(String status, String message, String user, String event, LocalDateTime time) {
            this.status = status;
            this.message = message;
            this.user = user;
            this.event = event;
            this.time = time;
        }
        // Getters needed for Thymeleaf access
        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public String getUser() { return user; }
        public String getEvent() { return event; }
        public LocalDateTime getTime() { return time; }
    }

    @GetMapping("/scan-ticket")
    public String processScan(@RequestParam(required = false) String data, Model model) {
        ScanResult scanResult;

        if (data == null || !data.startsWith("RegID:")) {
            scanResult = new ScanResult("error", "No ticket data provided or invalid format.");
        } else {
            try {
                String regIdStr = data.substring(6); // Get ID part
                Long registrationId = Long.parseLong(regIdStr);

                Optional<EventRegistration> regOpt = registrationRepository.findById(registrationId);

                if (regOpt.isPresent()) {
                    EventRegistration registration = regOpt.get();
                    if (registration.getCheckedInAt() == null) {
                        // Success: Not checked in yet
                        LocalDateTime checkinTime = LocalDateTime.now();
                        registration.setCheckedInAt(checkinTime);
                        registrationRepository.save(registration);
                        scanResult = new ScanResult("success", "Check-in successful!",
                                registration.getUser().getFullname(),
                                registration.getEvent().getTitle(),
                                checkinTime);
                    } else {
                        // Warning: Already checked in
                        scanResult = new ScanResult("warning", "This ticket has already been checked in.",
                                registration.getUser().getFullname(),
                                registration.getEvent().getTitle(),
                                registration.getCheckedInAt());
                    }
                } else {
                    // Error: Registration ID not found
                    scanResult = new ScanResult("error", "Invalid Ticket: Registration not found.");
                }
            } catch (NumberFormatException e) {
                scanResult = new ScanResult("error", "Invalid Ticket Data Format.");
            } catch (Exception e) {
                scanResult = new ScanResult("error", "An unexpected error occurred during check-in.");
                e.printStackTrace(); // Log the full error
            }
        }

        model.addAttribute("scanResult", scanResult);
        return "scan-result";
    }
}