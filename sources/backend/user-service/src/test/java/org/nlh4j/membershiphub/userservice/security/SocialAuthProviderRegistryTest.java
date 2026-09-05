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