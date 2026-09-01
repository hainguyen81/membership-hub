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