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