/**
 * JwtProvider handles JWT token generation, validation, and extraction for authentication.
 * It leverages HMAC-SHA256 signing, configurable expiration times, and comprehensive error handling
 * to ensure secure token management across the membership-hub platform.
 *
 * @traceability [REQ-001], [REQ-002], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [DAT-001], [DAT-002], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
 */
package org.nlh4j.saas.membership-hub.authservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.concurrent.TimeUnit;

/**
 * Custom runtime exception for JWT‑related authentication failures.
 * This exception preserves the original cause to maintain full diagnostic context.
 *
 * @traceability [ARC-001], [NFR-001]
 */
@SuppressWarnings("serial")
class JwtAuthenticationException extends RuntimeException {
    public JwtAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Provider component that issues, validates, and extracts claims from JWT tokens.
 * All configuration values are hoisted to class‑level constants to satisfy anti‑magic‑number policies.
 *
 * @traceability [REQ-001], [REQ-002], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [DAT-001], [DAT-002], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
 */
@Component
public class JwtProvider {

    // Logger for enterprise‑grade audit trails
    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    /**
     * Secret key used for HMAC‑SHA256 signing. In production this should be stored in a secure vault
     * (e.g., GCP Secret Manager) and injected via configuration.
     *
     * @traceability [ARC-001], [NFR-001]
     */
    public static final String JWT_SECRET = "membership-hub-super-secret-key-2026-change-in-production";
    /**
     * Access token validity period – 15 hours.
     *
     * @traceability [ARC-002], [NFR-002]
     */
    public static final long JWT_EXPIRATION_MS = TimeUnit.HOURS.toMillis(15);
    /**
     * Refresh token validity period – 7 days.
     *
     * @traceability [ARC-003], [NFR-003]
     */
    public static final long JWT_REFRESH_EXPIRATION_MS = TimeUnit.DAYS.toMillis(7);
    /**
     * Issuer claim value to guarantee token provenance.
     *
     * @traceability [ARC-004], [NFR-004]
     */
    public static final String JWT_ISSUER = "membership-hub";

    /**
     * Derive a {@link SecretKey} from the raw secret string using UTF‑8 encoding.
     * This key is used for signing and verification of JWT tokens.
     *
     * @return a {@link SecretKey} instance ready for HMAC‑SHA256 operations
     *
     * @traceability [ARC-001], [NFR-001]
     */
    private static SecretKey getSigningKey() {
        // Convert the constant secret to a byte array and create a HMAC SHA key
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate an access token for the supplied {@link UserDetails}.
     * The token includes standard claims: issuer, issued‑at, expiration, and a custom user identifier.
     *
     * @param user the authenticated user principal
     * @return a signed JWT string
     *
     * @traceability [REQ-001], [REQ-002], [ARC-001], [ARC-002], [NFR-001], [NFR-002]
     */
    public String generateToken(UserDetails user) {
        logger.info("[JWT_PROVIDER] Generating access token for user: {}", user.getUsername());
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUsername()); // simplistic user identifier; adjust to real user ID as needed
        return buildToken(claims, user.getUsername(), JWT_EXPIRATION_MS);
    }

    /**
     * Generate a refresh token for the supplied {@link UserDetails}.
     * Refresh tokens have a longer lifespan and are used to obtain new access tokens without re‑authenticating.
     *
     * @param user the authenticated user principal
     * @return a signed refresh JWT string
     *
     * @traceability [REQ-002], [ARC-003], [NFR-003]
     */
    public String generateRefreshToken(UserDetails user) {
        logger.info("[JWT_PROVIDER] Generating refresh token for user: {}", user.getUsername());
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return buildToken(claims, user.getUsername(), JWT_REFRESH_EXPIRATION_MS);
    }

    /**
     * Core token construction routine. Packages claims, subject, timestamps, issuer and signs with HMAC‑SHA256.
     *
     * @param extraClaims additional custom claims to embed
     * @param subject    the subject (typically username)
     * @param ttlMs      time‑to‑live in milliseconds for the token
     * @return a signed JWT string
     *
     * @traceability [ARC-001], [ARC-002], [ARC-003], [ARC-004], [NFR-001], [NFR-002], [NFR-003], [NFR-004]
     */
    private String buildToken(Map<String, Object> extraClaims, String subject, long ttlMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ttlMs);
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuer(JWT_ISSUER)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract the username (subject) from a JWT string.
     *
     * @param token the JWT token
     * @return the subject claim (username)
     *
     * @traceability [REQ-001], [ARC-001], [NFR-001]
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Generic claim extraction helper. Validates the token format before delegating to the claim resolver.
     *
     * @param token the JWT token
     * @param claimsResolver function to extract a specific claim
     * @param <T>   type of the claim
     * @return the resolved claim value
     *
     * @traceability [REQ-001], [ARC-001], [NFR-001]
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse and validate the JWT, returning the full {@link Claims} set.
     * Any parsing error is caught, logged with the appropriate traceability tag, and re‑thrown as a
     * {@link JwtAuthenticationException} to preserve the original cause.
     *
     * @param token the JWT token
     * @return the deserialized {@link Claims}
     *
     * @traceability [REQ-001], [ARC-001], [ARC-002], [NFR-001], [NFR-002]
     */
    private Claims extractAllClaims(String token) {
        try {
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return jws.getBody();
        } catch (JwtException e) {
            // Log with module name, raw error, and traceability tag per enterprise audit rules
            logger.error("[JWT_PROVIDER] [ARC-001] JWT token parsing failed. Raw error: {}", e.getMessage(), e);
            throw new JwtAuthenticationException("Invalid JWT token", e);
        }
    }

    /**
     * Determine whether a token is expired by inspecting its expiration claim.
     *
     * @param token the JWT token
     * @return {@code true} if the token has passed its expiry time, otherwise {@code false}
     *
     * @traceability [REQ-001], [ARC-001], [NFR-001]
     */
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Validate the supplied token for structural integrity, issuer, and expiration.
     * Returns {@code true} only when the token is well‑formed, issued by this provider, and not expired.
     *
     * @param token the JWT token
     * @return validation result
     *
     * @traceability [REQ-001], [REQ-002], [ARC-001], [ARC-002], [NFR-001], [NFR-002]
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            // Additional business‑level validation: issuer must match
            boolean issuerValid = JWT_ISSUER.equals(claims.getIssuer());
            if (!issuerValid) {
                logger.warn("[JWT_PROVIDER] [ARC-002] JWT issuer mismatch. Token issuer: {}", claims.getIssuer());
                return false;
            }
            boolean notExpired = !isTokenExpired(token);
            if (!notExpired) {
                logger.warn("[JWT_PROVIDER] [ARC-003] JWT token has expired for subject: {}", claims.getSubject());
            }
            return notExpired && issuerValid;
        } catch (JwtAuthenticationException e) {
            // Re‑throw after logging; cause chain is already captured in the custom exception
            logger.error("[JWT_PROVIDER] [ARC-004] Token validation aborted due to authentication failure. Raw error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            // Catch‑all for any unexpected validation issues
            logger.error("[JWT_PROVIDER] [ARC-005] Unexpected error during token validation. Raw error: {}", e.getMessage(), e);
            throw new JwtAuthenticationException("Token validation error", e);
        }
    }

    /**
     * Convenience overload that also checks the authenticated user name matches the token subject.
     * Used during API request filtering to ensure the token belongs to the currently logged‑in principal.
     *
     * @param token   the JWT token
     * @param userDetails the {@link UserDetails} of the request context
     * @return {@code true} if token is valid and subject matches userDetails
     *
     * @traceability [REQ-001], [REQ-002], [ARC-001], [ARC-002], [NFR-001], [NFR-002]
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && validateToken(token));
    }
}