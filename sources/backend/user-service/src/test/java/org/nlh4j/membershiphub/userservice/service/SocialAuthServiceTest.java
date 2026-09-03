package org.nlh4j.membershiphub.userservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nlh4j.membershiphub.userservice.dto.AuthResponse;
import org.nlh4j.membershiphub.userservice.dto.SocialAuthRequest;
import org.nlh4j.membershiphub.userservice.entity.Role;
import org.nlh4j.membershiphub.userservice.entity.User;
import org.nlh4j.membershiphub.userservice.entity.UserSocialAccount;
import org.nlh4j.membershiphub.userservice.exception.InvalidTokenException;
import org.nlh4j.membershiphub.userservice.exception.SocialAuthProcessingException;
import org.nlh4j.membershiphub.userservice.exception.UnsupportedProviderException;
import org.nlh4j.membershiphub.userservice.model.SocialUserInfo;
import org.nlh4j.membershiphub.userservice.repository.RoleRepository;
import org.nlh4j.membershiphub.userservice.repository.UserRepository;
import org.nlh4j.membershiphub.userservice.repository.UserSocialAccountRepository;
import org.nlh4j.membershiphub.userservice.security.AuthAuditLogger;
import org.nlh4j.membershiphub.userservice.security.JwtTokenProvider;
import org.nlh4j.membershiphub.userservice.security.SocialTokenVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Isolated Unit Test Suite for {@link SocialAuthService}.
 * <p>
 * Evaluates third-party federated social authentication via OAuth2/OIDC, account linkage,
 * automatic Student role provisioning, profile synchronization, and JWT token issuance.
 * </p>
 *
 * @verifies [REQ-002], [ARC-006], [NFR-003], [EXC-004]
 */
@ExtendWith(MockitoExtension.class)
class SocialAuthServiceTest {

    // [REQ-002] Top-of-Class Constants Declaration Law
    private static final Logger LOGGER = LoggerFactory.getLogger(SocialAuthServiceTest.class);

    private static final String PROVIDER_GOOGLE = "google";
    private static final String PROVIDER_FACEBOOK = "facebook";
    private static final String PROVIDER_FIREBASE = "firebase";
    private static final String PROVIDER_UNSUPPORTED = "twitter";

    private static final String MOCK_GOOGLE_ID_TOKEN = "mock.google.id.token.jwt";
    private static final String MOCK_FACEBOOK_ACCESS_TOKEN = "mock.facebook.access.token.string";
    private static final String MOCK_EXPIRED_ID_TOKEN = "mock.expired.id.token";
    private static final String MOCK_MALFORMED_ID_TOKEN = "mock.malformed.id.token";

    private static final String MOCK_GOOGLE_USER_ID = "google-sub-123456789";
    private static final String MOCK_FACEBOOK_USER_ID = "facebook-uid-987654321";

    private static final String MOCK_USER_EMAIL = "student.tester@membershiphub.org";
    private static final String MOCK_USER_FULL_NAME = "Tester Nguyen";
    private static final String MOCK_GOOGLE_PICTURE_URL = "https://lh3.googleusercontent.com/a/mock-pic-google";
    private static final String MOCK_FACEBOOK_PICTURE_URL = "https://graph.facebook.com/v18.0/mock-pic-facebook";
    private static final String MOCK_CLIENT_OVERRIDE_PICTURE_URL = "https://cdn.membershiphub.org/avatar-override.png";

    private static final String MOCK_ACCESS_TOKEN = "mock.generated.access.token.jwt";
    private static final String MOCK_REFRESH_TOKEN = "mock.generated.refresh.token.jwt";
    private static final int EXPECTED_EXPIRES_IN_SECONDS = 900;
    private static final String DEFAULT_ROLE_NAME = "STUDENT";
    private static final short DEFAULT_ROLE_ID = 5;

    @Mock
    private SocialTokenVerifier socialTokenVerifier;

    @Mock
    private UserSocialAccountRepository userSocialAccountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthAuditLogger authAuditLogger;

    @InjectMocks
    private SocialAuthService socialAuthService;

    private Role defaultStudentRole;

    @BeforeEach
    void setUp() {
        // [ARC-006] Initialize baseline Student role fixture
        this.defaultStudentRole = new Role();
        this.defaultStudentRole.setRoleId(DEFAULT_ROLE_ID);
        this.defaultStudentRole.setName(DEFAULT_ROLE_NAME);
    }

    /**
     * Test Case 1: Happy Path - Brand new user registering via Google OAuth2.
     * <p>
     * Verifies that when a valid Google token is provided for an unregistered user:
     * 1. Token identity is verified via {@link SocialTokenVerifier}.
     * 2. A new {@link User} is provisioned with default role 'STUDENT'.
     * 3. A linked {@link UserSocialAccount} is persisted with the provider user ID.
     * 4. Session tokens are generated and returned in {@link AuthResponse} with isNewUser = true.
     * </p>
     *
     * @verifies [REQ-002], [ARC-006], [NFR-003]
     */
    @Test
    @DisplayName("[REQ-002] Happy Path: Register and authenticate brand new user via Google OAuth2")
    void authenticateWithGoogle_forNewUser_createsAccountAndReturnsJwt() {
        LOGGER.info("[TEST_START] [REQ-002] Executing authenticateWithGoogle_forNewUser_createsAccountAndReturnsJwt");

        // GIVEN: Incoming request with valid Google provider and ID token
        final SocialAuthRequest request = new SocialAuthRequest();
        request.setProvider(PROVIDER_GOOGLE);
        request.setIdToken(MOCK_GOOGLE_ID_TOKEN);
        request.setProfilePicture(MOCK_GOOGLE_PICTURE_URL);

        final SocialUserInfo verifiedUserInfo = new SocialUserInfo(
                MOCK_USER_EMAIL,
                MOCK_USER_FULL_NAME,
                MOCK_GOOGLE_USER_ID,
                MOCK_GOOGLE_PICTURE_URL
        );

        // Mock SocialTokenVerifier behavior
        when(this.socialTokenVerifier.verify(PROVIDER_GOOGLE, MOCK_GOOGLE_ID_TOKEN))
                .thenReturn(verifiedUserInfo);

        // Mock account lookup: no existing social account link
        when(this.userSocialAccountRepository.findByProviderAndProviderUserId(PROVIDER_GOOGLE, MOCK_GOOGLE_USER_ID))
                .thenReturn(Optional.empty());

        // Mock email lookup: no existing primary user with this email
        when(this.userRepository.findByEmail(MOCK_USER_EMAIL))
                .thenReturn(Optional.empty());

        // Mock role repository lookup for Student role
        when(this.roleRepository.findByIdOptional((long) DEFAULT_ROLE_ID))
                .thenReturn(Optional.of(this.defaultStudentRole));

        // Mock repository persistence
        doNothing().when(this.userRepository).persist(any(User.class));
        doNothing().when(this.userSocialAccountRepository).persist(any(UserSocialAccount.class));

        // Mock JWT Token Provider outputs
        when(this.jwtTokenProvider.generateAccessToken(anyString(), eq(DEFAULT_ROLE_NAME), eq(PROVIDER_GOOGLE)))
                .thenReturn(MOCK_ACCESS_TOKEN);
        when(this.jwtTokenProvider.generateRefreshToken(anyString()))
                .thenReturn(MOCK_REFRESH_TOKEN);

        // WHEN: Executing social authentication
        final AuthResponse response = this.socialAuthService.authenticateWithSocial(request);

        // THEN: Assert structured response contract compliance
        assertNotNull(response, "AuthResponse must not be null");
        assertAll("Verify New User Authentication Response Payload",
                () -> assertEquals(MOCK_ACCESS_TOKEN, response.getAccessToken(), "Access token must match generated mock"),
                () -> assertEquals(MOCK_REFRESH_TOKEN, response.getRefreshToken(), "Refresh token must match generated mock"),
                () -> assertEquals(EXPECTED_EXPIRES_IN_SECONDS, response.getExpiresIn(), "Token expiration must be exactly 900 seconds (15 mins)"),
                () -> assertEquals(DEFAULT_ROLE_NAME, response.getRole(), "Default role must be STUDENT"),
                () -> assertTrue(response.isNewUser(), "isNewUser flag must evaluate to true for first-time registration"),
                () -> assertNotNull(response.getUserId(), "Assigned User UUID must not be null")
        );

        // Capture and verify persisted User entity attributes
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(this.userRepository, times(1)).persist(userCaptor.capture());
        final User createdUser = userCaptor.getValue();

        assertEquals(MOCK_USER_EMAIL, createdUser.getEmail(), "Persisted email must match verified identity");
        assertEquals(MOCK_USER_FULL_NAME, createdUser.getFullName(), "Persisted full name must match verified identity");
        assertEquals(PROVIDER_GOOGLE, createdUser.getProvider(), "Persisted provider must be normalized to 'google'");
        assertEquals(DEFAULT_ROLE_NAME, createdUser.getRole().getName(), "Persisted user role must be STUDENT");

        // Capture and verify persisted UserSocialAccount entity attributes
        final ArgumentCaptor<UserSocialAccount> socialAccountCaptor = ArgumentCaptor.forClass(UserSocialAccount.class);
        verify(this.userSocialAccountRepository, times(1)).persist(socialAccountCaptor.capture());
        final UserSocialAccount createdAccount = socialAccountCaptor.getValue();

        assertEquals(PROVIDER_GOOGLE, createdAccount.getProvider(), "Social account provider must be 'google'");
        assertEquals(MOCK_GOOGLE_USER_ID, createdAccount.getProviderUserId(), "Social account provider user ID must match Google sub");
        assertEquals(createdUser, createdAccount.getUser(), "Social account must link directly to the newly provisioned user");

        // Verify audit logging triggers
        verify(this.authAuditLogger, times(1)).logAuthEvent(
                eq(createdUser.getUserId()),
                eq(SocialAuthService.AUDIT_ACTION_SOCIAL_USER_CREATED),
                anyString()
        );
        verify(this.authAuditLogger, times(1)).logAuthEvent(
                eq(createdUser.getUserId()),
                eq(SocialAuthService.AUDIT_ACTION_SOCIAL_LOGIN_SUCCESS),
                anyString()
        );

        LOGGER.info("[TEST_END] [REQ-002] authenticateWithGoogle_forNewUser_createsAccountAndReturnsJwt passed successfully");
    }

    /**
     * Test Case 2: Happy Path - Existing linked user logging in via Google OAuth2.
     * <p>
     * Verifies that when a returning user authenticates:
     * 1. Existing {@link UserSocialAccount} is located.
     * 2. No new primary {@link User} or linkage record is inserted into the database.
     * 3. Issued {@link AuthResponse} indicates isNewUser = false and reflects existing user role.
     * </p>
     *
     * @verifies [REQ-002], [ARC-006]
     */
    @Test
    @DisplayName("[REQ-002] Happy Path: Authenticate returning linked user via Google OAuth2")
    void authenticateWithGoogle_forExistingUser_returnsExistingJwt() {
        LOGGER.info("[TEST_START] [REQ-002] Executing authenticateWithGoogle_forExistingUser_returnsExistingJwt");

        // GIVEN: An existing user and linked social account in the system
        final UUID existingUserId = UUID.randomUUID();
        final User existingUser = new User();
        existingUser.setUserId(existingUserId);
        existingUser.setEmail(MOCK_USER_EMAIL);
        existingUser.setFullName(MOCK_USER_FULL_NAME);
        existingUser.setRole(this.defaultStudentRole);
        existingUser.setProvider(PROVIDER_GOOGLE);

        final UserSocialAccount existingSocialAccount = new UserSocialAccount();
        existingSocialAccount.setSocialAccountId(UUID.randomUUID());
        existingSocialAccount.setUser(existingUser);
        existingSocialAccount.setProvider(PROVIDER_GOOGLE);
        existingSocialAccount.setProviderUserId(MOCK_GOOGLE_USER_ID);
        existingSocialAccount.setProfilePictureUrl(MOCK_GOOGLE_PICTURE_URL);
        existingSocialAccount.setLinkedAt(LocalDateTime.now().minusDays(10));

        final SocialAuthRequest request = new SocialAuthRequest();
        request.setProvider(PROVIDER_GOOGLE);
        request.setIdToken(MOCK_GOOGLE_ID_TOKEN);

        final SocialUserInfo verifiedUserInfo = new SocialUserInfo(
                MOCK_USER_EMAIL,
                MOCK_USER_FULL_NAME,
                MOCK_GOOGLE_USER_ID,
                MOCK_GOOGLE_PICTURE_URL
        );

        when(this.socialTokenVerifier.verify(PROVIDER_GOOGLE, MOCK_GOOGLE_ID_TOKEN))
                .thenReturn(verifiedUserInfo);
        when(this.userSocialAccountRepository.findByProviderAndProviderUserId(PROVIDER_GOOGLE, MOCK_GOOGLE_USER_ID))
                .thenReturn(Optional.of(existingSocialAccount));

        when(this.jwtTokenProvider.generateAccessToken(existingUserId.toString(), DEFAULT_ROLE_NAME, PROVIDER_GOOGLE))
                .thenReturn(MOCK_ACCESS_TOKEN);
        when(this.jwtTokenProvider.generateRefreshToken(existingUserId.toString()))
                .thenReturn(MOCK_REFRESH_TOKEN);

        // WHEN: Authenticating existing linked user
        final AuthResponse response = this.socialAuthService.authenticateWithSocial(request);

        // THEN: Verify response metadata and state flags
        assertNotNull(response, "AuthResponse must not be null");
        assertAll("Verify Existing User Login Response",
                () -> assertEquals(MOCK_ACCESS_TOKEN, response.getAccessToken()),
                () -> assertEquals(MOCK_REFRESH_TOKEN, response.getRefreshToken()),
                () -> assertEquals(existingUserId, response.getUserId(), "User ID must match existing record"),
                () -> assertFalse(response.isNewUser(), "isNewUser must be false for existing account"),
                () -> assertEquals(DEFAULT_ROLE_NAME, response.getRole())
        );

        // Verify that no unnecessary insert operations were triggered
        verify(this.userRepository, never()).persist(any(User.class));
        verify(this.userRepository, never()).findByEmail(anyString());

        // Verify successful login audit event
        verify(this.authAuditLogger, times(1)).logAuthEvent(
                eq(existingUserId),
                eq(SocialAuthService.AUDIT_ACTION_SOCIAL_LOGIN_SUCCESS),
                anyString()
        );
        verify(this.authAuditLogger, never()).logAuthEvent(
                any(),
                eq(SocialAuthService.AUDIT_ACTION_SOCIAL_USER_CREATED),
                anyString()
        );

        LOGGER.info("[TEST_END] [REQ-002] authenticateWithGoogle_forExistingUser_returnsExistingJwt passed successfully");
    }

    /**
     * Test Case 3: Negative / Exception Path - Expired or structurally malformed third-party token.
     * <p>
     * Verifies that when {@link SocialTokenVerifier} rejects an expired or unverified token:
     * 1. {@link InvalidTokenException} is raised.
     * 2. Failed authentication audit log is emitted with target Tag ID tracking.
     * 3. No database mutations or JWT token generation takes place.
     * </p>
     *
     * @verifies [REQ-002], [EXC-004], [NFR-003]
     */
    @Test
    @DisplayName("[REQ-002][EXC-004] Negative Path: Expired or invalid third-party ID token triggers InvalidTokenException")
    void authenticateWithExpiredToken_throwsInvalidTokenException() {
        LOGGER.info("[TEST_START] [REQ-002] Executing authenticateWithExpiredToken_throwsInvalidTokenException");

        // GIVEN: Request containing an expired token
        final SocialAuthRequest request = new SocialAuthRequest();
        request.setProvider(PROVIDER_GOOGLE);
        request.setIdToken(MOCK_EXPIRED_ID_TOKEN);

        when(this.socialTokenVerifier.verify(PROVIDER_GOOGLE, MOCK_EXPIRED_ID_TOKEN))
                .thenThrow(new InvalidTokenException("OAuth2 ID Token has expired or signature verification failed."));

        // WHEN & THEN: Assert InvalidTokenException is thrown
        final InvalidTokenException exception = assertThrows(
                InvalidTokenException.class,
                () -> this.socialAuthService.authenticateWithSocial(request),
                "Expected InvalidTokenException when token is expired or invalid"
        );

        assertTrue(exception.getMessage().contains("expired or signature verification failed"),
                "Exception message must reflect token validation failure reason");

        // Verify audit log tracks authentication failure
        verify(this.authAuditLogger, times(1)).logAuthEvent(
                eq(null),
                eq(SocialAuthService.AUDIT_ACTION_SOCIAL_LOGIN_FAILED),
                anyString()
        );

        // Verify database and token issuance operations are completely bypassed
        verify(this.userSocialAccountRepository, never()).findByProviderAndProviderUserId(anyString(), anyString());
        verify(this.userRepository, never()).persist(any(User.class));
        verify(this.jwtTokenProvider, never()).generateAccessToken(anyString(), anyString(), anyString());

        LOGGER.info("[TEST_END] [REQ-002] authenticateWithExpiredToken_throwsInvalidTokenException passed successfully");
    }

    /**
     * Test Case 4: Negative / Exception Path - Unsupported external social identity provider.
     * <p>
     * Verifies that when an unauthorized provider (e.g. 'twitter') is requested:
     * 1. {@link UnsupportedProviderException} is raised by {@link SocialTokenVerifier}.
     * 2. Failed audit log event is recorded.
     * 3. Execution halts without database alteration.
     * </p>
     *
     * @verifies [REQ-002], [EXC-004]
     */
    @Test
    @DisplayName("[REQ-002][EXC-004] Negative Path: Unsupported provider request throws UnsupportedProviderException")
    void authenticateWithUnknownProvider_throwsUnsupportedProviderException() {
        LOGGER.info("[TEST_START] [REQ-002] Executing authenticateWithUnknownProvider_throwsUnsupportedProviderException");

        // GIVEN: Request specifying an unsupported provider
        final SocialAuthRequest request = new SocialAuthRequest();
        request.setProvider(PROVIDER_UNSUPPORTED);
        request.setIdToken(MOCK_MALFORMED_ID_TOKEN);

        when(this.socialTokenVerifier.verify(PROVIDER_UNSUPPORTED, MOCK_MALFORMED_ID_TOKEN))
                .thenThrow(new UnsupportedProviderException("Provider 'twitter' is not supported. Allowed: firebase, google, facebook."));

        // WHEN & THEN: Assert UnsupportedProviderException is thrown
        final UnsupportedProviderException exception = assertThrows(
                UnsupportedProviderException.class,
                () -> this.socialAuthService.authenticateWithSocial(request),
                "Expected UnsupportedProviderException for unsupported provider name"
        );

        assertTrue(exception.getMessage().contains("is not supported"),
                "Exception message must identify the unsupported provider");

        // Verify audit log registers the failed attempt
        verify(this.authAuditLogger, times(1)).logAuthEvent(
                eq(null),
                eq(SocialAuthService.AUDIT_ACTION_SOCIAL_LOGIN_FAILED),
                anyString()
        );

        // Verify downstream repository interactions are bypassed
        verify(this.userSocialAccountRepository, never()).findByProviderAndProviderUserId(anyString(), anyString());
        verify(this.jwtTokenProvider, never()).generateAccessToken(anyString(), anyString(), anyString());

        LOGGER.info("[TEST_END] [REQ-002] authenticateWithUnknownProvider_throwsUnsupportedProviderException passed successfully");
    }

    /**
     * Test Case 5: Happy Path / Edge Case - Facebook authentication updates profile picture URL.
     * <p>
     * Verifies that when Facebook OAuth2 authentication succeeds:
     * 1. Method {@code findByProviderAndProviderUserId} on {@link UserSocialAccountRepository} is invoked with exact parameters.
     * 2. Existing {@link UserSocialAccount} receives updated {@code profilePictureUrl}.
     * 3. Modified social account entity is persisted back to the database.
     * </p>
     *
     * @verifies [REQ-002], [ARC-006], [NFR-003]
     */
    @Test
    @DisplayName("[REQ-002] Edge Case: Facebook login synchronizes updated profile picture to UserSocialAccount")
    void authenticateWithFacebook_savesProfilePicture() {
        LOGGER.info("[TEST_START] [REQ-002] Executing authenticateWithFacebook_savesProfilePicture");

        // GIVEN: Existing Facebook-linked user with an outdated avatar URL
        final UUID existingUserId = UUID.randomUUID();
        final User existingUser = new User();
        existingUser.setUserId(existingUserId);
        existingUser.setEmail("facebook.user@membershiphub.org");
        existingUser.setFullName("Facebook Member");
        existingUser.setRole(this.defaultStudentRole);
        existingUser.setProvider(PROVIDER_FACEBOOK);

        final UserSocialAccount existingSocialAccount = new UserSocialAccount();
        existingSocialAccount.setSocialAccountId(UUID.randomUUID());
        existingSocialAccount.setUser(existingUser);
        existingSocialAccount.setProvider(PROVIDER_FACEBOOK);
        existingSocialAccount.setProviderUserId(MOCK_FACEBOOK_USER_ID);
        existingSocialAccount.setProfilePictureUrl("https://graph.facebook.com/v18.0/outdated-avatar.png");
        existingSocialAccount.setLinkedAt(LocalDateTime.now().minusMonths(1));

        final SocialAuthRequest request = new SocialAuthRequest();
        request.setProvider(PROVIDER_FACEBOOK);
        request.setIdToken(MOCK_FACEBOOK_ACCESS_TOKEN);
        // Explicit profile picture override supplied in client request
        request.setProfilePicture(MOCK_CLIENT_OVERRIDE_PICTURE_URL);

        final SocialUserInfo verifiedUserInfo = new SocialUserInfo(
                "facebook.user@membershiphub.org",
                "Facebook Member",
                MOCK_FACEBOOK_USER_ID,
                MOCK_FACEBOOK_PICTURE_URL
        );

        when(this.socialTokenVerifier.verify(PROVIDER_FACEBOOK, MOCK_FACEBOOK_ACCESS_TOKEN))
                .thenReturn(verifiedUserInfo);
        when(this.userSocialAccountRepository.findByProviderAndProviderUserId(PROVIDER_FACEBOOK, MOCK_FACEBOOK_USER_ID))
                .thenReturn(Optional.of(existingSocialAccount));
        doNothing().when(this.userSocialAccountRepository).persist(any(UserSocialAccount.class));

        when(this.jwtTokenProvider.generateAccessToken(existingUserId.toString(), DEFAULT_ROLE_NAME, PROVIDER_FACEBOOK))
                .thenReturn(MOCK_ACCESS_TOKEN);
        when(this.jwtTokenProvider.generateRefreshToken(existingUserId.toString()))
                .thenReturn(MOCK_REFRESH_TOKEN);

        // WHEN: Executing Facebook authentication with avatar update
        final AuthResponse response = this.socialAuthService.authenticateWithSocial(request);

        // THEN: Verify response structure
        assertNotNull(response);
        assertEquals(existingUserId, response.getUserId());
        assertFalse(response.isNewUser());

        // Verify repository lookup was invoked with exact normalized parameters
        verify(this.userSocialAccountRepository, times(1))
                .findByProviderAndProviderUserId(eq(PROVIDER_FACEBOOK), eq(MOCK_FACEBOOK_USER_ID));

        // Capture updated social account and verify avatar synchronization
        final ArgumentCaptor<UserSocialAccount> accountCaptor = ArgumentCaptor.forClass(UserSocialAccount.class);
        verify(this.userSocialAccountRepository, times(1)).persist(accountCaptor.capture());
        final UserSocialAccount updatedAccount = accountCaptor.getValue();

        assertEquals(MOCK_CLIENT_OVERRIDE_PICTURE_URL, updatedAccount.getProfilePictureUrl(),
                "Persisted profile picture must match the newly provided client override URL");

        LOGGER.info("[TEST_END] [REQ-002] authenticateWithFacebook_savesProfilePicture passed successfully");
    }

    /**
     * Test Case 6: Edge Case / Boundary - Null or empty SocialAuthRequest payload.
     * <p>
     * Verifies that when an empty or null request payload is submitted:
     * 1. {@link InvalidTokenException} is immediately thrown before hitting verification services.
     * 2. Downstream verifiers and repositories are never invoked.
     * </p>
     *
     * @verifies [REQ-002], [EXC-004]
     */
    @Test
    @DisplayName("[REQ-002][EXC-004] Boundary: Null request or missing mandatory fields throws InvalidTokenException")
    void authenticateWithNullPayload_throwsInvalidTokenException() {
        LOGGER.info("[TEST_START] [REQ-002] Executing authenticateWithNullPayload_throwsInvalidTokenException");

        // Case A: Entire request is null
        assertThrows(
                InvalidTokenException.class,
                () -> this.socialAuthService.authenticateWithSocial(null),
                "Passing null SocialAuthRequest must trigger InvalidTokenException"
        );

        // Case B: Provider field is null
        final SocialAuthRequest requestMissingProvider = new SocialAuthRequest();
        requestMissingProvider.setIdToken(MOCK_GOOGLE_ID_TOKEN);
        assertThrows(
                InvalidTokenException.class,
                () -> this.socialAuthService.authenticateWithSocial(requestMissingProvider),
                "Missing provider must trigger InvalidTokenException"
        );

        // Case C: IdToken field is null
        final SocialAuthRequest requestMissingToken = new SocialAuthRequest();
        requestMissingToken.setProvider(PROVIDER_GOOGLE);
        assertThrows(
                InvalidTokenException.class,
                () -> this.socialAuthService.authenticateWithSocial(requestMissingToken),
                "Missing idToken must trigger InvalidTokenException"
        );

        // Verify verification services are completely untouched
        verify(this.socialTokenVerifier, never()).verify(anyString(), anyString());

        LOGGER.info("[TEST_END] [REQ-002] authenticateWithNullPayload_throwsInvalidTokenException passed successfully");
    }

    /**
     * Test Case 7: Edge Case - Existing primary user by email links a new social provider.
     * <p>
     * Verifies that when an unlinked social account authenticates with an email already present in the primary table:
     * 1. The existing {@link User} entity is matched by email.
     * 2. A new {@link UserSocialAccount} link is established for that user without creating a duplicate primary user.
     * 3. {@link AuthResponse} indicates isNewUser = false.
     * </p>
     *
     * @verifies [REQ-002], [ARC-006]
     */
    @Test
    @DisplayName("[REQ-002] Edge Case: Existing user by email establishes linkage with new social provider")
    void authenticateWithFirebase_existingUserByEmail_linksAccount() {
        LOGGER.info("[TEST_START] [REQ-002] Executing authenticateWithFirebase_existingUserByEmail_linksAccount");

        // GIVEN: An existing primary user in the database (e.g. registered locally previously)
        final UUID existingUserId = UUID.randomUUID();
        final User existingUser = new User();
        existingUser.setUserId(existingUserId);
        existingUser.setEmail(MOCK_USER_EMAIL);
        existingUser.setFullName("Existing Local Member");
        existingUser.setRole(this.defaultStudentRole);
        existingUser.setProvider("local");

        final String firebaseUserId = "firebase-uid-777888";
        final SocialAuthRequest request = new SocialAuthRequest();
        request.setProvider(PROVIDER_FIREBASE);
        request.setIdToken("mock.firebase.id.token");

        final SocialUserInfo verifiedUserInfo = new SocialUserInfo(
                MOCK_USER_EMAIL,
                "Existing Local Member",
                firebaseUserId,
                null
        );

        when(this.socialTokenVerifier.verify(PROVIDER_FIREBASE, "mock.firebase.id.token"))
                .thenReturn(verifiedUserInfo);

        // No link exists for (firebase, firebase-uid-777888) yet
        when(this.userSocialAccountRepository.findByProviderAndProviderUserId(PROVIDER_FIREBASE, firebaseUserId))
                .thenReturn(Optional.empty());

        // Match found by email in primary Users repository
        when(this.userRepository.findByEmail(MOCK_USER_EMAIL))
                .thenReturn(Optional.of(existingUser));

        doNothing().when(this.userSocialAccountRepository).persist(any(UserSocialAccount.class));

        when(this.jwtTokenProvider.generateAccessToken(existingUserId.toString(), DEFAULT_ROLE_NAME, PROVIDER_FIREBASE))
                .thenReturn(MOCK_ACCESS_TOKEN);
        when(this.jwtTokenProvider.generateRefreshToken(existingUserId.toString()))
                .thenReturn(MOCK_REFRESH_TOKEN);

        // WHEN: Authenticating via Firebase
        final AuthResponse response = this.socialAuthService.authenticateWithSocial(request);

        // THEN: Verify response connects to existing user record
        assertNotNull(response);
        assertEquals(existingUserId, response.getUserId());
        assertFalse(response.isNewUser(), "isNewUser must be false when linking to an existing email profile");

        // Verify primary user was not recreated
        verify(this.userRepository, never()).persist(any(User.class));

        // Verify new linkage record was persisted
        final ArgumentCaptor<UserSocialAccount> linkCaptor = ArgumentCaptor.forClass(UserSocialAccount.class);
        verify(this.userSocialAccountRepository, times(1)).persist(linkCaptor.capture());
        final UserSocialAccount persistedLink = linkCaptor.getValue();

        assertEquals(PROVIDER_FIREBASE, persistedLink.getProvider());
        assertEquals(firebaseUserId, persistedLink.getProviderUserId());
        assertEquals(existingUser, persistedLink.getUser());

        LOGGER.info("[TEST_END] [REQ-002] authenticateWithFirebase_existingUserByEmail_linksAccount passed successfully");
    }

    /**
     * Test Case 8: Exception Case - Unexpected internal runtime failure wraps into SocialAuthProcessingException.
     * <p>
     * Verifies that unexpected runtime exceptions (e.g. database network disconnection)
     * are caught and wrapped into {@link SocialAuthProcessingException} with preserved root cause.
     * </p>
     *
     * @verifies [REQ-002], [EXC-004], [NFR-003]
     */
    @Test
    @DisplayName("[REQ-002][EXC-004] Exception Path: Unexpected runtime exception wraps into SocialAuthProcessingException")
    void authenticateWithUnexpectedDbException_wrapsAndPreservesRootCause() {
        LOGGER.info("[TEST_START] [REQ-002] Executing authenticateWithUnexpectedDbException_wrapsAndPreservesRootCause");

        // GIVEN: Request encountering an unexpected database runtime failure
        final SocialAuthRequest request = new SocialAuthRequest();
        request.setProvider(PROVIDER_GOOGLE);
        request.setIdToken(MOCK_GOOGLE_ID_TOKEN);

        final SocialUserInfo verifiedUserInfo = new SocialUserInfo(
                MOCK_USER_EMAIL,
                MOCK_USER_FULL_NAME,
                MOCK_GOOGLE_USER_ID,
                MOCK_GOOGLE_PICTURE_URL
        );

        when(this.socialTokenVerifier.verify(PROVIDER_GOOGLE, MOCK_GOOGLE_ID_TOKEN))
                .thenReturn(verifiedUserInfo);

        final RuntimeException simulatedDbException = new RuntimeException("Database cluster connection timeout");
        when(this.userSocialAccountRepository.findByProviderAndProviderUserId(PROVIDER_GOOGLE, MOCK_GOOGLE_USER_ID))
                .thenThrow(simulatedDbException);

        // WHEN & THEN: Assert SocialAuthProcessingException is thrown
        final SocialAuthProcessingException exception = assertThrows(
                SocialAuthProcessingException.class,
                () -> this.socialAuthService.authenticateWithSocial(request),
                "Expected SocialAuthProcessingException on unexpected internal failure"
        );

        assertEquals(SocialAuthService.ERROR_CODE_PROCESSING_FAILED, exception.getErrorCode(),
                "Error code must match SOCIAL_AUTH_PROCESSING_FAILED constant");
        assertEquals(simulatedDbException, exception.getCause(),
                "Original root cause exception stack trace must be preserved");

        // Verify audit log registers the failed authentication attempt
        verify(this.authAuditLogger, times(1)).logAuthEvent(
                eq(null),
                eq(SocialAuthService.AUDIT_ACTION_SOCIAL_LOGIN_FAILED),
                anyString()
        );

        LOGGER.info("[TEST_END] [REQ-002] authenticateWithUnexpectedDbException_wrapsAndPreservesRootCause passed successfully");
    }
}