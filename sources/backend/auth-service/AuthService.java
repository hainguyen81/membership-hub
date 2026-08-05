package org.nlh4j.saas.membership-hub.authservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.membershiphub.auth.model.User;
import com.membershiphub.auth.model.Role;
import com.membershiphub.auth.repository.UserRepository;
import com.membershiphub.auth.repository.RoleRepository;
import com.membershiphub.auth.dto.AuthRequest;
import com.membershiphub.auth.dto.AuthResponse;
import com.membershiphub.auth.exception.DuplicateEmailException;
import com.membershiphub.auth.exception.InvalidCredentialsException;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * AuthService implements core authentication and registration workflows.
 * <p>
 * Traceability Tags:
 *   [REQ-001] User registration endpoint.
 *   [REQ-002] User login endpoint.
 *   [ARC-001] JWT token generation and validation.
 *   [ARC-002] Password hashing using BCrypt.
 *   [ARC-003] Role‑based access control (RBAC) integration.
 *   [ARC-004] Idempotent request handling for registration.
 *   [ARC-005] Comprehensive logging and exception handling.
 *   [DAT-001] User entity schema definition.
 *   [DAT-002] Role entity schema definition.
 *   [NFR-001] Secure token expiration (15 min).
 *   [NFR-002] Refresh token expiration (7 days).
 *   [NFR-003] Input validation and sanitization.
 *   [NFR-004] Logging with structured context.
 *   [NFR-005] Exception traceability with Tag IDs.
 *   [NFR-006] Idempotency key validation.
 *   [NFR-007] Rate limiting considerations.
 *   [NFR-008] Secure password storage.
 *   [NFR-009] Audit trail for authentication events.
 */
@Service
@Transactional
public class AuthService implements UserDetailsService {

    /* --------------------------------------------------------------------- */
    /* CONSTANTS – All configuration values are hoisted to the class level    */
    /* --------------------------------------------------------------------- */
    /** JWT signing secret – must be kept confidential in production. */
    private static final String JWT_SECRET = "mySuperSecretKeyForJwtSigning1234567890";
    /** Access token lifetime in milliseconds (15 minutes). */
    private static final long JWT_EXPIRATION_MS = 15 * 60 * 1000;
    /** Refresh token lifetime in milliseconds (7 days). */
    private static final long REFRESH_TOKEN_EXPIRATION_MS = 7L * 24 * 60 * 60 * 1000;

    /* --------------------------------------------------------------------- */
    /* LOGGER – Enterprise‑grade logging integration.                         */
    /* --------------------------------------------------------------------- */
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        logger.info("[ENTRY] AuthService initialized – ready to process registration and login requests.");
    }

    /* --------------------------------------------------------------------- */
    /* REGISTRATION – Implements [REQ-001] and [ARC-004] idempotency.         */
    /* --------------------------------------------------------------------- */
    /**
     * Register a new user account.
     *
     * @param request DTO containing email, password, optional fullName and provider.
     * @return AuthResponse with generated JWT and refresh tokens.
     */
    public AuthResponse register(AuthRequest request) {
        logger.info("[REQ-001] Registration attempt for email: {}", request.getEmail());

        // ---- Input validation (NFR‑003) ----
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            logger.error("[EXC-004] [ARC-005] Registration failed – email is required");
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            logger.error("[EXC-004] [ARC-005] Registration failed – password must be at least 8 characters");
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        // ---- Idempotency check (simplified – could use a distributed lock or Redis) ----
        // If a user with this email already exists, treat as duplicate (NFR‑006).
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.error("[EXC-004] [ARC-005] Duplicate email during registration: {}", request.getEmail());
            throw new DuplicateEmailException("Email already exists");
        }

        // ---- Resolve default role (USER) – ARC‑003 ----
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> {
                    logger.error("[EXC-004] [ARC-005] Default role USER not found in database");
                    return new IllegalStateException("Default role not configured");
                });

        // ---- Password hashing – ARC‑002 ----
        String passwordHash = passwordEncoder.encode(request.getPassword());

        // ---- Build User entity – DAT‑001 ----
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordHash);
        user.setFullName(request.getFullName() != null ? request.getFullName() : "");
        user.setRole(userRole);
        user.setProvider(request.getProvider() != null ? request.getProvider() : "local");

        try {
            User saved = userRepository.save(user);
            logger.info("[DAT-001] User persisted with ID: {}", saved.getUserId());
        } catch (DataIntegrityViolationException e) {
            // Capture raw exception for audit – NFR‑005
            logger.error("[CRITICAL FAIL] [ARC-005] Data integrity violation during user registration. Raw error: {}", e.getMessage(), e);
            throw new DuplicateEmailException("Unique constraint violation");
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [ARC-005] Unexpected error during user registration. Raw error: {}", e.getMessage(), e);
            throw e; // re‑throw to preserve stack trace (Exception Cause Chain Preservation Law)
        }

        // ---- JWT issuance – ARC‑001, NFR‑001, NFR‑002 ----
        String token = jwtTokenProvider.generateToken(saved.getEmail(), saved.getUserId(), userRole.getName());
        String refresh = jwtTokenProvider.generateRefreshToken(saved.getEmail());

        logger.info("[EXIT] Registration completed successfully for email: {}", request.getEmail());
        return new AuthResponse(token, refresh);
    }

    /* --------------------------------------------------------------------- */
    /* LOGIN – Implements [REQ‑002] and [ARC‑004] idempotent validation.    */
    /* --------------------------------------------------------------------- */
    /**
     * Authenticate a user and issue JWT tokens.
     *
     * @param request DTO containing email and password.
     * @return AuthResponse with generated JWT and refresh tokens.
     */
    public AuthResponse login(AuthRequest request) {
        logger.info("[REQ-002] Login attempt for email: {}", request.getEmail());

        // ---- User lookup – ARC‑003 ----
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.error("[EXC-004] [ARC-005] Login failed – user not found for email {}", request.getEmail());
                    return new InvalidCredentialsException("Invalid credentials");
                });

        // ---- Password verification – ARC‑002, NFR‑008 ----
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            logger.error("[EXC-004] [ARC-005] Login failed – incorrect password for email {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid credentials");
        }

        // ---- Token generation – ARC‑001, NFR‑001, NFR‑002 ----
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getUserId(), user.getRole().getName());
        String refresh = jwtTokenProvider.generateRefreshToken(user.getEmail());

        logger.info("[EXIT] Login successful for email: {}", request.getEmail());
        return new AuthResponse(token, refresh);
    }

    /* --------------------------------------------------------------------- */
    /* USER DETAILS SERVICE – Required for Spring Security integration.      */
    /* --------------------------------------------------------------------- */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("[ARC-003] Loading user details for username: {}", username);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    logger.error("[EXC-004] [ARC-005] UserDetails not found for username {}", username);
                    return new UsernameNotFoundException("User not found");
                });

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole().getName())
                .build();
    }

    /* --------------------------------------------------------------------- */
    /* INTERNAL JWT PROVIDER – Simplified token handling for demonstration.   */
    /* --------------------------------------------------------------------- */
    @Service
    public static class JwtTokenProvider {
        private final String secret = JWT_SECRET;
        private final long accessTokenValidity = JWT_EXPIRATION_MS;
        private final long refreshTokenValidity = REFRESH_TOKEN_EXPIRATION_MS;

        public String generateToken(String email, UUID userId, String role) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId.toString());
            claims.put("role", role);
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(email)
                    .setIssuedAt(Date.from(Instant.now()))
                    .setExpiration(Date.from(Instant.now().plusMillis(accessTokenValidity)))
                    .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
                    .compact();
        }

        public String generateRefreshToken(String email) {
            return Jwts.builder()
                    .setSubject(email)
                    .setIssuedAt(Date.from(Instant.now()))
                    .setExpiration(Date.from(Instant.now().plusMillis(refreshTokenValidity)))
                    .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
                    .compact();
        }
    }
}