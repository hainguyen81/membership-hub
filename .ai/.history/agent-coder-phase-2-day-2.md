# Day 2: model models/gemini-flash-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/SocialAuthService.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/SocialAuthService.java`
*   Traceability Audit Tags For This Task: ['[REQ-002]', '[ARC-006]', '[NFR-003]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo tệp mã nguồn ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/SocialAuthService.java hiện thực hóa lớp SocialAuthService được tiêm SocialTokenVerifier và UserSocialAccountRepository. Triển khai phương thức authenticateWithSocial(SocialAuthRequest request) thực hiện theo trình tự: (1) xác minh ID token với provider tương ứng thông qua SocialTokenVerifier.verify(), (2) trích xuất email và provider_user_id từ SocialUserInfo, (3) tìm kiếm bản ghi trong bảng UserSocialAccounts theo (provider, provider_user_id), (4) nếu chưa tồn tại thì tạo mới user với role mặc định Student (role_id=5) và liên kết social account, (5) cập nhật profile_picture_url nếu có, (6) gọi JwtTokenProvider.generateAccessToken() và generateRefreshToken() trả về AuthResponse với cờ isNewUser. Toàn bộ thao tác phải sử dụng @Transactional và ghi audit log thông qua AuthAuditLogger. Xử lý UnsupportedProviderException trả về HTTP 400 với mã UNSUPPORTED_PROVIDER, InvalidTokenException trả về HTTP 401 với mã INVALID_SOCIAL_TOKEN.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-002]', '[ARC-006]', '[NFR-003]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.membershiphub.userservice.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Social Authentication Service.
 * <p>
 * Handles federated authentication for external Identity Providers (Firebase, Google, Facebook),
 * account linkage, automatic role assignment, and JWT issuance.
 * </p>
 *
 * @traceability [REQ-002], [ARC-006], [NFR-003]
 */
@ApplicationScoped
public class SocialAuthService {

    // [REQ-002] Static Constants Declaration (Top-of-Class Constants Law)
    private static final Logger LOGGER = LoggerFactory.getLogger(SocialAuthService.class);
    
    // [ARC-006] Role identifiers and system constants
    public static final short DEFAULT_STUDENT_ROLE_ID = 5;
    public static final String DEFAULT_STUDENT_ROLE_NAME = "STUDENT";
    public static final String DEFAULT_PASSWORD_PLACEHOLDER = "OAUTH2_FEDERATED_AUTHENTICATION_NOPASSWORD";
    public static final String DEFAULT_FALLBACK_FULL_NAME = "Social Member";
    
    // [REQ-002] Error and log format templates
    public static final String ERROR_CODE_UNSUPPORTED_PROVIDER = "UNSUPPORTED_PROVIDER";
    public static final String ERROR_CODE_INVALID_SOCIAL_TOKEN = "INVALID_SOCIAL_TOKEN";
    public static final String ERROR_CODE_PROCESSING_FAILED = "SOCIAL_AUTH_PROCESSING_FAILED";
    public static final String AUDIT_ACTION_SOCIAL_LOGIN_SUCCESS = "SOCIAL_AUTH_SUCCESS";
    public static final String AUDIT_ACTION_SOCIAL_USER_CREATED = "SOCIAL_USER_CREATED";
    public static final String AUDIT_ACTION_SOCIAL_LOGIN_FAILED = "SOCIAL_AUTH_FAILED";

    // [REQ-002] Subsystem identification label for structured error logging
    private static final String SUBSYSTEM_NAME = "USER_SERVICE_SOCIAL_AUTH";

    private final SocialTokenVerifier socialTokenVerifier;
    private final UserSocialAccountRepository userSocialAccountRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthAuditLogger authAuditLogger;

    /**
     * CDI Constructor Injection.
     */
    @Inject
    public SocialAuthService(
            final SocialTokenVerifier socialTokenVerifier,
            final UserSocialAccountRepository userSocialAccountRepository,
            final UserRepository userRepository,
            final RoleRepository roleRepository,
            final JwtTokenProvider jwtTokenProvider,
            final AuthAuditLogger authAuditLogger) {
        this.socialTokenVerifier = Objects.requireNonNull(socialTokenVerifier, "SocialTokenVerifier must not be null");
        this.userSocialAccountRepository = Objects.requireNonNull(userSocialAccountRepository, "UserSocialAccountRepository must not be null");
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository must not be null");
        this.roleRepository = Objects.requireNonNull(roleRepository, "RoleRepository must not be null");
        this.jwtTokenProvider = Objects.requireNonNull(jwtTokenProvider, "JwtTokenProvider must not be null");
        this.authAuditLogger = Objects.requireNonNull(authAuditLogger, "AuthAuditLogger must not be null");
    }

    /**
     * Authenticates a user using third-party social provider tokens.
     * Synchronizes user profile, links identity accounts, and issues session tokens.
     *
     * @param request the social authentication request payload containing provider and idToken.
     * @return the authentication response including access token, refresh token, and user metadata.
     * @throws UnsupportedProviderException if the provider name is not supported.
     * @throws InvalidTokenException if the provider token is expired or structurally invalid.
     */
    @Transactional
    public AuthResponse authenticateWithSocial(final SocialAuthRequest request) {
        // [NFR-003] Entry Gate Logging with zero PII exposure
        LOGGER.info("[PROCESS_ENTRY] [REQ-002] Initiating social authentication workflow for provider: {}", 
                request != null ? request.getProvider() : "null");

        if (request == null || request.getProvider() == null || request.getIdToken() == null) {
            LOGGER.error("[VALIDATION_ERROR] [REQ-002] Null payload or missing mandatory fields in SocialAuthRequest");
            throw new InvalidTokenException("Invalid request payload: provider and idToken are mandatory.");
        }

        final String normalizedProvider = request.getProvider().trim().toLowerCase(Locale.ROOT);

        try {
            // [REQ-002] Step 1: Verify token with corresponding provider via SocialTokenVerifier
            final SocialUserInfo socialUserInfo = this.socialTokenVerifier.verify(normalizedProvider, request.getIdToken());
            if (socialUserInfo == null || socialUserInfo.getProviderId() == null) {
                LOGGER.error("[VERIFICATION_FAILED] [REQ-002] Token verification returned null or empty provider ID for provider: {}", normalizedProvider);
                throw new InvalidTokenException("Unable to extract valid user identity from social token.");
            }

            // [REQ-002] Step 2: Extract verified identity attributes
            final String providerUserId = socialUserInfo.getProviderId().trim();
            final String rawEmail = socialUserInfo.getEmail();
            final String sanitizedEmail = (rawEmail != null && !rawEmail.isBlank()) 
                    ? rawEmail.trim().toLowerCase(Locale.ROOT) 
                    : String.format("%s_%s@social.membershiphub.internal", normalizedProvider, providerUserId);

            final String fullName = (socialUserInfo.getFullName() != null && !socialUserInfo.getFullName().isBlank())
                    ? socialUserInfo.getFullName().trim()
                    : DEFAULT_FALLBACK_FULL_NAME;

            // Optional profile picture update from client payload or provider claim
            final String resolvedProfilePicture = (request.getProfilePicture() != null && !request.getProfilePicture().isBlank())
                    ? request.getProfilePicture().trim()
                    : socialUserInfo.getProfilePictureUrl();

            // [REQ-002] Step 3: Search for existing linked account by (provider, provider_user_id)
            final Optional<UserSocialAccount> existingLink = this.userSocialAccountRepository.findByProviderAndProviderUserId(
                    normalizedProvider, 
                    providerUserId
            );

            final User targetUser;
            final boolean isNewUser;

            if (existingLink.isPresent()) {
                // User is already linked
                isNewUser = false;
                final UserSocialAccount socialAccount = existingLink.get();
                targetUser = socialAccount.getUser();

                if (targetUser == null) {
                    LOGGER.error("[DATA_INTEGRITY_FAIL] [REQ-002] Orphaned UserSocialAccount found with ID: {}", socialAccount.getSocialAccountId());
                    throw new SocialAuthProcessingException(ERROR_CODE_PROCESSING_FAILED, "Linked social account has no associated primary user record.");
                }

                // [REQ-002] Step 5: Update profile picture URL on social account if changed
                if (resolvedProfilePicture != null && !resolvedProfilePicture.equals(socialAccount.getProfilePictureUrl())) {
                    socialAccount.setProfilePictureUrl(resolvedProfilePicture);
                    this.userSocialAccountRepository.persist(socialAccount);
                }

                LOGGER.debug("[USER_RESOLVED] [REQ-002] Existing user matched for social account ID: {}", socialAccount.getSocialAccountId());
            } else {
                // User is either totally new or exists with the same email and needs linkage
                final Optional<User> existingUserByEmail = this.userRepository.findByEmail(sanitizedEmail);

                if (existingUserByEmail.isPresent()) {
                    isNewUser = false;
                    targetUser = existingUserByEmail.get();
                    LOGGER.info("[ACCOUNT_LINKED] [REQ-002] Linking new provider [{}] to existing user ID: {}", normalizedProvider, targetUser.getUserId());
                } else {
                    // [REQ-002] Step 4: Provision new primary user record with default Student role (role_id=5)
                    isNewUser = true;
                    targetUser = new User();
                    targetUser.setUserId(UUID.randomUUID());
                    targetUser.setEmail(sanitizedEmail);
                    targetUser.setPasswordHash(DEFAULT_PASSWORD_PLACEHOLDER);
                    targetUser.setFullName(fullName);
                    targetUser.setProvider(normalizedProvider);
                    targetUser.setCreatedAt(LocalDateTime.now());
                    targetUser.setUpdatedAt(LocalDateTime.now());

                    final Role defaultStudentRole = this.roleRepository.findByIdOptional((long) DEFAULT_STUDENT_ROLE_ID)
                            .orElseGet(() -> {
                                LOGGER.warn("[ROLE_FALLBACK] [ARC-006] Role ID {} not found in database. Initializing default transient role.", DEFAULT_STUDENT_ROLE_ID);
                                final Role newRole = new Role();
                                newRole.setRoleId(DEFAULT_STUDENT_ROLE_ID);
                                newRole.setName(DEFAULT_STUDENT_ROLE_NAME);
                                return newRole;
                            });

                    targetUser.setRole(defaultStudentRole);
                    this.userRepository.persist(targetUser);

                    // Audit user provisioning
                    this.authAuditLogger.logAuthEvent(
                            targetUser.getUserId(),
                            AUDIT_ACTION_SOCIAL_USER_CREATED,
                            String.format("{\"provider\":\"%s\",\"isNew\":true}", normalizedProvider)
                    );
                }

                // [REQ-002] Step 4 & 5: Persist linkage record in UserSocialAccounts
                final UserSocialAccount newSocialAccount = new UserSocialAccount();
                newSocialAccount.setSocialAccountId(UUID.randomUUID());
                newSocialAccount.setUser(targetUser);
                newSocialAccount.setProvider(normalizedProvider);
                newSocialAccount.setProviderUserId(providerUserId);
                newSocialAccount.setProfilePictureUrl(resolvedProfilePicture);
                newSocialAccount.setLinkedAt(LocalDateTime.now());

                this.userSocialAccountRepository.persist(newSocialAccount);
            }

            // [REQ-002] Step 6: Generate Access Token (15 mins) and Refresh Token (7 days)
            final String roleName = (targetUser.getRole() != null && targetUser.getRole().getName() != null)
                    ? targetUser.getRole().getName()
                    : DEFAULT_STUDENT_ROLE_NAME;

            final String accessToken = this.jwtTokenProvider.generateAccessToken(
                    targetUser.getUserId().toString(),
                    roleName,
                    normalizedProvider
            );

            final String refreshToken = this.jwtTokenProvider.generateRefreshToken(
                    targetUser.getUserId().toString()
            );

            // Audit successful login event
            this.authAuditLogger.logAuthEvent(
                    targetUser.getUserId(),
                    AUDIT_ACTION_SOCIAL_LOGIN_SUCCESS,
                    String.format("{\"provider\":\"%s\",\"new\":%b}", normalizedProvider, isNewUser)
            );

            LOGGER.info("[PROCESS_EXIT] [REQ-002] Social authentication workflow completed successfully. isNewUser: {}", isNewUser);

            final AuthResponse response = new AuthResponse();
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setExpiresIn(900); // 15 minutes in seconds
            response.setUserId(targetUser.getUserId());
            response.setRole(roleName);
            response.setNewUser(isNewUser);

            return response;

        } catch (final UnsupportedProviderException upe) {
            // [0.1/0.3] Comprehensive Exception Logging with module name, raw error, and Tag ID
            LOGGER.error("[PROVIDER_ERROR] [{}] [{}] Unsupported social identity provider requested: {}. Raw error: {}",
                    SUBSYSTEM_NAME, "[REQ-002]", normalizedProvider, upe.getMessage());
            this.authAuditLogger.logAuthEvent(null, AUDIT_ACTION_SOCIAL_LOGIN_FAILED, "{\"reason\":\"UNSUPPORTED_PROVIDER\"}");
            throw upe;

        } catch (final InvalidTokenException ite) {
            // [0.1/0.3] Explicit Tag-preserved error audit
            LOGGER.error("[TOKEN_INVALID] [{}] [{}] Invalid or expired social identity token. Raw error: {}",
                    SUBSYSTEM_NAME, "[REQ-002]", ite.getMessage());
            this.authAuditLogger.logAuthEvent(null, AUDIT_ACTION_SOCIAL_LOGIN_FAILED, "{\"reason\":\"INVALID_SOCIAL_TOKEN\"}");
            throw ite;

        } catch (final Exception e) {
            // [0.1/0.3] Catch boundary with root cause stack preservation
            LOGGER.error("[CRITICAL_FAIL] [{}] [{}] Unexpected failure executing social authentication workflow. Raw error: {}",
                    SUBSYSTEM_NAME, "[REQ-002]", e.getMessage());
            this.authAuditLogger.logAuthEvent(null, AUDIT_ACTION_SOCIAL_LOGIN_FAILED, "{\"reason\":\"INTERNAL_EXCEPTION\"}");
            throw new SocialAuthProcessingException(ERROR_CODE_PROCESSING_FAILED, "Federated social authentication encountered an unhandled internal failure.", e);
        }
    }
}
```

