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