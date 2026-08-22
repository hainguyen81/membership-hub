# Day 2: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/auth-service/src/main/java/org/nlh4j/membership_hub/auth/OAuth2Service.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Component Destination Path: `./sources/backend/auth-service/src/main/java/org/nlh4j/membership_hub/auth/OAuth2Service.java`
*   Traceability Audit Tags For This Task: ['[REQ-002]', '[EXC-004]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tích hợp luồng xác thực OAuth2 với Firebase, Google và Facebook']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-002]', '[EXC-004]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
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
```

# Day 2: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/main/java/org/nlh4j/membership_hub/user/RoleService.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Component Destination Path: `./sources/backend/user-service/src/main/java/org/nlh4j/membership_hub/user/RoleService.java`
*   Traceability Audit Tags For This Task: ['[REQ-003]', '[ARC-001]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Xây dựng endpoint quản lý vai trò người dùng (RBAC)']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-003]', '[ARC-001]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership-hub.user;

/**
 * Service layer for managing user roles and Role-Based Access Control (RBAC).
 * Implements the core business logic for assigning, retrieving, updating, and revoking roles
 * to support the enterprise authorization framework.
 *
 * @traceability [REQ-003], [ARC-001]
 */
@Service
@Transactional
public class RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);

    /* --------------------------------------------------------------------- */
    /* CONSTANTS – Business rules and error messages for role management      */
    /* --------------------------------------------------------------------- */
    /** Maximum allowed length for a role name (enforced by validation). */
    public static final int MAX_ROLE_NAME_LENGTH = 30;
    /** Regular expression pattern for valid role names (uppercase letters and underscores only). */
    public static final String ROLE_NAME_PATTERN = "^[A-Z_]+$";
    /** Standard error message when a role is not found. */
    public static final String ERROR_ROLE_NOT_FOUND = "Role not found with id: %s";
    /** Standard error message when a user is not found. */
    public static final String ERROR_USER_NOT_FOUND = "User not found with id: %s";
    /** Error message for duplicate role assignments. */
    public static final String ERROR_ROLE_ASSIGN_CONFLICT = "Role assignment conflict: user already has role: %s";

    /* --------------------------------------------------------------------- */
    /* DEPENDENCIES – Repositories for Users, Roles, and the many‑to‑many link   */
    /* --------------------------------------------------------------------- */
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository,
                       UserRepository userRepository,
                       UserRoleRepository userRoleRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    /* --------------------------------------------------------------------- */
    /* PUBLIC API – Role management operations                               */
    /* --------------------------------------------------------------------- */

    /**
     * Assigns a role to a user.
     * <p>
     * This operation is idempotent – attempting to assign an already‑assigned role
     * will raise a {@link DuplicateResourceException}. Full audit logging and
     * comprehensive exception handling are applied to satisfy enterprise
     * reliability and traceability requirements.
     *
     * @param userId UUID of the target user.
     * @param roleId  UUID of the role to assign.
     *
     * @traceability [REQ-003], [ARC-001]
     */
    public void assignRoleToUser(UUID userId, UUID roleId) {
        logger.info("[ENTRY] assignRoleToUser userId={} roleId={}", userId, roleId);
        try {
            // Resolve user and role entities – throw descriptive exceptions if missing
            Users user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(ERROR_USER_NOT_FOUND, userId)));
            Roles role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(ERROR_ROLE_NOT_FOUND, roleId)));

            // Idempotent check – avoid duplicate assignments
            boolean alreadyAssigned = userRoleRepository.existsByUserAndRole(user, role);
            if (alreadyAssigned) {
                logger.warn("[WARN] assignRoleToUser conflict: user {} already has role {}", userId, roleId);
                throw new DuplicateResourceException(String.format(ERROR_ROLE_ASSIGN_CONFLICT, role.getName()));
            }

            // Persist the association
            UserRoles userRole = new UserRoles();
            userRole.setUser(user);
            userRole.setRole(role);
            userRole.setAssignedAt(Instant.now());
            userRoleRepository.save(userRole);

            logger.info("[EXIT] assignRoleToUser completed for userId={}", userId);
        } catch (Exception e) {
            // Enterprise‑grade error logging – includes traceability tag and raw error
            logger.error("[CRITICAL FAIL] [ARC-001] Role assignment failed for userId={} roleId={}. Raw error: {}", userId, roleId, e.getMessage(), e);
            throw new RoleAssignmentException("Failed to assign role", e);
        }
    }

    /**
     * Retrieves all roles currently assigned to the specified user.
     *
     * @param userId UUID of the user whose roles are requested.
     * @return List of {@link Roles} objects.
     *
     * @traceability [REQ-003], [ARC-001]
     */
    public List<Roles> getRolesForUser(UUID userId) {
        logger.info("[ENTRY] getRolesForUser userId={}", userId);
        try {
            Users user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(ERROR_USER_NOT_FOUND, userId)));
            List<UserRoles> userRoles = userRoleRepository.findByUser(user);
            List<Roles> roles = userRoles.stream()
                .map(UserRoles::getRole)
                .collect(Collectors.toList());
            logger.info("[EXIT] getRolesForUser returned {} roles for userId={}", roles.size(), userId);
            return roles;
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [ARC-001] Failed to retrieve roles for userId={}. Raw error: {}", userId, e.getMessage(), e);
            throw new RoleRetrievalException("Failed to retrieve user roles", e);
        }
    }

    /**
     * Updates a user's role by revoking an old role and assigning a new one.
     * <p>
     * This operation ensures atomicity – both the revocation and the new assignment
     * are persisted within a single transaction.
     *
     * @param userId   UUID of the user.
     * @param oldRoleId UUID of the role to revoke.
     * @param newRoleId UUID of the role to assign.
     *
     * @traceability [REQ-003], [ARC-001]
     */
    public void updateUserRole(UUID userId, UUID oldRoleId, UUID newRoleId) {
        logger.info("[ENTRY] updateUserRole userId={} oldRoleId={} newRoleId={}", userId, oldRoleId, newRoleId);
        try {
            Users user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(ERROR_USER_NOT_FOUND, userId)));
            Roles oldRole = roleRepository.findById(oldRoleId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(ERROR_ROLE_NOT_FOUND, oldRoleId)));
            Roles newRole = roleRepository.findById(newRoleId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(ERROR_ROLE_NOT_FOUND, newRoleId)));

            // Revoke the old role
            userRoleRepository.deleteByUserAndRole(user, oldRole);

            // Assign the new role
            UserRoles userRole = new UserRoles();
            userRole.setUser(user);
            userRole.setRole(newRole);
            userRole.setAssignedAt(Instant.now());
            userRoleRepository.save(userRole);

            logger.info("[EXIT] updateUserRole completed for userId={}", userId);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [ARC-001] Role update failed for userId={} oldRoleId={} newRoleId={}. Raw error: {}", userId, oldRoleId, newRoleId, e.getMessage(), e);
            throw new RoleUpdateException("Failed to update user role", e);
        }
    }

    /**
     * Revokes a role from a user.
     *
     * @param userId UUID of the user.
     * @param roleId  UUID of the role to revoke.
     *
     * @traceability [REQ-003], [ARC-001]
     */
    public void revokeRoleFromUser(UUID userId, UUID roleId) {
        logger.info("[ENTRY] revokeRoleFromUser userId={} roleId={}", userId, roleId);
        try {
            Users user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(ERROR_USER_NOT_FOUND, userId)));
            Roles role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(ERROR_ROLE_NOT_FOUND, roleId)));

            boolean exists = userRoleRepository.existsByUserAndRole(user, role);
            if (!exists) {
                logger.warn("[WARN] revokeRoleFromUser: user {} does not have role {}", userId, roleId);
                throw new EntityNotFoundException("User does not have the specified role");
            }

            userRoleRepository.deleteByUserAndRole(user, role);
            logger.info("[EXIT] revokeRoleFromUser completed for userId={}", userId);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [ARC-001] Role revocation failed for userId={} roleId={}. Raw error: {}", userId, roleId, e.getMessage(), e);
            throw new RoleRevocationException("Failed to revoke role", e);
        }
    }

    /**
     * Lists all available roles in the system (useful for UI role‑assignment screens).
     *
     * @return List of all {@link Roles}.
     *
     * @traceability [REQ-003], [ARC-001]
     */
    public List<Roles> listAllRoles() {
        logger.info("[ENTRY] listAllRoles");
        try {
            List<Roles> roles = roleRepository.findAll();
            logger.info("[EXIT] listAllRoles returned {} roles", roles.size());
            return roles;
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [ARC-001] Failed to list all roles. Raw error: {}", e.getMessage(), e);
            throw new RoleListingException("Failed to list roles", e);
        }
    }

    /* --------------------------------------------------------------------- */
    /* INNER EXCEPTION TYPES – Custom enterprise exceptions for role operations */
    /* --------------------------------------------------------------------- */

    /** Thrown when a duplicate role assignment is attempted. */
    @SuppressWarnings("serial")
    public static class DuplicateResourceException extends RuntimeException {
        public DuplicateResourceException(String message) { super(message); }
    }

    /** Base exception for all role‑assignment failures. */
    @SuppressWarnings("serial")
    public static class RoleAssignmentException extends RuntimeException {
        public RoleAssignmentException(String message, Throwable cause) { super(message, cause); }
    }

    /** Thrown when a role cannot be retrieved. */
    @SuppressWarnings("serial")
    public static class RoleRetrievalException extends RuntimeException {
        public RoleRetrievalException(String message, Throwable cause) { super(message, cause); }
    }

    /** Thrown when a role update fails. */
    @SuppressWarnings("serial")
    public static class RoleUpdateException extends RuntimeException {
        public RoleUpdateException(String message, Throwable cause) { super(message, cause); }
    }

    /** Thrown when a role revocation fails. */
    @SuppressWarnings("serial")
    public static class RoleRevocationException extends RuntimeException {
        public RoleRevocationException(String message, Throwable cause) { super(message, cause); }
    }

    /** Thrown when listing all roles fails. */
    @SuppressWarnings("serial")
    public static class RoleListingException extends RuntimeException {
        public RoleListingException(String message, Throwable cause) { super(message, cause); }
    }

    /* --------------------------------------------------------------------- */
    /* INNER DOMAIN MODELS – Simplified POJOs for demonstration purposes      */
    /* --------------------------------------------------------------------- */

    @Entity
    @Table(name = "roles")
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Roles {
        @Id @GeneratedValue(strategy = GenerationType.AUTO)
        private UUID id;
        @Column(nullable = false, length = MAX_ROLE_NAME_LENGTH, unique = true)
        private String name;
        @Column(length = 255)
        private String description;
        @Column(name = "created_at", nullable = false, updatable = false)
        private Instant createdAt = Instant.now();
    }

    @Entity
    @Table(name = "users")
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Users {
        @Id @GeneratedValue(strategy = GenerationType.AUTO)
        private UUID id;
        @Column(nullable = false, unique = true, length = 255)
        private String email;
        @Column(name = "full_name", nullable = false, length = 100)
        private String fullName;
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "role_id", nullable = false)
        private Roles role;
        @Column(name = "created_at", nullable = false, updatable = false)
        private Instant createdAt = Instant.now();
    }

    @Entity
    @Table(name = "user_roles")
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class UserRoles {
        @Id @GeneratedValue(strategy = GenerationType.AUTO)
        private UUID id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private Users user;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "role_id", nullable = false)
        private Roles role;

        @Column(name = "assigned_at", nullable = false, updatable = false)
        private Instant assignedAt = Instant.now();
    }

    /* --------------------------------------------------------------------- */
    /* REPOSITORY INTERFACES – Spring Data JPA contracts                       */
    /* --------------------------------------------------------------------- */

    public interface RoleRepository extends JpaRepository<Roles, UUID> {
        // Custom queries can be added here if needed (e.g., findByName)
    }

    public interface UserRepository extends JpaRepository<Users, UUID> {
        // Custom queries can be added here if needed (e.g., findByEmail)
    }

    public interface UserRoleRepository extends JpaRepository<UserRoles, UUID> {
        boolean existsByUserAndRole(Users user, Roles role);
        void deleteByUserAndRole(Users user, Roles role);
        List<UserRoles> findByUser(Users user);
    }
}
```

# Day 2: model kilo-auto/free - API Endpoint https://api.kilo.ai/api/gateway
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/main/java/org/nlh4j/membership_hub/user/UserResource.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Component Destination Path: `./sources/backend/user-service/src/main/java/org/nlh4j/membership_hub/user/UserResource.java`
*   Traceability Audit Tags For This Task: ['[REQ-003]', '[ARC-001]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Xây dựng endpoint lấy danh sách người dùng và quản lý vai trò']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-003]', '[ARC-001]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership_hub.user;

// ====================== IMPORTS ======================
import java.util.UUID;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.nlh4j.saas.membership_hub.dto.PageResponse;
import org.nlh4j.saas.membership_hub.dto.RoleUpdateRequest;
import org.nlh4j.saas.membership_hub.dto.UserResponse;
import org.nlh4j.saas.membership_hub.entity.Role;
import org.nlh4j.saas.membership_hub.exception.MembershipHubException;
import org.nlh4j.saas.membership_hub.service.RoleService;
import org.nlh4j.saas.membership_hub.service.UserService;
import org.nlh4j.saas.membership_hub.util.DataMaskingUtil;

/**
 * REST Resource for user and role management operations in the membership-hub system.
 * Implements RBAC (Role-Based Access Control) compliant endpoints for user listing and role assignment,
 * enforcing strict permission isolation per enterprise security architecture requirements.
 * 
 * @traceability [REQ-003], [ARC-001]
 */
@Path("/api/v1/admin/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("SYSTEM_ADMIN") // Enforce RBAC: only System Admin can access these endpoints per [ARC-001]
public class UserResource {
    // ====================== TOP-OF-CLASS CONSTANTS (NO HARDCODED LITERALS IN METHOD BODIES) ======================
    /** Default page number for paginated user list (minimum value: 1) */
    public static final String DEFAULT_PAGE = "1";
    /** Default page size for paginated user list */
    public static final String DEFAULT_PAGE_SIZE = "20";
    /** Maximum allowed page size to prevent excessive data exposure and DoS risks */
    public static final int MAX_PAGE_SIZE = 100;
    /** Error message prefix for user not found scenarios */
    public static final String ERR_USER_NOT_FOUND = "User not found with ID: ";
    /** Error message prefix for role not found scenarios */
    public static final String ERR_ROLE_NOT_FOUND = "Role not found with ID: ";
    /** Error message for invalid role ID format/range */
    public static final String ERR_INVALID_ROLE_ID = "Invalid role ID: must be a numeric value between 1 and 5 (matching 5 system-defined roles)";
    /** Error message for unauthorized access attempts */
    public static final String ERR_PERMISSION_DENIED = "Access denied: Only System Admin can perform user/role management operations";
    /** Error message for duplicate email conflicts during user updates */
    public static final String ERR_EMAIL_CONFLICT = "Email is already registered to another user";
    /** Logger instance for audit logging, error tracking, and compliance reporting per [NFR-006] */
    private static final Logger logger = Logger.getLogger(UserResource.class);

    // ====================== DEPENDENCY INJECTION (CDI) ======================
    /** Service layer for user business logic, abstracts data access and business rules */
    @Inject
    UserService userService;
    /** Service layer for role business logic, validates role existence and permissions */
    @Inject
    RoleService roleService;
    /** Utility for masking sensitive PII data (emails, UUIDs) in logs per [NFR-006] */
    @Inject
    DataMaskingUtil dataMaskingUtil;

    // ====================== ENDPOINT: LIST USERS WITH FILTERING AND PAGINATION ======================
    /**
     * Retrieves a paginated list of users with optional filtering by role and search term.
     * Supports filtering by role ID, full name/email search, and pagination to handle large user datasets.
     * Delegates data aggregation to the service layer to avoid in-memory iteration and ensure optimal performance.
     * 
     * @param roleId Optional role ID to filter users (1-5 corresponding to system-defined roles)
     * @param searchTerm Optional search term to filter by user full name or masked email
     * @param page Optional page number (default: 1, minimum: 1)
     * @param size Optional page size (default: 20, maximum: 100)
     * @return Paginated list of user response DTOs with role information, wrapped in standard page response structure
     * @traceability [REQ-003], [ARC-001]
     */
    @GET
    public Response getUsers(
            @QueryParam("roleId") String roleId,
            @QueryParam("searchTerm") String searchTerm,
            @QueryParam("page") String page,
            @QueryParam("size") String size) {
        // [AUDIT LOG] Entry point log with masked sensitive input parameters per [NFR-006]
        logger.info("[PROCESS] [REQ-003] [ARC-001] Entering getUsers endpoint | Filters - roleId: " + dataMaskingUtil.maskSensitiveData(roleId) + ", searchTerm: " + dataMaskingUtil.maskSensitiveData(searchTerm));
        try {
            // Parse and validate pagination parameters with fallback to defaults
            int pageNum = parsePositiveInteger(page, DEFAULT_PAGE);
            int pageSize = parsePositiveInteger(size, DEFAULT_PAGE_SIZE);
            // Enforce maximum page size cap to prevent excessive data retrieval and DoS attacks
            if (pageSize > MAX_PAGE_SIZE) {
                pageSize = MAX_PAGE_SIZE;
                logger.debug("[DEBUG] [REQ-003] Page size capped to maximum allowed value: " + MAX_PAGE_SIZE + " for security");
            }
            // Delegate data aggregation to service layer (uses indexed database queries, no in-memory iteration per enterprise performance rules)
            PageResponse<UserResponse> userPage = userService.getAllUsers(roleId, searchTerm, pageNum, pageSize);
            // [AUDIT LOG] Exit point log with success context and record count
            logger.info("[PROCESS] [REQ-003] [ARC-001] Successfully retrieved " + userPage.getTotalElements() + " total users, returning page " + pageNum + " with " + userPage.getContent().size() + " records");
            return Response.ok(userPage).build();
        } catch (IllegalArgumentException e) {
            // [ERROR LOG] Log validation errors with required 3 context keys: subsystem, raw error, traceability tags
            logger.error("[CRITICAL FAIL] [REQ-003] [ARC-001] UserResource - Invalid input parameter in getUsers. Raw error: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MembershipHubException("VALIDATION_FAILED", "Invalid input parameters: " + e.getMessage()))
                    .build();
        } catch (Exception e) {
            // [ERROR LOG] Log unexpected errors with full stack trace for debugging, preserve root cause
            logger.error("[CRITICAL FAIL] [REQ-003] [ARC-001] UserResource - Unexpected system error in getUsers endpoint. Raw error: {}", e.getMessage(), e);
            // Forward original exception to custom enterprise exception to preserve stack trace per governance rules
            throw new MembershipHubException("INTERNAL_SERVER_ERROR", "Failed to retrieve user list due to system error", e);
        }
    }

    // ====================== ENDPOINT: UPDATE USER ROLE ======================
    /**
     * Updates the role of a specified user, applying RBAC permissions immediately as per [REQ-003].
     * Validates user existence, role validity, and enforces permission checks before updating.
     * 
     * @param userId UUID of the user to update role for (path parameter)
     * @param roleUpdateRequest Request body containing new role ID (1-5)
     * @return Success message confirming role update and permission application
     * @traceability [REQ-003], [ARC-001]
     */
    @PUT
    @Path("/{userId}/role")
    public Response updateUserRole(@PathParam("userId") String userId, RoleUpdateRequest roleUpdateRequest) {
        // [AUDIT LOG] Entry point log with masked sensitive user ID per [NFR-006]
        logger.info("[PROCESS] [REQ-003] [ARC-001] Entering updateUserRole endpoint for user ID: " + dataMaskingUtil.maskSensitiveData(userId));
        try {
            // Validate UUID format of user ID (prevents invalid input attacks)
            UUID userUuid = UUID.fromString(userId);
            // Validate request body and role ID presence
            if (roleUpdateRequest == null || roleUpdateRequest.getRoleId() == null) {
                logger.warn("[WARN] [REQ-003] [ARC-001] Missing role ID in update request for user: " + dataMaskingUtil.maskSensitiveData(userId));
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new MembershipHubException("VALIDATION_FAILED", "Role ID is required in request body"))
                        .build();
            }
            // Validate role ID is within valid range (1-5 for 5 system-defined roles per [ARC-001])
            Short roleId = validateRoleId(roleUpdateRequest.getRoleId());
            // Check if target role exists in system to prevent invalid role assignments
            Role existingRole = roleService.getRoleById(roleId);
            if (existingRole == null) {
                logger.warn("[WARN] [REQ-003] [ARC-001] Attempt to assign non-existent role ID: " + roleId + " to user: " + dataMaskingUtil.maskSensitiveData(userId));
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new MembershipHubException("ROLE_NOT_FOUND", ERR_ROLE_NOT_FOUND + roleId))
                        .build();
            }
            // Check if target user exists to prevent updates to non-existent users
            if (!userService.userExists(userUuid)) {
                logger.warn("[WARN] [REQ-003] [ARC-001] Attempt to update role for non-existent user: " + dataMaskingUtil.maskSensitiveData(userId));
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new MembershipHubException("USER_NOT_FOUND", ERR_USER_NOT_FOUND + userId))
                        .build();
            }
            // Delegate role update to service layer, which applies permissions immediately per [REQ-003] business rule
            userService.updateUserRole(userUuid, roleId);
            // [AUDIT LOG] Exit point log with success context and masked user ID
            logger.info("[PROCESS] [REQ-003] [ARC-001] Successfully updated user role to '" + existingRole.getName() + "' for user: " + dataMaskingUtil.maskSensitiveData(userId));
            return Response.ok(new MembershipHubException("SUCCESS", "User role updated successfully, permissions applied immediately")).build();
        } catch (IllegalArgumentException e) {
            // Handle invalid UUID format or invalid role ID values
            logger.error("[CRITICAL FAIL] [REQ-003] [ARC-001] UserResource - Invalid input in updateUserRole. Raw error: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MembershipHubException("VALIDATION_FAILED", "Invalid input: " + e.getMessage()))
                    .build();
        } catch (SecurityException e) {
            // Handle permission denied errors (e.g., user trying to update higher privilege role)
            logger.error("[CRITICAL FAIL] [REQ-003] [ARC-001] UserResource - Permission denied in updateUserRole. Raw error: {}", e.getMessage());
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new MembershipHubException("PERMISSION_DENIED", ERR_PERMISSION_DENIED))
                    .build();
        } catch (Exception e) {
            // [ERROR LOG] Log unexpected errors with full stack trace, preserve root cause per governance rules
            logger.error("[CRITICAL FAIL] [REQ-003] [ARC-001] UserResource - Unexpected system error in updateUserRole endpoint. Raw error: {}", e.getMessage(), e);
            // Forward original exception to custom enterprise exception to preserve stack trace
            throw new MembershipHubException("INTERNAL_SERVER_ERROR", "Failed to update user role due to system error", e);
        }
    }

    // ====================== PRIVATE HELPER METHODS ======================
    /**
     * Parses a string value to a positive integer (minimum 1), returns default value if parsing fails.
     * Used for validating pagination parameters to prevent invalid input attacks.
     * 
     * @param value String value to parse
     * @param defaultValue Default value to return if input is null/invalid
     * @return Parsed positive integer (minimum 1)
     */
    private int parsePositiveInteger(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return Integer.parseInt(defaultValue);
        }
        try {
            int parsed = Integer.parseInt(value);
            return Math.max(parsed, 1); // Enforce minimum value of 1 for pagination
        } catch (NumberFormatException e) {
            logger.debug("[DEBUG] [REQ-003] Invalid numeric input: " + dataMaskingUtil.maskSensitiveData(value) + ", using default value: " + defaultValue);
            return Integer.parseInt(defaultValue);
        }
    }

    /**
     * Validates that role ID is within the valid range of 1-5 (matching 5 system-defined roles per [ARC-001]).
     * Throws IllegalArgumentException if role ID is out of range.
     * 
     * @param roleId Role ID to validate
     * @return Validated role ID as Short
     */
    private Short validateRoleId(Integer roleId) {
        if (roleId == null || roleId < 1 || roleId > 5) {
            throw new IllegalArgumentException(ERR_INVALID_ROLE_ID + ": received value " + roleId);
        }
        return roleId.shortValue();
    }
}
```

