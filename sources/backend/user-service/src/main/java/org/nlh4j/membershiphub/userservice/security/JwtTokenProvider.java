package org.nlh4j.membershiphub.userservice.security;

import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Enterprise JSON Web Token (JWT) Provider and Cryptographic Verification Engine.
 * Responsible for issuing, signing, parsing, and validating RS256 access and refresh tokens
 * in compliance with OAuth2 resource server standards and enterprise security guardrails.
 *
 * @author Enterprise Architecture Core Team
 * @version 1.0.0
 * @traceability [ARC-006], [NFR-003]
 */
@ApplicationScoped
public class JwtTokenProvider {

    // =========================================================================
    // 0. TOP-OF-CLASS IMMUTABLE CONSTANTS DECLARATION [0.2]
    // =========================================================================

    /** Subsystem identification tag for audit logging [0.1] */
    public static final String SUBSYSTEM_NAME = "USER-SERVICE:JWT-PROVIDER";

    /** Architecture requirement tag for hybrid OAuth2/JWT authentication */
    public static final String TAG_ARC_006 = "[ARC-006]";

    /** Non-functional requirement tag for zero-trust enterprise security standards */
    public static final String TAG_NFR_003 = "[NFR-003]";

    /** Standard JWT claim identifier for Subject (User ID) */
    public static final String CLAIM_SUB = "sub";

    /** Standard JWT claim identifier for Authorization Role Groups */
    public static final String CLAIM_GROUPS = "groups";

    /** Custom JWT claim identifier for legacy single-role resolution */
    public static final String CLAIM_GROUP = "group";

    /** Standard JWT claim identifier for Issuer */
    public static final String CLAIM_ISS = "iss";

    /** Standard JWT claim identifier for Audience */
    public static final String CLAIM_AUD = "aud";

    /** Custom JWT claim identifier for token category discrimination */
    public static final String CLAIM_TYPE = "type";

    /** Custom JWT claim identifier for identity provider designation */
    public static final String CLAIM_PROVIDER = "provider";

    /** Custom JWT claim identifier for token issue timestamp in epoch seconds */
    public static final String CLAIM_IAT = "iat";

    /** Token type value discriminator representing access authorization */
    public static final String TOKEN_TYPE_ACCESS = "access";

    /** Token type value discriminator representing session renewal authorization */
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    /** Default fallback issuer name when not configured in microprofile properties */
    public static final String DEFAULT_ISSUER = "membership-hub";

    /** Default fallback audience name when not configured in microprofile properties */
    public static final String DEFAULT_AUDIENCE = "membership-hub-client";

    /** Default access token lifespan: 15 minutes (900 seconds) [ARC-006] */
    public static final long ACCESS_TOKEN_VALIDITY_SECONDS = 900L;

    /** Default refresh token lifespan: 7 days (604,800 seconds) [ARC-006] */
    public static final long REFRESH_TOKEN_VALIDITY_SECONDS = 604800L;

    /** Cryptographic RSA key algorithm name */
    public static final String RSA_ALGORITHM = "RSA";

    /** PEM header line prefix for standard X.509 public certificates */
    public static final String PEM_PUBLIC_KEY_HEADER = "-----BEGIN PUBLIC KEY-----";

    /** PEM footer line suffix for standard X.509 public certificates */
    public static final String PEM_PUBLIC_KEY_FOOTER = "-----END PUBLIC KEY-----";

    /** Classpath resource path prefix discriminator */
    public static final String CLASSPATH_PREFIX = "classpath:";

    /** Log pattern for entry point auditing */
    public static final String LOG_ENTRY_PREFIX = "[ENTRY] [{}] Action: {} | Trace: {}";

    /** Log pattern for completion point auditing */
    public static final String LOG_EXIT_PREFIX = "[EXIT] [{}] Action: {} completed successfully | Trace: {}";

    /** Log pattern for exception failure auditing [0.1] */
    public static final String LOG_ERROR_PATTERN = "[CRITICAL FAIL] [{}] {} failed. Trace Tag: {}. Raw error: {}";

    /** Logger instance for process state tracing and security auditing */
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    // =========================================================================
    // INJECTED CONFIGURATION & COMPONENT HANDLES
    // =========================================================================

    /** Configured issuer URL/name injected from SmallRye configuration [ARC-006] */
    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = DEFAULT_ISSUER)
    String configuredIssuer;

    /** Path to the RS256 RSA public key used for signature verification [NFR-003] */
    @ConfigProperty(name = "mp.jwt.verify.publickey.location", defaultValue = "publicKey.pem")
    String publicKeyLocation;

    /** Configured audience expectation injected from MicroProfile Config */
    @ConfigProperty(name = "mp.jwt.verify.audiences", defaultValue = DEFAULT_AUDIENCE)
    Optional<String> configuredAudience;

    /** SmallRye JWT Parser for parsing and cryptographically verifying tokens */
    @Inject
    JWTParser jwtParser;

    /** Cached RSA public key for cryptographic signature verification */
    private PublicKey cachedPublicKey;

    // =========================================================================
    // LIFECYCLE & INITIALIZATION
    // =========================================================================

    /**
     * Initializes the token provider and pre-loads the RS256 public key into memory.
     * Enforces immediate failure at startup if cryptographic materials are unavailable.
     * // [ARC-006] [NFR-003]
     */
    @PostConstruct
    public void initializeProvider() {
        // [NFR-003] Log initialization entry gate
        logger.info(LOG_ENTRY_PREFIX, SUBSYSTEM_NAME, "initializeProvider", TAG_NFR_003);
        try {
            // Load and parse the RSA public key from the configured location
            this.cachedPublicKey = loadPublicKey(this.publicKeyLocation);
            logger.info("[INIT] Loaded RSA Public Key successfully from location: {}", this.publicKeyLocation);
            // [ARC-006] Log successful startup completion gate
            logger.info(LOG_EXIT_PREFIX, SUBSYSTEM_NAME, "initializeProvider", TAG_ARC_006);
        } catch (Exception e) {
            // [0.1] Structured exception logging including subsystem, raw error, and traceability tag
            logger.error(LOG_ERROR_PATTERN, SUBSYSTEM_NAME, "Provider Initialization", TAG_NFR_003, e.getMessage());
            throw new SecurityException("Failed to initialize cryptographic keys for JWT Token Provider", e);
        }
    }

    // =========================================================================
    // PUBLIC CONTRACT IMPLEMENTATION METHODS
    // =========================================================================

    /**
     * Generates a digitally signed RS256 OAuth2 Access Token with a 15-minute validity window.
     * Embeds identity, authorization role groups, issuer, audience, and provider metadata.
     *
     * @param userId   The unique UUID identity string of the subject user.
     * @param role     The system authorization role assigned to the user.
     * @param provider The identity authentication provider (e.g., "local", "google", "firebase").
     * @return Fully signed and encoded RS256 Compact JWT String.
     * // [REQ-001], [REQ-002], [ARC-006], [NFR-003]
     */
    public String generateAccessToken(String userId, String role, String provider) {
        // [0.1] Entry logging with masked/safe payload tracing
        logger.info(LOG_ENTRY_PREFIX, SUBSYSTEM_NAME, "generateAccessToken", TAG_ARC_006);

        if (userId == null || userId.trim().isEmpty()) {
            logger.error(LOG_ERROR_PATTERN, SUBSYSTEM_NAME, "generateAccessToken", TAG_ARC_006, "User ID cannot be null or empty");
            throw new IllegalArgumentException("User ID parameter must not be null or empty");
        }

        try {
            // Determine active target issuer and audience
            final String targetIssuer = (this.configuredIssuer != null && !this.configuredIssuer.trim().isEmpty())
                    ? this.configuredIssuer
                    : DEFAULT_ISSUER;
            final String targetAudience = this.configuredAudience.orElse(DEFAULT_AUDIENCE);

            // Construct normalized groups collection for RBAC mapping
            final Set<String> rolesSet = new HashSet<>();
            if (role != null && !role.trim().isEmpty()) {
                rolesSet.add(role.trim());
            }

            // [ARC-006] Build the claims set using SmallRye JWT Claims Builder
            final JwtClaimsBuilder claimsBuilder = Jwt.claims();
            claimsBuilder.issuer(targetIssuer);
            claimsBuilder.subject(userId.trim());
            claimsBuilder.audience(targetAudience);
            claimsBuilder.expiresIn(Duration.ofSeconds(ACCESS_TOKEN_VALIDITY_SECONDS));
            claimsBuilder.issuedAt(Instant.now());
            claimsBuilder.groups(rolesSet);

            // [ARC-006] Inject custom domain claims for backward compatibility and provider audit
            claimsBuilder.claim(CLAIM_GROUP, role != null ? role.trim() : "");
            claimsBuilder.claim(CLAIM_PROVIDER, provider != null ? provider.trim() : "local");
            claimsBuilder.claim(CLAIM_TYPE, TOKEN_TYPE_ACCESS);

            // Sign compact JWT using SmallRye default configured private key location
            final String signedToken = claimsBuilder.sign();

            // [0.1] Exit point execution logging
            logger.info(LOG_EXIT_PREFIX, SUBSYSTEM_NAME, "generateAccessToken", TAG_NFR_003);
            return signedToken;
        } catch (Exception e) {
            // [0.1] Comprehensive exception logging tracking ancestral cause
            logger.error(LOG_ERROR_PATTERN, SUBSYSTEM_NAME, "generateAccessToken", TAG_ARC_006, e.getMessage());
            throw new SecurityException("Failed to digitally sign Access Token for subject", e);
        }
    }

    /**
     * Generates a digitally signed RS256 Refresh Token with a 7-day validity window.
     * Intended exclusively for session renewal flows without carrying permission groups.
     *
     * @param userId The unique UUID identity string of the subject user.
     * @return Fully signed and encoded RS256 Refresh JWT String.
     * // [ARC-006], [NFR-003]
     */
    public String generateRefreshToken(String userId) {
        // [0.1] Entry logging gate
        logger.info(LOG_ENTRY_PREFIX, SUBSYSTEM_NAME, "generateRefreshToken", TAG_ARC_006);

        if (userId == null || userId.trim().isEmpty()) {
            logger.error(LOG_ERROR_PATTERN, SUBSYSTEM_NAME, "generateRefreshToken", TAG_ARC_006, "User ID cannot be null or empty");
            throw new IllegalArgumentException("User ID parameter must not be null or empty");
        }

        try {
            // Determine active target issuer and audience
            final String targetIssuer = (this.configuredIssuer != null && !this.configuredIssuer.trim().isEmpty())
                    ? this.configuredIssuer
                    : DEFAULT_ISSUER;
            final String targetAudience = this.configuredAudience.orElse(DEFAULT_AUDIENCE);

            // [ARC-006] Build the refresh claims set with 7 days expiration
            final JwtClaimsBuilder claimsBuilder = Jwt.claims();
            claimsBuilder.issuer(targetIssuer);
            claimsBuilder.subject(userId.trim());
            claimsBuilder.audience(targetAudience);
            claimsBuilder.expiresIn(Duration.ofSeconds(REFRESH_TOKEN_VALIDITY_SECONDS));
            claimsBuilder.issuedAt(Instant.now());
            claimsBuilder.claim(CLAIM_TYPE, TOKEN_TYPE_REFRESH);
            claimsBuilder.groups(Collections.emptySet());

            // Sign compact JWT
            final String signedRefreshToken = claimsBuilder.sign();

            // [0.1] Exit logging gate
            logger.info(LOG_EXIT_PREFIX, SUBSYSTEM_NAME, "generateRefreshToken", TAG_NFR_003);
            return signedRefreshToken;
        } catch (Exception e) {
            // [0.1] Comprehensive exception logging
            logger.error(LOG_ERROR_PATTERN, SUBSYSTEM_NAME, "generateRefreshToken", TAG_ARC_006, e.getMessage());
            throw new SecurityException("Failed to digitally sign Refresh Token for subject", e);
        }
    }

    /**
     * Cryptographically validates token signature, issuer conformity, and temporal expiration.
     *
     * @param token Compact serialized JWT string to be validated.
     * @return {@code true} if signature is authentic, issuer matches, and token is active; {@code false} otherwise.
     * // [ARC-006], [NFR-003]
     */
    public boolean validateToken(String token) {
        // [0.1] Entry point debug trace
        logger.debug(LOG_ENTRY_PREFIX, SUBSYSTEM_NAME, "validateToken", TAG_NFR_003);

        if (token == null || token.trim().isEmpty()) {
            logger.warn("[SECURITY WARN] [{}] Validation aborted: token payload is null or blank", SUBSYSTEM_NAME);
            return false;
        }

        try {
            // Perform rigorous parsing and cryptographic validation
            final JsonWebToken parsedToken = getClaims(token);

            // Validate mandatory subject presence
            if (parsedToken.getSubject() == null || parsedToken.getSubject().trim().isEmpty()) {
                logger.warn("[SECURITY WARN] [{}] Token validation failed: Missing subject claim", SUBSYSTEM_NAME);
                return false;
            }

            // Validate token expiration boundary
            final long expirationTime = parsedToken.getExpirationTime();
            final long currentTimeSeconds = Instant.now().getEpochSecond();
            if (expirationTime <= currentTimeSeconds) {
                logger.warn("[SECURITY WARN] [{}] Token validation failed: Token expired at epoch {}", SUBSYSTEM_NAME, expirationTime);
                return false;
            }

            // Validate issuer conformity
            final String tokenIssuer = parsedToken.getIssuer();
            final String expectedIssuer = (this.configuredIssuer != null && !this.configuredIssuer.trim().isEmpty())
                    ? this.configuredIssuer
                    : DEFAULT_ISSUER;
            if (tokenIssuer == null || !tokenIssuer.equals(expectedIssuer)) {
                logger.warn("[SECURITY WARN] [{}] Token validation failed: Invalid issuer '{}', expected '{}'",
                        SUBSYSTEM_NAME, tokenIssuer, expectedIssuer);
                return false;
            }

            // [0.1] Successful validation trace
            logger.debug(LOG_EXIT_PREFIX, SUBSYSTEM_NAME, "validateToken", TAG_ARC_006);
            return true;
        } catch (Exception e) {
            // Catch all verification failures (signature mismatch, malformed payload, expired token)
            logger.warn("[VALIDATION FAIL] [{}] Token validation rejected. Reason: {}", SUBSYSTEM_NAME, e.getMessage());
            return false;
        }
    }

    /**
     * Parses the compact JWT string into a typed {@link JsonWebToken} instance.
     * Enforces RS256 signature verification against the internal public key.
     *
     * @param token Compact serialized JWT string.
     * @return Decoded and verified {@link JsonWebToken} claims context.
     * @throws SecurityException If signature verification fails, payload is malformed, or token is expired.
     * // [ARC-006], [NFR-003]
     */
    public JsonWebToken getClaims(String token) {
        // [0.1] Entry trace
        logger.debug(LOG_ENTRY_PREFIX, SUBSYSTEM_NAME, "getClaims", TAG_NFR_003);

        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token string to parse cannot be null or empty");
        }

        try {
            // [ARC-006] Configure authentication context information for SmallRye JWTParser
            final JWTAuthContextInfo authContextInfo = new JWTAuthContextInfo();
            final String expectedIssuer = (this.configuredIssuer != null && !this.configuredIssuer.trim().isEmpty())
                    ? this.configuredIssuer
                    : DEFAULT_ISSUER;

            authContextInfo.setIssuedBy(expectedIssuer);
            authContextInfo.setSignerKey(this.cachedPublicKey);

            // Execute cryptographic parsing using the SmallRye parser engine
            final JsonWebToken jwt = this.jwtParser.parse(token.trim(), authContextInfo);

            // [0.1] Exit trace
            logger.debug(LOG_EXIT_PREFIX, SUBSYSTEM_NAME, "getClaims", TAG_ARC_006);
            return jwt;
        } catch (ParseException e) {
            // [0.1] Log parse failure and wrap preserving cause chain
            logger.error(LOG_ERROR_PATTERN, SUBSYSTEM_NAME, "getClaims", TAG_ARC_006, e.getMessage());
            throw new SecurityException("Malformed or unparseable JWT signature verification failure", e);
        } catch (Exception e) {
            // [0.1] Log general security failure
            logger.error(LOG_ERROR_PATTERN, SUBSYSTEM_NAME, "getClaims", TAG_NFR_003, e.getMessage());
            throw new SecurityException("Cryptographic verification rejected for the supplied token", e);
        }
    }

    // =========================================================================
    // INTERNAL CRYPTOGRAPHIC UTILITIES & KEY LOADERS
    // =========================================================================

    /**
     * Loads an RSA 2048-bit Public Key from a PEM formatted resource or classpath file.
     * Sanitizes headers, footers, whitespace, and decodes Base64 into standard X.509 format.
     *
     * @param location Path to the public key PEM file.
     * @return Reconstituted RSA {@link PublicKey} instance.
     * @throws Exception If the file is inaccessible or the key specification is malformed.
     * // [NFR-003]
     */
    private PublicKey loadPublicKey(String location) throws Exception {
        logger.debug("[KEY_LOAD] Loading public key from: {}", location);
        String resolvedLocation = location;
        if (resolvedLocation.startsWith(CLASSPATH_PREFIX)) {
            resolvedLocation = resolvedLocation.substring(CLASSPATH_PREFIX.length());
        }

        // Attempt reading from ClassLoader resources
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resolvedLocation);
        if (is == null) {
            is = JwtTokenProvider.class.getResourceAsStream(resolvedLocation);
        }
        if (is == null && !resolvedLocation.startsWith("/")) {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream("/" + resolvedLocation);
        }

        if (is == null) {
            throw new IllegalStateException("RSA Public Key PEM resource not found at location: " + location);
        }

        try (InputStream inputStream = is) {
            final String keyContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            // Clean PEM artifacts and whitespace
            final String normalizedKey = keyContent
                    .replace(PEM_PUBLIC_KEY_HEADER, "")
                    .replace(PEM_PUBLIC_KEY_FOOTER, "")
                    .replaceAll("\\s+", "");

            // Decode base64 bytes and build X509 key specification
            final byte[] keyBytes = Base64.getDecoder().decode(normalizedKey);
            final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            final KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);

            return keyFactory.generatePublic(keySpec);
        }
    }
}