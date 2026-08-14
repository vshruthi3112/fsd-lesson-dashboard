package com.lessondashboard.security;

import com.lessondashboard.model.User;
import com.lessondashboard.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CustomUserDetailsService - Tells Spring Security how to load user data.
 *
 * WHY THIS EXISTS:
 * Spring Security has its own internal system for looking up users.
 * It uses an interface called UserDetailsService with one method:
 *   loadUserByUsername(String username) → UserDetails
 *
 * We implement this to connect Spring Security to OUR database.
 * Without this, Spring Security wouldn't know where to find users.
 *
 * This is used internally by Spring Security's AuthenticationManager.
 * You won't call it directly — Spring calls it during authentication.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Load a user from the database by username.
     *
     * Spring Security calls this when it needs to verify credentials.
     * We convert our User entity into Spring's UserDetails format.
     *
     * @param username the username to look up
     * @return UserDetails object that Spring Security can work with
     * @throws UsernameNotFoundException if user doesn't exist
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username));

        // Convert our Role to Spring Security's authority format
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        // Return Spring's built-in User implementation
        // (different from our User entity — this is Spring Security's User)
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(authority)
        );
    }
}
