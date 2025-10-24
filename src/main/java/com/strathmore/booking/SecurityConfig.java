package com.strathmore.booking;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// Import this
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// Import this
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults; // Import this

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Add this bean for password hashing
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Allow public access to static resources, signin, signup, home, events
                        .requestMatchers(
                                "/", "/events", "/event-details", // Public pages
                                "/signin", "/signup", "/verify", // Auth pages
                                "/assets/**", "/uploads/**", "/css/**", "/js/**", "/images/**" // Static files
                        ).permitAll()
                        // Require authentication for all other pages (like /dashboard, /profile)
                        .anyRequest().authenticated()
                )
                // Configure the login form
                .formLogin(formLogin -> formLogin
                        .loginPage("/signin") // Use our custom signin page
                        .loginProcessingUrl("/signin") // The URL the form POSTs to
                        .defaultSuccessUrl("/dashboard", true) // Redirect to dashboard after login
                        .permitAll() // Allow everyone to access the login page
                )
                // Configure logout
                .logout(logout -> logout
                        .logoutUrl("/logout") // The URL to trigger logout
                        .logoutSuccessUrl("/signin?logout") // Redirect after logout
                        .permitAll() // Allow everyone to logout
                )
                // CSRF is enabled by default, good for security
                .csrf(withDefaults());

        return http.build();
    }
}