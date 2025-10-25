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

                        .requestMatchers(
                                "/", "/events", "/event-details", "/signup", "/verify", // Public pages
                                "/signin", // Explicitly permit the login page URL
                                "/assets/**", "/uploads/**", "/css/**", "/js/**", "/images/**", "/favicon.ico" // Static files
                        ).permitAll()
                        // Rule 2: All *other* requests must be authenticated
                        .anyRequest().authenticated()
                )
                // Configure the login form
                .formLogin(formLogin -> formLogin
                        .loginPage("/signin")           // The URL for our custom login page (GET)
                        .loginProcessingUrl("/login")   // **CHANGE:** Use the default URL for submission (POST)
                        .defaultSuccessUrl("/dashboard", true) // Redirect after successful login
                        .permitAll()                     // Allows access to loginPage and loginProcessingUrl
                )
                // Configure logout (remains the same)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/signin?logout")
                        .permitAll()
                )
                .csrf(withDefaults()); // Keep CSRF enabled

        return http.build();
    }
}