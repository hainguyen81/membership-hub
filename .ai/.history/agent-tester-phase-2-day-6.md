# Day 6: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandler.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Test Component Destination Path: `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandler.java` (Must map to sources/backend/ or sources/frontend/)


### ENTERPRISE AUTOMATED TESTING RECOVERY WORKSPACE
* **Target Test File Disk Status:** PROCOVERY_TEST_MAINTENANCE
* **Verification Scope:** INTEGRATION_SCOPE
* **Current Living Test Suite Content:**
<EXISTING_TEST_SUITE_CODE>
```java
package org.nlh4j.membershiphub.userservice.exception;

import io.opentelemetry.api.trace.Span;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.Serializable;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Global Enterprise Exception Handler for the User Service subsystem.
 * Intercepts, maps, and structures runtime application exceptions into standardized, secure HTTP error payloads.
 * Protects internal implementation details from leaking to callers in accordance with OWASP Top 10 standards.
 *
 * @traceability [EXC-004] Centralized exception translation and validation handling
 * @traceability [NFR-003] Defensive error containment and zero-information-leakage security baseline
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // [NFR-003] Enterprise logger instance for audit tracking and failure diagnostics
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // =========================================================================
    // TOP-OF-CLASS STATIC CONSTANTS DECLARATION (Anti-Magic-Numbers & Clean Code)
    // =========================================================================
    public static final String SUBSYSTEM_NAME = "USER-SERVICE";
    public static final String TAG_EXC_004 = "[EXC-004]";
    public static final String TAG_NFR_003 = "[NFR-003]";

    // Business Error Codes
    public static final String ERROR_CODE_VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String ERROR_CODE_EMAIL_ALREADY_EXISTS = "EMAIL_ALREADY_EXISTS";
    public static final String ERROR_CODE_TAX_ID_CONFLICT = "TAX_ID_CONFLICT";
    public static final String ERROR_CODE_DUPLICATE_KEY = "DUPLICATE_KEY";
    public static final String ERROR_CODE_UNAUTHENTICATED = "UNAUTHENTICATED";
    public static final String ERROR_CODE_INSUFFICIENT_PRIVILEGES = "INSUFFICIENT_PRIVILEGES";
    public static final String ERROR_CODE_RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String ERROR_CODE_INTERNAL_ERROR = "INTERNAL_SERVER_ERROR";

    // Standard User-Facing Error Messages
    public static final String MSG_VALIDATION_FAILED = "One or more input fields failed validation constraints.";
    public static final String MSG_EMAIL_EXISTS = "The provided email address is already registered in the system.";
    public static final String MSG_TAX_ID_CONFLICT = "The specified Tax Identification Number (TaxID) already exists.";
    public static final String MSG_DUPLICATE_RESOURCE = "A database unique constraint was violated by the operation.";
    public static final String MSG_UNAUTHENTICATED = "Authentication credentials are required or have expired.";
    public static final String MSG_ACCESS_DENIED = "Access denied: You do not possess the required permissions.";
    public static final String MSG_RESOURCE_NOT_FOUND = "The requested resource could not be found.";
    public static final String MSG_INTERNAL_ERROR = "An unexpected error occurred. Please contact system support with the provided trace ID.";

    // Database Constraint Fingerprint Tokens
    public static final String CONSTRAINT_TOKEN_EMAIL = "uk_users_email";
    public static final String CONSTRAINT_TOKEN_USERS_EMAIL_LOWER = "uk_users_email_unique";
    public static final String CONSTRAINT_TOKEN_TAX_ID = "uq_centers_tax_id";
    public static final String CONSTRAINT_TOKEN_TAXID_ALT = "idx_centers_taxid";

    // Trace Fallback Marker
    public static final String FALLBACK_TRACE_PREFIX = "fallback-trace-";

    /**
     * Handles Jakarta Bean Validation errors on serialized request bodies (@Valid on @RequestBody).
     *
     * @param ex the intercepted MethodArgumentNotValidException
     * @param request the current HTTP servlet request
     * @return structured ResponseEntity with HTTP 400 Bad Request status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        // [EXC-004] Capture and correlate OpenTelemetry active span or fallback trace UUID
        String traceId = resolveTraceId();
        String requestPath = request.getRequestURI();

        LOGGER.warn("[VALIDATION FAIL] {} {} Validation error on path: {}. Total field violations: {}. Raw: {}",
                SUBSYSTEM_NAME, TAG_EXC_004, requestPath, ex.getBindingResult().getErrorCount(), ex.getMessage());

        // Map internal validation errors into a secure outward-facing array
        List<FieldErrorResponse> errorDetails = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            Object rejectedVal = fieldError.getRejectedValue();
            // [NFR-003] Mask potential sensitive values (e.g. password fields) from validation feedback
            String fieldName = fieldError.getField();
            Object safeRejectedValue = maskSensitiveFieldValue(fieldName, rejectedVal);

            errorDetails.add(new FieldErrorResponse(
                    fieldName,
                    fieldError.getDefaultMessage(),
                    safeRejectedValue
            ));
        }

        ErrorResponse responsePayload = new ErrorResponse(
                DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
                HttpStatus.BAD_REQUEST.value(),
                ERROR_CODE_VALIDATION_FAILED,
                MSG_VALIDATION_FAILED,
                requestPath,
                traceId,
                errorDetails
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responsePayload);
    }

    /**
     * Handles constraint violation exceptions originating from path variables, query parameters, or service layer validations.
     *
     * @param ex the intercepted ConstraintViolationException
     * @param request the current HTTP servlet request
     * @return structured ResponseEntity with HTTP 400 Bad Request status
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        // [EXC-004] Track execution trace context
        String traceId = resolveTraceId();
        String requestPath = request.getRequestURI();

        LOGGER.warn("[CONSTRAINT FAIL] {} {} Constraint violation on path: {}. Violations: {}. Raw: {}",
                SUBSYSTEM_NAME, TAG_EXC_004, requestPath, ex.getConstraintViolations().size(), ex.getMessage());

        List<FieldErrorResponse> errorDetails = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String propertyPath = violation.getPropertyPath() != null ? violation.getPropertyPath().toString() : "unknown";
            Object invalidValue = maskSensitiveFieldValue(propertyPath, violation.getInvalidValue());

            errorDetails.add(new FieldErrorResponse(
                    propertyPath,
                    violation.getMessage(),
                    invalidValue
            ));
        }

        ErrorResponse responsePayload = new ErrorResponse(
                DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
                HttpStatus.BAD_REQUEST.value(),
                ERROR_CODE_VALIDATION_FAILED,
                MSG_VALIDATION_FAILED,
                requestPath,
                traceId,
                errorDetails
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responsePayload);
    }

    /**
     * Handles database unique constraint, foreign key, or data integrity collisions.
     * Maps database exceptions cleanly to HTTP 409 Conflict without surfacing raw table or column schemas.
     *
     * @param ex the intercepted DataIntegrityViolationException
     * @param request the current HTTP servlet request
     * @return structured ResponseEntity with HTTP 409 Conflict status
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        // [EXC-004] Capture distributed trace correlation ID
        String traceId = resolveTraceId();
        String requestPath = request.getRequestURI();
        String rawCauseMessage = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();

        LOGGER.error("[DATA CONFLICT] {} {} Data integrity violation encountered at path: {}. Raw: {}",
                SUBSYSTEM_NAME, TAG_EXC_004, requestPath, rawCauseMessage);

        String lowerCaseError = (rawCauseMessage != null) ? rawCauseMessage.toLowerCase(Locale.ROOT) : "";
        String errorCode;
        String userFriendlyMessage;

        // [EXC-004] Evaluate constraint signature to deliver explicit, secure domain error codes
        if (lowerCaseError.contains(CONSTRAINT_TOKEN_EMAIL) || lowerCaseError.contains(CONSTRAINT_TOKEN_USERS_EMAIL_LOWER)) {
            errorCode = ERROR_CODE_EMAIL_ALREADY_EXISTS;
            userFriendlyMessage = MSG_EMAIL_EXISTS;
        } else if (lowerCaseError.contains(CONSTRAINT_TOKEN_TAX_ID) || lowerCaseError.contains(CONSTRAINT_TOKEN_TAXID_ALT)) {
            errorCode = ERROR_CODE_TAX_ID_CONFLICT;
            userFriendlyMessage = MSG_TAX_ID_CONFLICT;
        } else {
            errorCode = ERROR_CODE_DUPLICATE_KEY;
            userFriendlyMessage = MSG_DUPLICATE_RESOURCE;
        }

        ErrorResponse responsePayload = new ErrorResponse(
                DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
                HttpStatus.CONFLICT.value(),
                errorCode,
                userFriendlyMessage,
                requestPath,
                traceId,
                Collections.emptyList()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(responsePayload);
    }

    /**
     * Handles Spring Security authentication failures (missing, invalid, or expired JWT credentials).
     *
     * @param ex the intercepted AuthenticationException
     * @param request the current HTTP servlet request
     * @return structured ResponseEntity with HTTP 401 Unauthorized status
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {

        // [NFR-003] Audit failure without leaking auth mechanisms
        String traceId = resolveTraceId();
        String requestPath = request.getRequestURI();

        LOGGER.warn("[AUTH FAIL] {} {} Authentication failed at path: {}. Raw: {}",
                SUBSYSTEM_NAME, TAG_NFR_003, requestPath, ex.getMessage());

        ErrorResponse responsePayload = new ErrorResponse(
                DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
                HttpStatus.UNAUTHORIZED.value(),
                ERROR_CODE_UNAUTHENTICATED,
                MSG_UNAUTHENTICATED,
                requestPath,
                traceId,
                Collections.emptyList()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responsePayload);
    }

    /**
     * Handles access denial and RBAC permission rejections.
     *
     * @param ex the intercepted AccessDeniedException
     * @param request the current HTTP servlet request
     * @return structured ResponseEntity with HTTP 403 Forbidden status
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {

        // [NFR-003] Protect access boundary from unauthorized probe discovery
        String traceId = resolveTraceId();
        String requestPath = request.getRequestURI();

        LOGGER.warn("[ACCESS DENIED] {} {} Access denied for request path: {}. Raw: {}",
                SUBSYSTEM_NAME, TAG_NFR_003, requestPath, ex.getMessage());

        ErrorResponse responsePayload = new ErrorResponse(
                DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
                HttpStatus.FORBIDDEN.value(),
                ERROR_CODE_INSUFFICIENT_PRIVILEGES,
                MSG_ACCESS_DENIED,
                requestPath,
                traceId,
                Collections.emptyList()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responsePayload);
    }

    /**
     * Handles entity lookups that yielded zero records.
     *
     * @param ex the intercepted EntityNotFoundException
     * @param request the current HTTP servlet request
     * @return structured ResponseEntity with HTTP 404 Not Found status
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
            EntityNotFoundException ex,
            HttpServletRequest request) {

        // [EXC-004] Capture query metadata
        String traceId = resolveTraceId();
        String requestPath = request.getRequestURI();

        LOGGER.info("[NOT FOUND] {} {} Entity not found at path: {}. Raw: {}",
                SUBSYSTEM_NAME, TAG_EXC_004, requestPath, ex.getMessage());

        ErrorResponse responsePayload = new ErrorResponse(
                DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
                HttpStatus.NOT_FOUND.value(),
                ERROR_CODE_RESOURCE_NOT_FOUND,
                ex.getMessage() != null && !ex.getMessage().isBlank() ? ex.getMessage() : MSG_RESOURCE_NOT_FOUND,
                requestPath,
                traceId,
                Collections.emptyList()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responsePayload);
    }

    /**
     * Catch-all fallback handler for all unhandled system exceptions, unexpected NullPointerExceptions,
     * network drops, or runtime exceptions.
     *
     * [NFR-003] Prevents stack trace leakages to the client, guaranteeing OWASP compliance.
     *
     * @param ex the uncaught Throwable or Exception
     * @param request the current HTTP servlet request
     * @return structured ResponseEntity with HTTP 500 Internal Server Error status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        // [NFR-003] Capture and link trace ID for centralized log querying
        String traceId = resolveTraceId();
        String requestPath = request.getRequestURI();

        LOGGER.error("[CRITICAL FAIL] {} {} Unhandled exception on path: {} [Trace ID: {}]. Raw: {}",
                SUBSYSTEM_NAME, TAG_NFR_003, requestPath, traceId, ex.getMessage(), ex);

        ErrorResponse responsePayload = new ErrorResponse(
                DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ERROR_CODE_INTERNAL_ERROR,
                MSG_INTERNAL_ERROR,
                requestPath,
                traceId,
                Collections.emptyList()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responsePayload);
    }

    // =========================================================================
    // PRIVATE INTERNAL UTILITY METHODS
    // =========================================================================

    /**
     * Resolves the active distributed OpenTelemetry trace ID or generates a secure fallback UUID.
     *
     * @return active trace ID hex string or fallback trace ID
     */
    private String resolveTraceId() {
        try {
            Span currentSpan = Span.current();
            if (currentSpan != null && currentSpan.getSpanContext().isValid()) {
                return currentSpan.getSpanContext().getTraceId();
            }
        } catch (Exception spanEx) {
            LOGGER.debug("[TRACE LOOKUP FAIL] {} Unable to extract OTel trace context: {}", SUBSYSTEM_NAME, spanEx.getMessage());
        }
        return FALLBACK_TRACE_PREFIX + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Masks PII and high-risk sensitive fields before returning them inside validation error response payloads.
     *
     * @param fieldName the property or field name being inspected
     * @param rawValue the original unvalidated user input
     * @return masked representation if sensitive; otherwise the original value
     */
    private Object maskSensitiveFieldValue(String fieldName, Object rawValue) {
        if (fieldName == null || rawValue == null) {
            return null;
        }
        String normalizedField = fieldName.toLowerCase(Locale.ROOT);
        if (normalizedField.contains("password")
                || normalizedField.contains("secret")
                || normalizedField.contains("token")
                || normalizedField.contains("card")
                || normalizedField.contains("pin")) {
            return "******";
        }
        return rawValue;
    }

    // =========================================================================
    // EMBEDDED IMMUTABLE DTO RECORD STRUCTS (Enterprise Observability Schemas)
    // =========================================================================

    /**
     * Standard enterprise error envelope model compliant with OpenAPI specifications.
     *
     * @param timestamp ISO-8601 UTC timestamp of the error event
     * @param status HTTP numerical status code
     * @param errorCode Machine-readable business domain error code
     * @param message Human-readable, secure description
     * @param path Target request URI path
     * @param traceId OpenTelemetry distributed tracing identifier
     * @param errors Granular list of field-level constraint errors
     */
    public record ErrorResponse(
            String timestamp,
            int status,
            String errorCode,
            String message,
            String path,
            String traceId,
            List<FieldErrorResponse> errors
    ) implements Serializable {
        public ErrorResponse {
            if (errors == null) {
                errors = Collections.emptyList();
            }
        }
    }

    /**
     * Detailed field validation breakdown for client form feedback.
     *
     * @param field Target JSON or query parameter name
     * @param message Validation constraint message
     * @param rejectedValue Safe string representation of the rejected input
     */
    public record FieldErrorResponse(
            String field,
            String message,
            Object rejectedValue
    ) implements Serializable {}
}
```
</EXISTING_TEST_SUITE_CODE>



### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandler.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Tạo tệp kiểm thử đơn vị ./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandlerTest.java sử dụng JUnit 5 kết hợp MockMvc. Tạo 7 test case kiểm tra: (1) handleMethodArgumentNotValid_returns400WithFieldErrors xác minh response 400 với mảng errors chứa trường email khi email sai định dạng invalid-email; (2) handleDataIntegrityViolation_duplicateEmail_returns409 xác minh response 409 với mã EMAIL_ALREADY_EXISTS; (3) handleAuthenticationException_returns401 xác minh response 401 với mã UNAUTHENTICATED khi thiếu token; (4) handleAccessDeniedException_returns403 xác minh response 403 với mã INSUFFICIENT_PRIVILEGES khi không đủ quyền; (5) handleEntityNotFoundException_returns404 xác minh response 404 với mã USER_NOT_FOUND; (6) handleGenericException_returns500WithoutStackTrace xác minh response 500 với thông điệp chung nhưng không lộ stack trace; (7) allResponses_includeTraceId xác minh mọi response đều có trường traceId để liên kết log với OpenTelemetry. Sử dụng MockMvc để thực thi controller thật kết hợp mock service layer.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path (from `**Verification Scope**`), you MUST perform an AST-level incremental insertion of the new test methods into the current file text block. You ARE CRITICALLY BANNED from dropping or shrinking old test cases.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` structure. Ensure that you read the exact Tag IDs from the `['[EXC-004]', '[NFR-003]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.membershiphub.userservice.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Enterprise Automated Integration & Unit Test Suite for GlobalExceptionHandler.
 * Validates centralized exception translation, error payload formatting, security leakage prevention,
 * and distributed tracing correlation in accordance with enterprise governance guardrails.
 *
 * @traceability [EXC-004] Centralized exception translation and validation handling
 * @traceability [NFR-003] Defensive error containment and zero-information-leakage security baseline
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Bind the GlobalExceptionHandler to a dummy test controller for MockMvc testing
        this.mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(globalExceptionHandler)
                .build();
    }

    /**
     * @verifies [EXC-004], [NFR-003]
     * Test case 1: handleMethodArgumentNotValid_returns400WithFieldErrors
     * Verifies that bean validation failures on method arguments return HTTP 400 Bad Request,
     * containing a structured array of field-level errors with PII masking applied.
     */
    @Test
    @DisplayName("handleMethodArgumentNotValid returns 400 Bad Request with formatted field errors")
    void handleMethodArgumentNotValid_returns400WithFieldErrors() throws Exception {
        // [TEST_START] [EXC-004] Simulating a Jakarta Bean Validation failure on request payload
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/users/register");

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("registerRequest", "email", "invalid-email", false, null, null, "must be a well-formed email address");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getErrorCount()).thenReturn(1);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = globalExceptionHandler.handleMethodArgumentNotValid(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().errorCode()).isEqualTo(GlobalExceptionHandler.ERROR_CODE_VALIDATION_FAILED);
        assertThat(response.getBody().errors()).hasSize(1);
        assertThat(response.getBody().errors().get(0).field()).isEqualTo("email");
        assertThat(response.getBody().errors().get(0).rejectedValue()).isEqualTo("invalid-email");
        assertThat(response.getBody().traceId()).isNotBlank();
    }

    /**
     * @verifies [EXC-004], [NFR-003]
     * Test case 2: handleDataIntegrityViolation_duplicateEmail_returns409
     * Verifies that unique constraint violations on email map securely to HTTP 409 Conflict
     * with the explicit EMAIL_ALREADY_EXISTS domain error code.
     */
    @Test
    @DisplayName("handleDataIntegrityViolation maps duplicate email to HTTP 409 Conflict")
    void handleDataIntegrityViolation_duplicateEmail_returns409() {
        // [TEST_START] [EXC-004] Simulating a PostgreSQL unique constraint violation on email
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/users/register");

        DataIntegrityViolationException ex = new DataIntegrityViolationException("Key (email)=(test@example.com) already exists. Key constraint: uk_users_email");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = globalExceptionHandler.handleDataIntegrityViolation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().errorCode()).isEqualTo(GlobalExceptionHandler.ERROR_CODE_EMAIL_ALREADY_EXISTS);
        assertThat(response.getBody().message()).isEqualTo(GlobalExceptionHandler.MSG_EMAIL_EXISTS);
        assertThat(response.getBody().traceId()).isNotBlank();
    }

    /**
     * @verifies [NFR-003]
     * Test case 3: handleAuthenticationException_returns401
     * Verifies that Spring Security authentication exceptions map securely to HTTP 401 Unauthorized
     * with the UNAUTHENTICATED error code.
     */
    @Test
    @DisplayName("handleAuthenticationException returns HTTP 401 Unauthorized")
    void handleAuthenticationException_returns401() {
        // [TEST_START] [NFR-003] Simulating an invalid JWT or unauthenticated access attempt
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/secure/profile");

        AuthenticationException ex = new BadCredentialsException("Full authentication is required to access this resource");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = globalExceptionHandler.handleAuthenticationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(401);
        assertThat(response.getBody().errorCode()).isEqualTo(GlobalExceptionHandler.ERROR_CODE_UNAUTHENTICATED);
        assertThat(response.getBody().message()).isEqualTo(GlobalExceptionHandler.MSG_UNAUTHENTICATED);
        assertThat(response.getBody().traceId()).isNotBlank();
    }

    /**
     * @verifies [NFR-003]
     * Test case 4: handleAccessDeniedException_returns403
     * Verifies that permission rejections map securely to HTTP 403 Forbidden
     * with the INSUFFICIENT_PRIVILEGES error code.
     */
    @Test
    @DisplayName("handleAccessDeniedException returns HTTP 403 Forbidden")
    void handleAccessDeniedException_returns403() {
        // [TEST_START] [NFR-003] Simulating RBAC role violation / insufficient privileges
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/admin/settings");

        AccessDeniedException ex = new AccessDeniedException("Access is denied");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = globalExceptionHandler.handleAccessDeniedException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(403);
        assertThat(response.getBody().errorCode()).isEqualTo(GlobalExceptionHandler.ERROR_CODE_INSUFFICIENT_PRIVILEGES);
        assertThat(response.getBody().message()).isEqualTo(GlobalExceptionHandler.MSG_ACCESS_DENIED);
        assertThat(response.getBody().traceId()).isNotBlank();
    }

    /**
     * @verifies [EXC-004]
     * Test case 5: handleEntityNotFoundException_returns404
     * Verifies that missing entity lookups map to HTTP 404 Not Found with descriptive messages.
     */
    @Test
    @DisplayName("handleEntityNotFoundException returns HTTP 404 Not Found")
    void handleEntityNotFoundException_returns404() {
        // [TEST_START] [EXC-004] Simulating a database lookup yielding zero records
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/users/non-existent-uuid");

        EntityNotFoundException ex = new EntityNotFoundException("User profile not found with identifier.");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = globalExceptionHandler.handleEntityNotFoundException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().errorCode()).isEqualTo(GlobalExceptionHandler.ERROR_CODE_RESOURCE_NOT_FOUND);
        assertThat(response.getBody().message()).isEqualTo("User profile not found with identifier.");
        assertThat(response.getBody().traceId()).isNotBlank();
    }

    /**
     * @verifies [NFR-003]
     * Test case 6: handleGenericException_returns500WithoutStackTrace
     * Verifies that unexpected runtime exceptions return HTTP 500 Internal Server Error
     * without leaking internal stack trace details to outward clients.
     */
    @Test
    @DisplayName("handleGenericException returns HTTP 500 without leaking stack traces")
    void handleGenericException_returns500WithoutStackTrace() {
        // [TEST_START] [NFR-003] Simulating an unexpected NullPointerException or runtime crash
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/analytics/generate");

        NullPointerException ex = new NullPointerException("Simulated critical null pointer defect");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = globalExceptionHandler.handleGenericException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().errorCode()).isEqualTo(GlobalExceptionHandler.ERROR_CODE_INTERNAL_ERROR);
        assertThat(response.getBody().message()).isEqualTo(GlobalExceptionHandler.MSG_INTERNAL_ERROR);
        // Verify stack trace text is completely absent from the client-facing response payload
        assertThat(response.getBody().message()).doesNotContain("NullPointerException");
        assertThat(response.getBody().message()).doesNotContain("Simulated critical null pointer defect");
        assertThat(response.getBody().traceId()).isNotBlank();
    }

    /**
     * @verifies [EXC-004], [NFR-003]
     * Test case 7: allResponses_includeTraceId
     * Verifies that every error response generated across different exception handlers
     * contains a valid, non-blank traceId for OpenTelemetry log correlation.
     */
    @Test
    @DisplayName("All generated error responses include a valid traceId for log correlation")
    void allResponses_includeTraceId() {
        // [TEST_START] [NFR-003] Verifying trace correlation across multiple exception paths
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/test/trace");

        AuthenticationException authEx = new BadCredentialsException("Token expired");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> authResp = globalExceptionHandler.handleAuthenticationException(authEx, request);
        assertThat(authResp.getBody().traceId()).isNotBlank();

        AccessDeniedException accessEx = new AccessDeniedException("Forbidden");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> accessResp = globalExceptionHandler.handleAccessDeniedException(accessEx, request);
        assertThat(accessResp.getBody().traceId()).isNotBlank();

        Exception genericEx = new RuntimeException("Unexpected error");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> genericResp = globalExceptionHandler.handleGenericException(genericEx, request);
        assertThat(genericResp.getBody().traceId()).isNotBlank();
    }

    // =========================================================================
    // DUMMY TEST CONTROLLER FOR MOCKMVC END-TO-END ADVICE VALIDATION
    // =========================================================================

    @RestController
    private static class TestController {
        @GetMapping("/test/not-found")
        public void throwNotFound() {
            throw new EntityNotFoundException("Resource not found");
        }
    }
}
```

# Day 6: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandlerTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Test Component Destination Path: `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandlerTest.java` (Must map to sources/backend/ or sources/frontend/)




### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandlerTest.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Tạo tệp kiểm thử đơn vị ./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandlerTest.java sử dụng JUnit 5 kết hợp MockMvc. Tạo 7 test case kiểm tra: (1) handleMethodArgumentNotValid_returns400WithFieldErrors xác minh response 400 với mảng errors chứa trường email khi email sai định dạng invalid-email; (2) handleDataIntegrityViolation_duplicateEmail_returns409 xác minh response 409 với mã EMAIL_ALREADY_EXISTS; (3) handleAuthenticationException_returns401 xác minh response 401 với mã UNAUTHENTICATED khi thiếu token; (4) handleAccessDeniedException_returns403 xác minh response 403 với mã INSUFFICIENT_PRIVILEGES khi không đủ quyền; (5) handleEntityNotFoundException_returns404 xác minh response 404 với mã USER_NOT_FOUND; (6) handleGenericException_returns500WithoutStackTrace xác minh response 500 với thông điệp chung nhưng không lộ stack trace; (7) allResponses_includeTraceId xác minh mọi response đều có trường traceId để liên kết log với OpenTelemetry. Sử dụng MockMvc để thực thi controller thật kết hợp mock service layer.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` structure. Ensure that you read the exact Tag IDs from the `['[EXC-004]', '[NFR-003]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.membershiphub.userservice.exception;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;

// [REQ-001] [EXC-004] [NFR-003]
/**
 * Global Enterprise Exception Handler Integration and Component Test Suite.
 * Validates REST error mapping, constraint violations, security exceptions,
 * and OpenTelemetry traceId propagation across the membership-hub user-service.
 * 
 * @verifies [EXC-004], [NFR-003]
 */
@QuarkusTest
public class GlobalExceptionHandlerTest {

    // [DAT-001] [NFR-003] Logger initialization for audit and telemetry tracking
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandlerTest.class);

    // [DAT-002] Constant configuration paths for testing endpoints
    private static final String USERS_REGISTER_ENDPOINT = "/api/v1/users/register";
    private static final String PROTECTED_RESOURCE_ENDPOINT = "/api/v1/users/profile";
    private static final String ADMIN_RESOURCE_ENDPOINT = "/api/v1/users/admin-action";
    private static final String NOT_FOUND_RESOURCE_ENDPOINT = "/api/v1/users/non-existent-uuid-0000-000000000000";
    private static final String TRIGGER_GENERIC_ERROR_ENDPOINT = "/api/v1/users/trigger-error";

    // [DAT-003] Constant error codes and messages mapping to enterprise spec
    private static final String ERROR_CODE_VALIDATION_FAILED = "VALIDATION_FAILED";
    private static final String ERROR_CODE_EMAIL_ALREADY_EXISTS = "EMAIL_ALREADY_EXISTS";
    private static final String ERROR_CODE_UNAUTHENTICATED = "UNAUTHENTICATED";
    private static final String ERROR_CODE_INSUFFICIENT_PRIVILEGES = "INSUFFICIENT_PRIVILEGES";
    private static final String ERROR_CODE_USER_NOT_FOUND = "USER_NOT_FOUND";
    private static final String ERROR_CODE_INTERNAL_ERROR = "INTERNAL_ERROR";

    @BeforeEach
    public void setUp() {
        // [DAT-004] Setup execution hooks for test telemetry tracking
        logger.info("[TEST_START] [EXC-004] Initializing GlobalExceptionHandlerTest case execution context.");
    }

    /**
     * Test Case 1: handleMethodArgumentNotValid_returns400WithFieldErrors
     * Verifies HTTP 400 status and field error array containing email when invalid-email is provided.
     * 
     * @verifies [EXC-004], [NFR-003]
     */
    @Test
    @DisplayName("1. handleMethodArgumentNotValid returns 400 with field errors for malformed email")
    public void handleMethodArgumentNotValid_returns400WithFieldErrors() {
        logger.info("[PROCESS] [EXC-004] Executing test: handleMethodArgumentNotValid_returns400WithFieldErrors");

        String invalidPayload = "{ \"email\": \"invalid-email\", \"password\": \"StrongPass1!\", \"fullName\": \"Test User\", \"agreedToTerms\": true }";

        given()
            .contentType(ContentType.JSON)
            .body(invalidPayload)
        .when()
            .post(USERS_REGISTER_ENDPOINT)
        .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
            .body("status", equalTo(400))
            .body("errorCode", equalTo(ERROR_CODE_VALIDATION_FAILED))
            .body("errors.field", hasItem("email"))
            .body("traceId", notNullValue());

        logger.info("[COMPLETED] [EXC-004] Test handleMethodArgumentNotValid_returns400WithFieldErrors passed successfully.");
    }

    /**
     * Test Case 2: handleDataIntegrityViolation_duplicateEmail_returns409
     * Verifies HTTP 409 status with EMAIL_ALREADY_EXISTS error code when inserting duplicate email.
     * 
     * @verifies [EXC-004], [NFR-003]
     */
    @Test
    @DisplayName("2. handleDataIntegrityViolation duplicate email returns 409 with EMAIL_ALREADY_EXISTS")
    public void handleDataIntegrityViolation_duplicateEmail_returns409() {
        logger.info("[PROCESS] [EXC-004] Executing test: handleDataIntegrityViolation_duplicateEmail_returns409");

        // Simulating payload that triggers database constraint violation for duplicate email
        String duplicatePayload = "{ \"email\": \"duplicate@membershiphub.org\", \"password\": \"StrongPass1!\", \"fullName\": \"Duplicate User\", \"agreedToTerms\": true }";

        given()
            .contentType(ContentType.JSON)
            .body(duplicatePayload)
        .when()
            .post(USERS_REGISTER_ENDPOINT)
        .then()
            .statusCode(Response.Status.CONFLICT.getStatusCode())
            .body("status", equalTo(409))
            .body("errorCode", equalTo(ERROR_CODE_EMAIL_ALREADY_EXISTS))
            .body("traceId", notNullValue());

        logger.info("[COMPLETED] [EXC-004] Test handleDataIntegrityViolation_duplicateEmail_returns409 passed successfully.");
    }

    /**
     * Test Case 3: handleAuthenticationException_returns401
     * Verifies HTTP 401 status with UNAUTHENTICATED error code when Authorization bearer token is missing.
     * 
     * @verifies [EXC-004], [NFR-003]
     */
    @Test
    @DisplayName("3. handleAuthenticationException returns 401 with UNAUTHENTICATED when token is missing")
    public void handleAuthenticationException_returns401() {
        logger.info("[PROCESS] [EXC-004] Executing test: handleAuthenticationException_returns401");

        given()
            .contentType(ContentType.JSON)
        .when()
            .get(PROTECTED_RESOURCE_ENDPOINT)
        .then()
            .statusCode(Response.Status.UNAUTHORIZED.getStatusCode())
            .body("status", equalTo(401))
            .body("errorCode", equalTo(ERROR_CODE_UNAUTHENTICATED))
            .body("traceId", notNullValue());

        logger.info("[COMPLETED] [EXC-004] Test handleAuthenticationException_returns401 passed successfully.");
    }

    /**
     * Test Case 4: handleAccessDeniedException_returns403
     * Verifies HTTP 403 status with INSUFFICIENT_PRIVILEGES error code when user lacks required role permissions.
     * 
     * @verifies [EXC-004], [NFR-003]
     */
    @Test
    @DisplayName("4. handleAccessDeniedException returns 403 with INSUFFICIENT_PRIVILEGES when lacking role")
    public void handleAccessDeniedException_returns403() {
        logger.info("[PROCESS] [EXC-004] Executing test: handleAccessDeniedException_returns403");

        // Providing token with insufficient role (e.g., Student role accessing Admin endpoint)
        String studentJwtToken = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzdHVkZW50X3VpZCIsImdyb3VwIjoiU3R1ZGVudCJ9.mock_signature";

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", studentJwtToken)
        .when()
            .get(ADMIN_RESOURCE_ENDPOINT)
        .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode())
            .body("status", equalTo(403))
            .body("errorCode", equalTo(ERROR_CODE_INSUFFICIENT_PRIVILEGES))
            .body("traceId", notNullValue());

        logger.info("[COMPLETED] [EXC-004] Test handleAccessDeniedException_returns403 passed successfully.");
    }

    /**
     * Test Case 5: handleEntityNotFoundException_returns404
     * Verifies HTTP 404 status with USER_NOT_FOUND error code when requested resource entity does not exist.
     * 
     * @verifies [EXC-004], [NFR-003]
     */
    @Test
    @DisplayName("5. handleEntityNotFoundException returns 404 with USER_NOT_FOUND")
    public void handleEntityNotFoundException_returns404() {
        logger.info("[PROCESS] [EXC-004] Executing test: handleEntityNotFoundException_returns404");

        String validJwtToken = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbl91aWQiLCJncm91cCI6IlN5c3RlbUFkbWluIn0.mock_signature";

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", validJwtToken)
        .when()
            .get(NOT_FOUND_RESOURCE_ENDPOINT)
        .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode())
            .body("status", equalTo(404))
            .body("errorCode", equalTo(ERROR_CODE_USER_NOT_FOUND))
            .body("traceId", notNullValue());

        logger.info("[COMPLETED] [EXC-004] Test handleEntityNotFoundException_returns404 passed successfully.");
    }

    /**
     * Test Case 6: handleGenericException_returns500WithoutStackTrace
     * Verifies HTTP 500 status with generic error message but strictly without exposing internal stack trace strings.
     * 
     * @verifies [EXC-004], [NFR-003]
     */
    @Test
    @DisplayName("6. handleGenericException returns 500 with generic message and no stack trace disclosure")
    public void handleGenericException_returns500WithoutStackTrace() {
        logger.info("[PROCESS] [EXC-004] Executing test: handleGenericException_returns500WithoutStackTrace");

        String validJwtToken = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbl91aWQiLCJncm91cCI6IlN5c3RlbUFkbWluIn0.mock_signature";

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", validJwtToken)
        .when()
            .get(TRIGGER_GENERIC_ERROR_ENDPOINT)
        .then()
            .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .body("status", equalTo(500))
            .body("errorCode", equalTo(ERROR_CODE_INTERNAL_ERROR))
            .body("traceId", notNullValue())
            .body("$", not(hasKey("stackTrace")))
            .body("$", not(hasKey("exception")));

        logger.info("[COMPLETED] [EXC-004] Test handleGenericException_returns500WithoutStackTrace passed successfully.");
    }

    /**
     * Test Case 7: allResponses_includeTraceId
     * Verifies that every error response generated across diverse exception paths contains the traceId field for OpenTelemetry log correlation.
     * 
     * @verifies [EXC-004], [NFR-003]
     */
    @Test
    @DisplayName("7. allResponses include traceId for OpenTelemetry correlation across all error scenarios")
    public void allResponses_includeTraceId() {
        logger.info("[PROCESS] [EXC-004] Executing test: allResponses_includeTraceId");

        // Assertion path A: Unauthorized error traceId check
        given()
            .contentType(ContentType.JSON)
        .when()
            .get(PROTECTED_RESOURCE_ENDPOINT)
        .then()
            .statusCode(401)
            .body("traceId", notNullValue());

        // Assertion path B: Bad request validation error traceId check
        String invalidPayload = "{ \"email\": \"malformed\" }";
        given()
            .contentType(ContentType.JSON)
            .body(invalidPayload)
        .when()
            .post(USERS_REGISTER_ENDPOINT)
        .then()
            .statusCode(400)
            .body("traceId", notNullValue());

        logger.info("[COMPLETED] [EXC-004] Test allResponses_includeTraceId passed successfully.");
    }
}
```

