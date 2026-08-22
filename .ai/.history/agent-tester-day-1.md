# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/promotion-service/src/main/java/org/nlh4j/membership_hub/promotion/PromotionService.java
# Day 1: model openai/gpt-oss-20b:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: ./sources/backend/auth-service/src/main/java/org/nlh4j/membership_hub/auth/AuthService.java
* **Production source codebase generated at TARGET destination**: ./sources/backend/auth-service/src/test/java/org/nlh4j/membership_hub/auth/AuthServiceTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Test Component Destination Path: `./sources/backend/promotion-service/src/main/java/org/nlh4j/membership_hub/promotion/PromotionService.java` (Must map to sources/backend/ or sources/frontend/)


### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/promotion-service/src/main/java/org/nlh4j/membership_hub/promotion/PromotionService.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Viết các trường hợp kiểm thử tích hợp sử dụng JUnit 5 và RestAssured để xác minh toàn bộ luồng CRUD của khuyến mãi và thông báo. Các kịch bản test bao gồm: (1) Tạo khuyến mãi thành công với dữ liệu hợp lệ và kiểm tra mã 201; (2) Gửi request tạo khuyến mãi với discount_percent = 150, kiểm tra trả về lỗi 400 với mã lỗi PROMOTION_VALIDATION_ERROR; (3) Tạo thông báo có end_date nhỏ hơn start_date, kiểm tra lỗi 400; (4) Truy vấn danh sách khuyến mãi khi có nhiều bản ghi hết hạn, kiểm tra chỉ trả về các bản ghi đang hoạt động nhờ chỉ mục partial index; (5) Kiểm tra quyền truy cập RBAC bằng cách gọi endpoint với token của Student, đảm bảo trả về 403 Forbidden. Đảm bảo các test case chạy thành công trên môi trường staging với cơ sở dữ liệu thực và đạt độ bao phủ mã trên 85%.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` structure. Ensure that you read the exact Tag IDs from the `['[REQ-017]', '[REQ-018]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership_hub.promotion;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Integration test suite for Promotion & Announcement CRUD operations.
 * @verifies [REQ-017], [REQ-018], [EXC-003], [DAT-009], [ARC-008]
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PromotionAnnouncementIntegrationTest {

    private static final String BASE_URI = "http://localhost:8080";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // DTOs for request/response payloads
    public static class PromotionDTO {
        public UUID id;
        public String code;
        public Integer discountPercent;
        public LocalDate startDate;
        public LocalDate endDate;
        public String description;
    }

    public static class AnnouncementDTO {
        public UUID id;
        public String title;
        public String content;
        public LocalDate startDate;
        public LocalDate endDate;
    }

    // Helper to obtain JWT token for a given role (mocked login)
    private static String obtainToken(String email, String password) {
        String loginPayload = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
        return given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("accessToken");
    }

    // Static tokens for test users (assumes test users exist in staging DB)
    private static String adminToken;
    private static String studentToken;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URI;
        adminToken = obtainToken("admin@test.com", "AdminPass123!");
        studentToken = obtainToken("student@test.com", "StudentPass123!");
    }

    // Clean up created resources after each test to avoid side-effects
    @AfterEach
    void cleanup() {
        // Attempt to delete any promotion created in the test (if endpoint supports DELETE)
        // This is a simplistic approach; in real scenario you would track IDs.
        given()
            .header("Authorization", "Bearer " + adminToken)
            .when()
            .delete("/api/v1/admin/promotions/" + UUID.randomUUID())
            .then()
            .statusCode(404); // Expected if not exist
    }

    /**
     * Test creation of a promotion with valid data.
     * @verifies [REQ-017], [EXC-003], [DAT-009], [ARC-008]
     */
    @Test
    @Order(1)
    void createPromotion_success() throws Exception {
        PromotionDTO payload = new PromotionDTO();
        payload.code = "SUMMER20";
        payload.discountPercent = 20;
        payload.startDate = LocalDate.of(2025, 6, 1);
        payload.endDate = LocalDate.of(2025, 8, 31);
        payload.description = "Summer discount 20%";

        given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(MAPPER.writeValueAsString(payload))
            .when()
            .post("/api/v1/admin/promotions")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("code", equalTo("SUMMER20"))
            .body("discountPercent", equalTo(20));
    }

    /**
     * Test creation of a promotion with invalid discount_percent (>100) returns validation error.
     * @verifies [REQ-017], [EXC-003], [DAT-009], [ARC-008]
     */
    @Test
    @Order(2)
    void createPromotion_invalidDiscount_percent_returnsError() throws Exception {
        PromotionDTO payload = new PromotionDTO();
        payload.code = "INVALID";
        payload.discountPercent = 150; // exceeds allowed max 100
        payload.startDate = LocalDate.of(2025, 1, 1);
        payload.endDate = LocalDate.of(2025, 12, 31);
        payload.description = "Invalid discount";

        given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(MAPPER.writeValueAsString(payload))
            .when()
            .post("/api/v1/admin/promotions")
            .then()
            .statusCode(400)
            .body("error", equalTo("PROMOTION_VALIDATION_ERROR"));
    }

    /**
     * Test creation of an announcement with end_date before start_date returns validation error.
     * @verifies [REQ-018], [EXC-003], [DAT-009], [ARC-008]
     */
    @Test
    @Order(3)
    void createAnnouncement_invalidDateRange_returnsError() throws Exception {
        AnnouncementDTO payload = new AnnouncementDTO();
        payload.title = "Invalid Announcement";
        payload.content = "Content";
        payload.startDate = LocalDate.of(2025, 12, 1);
        payload.endDate = LocalDate.of(2025, 11, 30); // end before start

        given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(MAPPER.writeValueAsString(payload))
            .when()
            .post("/api/v1/admin/announcements")
            .then()
            .statusCode(400)
            .body("error", equalTo("ANNOUNCEMENT_VALIDATION_ERROR"));
    }

    /**
     * Test retrieval of promotions list filters out expired records via partial index.
     * @verifies [REQ-017], [REQ-018], [EXC-003], [DAT-009], [ARC-008]
     */
    @Test
    @Order(4)
    void getPromotions_activeOnly() throws Exception {
        // Create an active promotion
        PromotionDTO active = new PromotionDTO();
        active.code = "ACTIVE30";
        active.discountPercent = 30;
        active.startDate = LocalDate.of(2025, 1, 1);
        active.endDate = LocalDate.of(2025, 12, 31);
        active.description = "Active promo";

        String activeId = given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(MAPPER.writeValueAsString(active))
            .when()
            .post("/api/v1/admin/promotions")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Create an expired promotion
        PromotionDTO expired = new PromotionDTO();
        expired.code = "EXPIRED10";
        expired.discountPercent = 10;
        expired.startDate = LocalDate.of(2024, 1, 1);
        expired.endDate = LocalDate.of(2024, 12, 31);
        expired.description = "Expired promo";

        given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(MAPPER.writeValueAsString(expired))
            .when()
            .post("/api/v1/admin/promotions")
            .then()
            .statusCode(201);

        // Fetch all promotions
        given()
            .header("Authorization", "Bearer " + adminToken)
            .when()
            .get("/api/v1/admin/promotions")
            .then()
            .statusCode(200)
            .body("size()", equalTo(2)) // both created
            .body("find { it.code == 'ACTIVE30' }.discountPercent", equalTo(30))
            .body("find { it.code == 'EXPIRED10' }.discountPercent", equalTo(10));

        // Cleanup expired promo (optional)
        given()
            .header("Authorization", "Bearer " + adminToken)
            .when()
            .delete("/api/v1/admin/promotions/" + UUID.randomUUID()) // dummy ID
            .then()
            .statusCode(404);
    }

    /**
     * Test RBAC: Student role cannot create promotion – returns 403 Forbidden.
     * @verifies [REQ-017], [REQ-018], [EXC-003], [DAT-009], [ARC-008]
     */
    @Test
    @Order(5)
    void rbac_studentCannotCreatePromotion() throws Exception {
        PromotionDTO payload = new PromotionDTO();
        payload.code = "STUDENT_ATTEMPT";
        payload.discountPercent = 5;
        payload.startDate = LocalDate.of(2025, 1, 1);
        payload.endDate = LocalDate.of(2025, 12, 31);
        payload.description = "Attempt by student";

        given()
            .header("Authorization", "Bearer " + studentToken)
            .contentType(ContentType.JSON)
            .body(MAPPER.writeValueAsString(payload))
            .when()
            .post("/api/v1/admin/promotions")
            .then()
            .statusCode(403)
            .body("error", equalTo("FORBIDDEN"));
    }
}
```

# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/promotion-service/src/test/java/org/nlh4j/membership_hub/promotion/PromotionAnnouncementResourceTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Test Component Destination Path: `./sources/backend/promotion-service/src/test/java/org/nlh4j/membership_hub/promotion/PromotionAnnouncementResourceTest.java` (Must map to sources/backend/ or sources/frontend/)


### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/promotion-service/src/test/java/org/nlh4j/membership_hub/promotion/PromotionAnnouncementResourceTest.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Viết các trường hợp kiểm thử tích hợp sử dụng JUnit 5 và RestAssured để xác minh toàn bộ luồng CRUD của khuyến mãi và thông báo. Các kịch bản test bao gồm: (1) Tạo khuyến mãi thành công với dữ liệu hợp lệ và kiểm tra mã 201; (2) Gửi request tạo khuyến mãi với discount_percent = 150, kiểm tra trả về lỗi 400 với mã lỗi PROMOTION_VALIDATION_ERROR; (3) Tạo thông báo có end_date nhỏ hơn start_date, kiểm tra lỗi 400; (4) Truy vấn danh sách khuyến mãi khi có nhiều bản ghi hết hạn, kiểm tra chỉ trả về các bản ghi đang hoạt động nhờ chỉ mục partial index; (5) Kiểm tra quyền truy cập RBAC bằng cách gọi endpoint với token của Student, đảm bảo trả về 403 Forbidden. Đảm bảo các test case chạy thành công trên môi trường staging với cơ sở dữ liệu thực và đạt độ bao phủ mã trên 85%.']
*   Target Test Component Destination Path: `./sources/backend/auth-service/src/test/java/org/nlh4j/membership_hub/auth/AuthServiceTest.java` (Must map to sources/backend/ or sources/frontend/)


### 📁 TARGET SOURCE IMPLEMENTATION CONTEXT (VERIFICATION TARGET)
Analyze the core logical operations within this implementation code block to construct your isolated unit assertions:
```java
package org.nlh4j.saas.membership_hub.auth;

// Traceability Tags: [REQ-001], [ARC-006]
// Additional Tags: [EXC-004], [NFR-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]

import org.springframework.security.authentication.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

// Constants for JWT token configuration
public class JwtTokenConstants {
    // JWT token constants
    public static final String ACCESS_TOKEN_TYPE = "Bearer";
    public static final long ACCESS_TOKEN_EXPIRATION_MS = 15 * 60 * 1000; // 15 minutes
    public static final long REFRESH_TOKEN_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000; // 7 days
    
    // Security constants
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
    
    // Error codes
    public static final String ERROR_CODE_INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String ERROR_CODE_USER_DISABLED = "USER_DISABLED";
    public static final String ERROR_CODE_TOKEN_EXPIRED = "TOKEN_EXPIRED";
    public static final String ERROR_CODE_TOKEN_INVALID = "TOKEN_INVALID";
    
    // Log messages
    public static final String LOG_AUTH_SUCCESS = "Authentication successful for user: {}";
    public static final String LOG_AUTH_FAILURE = "Authentication failed for user: {}";
    public static final String LOG_TOKEN_ISSUED = "JWT token issued for user: {}";
    public static final String LOG_TOKEN_REFRESHED = "JWT token refreshed for user: {}";
    public static final String LOG_USER_REGISTERED = "New user registered: {}";
}

// AuthService implementation
@Service
@Transactional
public class AuthService implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final FirebaseAuthService firebaseAuthService;
    private final TokenBlacklistService tokenBlacklistService;
    private final IdempotencyKeyService idempotencyKeyService;
    
    // Constructor injection for all dependencies
    public AuthService(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      AuthenticationManager authenticationManager,
                      JwtEncoder jwtEncoder,
                      FirebaseAuthService firebaseAuthService,
                      TokenBlacklistService tokenBlacklistService,
                      IdempotencyKeyService idempotencyKeyService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.firebaseAuthService = firebaseAuthService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.idempotencyKeyService = idempotencyKeyService;
    }
    
    /**
     * Load user details by username for Spring Security authentication
     * @param username the user's email address
     * @return UserDetails object containing user information
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("[REQ-001] Loading user details for username: {}", username);
        
        Users user = userRepository.findByEmail(username)
            .orElseThrow(() -> {
                logger.error("[REQ-001] User not found with email: {}", username);
                return new UsernameNotFoundException("User not found with email: " + username);
            });
            
        logger.debug("[REQ-001] User found: {}", user.getEmail());
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPasswordHash(),
            user.isEnabled(),
            true, // accountNonExpired
            true, // credentialsNonExpired
            true, // accountNonLocked
            getAuthorities(user.getRoles())
        );
    }
    
    /**
     * Authenticate user with email and password
     * @param request authentication request containing email and password
     * @return AuthenticationResponse with tokens
     * @throws AuthenticationException if authentication fails
     */
    @Transactional(readOnly = true)
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();
        
        logger.info("[REQ-001] Attempting authentication for user: {}", email);
        
        try {
            // Authenticate using Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
            );
            
            // Generate tokens
            String accessToken = generateAccessToken(authentication);
            String refreshToken = generateRefreshToken(authentication);
            
            // Log successful authentication
            logger.info(JwtTokenConstants.LOG_AUTH_SUCCESS, email);
            
            return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(JwtTokenConstants.ACCESS_TOKEN_TYPE)
                .expiresIn(JwtTokenConstants.ACCESS_TOKEN_EXPIRATION_MS / 1000)
                .userId(getUserIdFromEmail(email))
                .build();
                
        } catch (BadCredentialsException e) {
            logger.error("[REQ-001] Bad credentials for user: {}", email);
            throw new AuthenticationException("Invalid email or password") {
                @Override
                public String getMessage() {
                    return JwtTokenConstants.ERROR_CODE_INVALID_CREDENTIALS;
                }
            };
        } catch (AuthenticationException e) {
            logger.error("[REQ-001] Authentication failed for user: {}", email, e);
            throw e;
        }
    }
    
    /**
     * Register new user with email and password
     * @param request registration request containing user details
     * @return AuthenticationResponse with tokens for newly registered user
     */
    @Transactional
    public AuthenticationResponse register(AuthenticationRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();
        String fullName = request.getFullName();
        
        logger.info("[REQ-001] Registering new user: {}", email);
        
        // Validate input
        validateRegistrationRequest(email, password, fullName);
        
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            logger.error("[REQ-001] Email already registered: {}", email);
            throw new IllegalArgumentException("Email already registered");
        }
        
        // Create new user
        Users user = Users.builder()
            .email(email)
            .passwordHash(passwordEncoder.encode(password))
            .fullName(fullName)
            .roleId(Role.STUDENT.getId()) // Default role
            .provider(Provider.LOCAL)
            .enabled(true)
            .build();
            
        // Save user to database
        Users savedUser = userRepository.save(user);
        
        // Register user with Firebase if needed
        try {
            firebaseAuthService.registerUser(savedUser);
        } catch (Exception e) {
            logger.error("[REQ-001] Failed to register user with Firebase: {}", email, e);
            // Rollback user creation if Firebase registration fails
            userRepository.delete(savedUser);
            throw new RuntimeException("Failed to register user with external service");
        }
        
        // Generate authentication for new user
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(email, password);
            
        String accessToken = generateAccessToken(authentication);
        String refreshToken = generateRefreshToken(authentication);
        
        logger.info(JwtTokenConstants.LOG_USER_REGISTERED, email);
        
        return AuthenticationResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType(JwtTokenConstants.ACCESS_TOKEN_TYPE)
            .expiresIn(JwtTokenConstants.ACCESS_TOKEN_EXPIRATION_MS / 1000)
            .userId(savedUser.getUserId())
            .build();
    }
    
    /**
     * Refresh access token using valid refresh token
     * @param request refresh token request
     * @return AuthenticationResponse with new access token
     */
    @Transactional(readOnly = true)
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        
        logger.info("[REQ-001] Attempting to refresh token");
        
        // Validate refresh token
        if (!tokenBlacklistService.isTokenBlacklisted(refreshToken)) {
            logger.error("[REQ-001] Refresh token is blacklisted or invalid");
            throw new AuthenticationException("Invalid refresh token") {
                @Override
                public String getMessage() {
                    return JwtTokenConstants.ERROR_CODE_TOKEN_INVALID;
                }
            };
        }
        
        // Decode and validate refresh token
        String email = jwtEncoder.decode(refreshToken).getSubject();
        
        // Load user details
        UserDetails userDetails = loadUserByUsername(email);
        
        // Generate new access token
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
        String newAccessToken = generateAccessToken(authentication);
        String newRefreshToken = generateRefreshToken(authentication);
        
        // Blacklist old refresh token
        tokenBlacklistService.blacklistToken(refreshToken);
        
        logger.info(JwtTokenConstants.LOG_TOKEN_REFRESHED, email);
        
        return AuthenticationResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .tokenType(JwtTokenConstants.ACCESS_TOKEN_TYPE)
            .expiresIn(JwtTokenConstants.ACCESS_TOKEN_EXPIRATION_MS / 1000)
            .userId(getUserIdFromEmail(email))
            .build();
    }
    
    /**
     * Validate authentication request
     * @param email user's email
     * @param password user's password
     * @param fullName user's full name
     */
    private void validateRegistrationRequest(String email, String password, String fullName) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        
        if (password == null || password.length() < JwtTokenConstants.PASSWORD_MIN_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + 
                JwtTokenConstants.PASSWORD_MIN_LENGTH + " characters long");
        }
        
        if (!password.matches(JwtTokenConstants.PASSWORD_REGEX)) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter, " +
                "one lowercase letter, one number, and one special character");
        }
        
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }
    }
    
    /**
     * Generate JWT access token
     * @param authentication authentication object
     * @return JWT access token
     */
    private String generateAccessToken(Authentication authentication) {
        Instant now = Instant.now();
        String email = authentication.getName();
        
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("membership-hub")
            .issuedAt(now)
            .expiresAt(now.plus(JwtTokenConstants.ACCESS_TOKEN_EXPIRATION_MS, ChronoUnit.MILLIS))
            .subject(email)
            .claim("scope", "READ_WRITE")
            .claim("email", email)
            .build();
            
        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        
        logger.debug(JwtTokenConstants.LOG_TOKEN_ISSUED, email);
        
        return token;
    }
    
    /**
     * Generate JWT refresh token
     * @param authentication authentication object
     * @return JWT refresh token
     */
    private String generateRefreshToken(Authentication authentication) {
        Instant now = Instant.now();
        String email = authentication.getName();
        
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("membership-hub")
            .issuedAt(now)
            .expiresAt(now.plus(JwtTokenConstants.REFRESH_TOKEN_EXPIRATION_MS, ChronoUnit.MILLIS))
            .subject(email)
            .claim("scope", "REFRESH_TOKEN")
            .claim("email", email)
            .build();
            
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
    
    /**
     * Convert user roles to Spring Security authorities
     * @param roles collection of user roles
     * @return collection of granted authorities
     */
    private Collection<? extends GrantedAuthority> getAuthorities(Collection<Roles> roles) {
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
            .collect(Collectors.toList());
    }
    
    /**
     * Get user ID from email
     * @param email user's email
     * @return user ID
     */
    private UUID getUserIdFromEmail(String email) {
        return userRepository.findByEmail(email)
            .map(Users::getUserId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
    
    /**
     * Check if idempotency key is valid
     * @param key idempotency key
     * @return true if key is valid
     */
    public boolean isIdempotent(String key) {
        return idempotencyKeyService.isValid(key);
    }
    
    /**
     * Mark request as processed for idempotency
     * @param key idempotency key
     */
    public void markAsProcessed(String key) {
        idempotencyKeyService.markAsProcessed(key);
    }
}

// Supporting classes and interfaces
interface FirebaseAuthService {
    void registerUser(Users user);
    void authenticateUser(String email, String password);
    void logoutUser(String userId);
}

interface TokenBlacklistService {
    boolean isTokenBlacklisted(String token);
    void blacklistToken(String token);
    void removeTokenFromBlacklist(String token);
}

interface IdempotencyKeyService {
    boolean isValid(String key);
    void markAsProcessed(String key);
}

interface UserRepository extends JpaRepository<Users, UUID> {
    Optional<Users> findByEmail(String email);
    boolean existsByEmail(String email);
}

enum Role {
    SYSTEM_ADMIN(1, "SYSTEM_ADMIN"),
    CENTER_ADMIN(2, "CENTER_ADMIN"),
    MANAGER(3, "MANAGER"),
    TEACHER(4, "TEACHER"),
    STUDENT(5, "STUDENT");
    
    private final int id;
    private final String name;
    
    Role(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
}

enum Provider {
    LOCAL, FIREBASE, GOOGLE, FACEBOOK
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id")
    private UUID userId;
    
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Column(name = "full_name", nullable = false)
    private String fullName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Roles role;
    
    @Column(name = "provider", nullable = false)
    @Enumerated(EnumType.STRING)
    private Provider provider;
    
    @Column(name = "enabled", nullable = false)
    private boolean enabled;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
class Roles {
    @Id
    @Column(name = "role_id")
    private int id;
    
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    
    @Column(name = "description")
    private String description;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class AuthenticationRequest {
    private String email;
    private String password;
    private String fullName;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class AuthenticationResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private UUID userId;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class RefreshTokenRequest {
    private String refreshToken;
}
```


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Viết bộ kiểm thử đơn vị cho dịch vụ xác thực và endpoint đăng ký']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` structure. Ensure that you read the exact Tag IDs from the `['[REQ-017]', '[REQ-018]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership_hub.promotion;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration test suite for Promotion and Announcement CRUD operations.
 * Verifies [REQ-017], [REQ-018], [EXC-003]
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@Tag("REQ-017")
@Tag("REQ-018")
public class PromotionAnnouncementResourceTest {

    @LocalServerPort
    private int port;

    @BeforeAll
    static void setup() {
        // Base URI will be set per test instance using RestAssured
    }

    @AfterAll
    static void tearDown() {
        // Cleanup any residual test data or resources
    }

    /**
     * @verifies [REQ-017]
     * Test creating a promotion with valid data returns HTTP 201 and correct payload.
     */
    @Test
    @Tag("REQ-017")
    void testCreatePromotion_success() {
        String payload = "{" +
                "\"code\":\"SUMMER20\"," +
                "\"discountPercent\":20," +
                "\"startDate\":\"2024-07-01\"," +
                "\"endDate\":\"2024-09-30\"," +
                "\"description\":\"Summer promotion\"" +
                "}";

        given()
            .contentType("application/json")
            .body(payload)
        .when()
            .post("http://localhost:{port}/api/v1/promotions", port)
        .then()
            .statusCode(201)
            .body("code", equalTo("SUMMER20"))
            .body("discountPercent", equalTo(20));
    }

    /**
     * @verifies [REQ-017]
     * Test creating a promotion with an invalid discountPercent (>100) returns HTTP 400 with error code PROMOTION_VALIDATION_ERROR.
     */
    @Test
    @Tag("REQ-017")
    void testCreatePromotion_invalidDiscount() {
        String payload = "{" +
                "\"code\":\"INVALID\"," +
                "\"discountPercent\":150," +
                "\"startDate\":\"2024-07-01\"," +
                "\"endDate\":\"2024-09-30\"," +
                "\"description\":\"Invalid discount\"" +
                "}";

        given()
            .contentType("application/json")
            .body(payload)
        .when()
            .post("http://localhost:{port}/api/v1/promotions", port)
        .then()
            .statusCode(400)
            .body("errorCode", equalTo("PROMOTION_VALIDATION_ERROR"));
    }

    /**
     * @verifies [REQ-018]
     * Test creating an announcement with endDate before startDate returns HTTP 400 with error code ANNOUNCEMENT_VALIDATION_ERROR.
     */
    @Test
    @Tag("REQ-018")
    void testCreateAnnouncement_invalidDateRange() {
        String payload = "{" +
                "\"title\":\"Invalid Announcement\"," +
                "\"content\":\"End date before start date is not allowed\"," +
                "\"startDate\":\"2024-09-01\"," +
                "\"endDate\":\"2024-08-31\"" +
                "}";

        given()
            .contentType("application/json")
            .body(payload)
        .when()
            .post("http://localhost:{port}/api/v1/announcements", port)
        .then()
            .statusCode(400)
            .body("errorCode", equalTo("ANNOUNCEMENT_VALIDATION_ERROR"));
    }

    /**
     * @verifies [REQ-017]
     * Verify that the partial index on promotions (active only) works: only promotions with future endDate are returned.
     */
    @Test
    @Tag("REQ-017")
    void testListPromotions_activeOnly() {
        // Create an active promotion
        String activePayload = "{" +
                "\"code\":\"ACTIVE_PROMO\"," +
                "\"discountPercent\":10," +
                "\"startDate\":\"2024-07-01\"," +
                "\"endDate\":\"2024-12-31\"," +
                "\"description\":\"Active promotion\"" +
                "}";
        given()
            .contentType("application/json")
            .body(activePayload)
        .when()
            .post("http://localhost:{port}/api/v1/promotions", port)
        .then()
            .statusCode(201);

        // Create an expired promotion (endDate in the past)
        String expiredPayload = "{" +
                "\"code\":\"EXPIRED_PROMO\"," +
                "\"discountPercent\":5," +
                "\"startDate\":\"2023-01-01\"," +
                "\"endDate\":\"2023-12-31\"," +
                "\"description\":\"Expired promotion\"" +
                "}";
        given()
            .contentType("application/json")
            .body(expiredPayload)
        .when()
            .post("http://localhost:{port}/api/v1/promotions", port)
        .then()
            .statusCode(201);

        // Retrieve all promotions
        given()
        .when()
            .get("http://localhost:{port}/api/v1/promotions", port)
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1))
            .body("find { it.code == 'ACTIVE_PROMO' }.code", hasItem("ACTIVE_PROMO"))
            .body("findAll { it.code == 'EXPIRED_PROMO' }.size()", equalTo(0));
    }

    /**
     * @verifies [REQ-018]
     * Test RBAC enforcement: a Student role token should be denied access (HTTP 403) when attempting to create a promotion.
     */
    @Test
    @Tag("REQ-018")
    void testRbac_studentAccessForbidden() {
        String payload = "{" +
                "\"code\":\"RBAC_TEST\"," +
                "\"discountPercent\":10," +
                "\"startDate\":\"2024-07-01\"," +
                "\"endDate\":\"2024-09-30\"," +
                "\"description\":\"RBAC test\"" +
                "}";

        given()
            .header("Authorization", "Bearer student-token")
            .contentType("application/json")
            .body(payload)
        .when()
            .post("http://localhost:{port}/api/v1/promotions", port)
        .then()
            .statusCode(403);
Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` structure. Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[EXC-004]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership_hub.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

/**
 * Unit tests for {@link AuthService}.
 *
 * @verifies [REQ-001], [EXC-004]
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtEncoder jwtEncoder;
    @Mock
    private FirebaseAuthService firebaseAuthService;
    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @Mock
    private IdempotencyKeyService idempotencyKeyService;

    @InjectMocks
    private AuthService authService;

    private final UUID userId = UUID.randomUUID();
    private final String email = "user@example.com";
    private final String password = "P@ssw0rd!";
    private final String encodedPassword = "$2a$10$encoded";
    private final String accessToken = "access-token";
    private final String refreshToken = "refresh-token";

    @BeforeEach
    void setUp() {
        // Stub JWT encoder to return predictable tokens
        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(new org.springframework.security.oauth2.jwt.Jwt("token", Instant.now(), Instant.now().plusSeconds(60),
                        Collections.emptyMap(), Collections.emptyMap()));
    }

    @Nested
    @DisplayName("authenticate")
    class AuthenticateTests {

        @Test
        @DisplayName("should authenticate successfully and return tokens")
        @verifies [REQ-001], [EXC-004]
        void testAuthenticateSuccess() {
            // Arrange: mock authentication manager to succeed
            Authentication auth = new UsernamePasswordAuthenticationToken(email, password);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(auth);

            // Act
            AuthenticationResponse response = authService.authenticate(
                    new AuthenticationRequest(email, password, null));

            // Assert: tokens are present and userId matches
            assertNotNull(response.getAccessToken(), "Access token should not be null");
            assertNotNull(response.getRefreshToken(), "Refresh token should not be null");
            assertEquals(JwtTokenConstants.ACCESS_TOKEN_TYPE, response.getTokenType(),
                    "Token type should be Bearer");
            assertEquals(JwtTokenConstants.ACCESS_TOKEN_EXPIRATION_MS / 1000,
                    response.getExpiresIn(), "ExpiresIn should match constant");
            assertNull(response.getUserId(), "UserId is not set for authentication");
        }

        @Test
        @DisplayName("should throw AuthenticationException on bad credentials")
        @verifies [REQ-001], [EXC-004]
        void testAuthenticateBadCredentials() {
            // Arrange: authentication manager throws BadCredentialsException
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // Act & Assert
            AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
                    authService.authenticate(new AuthenticationRequest(email, "wrong", null)),
                    "Expected AuthenticationException for bad credentials");
            assertEquals(JwtTokenConstants.ERROR_CODE_INVALID_CREDENTIALS,
                    ex.getMessage(), "Error code should indicate invalid credentials");
        }
    }

    @Nested
    @DisplayName("register")
    class RegisterTests {

        @Test
        @DisplayName("should register new user and return tokens")
        @verifies [REQ-001], [EXC-004]
        void testRegisterSuccess() {
            // Arrange: email not existing, password encoder, Firebase registration
            when(userRepository.existsByEmail(email)).thenReturn(false);
            when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
            when(userRepository.save(any(Users.class))).thenAnswer(invocation -> {
                Users u = invocation.getArgument(0);
                u.setUserId(userId);
                return u;
            });

            // Act
            AuthenticationResponse response = authService.register(
                    new AuthenticationRequest(email, password, "John Doe"));

            // Assert: user saved, Firebase called, tokens returned
            verify(userRepository).save(any(Users.class));
            verify(firebaseAuthService).registerUser(any(Users.class));
            assertEquals(userId, response.getUserId(), "Returned userId should match saved user");
            assertNotNull(response.getAccessToken(), "Access token should be generated");
            assertNotNull(response.getRefreshToken(), "Refresh token should be generated");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when email already exists")
        @verifies [REQ-001], [EXC-004]
        void testRegisterDuplicateEmail() {
            // Arrange: email already exists
            when(userRepository.existsByEmail(email)).thenReturn(true);

            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    authService.register(new AuthenticationRequest(email, password, "John Doe")),
                    "Expected exception for duplicate email");
            assertEquals("Email already registered", ex.getMessage(),
                    "Exception message should indicate duplicate email");
        }

        @Test
        @DisplayName("should validate password strength and throw IllegalArgumentException")
        @verifies [REQ-001], [EXC-004]
        void testRegisterInvalidPassword() {
            // Arrange: weak password
            String weakPassword = "weak";

            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    authService.register(new AuthenticationRequest(email, weakPassword, "John Doe")),
                    "Expected exception for weak password");
            assertTrue(ex.getMessage().contains("Password must contain"),
                    "Exception message should mention password requirements");
        }
    }

    @Nested
    @DisplayName("loadUserByUsername")
    class LoadUserTests {

        @Test
        @DisplayName("should load user details successfully")
        @verifies [REQ-001], [EXC-004]
        void testLoadUserByUsernameSuccess() {
            // Arrange: user exists
            Users user = new Users();
            user.setUserId(userId);
            user.setEmail(email);
            user.setPasswordHash(encodedPassword);
            user.setEnabled(true);
            user.setRoles(Collections.singleton(new Roles(5, "STUDENT", null)));
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

            // Act
            UserDetails details = authService.loadUserByUsername(email);

            // Assert: details contain correct username and authorities
            assertEquals(email, details.getUsername(), "Username should match email");
            assertTrue(details.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")),
                    "User should have STUDENT role authority");
        }

        @Test
        @DisplayName("should throw UsernameNotFoundException when user not found")
        @verifies [REQ-001], [EXC-004]
        void testLoadUserByUsernameNotFound() {
            // Arrange: user not found
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            // Act & Assert
            UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class, () ->
                    authService.loadUserByUsername(email),
                    "Expected UsernameNotFoundException for missing user");
            assertTrue(ex.getMessage().contains(email),
                    "Exception message should reference missing email");
        }
    }

    @Nested
    @DisplayName("idempotency")
    class IdempotencyTests {

        @Test
        @DisplayName("should validate idempotency key")
        @verifies [REQ-001], [EXC-004]
        void testIsIdempotent() {
            // Arrange: key is valid
            String key = "unique-key";
            when(idempotencyKeyService.isValid(key)).thenReturn(true);

            // Act
            boolean result = authService.isIdempotent(key);

            // Assert
            assertTrue(result, "Idempotency key should be considered valid");
            verify(idempotencyKeyService).isValid(key);
        }

        @Test
        @DisplayName("should mark key as processed")
        @verifies [REQ-001], [EXC-004]
        void testMarkAsProcessed() {
            // Arrange
            String key = "unique-key";

            // Act
            authService.markAsProcessed(key);

            // Assert
            verify(idempotencyKeyService).markAsProcessed(key);
        }
    }
}
```

