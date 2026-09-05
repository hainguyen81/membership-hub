package org.nlh4j.membershiphub.userservice.controller;

// [REQ-001] User registration endpoint support
// [EXC-004] Global exception handling and validation mapping
// [ARC-006] JWT and OAuth2 security authentication integration

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.nlh4j.membershiphub.userservice.audit.AuditLogged;
import org.nlh4j.membershiphub.userservice.dto.AuthResponse;
import org.nlh4j.membershiphub.userservice.dto.RegisterRequest;
import org.nlh4j.membershiphub.userservice.service.AuthService;

/**
 * -----------------------------------------------------------------------------------
 * Enterprise Controller: AuthController
 * -----------------------------------------------------------------------------------
 * Architectural Responsibility:
 * Exposes RESTful API endpoints for user authentication and registration workflows.
 * Integrates Jakarta Bean Validation, audit logging via AOP, and secure JWT issuance.
 * 
 * Traceability Audit Tags:
 * - [REQ-001]: Endpoint đăng ký người dùng mới với xác thực email và mật khẩu mạnh.
 * - [EXC-004]: Chuẩn hóa xử lý ngoại lệ validation (HTTP 400) và trùng lặp (HTTP 409).
 * - [ARC-006]: Lai tạo cơ chế xác thực JWT access/refresh token theo chuẩn bảo mật.
 * -----------------------------------------------------------------------------------
 */
@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication & User Registration", description = "Endpoints for managing user onboarding and authentication contracts.")
public class AuthController {

    // ===============================================================================
    // TOP-OF-CLASS CONSTANTS DECLARATION LAW [0.2]
    // ===============================================================================
    private static final Logger LOGGER = Logger.getLogger(AuthController.class);
    
    // Immutable constant parameters for auditing and response status messages
    public static final String LOG_PROCESS_REGISTRATION_START = "[PROCESS] Initiating user registration request for email: {}";
    public static final String LOG_PROCESS_REGISTRATION_SUCCESS = "[PROCESS] Successfully registered user with ID: {} and assigned role: {}";
    public static final String AUDIT_ACTION_USER_REGISTERED = "USER_REGISTERED";
    public static final int HTTP_STATUS_CREATED = 201;

    // ===============================================================================
    // DEPENDENCY INJECTION HANDLES
    // ===============================================================================
    private final AuthService authService;

    /**
     * Constructor injection for AuthService instance.
     * Enforces CDI best practices without field-injection anti-patterns.
     * 
     * @param authService the business logic service handling registration workflows
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * REST Endpoint: POST /api/v1/users/register
     * -------------------------------------------------------------------------------
     * Registers a new user account, validates payload integrity via Bean Validation,
     * persists credentials securely, and issues a JWT access token paired with a refresh token.
     * 
     * @param registerRequest the validated payload containing email, password, and full name [REQ-001]
     * @param securityContext injected security context for request tracing
     * @return Response containing HTTP 201 Created and AuthResponse payload [ARC-006]
     */
    @POST
    @Path("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with validated credentials and returns JWT tokens.")
    @APIResponse(responseCode = "201", description = "User successfully registered", 
                 content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @APIResponse(responseCode = "400", description = "Validation failed for registration payload [EXC-004]")
    @APIResponse(responseCode = "409", description = "Email address is already registered in the system")
    @AuditLogged(action = AUDIT_ACTION_USER_REGISTERED)
    public Response registerUser(@Valid RegisterRequest registerRequest, @Context SecurityContext securityContext) {
        // [ARC-006] Log entry gate transaction payload securely with masking
        LOGGER.infof(LOG_PROCESS_REGISTRATION_START, maskEmail(registerRequest.getEmail()));

        try {
            // Delegate core persistence, validation, and token generation to AuthService
            AuthResponse authResponse = authService.register(registerRequest);

            // [ARC-006] Log successful completion of the registration workflow
            LOGGER.infof(LOG_PROCESS_REGISTRATION_SUCCESS, authResponse.getUserId(), authResponse.getRole());

            // Return HTTP 201 Created along with authentication tokens and metadata
            return Response.status(HTTP_STATUS_CREATED)
                    .entity(authResponse)
                    .build();

        } catch (Exception e) {
            // [ARC-007] Comprehensive exception logging with subsystem context and trace tags
            LOGGER.errorf("[CRITICAL FAIL] [REQ-001] [EXC-004] User registration failed for email: {}. Raw error: {}", 
                    maskEmail(registerRequest.getEmail()), e.getMessage());
            
            // Re-throw or let the GlobalExceptionMapper translate business exceptions into proper HTTP contracts
            throw e;
        }
    }

    /**
     * Utility method to implement sensitive data masking for log emission.
     * Obfuscates email addresses to comply with the Enterprise Logging & Sensitive Data Masking Framework.
     * 
     * @param email the cleartext email string
     * @return masked email string (e.g., n***@domain.com)
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***MASKED***";
        }
        int idx = email.indexOf('@');
        String name = email.substring(0, idx);
        String domain = email.substring(idx);
        if (name.length() <= 1) {
            return "*" + domain;
        }
        return name.charAt(0) + "***" + domain;
    }
}