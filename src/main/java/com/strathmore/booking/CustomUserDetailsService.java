package com.strathmore.booking;

import org.springframework.beans.factory.annotation.Autowired;
// No longer need GrantedAuthority or SimpleGrantedAuthority here
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
// No longer need Collections here

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // We can optionally keep the verification check here,
        // but CustomUserDetails also handles it via isEnabled()
        // if (!user.isVerified()) {
        //     throw new UsernameNotFoundException("User account is not verified: " + email);
        // }

        // Return our custom UserDetails implementation
        return new CustomUserDetails(user);
    }

    // Removed getAuthorities method as it's now in CustomUserDetails
}