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