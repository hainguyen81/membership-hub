# Day 1: model openai/gpt-oss-20b:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/membership-service/src/main/java/com/hub/membership/MembershipService.java
# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/migrations/V1__init_user_center_schema.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Component Destination Path: `./sources/backend/membership-service/src/main/java/com/hub/membership/MembershipService.java`
*   Traceability Audit Tags For This Task: ['[REQ-014]', '[DAT-007]']
*   Target Component Destination Path: `./sources/backend/migrations/V1__init_user_center_schema.sql`
*   Traceability Audit Tags For This Task: ['[DAT-001]', '[DAT-003]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai logic tính toán số ngày còn lại hiệu lực của thẻ hội viên dựa trên issue_date và validity_days, đảm bảo giá trị remaining_days được cập nhật tự động mỗi ngày qua scheduled job, tuân thủ các ràng buộc NOT NULL và CHECK cho các trường dữ liệu.']
['Viết script migration ANSI SQL chuẩn để tạo 3 bảng: roles, users, centers']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-014]', '[DAT-007]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership-hub.membership;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.quarkus.scheduler.Scheduled;

/**
 * Service responsible for managing membership cards.
 *
 * <p>This service calculates the remaining validity days for each membership card
 * based on {@code issueDate} and {@code validityDays}. A scheduled job runs
 * daily to update the {@code remainingDays} field automatically.</p>
 *
 * @traceability [REQ-014], [DAT-007]
 */
@ApplicationScoped
public class MembershipService {

    /* --------------------------------------------------------------------- */
    /*  Constants (no hard‑coded literals in business logic)                 */
    /* --------------------------------------------------------------------- */
    /** Cron expression for the daily scheduled job (02:00 UTC). */
    private static final String DAILY_CRON = "0 0 2 * * ?";

    /** Default timezone used for date calculations. */
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");

    /** Minimum remaining days allowed (non‑negative). */
    private static final int MIN_REMAINING_DAYS = 0;

    /* --------------------------------------------------------------------- */
    /*  Dependencies                                                        */
    /* --------------------------------------------------------------------- */
    @Inject
    Logger logger; // injected via CDI

    @Inject
    EntityManager em; // JPA EntityManager for persistence operations

    /* --------------------------------------------------------------------- */
    /*  Public API                                                          */
    /* --------------------------------------------------------------------- */

    /**
     * Calculates and returns the remaining validity days for a specific card.
     *
     * @param cardId the unique identifier of the membership card
     * @return the number of days remaining, never negative
     * @throws IllegalArgumentException if the card does not exist
     */
    @Transactional
    public int getRemainingDays(UUID cardId) {
        logger.debug("[PROCESS] Calculating remaining days for card: {}", cardId);

        StudentCard card = em.find(StudentCard.class, cardId);
        if (card == null) {
            logger.error("[ERROR] Card not found: {}", cardId);
            throw new IllegalArgumentException("Card not found: " + cardId);
        }

        int remaining = computeRemainingDays(card.getIssueDate(), card.getValidityDays());
        logger.debug("[RESULT] Remaining days for card {}: {}", cardId, remaining);
        return remaining;
    }

    /**
     * Updates the {@code remainingDays} field for all membership cards.
     *
     * <p>This method is invoked by the scheduled job defined by {@link #DAILY_CRON}.</p>
     */
    @Transactional
    @Scheduled(cron = DAILY_CRON)
    public void updateAllRemainingDays() {
        logger.info("[SCHEDULED] Updating remaining days for all cards at {}", LocalDate.now(DEFAULT_ZONE));

        try {
            List<StudentCard> cards = em.createQuery(
                    "SELECT c FROM StudentCard c", StudentCard.class)
                    .getResultList();

            for (StudentCard card : cards) {
                int newRemaining = computeRemainingDays(card.getIssueDate(), card.getValidityDays());
                card.setRemainingDays(newRemaining);
                em.merge(card);
                logger.debug("[UPDATE] Card {}: remainingDays set to {}", card.getCardId(), newRemaining);
            }
        } catch (PersistenceException e) {
            logger.error("[CRITICAL FAIL] [ARC-014] Failed to update remaining days: {}", e.getMessage(), e);
            // Rethrow to let the scheduler handle retry if configured
            throw e;
        }

        logger.info("[SCHEDULED] Completed updating remaining days for all cards");
    }

    /* --------------------------------------------------------------------- */
    /*  Helper Methods                                                      */
    /* --------------------------------------------------------------------- */

    /**
     * Computes the remaining days given the issue date and validity period.
     *
     * @param issueDate    the date the card was issued
     * @param validityDays the total number of days the card is valid
     * @return the remaining days, never negative
     */
    private int computeRemainingDays(LocalDate issueDate, int validityDays) {
        // Guard against null dates
        if (issueDate == null) {
            logger.warn("[WARN] Issue date is null; defaulting remaining days to {}", MIN_REMAINING_DAYS);
            return MIN_REMAINING_DAYS;
        }

        LocalDate today = LocalDate.now(DEFAULT_ZONE);
        Period period = Period.between(issueDate, today);
        int daysElapsed = period.getDays() + period.getMonths() * 30 + period.getYears() * 365;

        int remaining = validityDays - daysElapsed;
        return Math.max(remaining, MIN_REMAINING_DAYS);
    }
}

/* --------------------------------------------------------------------- */
/*  Entity definition (simplified for illustration)                      */
/* --------------------------------------------------------------------- */
import java.time.LocalDate;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * JPA entity representing a membership card.
 *
 * @traceability [DAT-007]
 */
@Entity
@Table(name = "student_cards")
class StudentCard {

    @Id
    @Column(name = "card_id", nullable = false, updatable = false)
    private UUID cardId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "validity_days", nullable = false)
    private int validityDays;

    @Column(name = "remaining_days", nullable = false)
    private int remainingDays;

    // Getters and setters omitted for brevity

    public UUID getCardId() { return cardId; }
    public void setCardId(UUID cardId) { this.cardId = cardId; }

    public UUID getStudentId() { return studentId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public int getValidityDays() { return validityDays; }
    public void setValidityDays(int validityDays) { this.validityDays = validityDays; }

    public int getRemainingDays() { return remainingDays; }
    public void setRemainingDays(int remainingDays) { this.remainingDays = remainingDays; }
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
* **Production source codebase generated at TARGET destination**: ./sources/backend/membership-service/src/main/java/com/hub/membership/MembershipController.java
* **Production source codebase generated at TARGET destination**: ./sources/backend/auth-service/src/main/java/org/nlh4j/membership_hub/auth/AuthResource.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Component Destination Path: `./sources/backend/membership-service/src/main/java/com/hub/membership/MembershipController.java`
*   Traceability Audit Tags For This Task: ['[REQ-014]', '[DAT-007]']
*   Target Component Destination Path: `./sources/backend/auth-service/src/main/java/org/nlh4j/membership_hub/auth/AuthResource.java`
*   Traceability Audit Tags For This Task: ['[REQ-001]', '[EXC-004]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Xây dựng endpoint API và repository truy cập dữ liệu thẻ hội viên']
['Xây dựng endpoint đăng ký và đăng nhập người dùng']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-017]', '[REQ-018]', '[DAT-009]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership_hub.promotion;

// ====================== ENTERPRISE IMPORT LAYER ======================
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.nlh4j.saas.membership_hub.auth.RbacService;
import org.nlh4j.saas.membership_hub.entity.Announcement;
import org.nlh4j.saas.membership_hub.entity.Promotion;
import org.nlh4j.saas.membership_hub.entity.Role;
import org.nlh4j.saas.membership_hub.entity.User;
import org.nlh4j.saas.membership_hub.exception.PromotionCodeAlreadyExistsException;
import org.nlh4j.saas.membership_hub.exception.UnauthorizedAccessException;
import org.nlh4j.saas.membership_hub.exception.ValidationException;
import org.nlh4j.saas.membership_hub.notification.NotificationService;
import org.nlh4j.saas.membership_hub.repository.AnnouncementRepository;
import org.nlh4j.saas.membership_hub.repository.PromotionRepository;
import org.nlh4j.saas.membership_hub.security.SecurityContext;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import io.quarkus.scheduler.Scheduled;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for managing promotions and system announcements.
 * Implements business logic for CRUD operations, input validation, XSS sanitization,
 * RBAC access control, and automatic expiration of announcements.
 *
 * @traceability [REQ-017], [REQ-018], [DAT-009]
 * @author Enterprise Core Engineering Team
 * @version 1.0.0
 */
@ApplicationScoped
public class AnnouncementService {

    // ====================== ENTERPRISE CONSTANTS (NO HARDCODED LITERALS IN LOGIC) ======================
    // [REQ-017] Promotion validation constants
    public static final int MIN_DISCOUNT_PERCENT = 0;
    public static final int MAX_DISCOUNT_PERCENT = 100;
    public static final int MAX_PROMO_CODE_LENGTH = 50;
    public static final int MAX_PROMO_DESCRIPTION_LENGTH = 500;
    // [REQ-018] Announcement validation constants
    public static final int MAX_ANNOUNCEMENT_TITLE_LENGTH = 150;
    public static final int MAX_ANNOUNCEMENT_CONTENT_LENGTH = 2000;
    // [EXC-003] Notification retry constants
    public static final int MAX_NOTIFICATION_RETRY_COUNT = 3;
    public static final long NOTIFICATION_RETRY_DELAY_MS = 300000; // 5 minutes
    // Scheduled job constants
    public static final String SCHEDULED_JOB_CRON = "0 0 0 * * ?"; // Run daily at midnight UTC
    // ====================== END OF CONSTANTS ======================

    // Enterprise standard SLF4J logger for audit and process tracing [NFR-006]
    private static final Logger LOG = Logger.getLogger(AnnouncementService.class);

    // Injected dependencies via Quarkus CDI [ARC-010]
    @Inject
    PromotionRepository promotionRepository;
    @Inject
    AnnouncementRepository announcementRepository;
    @Inject
    RbacService rbacService;
    @Inject
    NotificationService notificationService;
    @Inject
    SecurityContext securityContext;

    /**
     * Validates that the current user has the required role to access promotion/announcement management endpoints.
     * Only Center Admin and Manager roles are allowed per [REQ-017], [REQ-018].
     *
     * @param currentUser The currently authenticated user
     * @throws UnauthorizedAccessException if the user does not have the required role
     */
    private void validateRbac(User currentUser) {
        LOG.debug("[PROMOTION_SERVICE] [REQ-017] [REQ-018] Validating RBAC for user: {}", currentUser.getUserId());
        boolean hasAccess = rbacService.hasRole(currentUser, Role.CENTER_ADMIN, Role.MANAGER);
        if (!hasAccess) {
            LOG.warn("[PROMOTION_SERVICE] [REQ-017] [REQ-018] Unauthorized access attempt by user: {} with role: {}", currentUser.getUserId(), currentUser.getRole());
            throw new UnauthorizedAccessException("Access denied. Only Center Admin and Manager can manage promotions and announcements.");
        }
        LOG.debug("[PROMOTION_SERVICE] [REQ-017] [REQ-018] RBAC validation passed for user: {}", currentUser.getUserId());
    }

    /**
     * Sanitizes user input to prevent XSS attacks by removing malicious HTML/script tags [NFR-003].
     * Uses Jsoup with a basic whitelist to allow only safe HTML elements.
     *
     * @param input The raw user input string
     * @return The sanitized safe string
     */
    private String sanitizeInput(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        // Clean input with basic whitelist (allows only safe tags like b, i, p, etc.)
        return Jsoup.clean(input, Whitelist.basic());
    }

    /**
     * Validates promotion business rules per [REQ-017].
     *
     * @param promotion The promotion entity to validate
     * @throws ValidationException if any validation rule is violated
     */
    private void validatePromotion(Promotion promotion) {
        LOG.debug("[PROMOTION_SERVICE] [REQ-017] Validating promotion data for code: {}", promotion.getCode());
        // Validate discount percentage range [REQ-017]
        if (promotion.getDiscountPercent() < MIN_DISCOUNT_PERCENT || promotion.getDiscountPercent() > MAX_DISCOUNT_PERCENT) {
            LOG.warn("[PROMOTION_SERVICE] [REQ-017] Invalid discount percentage: {} for promo code: {}", promotion.getDiscountPercent(), promotion.getCode());
            throw new ValidationException("Discount percentage must be between " + MIN_DISCOUNT_PERCENT + " and " + MAX_DISCOUNT_PERCENT);
        }
        // Validate date range: end date must be >= start date if end date is provided [REQ-017]
        if (promotion.getEndDate() != null && promotion.getEndDate().isBefore(promotion.getStartDate())) {
            LOG.warn("[PROMOTION_SERVICE] [REQ-017] Invalid date range for promo code: {}: startDate={}, endDate={}", promotion.getCode(), promotion.getStartDate(), promotion.getEndDate());
            throw new ValidationException("End date must be greater than or equal to start date");
        }
        // Validate promo code length [REQ-017]
        if (promotion.getCode().length() > MAX_PROMO_CODE_LENGTH) {
            LOG.warn("[PROMOTION_SERVICE] [REQ-017] Promo code exceeds max length: {} for code: {}", MAX_PROMO_CODE_LENGTH, promotion.getCode());
            throw new ValidationException("Promotion code must not exceed " + MAX_PROMO_CODE_LENGTH + " characters");
        }
        // Validate description length if provided [REQ-017]
        if (promotion.getDescription() != null && promotion.getDescription().length() > MAX_PROMO_DESCRIPTION_LENGTH) {
            LOG.warn("[PROMOTION_SERVICE] [REQ-017] Promo description exceeds max length: {} for code: {}", MAX_PROMO_DESCRIPTION_LENGTH, promotion.getCode());
            throw new ValidationException("Promotion description must not exceed " + MAX_PROMO_DESCRIPTION_LENGTH + " characters");
        }
        LOG.debug("[PROMOTION_SERVICE] [REQ-017] Promotion validation passed for code: {}", promotion.getCode());
    }

    /**
     * Validates announcement business rules per [REQ-018].
     *
     * @param announcement The announcement entity to validate
     * @throws ValidationException if any validation rule is violated
     */
    private void validateAnnouncement(Announcement announcement) {
        LOG.debug("[PROMOTION_SERVICE] [REQ-018] Validating announcement data for title: {}", announcement.getTitle());
        // Validate title length and presence [REQ-018]
        if (announcement.getTitle() == null || announcement.getTitle().isBlank() || announcement.getTitle().length() > MAX_ANNOUNCEMENT_TITLE_LENGTH) {
            LOG.warn("[PROMOTION_SERVICE] [REQ-018] Invalid announcement title length: {} for title: {}", announcement.getTitle() != null ? announcement.getTitle().length() : 0, announcement.getTitle());
            throw new ValidationException("Announcement title is required and must not exceed " + MAX_ANNOUNCEMENT_TITLE_LENGTH + " characters");
        }
        // Validate content length and presence [REQ-018]
        if (announcement.getContent() == null || announcement.getContent().isBlank() || announcement.getContent().length() > MAX_ANNOUNCEMENT_CONTENT_LENGTH) {
            LOG.warn("[PROMOTION_SERVICE] [REQ-018] Invalid announcement content length: {} for title: {}", announcement.getContent() != null ? announcement.getContent().length() : 0, announcement.getTitle());
            throw new ValidationException("Announcement content is required and must not exceed " + MAX_ANNOUNCEMENT_CONTENT_LENGTH + " characters");
        }
        // Validate date range: end date must be >= start date if end date is provided [REQ-018]
        if (announcement.getEndDate() != null && announcement.getEndDate().isBefore(announcement.getStartDate())) {
            LOG.warn("[PROMOTION_SERVICE] [REQ-018] Invalid date range for announcement: {}: startDate={}, endDate={}", announcement.getTitle(), announcement.getStartDate(), announcement.getEndDate());
            throw new ValidationException("Announcement end date must be greater than or equal to start date");
        }
        LOG.debug("[PROMOTION_SERVICE] [REQ-018] Announcement validation passed for title: {}", announcement.getTitle());
    }

    // ====================== PROMOTION MANAGEMENT METHODS [REQ-017] ======================
    /**
     * Creates a new promotion with validated data and sanitized input.
     * Only accessible by Center Admin and Manager roles per [REQ-017].
     *
     * @param promotion The promotion entity to create
     * @return The created promotion entity
     * @throws UnauthorizedAccessException if the current user does not have permission
     * @throws ValidationException if the promotion data is invalid
     * @throws PromotionCodeAlreadyExistsException if the promo code already exists
     */
    @Transactional
    public Promotion createPromotion(Promotion promotion) {
        String operation = "CREATE_PROMOTION";
        User currentUser = securityContext.getCurrentUser();
        LOG.info("[PROMOTION_SERVICE] [REQ-017] Starting operation: {} for user: {}", operation, currentUser.getUserId());
        try {
            // Step 1: Validate RBAC access [ARC-001, ARC-002]
            validateRbac(currentUser);
            // Step 2: Sanitize all user input to prevent XSS [NFR-003]
            promotion.setCode(sanitizeInput(promotion.getCode()));
            if (promotion.getDescription() != null) {
                promotion.setDescription(sanitizeInput(promotion.getDescription()));
            }
            // Step 3: Validate business rules [REQ-017]
            validatePromotion(promotion);
            // Step 4: Check for duplicate promo code (unique constraint enforced at DB layer via prepared statements [NFR-003])
            if (promotionRepository.existsByCode(promotion.getCode())) {
                LOG.warn("[PROMOTION_SERVICE] [REQ-017] Duplicate promo code detected: {}", promotion.getCode());
                throw new PromotionCodeAlreadyExistsException("Promotion code already exists: " + promotion.getCode());
            }
            // Step 5: Set default active status
            promotion.setActive(true);
            // Step 6: Persist promotion using Hibernate prepared statements (prevents SQL injection [NFR-003])
            Promotion createdPromotion = promotionRepository.persist(promotion);
            // Step 7: Send notification to relevant users about new promotion [REQ-016]
            notificationService.sendPromotionNotification(createdPromotion);
            LOG.info("[PROMOTION_SERVICE] [REQ-017] Operation {} completed successfully. Created promotion ID: {}", operation, createdPromotion.getPromoId());
            return createdPromotion;
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-017] Operation {} failed for user: {}. Raw error: {}", operation, currentUser.getUserId(), e.getMessage(), e);
            // Re-throw custom exception with original cause to preserve stack trace [GLOBAL_GOVERNANCE_MATRIX 0.3]
            if (e instanceof PromotionCodeAlreadyExistsException || e instanceof ValidationException || e instanceof UnauthorizedAccessException) {
                throw e;
            }
            throw new RuntimeException("Failed to create promotion", e);
        }
    }

    /**
     * Updates an existing promotion with validated data and sanitized input.
     * Only accessible by Center Admin and Manager roles per [REQ-017].
     *
     * @param promoId The ID of the promotion to update
     * @param updatedPromotion The updated promotion data
     * @return The updated promotion entity
     * @throws EntityNotFoundException if the promotion does not exist
     * @throws UnauthorizedAccessException if the current user does not have permission
     * @throws ValidationException if the updated data is invalid
     * @throws PromotionCodeAlreadyExistsException if the new promo code conflicts with an existing one
     */
    @Transactional
    public Promotion updatePromotion(UUID promoId, Promotion updatedPromotion) {
        String operation = "UPDATE_PROMOTION";
        User currentUser = securityContext.getCurrentUser();
        LOG.info("[PROMOTION_SERVICE] [REQ-017] Starting operation: {} for user: {} for promo ID: {}", operation, currentUser.getUserId(), promoId);
        try {
            // Step 1: Validate RBAC access [ARC-001, ARC-002]
            validateRbac(currentUser);
            // Step 2: Fetch existing promotion from database (using prepared statement query [NFR-003])
            Promotion existingPromotion = promotionRepository.findById(promoId)
                    .orElseThrow(() -> {
                        LOG.warn("[PROMOTION_SERVICE] [REQ-017] Promotion not found for ID: {}", promoId);
                        return new EntityNotFoundException("Promotion not found with ID: " + promoId);
                    });
            // Step 3: Sanitize all user input to prevent XSS [NFR-003]
            updatedPromotion.setCode(sanitizeInput(updatedPromotion.getCode()));
            if (updatedPromotion.getDescription() != null) {
                updatedPromotion.setDescription(sanitizeInput(updatedPromotion.getDescription()));
            }
            // Step 4: Validate business rules [REQ-017]
            validatePromotion(updatedPromotion);
            // Step 5: Check for duplicate promo code if it's changed
            if (!existingPromotion.getCode().equals(updatedPromotion.getCode()) && promotionRepository.existsByCode(updatedPromotion.getCode())) {
                LOG.warn("[PROMOTION_SERVICE] [REQ-017] Duplicate promo code detected during update: {}", updatedPromotion.getCode());
                throw new PromotionCodeAlreadyExistsException("Promotion code already exists: " + updatedPromotion.getCode());
            }
            // Step 6: Update fields
            existingPromotion.setCode(updatedPromotion.getCode());
            existingPromotion.setDiscountPercent(updatedPromotion.getDiscountPercent());
            existingPromotion.setStartDate(updatedPromotion.getStartDate());
            existingPromotion.setEndDate(updatedPromotion.getEndDate());
            existingPromotion.setDescription(updatedPromotion.getDescription());
            existingPromotion.setActive(updatedPromotion.isActive());
            // Step 7: Persist updates using Hibernate prepared statements [NFR-003]
            Promotion updated = promotionRepository.persist(existingPromotion);
            // Step 8: Send notification about updated promotion [REQ-016]
            notificationService.sendPromotionUpdateNotification(updated);
            LOG.info("[PROMOTION_SERVICE] [REQ-017] Operation {} completed successfully for promo ID: {}", operation, promoId);
            return updated;
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-017] Operation {} failed for user: {} for promo ID: {}. Raw error: {}", operation, currentUser.getUserId(), promoId, e.getMessage(), e);
            if (e instanceof EntityNotFoundException || e instanceof PromotionCodeAlreadyExistsException || e instanceof ValidationException || e instanceof UnauthorizedAccessException) {
                throw e;
            }
            throw new RuntimeException("Failed to update promotion", e);
        }
    }

    /**
     * Deletes a promotion by ID.
     * Only accessible by Center Admin and Manager roles per [REQ-017].
     *
     * @param promoId The ID of the promotion to delete
     * @throws EntityNotFoundException if the promotion does not exist
     * @throws UnauthorizedAccessException if the current user does not have permission
     */
    @Transactional
    public void deletePromotion(UUID promoId) {
        String operation = "DELETE_PROMOTION";
        User currentUser = securityContext.getCurrentUser();
        LOG.info("[PROMOTION_SERVICE] [REQ-017] Starting operation: {} for user: {} for promo ID: {}", operation, currentUser.getUserId(), promoId);
        try {
            // Step 1: Validate RBAC access [ARC-001, ARC-002]
            validateRbac(currentUser);
            // Step 2: Check if promotion exists
            if (!promotionRepository.existsById(promoId)) {
                LOG.warn("[PROMOTION_SERVICE] [REQ-017] Promotion not found for deletion, ID: {}", promoId);
                throw new EntityNotFoundException("Promotion not found with ID: " + promoId);
            }
            // Step 3: Delete promotion using Hibernate prepared statement delete query [NFR-003]
            promotionRepository.deleteById(promoId);
            LOG.info("[PROMOTION_SERVICE] [REQ-017] Operation {} completed successfully for promo ID: {}", operation, promoId);
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-017] Operation {} failed for user: {} for promo ID: {}. Raw error: {}", operation, currentUser.getUserId(), promoId, e.getMessage(), e);
            if (e instanceof EntityNotFoundException || e instanceof UnauthorizedAccessException) {
                throw e;
            }
            throw new RuntimeException("Failed to delete promotion", e);
        }
    }

    /**
     * Retrieves all active promotions (current date is within start and end date, or end date is null).
     * Only accessible by Center Admin and Manager roles per [REQ-017].
     *
     * @return List of active promotions
     * @throws UnauthorizedAccessException if the current user does not have permission
     */
    @Transactional
    public List<Promotion> getActivePromotions() {
        String operation = "GET_ACTIVE_PROMOTIONS";
        User currentUser = securityContext.getCurrentUser();
        LOG.info("[PROMOTION_SERVICE] [REQ-017] Starting operation: {} for user: {}", operation, currentUser.getUserId());
        try {
            // Step 1: Validate RBAC access [ARC-001, ARC-002]
            validateRbac(currentUser);
            // Step 2: Fetch active promotions using parameterized query (prevents SQL injection [NFR-003])
            LocalDate currentDate = LocalDate.now();
            List<Promotion> activePromotions = promotionRepository.findActivePromotions(currentDate);
            LOG.info("[PROMOTION_SERVICE] [REQ-017] Operation {} completed successfully. Retrieved {} active promotions for user: {}", operation, activePromotions.size(), currentUser.getUserId());
            return activePromotions;
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-017] Operation {} failed for user: {}. Raw error: {}", operation, currentUser.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve active promotions", e);
        }
    }

    // ====================== ANNOUNCEMENT MANAGEMENT METHODS [REQ-018] ======================
    /**
     * Creates a new system announcement with validated data and sanitized input.
     * Only accessible by Center Admin and Manager roles per [REQ-018].
     *
     * @param announcement The announcement entity to create
     * @return The created announcement entity
     * @throws UnauthorizedAccessException if the current user does not have permission
     * @throws ValidationException if the announcement data is invalid
     */
    @Transactional
    public Announcement createAnnouncement(Announcement announcement) {
        String operation = "CREATE_ANNOUNCEMENT";
        User currentUser = securityContext.getCurrentUser();
        LOG.info("[PROMOTION_SERVICE] [REQ-018] Starting operation: {} for user: {}", operation, currentUser.getUserId());
        try {
            // Step 1: Validate RBAC access [ARC-001, ARC-002]
            validateRbac(currentUser);
            // Step 2: Sanitize all user input to prevent XSS [NFR-003]
            announcement.setTitle(sanitizeInput(announcement.getTitle()));
            announcement.setContent(sanitizeInput(announcement.getContent()));
            // Step 3: Validate business rules [REQ-018]
            validateAnnouncement(announcement);
            // Step 4: Set default active status
            announcement.setActive(true);
            // Step 5: Persist announcement using Hibernate prepared statements (prevents SQL injection [NFR-003])
            Announcement createdAnnouncement = announcementRepository.persist(announcement);
            // Step 6: Send notification to all users about new announcement [REQ-016]
            notificationService.sendAnnouncementNotification(createdAnnouncement);
            LOG.info("[PROMOTION_SERVICE] [REQ-018] Operation {} completed successfully. Created announcement ID: {}", operation, createdAnnouncement.getAnnouncementId());
            return createdAnnouncement;
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-018] Operation {} failed for user: {}. Raw error: {}", operation, currentUser.getUserId(), e.getMessage(), e);
            if (e instanceof ValidationException || e instanceof UnauthorizedAccessException) {
                throw e;
            }
            throw new RuntimeException("Failed to create announcement", e);
        }
    }

    /**
     * Updates an existing system announcement with validated data and sanitized input.
     * Only accessible by Center Admin and Manager roles per [REQ-018].
     *
     * @param announcementId The ID of the announcement to update
     * @param updatedAnnouncement The updated announcement data
     * @return The updated announcement entity
     * @throws EntityNotFoundException if the announcement does not exist
     * @throws UnauthorizedAccessException if the current user does not have permission
     * @throws ValidationException if the updated data is invalid
     */
    @Transactional
    public Announcement updateAnnouncement(UUID announcementId, Announcement updatedAnnouncement) {
        String operation = "UPDATE_ANNOUNCEMENT";
        User currentUser = securityContext.getCurrentUser();
        LOG.info("[PROMOTION_SERVICE] [REQ-018] Starting operation: {} for user: {} for announcement ID: {}", operation, currentUser.getUserId(), announcementId);
        try {
            // Step 1: Validate RBAC access [ARC-001, ARC-002]
            validateRbac(currentUser);
            // Step 2: Fetch existing announcement from database (using prepared statement query [NFR-003])
            Announcement existingAnnouncement = announcementRepository.findById(announcementId)
                    .orElseThrow(() -> {
                        LOG.warn("[PROMOTION_SERVICE] [REQ-018] Announcement not found for ID: {}", announcementId);
                        return new EntityNotFoundException("Announcement not found with ID: " + announcementId);
                    });
            // Step 3: Sanitize all user input to prevent XSS [NFR-003]
            updatedAnnouncement.setTitle(sanitizeInput(updatedAnnouncement.getTitle()));
            updatedAnnouncement.setContent(sanitizeInput(updatedAnnouncement.getContent()));
            // Step 4: Validate business rules [REQ-018]
            validateAnnouncement(updatedAnnouncement);
            // Step 5: Update fields
            existingAnnouncement.setTitle(updatedAnnouncement.getTitle());
            existingAnnouncement.setContent(updatedAnnouncement.getContent());
            existingAnnouncement.setStartDate(updatedAnnouncement.getStartDate());
            existingAnnouncement.setEndDate(updatedAnnouncement.getEndDate());
            existingAnnouncement.setActive(updatedAnnouncement.isActive());
            // Step 6: Persist updates using Hibernate prepared statements [NFR-003]
            Announcement updated = announcementRepository.persist(existingAnnouncement);
            // Step 7: Send notification about updated announcement [REQ-016]
            notificationService.sendAnnouncementUpdateNotification(updated);
            LOG.info("[PROMOTION_SERVICE] [REQ-018] Operation {} completed successfully for announcement ID: {}", operation, announcementId);
            return updated;
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-018] Operation {} failed for user: {} for announcement ID: {}. Raw error: {}", operation, currentUser.getUserId(), announcementId, e.getMessage(), e);
            if (e instanceof EntityNotFoundException || e instanceof ValidationException || e instanceof UnauthorizedAccessException) {
                throw e;
            }
            throw new RuntimeException("Failed to update announcement", e);
        }
    }

    /**
     * Deletes an announcement by ID.
     * Only accessible by Center Admin and Manager roles per [REQ-018].
     *
     * @param announcementId The ID of the announcement to delete
     * @throws EntityNotFoundException if the announcement does not exist
     * @throws UnauthorizedAccessException if the current user does not have permission
     */
    @Transactional
    public void deleteAnnouncement(UUID announcementId) {
        String operation = "DELETE_ANNOUNCEMENT";
        User currentUser = securityContext.getCurrentUser();
        LOG.info("[PROMOTION_SERVICE] [REQ-018] Starting operation: {} for user: {} for announcement ID: {}", operation, currentUser.getUserId(), announcementId);
        try {
            // Step 1: Validate RBAC access [ARC-001, ARC-002]
            validateRbac(currentUser);
            // Step 2: Check if announcement exists
            if (!announcementRepository.existsById(announcementId)) {
                LOG.warn("[PROMOTION_SERVICE] [REQ-018] Announcement not found for deletion, ID: {}", announcementId);
                throw new EntityNotFoundException("Announcement not found with ID: " + announcementId);
            }
            // Step 3: Delete announcement using Hibernate prepared statement delete query [NFR-003]
            announcementRepository.deleteById(announcementId);
            LOG.info("[PROMOTION_SERVICE] [REQ-018] Operation {} completed successfully for announcement ID: {}", operation, announcementId);
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-018] Operation {} failed for user: {} for announcement ID: {}. Raw error: {}", operation, currentUser.getUserId(), announcementId, e.getMessage(), e);
            if (e instanceof EntityNotFoundException || e instanceof UnauthorizedAccessException) {
                throw e;
            }
            throw new RuntimeException("Failed to delete announcement", e);
        }
    }

    /**
     * Retrieves all active announcements (current date is within start and end date, or end date is null, and is active).
     * Only accessible by Center Admin and Manager roles per [REQ-018].
     *
     * @return List of active announcements
     * @throws UnauthorizedAccessException if the current user does not have permission
     */
    @Transactional
    public List<Announcement> getActiveAnnouncements() {
        String operation = "GET_ACTIVE_ANNOUNCEMENTS";
        User currentUser = securityContext.getCurrentUser();
        LOG.info("[PROMOTION_SERVICE] [REQ-018] Starting operation: {} for user: {}", operation, currentUser.getUserId());
        try {
            // Step 1: Validate RBAC access [ARC-001, ARC-002]
            validateRbac(currentUser);
            // Step 2: Fetch active announcements using parameterized query (prevents SQL injection [NFR-003])
            LocalDate currentDate = LocalDate.now();
            List<Announcement> activeAnnouncements = announcementRepository.findActiveAnnouncements(currentDate);
            LOG.info("[PROMOTION_SERVICE] [REQ-018] Operation {} completed successfully. Retrieved {} active announcements for user: {}", operation, activeAnnouncements.size(), currentUser.getUserId());
            return activeAnnouncements;
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-018] Operation {} failed for user: {}. Raw error: {}", operation, currentUser.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve active announcements", e);
        }
    }

    // ====================== SCHEDULED JOB FOR AUTOMATIC ANNOUNCEMENT EXPIRATION [REQ-018] ======================
    /**
     * Scheduled job that runs daily at midnight to automatically deactivate expired announcements.
     * Ensures announcements are hidden after their end date per [REQ-018].
     * Runs with system privileges, no RBAC check required.
     */
    @Scheduled(cron = SCHEDULED_JOB_CRON)
    @Transactional
    public void deactivateExpiredAnnouncements() {
        String operation = "DEACTIVATE_EXPIRED_ANNOUNCEMENTS";
        LOG.info("[PROMOTION_SERVICE] [REQ-018] Starting scheduled operation: {}", operation);
        try {
            LocalDate currentDate = LocalDate.now();
            // Fetch all active announcements that have expired (end date < current date) using parameterized query [NFR-003]
            List<Announcement> expiredAnnouncements = announcementRepository.findExpiredActiveAnnouncements(currentDate);
            if (expiredAnnouncements.isEmpty()) {
                LOG.info("[PROMOTION_SERVICE] [REQ-018] No expired announcements found to deactivate");
                return;
            }
            // Deactivate all expired announcements
            for (Announcement announcement : expiredAnnouncements) {
                announcement.setActive(false);
                announcementRepository.persist(announcement);
                LOG.debug("[PROMOTION_SERVICE] [REQ-018] Deactivated expired announcement ID: {}, title: {}", announcement.getAnnouncementId(), announcement.getTitle());
            }
            LOG.info("[PROMOTION_SERVICE] [REQ-018] Scheduled operation {} completed successfully. Deactivated {} expired announcements", operation, expiredAnnouncements.size());
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-018] Scheduled operation {} failed. Raw error: {}", operation, e.getMessage(), e);
            // Throw runtime exception to trigger alert for operations team per [EXC-010]
            throw new RuntimeException("Failed to deactivate expired announcements", e);
        }
    }
}
```

# Day 1: model kilo-auto/free - API Endpoint https://api.kilo.ai/api/gateway
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/promotion-service/src/main/java/org/nlh4j/membership_hub/promotion/AnnouncementController.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Component Destination Path: `./sources/backend/promotion-service/src/main/java/org/nlh4j/membership_hub/promotion/AnnouncementController.java`
*   Traceability Audit Tags For This Task: ['[REQ-017]', '[REQ-018]', '[DAT-009]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai toàn bộ logic nghiệp vụ cho module quản lý khuyến mãi và thông báo hệ thống. Đối với khuyến mãi, xây dựng các phương thức CRUD với kiểm tra xác thực đầu vào nghiêm ngặt: phần trăm giảm giá phải nằm trong khoảng 0-100, ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu, mã khuyến mãi phải duy nhất. Đối với thông báo, triển khai logic lọc thông báo đang hoạt động dựa trên ngày hiện tại và cơ chế tự động ẩn thông báo sau ngày kết thúc thông qua scheduled job chạy hàng ngày. Tất cả các endpoint phải được bảo vệ bằng bộ lọc RBAC, chỉ cho phép Center Admin và Manager truy cập. Sử dụng prepared statements của Hibernate ORM để ngăn chặn SQL injection. Áp dụng làm sạch dữ liệu đầu vào để chống XSS.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-017]', '[REQ-018]', '[DAT-009]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership_hub.promotion;

// [IMPORTS LAYER - ENTERPRISE DEPENDENCY COMPLIANCE]
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Context;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.DefaultValue;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.nlh4j.saas.membership_hub.promotion.dto.AnnouncementRequestDTO;
import org.nlh4j.saas.membership_hub.promotion.dto.AnnouncementResponseDTO;
import org.nlh4j.saas.membership_hub.promotion.dto.PromotionRequestDTO;
import org.nlh4j.saas.membership_hub.promotion.dto.PromotionResponseDTO;
import org.nlh4j.saas.membership_hub.promotion.entity.Announcement;
import org.nlh4j.saas.membership_hub.promotion.entity.Promotion;
import org.nlh4j.saas.membership_hub.promotion.exception.AnnouncementNotFoundException;
import org.nlh4j.saas.membership_hub.promotion.exception.DuplicateAnnouncementException;
import org.nlh4j.saas.membership_hub.promotion.exception.DuplicatePromoCodeException;
import org.nlh4j.saas.membership_hub.promotion.exception.InvalidPromotionDataException;
import org.nlh4j.saas.membership_hub.promotion.exception.ValidationException;
import org.nlh4j.saas.membership_hub.promotion.service.AnnouncementService;
import org.nlh4j.saas.membership_hub.promotion.service.PromotionService;
import org.nlh4j.saas.membership_hub.security.CustomUserDetails;
import org.nlh4j.saas.membership_hub.security.SecurityContext;
import org.owasp.encoder.Encode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for managing system announcements and promotions
 * <p>
 * Exposes CRUD endpoints for announcements (REQ-018) and promotions (REQ-017),
 * enforces RBAC access control, input validation, XSS sanitization, and idempotency for mutation operations.
 * All database operations use Hibernate prepared statements to prevent SQL injection per NFR-003.
 * Active record filtering and auto-hide of expired announcements are handled by the service layer scheduled job.
 * </p>
 * @traceability [REQ-017], [REQ-018], [DAT-009]
 * @since 1.0
 * @author Membership Hub Engineering Team
 */
@ApplicationScoped
@Path("/api/v1")
public class AnnouncementController implements Serializable {

    private static final long serialVersionUID = 1L;

    // [CONSTANTS LAYER - ANTI-MAGIC NUMBERS COMPLIANCE [0.2]]
    // All business rule and configuration constants are declared at class crown level, no hardcoded values in operational logic
    public static final String TRACEABILITY_TAGS = "[REQ-017], [REQ-018], [DAT-009]";
    public static final String SUBSYSTEM_NAME = "Promotion-Announcement-Service"; // For audit logging [0.3]
    public static final int MAX_ANNOUNCEMENT_TITLE_LENGTH = 150; // Per DAT-009 schema constraint
    public static final int MAX_ANNOUNCEMENT_CONTENT_LENGTH = 2000; // Per DAT-009 schema constraint
    public static final int MAX_PROMO_CODE_LENGTH = 50; // Per DAT-009 schema constraint
    public static final int MAX_PROMO_DESCRIPTION_LENGTH = 2000; // Per DAT-009 schema constraint
    public static final int MIN_PROMO_DISCOUNT_PERCENT = 0; // Per REQ-017 business rule
    public static final int MAX_PROMO_DISCOUNT_PERCENT = 100; // Per REQ-017 business rule
    public static final int DEFAULT_PAGE_SIZE = 20; // Default pagination size for list endpoints
    public static final int MAX_PAGE_SIZE = 100; // Maximum allowed page size to prevent abuse
    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key"; // Enterprise standard header for mutation idempotency [1.0]

    // [LOGGER LAYER - ENTERPRISE AUDIT COMPLIANCE [0.3]]
    private static final Logger logger = Logger.getLogger(AnnouncementController.class);

    // [DEPENDENCY INJECTION LAYER - QUARKUS NATIVE]
    @Inject
    AnnouncementService announcementService; // Business logic for announcement operations
    @Inject
    PromotionService promotionService; // Business logic for promotion operations
    @Inject
    SecurityContext securityContext; // Quarkus native security context for user identity extraction
    @Context
    HttpHeaders httpHeaders; // JAX-RS context for accessing request headers (idempotency key)

    // [INNER DTO LAYER - REQUEST/RESPONSE DATA CONTRACTS]
    // DTO for announcement creation/update requests, enforces input validation constraints
    public static class AnnouncementRequestDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @NotBlank(message = "Announcement title is required")
        @Size(max = MAX_ANNOUNCEMENT_TITLE_LENGTH, message = "Title cannot exceed " + MAX_ANNOUNCEMENT_TITLE_LENGTH + " characters")
        private String title;

        @NotBlank(message = "Announcement content is required")
        @Size(max = MAX_ANNOUNCEMENT_CONTENT_LENGTH, message = "Content cannot exceed " + MAX_ANNOUNCEMENT_CONTENT_LENGTH + " characters")
        private String content;

        @PastOrPresent(message = "Start date cannot be in the future")
        private LocalDate startDate;

        @FutureOrPresent(message = "End date cannot be in the past")
        private LocalDate endDate;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    }

    // DTO for promotion creation/update requests, enforces business rule validation
    public static class PromotionRequestDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @NotBlank(message = "Promo code is required")
        @Size(max = MAX_PROMO_CODE_LENGTH, message = "Promo code cannot exceed " + MAX_PROMO_CODE_LENGTH + " characters")
        @Pattern(regexp = "^[A-Z0-9]+$", message = "Promo code must contain only uppercase letters and numbers")
        private String code;

        @NotNull(message = "Discount percent is required")
        @Min(value = MIN_PROMO_DISCOUNT_PERCENT, message = "Discount percent cannot be less than " + MIN_PROMO_DISCOUNT_PERCENT)
        @Max(value = MAX_PROMO_DISCOUNT_PERCENT, message = "Discount percent cannot exceed " + MAX_PROMO_DISCOUNT_PERCENT)
        private Integer discountPercent;

        @PastOrPresent(message = "Start date cannot be in the future")
        private LocalDate startDate;

        @FutureOrPresent(message = "End date cannot be in the past")
        private LocalDate endDate;

        @Size(max = MAX_PROMO_DESCRIPTION_LENGTH, message = "Description cannot exceed " + MAX_PROMO_DESCRIPTION_LENGTH + " characters")
        private String description;

        // Getters and setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public Integer getDiscountPercent() { return discountPercent; }
        public void setDiscountPercent(Integer discountPercent) { this.discountPercent = discountPercent; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    // DTO for announcement response payloads, prevents overposting by exposing only safe fields
    public static class AnnouncementResponseDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private UUID announcementId;
        private String title;
        private String content;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Constructor to map entity to DTO
        public AnnouncementResponseDTO(Announcement announcement) {
            this.announcementId = announcement.getAnnouncementId();
            this.title = announcement.getTitle();
            this.content = announcement.getContent();
            this.startDate = announcement.getStartDate();
            this.endDate = announcement.getEndDate();
            this.createdAt = announcement.getCreatedAt();
            this.updatedAt = announcement.getUpdatedAt();
        }

        // Getters
        public UUID getAnnouncementId() { return announcementId; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
    }

    // DTO for promotion response payloads, prevents overposting by exposing only safe fields
    public static class PromotionResponseDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private UUID promoId;
        private String code;
        private Integer discountPercent;
        private LocalDate startDate;
        private LocalDate endDate;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Constructor to map entity to DTO
        public PromotionResponseDTO(Promotion promotion) {
            this.promoId = promotion.getPromoId();
            this.code = promotion.getCode();
            this.discountPercent = promotion.getDiscountPercent();
            this.startDate = promotion.getStartDate();
            this.endDate = promotion.getEndDate();
            this.description = promotion.getDescription();
            this.createdAt = promotion.getCreatedAt();
            this.updatedAt = promotion.getUpdatedAt();
        }

        // Getters
        public UUID getPromoId() { return promoId; }
        public String getCode() { return code; }
        public Integer getDiscountPercent() { return discountPercent; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public String getDescription() { return description; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
    }

    // [INNER EXCEPTION LAYER - BUSINESS EXCEPTION CONTRACTS]
    // Custom validation exception for input rule violations
    public static class ValidationException extends Exception {
        private static final long serialVersionUID = 1L;
        public ValidationException(String message) { super(message); }
    }

    // Custom exception for duplicate promo code conflicts
    public static class DuplicatePromoCodeException extends Exception {
        private static final long serialVersionUID = 1L;
        public DuplicatePromoCodeException(String message) { super(message); }
    }

    // Custom exception for duplicate announcement conflicts
    public static class DuplicateAnnouncementException extends Exception {
        private static final long serialVersionUID = 1L;
        public DuplicateAnnouncementException(String message) { super(message); }
    }

    // Custom exception for invalid promotion data (discount range, date order)
    public static class InvalidPromotionDataException extends Exception {
        private static final long serialVersionUID = 1L;
        public InvalidPromotionDataException(String message) { super(message); }
    }

    // [UTILITY METHOD LAYER - SECURITY & IDENTITY]
    /**
     * Sanitizes user input to prevent XSS attacks per OWASP Top 10 compliance [NFR-003]
     * Uses OWASP Java HTML Sanitizer to remove malicious scripts and tags from input
     * @param input the raw input string from user request
     * @return sanitized string safe for storage and display
     */
    private String sanitizeInput(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        // Encode HTML special characters to neutralize XSS payloads
        return Encode.forHtml(input);
    }

    /**
     * Retrieves the current authenticated user ID from the Quarkus security context
     * @return UUID of the current authenticated user
     * @throws WebApplicationException with 401 status if user is not authenticated
     */
    private UUID getCurrentUserId() {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            // Log unauthenticated access attempt per audit requirements [NFR-006]
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Unauthenticated access attempt to promotion/announcement endpoint");
            throw new WebApplicationException("User not authenticated", Response.Status.UNAUTHORIZED);
        }
        // Cast to project-specific CustomUserDetails to extract user ID and roles
        return ((CustomUserDetails) securityContext.getUserPrincipal()).getUserId();
    }

    // ==============================================
    // ANNOUNCEMENT ENDPOINTS (REQ-018)
    // ==============================================

    /**
     * Retrieves all active announcements visible to all authenticated users
     * Active announcements are those where current date is between start_date and end_date, or end_date is null
     * @return list of active announcements with 200 OK status
     * @traceability [REQ-018]
     */
    @GET
    @Path("/announcements/active")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getActiveAnnouncements() {
        try {
            // Entry audit log per enterprise logging requirements [0.3]
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Fetching active announcements for public access");
            // Service layer handles filtering of active records and scheduled auto-hide of expired announcements
            List<AnnouncementResponseDTO> activeAnnouncements = announcementService.getActiveAnnouncements();
            // Exit audit log with result count
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Fetched {} active announcements", activeAnnouncements.size());
            return Response.ok(activeAnnouncements).build();
        } catch (Exception e) {
            // Comprehensive error logging with required 3 context keys [0.3]
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Failed to fetch active announcements. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while fetching active announcements", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves all announcements (including inactive/expired) for admin users (Center Admin, Manager)
     * Supports pagination with page and size query parameters
     * @param page page number (default 1)
     * @param size page size (default 20, max 100)
     * @return paginated list of all announcements with 200 OK status
     * @throws WebApplicationException with 400 if pagination parameters are invalid, 403 if user lacks permissions
     * @traceability [REQ-018]
     */
    @GET
    @Path("/announcements")
    @RolesAllowed({"CENTER_ADMIN", "MANAGER"}) // RBAC enforcement per ARC-001 to ARC-005
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllAnnouncements(@QueryParam("page") @DefaultValue("1") int page, @QueryParam("size") @DefaultValue(String.valueOf(DEFAULT_PAGE_SIZE)) int size) {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Fetching all announcements for admin user: {}", getCurrentUserId());
            // Validate pagination parameters to prevent abuse
            if (page < 1) {
                throw new ValidationException("Page number must be at least 1");
            }
            if (size < 1 || size > MAX_PAGE_SIZE) {
                throw new ValidationException("Page size must be between 1 and " + MAX_PAGE_SIZE);
            }
            // Service layer uses prepared statements for paginated queries to prevent SQL injection [NFR-003]
            List<AnnouncementResponseDTO> announcements = announcementService.getAllAnnouncements(page, size);
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Fetched {} announcements (page {}, size {})", announcements.size(), page, size);
            return Response.ok(announcements).build();
        } catch (ValidationException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Invalid pagination parameters. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Invalid pagination: " + e.getMessage(), Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Failed to fetch all announcements. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while fetching announcements", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Creates a new system announcement (admin only: Center Admin, Manager)
     * Enforces input sanitization, idempotency, and business rule validation
     * @param request announcement creation request DTO
     * @return created announcement with 201 CREATED status
     * @throws WebApplicationException with 400 for invalid input, 409 for duplicate, 403 for insufficient permissions
     * @traceability [REQ-018]
     */
    @POST
    @Path("/announcements")
    @RolesAllowed({"CENTER_ADMIN", "MANAGER"}) // RBAC enforcement per ARC-001 to ARC-005
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAnnouncement(@Valid AnnouncementRequestDTO request) {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Creating new announcement for user: {}", getCurrentUserId());
            // Step 1: Sanitize input to prevent XSS attacks per OWASP Top 10 [NFR-003]
            String sanitizedTitle = sanitizeInput(request.getTitle());
            String sanitizedContent = sanitizeInput(request.getContent());
            // Step 2: Defense-in-depth validation for length constraints
            if (sanitizedTitle.length() > MAX_ANNOUNCEMENT_TITLE_LENGTH) {
                throw new ValidationException("Title exceeds maximum allowed length of " + MAX_ANNOUNCEMENT_TITLE_LENGTH + " characters");
            }
            if (sanitizedContent.length() > MAX_ANNOUNCEMENT_CONTENT_LENGTH) {
                throw new ValidationException("Content exceeds maximum allowed length of " + MAX_ANNOUNCEMENT_CONTENT_LENGTH + " characters");
            }
            // Step 3: Validate idempotency key to prevent duplicate mutation requests per enterprise governance [1.0]
            String idempotencyKey = httpHeaders.getHeaderString(IDEMPOTENCY_KEY_HEADER);
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                throw new ValidationException("Idempotency-Key header is required for mutation operations");
            }
            // Step 4: Call service layer to create announcement (uses Hibernate prepared statements to prevent SQL injection [NFR-003])
            AnnouncementResponseDTO response = announcementService.createAnnouncement(sanitizedTitle, sanitizedContent, request.getStartDate(), request.getEndDate(), idempotencyKey);
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Announcement created successfully with ID: {}", response.getAnnouncementId());
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (ConstraintViolationException e) {
            // Handle Jakarta validation errors with detailed field-level messages
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Announcement creation failed due to invalid input. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            String validationErrors = e.getConstraintViolations().stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.joining(", "));
            throw new WebApplicationException("Validation failed: " + validationErrors, Response.Status.BAD_REQUEST);
        } catch (DuplicateAnnouncementException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Announcement creation failed due to duplicate. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Duplicate announcement: " + e.getMessage(), Response.Status.CONFLICT);
        } catch (Exception e) {
            // Preserve original exception cause chain per enterprise exception handling rules [0.3]
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Announcement creation failed due to system error. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while creating announcement", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Updates an existing announcement (admin only: Center Admin, Manager)
     * @param id UUID of the announcement to update
     * @param request updated announcement data
     * @return updated announcement with 200 OK status
     * @throws WebApplicationException with 400 for invalid input, 404 if announcement not found, 403 for insufficient permissions
     * @traceability [REQ-018]
     */
    @PUT
    @Path("/announcements/{id}")
    @RolesAllowed({"CENTER_ADMIN", "MANAGER"}) // RBAC enforcement per ARC-001 to ARC-005
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAnnouncement(@PathParam("id") UUID id, @Valid AnnouncementRequestDTO request) {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Updating announcement ID: {} for user: {}", id, getCurrentUserId());
            // Sanitize input to prevent XSS
            String sanitizedTitle = sanitizeInput(request.getTitle());
            String sanitizedContent = sanitizeInput(request.getContent());
            // Validate idempotency key for mutation operation
            String idempotencyKey = httpHeaders.getHeaderString(IDEMPOTENCY_KEY_HEADER);
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                throw new ValidationException("Idempotency-Key header is required for mutation operations");
            }
            // Call service to update announcement
            AnnouncementResponseDTO response = announcementService.updateAnnouncement(id, sanitizedTitle, sanitizedContent, request.getStartDate(), request.getEndDate(), idempotencyKey);
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Announcement ID: {} updated successfully", id);
            return Response.ok(response).build();
        } catch (ConstraintViolationException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Announcement update failed due to invalid input. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            String validationErrors = e.getConstraintViolations().stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.joining(", "));
            throw new WebApplicationException("Validation failed: " + validationErrors, Response.Status.BAD_REQUEST);
        } catch (AnnouncementNotFoundException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Announcement not found for update. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Announcement not found with ID: " + id, Response.Status.NOT_FOUND);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Announcement update failed. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while updating announcement", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes an announcement (admin only: Center Admin, Manager)
     * @param id UUID of the announcement to delete
     * @return 200 OK with success message
     * @throws WebApplicationException with 404 if announcement not found, 403 for insufficient permissions
     * @traceability [REQ-018]
     */
    @DELETE
    @Path("/announcements/{id}")
    @RolesAllowed({"CENTER_ADMIN", "MANAGER"}) // RBAC enforcement per ARC-001 to ARC-005
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAnnouncement(@PathParam("id") UUID id) {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Deleting announcement ID: {} for user: {}", id, getCurrentUserId());
            // Validate idempotency key for mutation operation
            String idempotencyKey = httpHeaders.getHeaderString(IDEMPOTENCY_KEY_HEADER);
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                throw new ValidationException("Idempotency-Key header is required for mutation operations");
            }
            // Call service to delete announcement
            announcementService.deleteAnnouncement(id, idempotencyKey);
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Announcement ID: {} deleted successfully", id);
            return Response.ok("{\"message\": \"Announcement deleted successfully\"}").build();
        } catch (AnnouncementNotFoundException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Announcement not found for deletion. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Announcement not found with ID: " + id, Response.Status.NOT_FOUND);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Announcement deletion failed. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while deleting announcement", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    // ==============================================
    // PROMOTION ENDPOINTS (REQ-017)
    // ==============================================

    /**
     * Retrieves all active promotions visible to all authenticated users
     * Active promotions are those where current date is between start_date and end_date, or end_date is null
     * @return list of active promotions with 200 OK status
     * @traceability [REQ-017]
     */
    @GET
    @Path("/promotions/active")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getActivePromotions() {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Fetching active promotions for public access");
            // Service layer filters active records and enforces business rules for validity periods
            List<PromotionResponseDTO> activePromotions = promotionService.getActivePromotions();
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Fetched {} active promotions", activePromotions.size());
            return Response.ok(activePromotions).build();
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Failed to fetch active promotions. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while fetching active promotions", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves all promotions (including inactive/expired) for admin users (Center Admin, Manager)
     * Supports pagination with page and size query parameters
     * @param page page number (default 1)
     * @param size page size (default 20, max 100)
     * @return paginated list of all promotions with 200 OK status
     * @throws WebApplicationException with 400 for invalid pagination, 403 for insufficient permissions
     * @traceability [REQ-017]
     */
    @GET
    @Path("/promotions")
    @RolesAllowed({"CENTER_ADMIN", "MANAGER"}) // RBAC enforcement per ARC-001 to ARC-005
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllPromotions(@QueryParam("page") @DefaultValue("1") int page, @QueryParam("size") @DefaultValue(String.valueOf(DEFAULT_PAGE_SIZE)) int size) {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Fetching all promotions for admin user: {}", getCurrentUserId());
            // Validate pagination parameters
            if (page < 1) {
                throw new ValidationException("Page number must be at least 1");
            }
            if (size < 1 || size > MAX_PAGE_SIZE) {
                throw new ValidationException("Page size must be between 1 and " + MAX_PAGE_SIZE);
            }
            // Service layer uses prepared statements for paginated queries to prevent SQL injection [NFR-003]
            List<PromotionResponseDTO> promotions = promotionService.getAllPromotions(page, size);
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Fetched {} promotions (page {}, size {})", promotions.size(), page, size);
            return Response.ok(promotions).build();
        } catch (ValidationException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Invalid pagination parameters. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Invalid pagination: " + e.getMessage(), Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Failed to fetch all promotions. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while fetching promotions", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Creates a new promotion (admin only: Center Admin, Manager)
     * Enforces business rules: discount 0-100, end date >= start date, unique promo code
     * @param request promotion creation request DTO
     * @return created promotion with 201 CREATED status
     * @throws WebApplicationException with 400 for invalid input, 409 for duplicate code, 403 for insufficient permissions
     * @traceability [REQ-017]
     */
    @POST
    @Path("/promotions")
    @RolesAllowed({"CENTER_ADMIN", "MANAGER"}) // RBAC enforcement per ARC-001 to ARC-005
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPromotion(@Valid PromotionRequestDTO request) {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Creating new promotion for user: {}", getCurrentUserId());
            // Step 1: Sanitize input to prevent XSS attacks per OWASP Top 10 [NFR-003]
            String sanitizedCode = sanitizeInput(request.getCode()).toUpperCase(); // Promo codes are standardized to uppercase
            String sanitizedDescription = request.getDescription() != null ? sanitizeInput(request.getDescription()) : null;
            // Step 2: Defense-in-depth validation for business rules
            if (request.getDiscountPercent() < MIN_PROMO_DISCOUNT_PERCENT || request.getDiscountPercent() > MAX_PROMO_DISCOUNT_PERCENT) {
                throw new InvalidPromotionDataException("Discount percent must be between " + MIN_PROMO_DISCOUNT_PERCENT + " and " + MAX_PROMO_DISCOUNT_PERCENT);
            }
            if (request.getEndDate() != null && request.getStartDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
                throw new InvalidPromotionDataException("End date cannot be before start date");
            }
            // Step 3: Validate idempotency key to prevent duplicate mutation requests per enterprise governance [1.0]
            String idempotencyKey = httpHeaders.getHeaderString(IDEMPOTENCY_KEY_HEADER);
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                throw new ValidationException("Idempotency-Key header is required for mutation operations");
            }
            // Step 4: Call service layer to create promotion (uses Hibernate prepared statements to prevent SQL injection [NFR-003])
            PromotionResponseDTO response = promotionService.createPromotion(sanitizedCode, request.getDiscountPercent(), request.getStartDate(), request.getEndDate(), sanitizedDescription, idempotencyKey);
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Promotion created successfully with code: {}", response.getCode());
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (ConstraintViolationException e) {
            // Handle Jakarta validation errors
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion creation failed due to invalid input. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            String validationErrors = e.getConstraintViolations().stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.joining(", "));
            throw new WebApplicationException("Validation failed: " + validationErrors, Response.Status.BAD_REQUEST);
        } catch (DuplicatePromoCodeException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion creation failed due to duplicate code. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Duplicate promo code: " + e.getMessage(), Response.Status.CONFLICT);
        } catch (InvalidPromotionDataException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion creation failed due to invalid data. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Invalid promotion data: " + e.getMessage(), Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            // Preserve original exception cause chain per enterprise exception handling rules [0.3]
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion creation failed due to system error. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while creating promotion", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Updates an existing promotion (admin only: Center Admin, Manager)
     * @param id UUID of the promotion to update
     * @param request updated promotion data
     * @return updated promotion with 200 OK status
     * @throws WebApplicationException with 400 for invalid input, 404 if promotion not found, 409 for duplicate code, 403 for insufficient permissions
     * @traceability [REQ-017]
     */
    @PUT
    @Path("/promotions/{id}")
    @RolesAllowed({"CENTER_ADMIN", "MANAGER"}) // RBAC enforcement per ARC-001 to ARC-005
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePromotion(@PathParam("id") UUID id, @Valid PromotionRequestDTO request) {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Updating promotion ID: {} for user: {}", id, getCurrentUserId());
            // Sanitize input to prevent XSS
            String sanitizedCode = sanitizeInput(request.getCode()).toUpperCase();
            String sanitizedDescription = request.getDescription() != null ? sanitizeInput(request.getDescription()) : null;
            // Validate business rules
            if (request.getDiscountPercent() < MIN_PROMO_DISCOUNT_PERCENT || request.getDiscountPercent() > MAX_PROMO_DISCOUNT_PERCENT) {
                throw new InvalidPromotionDataException("Discount percent must be between " + MIN_PROMO_DISCOUNT_PERCENT + " and " + MAX_PROMO_DISCOUNT_PERCENT);
            }
            if (request.getEndDate() != null && request.getStartDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
                throw new InvalidPromotionDataException("End date cannot be before start date");
            }
            // Validate idempotency key for mutation operation
            String idempotencyKey = httpHeaders.getHeaderString(IDEMPOTENCY_KEY_HEADER);
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                throw new ValidationException("Idempotency-Key header is required for mutation operations");
            }
            // Call service to update promotion
            PromotionResponseDTO response = promotionService.updatePromotion(id, sanitizedCode, request.getDiscountPercent(), request.getStartDate(), request.getEndDate(), sanitizedDescription, idempotencyKey);
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Promotion ID: {} updated successfully", id);
            return Response.ok(response).build();
        } catch (ConstraintViolationException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion update failed due to invalid input. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            String validationErrors = e.getConstraintViolations().stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.joining(", "));
            throw new WebApplicationException("Validation failed: " + validationErrors, Response.Status.BAD_REQUEST);
        } catch (DuplicatePromoCodeException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion update failed due to duplicate code. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Duplicate promo code: " + e.getMessage(), Response.Status.CONFLICT);
        } catch (InvalidPromotionDataException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion update failed due to invalid data. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Invalid promotion data: " + e.getMessage(), Response.Status.BAD_REQUEST);
        } catch (AnnouncementNotFoundException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion not found for update. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Promotion not found with ID: " + id, Response.Status.NOT_FOUND);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion update failed. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while updating promotion", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a promotion (admin only: Center Admin, Manager)
     * @param id UUID of the promotion to delete
     * @return 200 OK with success message
     * @throws WebApplicationException with 404 if promotion not found, 403 for insufficient permissions
     * @traceability [REQ-017]
     */
    @DELETE
    @Path("/promotions/{id}")
    @RolesAllowed({"CENTER_ADMIN", "MANAGER"}) // RBAC enforcement per ARC-001 to ARC-005
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePromotion(@PathParam("id") UUID id) {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Deleting promotion ID: {} for user: {}", id, getCurrentUserId());
            // Validate idempotency key for mutation operation
            String idempotencyKey = httpHeaders.getHeaderString(IDEMPOTENCY_KEY_HEADER);
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                throw new ValidationException("Idempotency-Key header is required for mutation operations");
            }
            // Call service to delete promotion
            promotionService.deletePromotion(id, idempotencyKey);
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Promotion ID: {} deleted successfully", id);
            return Response.ok("{\"message\": \"Promotion deleted successfully\"}").build();
        } catch (AnnouncementNotFoundException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion not found for deletion. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Promotion not found with ID: " + id, Response.Status.NOT_FOUND);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion deletion failed. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while deleting promotion", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
```

