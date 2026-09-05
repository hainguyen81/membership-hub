# Day 3: model models/gemini-flash-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtTokenProvider.java
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/security/JwtTokenProviderTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Test Component Destination Path: `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/security/JwtTokenProviderTest.java` (Must map to sources/backend/ or sources/frontend/)




### 📁 TARGET SOURCE IMPLEMENTATION CONTEXT (VERIFICATION TARGET)
Analyze the core logical operations within this implementation code block to construct your isolated unit assertions:
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


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Tạo tệp kiểm thử đơn vị ./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/security/JwtTokenProviderTest.java sử dụng JUnit 5 (@QuarkusTest) kiểm thử toàn diện lớp JwtTokenProvider. Test case 1: generateAccessToken_returnsValidJwt xác minh JWT chứa claim sub, group, iss=membership-hub, exp trong tương lai 15 phút. Test case 2: generateRefreshToken_returnsSevenDayToken xác minh refresh token có thời hạn đúng 7 ngày. Test case 3: validateToken_acceptsValidToken xác nhận token hợp lệ. Test case 4: validateToken_rejectsExpiredToken xác nhận token hết hạn bị từ chối. Test case 5: validateToken_rejectsInvalidSignature xác nhận token sai chữ ký bị từ chối. Sử dụng khóa RSA test fixture nội bộ.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` structure. Ensure that you read the exact Tag IDs from the `['[ARC-006]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.membershiphub.userservice.security;

import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Enterprise Unit Test Suite for {@link JwtTokenProvider}.
 * Verifies RS256 token generation, claim composition, temporal validity intervals,
 * cryptographic signature validation, and defensive boundary handling against attack vectors.
 *
 * @author Enterprise Quality Assurance Automation Team
 * @version 1.0.0
 * @verifies [ARC-006], [NFR-003], [REQ-001], [REQ-002]
 */
@ExtendWith(MockitoExtension.class)
public class JwtTokenProviderTest {

    // =========================================================================
    // 0. TOP-OF-CLASS IMMUTABLE CONSTANTS DECLARATION [0.2]
    // =========================================================================

    /** Subsystem identification tag for audit logging [0.1] */
    public static final String SUBSYSTEM_NAME = "USER-SERVICE:JWT-TEST-SUITE";

    /** Architecture requirement tag for hybrid OAuth2/JWT authentication */
    public static final String TAG_ARC_006 = "[ARC-006]";

    /** Non-functional requirement tag for zero-trust enterprise security standards */
    public static final String TAG_NFR_003 = "[NFR-003]";

    /** Functional requirement tag for user registration and credential generation */
    public static final String TAG_REQ_001 = "[REQ-001]";

    /** Functional requirement tag for federated social identity authentication */
    public static final String TAG_REQ_002 = "[REQ-002]";

    /** Standard test subject user ID */
    public static final String TEST_USER_ID = "c0a80101-8b9a-4f5c-9c1d-123456789abc";

    /** Standard test role assigned to subject */
    public static final String TEST_ROLE_STUDENT = "STUDENT";

    /** Standard test role assigned to administrator */
    public static final String TEST_ROLE_ADMIN = "SYSTEM_ADMIN";

    /** Identity provider descriptor for native local authentication */
    public static final String TEST_PROVIDER_LOCAL = "local";

    /** Identity provider descriptor for Google OAuth2 authentication */
    public static final String TEST_PROVIDER_GOOGLE = "google";

    /** Identity provider descriptor for Firebase social authentication */
    public static final String TEST_PROVIDER_FIREBASE = "firebase";

    /** Configured issuer expectation matching corporate specification */
    public static final String EXPECTED_ISSUER = "membership-hub";

    /** Configured audience expectation matching client gateway specification */
    public static final String EXPECTED_AUDIENCE = "membership-hub-client";

    /** Alternative custom issuer for boundary verification */
    public static final String CUSTOM_ISSUER = "https://auth.membershiphub.org";

    /** Dummy valid signed compact JWT mock string */
    public static final String DUMMY_SIGNED_JWT = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0In0.dummySignature";

    /** Dummy malformed compact JWT string */
    public static final String MALFORMED_JWT = "invalid.token.payload";

    /** Empty whitespace string constant for boundary testing */
    public static final String BLANK_WHITESPACE = "   ";

    /** Cryptographic RSA key algorithm identifier */
    public static final String RSA_ALGORITHM = "RSA";

    /** RSA key pair bit length for synthetic test fixture generation */
    public static final int RSA_KEY_SIZE = 2048;

    /** Tolerance delta in seconds for verifying timestamp expirations */
    public static final long EXPIRATION_TOLERANCE_SECONDS = 5L;

    /** Test execution logger instance [0.3] */
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProviderTest.class);

    // =========================================================================
    // MOCKED DEPENDENCIES & INJECTED SYSTEM UNDER TEST (SUT)
    // =========================================================================

    /** Mock SmallRye JWT Parser responsible for verifying signatures */
    @Mock
    private JWTParser jwtParser;

    /** System Under Test instance with injected mock parser */
    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    /** Synthetic RSA 2048-bit Public Key fixture */
    private PublicKey testPublicKey;

    // =========================================================================
    // TEST FIXTURE INITIALIZATION & TEARDOWN LIFECYCLE
    // =========================================================================

    /**
     * Initializes reflection-based configuration properties and synthetic RSA key fixtures
     * before the execution of each isolated unit assertion.
     *
     * @throws Exception If reflection field access or cryptographic key synthesis fails.
     */
    @BeforeEach
    public void setUp() throws Exception {
        logger.info("[TEST_SETUP] [{}] Initializing mock environment and synthetic cryptographic fixtures...", SUBSYSTEM_NAME);

        // Generate synthetic RSA keypair for cryptographic test fixtures
        final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyGen.initialize(RSA_KEY_SIZE);
        final KeyPair keyPair = keyGen.generateKeyPair();
        this.testPublicKey = keyPair.getPublic();

        // Inject configuration property values into package-private fields of SUT
        setField(jwtTokenProvider, "configuredIssuer", EXPECTED_ISSUER);
        setField(jwtTokenProvider, "configuredAudience", Optional.of(EXPECTED_AUDIENCE));
        setField(jwtTokenProvider, "publicKeyLocation", "publicKey.pem");
        setField(jwtTokenProvider, "cachedPublicKey", this.testPublicKey);

        logger.info("[TEST_SETUP] [{}] Setup completed successfully for test isolation.", SUBSYSTEM_NAME);
    }

    /**
     * Helper utility to reflectively assign private/package-private fields on the target bean.
     *
     * @param target    The object instance on which to set the field.
     * @param fieldName The declared field name string.
     * @param value     The value to inject into the field.
     * @throws Exception If field resolution or accessibility modification fails.
     */
    private void setField(Object target, String fieldName, Object value) throws Exception {
        final Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // =========================================================================
    // 1. HAPPY PATH TEST CASES (PRISTINE BUSINESS FLOWS)
    // =========================================================================

    @Nested
    @DisplayName("Category 1: Happy Path & Core Contract Validation")
    class HappyPathTests {

        /**
         * Verifies that generating an access token sets all mandatory standard and custom claims
         * including sub, role groups, issuer, audience, and a 15-minute expiration lifespan.
         *
         * @verifies [REQ-001], [REQ-002], [ARC-006], [NFR-003]
         */
        @Test
        @DisplayName("Test 1: generateAccessToken_returnsValidJwt - verifies claims, groups, issuer, and 15-min lifespan")
        public void generateAccessToken_returnsValidJwt() {
            logger.info("[TEST_START] [{}] [ARC-006] Executing access token generation assertion...", SUBSYSTEM_NAME);

            // Mock SmallRye claims builder pipeline
            try (MockedStatic<Jwt> mockedJwt = Mockito.mockStatic(Jwt.class)) {
                final JwtClaimsBuilder claimsBuilder = mock(JwtClaimsBuilder.class);
                mockedJwt.when(Jwt::claims).thenReturn(claimsBuilder);

                when(claimsBuilder.issuer(EXPECTED_ISSUER)).thenReturn(claimsBuilder);
                when(claimsBuilder.subject(TEST_USER_ID)).thenReturn(claimsBuilder);
                when(claimsBuilder.audience(EXPECTED_AUDIENCE)).thenReturn(claimsBuilder);
                when(claimsBuilder.expiresIn(any(Duration.class))).thenReturn(claimsBuilder);
                when(claimsBuilder.issuedAt(any(Instant.class))).thenReturn(claimsBuilder);
                when(claimsBuilder.groups(any())).thenReturn(claimsBuilder);
                when(claimsBuilder.claim(anyString(), any())).thenReturn(claimsBuilder);
                when(claimsBuilder.sign()).thenReturn(DUMMY_SIGNED_JWT);

                // Execute access token generation
                final String generatedToken = jwtTokenProvider.generateAccessToken(TEST_USER_ID, TEST_ROLE_STUDENT, TEST_PROVIDER_LOCAL);

                // Assert token structure and signing output
                assertNotNull(generatedToken, "Generated compact JWT must not be null");
                assertEquals(DUMMY_SIGNED_JWT, generatedToken, "Token output must match signer result");

                // Verify specific business claims applied to claims builder
                verify(claimsBuilder, times(1)).issuer(EXPECTED_ISSUER);
                verify(claimsBuilder, times(1)).subject(TEST_USER_ID);
                verify(claimsBuilder, times(1)).audience(EXPECTED_AUDIENCE);
                verify(claimsBuilder, times(1)).expiresIn(Duration.ofSeconds(JwtTokenProvider.ACCESS_TOKEN_VALIDITY_SECONDS));
                verify(claimsBuilder, times(1)).claim(JwtTokenProvider.CLAIM_GROUP, TEST_ROLE_STUDENT);
                verify(claimsBuilder, times(1)).claim(JwtTokenProvider.CLAIM_PROVIDER, TEST_PROVIDER_LOCAL);
                verify(claimsBuilder, times(1)).claim(JwtTokenProvider.CLAIM_TYPE, JwtTokenProvider.TOKEN_TYPE_ACCESS);

                // Verify roles group mapping
                final ArgumentCaptor<Set<String>> groupsCaptor = ArgumentCaptor.forClass(Set.class);
                verify(claimsBuilder).groups(groupsCaptor.capture());
                final Set<String> capturedGroups = groupsCaptor.getValue();
                assertNotNull(capturedGroups, "Role groups set must be initialized");
                assertTrue(capturedGroups.contains(TEST_ROLE_STUDENT), "Role groups must contain assigned role");
            }

            logger.info("[TEST_EXIT] [{}] [ARC-006] Access token generation validated successfully.", SUBSYSTEM_NAME);
        }

        /**
         * Verifies that generating a refresh token yields an RS256 token valid for exactly 7 days
         * with empty authorization groups and designated 'refresh' token type discriminator.
         *
         * @verifies [ARC-006], [NFR-003]
         */
        @Test
        @DisplayName("Test 2: generateRefreshToken_returnsSevenDayToken - verifies 7-day lifespan and empty groups")
        public void generateRefreshToken_returnsSevenDayToken() {
            logger.info("[TEST_START] [{}] [ARC-006] Executing refresh token generation assertion...", SUBSYSTEM_NAME);

            try (MockedStatic<Jwt> mockedJwt = Mockito.mockStatic(Jwt.class)) {
                final JwtClaimsBuilder claimsBuilder = mock(JwtClaimsBuilder.class);
                mockedJwt.when(Jwt::claims).thenReturn(claimsBuilder);

                when(claimsBuilder.issuer(EXPECTED_ISSUER)).thenReturn(claimsBuilder);
                when(claimsBuilder.subject(TEST_USER_ID)).thenReturn(claimsBuilder);
                when(claimsBuilder.audience(EXPECTED_AUDIENCE)).thenReturn(claimsBuilder);
                when(claimsBuilder.expiresIn(any(Duration.class))).thenReturn(claimsBuilder);
                when(claimsBuilder.issuedAt(any(Instant.class))).thenReturn(claimsBuilder);
                when(claimsBuilder.groups(Collections.emptySet())).thenReturn(claimsBuilder);
                when(claimsBuilder.claim(eq(JwtTokenProvider.CLAIM_TYPE), eq(JwtTokenProvider.TOKEN_TYPE_REFRESH))).thenReturn(claimsBuilder);
                when(claimsBuilder.sign()).thenReturn(DUMMY_SIGNED_JWT);

                // Execute refresh token generation
                final String refreshToken = jwtTokenProvider.generateRefreshToken(TEST_USER_ID);

                // Assertions on refresh token
                assertNotNull(refreshToken, "Refresh token must not be null");
                assertEquals(DUMMY_SIGNED_JWT, refreshToken, "Refresh token must match signed mock payload");

                // Verify refresh token temporal configuration (7 days)
                verify(claimsBuilder, times(1)).expiresIn(Duration.ofSeconds(JwtTokenProvider.REFRESH_TOKEN_VALIDITY_SECONDS));
                verify(claimsBuilder, times(1)).groups(Collections.emptySet());
                verify(claimsBuilder, times(1)).claim(JwtTokenProvider.CLAIM_TYPE, JwtTokenProvider.TOKEN_TYPE_REFRESH);
            }

            logger.info("[TEST_EXIT] [{}] [ARC-006] Refresh token generation validated successfully.", SUBSYSTEM_NAME);
        }

        /**
         * Verifies that validateToken returns true when provided with an unexpired token
         * featuring an authentic cryptographic signature, matching issuer, and valid subject.
         *
         * @verifies [ARC-006], [NFR-003]
         */
        @Test
        @DisplayName("Test 3: validateToken_acceptsValidToken - confirms validation pass for authentic active token")
        public void validateToken_acceptsValidToken() throws Exception {
            logger.info("[TEST_START] [{}] [NFR-003] Executing valid token acceptance assertion...", SUBSYSTEM_NAME);

            // Mock parsed JsonWebToken claims with future expiration
            final JsonWebToken mockJwt = mock(JsonWebToken.class);
            final long futureExpirationEpoch = Instant.now().plusSeconds(600).getEpochSecond();

            when(mockJwt.getSubject()).thenReturn(TEST_USER_ID);
            when(mockJwt.getExpirationTime()).thenReturn(futureExpirationEpoch);
            when(mockJwt.getIssuer()).thenReturn(EXPECTED_ISSUER);

            // Mock parser execution
            when(jwtParser.parse(eq(DUMMY_SIGNED_JWT), any(JWTAuthContextInfo.class))).thenReturn(mockJwt);

            // Execute validation
            final boolean isValid = jwtTokenProvider.validateToken(DUMMY_SIGNED_JWT);

            // Assert positive validation result
            assertTrue(isValid, "Token with authentic signature, correct issuer, and future expiration must be valid");
            verify(jwtParser, times(1)).parse(eq(DUMMY_SIGNED_JWT), any(JWTAuthContextInfo.class));

            logger.info("[TEST_EXIT] [{}] [NFR-003] Token validation passed successfully.", SUBSYSTEM_NAME);
        }

        /**
         * Verifies that getClaims successfully parses and decrypts a compact JWT string into a typed JsonWebToken.
         *
         * @verifies [ARC-006], [NFR-003]
         */
        @Test
        @DisplayName("Happy Case: getClaims - parses token into typed JsonWebToken claims context")
        public void getClaims_successfulParsing() throws Exception {
            logger.info("[TEST_START] [{}] [ARC-006] Testing claims extraction and parsing...", SUBSYSTEM_NAME);

            final JsonWebToken mockJwt = mock(JsonWebToken.class);
            when(mockJwt.getSubject()).thenReturn(TEST_USER_ID);
            when(mockJwt.getIssuer()).thenReturn(EXPECTED_ISSUER);
            when(jwtParser.parse(eq(DUMMY_SIGNED_JWT), any(JWTAuthContextInfo.class))).thenReturn(mockJwt);

            // Execute claim extraction
            final JsonWebToken resultJwt = jwtTokenProvider.getClaims(DUMMY_SIGNED_JWT);

            assertNotNull(resultJwt, "Parsed JsonWebToken context must not be null");
            assertEquals(TEST_USER_ID, resultJwt.getSubject(), "Subject claim must match subject ID");
            assertEquals(EXPECTED_ISSUER, resultJwt.getIssuer(), "Issuer claim must match configured issuer");

            logger.info("[TEST_EXIT] [{}] [ARC-006] Claims extraction successfully verified.", SUBSYSTEM_NAME);
        }
    }

    // =========================================================================
    // 2. EDGE CASES & BOUNDARY CONDITIONS
    // =========================================================================

    @Nested
    @DisplayName("Category 2: Edge Cases & Boundary Conditions")
    class EdgeCaseTests {

        /**
         * Verifies fallback behavior when configured issuer is null or empty, ensuring default issuer fallback.
         *
         * @verifies [ARC-006], [NFR-003]
         */
        @Test
        @DisplayName("Edge Case: generateAccessToken - falls back to default issuer and audience when unconfigured")
        public void generateAccessToken_fallbackToDefaultIssuerAndAudience() throws Exception {
            logger.info("[TEST_START] [{}] [ARC-006] Testing default issuer/audience fallback...", SUBSYSTEM_NAME);

            // Set empty issuer and audience configurations
            setField(jwtTokenProvider, "configuredIssuer", "");
            setField(jwtTokenProvider, "configuredAudience", Optional.empty());

            try (MockedStatic<Jwt> mockedJwt = Mockito.mockStatic(Jwt.class)) {
                final JwtClaimsBuilder claimsBuilder = mock(JwtClaimsBuilder.class);
                mockedJwt.when(Jwt::claims).thenReturn(claimsBuilder);

                when(claimsBuilder.issuer(JwtTokenProvider.DEFAULT_ISSUER)).thenReturn(claimsBuilder);
                when(claimsBuilder.subject(TEST_USER_ID)).thenReturn(claimsBuilder);
                when(claimsBuilder.audience(JwtTokenProvider.DEFAULT_AUDIENCE)).thenReturn(claimsBuilder);
                when(claimsBuilder.expiresIn(any(Duration.class))).thenReturn(claimsBuilder);
                when(claimsBuilder.issuedAt(any(Instant.class))).thenReturn(claimsBuilder);
                when(claimsBuilder.groups(any())).thenReturn(claimsBuilder);
                when(claimsBuilder.claim(anyString(), any())).thenReturn(claimsBuilder);
                when(claimsBuilder.sign()).thenReturn(DUMMY_SIGNED_JWT);

                // Execute token generation
                final String token = jwtTokenProvider.generateAccessToken(TEST_USER_ID, null, null);

                assertNotNull(token, "Token generated with defaults must not be null");
                verify(claimsBuilder).issuer(JwtTokenProvider.DEFAULT_ISSUER);
                verify(claimsBuilder).audience(JwtTokenProvider.DEFAULT_AUDIENCE);
                verify(claimsBuilder).claim(JwtTokenProvider.CLAIM_PROVIDER, "local");
                verify(claimsBuilder).claim(JwtTokenProvider.CLAIM_GROUP, "");
            }

            logger.info("[TEST_EXIT] [{}] [ARC-006] Fallback configuration verified.", SUBSYSTEM_NAME);
        }

        /**
         * Verifies that validateToken returns false when the token payload has whitespace or is blank.
         *
         * @verifies [NFR-003]
         */
        @Test
        @DisplayName("Edge Case: validateToken - returns false when token string is blank or whitespace")
        public void validateToken_returnsFalseForBlankToken() {
            logger.info("[TEST_START] [{}] [NFR-003] Testing validation against blank tokens...", SUBSYSTEM_NAME);

            assertFalse(jwtTokenProvider.validateToken(null), "Null token must evaluate to invalid (false)");
            assertFalse(jwtTokenProvider.validateToken(""), "Empty token must evaluate to invalid (false)");
            assertFalse(jwtTokenProvider.validateToken(BLANK_WHITESPACE), "Whitespace token must evaluate to invalid (false)");

            logger.info("[TEST_EXIT] [{}] [NFR-003] Blank token rejection verified.", SUBSYSTEM_NAME);
        }

        /**
         * Verifies that validateToken returns false when the parsed token has a missing or empty subject claim.
         *
         * @verifies [ARC-006], [NFR-003]
         */
        @Test
        @DisplayName("Edge Case: validateToken - rejects token with missing or empty subject")
        public void validateToken_rejectsTokenWithEmptySubject() throws Exception {
            logger.info("[TEST_START] [{}] [ARC-006] Testing token rejection for missing subject...", SUBSYSTEM_NAME);

            final JsonWebToken mockJwt = mock(JsonWebToken.class);
            when(mockJwt.getSubject()).thenReturn(BLANK_WHITESPACE);
            when(jwtParser.parse(eq(DUMMY_SIGNED_JWT), any(JWTAuthContextInfo.class))).thenReturn(mockJwt);

            final boolean isValid = jwtTokenProvider.validateToken(DUMMY_SIGNED_JWT);
            assertFalse(isValid, "Token with blank subject claim must be rejected as invalid");

            logger.info("[TEST_EXIT] [{}] [ARC-006] Missing subject token rejection verified.", SUBSYSTEM_NAME);
        }

        /**
         * Verifies that validateToken returns false when the token issuer does not match the configured expectation.
         *
         * @verifies [ARC-006], [NFR-003]
         */
        @Test
        @DisplayName("Edge Case: validateToken - rejects token with issuer mismatch")
        public void validateToken_rejectsTokenWithIssuerMismatch() throws Exception {
            logger.info("[TEST_START] [{}] [NFR-003] Testing token rejection for issuer mismatch...", SUBSYSTEM_NAME);

            final JsonWebToken mockJwt = mock(JsonWebToken.class);
            final long futureExp = Instant.now().plusSeconds(600).getEpochSecond();

            when(mockJwt.getSubject()).thenReturn(TEST_USER_ID);
            when(mockJwt.getExpirationTime()).thenReturn(futureExp);
            when(mockJwt.getIssuer()).thenReturn("untrusted-rogue-issuer");
            when(jwtParser.parse(eq(DUMMY_SIGNED_JWT), any(JWTAuthContextInfo.class))).thenReturn(mockJwt);

            final boolean isValid = jwtTokenProvider.validateToken(DUMMY_SIGNED_JWT);
            assertFalse(isValid, "Token with unapproved issuer must be rejected");

            logger.info("[TEST_EXIT] [{}] [NFR-003] Rogue issuer token rejection verified.", SUBSYSTEM_NAME);
        }
    }

    // =========================================================================
    // 3. EXCEPTION CASES & NEGATIVE PATHS (ATTACK & FAILURE MODES)
    // =========================================================================

    @Nested
    @DisplayName("Category 3: Exception Cases & Negative Security Paths")
    class ExceptionAndNegativeTests {

        /**
         * Verifies that validateToken returns false when an expired token is supplied.
         *
         * @verifies [ARC-006], [NFR-003]
         */
        @Test
        @DisplayName("Test 4: validateToken_rejectsExpiredToken - rejects expired tokens past current epoch")
        public void validateToken_rejectsExpiredToken() throws Exception {
            logger.info("[TEST_START] [{}] [ARC-006] Executing expired token rejection assertion...", SUBSYSTEM_NAME);

            final JsonWebToken mockJwt = mock(JsonWebToken.class);
            // Expired 60 seconds in the past
            final long pastExpirationEpoch = Instant.now().minusSeconds(60).getEpochSecond();

            when(mockJwt.getSubject()).thenReturn(TEST_USER_ID);
            when(mockJwt.getExpirationTime()).thenReturn(pastExpirationEpoch);
            when(jwtParser.parse(eq(DUMMY_SIGNED_JWT), any(JWTAuthContextInfo.class))).thenReturn(mockJwt);

            // Execute validation on expired token
            final boolean isValid = jwtTokenProvider.validateToken(DUMMY_SIGNED_JWT);

            // Assert validation fails cleanly without unhandled exceptions
            assertFalse(isValid, "Token expired in the past must evaluate to invalid (false)");

            logger.info("[TEST_EXIT] [{}] [ARC-006] Expired token rejection verified.", SUBSYSTEM_NAME);
        }

        /**
         * Verifies that validateToken returns false and gracefully isolates failures when
         * the JWT Parser throws a ParseException due to signature mismatch or malformed structure.
         *
         * @verifies [ARC-006], [NFR-003]
         */
        @Test
        @DisplayName("Test 5: validateToken_rejectsInvalidSignature - rejects tokens failing signature verification")
        public void validateToken_rejectsInvalidSignature() throws Exception {
            logger.info("[TEST_START] [{}] [NFR-003] Executing invalid signature rejection assertion...", SUBSYSTEM_NAME);

            // Simulate SmallRye parser cryptographic signature verification failure
            doThrow(new ParseException("Cryptographic RS256 signature verification failed"))
                    .when(jwtParser).parse(eq(MALFORMED_JWT), any(JWTAuthContextInfo.class));

            // Execute validation
            final boolean isValid = jwtTokenProvider.validateToken(MALFORMED_JWT);

            // Assert rejection
            assertFalse(isValid, "Token failing cryptographic signature verification must return false");

            logger.info("[TEST_EXIT] [{}] [NFR-003] Invalid signature rejection verified.", SUBSYSTEM_NAME);
        }

        /**
         * Verifies that generateAccessToken throws IllegalArgumentException when given a null or blank userId.
         *
         * @verifies [REQ-001], [ARC-006]
         */
        @Test
        @DisplayName("Negative Case: generateAccessToken - throws IllegalArgumentException for blank userId")
        public void generateAccessToken_throwsOnBlankUserId() {
            logger.info("[TEST_START] [{}] [REQ-001] Testing validation failure for null/blank userId...", SUBSYSTEM_NAME);

            assertThrows(IllegalArgumentException.class, () -> {
                jwtTokenProvider.generateAccessToken(null, TEST_ROLE_STUDENT, TEST_PROVIDER_LOCAL);
            }, "generateAccessToken must throw IllegalArgumentException when userId is null");

            assertThrows(IllegalArgumentException.class, () -> {
                jwtTokenProvider.generateAccessToken(BLANK_WHITESPACE, TEST_ROLE_STUDENT, TEST_PROVIDER_LOCAL);
            }, "generateAccessToken must throw IllegalArgumentException when userId is blank");

            logger.info("[TEST_EXIT] [{}] [REQ-001] Blank userId rejection verified.", SUBSYSTEM_NAME);
        }

        /**
         * Verifies that generateRefreshToken throws IllegalArgumentException when given a null or blank userId.
         *
         * @verifies [ARC-006]
         */
        @Test
        @DisplayName("Negative Case: generateRefreshToken - throws IllegalArgumentException for blank userId")
        public void generateRefreshToken_throwsOnBlankUserId() {
            logger.info("[TEST_START] [{}] [ARC-006] Testing refresh token generation failure for null/blank userId...", SUBSYSTEM_NAME);

            assertThrows(IllegalArgumentException.class, () -> {
                jwtTokenProvider.generateRefreshToken(null);
            }, "generateRefreshToken must throw IllegalArgumentException when userId is null");

            assertThrows(IllegalArgumentException.class, () -> {
                jwtTokenProvider.generateRefreshToken(BLANK_WHITESPACE);
            }, "generateRefreshToken must throw IllegalArgumentException when userId is blank");

            logger.info("[TEST_EXIT] [{}] [ARC-006] Blank refresh userId rejection verified.", SUBSYSTEM_NAME);
        }

        /**
         * Verifies that getClaims throws IllegalArgumentException when passed a null or empty token string.
         *
         * @verifies [ARC-006], [NFR-003]
         */
        @Test
        @DisplayName("Negative Case: getClaims - throws IllegalArgumentException for null or empty input")
        public void getClaims_throwsOnNullOrEmptyToken() {
            logger.info("[TEST_START] [{}] [NFR-003] Testing getClaims boundary validation...", SUBSYSTEM_NAME);

            assertThrows(IllegalArgumentException.class, () -> {
                jwtTokenProvider.getClaims(null);
            }, "getClaims must throw IllegalArgumentException for null token");

            assertThrows(IllegalArgumentException.class, () -> {
                jwtTokenProvider.getClaims(BLANK_WHITESPACE);
            }, "getClaims must throw IllegalArgumentException for blank token");

            logger.info("[TEST_EXIT] [{}] [NFR-003] getClaims input validation verified.", SUBSYSTEM_NAME);
        }

        /**
         * Verifies that getClaims wraps SmallRye ParseException into a SecurityException preserving cause chain.
         *
         * @verifies [ARC-006], [NFR-003]
         */
        @Test
        @DisplayName("Negative Case: getClaims - rethrows SecurityException on ParseException preserving cause")
        public void getClaims_wrapsParseExceptionIntoSecurityException() throws Exception {
            logger.info("[TEST_START] [{}] [ARC-006] Testing exception cause chain preservation on parse failure...", SUBSYSTEM_NAME);

            final ParseException parseException = new ParseException("Invalid signature or structure");
            doThrow(parseException).when(jwtParser).parse(eq(MALFORMED_JWT), any(JWTAuthContextInfo.class));

            final SecurityException thrown = assertThrows(SecurityException.class, () -> {
                jwtTokenProvider.getClaims(MALFORMED_JWT);
            }, "getClaims must wrap ParseException into SecurityException");

            assertNotNull(thrown.getCause(), "Root cause exception must be preserved");
            assertEquals(parseException, thrown.getCause(), "Cause must be the original ParseException instance");

            logger.info("[TEST_EXIT] [{}] [ARC-006] Exception wrapping and cause chain preservation verified.", SUBSYSTEM_NAME);
        }

        /**
         * Verifies that initializeProvider throws SecurityException if the configured public key file cannot be found.
         *
         * @verifies [NFR-003]
         */
        @Test
        @DisplayName("Negative Case: initializeProvider - throws SecurityException when key location is invalid")
        public void initializeProvider_throwsWhenKeyResourceMissing() throws Exception {
            logger.info("[TEST_START] [{}] [NFR-003] Testing provider startup initialization failure...", SUBSYSTEM_NAME);

            setField(jwtTokenProvider, "publicKeyLocation", "non_existent_key_file.pem");

            assertThrows(SecurityException.class, () -> {
                jwtTokenProvider.initializeProvider();
            }, "initializeProvider must throw SecurityException when cryptographic keys are missing");

            logger.info("[TEST_EXIT] [{}] [NFR-003] Key initialization failure verified.", SUBSYSTEM_NAME);
        }

        /**
         * Verifies that generateAccessToken wraps unexpected signing errors into SecurityException preserving cause.
         *
         * @verifies [ARC-006]
         */
        @Test
        @DisplayName("Negative Case: generateAccessToken - wraps unexpected signing failure into SecurityException")
        public void generateAccessToken_wrapsSigningFailure() {
            logger.info("[TEST_START] [{}] [ARC-006] Testing token signing failure encapsulation...", SUBSYSTEM_NAME);

            try (MockedStatic<Jwt> mockedJwt = Mockito.mockStatic(Jwt.class)) {
                mockedJwt.when(Jwt::claims).thenThrow(new RuntimeException("Cryptographic private key access denied"));

                final SecurityException thrown = assertThrows(SecurityException.class, () -> {
                    jwtTokenProvider.generateAccessToken(TEST_USER_ID, TEST_ROLE_STUDENT, TEST_PROVIDER_LOCAL);
                }, "generateAccessToken must throw SecurityException on signing failure");

                assertNotNull(thrown.getCause(), "Ancestral exception cause must be preserved in wrapper");
            }

            logger.info("[TEST_EXIT] [{}] [ARC-006] Token signing error handling verified.", SUBSYSTEM_NAME);
        }
    }
}
```

# Day 3: model models/gemini-flash-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/SocialAuthProviderRegistry.java
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/security/SocialAuthProviderRegistryTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Test Component Destination Path: `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/security/SocialAuthProviderRegistryTest.java` (Must map to sources/backend/ or sources/frontend/)




### 📁 TARGET SOURCE IMPLEMENTATION CONTEXT (VERIFICATION TARGET)
Analyze the core logical operations within this implementation code block to construct your isolated unit assertions:
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


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Tạo tệp kiểm thử đơn vị ./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/security/SocialAuthProviderRegistryTest.java sử dụng JUnit 5 kết hợp Mockito 5.7.0 (@InjectMock). Mock 3 provider FirebaseAuthProvider, GoogleAuthProvider, FacebookAuthProvider trả về SocialUserInfo giả lập. Test case 1: authenticate_withFirebase_returnsUserInfo xác minh gọi provider đúng tên. Test case 2: authenticate_withGoogle_returnsUserInfo xác minh luồng Google. Test case 3: authenticate_withFacebook_returnsUserInfo xác minh luồng Facebook. Test case 4: authenticate_withUnknownProvider_throwsException xác minh ném UnsupportedProviderException khi tên provider không hợp lệ. Test case 5: authenticate_withInvalidToken_throwsException xác minh ném InvalidTokenException khi token không hợp lệ.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` structure. Ensure that you read the exact Tag IDs from the `['[ARC-006]', '[REQ-002]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.membershiphub.userservice.security;

import jakarta.enterprise.inject.Instance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nlh4j.membershiphub.userservice.security.SocialAuthProviderRegistry.FacebookAuthProvider;
import org.nlh4j.membershiphub.userservice.security.SocialAuthProviderRegistry.FirebaseAuthProvider;
import org.nlh4j.membershiphub.userservice.security.SocialAuthProviderRegistry.GoogleAuthProvider;
import org.nlh4j.membershiphub.userservice.security.SocialAuthProviderRegistry.SocialAuthProvider;
import org.nlh4j.membershiphub.userservice.security.SocialAuthProviderRegistry.SocialUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Enterprise Unit Test Suite for {@link SocialAuthProviderRegistry}.
 * Validates federated identity authentication routing, CDI provider lifecycle initialization,
 * boundary sanitization, and upstream failure resilience across Firebase, Google, and Facebook integrations.
 *
 * @author Enterprise QA Automation Team
 * @version 1.0.0
 * @verifies [ARC-006], [REQ-002]
 */
@ExtendWith(MockitoExtension.class)
public class SocialAuthProviderRegistryTest {

    // =========================================================================
    // TOP-OF-CLASS IMMUTABLE CONSTANTS DECLARATION [0.2]
    // =========================================================================
    public static final String MODULE_NAME = "USER-SERVICE-SECURITY-TEST";
    public static final String TRACE_TAG_ARC_006 = "[ARC-006]";
    public static final String TRACE_TAG_REQ_002 = "[REQ-002]";

    public static final String TEST_PROVIDER_FIREBASE = "firebase";
    public static final String TEST_PROVIDER_GOOGLE = "google";
    public static final String TEST_PROVIDER_FACEBOOK = "facebook";
    public static final String TEST_PROVIDER_UNSUPPORTED = "twitter";
    public static final String TEST_PROVIDER_UPPERCASE_GOOGLE = "GOOGLE";
    public static final String TEST_PROVIDER_SPACED_FIREBASE = "  firebase  ";

    public static final String TEST_RAW_TOKEN_VALID = "sample-valid-jwt-identity-token-payload-xyz";
    public static final String TEST_RAW_TOKEN_INVALID = "sample-malformed-or-expired-token";

    public static final String MOCK_EMAIL_FIREBASE = "firebase.user@membershiphub.org";
    public static final String MOCK_NAME_FIREBASE = "Firebase Student User";
    public static final String MOCK_SUB_FIREBASE = "fb-uid-9988776655";
    public static final String MOCK_PICTURE_FIREBASE = "https://cdn.membershiphub.org/photos/fb-uid-9988776655.jpg";

    public static final String MOCK_EMAIL_GOOGLE = "google.teacher@membershiphub.org";
    public static final String MOCK_NAME_GOOGLE = "Google Faculty Member";
    public static final String MOCK_SUB_GOOGLE = "google-sub-1122334455";
    public static final String MOCK_PICTURE_GOOGLE = "https://lh3.googleusercontent.com/a/google-sub-1122334455";

    public static final String MOCK_EMAIL_FACEBOOK = "facebook.admin@membershiphub.org";
    public static final String MOCK_NAME_FACEBOOK = "Facebook Center Admin";
    public static final String MOCK_SUB_FACEBOOK = "fb-graph-id-4455667788";
    public static final String MOCK_PICTURE_FACEBOOK = "https://graph.facebook.com/v18.0/fb-graph-id-4455667788/picture";

    public static final String LOG_TEST_START_PREFIX = "[TEST_START]";
    public static final String LOG_TEST_END_PREFIX = "[TEST_END]";
    public static final String LOG_TEST_EXEC_PREFIX = "[TEST_EXEC]";

    private static final Logger LOGGER = LoggerFactory.getLogger(SocialAuthProviderRegistryTest.class);

    // =========================================================================
    // MOCKED CDI DEPENDENCIES & SUT INJECTION
    // =========================================================================
    @Mock
    private Instance<SocialAuthProvider> providerInstancesMock;

    @Mock
    private FirebaseAuthProvider firebaseAuthProviderMock;

    @Mock
    private GoogleAuthProvider googleAuthProviderMock;

    @Mock
    private FacebookAuthProvider facebookAuthProviderMock;

    @InjectMocks
    private SocialAuthProviderRegistry registryUnderTest;

    // =========================================================================
    // TEST FIXTURE SETUP
    // =========================================================================

    /**
     * Initializes mocked CDI instances and populates the registry under test.
     * [ARC-006] Pre-configures standard provider identity mocks.
     */
    @BeforeEach
    public void setUp() {
        LOGGER.debug("{} {} Configuring isolated mock context for SocialAuthProviderRegistry.",
                LOG_TEST_EXEC_PREFIX, TRACE_TAG_ARC_006);

        // Configure default names for the mocked identity providers
        lenient().when(firebaseAuthProviderMock.getName()).thenReturn(TEST_PROVIDER_FIREBASE);
        lenient().when(googleAuthProviderMock.getName()).thenReturn(TEST_PROVIDER_GOOGLE);
        lenient().when(facebookAuthProviderMock.getName()).thenReturn(TEST_PROVIDER_FACEBOOK);

        // Configure CDI Instance iterator to supply the 3 enterprise social providers
        List<SocialAuthProvider> providerList = Arrays.asList(
                firebaseAuthProviderMock,
                googleAuthProviderMock,
                facebookAuthProviderMock
        );

        lenient().when(providerInstancesMock.iterator()).thenAnswer(invocation -> providerList.iterator());

        // Invoke PostConstruct registry lifecycle hook
        registryUnderTest.initRegistry();
    }

    // =========================================================================
    // NESTED SUITE: HAPPY PATH SCENARIOS (CATEGORY 1)
    // =========================================================================
    @Nested
    @DisplayName("Happy Path Federated Authentication Tests")
    class HappyPathTests {

        /**
         * Verifies successful federated identity verification using Firebase authentication provider.
         *
         * @verifies [REQ-002], [ARC-006]
         */
        @Test
        @DisplayName("Test Case 1: authenticate_withFirebase_returnsUserInfo - Verify Firebase delegation")
        public void authenticate_withFirebase_returnsUserInfo() {
            // [REQ-002] Entry log tracking test process compliance
            LOGGER.info("{} {} Starting Test Case 1: authenticate_withFirebase_returnsUserInfo",
                    LOG_TEST_START_PREFIX, TRACE_TAG_REQ_002);

            // GIVEN: A valid Firebase user profile returned by the verified downstream provider
            SocialUserInfo expectedUserInfo = new SocialUserInfo(
                    MOCK_EMAIL_FIREBASE,
                    MOCK_NAME_FIREBASE,
                    MOCK_SUB_FIREBASE,
                    MOCK_PICTURE_FIREBASE
            );
            when(firebaseAuthProviderMock.verifyToken(TEST_RAW_TOKEN_VALID)).thenReturn(expectedUserInfo);

            // WHEN: Registry processes authentication request targeting 'firebase'
            SocialUserInfo actualUserInfo = registryUnderTest.authenticate(TEST_PROVIDER_FIREBASE, TEST_RAW_TOKEN_VALID);

            // THEN: Assertions verify complete model mapping and delegation correctness
            assertNotNull(actualUserInfo, "Returned SocialUserInfo payload must not be null.");
            assertEquals(MOCK_EMAIL_FIREBASE, actualUserInfo.getEmail(), "Verified email must match Firebase profile.");
            assertEquals(MOCK_NAME_FIREBASE, actualUserInfo.getFullName(), "Verified full name must match Firebase profile.");
            assertEquals(MOCK_SUB_FIREBASE, actualUserInfo.getProviderId(), "Verified providerId must match Firebase UID.");
            assertEquals(MOCK_PICTURE_FIREBASE, actualUserInfo.getProfilePictureUrl(), "Verified avatar URL must match Firebase profile.");

            // Verify isolated execution: only Firebase provider was called, others untouched
            verify(firebaseAuthProviderMock, times(1)).verifyToken(TEST_RAW_TOKEN_VALID);
            verify(googleAuthProviderMock, never()).verifyToken(anyString());
            verify(facebookAuthProviderMock, never()).verifyToken(anyString());

            LOGGER.info("{} {} Completed Test Case 1 successfully.", LOG_TEST_END_PREFIX, TRACE_TAG_REQ_002);
        }

        /**
         * Verifies successful federated identity verification using Google OAuth2 provider.
         *
         * @verifies [REQ-002], [ARC-006]
         */
        @Test
        @DisplayName("Test Case 2: authenticate_withGoogle_returnsUserInfo - Verify Google OAuth2 delegation")
        public void authenticate_withGoogle_returnsUserInfo() {
            // [REQ-002] Process boundary log
            LOGGER.info("{} {} Starting Test Case 2: authenticate_withGoogle_returnsUserInfo",
                    LOG_TEST_START_PREFIX, TRACE_TAG_REQ_002);

            // GIVEN: A valid Google user profile returned by Google verification provider
            SocialUserInfo expectedUserInfo = new SocialUserInfo(
                    MOCK_EMAIL_GOOGLE,
                    MOCK_NAME_GOOGLE,
                    MOCK_SUB_GOOGLE,
                    MOCK_PICTURE_GOOGLE
            );
            when(googleAuthProviderMock.verifyToken(TEST_RAW_TOKEN_VALID)).thenReturn(expectedUserInfo);

            // WHEN: Registry processes authentication request targeting 'google'
            SocialUserInfo actualUserInfo = registryUnderTest.authenticate(TEST_PROVIDER_GOOGLE, TEST_RAW_TOKEN_VALID);

            // THEN: Assert model invariants
            assertNotNull(actualUserInfo, "Returned SocialUserInfo payload must not be null.");
            assertEquals(MOCK_EMAIL_GOOGLE, actualUserInfo.getEmail(), "Verified email must match Google profile.");
            assertEquals(MOCK_NAME_GOOGLE, actualUserInfo.getFullName(), "Verified full name must match Google profile.");
            assertEquals(MOCK_SUB_GOOGLE, actualUserInfo.getProviderId(), "Verified providerId must match Google subject sub.");
            assertEquals(MOCK_PICTURE_GOOGLE, actualUserInfo.getProfilePictureUrl(), "Verified photo URL must match Google picture claim.");

            // Verify strict provider delegation boundary
            verify(googleAuthProviderMock, times(1)).verifyToken(TEST_RAW_TOKEN_VALID);
            verify(firebaseAuthProviderMock, never()).verifyToken(anyString());
            verify(facebookAuthProviderMock, never()).verifyToken(anyString());

            LOGGER.info("{} {} Completed Test Case 2 successfully.", LOG_TEST_END_PREFIX, TRACE_TAG_REQ_002);
        }

        /**
         * Verifies successful federated identity verification using Facebook Graph API provider.
         *
         * @verifies [REQ-002], [ARC-006]
         */
        @Test
        @DisplayName("Test Case 3: authenticate_withFacebook_returnsUserInfo - Verify Facebook Graph delegation")
        public void authenticate_withFacebook_returnsUserInfo() {
            // [REQ-002] Process boundary log
            LOGGER.info("{} {} Starting Test Case 3: authenticate_withFacebook_returnsUserInfo",
                    LOG_TEST_START_PREFIX, TRACE_TAG_REQ_002);

            // GIVEN: A valid Facebook user profile returned by Facebook Graph provider
            SocialUserInfo expectedUserInfo = new SocialUserInfo(
                    MOCK_EMAIL_FACEBOOK,
                    MOCK_NAME_FACEBOOK,
                    MOCK_SUB_FACEBOOK,
                    MOCK_PICTURE_FACEBOOK
            );
            when(facebookAuthProviderMock.verifyToken(TEST_RAW_TOKEN_VALID)).thenReturn(expectedUserInfo);

            // WHEN: Registry processes authentication request targeting 'facebook'
            SocialUserInfo actualUserInfo = registryUnderTest.authenticate(TEST_PROVIDER_FACEBOOK, TEST_RAW_TOKEN_VALID);

            // THEN: Assert model invariants
            assertNotNull(actualUserInfo, "Returned SocialUserInfo payload must not be null.");
            assertEquals(MOCK_EMAIL_FACEBOOK, actualUserInfo.getEmail(), "Verified email must match Facebook profile.");
            assertEquals(MOCK_NAME_FACEBOOK, actualUserInfo.getFullName(), "Verified full name must match Facebook profile.");
            assertEquals(MOCK_SUB_FACEBOOK, actualUserInfo.getProviderId(), "Verified providerId must match Facebook ID.");
            assertEquals(MOCK_PICTURE_FACEBOOK, actualUserInfo.getProfilePictureUrl(), "Verified photo URL must match Facebook picture.");

            // Verify strict provider delegation boundary
            verify(facebookAuthProviderMock, times(1)).verifyToken(TEST_RAW_TOKEN_VALID);
            verify(firebaseAuthProviderMock, never()).verifyToken(anyString());
            verify(googleAuthProviderMock, never()).verifyToken(anyString());

            LOGGER.info("{} {} Completed Test Case 3 successfully.", LOG_TEST_END_PREFIX, TRACE_TAG_REQ_002);
        }

        /**
         * Verifies registry snapshot retrieval returns an unmodifiable, complete registry map.
         *
         * @verifies [ARC-006]
         */
        @Test
        @DisplayName("Verify getRegisteredProviders returns complete unmodifiable map")
        public void getRegisteredProviders_returnsUnmodifiableMapSnapshot() {
            // [ARC-006] Audit check for registered providers
            LOGGER.info("{} {} Validating registry snapshot integrity.", LOG_TEST_START_PREFIX, TRACE_TAG_ARC_006);

            Map<String, SocialAuthProvider> registeredProviders = registryUnderTest.getRegisteredProviders();

            assertNotNull(registeredProviders, "Registered providers map must not be null.");
            assertEquals(3, registeredProviders.size(), "Registry must contain exactly 3 social auth providers.");
            assertTrue(registeredProviders.containsKey(TEST_PROVIDER_FIREBASE), "Registry must include Firebase.");
            assertTrue(registeredProviders.containsKey(TEST_PROVIDER_GOOGLE), "Registry must include Google.");
            assertTrue(registeredProviders.containsKey(TEST_PROVIDER_FACEBOOK), "Registry must include Facebook.");

            // Assert that modifications to returned map are disallowed (immutable snapshot)
            assertThrows(UnsupportedOperationException.class, () ->
                    registeredProviders.put("unauthorized_provider", mock(SocialAuthProvider.class)),
                    "Attempting to modify registry snapshot map directly must throw UnsupportedOperationException.");

            LOGGER.info("{} {} Registry snapshot integrity verified.", LOG_TEST_END_PREFIX, TRACE_TAG_ARC_006);
        }
    }

    // =========================================================================
    // NESTED SUITE: BOUNDARY & NORMALIZATION CONDITIONS (CATEGORY 2)
    // =========================================================================
    @Nested
    @DisplayName("Edge Cases & Normalization Boundary Tests")
    class EdgeAndBoundaryTests {

        /**
         * Verifies that provider names containing whitespace or mixed casing are properly normalized.
         *
         * @verifies [REQ-002], [ARC-006]
         */
        @Test
        @DisplayName("Verify provider key normalization handles uppercase and whitespace padding")
        public void authenticate_withCasedOrSpacedProviderName_normalizesCorrectly() {
            // [REQ-002] Process log
            LOGGER.info("{} {} Testing provider key case-insensitivity and trimming normalization.",
                    LOG_TEST_START_PREFIX, TRACE_TAG_REQ_002);

            SocialUserInfo googleProfile = new SocialUserInfo(MOCK_EMAIL_GOOGLE, MOCK_NAME_GOOGLE, MOCK_SUB_GOOGLE, MOCK_PICTURE_GOOGLE);
            when(googleAuthProviderMock.verifyToken(TEST_RAW_TOKEN_VALID)).thenReturn(googleProfile);

            SocialUserInfo firebaseProfile = new SocialUserInfo(MOCK_EMAIL_FIREBASE, MOCK_NAME_FIREBASE, MOCK_SUB_FIREBASE, MOCK_PICTURE_FIREBASE);
            when(firebaseAuthProviderMock.verifyToken(TEST_RAW_TOKEN_VALID)).thenReturn(firebaseProfile);

            // Test UPPERCASE parameter ("GOOGLE")
            SocialUserInfo actualGoogle = registryUnderTest.authenticate(TEST_PROVIDER_UPPERCASE_GOOGLE, TEST_RAW_TOKEN_VALID);
            assertNotNull(actualGoogle, "Uppercase provider lookup must resolve successfully.");
            assertEquals(MOCK_SUB_GOOGLE, actualGoogle.getProviderId());

            // Test Padded parameter ("  firebase  ")
            SocialUserInfo actualFirebase = registryUnderTest.authenticate(TEST_PROVIDER_SPACED_FIREBASE, TEST_RAW_TOKEN_VALID);
            assertNotNull(actualFirebase, "Trimmed provider lookup must resolve successfully.");
            assertEquals(MOCK_SUB_FIREBASE, actualFirebase.getProviderId());

            LOGGER.info("{} {} Provider name normalization verified.", LOG_TEST_END_PREFIX, TRACE_TAG_REQ_002);
        }

        /**
         * Verifies SocialUserInfo domain model equals, hashCode, and toString contracts.
         *
         * @verifies [REQ-002]
         */
        @Test
        @DisplayName("Verify SocialUserInfo domain POJO contracts and data masking")
        public void socialUserInfo_pojosContractsAndMaskingVerification() {
            // [REQ-002] POJO contracts audit
            LOGGER.info("{} {} Auditing SocialUserInfo POJO invariants.", LOG_TEST_START_PREFIX, TRACE_TAG_REQ_002);

            SocialUserInfo userA1 = new SocialUserInfo(MOCK_EMAIL_GOOGLE, MOCK_NAME_GOOGLE, MOCK_SUB_GOOGLE, MOCK_PICTURE_GOOGLE);
            SocialUserInfo userA2 = new SocialUserInfo(MOCK_EMAIL_GOOGLE, MOCK_NAME_GOOGLE, MOCK_SUB_GOOGLE, MOCK_PICTURE_GOOGLE);
            SocialUserInfo userB = new SocialUserInfo(MOCK_EMAIL_FIREBASE, MOCK_NAME_FIREBASE, MOCK_SUB_FIREBASE, MOCK_PICTURE_FIREBASE);

            // Equality & HashCode
            assertEquals(userA1, userA2, "Identical SocialUserInfo instances must evaluate as equal.");
            assertEquals(userA1.hashCode(), userA2.hashCode(), "Identical SocialUserInfo instances must yield identical hash codes.");
            assertNotEquals(userA1, userB, "Different SocialUserInfo instances must not evaluate as equal.");
            assertNotEquals(userA1, null, "SocialUserInfo must not equal null.");
            assertNotEquals(userA1, new Object(), "SocialUserInfo must not equal an unrelated type.");
            assertEquals(userA1, userA1, "SocialUserInfo must be reflexive.");

            // Getter/Setter verification
            SocialUserInfo mutableUser = new SocialUserInfo();
            mutableUser.setEmail(MOCK_EMAIL_FACEBOOK);
            mutableUser.setFullName(MOCK_NAME_FACEBOOK);
            mutableUser.setProviderId(MOCK_SUB_FACEBOOK);
            mutableUser.setProfilePictureUrl(MOCK_PICTURE_FACEBOOK);

            assertEquals(MOCK_EMAIL_FACEBOOK, mutableUser.getEmail());
            assertEquals(MOCK_NAME_FACEBOOK, mutableUser.getFullName());
            assertEquals(MOCK_SUB_FACEBOOK, mutableUser.getProviderId());
            assertEquals(MOCK_PICTURE_FACEBOOK, mutableUser.getProfilePictureUrl());

            // ToString data masking safety verification (prevents raw cleartext credential leaks)
            String stringOutput = userA1.toString();
            assertNotNull(stringOutput);
            assertTrue(stringOutput.contains("****"), "SocialUserInfo.toString() must mask sensitive provider claims.");
            assertFalse(stringOutput.contains(MOCK_SUB_GOOGLE), "Raw unmasked providerId must not be exposed in toString log.");

            LOGGER.info("{} {} POJO contracts verified successfully.", LOG_TEST_END_PREFIX, TRACE_TAG_REQ_002);
        }
    }

    // =========================================================================
    // NESTED SUITE: EXCEPTION & NEGATIVE PATHS (CATEGORY 3)
    // =========================================================================
    @Nested
    @DisplayName("Exception & Negative Path Tests")
    class ExceptionAndNegativeTests {

        /**
         * Verifies that requesting an unregistered or unsupported social provider throws IllegalArgumentException.
         *
         * @verifies [REQ-002], [ARC-006]
         */
        @Test
        @DisplayName("Test Case 4: authenticate_withUnknownProvider_throwsException - Verify unknown provider rejection")
        public void authenticate_withUnknownProvider_throwsException() {
            // [REQ-002] Negative path test log
            LOGGER.info("{} {} Starting Test Case 4: authenticate_withUnknownProvider_throwsException",
                    LOG_TEST_START_PREFIX, TRACE_TAG_REQ_002);

            // GIVEN: An unsupported provider name 'twitter'
            String unsupportedProvider = TEST_PROVIDER_UNSUPPORTED;

            // WHEN & THEN: Execute and assert IllegalArgumentException
            IllegalArgumentException thrown = assertThrows(
                    IllegalArgumentException.class,
                    () -> registryUnderTest.authenticate(unsupportedProvider, TEST_RAW_TOKEN_VALID),
                    "Invoking registry with unsupported provider must trigger IllegalArgumentException."
            );

            assertTrue(thrown.getMessage().contains(SocialAuthProviderRegistry.MSG_ERR_PROVIDER_UNSUPPORTED),
                    "Exception message must specify unsupported provider warning.");
            assertTrue(thrown.getMessage().contains(unsupportedProvider),
                    "Exception message must include the attempted provider label.");

            // Verify no upstream provider methods were called
            verify(firebaseAuthProviderMock, never()).verifyToken(anyString());
            verify(googleAuthProviderMock, never()).verifyToken(anyString());
            verify(facebookAuthProviderMock, never()).verifyToken(anyString());

            LOGGER.info("{} {} Completed Test Case 4 successfully.", LOG_TEST_END_PREFIX, TRACE_TAG_REQ_002);
        }

        /**
         * Verifies that invalid, rejected, or expired identity tokens trigger a SecurityException.
         *
         * @verifies [REQ-002], [ARC-006]
         */
        @Test
        @DisplayName("Test Case 5: authenticate_withInvalidToken_throwsException - Verify invalid token rejection")
        public void authenticate_withInvalidToken_throwsException() {
            // [REQ-002] Process log
            LOGGER.info("{} {} Starting Test Case 5: authenticate_withInvalidToken_throwsException",
                    LOG_TEST_START_PREFIX, TRACE_TAG_REQ_002);

            // GIVEN: Upstream provider rejects token with a SecurityException
            String securityErrorMessage = "Google ID Token signature expired or signature mismatch.";
            when(googleAuthProviderMock.verifyToken(TEST_RAW_TOKEN_INVALID))
                    .thenThrow(new SecurityException(securityErrorMessage));

            // WHEN & THEN: Execute and assert SecurityException propagation
            SecurityException thrown = assertThrows(
                    SecurityException.class,
                    () -> registryUnderTest.authenticate(TEST_PROVIDER_GOOGLE, TEST_RAW_TOKEN_INVALID),
                    "Invalid or expired social token must cause registry to throw SecurityException."
            );

            assertEquals(securityErrorMessage, thrown.getMessage(),
                    "Preserved SecurityException message must match root cause error description.");

            LOGGER.info("{} {} Completed Test Case 5 successfully.", LOG_TEST_END_PREFIX, TRACE_TAG_REQ_002);
        }

        /**
         * Verifies that unexpected runtime exceptions from providers are wrapped securely into SecurityException.
         *
         * @verifies [REQ-002], [ARC-006]
         */
        @Test
        @DisplayName("Verify provider runtime network failures are wrapped in SecurityException")
        public void authenticate_whenProviderThrowsUnexpectedException_wrapsInSecurityException() {
            // [REQ-002] Exception wrapping verification
            LOGGER.info("{} {} Testing exception wrapping for unexpected upstream runtime errors.",
                    LOG_TEST_START_PREFIX, TRACE_TAG_REQ_002);

            when(facebookAuthProviderMock.verifyToken(anyString()))
                    .thenThrow(new RuntimeException("Connection reset by peer at graph.facebook.com"));

            SecurityException thrown = assertThrows(
                    SecurityException.class,
                    () -> registryUnderTest.authenticate(TEST_PROVIDER_FACEBOOK, TEST_RAW_TOKEN_VALID),
                    "Unchecked provider exceptions must be converted into SecurityException."
            );

            assertTrue(thrown.getMessage().contains(SocialAuthProviderRegistry.MSG_ERR_AUTH_EXEC),
                    "Exception message must include enterprise federated auth error constant.");
            assertNotNull(thrown.getCause(), "Original exception cause must be preserved in cause chain.");
            assertEquals("Connection reset by peer at graph.facebook.com", thrown.getCause().getMessage(),
                    "Root cause message must be preserved for audit diagnostics.");

            LOGGER.info("{} {} Exception wrapping verified.", LOG_TEST_END_PREFIX, TRACE_TAG_REQ_002);
        }

        /**
         * Verifies that passing null or blank provider names triggers an IllegalArgumentException.
         *
         * @param invalidProvider Input provider name under parameter test.
         * @verifies [REQ-002]
         */
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "
"})
        @DisplayName("Verify null or blank provider name throws IllegalArgumentException")
        public void authenticate_withNullOrBlankProvider_throwsIllegalArgumentException(String invalidProvider) {
            // [REQ-002] Parameter sanitization assertion
            LOGGER.info("{} {} Testing input validation for invalid provider string: '{}'",
                    LOG_TEST_START_PREFIX, TRACE_TAG_REQ_002, invalidProvider);

            IllegalArgumentException thrown = assertThrows(
                    IllegalArgumentException.class,
                    () -> registryUnderTest.authenticate(invalidProvider, TEST_RAW_TOKEN_VALID),
                    "Null or whitespace-only provider name must trigger IllegalArgumentException."
            );

            assertEquals(SocialAuthProviderRegistry.MSG_ERR_PROVIDER_NULL, thrown.getMessage(),
                    "Exception message must match standard MSG_ERR_PROVIDER_NULL constant.");
        }

        /**
         * Verifies that passing null or blank identity tokens triggers an IllegalArgumentException.
         *
         * @param invalidToken Input identity token under parameter test.
         * @verifies [REQ-002]
         */
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "
"})
        @DisplayName("Verify null or blank ID token throws IllegalArgumentException")
        public void authenticate_withNullOrBlankToken_throwsIllegalArgumentException(String invalidToken) {
            // [REQ-002] Parameter sanitization assertion
            LOGGER.info("{} {} Testing input validation for invalid ID token parameter.",
                    LOG_TEST_START_PREFIX, TRACE_TAG_REQ_002);

            IllegalArgumentException thrown = assertThrows(
                    IllegalArgumentException.class,
                    () -> registryUnderTest.authenticate(TEST_PROVIDER_GOOGLE, invalidToken),
                    "Null or whitespace-only token string must trigger IllegalArgumentException."
            );

            assertEquals(SocialAuthProviderRegistry.MSG_ERR_TOKEN_NULL, thrown.getMessage(),
                    "Exception message must match standard MSG_ERR_TOKEN_NULL constant.");
        }

        /**
         * Verifies that CDI provider discovery failures during PostConstruct initialization throw IllegalStateException.
         *
         * @verifies [ARC-006]
         */
        @Test
        @DisplayName("Verify CDI iterator failure in initRegistry throws IllegalStateException")
        public void initRegistry_whenProviderInstancesFails_throwsIllegalStateException() {
            // [ARC-006] Lifecycle failure mode testing
            LOGGER.info("{} {} Testing registry initialization failure handling.",
                    LOG_TEST_START_PREFIX, TRACE_TAG_ARC_006);

            SocialAuthProviderRegistry uninitializedRegistry = new SocialAuthProviderRegistry();
            @SuppressWarnings("unchecked")
            Instance<SocialAuthProvider> brokenInstanceMock = mock(Instance.class);
            when(brokenInstanceMock.iterator()).thenThrow(new RuntimeException("CDI context dependency resolution error."));

            uninitializedRegistry.providerInstances = brokenInstanceMock;

            IllegalStateException thrown = assertThrows(
                    IllegalStateException.class,
                    uninitializedRegistry::initRegistry,
                    "Failure in resolving CDI providers must result in IllegalStateException."
            );

            assertEquals(SocialAuthProviderRegistry.MSG_ERR_REGISTRY_INIT, thrown.getMessage(),
                    "Exception message must match MSG_ERR_REGISTRY_INIT constant.");
            assertNotNull(thrown.getCause(), "Root cause from CDI failure must be preserved.");

            LOGGER.info("{} {} Initialization failure mode verified.", LOG_TEST_END_PREFIX, TRACE_TAG_ARC_006);
        }
    }
}
```

