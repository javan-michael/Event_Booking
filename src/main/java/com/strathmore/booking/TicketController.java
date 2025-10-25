package com.strathmore.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Add Value import
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class TicketController {

    @Autowired
    private EventRegistrationRepository registrationRepository;
    @Autowired
    private QrCodeService qrCodeService;

    // Inject the base URL from application.properties
    @Value("${app.base-url:http://localhost:8088}")
    private String appBaseUrl;

    @GetMapping("/my-tickets")
    public String showMyTickets(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            User currentUser = customUserDetails.getUser();
            LocalDate today = LocalDate.now();
            List<EventRegistration> registrations = registrationRepository.findUpcomingRegistrationsByUser(currentUser, today);

            List<RegistrationViewModel> registrationViewModels = registrations.stream()
                    .map(reg -> {
                        // Construct the FULL URL for the QR code
                        String qrDataUrl = appBaseUrl + "/scan-ticket?data=RegID:" + reg.getId();
                        String qrBase64 = qrCodeService.generateQrCodeBase64(qrDataUrl, 150, 150);
                        return new RegistrationViewModel(reg, qrBase64);
                    })
                    .collect(Collectors.toList());

            model.addAttribute("registrations", registrationViewModels);
        } else {
            model.addAttribute("registrations", Collections.emptyList());
        }

        return "my-tickets";
    }

    public static class RegistrationViewModel {
        private final EventRegistration registration;
        private final String qrCodeBase64;

        public RegistrationViewModel(EventRegistration registration, String qrCodeBase64) {
            this.registration = registration;
            this.qrCodeBase64 = qrCodeBase64;
        }
        public EventRegistration getRegistration() { return registration; }
        public Event getEvent() { return registration.getEvent(); }
        public String getQrCodeBase64() { return qrCodeBase64; }
        public LocalDateTime getCheckedInAt() { return registration.getCheckedInAt(); }
    }
}