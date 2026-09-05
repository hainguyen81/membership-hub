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