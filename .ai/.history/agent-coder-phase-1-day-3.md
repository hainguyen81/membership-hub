# Day 3: model models/gemini-flash-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtTokenProvider.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtTokenProvider.java`
*   Traceability Audit Tags For This Task: ['[ARC-006]', '[NFR-003]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo tệp mã nguồn ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtTokenProvider.java hiện thực hóa lớp JwtTokenProvider với annotation @ApplicationScoped, sử dụng Jwt.issuer() từ SmallRye JWT Build. Triển khai phương thức generateAccessToken(String userId, String role, String provider) trả về JWT có thời hạn 15 phút, claim sub chứa userId, claim group chứa role, claim iss là membership-hub, claim aud là membership-hub-client. Phương thức generateRefreshToken(String userId) sinh refresh token với thời hạn 7 ngày, claim type là refresh. Phương thức validateToken(String token) kiểm tra chữ ký bằng khóa RSA 2048-bit, xác minh thời hạn và issuer. Phương thức getClaims(String token) trả về JsonWebToken đã giải mã. Tích hợp @ConfigProperty(name = "mp.jwt.verify.issuer") và mp.jwt.verify.publickey.location. Sử dụng thuật toán RS256.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-006]', '[NFR-003]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
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
```

# Day 3: model models/gemini-flash-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/ResourceServerConfig.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/ResourceServerConfig.java`
*   Traceability Audit Tags For This Task: ['[ARC-006]', '[NFR-003]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo tệp mã nguồn ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/ResourceServerConfig.java cấu hình @ApplicationPath("/api/v1"), lớp ResourceServerConfig implement SecurityIdentityAugmentor từ Quarkus Security. Sử dụng @Produces cho SecurityIdentity, bổ sung role vào SecurityIdentity từ claim group trong JWT. Áp dụng annotation @DenyAll, @RolesAllowed, @PermitAll cho các REST endpoint. Cấu hình quarkus.http.auth.proactive=false để cho phép truy cập không xác thực vào /api/v1/auth/login và /api/v1/auth/social. Tích hợp quarkus.smallrye-jwt.enabled=true, mp.jwt.verify.issuer=membership-hub, mp.jwt.verify.publickey.location=publicKey.pem, smallrye.jwt.sign.key.location=privateKey.pem. Đảm bảo tất cả endpoint khác yêu cầu JWT hợp lệ, trả về HTTP 401 khi thiếu token, HTTP 403 khi không đủ quyền.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-006]', '[NFR-003]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.membershiphub.userservice.security;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipal;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Enterprise Resource Server and Security Identity Augmentor Configuration.
 * <p>
 * This configuration class registers the application root REST path prefix, sets up
 * dynamic augmentations for caller security identities extracted from incoming JWT claims,
 * and normalizes role claims into Quarkus runtime security authorization contexts.
 * </p>
 *
 * @traceability [ARC-006], [NFR-003]
 */
@ApplicationScoped
@ApplicationPath(ResourceServerConfig.API_V1_PATH_PREFIX)
public class ResourceServerConfig extends Application implements SecurityIdentityAugmentor {

    // [0.2] Top-of-Class Immutable Constants Declaration
    public static final String API_V1_PATH_PREFIX = "/api/v1";
    public static final String CLAIM_GROUPS = "groups";
    public static final String CLAIM_GROUP = "group";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_ROLE = "role";
    public static final String DEFAULT_ISSUER = "membership-hub";
    public static final String ROLE_PREFIX = "ROLE_";
    public static final int AUGMENTATION_PRIORITY_ORDER = 10;

    // Standard Logging Framework Instance
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceServerConfig.class);

    /**
     * Retrieves the augmentation priority order for chain processing.
     *
     * @return priority integer order
     */
    @Override
    public int priority() {
        // [ARC-006] Priority ordering for security identity augmentors in the filter chain
        return AUGMENTATION_PRIORITY_ORDER;
    }

    /**
     * Augments incoming reactive security identities with standardized application roles
     * extracted from JWT principal claims.
     *
     * @param identity caller's established identity
     * @param context  authentication context providing async execution facility
     * @return Uni containing augmented or unmodified SecurityIdentity
     */
    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
        // [0.3] Entry-point logging with context tracking
        LOGGER.debug("[ENTRY] [ARC-006] Augmenting security identity for principal: {}",
                identity.isAnonymous() ? "ANONYMOUS" : identity.getPrincipal().getName());

        // Anonymous callers require no role elevation or claim mapping
        if (identity.isAnonymous()) {
            LOGGER.debug("[EXIT] [ARC-006] Security identity is anonymous; skipping claim augmentation.");
            return Uni.createFrom().item(identity);
        }

        return context.runBlocking(() -> {
            try {
                Principal principal = identity.getPrincipal();

                // Validate if caller principal originates from a MicroProfile / SmallRye JWT token
                if (principal instanceof JsonWebToken jwtPrincipal) {
                    LOGGER.debug("[PROCESS] [NFR-003] Extracting role and group claims from JWT Principal: {}",
                            jwtPrincipal.getName());

                    Set<String> resolvedRoles = extractRolesFromJwt(jwtPrincipal);

                    // Build hardened QuarkusSecurityIdentity containing standard + normalized roles
                    QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder(identity);
                    for (String role : resolvedRoles) {
                        builder.addRole(role);
                        // Also inject non-prefixed role variant for strict @RolesAllowed compatibility
                        if (role.startsWith(ROLE_PREFIX)) {
                            builder.addRole(role.substring(ROLE_PREFIX.length()));
                        } else {
                            builder.addRole(ROLE_PREFIX + role);
                        }
                    }

                    SecurityIdentity augmentedIdentity = builder.build();
                    LOGGER.info("[EXIT] [ARC-006] Successfully augmented SecurityIdentity for user: {} with roles: {}",
                            augmentedIdentity.getPrincipal().getName(), augmentedIdentity.getRoles());
                    return augmentedIdentity;
                } else if (principal instanceof JWTCallerPrincipal callerPrincipal) {
                    LOGGER.debug("[PROCESS] [NFR-003] Extracting role claims from JWTCallerPrincipal: {}",
                            callerPrincipal.getName());

                    Set<String> resolvedRoles = extractRolesFromCallerPrincipal(callerPrincipal);
                    QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder(identity);
                    for (String role : resolvedRoles) {
                        builder.addRole(role);
                        if (role.startsWith(ROLE_PREFIX)) {
                            builder.addRole(role.substring(ROLE_PREFIX.length()));
                        } else {
                            builder.addRole(ROLE_PREFIX + role);
                        }
                    }

                    SecurityIdentity augmentedIdentity = builder.build();
                    LOGGER.info("[EXIT] [ARC-006] Successfully augmented SecurityIdentity via caller principal for user: {}",
                            augmentedIdentity.getPrincipal().getName());
                    return augmentedIdentity;
                }

                LOGGER.debug("[EXIT] [ARC-006] Principal is not an instance of JsonWebToken. Skipping role transformation.");
                return identity;
            } catch (Exception e) {
                // [0.3] Comprehensive exception auditing with explicit tag and subsystem context
                LOGGER.error("[CRITICAL FAIL] [NFR-003] Security identity augmentation failed due to token parsing error. Raw error: {}",
                        e.getMessage(), e);
                // Return original identity to allow standard authentication/authorization interceptors to handle failure safely
                return identity;
            }
        });
    }

    /**
     * Inspects diverse JWT claim topologies to extract authorization groups and roles.
     *
     * @param jwt parsed JSON Web Token
     * @return Set of extracted string role names
     */
    private Set<String> extractRolesFromJwt(JsonWebToken jwt) {
        Set<String> roles = new HashSet<>();

        // 1. Inspect 'groups' claim (Standard MicroProfile JWT collection format)
        // [ARC-006] Extracting group claims
        Set<String> groupsClaim = jwt.getGroups();
        if (groupsClaim != null && !groupsClaim.isEmpty()) {
            roles.addAll(groupsClaim);
        }

        // 2. Inspect singular 'group' claim
        Object singularGroup = jwt.getClaim(CLAIM_GROUP);
        if (singularGroup instanceof String groupStr && !groupStr.trim().isEmpty()) {
            roles.add(groupStr.trim());
        } else if (singularGroup instanceof Collection<?> groupColl) {
            groupColl.forEach(item -> {
                if (item != null) {
                    roles.add(item.toString().trim());
                }
            });
        }

        // 3. Inspect custom 'roles' claim collection
        Object rolesClaim = jwt.getClaim(CLAIM_ROLES);
        if (rolesClaim instanceof Collection<?> rolesColl) {
            rolesColl.forEach(item -> {
                if (item != null) {
                    roles.add(item.toString().trim());
                }
            });
        }

        // 4. Inspect singular 'role' claim
        Object singularRole = jwt.getClaim(CLAIM_ROLE);
        if (singularRole instanceof String roleStr && !roleStr.trim().isEmpty()) {
            roles.add(roleStr.trim());
        }

        return Collections.unmodifiableSet(roles);
    }

    /**
     * Inspects JWTCallerPrincipal claim topologies to extract roles.
     *
     * @param callerPrincipal Caller principal instance
     * @return Set of extracted string role names
     */
    private Set<String> extractRolesFromCallerPrincipal(JWTCallerPrincipal callerPrincipal) {
        Set<String> roles = new HashSet<>();

        // Inspect 'groups' claim
        Object groupsClaim = callerPrincipal.getClaim(CLAIM_GROUPS);
        if (groupsClaim instanceof Collection<?> coll) {
            coll.forEach(item -> {
                if (item != null) {
                    roles.add(item.toString().trim());
                }
            });
        } else if (groupsClaim instanceof String str && !str.trim().isEmpty()) {
            roles.add(str.trim());
        }

        // Inspect singular 'group' claim
        Object groupClaim = callerPrincipal.getClaim(CLAIM_GROUP);
        if (groupClaim instanceof String str && !str.trim().isEmpty()) {
            roles.add(str.trim());
        }

        // Inspect 'roles' claim
        Object rolesClaim = callerPrincipal.getClaim(CLAIM_ROLES);
        if (rolesClaim instanceof Collection<?> coll) {
            coll.forEach(item -> {
                if (item != null) {
                    roles.add(item.toString().trim());
                }
            });
        }

        return Collections.unmodifiableSet(roles);
    }
}
```

# Day 3: model models/gemini-flash-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/SocialAuthProviderRegistry.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/SocialAuthProviderRegistry.java`
*   Traceability Audit Tags For This Task: ['[ARC-006]', '[REQ-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo tệp mã nguồn ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/SocialAuthProviderRegistry.java hiện thực hóa interface SocialAuthProvider gồm String getName(), SocialUserInfo verifyToken(String idToken). Tạo 3 implementation: FirebaseAuthProvider (xác minh ID Token qua Firebase Admin SDK 9.2.0, endpoint https://identitytoolkit.googleapis.com/v1/accounts:lookup), GoogleAuthProvider (xác minh qua Google API https://oauth2.googleapis.com/tokeninfo?id_token=), FacebookAuthProvider (xác minh qua https://graph.facebook.com/v18.0/debug_token). Lớp SocialAuthProviderRegistry với annotation @ApplicationScoped chứa map Map<String, SocialAuthProvider> được inject tất cả Instance<SocialAuthProvider>, cung cấp phương thức SocialUserInfo authenticate(String providerName, String idToken) tra cứu provider theo tên. Lớp SocialUserInfo là POJO gồm String email, String fullName, String providerId, String profilePictureUrl.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-006]', '[REQ-002]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.membershiphub.userservice.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enterprise Social Authentication Provider Registry and Verification Engine.
 * Manages federated identity verification for Google, Facebook, and Firebase OAuth2/OIDC providers.
 *
 * @author Enterprise Architecture Engine
 * @version 1.0.0
 * @traceability [ARC-006], [REQ-002]
 */
@ApplicationScoped
public class SocialAuthProviderRegistry {

    // =========================================================================
    // TOP-OF-CLASS IMMUTABLE CONSTANTS DECLARATION [0.2]
    // =========================================================================
    public static final String MODULE_NAME = "USER-SERVICE-SECURITY";
    public static final String TRACE_TAG_ARC_006 = "[ARC-006]";
    public static final String TRACE_TAG_REQ_002 = "[REQ-002]";

    public static final String PROVIDER_FIREBASE = "firebase";
    public static final String PROVIDER_GOOGLE = "google";
    public static final String PROVIDER_FACEBOOK = "facebook";

    public static final String LOG_ENTRY_PREFIX = "[ENTRY]";
    public static final String LOG_EXIT_PREFIX = "[EXIT]";
    public static final String LOG_ERROR_PREFIX = "[CRITICAL FAIL]";

    public static final String MSG_ERR_PROVIDER_NULL = "Provider name parameter must not be null or blank.";
    public static final String MSG_ERR_TOKEN_NULL = "Identity token parameter must not be null or blank.";
    public static final String MSG_ERR_PROVIDER_UNSUPPORTED = "Unsupported social authentication provider: ";
    public static final String MSG_ERR_REGISTRY_INIT = "Failed to initialize social authentication providers registry.";
    public static final String MSG_ERR_AUTH_EXEC = "Federated social identity verification failed.";

    private static final Logger LOGGER = LoggerFactory.getLogger(SocialAuthProviderRegistry.class);

    // =========================================================================
    // INJECTED DEPENDENCIES & REGISTRY STATE
    // =========================================================================
    @Inject
    Instance<SocialAuthProvider> providerInstances;

    private final Map<String, SocialAuthProvider> registryMap = new ConcurrentHashMap<>();

    /**
     * Initializes the provider registry map by resolving all available CDI instances.
     */
    @PostConstruct
    public void initRegistry() {
        LOGGER.info("{} {} Initializing SocialAuthProviderRegistry with configured providers.",
                LOG_ENTRY_PREFIX, TRACE_TAG_ARC_006);
        try {
            for (SocialAuthProvider provider : providerInstances) {
                String providerKey = provider.getName().trim().toLowerCase(Locale.ROOT);
                registryMap.put(providerKey, provider);
                LOGGER.info("[PROCESS] {} Registered social auth provider implementation: '{}' -> {}",
                        TRACE_TAG_REQ_002, providerKey, provider.getClass().getName());
            }
            LOGGER.info("{} {} Successfully loaded {} social identity providers into registry.",
                    LOG_EXIT_PREFIX, TRACE_TAG_ARC_006, registryMap.size());
        } catch (Exception ex) {
            LOGGER.error("{} {} {} Subsystem: {}. Raw error: {}",
                    LOG_ERROR_PREFIX, TRACE_TAG_ARC_006, MSG_ERR_REGISTRY_INIT, MODULE_NAME, ex.getMessage(), ex);
            throw new IllegalStateException(MSG_ERR_REGISTRY_INIT, ex);
        }
    }

    /**
     * Authenticates and verifies a federated social identity token against the specified provider.
     *
     * @param providerName Target OAuth2/Identity provider name ('firebase', 'google', 'facebook').
     * @param idToken      Raw ID token, access token, or credential string issued by the social provider.
     * @return Standardized {@link SocialUserInfo} containing verified profile attributes.
     * @throws IllegalArgumentException If parameters are blank or provider is unsupported.
     * @throws SecurityException        If token verification or signature validation fails.
     */
    public SocialUserInfo authenticate(String providerName, String idToken) {
        LOGGER.info("{} {} Processing federated social authentication for provider: '{}'",
                LOG_ENTRY_PREFIX, TRACE_TAG_REQ_002, providerName);

        if (providerName == null || providerName.trim().isEmpty()) {
            LOGGER.error("{} {} Provider validation failed: {}", LOG_ERROR_PREFIX, TRACE_TAG_REQ_002, MSG_ERR_PROVIDER_NULL);
            throw new IllegalArgumentException(MSG_ERR_PROVIDER_NULL);
        }

        if (idToken == null || idToken.trim().isEmpty()) {
            LOGGER.error("{} {} ID Token validation failed: {}", LOG_ERROR_PREFIX, TRACE_TAG_REQ_002, MSG_ERR_TOKEN_NULL);
            throw new IllegalArgumentException(MSG_ERR_TOKEN_NULL);
        }

        String normalizedKey = providerName.trim().toLowerCase(Locale.ROOT);
        SocialAuthProvider provider = registryMap.get(normalizedKey);

        if (provider == null) {
            LOGGER.error("{} {} Requested provider '{}' is not registered in system. Subsystem: {}",
                    LOG_ERROR_PREFIX, TRACE_TAG_REQ_002, normalizedKey, MODULE_NAME);
            throw new IllegalArgumentException(MSG_ERR_PROVIDER_UNSUPPORTED + providerName);
        }

        try {
            SocialUserInfo userInfo = provider.verifyToken(idToken.trim());
            LOGGER.info("{} {} Successfully verified social identity for subject: '{}', provider: '{}'",
                    LOG_EXIT_PREFIX, TRACE_TAG_REQ_002, maskData(userInfo.getProviderId()), normalizedKey);
            return userInfo;
        } catch (Exception ex) {
            LOGGER.error("{} {} {} Provider: '{}'. Subsystem: {}. Raw error: {}",
                    LOG_ERROR_PREFIX, TRACE_TAG_REQ_002, MSG_ERR_AUTH_EXEC, normalizedKey, MODULE_NAME, ex.getMessage(), ex);
            if (ex instanceof SecurityException) {
                throw (SecurityException) ex;
            }
            throw new SecurityException(MSG_ERR_AUTH_EXEC + " Error: " + ex.getMessage(), ex);
        }
    }

    /**
     * Obtains an unmodifiable snapshot view of all registered provider handlers.
     *
     * @return Read-only map of provider names to provider instances.
     */
    public Map<String, SocialAuthProvider> getRegisteredProviders() {
        return Collections.unmodifiableMap(new HashMap<>(registryMap));
    }

    /**
     * Simple PII data masking utility for audit logging.
     *
     * @param input Raw sensitive string.
     * @return Obscured string safe for logging output.
     */
    private static String maskData(String input) {
        if (input == null || input.length() <= 4) {
            return "***MASKED***";
        }
        return input.substring(0, 2) + "****" + input.substring(input.length() - 2);
    }

    // =========================================================================
    // INNER CORE INTERFACES & DOMAIN POJOS
    // =========================================================================

    /**
     * Contract for all modular federated social identity verifiers.
     */
    public interface SocialAuthProvider {
        /**
         * Returns the unique discriminator identifier string of the provider.
         *
         * @return Unique provider key.
         */
        String getName();

        /**
         * Performs cryptographic/remote validation of the incoming identity token.
         *
         * @param idToken Raw authorization or identity token.
         * @return Normalized user profile data.
         * @throws SecurityException If the token is invalid, expired, or rejected by upstream.
         */
        SocialUserInfo verifyToken(String idToken);
    }

    /**
     * Standardized POJO container representing verified profile claims from a social provider.
     */
    public static class SocialUserInfo {
        private String email;
        private String fullName;
        private String providerId;
        private String profilePictureUrl;

        public SocialUserInfo() {
        }

        public SocialUserInfo(String email, String fullName, String providerId, String profilePictureUrl) {
            this.email = email;
            this.fullName = fullName;
            this.providerId = providerId;
            this.profilePictureUrl = profilePictureUrl;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getProviderId() {
            return providerId;
        }

        public void setProviderId(String providerId) {
            this.providerId = providerId;
        }

        public String getProfilePictureUrl() {
            return profilePictureUrl;
        }

        public void setProfilePictureUrl(String profilePictureUrl) {
            this.profilePictureUrl = profilePictureUrl;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SocialUserInfo that = (SocialUserInfo) o;
            return Objects.equals(email, that.email) &&
                    Objects.equals(fullName, that.fullName) &&
                    Objects.equals(providerId, that.providerId) &&
                    Objects.equals(profilePictureUrl, that.profilePictureUrl);
        }

        @Override
        public int hashCode() {
            return Objects.hash(email, fullName, providerId, profilePictureUrl);
        }

        @Override
        public String toString() {
            return "SocialUserInfo{" +
                    "email='" + maskData(email) + '\'' +
                    ", fullName='" + fullName + '\'' +
                    ", providerId='" + maskData(providerId) + '\'' +
                    ", profilePictureUrl='" + profilePictureUrl + '\'' +
                    '}';
        }
    }

    // =========================================================================
    // PROVIDER IMPLEMENTATION: GOOGLE OAUTH2 VERIFIER
    // =========================================================================
    @ApplicationScoped
    public static class GoogleAuthProvider implements SocialAuthProvider {

        public static final String PROVIDER_NAME = "google";
        public static final String GOOGLE_TOKEN_INFO_ENDPOINT = "https://oauth2.googleapis.com/tokeninfo?id_token=";
        public static final int HTTP_TIMEOUT_SECONDS = 5;

        private static final Logger GOOGLE_LOGGER = LoggerFactory.getLogger(GoogleAuthProvider.class);

        private final HttpClient httpClient;
        private final ObjectMapper objectMapper;

        @Inject
        @ConfigProperty(name = "membershiphub.security.oauth2.google.client-id", defaultValue = "")
        Optional<String> configuredClientId;

        public GoogleAuthProvider() {
            this.httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .connectTimeout(Duration.ofSeconds(HTTP_TIMEOUT_SECONDS))
                    .build();
            this.objectMapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }

        @Override
        public String getName() {
            return PROVIDER_NAME;
        }

        @Override
        public SocialUserInfo verifyToken(String idToken) {
            GOOGLE_LOGGER.info("{} [REQ-002] Starting Google ID token remote verification.", LOG_ENTRY_PREFIX);
            try {
                String encodedToken = URLEncoder.encode(idToken, StandardCharsets.UTF_8);
                URI uri = URI.create(GOOGLE_TOKEN_INFO_ENDPOINT + encodedToken);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .timeout(Duration.ofSeconds(HTTP_TIMEOUT_SECONDS))
                        .header("Accept", "application/json")
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    GOOGLE_LOGGER.error("{} [REQ-002] Google endpoint returned non-200 status: {}. Subsystem: {}",
                            LOG_ERROR_PREFIX, response.statusCode(), MODULE_NAME);
                    throw new SecurityException("Google ID Token verification rejected with status code: " + response.statusCode());
                }

                GoogleTokenInfoResponse tokenInfo = objectMapper.readValue(response.body(), GoogleTokenInfoResponse.class);

                if (tokenInfo.getErrorDescription() != null || tokenInfo.getSub() == null) {
                    GOOGLE_LOGGER.error("{} [REQ-002] Google token response contains error: {}. Subsystem: {}",
                            LOG_ERROR_PREFIX, tokenInfo.getErrorDescription(), MODULE_NAME);
                    throw new SecurityException("Invalid Google ID Token payload: " + tokenInfo.getErrorDescription());
                }

                // Audience binding check if configured
                configuredClientId.ifPresent(expectedAud -> {
                    if (!expectedAud.isBlank() && !expectedAud.equals(tokenInfo.getAud())) {
                        GOOGLE_LOGGER.error("{} [REQ-002] Google Token audience mismatch. Expected: {}, Actual: {}",
                                LOG_ERROR_PREFIX, expectedAud, tokenInfo.getAud());
                        throw new SecurityException("Google ID Token audience mismatch.");
                    }
                });

                SocialUserInfo userInfo = new SocialUserInfo(
                        tokenInfo.getEmail(),
                        tokenInfo.getName() != null ? tokenInfo.getName() : tokenInfo.getEmail(),
                        tokenInfo.getSub(),
                        tokenInfo.getPicture()
                );

                GOOGLE_LOGGER.info("{} [REQ-002] Successfully validated Google ID Token for sub: {}",
                        LOG_EXIT_PREFIX, maskData(userInfo.getProviderId()));
                return userInfo;

            } catch (IOException | InterruptedException ex) {
                if (ex instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                GOOGLE_LOGGER.error("{} [REQ-002] Network/IO error communicating with Google OAuth2 API. Subsystem: {}. Raw error: {}",
                        LOG_ERROR_PREFIX, MODULE_NAME, ex.getMessage(), ex);
                throw new SecurityException("Failed to verify Google token due to upstream network issue.", ex);
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class GoogleTokenInfoResponse {
            @JsonProperty("sub")
            private String sub;
            @JsonProperty("email")
            private String email;
            @JsonProperty("name")
            private String name;
            @JsonProperty("picture")
            private String picture;
            @JsonProperty("aud")
            private String aud;
            @JsonProperty("error_description")
            private String errorDescription;

            public String getSub() {
                return sub;
            }

            public void setSub(String sub) {
                this.sub = sub;
            }

            public String getEmail() {
                return email;
            }

            public void setEmail(String email) {
                this.email = email;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getPicture() {
                return picture;
            }

            public void setPicture(String picture) {
                this.picture = picture;
            }

            public String getAud() {
                return aud;
            }

            public void setAud(String aud) {
                this.aud = aud;
            }

            public String getErrorDescription() {
                return errorDescription;
            }

            public void setErrorDescription(String errorDescription) {
                this.errorDescription = errorDescription;
            }
        }
    }

    // =========================================================================
    // PROVIDER IMPLEMENTATION: FIREBASE AUTHENTICATION VERIFIER
    // =========================================================================
    @ApplicationScoped
    public static class FirebaseAuthProvider implements SocialAuthProvider {

        public static final String PROVIDER_NAME = "firebase";
        public static final String FIREBASE_LOOKUP_ENDPOINT = "https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=";
        public static final int HTTP_TIMEOUT_SECONDS = 5;

        private static final Logger FIREBASE_LOGGER = LoggerFactory.getLogger(FirebaseAuthProvider.class);

        private final HttpClient httpClient;
        private final ObjectMapper objectMapper;

        @Inject
        @ConfigProperty(name = "membershiphub.security.firebase.web-api-key", defaultValue = "")
        String firebaseWebApiKey;

        public FirebaseAuthProvider() {
            this.httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .connectTimeout(Duration.ofSeconds(HTTP_TIMEOUT_SECONDS))
                    .build();
            this.objectMapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }

        @Override
        public String getName() {
            return PROVIDER_NAME;
        }

        @Override
        public SocialUserInfo verifyToken(String idToken) {
            FIREBASE_LOGGER.info("{} [REQ-002] Starting Firebase ID token verification.", LOG_ENTRY_PREFIX);
            try {
                String targetUri = FIREBASE_LOOKUP_ENDPOINT + (firebaseWebApiKey != null ? firebaseWebApiKey.trim() : "");
                String payload = objectMapper.writeValueAsString(Map.of("idToken", idToken));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(targetUri))
                        .timeout(Duration.ofSeconds(HTTP_TIMEOUT_SECONDS))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    FIREBASE_LOGGER.error("{} [REQ-002] Firebase accounts:lookup rejected request. Status: {}. Subsystem: {}",
                            LOG_ERROR_PREFIX, response.statusCode(), MODULE_NAME);
                    throw new SecurityException("Firebase ID Token verification failed with status: " + response.statusCode());
                }

                FirebaseLookupResponse lookupResponse = objectMapper.readValue(response.body(), FirebaseLookupResponse.class);

                if (lookupResponse.getUsers() == null || lookupResponse.getUsers().length == 0) {
                    FIREBASE_LOGGER.error("{} [REQ-002] No user records returned by Firebase toolkit. Subsystem: {}",
                            LOG_ERROR_PREFIX, MODULE_NAME);
                    throw new SecurityException("Firebase ID Token is valid but references no active user.");
                }

                FirebaseUserRecord record = lookupResponse.getUsers()[0];
                SocialUserInfo userInfo = new SocialUserInfo(
                        record.getEmail(),
                        record.getDisplayName() != null ? record.getDisplayName() : record.getEmail(),
                        record.getLocalId(),
                        record.getPhotoUrl()
                );

                FIREBASE_LOGGER.info("{} [REQ-002] Successfully verified Firebase identity for localId: {}",
                        LOG_EXIT_PREFIX, maskData(userInfo.getProviderId()));
                return userInfo;

            } catch (IOException | InterruptedException ex) {
                if (ex instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                FIREBASE_LOGGER.error("{} [REQ-002] Communication failure with Firebase Admin Identity Toolkit. Subsystem: {}. Raw error: {}",
                        LOG_ERROR_PREFIX, MODULE_NAME, ex.getMessage(), ex);
                throw new SecurityException("Firebase verification network or parsing exception occurred.", ex);
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class FirebaseLookupResponse {
            @JsonProperty("users")
            private FirebaseUserRecord[] users;

            public FirebaseUserRecord[] getUsers() {
                return users;
            }

            public void setUsers(FirebaseUserRecord[] users) {
                this.users = users;
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class FirebaseUserRecord {
            @JsonProperty("localId")
            private String localId;
            @JsonProperty("email")
            private String email;
            @JsonProperty("displayName")
            private String displayName;
            @JsonProperty("photoUrl")
            private String photoUrl;

            public String getLocalId() {
                return localId;
            }

            public void setLocalId(String localId) {
                this.localId = localId;
            }

            public String getEmail() {
                return email;
            }

            public void setEmail(String email) {
                this.email = email;
            }

            public String getDisplayName() {
                return displayName;
            }

            public void setDisplayName(String displayName) {
                this.displayName = displayName;
            }

            public String getPhotoUrl() {
                return photoUrl;
            }

            public void setPhotoUrl(String photoUrl) {
                this.photoUrl = photoUrl;
            }
        }
    }

    // =========================================================================
    // PROVIDER IMPLEMENTATION: FACEBOOK GRAPH API VERIFIER
    // =========================================================================
    @ApplicationScoped
    public static class FacebookAuthProvider implements SocialAuthProvider {

        public static final String PROVIDER_NAME = "facebook";
        public static final String FB_DEBUG_TOKEN_ENDPOINT = "https://graph.facebook.com/v18.0/debug_token";
        public static final String FB_USER_ME_ENDPOINT = "https://graph.facebook.com/v18.0/me?fields=id,name,email,picture.width(200).height(200)&access_token=";
        public static final int HTTP_TIMEOUT_SECONDS = 5;

        private static final Logger FB_LOGGER = LoggerFactory.getLogger(FacebookAuthProvider.class);

        private final HttpClient httpClient;
        private final ObjectMapper objectMapper;

        @Inject
        @ConfigProperty(name = "membershiphub.security.oauth2.facebook.app-id", defaultValue = "")
        String facebookAppId;

        @Inject
        @ConfigProperty(name = "membershiphub.security.oauth2.facebook.app-secret", defaultValue = "")
        String facebookAppSecret;

        public FacebookAuthProvider() {
            this.httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .connectTimeout(Duration.ofSeconds(HTTP_TIMEOUT_SECONDS))
                    .build();
            this.objectMapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }

        @Override
        public String getName() {
            return PROVIDER_NAME;
        }

        @Override
        public SocialUserInfo verifyToken(String userAccessToken) {
            FB_LOGGER.info("{} [REQ-002] Starting Facebook User Access Token validation.", LOG_ENTRY_PREFIX);
            try {
                // Step 1: Validate token authenticity against App Access Token (if configured)
                if (facebookAppId != null && !facebookAppId.isBlank() && facebookAppSecret != null && !facebookAppSecret.isBlank()) {
                    String appAccessToken = facebookAppId.trim() + "|" + facebookAppSecret.trim();
                    String debugUrl = FB_DEBUG_TOKEN_ENDPOINT + "?input_token=" +
                            URLEncoder.encode(userAccessToken, StandardCharsets.UTF_8) +
                            "&access_token=" + URLEncoder.encode(appAccessToken, StandardCharsets.UTF_8);

                    HttpRequest debugRequest = HttpRequest.newBuilder()
                            .uri(URI.create(debugUrl))
                            .timeout(Duration.ofSeconds(HTTP_TIMEOUT_SECONDS))
                            .header("Accept", "application/json")
                            .GET()
                            .build();

                    HttpResponse<String> debugResponse = httpClient.send(debugRequest, HttpResponse.BodyHandlers.ofString());

                    if (debugResponse.statusCode() != 200) {
                        FB_LOGGER.error("{} [REQ-002] Facebook debug_token verification failed. Status: {}. Subsystem: {}",
                                LOG_ERROR_PREFIX, debugResponse.statusCode(), MODULE_NAME);
                        throw new SecurityException("Facebook token signature verification rejected by Graph API.");
                    }

                    FacebookDebugResponse debugResult = objectMapper.readValue(debugResponse.body(), FacebookDebugResponse.class);
                    if (debugResult.getData() == null || !debugResult.getData().isValid()) {
                        FB_LOGGER.error("{} [REQ-002] Facebook token is marked as invalid. Subsystem: {}",
                                LOG_ERROR_PREFIX, MODULE_NAME);
                        throw new SecurityException("Facebook access token is invalid or expired.");
                    }
                }

                // Step 2: Fetch User Profile Information from /me endpoint
                String meUrl = FB_USER_ME_ENDPOINT + URLEncoder.encode(userAccessToken, StandardCharsets.UTF_8);
                HttpRequest meRequest = HttpRequest.newBuilder()
                        .uri(URI.create(meUrl))
                        .timeout(Duration.ofSeconds(HTTP_TIMEOUT_SECONDS))
                        .header("Accept", "application/json")
                        .GET()
                        .build();

                HttpResponse<String> meResponse = httpClient.send(meRequest, HttpResponse.BodyHandlers.ofString());

                if (meResponse.statusCode() != 200) {
                    FB_LOGGER.error("{} [REQ-002] Facebook /me profile query returned non-200: {}. Subsystem: {}",
                            LOG_ERROR_PREFIX, meResponse.statusCode(), MODULE_NAME);
                    throw new SecurityException("Failed to retrieve Facebook user profile details.");
                }

                FacebookMeResponse meData = objectMapper.readValue(meResponse.body(), FacebookMeResponse.class);

                String profilePic = null;
                if (meData.getPicture() != null && meData.getPicture().getData() != null) {
                    profilePic = meData.getPicture().getData().getUrl();
                }

                SocialUserInfo userInfo = new SocialUserInfo(
                        meData.getEmail(),
                        meData.getName(),
                        meData.getId(),
                        profilePic
                );

                FB_LOGGER.info("{} [REQ-002] Successfully verified Facebook user token for id: {}",
                        LOG_EXIT_PREFIX, maskData(userInfo.getProviderId()));
                return userInfo;

            } catch (IOException | InterruptedException ex) {
                if (ex instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                FB_LOGGER.error("{} [REQ-002] Network/IO error communicating with Facebook Graph API. Subsystem: {}. Raw error: {}",
                        LOG_ERROR_PREFIX, MODULE_NAME, ex.getMessage(), ex);
                throw new SecurityException("Facebook verification network communication error.", ex);
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class FacebookDebugResponse {
            @JsonProperty("data")
            private FacebookDebugData data;

            public FacebookDebugData getData() {
                return data;
            }

            public void setData(FacebookDebugData data) {
                this.data = data;
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class FacebookDebugData {
            @JsonProperty("app_id")
            private String appId;
            @JsonProperty("is_valid")
            private boolean isValid;
            @JsonProperty("user_id")
            private String userId;

            public String getAppId() {
                return appId;
            }

            public void setAppId(String appId) {
                this.appId = appId;
            }

            public boolean isValid() {
                return isValid;
            }

            public void setValid(boolean valid) {
                isValid = valid;
            }

            public String getUserId() {
                return userId;
            }

            public void setUserId(String userId) {
                this.userId = userId;
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class FacebookMeResponse {
            @JsonProperty("id")
            private String id;
            @JsonProperty("name")
            private String name;
            @JsonProperty("email")
            private String email;
            @JsonProperty("picture")
            private FacebookPictureContainer picture;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getEmail() {
                return email;
            }

            public void setEmail(String email) {
                this.email = email;
            }

            public FacebookPictureContainer getPicture() {
                return picture;
            }

            public void setPicture(FacebookPictureContainer picture) {
                this.picture = picture;
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class FacebookPictureContainer {
            @JsonProperty("data")
            private FacebookPictureData data;

            public FacebookPictureData getData() {
                return data;
            }

            public void setData(FacebookPictureData data) {
                this.data = data;
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class FacebookPictureData {
            @JsonProperty("url")
            private String url;

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }
        }
    }
}
```

# Day 3: model models/gemini-flash-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/AuthAuditLogger.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/AuthAuditLogger.java`
*   Traceability Audit Tags For This Task: ['[NFR-003]', '[NFR-006]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo tệp mã nguồn ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/AuthAuditLogger.java hiện thực hóa lớp AuthAuditLogger với annotation @ApplicationScoped, inject AuditLogRepository (Panache Repository). Phương thức logAuthEvent(UUID userId, String action, String details) tạo bản ghi AuditLog với action thuộc tập LOGIN_SUCCESS, LOGIN_FAILED, LOGOUT, SOCIAL_AUTH_SUCCESS, SOCIAL_AUTH_FAILED, TOKEN_REFRESH, ROLE_CHANGED, lưu details dạng JSON chứa IP, User-Agent. Annotation @Transactional đảm bảo ghi log trong cùng transaction với nghiệp vụ. Cấu hình logger SLF4J với mức INFO, output định dạng JSON cho stack ELK, tích hợp OpenTelemetry tracing.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[NFR-003]', '[NFR-006]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.membershiphub.userservice.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.nlh4j.membershiphub.userservice.entity.AuditLog;
import org.nlh4j.membershiphub.userservice.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Enterprise Audit Logging Service for Authentication and Identity Operations.
 * <p>
 * This component provides robust, tamper-resistant auditing for all security and authentication
 * lifecycle events across the Membership Hub platform. It guarantees non-repudiation, captures
 * distributed tracing telemetry for centralized observability (ELK Stack, Google Cloud Logging),
 * and enforces strict PII scrubbing before committing audit data to persistence or log streams.
 * </p>
 *
 * @author Enterprise Architecture Core Team
 * @version 1.0.0
 * @since 2026-08-29
 * @traceability [NFR-003], [NFR-006]
 */
@ApplicationScoped
public class AuthAuditLogger {

    // =========================================================================
    // TOP-OF-CLASS CONSTANTS DECLARATION (GOVERNANCE MANDATE [0.2])
    // =========================================================================

    /** Subsystem identifier for logging and tracing context. // [NFR-006] */
    public static final String SUBSYSTEM_NAME = "USER-SERVICE-AUDIT";

    /** Traceability tracking tag identifier for audit compliance. // [NFR-006] */
    public static final String TAG_AUDIT_COMPLIANCE = "[NFR-006]";

    /** Traceability tracking tag identifier for baseline security. // [NFR-003] */
    public static final String TAG_SECURITY_BASELINE = "[NFR-003]";

    // --- Action Names ---
    public static final String ACTION_LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String ACTION_LOGIN_FAILED = "LOGIN_FAILED";
    public static final String ACTION_LOGOUT = "LOGOUT";
    public static final String ACTION_SOCIAL_AUTH_SUCCESS = "SOCIAL_AUTH_SUCCESS";
    public static final String ACTION_SOCIAL_AUTH_FAILED = "SOCIAL_AUTH_FAILED";
    public static final String ACTION_TOKEN_REFRESH = "TOKEN_REFRESH";
    public static final String ACTION_ROLE_CHANGED = "ROLE_CHANGED";

    /** Immutable set of valid authorized audit actions. // [NFR-006] */
    public static final Set<String> ALLOWED_ACTIONS = Collections.unmodifiableSet(Set.of(
            ACTION_LOGIN_SUCCESS,
            ACTION_LOGIN_FAILED,
            ACTION_LOGOUT,
            ACTION_SOCIAL_AUTH_SUCCESS,
            ACTION_SOCIAL_AUTH_FAILED,
            ACTION_TOKEN_REFRESH,
            ACTION_ROLE_CHANGED
    ));

    // --- JSON Schema Keys for Structured Logs & Audit Details ---
    public static final String JSON_KEY_EVENT_TYPE = "eventType";
    public static final String JSON_KEY_SUBSYSTEM = "subsystem";
    public static final String JSON_KEY_USER_ID = "userId";
    public static final String JSON_KEY_ACTION = "action";
    public static final String JSON_KEY_OCCURRED_AT = "occurredAt";
    public static final String JSON_KEY_TRACE_ID = "traceId";
    public static final String JSON_KEY_SPAN_ID = "spanId";
    public static final String JSON_KEY_CLIENT_IP = "clientIp";
    public static final String JSON_KEY_USER_AGENT = "userAgent";
    public static final String JSON_KEY_DETAILS = "details";
    public static final String JSON_KEY_RAW_DETAILS = "rawDetails";
    public static final String JSON_KEY_AUDIT_ID = "auditId";
    public static final String JSON_KEY_STATUS = "status";

    // --- Default & Fallback Constants ---
    public static final String DEFAULT_UNKNOWN_VALUE = "UNKNOWN";
    public static final String DEFAULT_ANONYMOUS_USER = "ANONYMOUS";
    public static final String DEFAULT_SUCCESS_STATUS = "SUCCESS";
    public static final String DEFAULT_FAILURE_STATUS = "FAILED";
    public static final String MASKED_CREDENTIAL_PLACEHOLDER = "******";
    public static final String EMPTY_JSON_OBJECT = "{}";

    // --- Sensitive Data Scrubbing Regex Patterns ---
    private static final Pattern SENSITIVE_TOKEN_PATTERN = Pattern.compile("(?i)(bearer\\s+|token[\"':\\s]+)[a-zA-Z0-9._\\-]{15,}");
    private static final Pattern SENSITIVE_PASSWORD_PATTERN = Pattern.compile("(?i)(\"password\"\\s*:\\s*\")[^\"]+(\")");
    private static final Pattern SENSITIVE_CREDIT_CARD_PATTERN = Pattern.compile("\\b(?:\\d[ -]*?){13,16}\\b");

    // =========================================================================
    // CLASS STATE & INJECTED DEPENDENCIES
    // =========================================================================

    /** Standard SLF4J Logger binding for enterprise structured log emission. // [0.3] */
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthAuditLogger.class);

    /** Injected Panache Repository for managing AuditLog persistence. // [NFR-006] */
    @Inject
    AuditLogRepository auditLogRepository;

    /** Injected Jackson ObjectMapper for JSON serialization and parsing. // [NFR-006] */
    @Inject
    ObjectMapper objectMapper;

    // =========================================================================
    // PUBLIC AUDIT LOGGING OPERATIONS
    // =========================================================================

    /**
     * Primary operational method to record security and authentication audit events.
     * <p>
     * Ensures persistence within the active transaction boundary, scrubbed logging for
     * centralized indexing (ELK/Stackdriver), and OpenTelemetry trace binding.
     * </p>
     *
     * @param userId  The unique identifier of the user executing or targeted by the action (nullable for anonymous).
     * @param action  The audit action label (must belong to {@link #ALLOWED_ACTIONS}).
     * @param details Structured JSON string containing auxiliary operational attributes (IP, User-Agent, metadata).
     * @throws IllegalArgumentException if the action parameter is invalid or unapproved.
     * @traceability [NFR-003], [NFR-006]
     */
    @Transactional(Transactional.TxType.REQUIRED) // [NFR-006]: Guarantees transactional consistency with caller flow
    public void logAuthEvent(UUID userId, String action, String details) {
        // [PROCESS] Entry Gate Logging with tracing metadata
        LOGGER.debug("[ENTRY] {} {} Executing logAuthEvent for Action: {}, UserID: {}", 
                TAG_AUDIT_COMPLIANCE, SUBSYSTEM_NAME, action, userId != null ? userId : DEFAULT_ANONYMOUS_USER);

        // 1. Validate mandatory parameters against governance rules
        if (action == null || action.trim().isEmpty()) {
            LOGGER.error("[CRITICAL FAIL] {} {} Audit action cannot be null or blank.", 
                    TAG_SECURITY_BASELINE, SUBSYSTEM_NAME);
            throw new IllegalArgumentException("Audit action must not be null or blank.");
        }

        final String normalizedAction = action.trim().toUpperCase();
        if (!ALLOWED_ACTIONS.contains(normalizedAction)) {
            LOGGER.error("[CRITICAL FAIL] {} {} Unauthorized audit action attempted: {}. Allowed: {}", 
                    TAG_SECURITY_BASELINE, SUBSYSTEM_NAME, normalizedAction, ALLOWED_ACTIONS);
            throw new IllegalArgumentException("Unauthorized audit action: " + normalizedAction);
        }

        // 2. Sanitize and mask details payload to prevent PII/Credential leakage
        final String sanitizedDetails = sanitizeAndMaskDetails(details);

        // 3. Extract OpenTelemetry distributed tracing context for correlation
        final SpanContext currentSpanContext = Span.current().getSpanContext();
        final String traceId = currentSpanContext.isValid() ? currentSpanContext.getTraceId() : DEFAULT_UNKNOWN_VALUE;
        final String spanId = currentSpanContext.isValid() ? currentSpanContext.getSpanId() : DEFAULT_UNKNOWN_VALUE;

        // 4. Construct AuditLog persistent entity
        final AuditLog auditLog = new AuditLog();
        auditLog.setLogId(UUID.randomUUID());
        auditLog.setUserId(userId);
        auditLog.setAction(normalizedAction);
        auditLog.setDetails(sanitizedDetails);
        auditLog.setOccurredAt(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC));

        try {
            // 5. Persist audit log entity to PostgreSQL database via Panache
            auditLogRepository.persist(auditLog);

            // 6. Format and emit structured JSON log to stdout/stderr for ELK/Cloud Logging ingestion
            emitStructuredAuditLog(auditLog, traceId, spanId);

            // [PROCESS] Completion Gate Logging
            LOGGER.info("[SUCCESS] {} {} Audit record persisted. Action: {}, LogID: {}, TraceID: {}", 
                    TAG_AUDIT_COMPLIANCE, SUBSYSTEM_NAME, normalizedAction, auditLog.getLogId(), traceId);

        } catch (Exception e) {
            // [CRITICAL FAIL] Detailed error logging preserving cause chain
            LOGGER.error("[CRITICAL FAIL] {} {} Database persistence failed for audit action: {}. Raw error: {}", 
                    TAG_AUDIT_COMPLIANCE, SUBSYSTEM_NAME, normalizedAction, e.getMessage(), e);
            throw new RuntimeException("Audit log persistence failed for action: " + normalizedAction, e);
        }
    }

    /**
     * Overloaded helper method to log authentication events with explicit client network metadata.
     *
     * @param userId    The unique identifier of the user (nullable).
     * @param action    The audit action label.
     * @param clientIp  Client IP address initiating the transaction.
     * @param userAgent HTTP User-Agent header string.
     * @param metadata  Additional key-value metadata to pack into the JSON payload.
     * @traceability [NFR-003], [NFR-006]
     */
    @Transactional(Transactional.TxType.REQUIRED)
    public void logAuthEventWithClientInfo(UUID userId, String action, String clientIp, String userAgent, ObjectNode metadata) {
        // [PROCESS] Entry Gate
        LOGGER.debug("[ENTRY] {} {} Executing logAuthEventWithClientInfo for Action: {}", 
                TAG_AUDIT_COMPLIANCE, SUBSYSTEM_NAME, action);

        final ObjectNode rootNode = (metadata != null) ? metadata : objectMapper.createObjectNode();
        rootNode.put(JSON_KEY_CLIENT_IP, (clientIp != null && !clientIp.isBlank()) ? clientIp.trim() : DEFAULT_UNKNOWN_VALUE);
        rootNode.put(JSON_KEY_USER_AGENT, (userAgent != null && !userAgent.isBlank()) ? userAgent.trim() : DEFAULT_UNKNOWN_VALUE);

        String serializedDetails;
        try {
            serializedDetails = objectMapper.writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            LOGGER.warn("[WARN] {} {} Failed to serialize structured audit metadata. Fallback to empty. Error: {}", 
                    TAG_AUDIT_COMPLIANCE, SUBSYSTEM_NAME, e.getMessage());
            serializedDetails = EMPTY_JSON_OBJECT;
        }

        // Forward to primary transactional persistence pipeline
        logAuthEvent(userId, action, serializedDetails);
    }

    // =========================================================================
    // INTERNAL UTILITY & DATA PROTECTION ROUTINES
    // =========================================================================

    /**
     * Programmatically scrubs sensitive credentials, private keys, and passwords from log payloads.
     *
     * @param rawDetails Raw JSON or plain text details.
     * @return Sanitized string safe for long-term audit storage and log shipping.
     * @traceability [NFR-003]
     */
    private String sanitizeAndMaskDetails(String rawDetails) {
        if (rawDetails == null || rawDetails.trim().isEmpty()) {
            return EMPTY_JSON_OBJECT;
        }

        // Apply regex scrubbing filters against credentials and sensitive tokens
        String sanitized = SENSITIVE_TOKEN_PATTERN.matcher(rawDetails).replaceAll("$1" + MASKED_CREDENTIAL_PLACEHOLDER);
        sanitized = SENSITIVE_PASSWORD_PATTERN.matcher(sanitized).replaceAll("$1" + MASKED_CREDENTIAL_PLACEHOLDER + "$2");
        sanitized = SENSITIVE_CREDIT_CARD_PATTERN.matcher(sanitized).replaceAll(MASKED_CREDENTIAL_PLACEHOLDER);

        // Verify if sanitized content is valid JSON; if not, wrap safely into JSON object
        try {
            objectMapper.readTree(sanitized);
            return sanitized;
        } catch (JsonProcessingException e) {
            LOGGER.debug("[DEBUG] {} {} Wrapping non-JSON audit detail string into standard JSON node.", 
                    TAG_AUDIT_COMPLIANCE, SUBSYSTEM_NAME);
            final ObjectNode wrapperNode = objectMapper.createObjectNode();
            wrapperNode.put(JSON_KEY_RAW_DETAILS, sanitized);
            try {
                return objectMapper.writeValueAsString(wrapperNode);
            } catch (JsonProcessingException ex) {
                return EMPTY_JSON_OBJECT;
            }
        }
    }

    /**
     * Emits a high-performance JSON-formatted log line enriched with OpenTelemetry spans and MDC.
     *
     * @param auditLog Persistent audit log model.
     * @param traceId  Distributed OpenTelemetry Trace ID.
     * @param spanId   Distributed OpenTelemetry Span ID.
     * @traceability [NFR-006]
     */
    private void emitStructuredAuditLog(AuditLog auditLog, String traceId, String spanId) {
        try {
            final ObjectNode logNode = objectMapper.createObjectNode();
            logNode.put(JSON_KEY_SUBSYSTEM, SUBSYSTEM_NAME);
            logNode.put(JSON_KEY_EVENT_TYPE, "AUDIT_SECURITY_EVENT");
            logNode.put(JSON_KEY_AUDIT_ID, Objects.toString(auditLog.getLogId(), DEFAULT_UNKNOWN_VALUE));
            logNode.put(JSON_KEY_USER_ID, auditLog.getUserId() != null ? auditLog.getUserId().toString() : DEFAULT_ANONYMOUS_USER);
            logNode.put(JSON_KEY_ACTION, auditLog.getAction());
            logNode.put(JSON_KEY_OCCURRED_AT, Objects.toString(auditLog.getOccurredAt(), Instant.now().toString()));
            logNode.put(JSON_KEY_TRACE_ID, traceId);
            logNode.put(JSON_KEY_SPAN_ID, spanId);
            logNode.put(JSON_KEY_DETAILS, auditLog.getDetails());

            // Bind MDC context variables for native Logback/Quarkus pattern formatters
            MDC.put(JSON_KEY_TRACE_ID, traceId);
            MDC.put(JSON_KEY_SPAN_ID, spanId);
            MDC.put(JSON_KEY_ACTION, auditLog.getAction());

            final String structuredJsonPayload = objectMapper.writeValueAsString(logNode);

            // Emit to SLF4J at INFO level for centralized cloud collector harvesting
            LOGGER.info("[AUDIT_STREAM] {}", structuredJsonPayload);

        } catch (Exception e) {
            LOGGER.error("[WARN] {} {} Failed to emit structured JSON audit stream. Error: {}", 
                    TAG_AUDIT_COMPLIANCE, SUBSYSTEM_NAME, e.getMessage(), e);
        } finally {
            // Clean MDC context to avoid thread-local pollution
            MDC.remove(JSON_KEY_TRACE_ID);
            MDC.remove(JSON_KEY_SPAN_ID);
            MDC.remove(JSON_KEY_ACTION);
        }
    }
}
```

