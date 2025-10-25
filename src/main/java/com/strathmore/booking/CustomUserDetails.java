package com.strathmore.booking;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // Delegate methods to the wrapped User entity
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        // Spring Security uses username, which is email in our case
        return user.getEmail();
    }

    // Account status methods - delegate or implement logic as needed
    @Override
    public boolean isAccountNonExpired() {
        return true; // Assuming accounts don't expire
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Assuming accounts don't get locked
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Assuming passwords don't expire
    }

    @Override
    public boolean isEnabled() {
        return user.isVerified(); // Only enable verified users
    }

    // --- Custom method to expose fullname ---
    public String getFullname() {
        return user.getFullname();
    }

    // Optional: Method to get the original User entity if needed elsewhere
    public User getUser() {
        return user;
    }
}