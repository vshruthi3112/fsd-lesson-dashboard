package com.lessondashboard.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JwtUtil - Utility class for creating and validating JWT tokens.
 *
 * HOW JWT WORKS:
 * 1. When a user logs in successfully, we CREATE a token containing their
 *    username and role. The token is "signed" with a secret key.
 *
 * 2. The client stores this token and sends it with every request in the
 *    Authorization header: "Bearer eyJhbG..."
 *
 * 3. On each request, we VALIDATE the token:
 *    - Is the signature valid? (nobody tampered with it)
 *    - Is it expired?
 *    - Extract the username and role from it
 *
 * The secret key is like a password for signing tokens.
 * It's stored in application.properties (in production, use environment variables).
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    /**
     * The secret key used to sign tokens.
     * @Value reads it from application.properties: jwt.secret=...
     * Must be at least 256 bits (32 characters) for HS256 algorithm.
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Token validity duration in milliseconds.
     * @Value reads from application.properties: jwt.expiration=...
     * Default: 86400000ms = 24 hours
     */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Create the signing key from our secret string.
     * HMAC-SHA256 requires a SecretKey object, not a raw string.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate a JWT token for a user.
     *
     * The token payload ("claims") contains:
     * - sub (subject): the username
     * - role: ADMIN or INSTRUCTOR
     * - iat (issued at): when the token was created
     * - exp (expiration): when the token expires
     *
     * @param username the user's username
     * @param role the user's role (e.g., "ADMIN")
     * @return signed JWT string like "eyJhbG..."
     */
    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(username)                    // Who this token is for
                .claim("role", role)                  // Custom claim: their role
                .issuedAt(now)                        // When it was issued
                .expiration(expiryDate)               // When it expires
                .signWith(getSigningKey())            // Sign with our secret
                .compact();                           // Build the string
    }

    /**
     * Extract the username from a token.
     * "subject" is the standard JWT field for identifying the user.
     *
     * @param token the JWT string
     * @return the username stored in the token
     */
    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extract the role from a token.
     *
     * @param token the JWT string
     * @return the role string (e.g., "ADMIN")
     */
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    /**
     * Check if a token is valid (not expired, not tampered with).
     *
     * If the signature doesn't match or the token is expired,
     * the JJWT library throws an exception, and we return false.
     *
     * @param token the JWT string
     * @return true if valid, false if expired or invalid
     */
    public boolean isTokenValid(String token) {
        try {
            getClaims(token); // This throws if invalid or expired
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Parse the token and extract all claims (payload data).
     *
     * This is the core verification step. Jwts.parser() will:
     * 1. Decode the base64 parts
     * 2. Verify the signature using our secret key
     * 3. Check the expiration date
     * 4. Return the payload if everything is valid
     *
     * If anything is wrong, it throws an exception.
     */
    private Claims getClaims(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())   // Use our secret to verify
                .build()
                .parseSignedClaims(token)      // Parse and validate
                .getPayload();                 // Get the claims (payload)

        logger.info("JWT Payload - subject: {}, role: {}, issuedAt: {}, expiration: {}",
                claims.getSubject(),
                claims.get("role"),
                claims.getIssuedAt(),
                claims.getExpiration());

        return claims;
    }
}
