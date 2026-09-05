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