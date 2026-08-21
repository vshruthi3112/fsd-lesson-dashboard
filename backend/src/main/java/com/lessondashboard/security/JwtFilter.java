package com.lessondashboard.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JwtFilter - Intercepts every HTTP request to check for a valid JWT token.
 *
 * HOW THIS WORKS (step by step):
 *
 * 1. Every request passes through this filter BEFORE reaching the controller.
 *
 * 2. We look for the "Authorization" header:
 *    Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 *
 * 3. If the header exists and starts with "Bearer ":
 *    a. Extract the token (remove "Bearer " prefix)
 *    b. Validate the token using JwtUtil
 *    c. If valid → Extract username + role
 *    d. Create a Spring Security "Authentication" object
 *    e. Put it in the SecurityContext (Spring's way of saying "this user is logged in")
 *
 * 4. If the header is missing or token is invalid:
 *    → Do nothing. The request continues without authentication.
 *    → Spring Security will then block it if the endpoint requires auth.
 *
 * OncePerRequestFilter guarantees this runs exactly once per request
 * (not multiple times if the request gets forwarded internally).
 *
 * Logging:
 * - DEBUG: Token validation steps (development visibility)
 * - WARN: Invalid/expired tokens (security monitoring)
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Skip token check for auth endpoints (login/register don't need a token)
        String path = request.getServletPath();
        if (path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 1: Get the Authorization header
        String authHeader = request.getHeader("Authorization");

        // Step 2: Check if it exists and has the right format
        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            // Step 3: Extract the token (everything after "Bearer ")
            String token = authHeader.substring(7);

            // Step 4: Validate the token
            if (jwtUtil.isTokenValid(token)) {

                // Step 5: Extract user info from the token
                String username = jwtUtil.getUsername(token);
                String role = jwtUtil.getRole(token);

                logger.debug("JWT authenticated user: '{}' with role: {} for path: {}",
                        username, role, path);

                // Step 6: Create Spring Security authorities
                // "ROLE_" prefix is a Spring Security convention.
                // When we say hasRole("ADMIN"), Spring checks for "ROLE_ADMIN".
                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority("ROLE_" + role);

                // Step 7: Create an Authentication object
                // This tells Spring Security: "This user is authenticated"
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,       // principal (who)
                                null,           // credentials (not needed, token is proof)
                                List.of(authority)  // authorities (what they can do)
                        );

                // Step 8: Store in SecurityContext
                // Now any @PreAuthorize or hasRole() check can see this user
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } else {
                // Token present but invalid (expired or tampered)
                logger.warn("Invalid JWT token received for path: {}", path);
            }
        } else if (authHeader != null) {
            // Authorization header present but wrong format
            logger.warn("Malformed Authorization header for path: {} (expected 'Bearer <token>')", path);
        }

        // Step 9: Continue the filter chain (let the request proceed)
        // If we didn't set authentication above, Spring Security will block
        // the request if the endpoint requires authentication.
        filterChain.doFilter(request, response);
    }
}
