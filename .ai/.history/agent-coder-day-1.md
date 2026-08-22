# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/migrations/V1__init_user_center_schema.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Component Destination Path: `./sources/backend/migrations/V1__init_user_center_schema.sql`
*   Traceability Audit Tags For This Task: ['[DAT-001]', '[DAT-003]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Viết script migration ANSI SQL chuẩn để tạo 3 bảng: roles, users, centers']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[DAT-001]', '[DAT-003]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
-- [DAT-001] [DAT-003] Enterprise traceability tags for initial user and center schema migration
-- This migration script creates the foundational relational schema for the membership-hub system.
-- It defines three core tables: roles, users, and centers, with appropriate constraints, indexes, and data validation rules.
-- All constraints enforce data integrity, security, and business rules as per the architectural specification.

-- ------------------------------------------------------------
-- Table: roles
-- Purpose: Stores system roles used for RBAC (Role-Based Access Control).
-- ------------------------------------------------------------
CREATE TABLE roles (
    role_id SMALLINT PRIMARY KEY,                     -- Unique identifier for the role (e.g., 1=System Admin)
    name VARCHAR(30) NOT NULL UNIQUE,                -- Role name (e.g., 'System Admin', 'Center Admin')
    description VARCHAR(200)                         -- Optional description of the role's responsibilities
);

-- ------------------------------------------------------------
-- Table: users
-- Purpose: Stores user account information, authentication details, and role assignments.
-- ------------------------------------------------------------
CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- Globally unique identifier for the user
    email VARCHAR(255) NOT NULL UNIQUE,               -- User's email address (must be unique)
    password_hash CHAR(60) NOT NULL,                 -- Hashed password using bcrypt (60-char output)
    full_name VARCHAR(100) NOT NULL,                 -- Full name of the user
    role_id SMALLINT NOT NULL REFERENCES roles(role_id), -- Foreign key to roles.role_id (enforces referential integrity)
    provider VARCHAR(20) NOT NULL DEFAULT 'local',   -- Authentication provider: 'local', 'firebase', 'google', or 'facebook'
    CHECK (provider IN ('local', 'firebase', 'google', 'facebook')), -- Validate allowed provider values
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Record creation timestamp
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP  -- Record last modification timestamp
);

-- Indexes for the users table to optimize frequent query patterns
CREATE INDEX idx_users_email ON users(email);       -- Fast lookup by email (e.g., login, duplicate check)
CREATE INDEX idx_users_role_id ON users(role_id);    -- Fast lookup by role for RBAC queries

-- ------------------------------------------------------------
-- Table: centers
-- Purpose: Stores information about each service center (tenant) in the multi‑center deployment.
-- ------------------------------------------------------------
CREATE TABLE centers (
    center_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- Unique identifier for the center
    name VARCHAR(100) NOT NULL,                         -- Center name
    address VARCHAR(255) NOT NULL,                      -- Physical address of the center
    tax_id VARCHAR(13) NOT NULL UNIQUE,                 -- Tax identification number (must be unique across centers)
    CHECK (tax_id ~ '^[0-9]{10,13}$'),                  -- Validate tax_id contains only digits and length 10‑13
    contact_phone VARCHAR(20),                          -- Optional contact phone number
    contact_email VARCHAR(255),                         -- Optional contact email (basic format validation)
    CHECK (contact_email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$') -- Simple email format check
);

-- Index for the centers table to accelerate tax_id lookups (e.g., validation, reporting)
CREATE INDEX idx_centers_tax_id ON centers(tax_id);
```

# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/auth-service/src/main/java/org/nlh4j/membership_hub/auth/AuthService.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Component Destination Path: `./sources/backend/auth-service/src/main/java/org/nlh4j/membership_hub/auth/AuthService.java`
*   Traceability Audit Tags For This Task: ['[REQ-001]', '[ARC-006]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai logic xác thực email/mật khẩu và cấp phát JWT token']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[ARC-006]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
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

# Day 1: model kilo-auto/free - API Endpoint https://api.kilo.ai/api/gateway
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/auth-service/src/main/java/org/nlh4j/membership_hub/auth/AuthResource.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Component Destination Path: `./sources/backend/auth-service/src/main/java/org/nlh4j/membership_hub/auth/AuthResource.java`
*   Traceability Audit Tags For This Task: ['[REQ-001]', '[EXC-004]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Xây dựng endpoint đăng ký và đăng nhập người dùng']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[EXC-004]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership_hub.auth;

// -------------------------- IMPORTS (Enterprise Standard) --------------------------
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import org.nlh4j.saas.membership_hub.service.AuthService;
import org.nlh4j.saas.membership_hub.service.UserService;
import org.nlh4j.saas.membership_hub.util.PasswordEncoder;
import org.nlh4j.saas.membership_hub.util.JwtUtils;
import org.nlh4j.saas.membership_hub.util.RedisService;
import org.nlh4j.saas.membership_hub.exception.UnauthorizedException;

import java.util.*;
import java.util.regex.Pattern;

/**
 * REST Resource for core user authentication operations (registration and local email/password login).
 * Implements requirement [REQ-001] (user registration with JWT token issuance) and handles exception [EXC-004] (input validation failures).
 * Follows enterprise security rules: input validation, password hashing, idempotency key support, PII masking, and OWASP Top 10 compliance.
 *
 * @traceability [REQ-001], [EXC-004]
 */
@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    // -------------------------- LOGGER INITIALIZATION (Per Enterprise Logging Rules [0.3]) --------------------------
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthResource.class);

    // -------------------------- TOP-OF-CLASS CONSTANTS (Per Anti-Magic-Numbers Rule [0.2]) --------------------------
    // Endpoint path constants (no hardcoded strings in logic)
    public static final String REGISTER_ENDPOINT = "/register";
    public static final String LOGIN_ENDPOINT = "/login";
    public static final String REFRESH_ENDPOINT = "/refresh";

    // Error code constants (standardized across enterprise system)
    public static final String ERROR_VALIDATION_FAILED = "VALIDATION_INPUT_INVALID";
    public static final String ERROR_EMAIL_EXISTS = "EMAIL_ALREADY_EXISTS";
    public static final String ERROR_INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String ERROR_IDEMPOTENCY_KEY_MISSING = "IDEMPOTENCY_KEY_MISSING";

    // Error message constants (no hardcoded strings in logic)
    public static final String MESSAGE_EMAIL_REQUIRED = "Email is required";
    public static final String MESSAGE_EMAIL_INVALID = "Email format is invalid";
    public static final String MESSAGE_PASSWORD_REQUIRED = "Password is required";
    public static final String MESSAGE_PASSWORD_WEAK = "Password must be at least 8 characters, contain uppercase, lowercase, number and special character";
    public static final String MESSAGE_FULL_NAME_REQUIRED = "Full name is required";
    public static final String MESSAGE_FULL_NAME_TOO_LONG = "Full name must not exceed 100 characters";
    public static final String MESSAGE_REGISTRATION_SUCCESS = "User registered successfully";
    public static final String MESSAGE_LOGIN_SUCCESS = "Login successful";

    // Validation regex patterns (centralized for maintainability)
    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    public static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    // Token configuration (per [ARC-006] JWT specification)
    public static final int ACCESS_TOKEN_EXPIRY_MINUTES = 15;
    public static final int REFRESH_TOKEN_EXPIRY_DAYS = 7;
    public static final String DEFAULT_NEW_USER_ROLE = "Student"; // Per [REQ-001] default role for new users

    // Idempotency configuration (per mutation API security rules [1.0])
    public static final long IDEMPOTENCY_KEY_TTL_SECONDS = 24 * 3600; // 24 hours cache TTL

    // -------------------------- DEPENDENCY INJECTION (Quarkus CDI) --------------------------
    @Inject
    AuthService authService;

    @Inject
    UserService userService;

    @Inject
    PasswordEncoder passwordEncoder; // BCrypt encoder per tech stack [ARC-010]

    @Inject
    JwtUtils jwtUtils; // JWT utility per [ARC-006]

    @Inject
    RedisService redisService; // Redis caching per [ARC-009] for idempotency keys

    // -------------------------- REST ENDPOINTS --------------------------
    /**
     * Registers a new user with email/password credentials.
     * Validates input, checks for duplicate email, creates user with default Student role,
     * issues JWT access token (15min expiry) and refresh token (7 days expiry, stored in HttpOnly secure cookie).
     * Supports idempotency via Idempotency-Key header to prevent duplicate registration requests.
     *
     * @param request Registration request payload
     * @param idempotencyKey Idempotency key from request header (required for mutation API per security rules)
     * @return 201 Created response with auth tokens and user info
     * @traceability [REQ-001]
     */
    @POST
    @Path(REGISTER_ENDPOINT)
    public Response register(RegisterRequest request, @HeaderParam("Idempotency-Key") String idempotencyKey) {
        // Log entry point per enterprise logging rules [0.3], mask PII (email) per [NFR-003]
        LOGGER.info("[PROCESS] [REQ-001] Starting user registration for email: {}", maskEmail(request.email()));

        try {
            // -------------------------- IDEMPOTENCY KEY VALIDATION (Per Mutation API Security Rules [1.0]) --------------------------
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                String errorMsg = "Idempotency key is missing for registration request";
                LOGGER.error("[CRITICAL FAIL] [EXC-004] {} Raw error: Missing Idempotency-Key header", errorMsg);
                throw new ValidationException(ERROR_IDEMPOTENCY_KEY_MISSING, errorMsg);
            }

            // Check if idempotency key was already processed to prevent duplicate registrations
            String cachedResponse = redisService.get(idempotencyKey);
            if (cachedResponse != null) {
                LOGGER.info("[PROCESS] [REQ-001] Duplicate registration request detected with idempotency key: {}", maskIdempotencyKey(idempotencyKey));
                return Response.ok(cachedResponse).build();
            }

            // -------------------------- INPUT VALIDATION (Per [REQ-001] Business Rules) --------------------------
            List<FieldError> fieldErrors = validateRegistrationRequest(request);
            if (!fieldErrors.isEmpty()) {
                String errorMsg = "Registration input validation failed";
                LOGGER.error("[CRITICAL FAIL] [EXC-004] {} Field errors: {}", errorMsg, fieldErrors);
                throw new ValidationException(ERROR_VALIDATION_FAILED, errorMsg, fieldErrors);
            }

            // -------------------------- DUPLICATE EMAIL CHECK (Unique Constraint) --------------------------
            if (userService.existsByEmail(request.email())) {
                String errorMsg = "Email already registered in system";
                LOGGER.error("[CRITICAL FAIL] [REQ-001] {} Email: {}", errorMsg, maskEmail(request.email()));
                throw new ConflictException(ERROR_EMAIL_EXISTS, errorMsg);
            }

            // -------------------------- PASSWORD HASHING (BCrypt per Tech Stack [ARC-010]) --------------------------
            // Never store plain text passwords per security rules [NFR-003]
            String passwordHash = passwordEncoder.encode(request.password());

            // -------------------------- USER CREATION (Default Student Role per [REQ-001]) --------------------------
            User newUser = userService.createUser(
                request.email(),
                passwordHash,
                request.fullName(),
                DEFAULT_NEW_USER_ROLE
            );

            // -------------------------- JWT TOKEN GENERATION (Per [ARC-006] Specification) --------------------------
            String accessToken = jwtUtils.generateAccessToken(newUser.getUserId(), newUser.getRole(), ACCESS_TOKEN_EXPIRY_MINUTES);
            String refreshToken = jwtUtils.generateRefreshToken(newUser.getUserId(), REFRESH_TOKEN_EXPIRY_DAYS);

            // -------------------------- RESPONSE BUILDING (Mask PII in response per [NFR-003]) --------------------------
            AuthResponse response = new AuthResponse(
                newUser.getUserId().toString(),
                maskEmail(newUser.getEmail()),
                newUser.getRole(),
                accessToken,
                refreshToken
            );

            // Cache response for idempotency key to prevent duplicate processing
            redisService.setex(idempotencyKey, response.toJson(), IDEMPOTENCY_KEY_TTL_SECONDS);

            // Set refresh token as HttpOnly secure cookie per security rules [NFR-003]
            NewCookie refreshCookie = new NewCookie.Builder("refreshToken")
                    .value(refreshToken)
                    .httpOnly(true) // Prevent XSS token theft
                    .secure(true) // Enforce HTTPS only in production
                    .sameSite(NewCookie.SameSite.STRICT) // Prevent CSRF attacks
                    .path(REFRESH_ENDPOINT)
                    .maxAge(REFRESH_TOKEN_EXPIRY_DAYS * 24 * 3600)
                    .build();

            // Log exit point per [0.3]
            LOGGER.info("[PROCESS] [REQ-001] User registration completed successfully for userId: {}", newUser.getUserId());
            return Response.status(Response.Status.CREATED)
                    .entity(response)
                    .cookie(refreshCookie)
                    .build();

        } catch (ValidationException | ConflictException e) {
            // Re-throw business exceptions with original cause per exception cause chain preservation rule [1.0]
            throw e;
        } catch (Exception e) {
            // Log unexpected errors with required 3 context keys per [0.3]: module name, raw error, tag ID
            LOGGER.error("[CRITICAL FAIL] [REQ-001] [EXC-004] User registration failed. Module: AuthResource. Raw error: {}", e.getMessage(), e);
            throw new InternalServerErrorException("Registration failed due to system error", e);
        }
    }

    /**
     * Authenticates user with email/password credentials.
     * Validates input, verifies password hash against stored BCrypt hash, issues JWT tokens if valid.
     *
     * @param request Login request payload
     * @return 200 OK response with auth tokens and user info
     * @traceability [REQ-001]
     */
    @POST
    @Path(LOGIN_ENDPOINT)
    public Response login(LoginRequest request) {
        // Log entry point per [0.3], mask PII (email)
        LOGGER.info("[PROCESS] [REQ-001] Starting user login for email: {}", maskEmail(request.email()));

        try {
            // -------------------------- INPUT VALIDATION --------------------------
            List<FieldError> fieldErrors = validateLoginRequest(request);
            if (!fieldErrors.isEmpty()) {
                String errorMsg = "Login input validation failed";
                LOGGER.error("[CRITICAL FAIL] [EXC-004] {} Field errors: {}", errorMsg, fieldErrors);
                throw new ValidationException(ERROR_VALIDATION_FAILED, errorMsg, fieldErrors);
            }

            // -------------------------- USER LOOKUP --------------------------
            User user = userService.findByEmail(request.email());
            if (user == null) {
                String errorMsg = "Invalid email or password";
                LOGGER.error("[CRITICAL FAIL] [REQ-001] {} Email: {}", errorMsg, maskEmail(request.email()));
                throw new UnauthorizedException(ERROR_INVALID_CREDENTIALS, errorMsg);
            }

            // -------------------------- PASSWORD VERIFICATION (BCrypt) --------------------------
            if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                String errorMsg = "Invalid email or password";
                LOGGER.error("[CRITICAL FAIL] [REQ-001] {} Email: {}", errorMsg, maskEmail(request.email()));
                throw new UnauthorizedException(ERROR_INVALID_CREDENTIALS, errorMsg);
            }

            // -------------------------- TOKEN GENERATION --------------------------
            String accessToken = jwtUtils.generateAccessToken(user.getUserId(), user.getRole(), ACCESS_TOKEN_EXPIRY_MINUTES);
            String refreshToken = jwtUtils.generateRefreshToken(user.getUserId(), REFRESH_TOKEN_EXPIRY_DAYS);

            // Build response with masked PII
            AuthResponse response = new AuthResponse(
                    user.getUserId().toString(),
                    maskEmail(user.getEmail()),
                    user.getRole(),
                    accessToken,
                    refreshToken
            );

            // Set refresh token as HttpOnly secure cookie
            NewCookie refreshCookie = new NewCookie.Builder("refreshToken")
                    .value(refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite(NewCookie.SameSite.STRICT)
                    .path(REFRESH_ENDPOINT)
                    .maxAge(REFRESH_TOKEN_EXPIRY_DAYS * 24 * 3600)
                    .build();

            // Log exit point per [0.3]
            LOGGER.info("[PROCESS] [REQ-001] User login completed successfully for userId: {}", user.getUserId());
            return Response.ok(response)
                    .cookie(refreshCookie)
                    .build();

        } catch (ValidationException | UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("[CRITICAL FAIL] [REQ-001] [EXC-004] User login failed. Module: AuthResource. Raw error: {}", e.getMessage(), e);
            throw new InternalServerErrorException("Login failed due to system error", e);
        }
    }

    // -------------------------- PRIVATE HELPER METHODS --------------------------
    /**
     * Validates registration request input against business rules and security requirements.
     * Checks email format, password complexity, and full name constraints.
     *
     * @param request Registration request to validate
     * @return List of field errors, empty if input is valid
     * @traceability [REQ-001], [EXC-004]
     */
    private List<FieldError> validateRegistrationRequest(RegisterRequest request) {
        List<FieldError> errors = new ArrayList<>();

        // Validate email field (required, valid format)
        if (request.email() == null || request.email().isBlank()) {
            errors.add(new FieldError("email", MESSAGE_EMAIL_REQUIRED));
        } else if (!EMAIL_PATTERN.matcher(request.email()).matches()) {
            errors.add(new FieldError("email", MESSAGE_EMAIL_INVALID));
        }

        // Validate password strength (per NFR-003 security rules: min 8 chars, mixed case, number, special char)
        if (request.password() == null || request.password().isBlank()) {
            errors.add(new FieldError("password", MESSAGE_PASSWORD_REQUIRED));
        } else if (!PASSWORD_PATTERN.matcher(request.password()).matches()) {
            errors.add(new FieldError("password", MESSAGE_PASSWORD_WEAK));
        }

        // Validate full name (required, max 100 chars per [REQ-001])
        if (request.fullName() == null || request.fullName().isBlank()) {
            errors.add(new FieldError("fullName", MESSAGE_FULL_NAME_REQUIRED));
        } else if (request.fullName().length() > 100) {
            errors.add(new FieldError("fullName", MESSAGE_FULL_NAME_TOO_LONG));
        }

        return errors;
    }

    /**
     * Validates login request input fields.
     *
     * @param request Login request to validate
     * @return List of field errors, empty if input is valid
     * @traceability [REQ-001], [EXC-004]
     */
    private List<FieldError> validateLoginRequest(LoginRequest request) {
        List<FieldError> errors = new ArrayList<>();

        if (request.email() == null || request.email().isBlank()) {
            errors.add(new FieldError("email", MESSAGE_EMAIL_REQUIRED));
        } else if (!EMAIL_PATTERN.matcher(request.email()).matches()) {
            errors.add(new FieldError("email", MESSAGE_EMAIL_INVALID));
        }

        if (request.password() == null || request.password().isBlank()) {
            errors.add(new FieldError("password", MESSAGE_PASSWORD_REQUIRED));
        }

        return errors;
    }

    /**
     * Masks email address for logging to comply with PII masking rules [NFR-003]
     * Example: john.doe@example.com -> j***n.d***@e*****e.com
     *
     * @param email Raw email address
     * @return Masked email address safe for logging
     */
    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "***";
        String[] parts = email.split("@");
        if (parts.length != 2) return "***";
        String localPart = parts[0];
        String domainPart = parts[1];
        String maskedLocal = localPart.length() > 2 ? localPart.substring(0, 2) + "***" : localPart.substring(0, 1) + "***";
        String maskedDomain = domainPart.length() > 2 ? domainPart.substring(0, 2) + "***" : domainPart.substring(0, 1) + "***";
        return maskedLocal + "@" + maskedDomain;
    }

    /**
     * Masks idempotency key for logging to prevent sensitive data exposure
     *
     * @param key Raw idempotency key
     * @return Masked idempotency key
     */
    private String maskIdempotencyKey(String key) {
        if (key == null || key.isBlank()) return "***";
        return key.length() > 8 ? key.substring(0, 4) + "***" + key.substring(key.length() - 4) : "***";
    }

    // -------------------------- INNER DTO RECORDS (Immutable, Type-Safe) --------------------------
    /**
     * Registration request DTO (immutable record for type safety)
     * @traceability [REQ-001]
     */
    public record RegisterRequest(String email, String password, String fullName) {}

    /**
     * Login request DTO (immutable record for type safety)
     * @traceability [REQ-001]
     */
    public record LoginRequest(String email, String password) {}

    /**
     * Authentication response DTO (immutable record for type safety)
     * @traceability [REQ-001]
     */
    public record AuthResponse(
            String userId,
            String email,
            String role,
            String accessToken,
            String refreshToken
    ) {
        // Convert to JSON string for idempotency key caching in Redis
        public String toJson() {
            return "{\"userId\":\"" + userId + "\",\"email\":\"" + email + "\",\"role\":\"" + role + "\",\"accessToken\":\"" + accessToken + "\",\"refreshToken\":\"" + refreshToken + "\"}";
        }
    }

    /**
     * Field error DTO for validation error responses
     * @traceability [EXC-004]
     */
    public record FieldError(String field, String message) {}

    // -------------------------- CUSTOM EXCEPTIONS --------------------------
    /**
     * Custom validation exception per [EXC-004] for input validation failures
     */
    public static class ValidationException extends RuntimeException {
        private final String errorCode;
        private final List<FieldError> fieldErrors;

        public ValidationException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
            this.fieldErrors = Collections.emptyList();
        }

        public ValidationException(String errorCode, String message, List<FieldError> fieldErrors) {
            super(message);
            this.errorCode = errorCode;
            this.fieldErrors = fieldErrors != null ? fieldErrors : Collections.emptyList();
        }

        public String getErrorCode() { return errorCode; }
        public List<FieldError> getFieldErrors() { return fieldErrors; }
    }

    /**
     * Custom conflict exception for duplicate resource (e.g. duplicate email)
     */
    public static class ConflictException extends RuntimeException {
        private final String errorCode;

        public ConflictException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() { return errorCode; }
    }
}

// -------------------------- EXCEPTION MAPPERS (Per Enterprise Error Handling Rules) --------------------------
/**
 * Maps ValidationException to 400 Bad Request response per [EXC-004]
 */
@Provider
class ValidationExceptionMapper implements ExceptionMapper<AuthResource.ValidationException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationExceptionMapper.class);

    @Override
    public Response toResponse(AuthResource.ValidationException e) {
        // Log error with required 3 keys per [0.3]: module name, raw error, tag ID
        LOGGER.error("[CRITICAL FAIL] [EXC-004] Validation error in AuthResource. Raw error: {}", e.getMessage(), e);
        ErrorResponse errorResponse = new ErrorResponse(e.getErrorCode(), e.getMessage(), e.getFieldErrors());
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build();
    }
}

/**
 * Maps ConflictException to 409 Conflict response for duplicate email
 */
@Provider
class ConflictExceptionMapper implements ExceptionMapper<AuthResource.ConflictException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConflictExceptionMapper.class);

    @Override
    public Response toResponse(AuthResource.ConflictException e) {
        LOGGER.error("[CRITICAL FAIL] [REQ-001] Conflict error in AuthResource. Raw error: {}", e.getMessage(), e);
        ErrorResponse errorResponse = new ErrorResponse(e.getErrorCode(), e.getMessage(), Collections.emptyList());
        return Response.status(Response.Status.CONFLICT)
                .entity(errorResponse)
                .build();
    }
}

/**
 * Maps UnauthorizedException to 401 Unauthorized response for invalid login credentials
 */
@Provider
class UnauthorizedExceptionMapper implements ExceptionMapper<UnauthorizedException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnauthorizedExceptionMapper.class);

    @Override
    public Response toResponse(UnauthorizedException e) {
        LOGGER.error("[CRITICAL FAIL] [REQ-001] Unauthorized access attempt in AuthResource. Raw error: {}", e.getMessage(), e);
        ErrorResponse errorResponse = new ErrorResponse(e.getErrorCode(), e.getMessage(), Collections.emptyList());
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(errorResponse)
                .build();
    }
}

/**
 * Generic error response DTO for all authentication errors
 */
record ErrorResponse(String error, String message, List<AuthResource.FieldError> fieldErrors) {}
```

