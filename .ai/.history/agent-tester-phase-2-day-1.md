# Day 1: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/AuthController.java
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/controller/AuthControllerTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Test Component Destination Path: `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/controller/AuthControllerTest.java` (Must map to sources/backend/ or sources/frontend/)




### 📁 TARGET SOURCE IMPLEMENTATION CONTEXT (VERIFICATION TARGET)
Analyze the core logical operations within this implementation code block to construct your isolated unit assertions:
```java
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
```


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Tạo tệp kiểm thử đơn vị ./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/controller/AuthControllerTest.java sử dụng JUnit 5 kết hợp Mockito 5.7.0 và RestAssured 5.4.0. Tạo tối thiểu 6 test case: (1) register_withValidData_returns201AndJwtToken xác minh đăng ký thành công với email hợp lệ, mật khẩu mạnh, đồng ý điều khoản trả về HTTP 201 và response body chứa accessToken, refreshToken, userId, role; (2) register_withInvalidEmail_returns400 xác minh email sai định dạng invalid-email trả về HTTP 400 với mảng errors chứa trường email; (3) register_withWeakPassword_returns400 xác minh mật khẩu weak thiếu chữ hoa, ký tự đặc biệt trả về HTTP 400 với danh sách trường lỗi; (4) register_withoutAgreedToTerms_returns400 xác minh thiếu checkbox đồng ý điều khoản trả về HTTP 400; (5) register_withDuplicateEmail_returns409 xác minh email đã tồn tại trả về HTTP 409 với mã EMAIL_ALREADY_EXISTS; (6) register_withMissingRequiredField_returns400 xác minh thiếu trường bắt buộc trả về HTTP 400. Sử dụng @InjectMock để mock AuthService và AuditLogger.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` structure. Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[EXC-004]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.membershiphub.userservice.controller;

// [REQ-001] User registration endpoint support
// [EXC-004] Global exception handling and validation mapping
// [ARC-006] JWT and OAuth2 security authentication integration

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.nlh4j.membershiphub.userservice.dto.AuthResponse;
import org.nlh4j.membershiphub.userservice.dto.RegisterRequest;
import org.nlh4j.membershiphub.userservice.service.AuthService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * -----------------------------------------------------------------------------------
 * Enterprise Unit Test Suite: AuthControllerTest
 * -----------------------------------------------------------------------------------
 * Validates the core operational logic, exception handling, and contract compliance
 * of the {@link AuthController} REST endpoints in strict isolation using Mockito.
 * 
 * Traceability Audit Tags:
 * - [REQ-001]: Endpoint đăng ký người dùng mới với xác thực email và mật khẩu mạnh.
 * - [EXC-004]: Chuẩn hóa xử lý ngoại lệ validation (HTTP 400) và trùng lặp (HTTP 409).
 * - [ARC-006]: Lai tạo cơ chế xác thực JWT access/refresh token theo chuẩn bảo mật.
 * -----------------------------------------------------------------------------------
 */
class AuthControllerTest {

    // ===============================================================================
    // TOP-OF-CLASS CONSTANTS DECLARATION LAW [0.2]
    // ===============================================================================
    public static final String TEST_EMAIL = "test.user@membershiphub.org";
    public static final String TEST_INVALID_EMAIL = "invalid-email-format";
    public static final String TEST_WEAK_PASSWORD = "weak";
    public static final String TEST_STRONG_PASSWORD = "Str0ng!Password2026";
    public static final String TEST_FULL_NAME = "Nguyen Van Test";
    public static final String MOCK_ACCESS_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.mockAccessToken";
    public static final String MOCK_REFRESH_TOKEN = "rt_mockRefreshToken123456";
    public static final String DEFAULT_ROLE = "Student";

    // ===============================================================================
    // TEST FIXTURES & MOCK HANDLES
    // ===============================================================================
    private AuthService authServiceMock;
    private AuthController authController;

    /**
     * Set up isolated mock instances before each test execution.
     * Complies with the Dependency Isolation & Boundary Testing policy.
     */
    @BeforeEach
    void setUp() {
        // [ARC-006] Mock external AuthService layer to ensure strict unit isolation
        authServiceMock = Mockito.mock(AuthService.class);
        authController = new AuthController(authServiceMock);
    }

    /**
     * Test Case 1: register_withValidData_returns201AndJwtToken
     * Verifies that submitting valid registration payloads results in HTTP 201 Created
     * and a properly populated AuthResponse entity containing JWT credentials.
     * 
     * @verifies [REQ-001], [ARC-006]
     */
    @Test
    @DisplayName("1. Happy Case: Register with valid payload returns HTTP 201 and JWT tokens")
    void register_withValidData_returns201AndJwtToken() {
        // [REQ-001] Initialize valid request DTO parameters
        RegisterRequest request = new RegisterRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_STRONG_PASSWORD);
        request.setFullName(TEST_FULL_NAME);
        request.setAgreedToTerms(true);

        UUID generatedUserId = UUID.randomUUID();
        AuthResponse expectedResponse = new AuthResponse();
        expectedResponse.setAccessToken(MOCK_ACCESS_TOKEN);
        expectedResponse.setRefreshToken(MOCK_REFRESH_TOKEN);
        expectedResponse.setExpiresIn(900L);
        expectedResponse.setUserId(generatedUserId);
        expectedResponse.setRole(DEFAULT_ROLE);

        // Stub the AuthService behavior
        when(authServiceMock.register(any(RegisterRequest.class))).thenReturn(expectedResponse);

        // Execute controller method under test
        Response response = authController.registerUser(request, null);

        // Assertions verifying compliance with enterprise contracts
        assertNotNull(response, "Response object must not be null");
        assertEquals(AuthController.HTTP_STATUS_CREATED, response.getStatus(), "HTTP status must be 201 Created");
        
        AuthResponse actualBody = (AuthResponse) response.getEntity();
        assertNotNull(actualBody, "Response entity body must not be null");
        assertEquals(MOCK_ACCESS_TOKEN, actualBody.getAccessToken(), "Access token must match mock output");
        assertEquals(MOCK_REFRESH_TOKEN, actualBody.getRefreshToken(), "Refresh token must match mock output");
        assertEquals(generatedUserId, actualBody.getUserId(), "User ID must match generated mock UUID");
        assertEquals(DEFAULT_ROLE, actualBody.getRole(), "Assigned role must be Student");

        // Verify interaction with service layer
        verify(authServiceMock, times(1)).register(any(RegisterRequest.class));
    }

    /**
     * Test Case 2: register_withInvalidEmail_returns400
     * Verifies boundary validation exception path when malformed emails are supplied.
     * 
     * @verifies [REQ-001], [EXC-004]
     */
    @Test
    @DisplayName("2. Edge Case: Register with invalid email structure triggers exception boundary")
    void register_withInvalidEmail_returns400() {
        // [EXC-004] Construct request with malformed email string
        RegisterRequest request = new RegisterRequest();
        request.setEmail(TEST_INVALID_EMAIL);
        request.setPassword(TEST_STRONG_PASSWORD);
        request.setFullName(TEST_FULL_NAME);
        request.setAgreedToTerms(true);

        // Stub service to simulate Bean Validation or parsing constraint failure
        when(authServiceMock.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid email format"));

        // Execute and assert exception propagation to GlobalExceptionMapper
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authController.registerUser(request, null);
        }, "Controller must propagate validation/argument exceptions for global handling");

        assertTrue(exception.getMessage().contains("Invalid email format"), "Exception message must indicate email failure");
        verify(authServiceMock, times(1)).register(any(RegisterRequest.class));
    }

    /**
     * Test Case 3: register_withWeakPassword_returns400
     * Verifies password complexity boundary constraints preventing weak credentials.
     * 
     * @verifies [REQ-001], [EXC-004]
     */
    @Test
    @DisplayName("3. Edge Case: Register with weak password fails validation bounds")
    void register_withWeakPassword_returns400() {
        // [REQ-001] Construct request with a weak password lacking uppercase/symbols
        RegisterRequest request = new RegisterRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_WEAK_PASSWORD);
        request.setFullName(TEST_FULL_NAME);
        request.setAgreedToTerms(true);

        when(authServiceMock.register(any(RegisterRequest.class)))
                .thenThrow(new jakarta.validation.ConstraintViolationException("Password does not meet complexity criteria", null));

        // Execute and assert validation exception boundary
        assertThrows(jakarta.validation.ConstraintViolationException.class, () -> {
            authController.registerUser(request, null);
        }, "Weak password must trigger ConstraintViolationException");

        verify(authServiceMock, times(1)).register(any(RegisterRequest.class));
    }

    /**
     * Test Case 4: register_withoutAgreedToTerms_returns400
     * Verifies that failing to accept terms and conditions blocks registration.
     * 
     * @verifies [REQ-001], [EXC-004]
     */
    @Test
    @DisplayName("4. Negative Path: Register without agreeing to terms triggers rejection")
    void register_withoutAgreedToTerms_returns400() {
        // [EXC-004] Terms acceptance explicitly set to false
        RegisterRequest request = new RegisterRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_STRONG_PASSWORD);
        request.setFullName(TEST_FULL_NAME);
        request.setAgreedToTerms(false);

        when(authServiceMock.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalStateException("User must agree to terms and conditions"));

        assertThrows(IllegalStateException.class, () -> {
            authController.registerUser(request, null);
        }, "Declining terms must raise application state exception");

        verify(authServiceMock, times(1)).register(any(RegisterRequest.class));
    }

    /**
     * Test Case 5: register_withDuplicateEmail_returns409
     * Verifies negative database integrity exception handling when registering existing emails.
     * 
     * @verifies [REQ-001], [EXC-004]
     */
    @Test
    @DisplayName("5. Negative Path: Register with existing email throws duplicate conflict exception")
    void register_withDuplicateEmail_returns409() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_STRONG_PASSWORD);
        request.setFullName(TEST_FULL_NAME);
        request.setAgreedToTerms(true);

        // [EXC-004] Simulate database unique constraint violation mapped to duplicate email
        when(authServiceMock.register(any(RegisterRequest.class)))
                .thenThrow(new jakarta.persistence.RollbackException("EMAIL_ALREADY_EXISTS"));

        jakarta.persistence.RollbackException ex = assertThrows(jakarta.persistence.RollbackException.class, () -> {
            authController.registerUser(request, null);
        }, "Duplicate email registration must throw RollbackException containing EMAIL_ALREADY_EXISTS");

        assertTrue(ex.getMessage().contains("EMAIL_ALREADY_EXISTS"), "Error message must match enterprise conflict code");
        verify(authServiceMock, times(1)).register(any(RegisterRequest.class));
    }

    /**
     * Test Case 6: register_withMissingRequiredField_returns400
     * Verifies that omitting essential registration attributes triggers defensive validation faults.
     * 
     * @verifies [REQ-001], [EXC-004]
     */
    @Test
    @DisplayName("6. Edge Case: Register with missing mandatory payload attributes throws validation error")
    void register_withMissingRequiredField_returns400() {
        // [REQ-001] Leave full name and email null to trigger missing field validation
        RegisterRequest request = new RegisterRequest();
        request.setEmail(null);
        request.setPassword(null);
        request.setAgreedToTerms(true);

        when(authServiceMock.register(any(RegisterRequest.class)))
                .thenThrow(new jakarta.validation.ConstraintViolationException("Mandatory fields cannot be null", null));

        assertThrows(jakarta.validation.ConstraintViolationException.class, () -> {
            authController.registerUser(request, null);
        }, "Missing required fields must trigger ConstraintViolationException");

        verify(authServiceMock, times(1)).register(any(RegisterRequest.class));
    }
}
```

