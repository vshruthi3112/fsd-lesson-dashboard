package com.lessondashboard.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig - Central configuration for Spring Security.
 *
 * KEY CONCEPTS:
 *
 * 1. STATELESS SESSIONS:
 *    Traditional web apps use cookies + sessions. We DON'T do that.
 *    Instead, every request carries its own proof (the JWT token).
 *    This is called "stateless" because the server doesn't store session state.
 *
 * 2. FILTER CHAIN:
 *    Spring Security works as a chain of filters. Each request passes through:
 *    [CORS] → [CSRF] → [JwtFilter] → [Authorization] → [Controller]
 *
 * 3. AUTHORIZATION RULES:
 *    We define which HTTP methods on which paths require which roles.
 *    - Public: login, register, H2 console
 *    - GET /api/lessons: any authenticated user (both ADMIN and INSTRUCTOR)
 *    - POST /api/lessons: ADMIN or INSTRUCTOR (both can create)
 *    - PUT/DELETE /api/lessons: ADMIN only
 *
 * 4. PASSWORD ENCODER:
 *    BCrypt is a one-way hashing algorithm. When a user registers,
 *    we hash their password before saving. When they log in,
 *    we hash the input and compare it to the stored hash.
 *    This way, even if the database is breached, passwords are safe.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * The main security filter chain configuration.
     *
     * This is where ALL the security rules live.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (Cross-Site Request Forgery) protection.
            // CSRF protection is for cookie-based sessions. Since we use JWT
            // tokens in headers, CSRF attacks don't apply to us.
            .csrf(csrf -> csrf.disable())

            // Allow H2 console to work (it uses iframes)
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))

            // Stateless session management — no cookies, no server-side sessions.
            // Each request must carry its own JWT token.
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Authorization rules: which endpoints require what
            .authorizeHttpRequests(auth -> auth

                // PUBLIC endpoints (no token needed):
                .requestMatchers("/api/auth/**").permitAll()   // Login & register
                .requestMatchers("/h2-console/**").permitAll()  // DB console (dev only)

                // LESSON endpoints with role-based access:
                // GET requests: any authenticated user can view lessons
                .requestMatchers(HttpMethod.GET, "/api/lessons/**").authenticated()

                // POST requests: both ADMIN and INSTRUCTOR can create lessons
                .requestMatchers(HttpMethod.POST, "/api/lessons/**")
                    .hasAnyRole("ADMIN", "INSTRUCTOR")

                // PUT and DELETE: only ADMIN can modify/remove lessons
                .requestMatchers(HttpMethod.PUT, "/api/lessons/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/lessons/**").hasRole("ADMIN")

                // Everything else: must be authenticated
                .anyRequest().authenticated()
            )

            // Add our JWT filter BEFORE Spring's default username/password filter.
            // This way, our filter checks the token first, and Spring Security
            // already knows who the user is by the time it checks permissions.
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Password encoder bean.
     *
     * BCrypt is the industry standard for password hashing:
     * - It's slow on purpose (makes brute-force attacks impractical)
     * - It includes a random "salt" (two identical passwords get different hashes)
     * - It's one-way (you can't reverse a hash back to the password)
     *
     * Usage:
     * - Registration: encoder.encode("password123") → "$2a$10$xYz..."
     * - Login: encoder.matches("password123", "$2a$10$xYz...") → true/false
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager bean.
     * Spring Security uses this internally. We need to expose it
     * so our AuthService can use it for login validation.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
