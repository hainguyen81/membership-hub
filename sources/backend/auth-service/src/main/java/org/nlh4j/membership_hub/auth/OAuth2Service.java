/**
 * OAuth2Service implements the enterprise OAuth2 authentication flow for external identity providers.
 * This service integrates with Firebase, Google, and Facebook OAuth2 mechanisms to validate user
 * credentials, retrieve user profile information, and issue internal JWT tokens.
 *
 * @traceability [REQ-002], [EXC-004]
 */
package org.nlh4j.saas.membership_hub.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Custom exception for OAuth2 authentication failures.
 * This exception preserves the original cause chain as required by enterprise error handling.
 */
@SuppressWarnings("serial")
class OAuth2AuthenticationException extends RuntimeException {
    public OAuth2AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Data transfer object representing an OAuth2 user profile retrieved from external providers.
 */
class OAuth2UserInfo {
    private String provider;
    private String providerId;
    private String email;
    private String fullName;
    // Additional fields can be added as needed (e.g., avatarUrl, locale)
}

/**
 * Service class responsible for OAuth2 authentication flows.
 * <p>
 * The implementation follows strict enterprise coding standards:
 * <ul>
 *   <li>All configuration values are defined as immutable constants at the top of the class.</li>
 *   <li>Comprehensive SLF4J logging is injected at entry/exit points and within catch blocks.</li>
 *   <li>Every exception is logged with the required three context keys (module, raw error, traceability tag).</li>
 *   <li>Idempotent user creation ensures a single user record per external provider identity.</li>
 * </ul>
 *
 * @traceability [REQ-002], [EXC-004]
 */
@Service
public class OAuth2Service {

    // -------------------------------------------------------------------------
    // ENTERPRISE CONSTANTS (Anti‑Magic‑Numbers Policy)
    // -------------------------------------------------------------------------
    /** OAuth2 token endpoint URLs for each supported provider. */
    public static final String FIREBASE_TOKEN_URL = "https://identitytoolkit.googleapis.com/v1/token";
    public static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    public static final String FACEBOOK_TOKEN_URL = "https://graph.facebook.com/v18.0/oauth/access_token";

    /** JWT token validity periods (in milliseconds). */
    public static final long JWT_ACCESS_TOKEN_VALIDITY_MS = 15 * 60 * 1000L; // 15 minutes
    public static final long JWT_REFRESH_TOKEN_VALIDITY_MS = 7 * 24 * 60 * 60 * 1000L; // 7 days

    /** Scopes required for provider APIs. */
    public static final String GOOGLE_USERINFO_SCOPE = "https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email";
    public static final String FACEBOOK_USERINFO_SCOPE = "public_profile,email";

    /** Firebase project configuration – injected from application.yml. */
    @Value("${firebase.web.api-key}")
    private String firebaseApiKey;

    /** Google OAuth2 client credentials – injected from application.yml. */
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    /** Facebook OAuth2 app credentials – injected from application.yml. */
    @Value("${spring.security.oauth2.client.registration.facebook.app-id}")
    private String facebookAppId;
    @Value("${spring.security.oauth2.client.registration.facebook.app-secret}")
    private String facebookAppSecret;

    /** Internal REST client for external HTTP calls. */
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Logger for audit and error tracking – must be present on every service node. */
    private static final Logger logger = LoggerFactory.getLogger(OAuth2Service.class);

    // -------------------------------------------------------------------------
    // PUBLIC API METHODS
    // -------------------------------------------------------------------------

    /**
     * Performs OAuth2 authentication for the specified provider.
     * <p>
     * The method follows a strict idempotent pattern: if a user already exists for the
     * provider‑specific identifier, the existing record is returned; otherwise a new
     * user is created with the role {@code Student} as the default.
     *
     * @param provider OAuth2 provider identifier – {@code firebase}, {@code google}, or {@code facebook}
     * @param code     Authorization code received from the provider’s redirect flow
     * @return a {@link UserDetails} representation of the authenticated user
     * @throws OAuth2AuthenticationException when the provider authentication fails
     * @traceability [REQ-002], [EXC-004]
     */
    public UserDetails authenticate(String provider, String code) {
        logger.info("[ENTRY] OAuth2Service.authenticate(provider={}, code=****)", provider);
        try {
            OAuth2UserInfo userInfo = switch (provider.toLowerCase()) {
                case "firebase" -> authenticateWithFirebase(code);
                case "google"   -> authenticateWithGoogle(code);
                case "facebook" -> authenticateWithFacebook(code);
                default -> throw new IllegalArgumentException("Unsupported OAuth2 provider: " + provider);
            };

            // Idempotent user creation / lookup
            UserDetails user = createOrUpdateUser(userInfo);

            logger.info("[EXIT] OAuth2Service.authenticate – userId={} email={}", user.getUsername(), user.getUsername());
            return user;

        } catch (Exception e) {
            // Comprehensive error logging as per enterprise protocol
            logger.error("[CRITICAL FAIL] [EXC-004] OAuth2 authentication failed for provider '{}'. Raw error: {}", provider, e.getMessage(), e);
            // Preserve the original cause chain when re‑throwing
            throw new OAuth2AuthenticationException("OAuth2 authentication failed for provider: " + provider, e);
        }
    }

    // -------------------------------------------------------------------------
    // PROVIDER‑SPECIFIC IMPLEMENTATIONS
    // -------------------------------------------------------------------------

    /**
     * Authenticates a Firebase ID token.
     * <p>
     * Firebase uses a simple ID token verification; this method delegates to the Firebase
     * Identity Toolkit endpoint to retrieve the decoded JWT payload.
     *
     * @param idToken Firebase ID token supplied by the client SDK
     * @return {@link OAuth2UserInfo} populated with Firebase user data
     */
    private OAuth2UserInfo authenticateWithFirebase(String idToken) {
        logger.debug("[ENTRY] OAuth2Service.authenticateWithFirebase – token=****");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id_token", idToken);
        params.add("providerId", "google.com");
        // Firebase requires an API key for verification
        params.add("key", firebaseApiKey);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(FIREBASE_TOKEN_URL, request, Map.class);
        if (response == null || !response.containsKey("localId")) {
            throw new OAuth2AuthenticationException("Firebase token verification failed – invalid ID token", null);
        }

        OAuth2UserInfo info = new OAuth2UserInfo();
        info.setProvider("firebase");
        info.setProviderId((String) response.get("localId"));
        info.setEmail((String) response.get("email"));
        info.setFullName((String) response.get("displayName"));
        logger.debug("[EXIT] OAuth2Service.authenticateWithFirebase – providerId={}", info.getProviderId());
        return info;
    }

    /**
     * Exchanges a Google authorization code for an access token and fetches user profile.
     *
     * @param code Google OAuth2 authorization code
     * @return {@link OAuth2UserInfo} populated with Google user data
     */
    private OAuth2UserInfo authenticateWithGoogle(String code) {
        logger.debug("[ENTRY] OAuth2Service.authenticateWithGoogle – code=****");
        // Step 1: Exchange code for access token
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
        tokenParams.add("code", code);
        tokenParams.add("client_id", googleClientId);
        tokenParams.add("client_secret", googleClientSecret);
        tokenParams.add("redirect_uri", "http://localhost:8080/login/oauth2/code/google"); // Should be configurable
        tokenParams.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenParams, tokenHeaders);
        @SuppressWarnings("unchecked")
        Map<String, Object> tokenResponse = restTemplate.postForObject(GOOGLE_TOKEN_URL, tokenRequest, Map.class);
        if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
            throw new OAuth2AuthenticationException("Google token exchange failed – invalid authorization code", null);
        }
        String googleAccessToken = (String) tokenResponse.get("access_token");

        // Step 2: Retrieve user info from Google UserInfo endpoint
        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(googleAccessToken);
        HttpEntity<Void> userInfoRequest = new HttpEntity<>(userInfoHeaders);
        Map<String, Object> userInfoMap = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v1/userinfo",
                HttpMethod.GET,
                userInfoRequest,
                Map.class
        ).getBody();

        OAuth2UserInfo info = new OAuth2UserInfo();
        info.setProvider("google");
        info.setProviderId((String) userInfoMap.get("id"));
        info.setEmail((String) userInfoMap.get("email"));
        info.setFullName((String) userInfoMap.get("name"));
        logger.debug("[EXIT] OAuth2Service.authenticateWithGoogle – providerId={}", info.getProviderId());
        return info;
    }

    /**
     * Exchanges a Facebook authorization code for an access token and fetches user profile.
     *
     * @param code Facebook OAuth2 authorization code
     * @return {@link OAuth2UserInfo} populated with Facebook user data
     */
    private OAuth2UserInfo authenticateWithFacebook(String code) {
        logger.debug("[ENTRY] OAuth2Service.authenticateWithFacebook – code=****");
        // Step 1: Exchange code for access token
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
        tokenParams.add("code", code);
        tokenParams.add("client_id", facebookAppId);
        tokenParams.add("client_secret", facebookAppSecret);
        tokenParams.add("redirect_uri", "http://localhost:8080/login/oauth2/code/facebook"); // Should be configurable
        tokenParams.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenParams, tokenHeaders);
        @SuppressWarnings("unchecked")
        Map<String, Object> tokenResponse = restTemplate.postForObject(FACEBOOK_TOKEN_URL, tokenRequest, Map.class);
        if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
            throw new OAuth2AuthenticationException("Facebook token exchange failed – invalid authorization code", null);
        }
        String facebookAccessToken = (String) tokenResponse.get("access_token");

        // Step 2: Retrieve user info from Facebook Graph API
        String userInfoUrl = "https://graph.facebook.com/me?fields=id,name,email&access_token=" + facebookAccessToken;
        Map<String, Object> userInfoMap = restTemplate.getForObject(userInfoUrl, Map.class);

        OAuth2UserInfo info = new OAuth2UserInfo();
        info.setProvider("facebook");
        info.setProviderId((String) userInfoMap.get("id"));
        info.setEmail((String) userInfoMap.get("email"));
        info.setFullName((String) userInfoMap.get("name"));
        logger.debug("[EXIT] OAuth2Service.authenticateWithFacebook – providerId={}", info.getProviderId());
        return info;
    }

    // -------------------------------------------------------------------------
    // USER CREATION / LOOKUP (IDEMPOTENT)
    // -------------------------------------------------------------------------

    /**
     * Creates a new internal user or returns an existing one based on provider‑specific identifiers.
     * <p>
     * This method enforces idempotency: a user is uniquely identified by the combination of
     * {@code provider} and {@code providerId}. If a record already exists, it is returned;
     * otherwise a new {@link UserDetails} object is constructed with the default role {@code Student}.
     *
     * @param userInfo external provider user information
     * @return {@link UserDetails} representing the internal user
     */
    private UserDetails createOrUpdateUser(OAuth2UserInfo userInfo) {
        // In a real implementation, this would query the database via a repository.
        // For demonstration, we simulate a lookup and creation using an in‑memory map.
        // Production code must replace this with a proper JPA/Spring Data repository call.
        logger.debug("[ENTRY] OAuth2Service.createOrUpdateUser – provider={}, providerId={}",
                userInfo.getProvider(), userInfo.getProviderId());

        // Simulate DB lookup – replace with actual repository call
        Map<String, UserDetails> userStore = new HashMap<>(); // placeholder
        String compositeKey = userInfo.getProvider() + ":" + userInfo.getProviderId();
        UserDetails existing = userStore.get(compositeKey);

        if (existing != null) {
            logger.info("[INFO] OAuth2Service.createOrUpdateUser – user already exists: {}", existing.getUsername());
            return existing;
        }

        // Build a Spring Security User object
        UserDetails newUser = User.builder()
                .username(UUID.randomUUID().toString()) // internal user ID – could be the providerId
                .password("") // OAuth2 users typically have no password; account may be password‑less
                .authorities("ROLE_" + "STUDENT") // default role
                .accountExpired(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();

        // Persist the user (simulated)
        userStore.put(compositeKey, newUser);
        logger.info("[INFO] OAuth2Service.createOrUpdateUser – new user created with internal id: {}", newUser.getUsername());
        logger.debug("[EXIT] OAuth2Service.createOrUpdateUser – internalUserId={}", newUser.getUsername());
        return newUser;
    }
}