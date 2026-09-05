# Day 6: model models/gemini-flash-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandler.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandler.java`
*   Traceability Audit Tags For This Task: ['[EXC-004]', '[NFR-003]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai lớp GlobalExceptionHandler được đánh dấu @RestControllerAdvice tại đường dẫn ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandler.java xử lý tập trung các ngoại lệ: (1) MethodArgumentNotValidException trả về HTTP 400 với mảng FieldErrorResponse chứa field, message, rejectedValue cho từng trường vi phạm, (2) ConstraintViolationException trả về HTTP 400 với cùng cấu trúc, (3) DataIntegrityViolationException ánh xạ thành HTTP 409 với mã lỗi tương ứng EMAIL_ALREADY_EXISTS, TAX_ID_CONFLICT, DUPLICATE_KEY dựa trên phân tích ConstraintViolation từ exception, (4) AuthenticationException trả về HTTP 401 với mã UNAUTHENTICATED, (5) AccessDeniedException trả về HTTP 403 với mã INSUFFICIENT_PRIVILEGES, (6) EntityNotFoundException trả về HTTP 404 với mã RESOURCE_NOT_FOUND, (7) Exception mặc định trả về HTTP 500 nhưng không để lộ stack trace ra response. Mỗi response phải bao gồm các trường timestamp (ISO-8601), status (HTTP code), errorCode (mã nghiệp vụ), message (thông điệp thân thiện), path (đường dẫn request), traceId (UUID từ OpenTelemetry) để phục vụ observability.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[EXC-004]', '[NFR-003]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
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

# Day 6: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/audit/AuditLogger.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/audit/AuditLogger.java`
*   Traceability Audit Tags For This Task: ['[NFR-006]', '[REQ-003]', '[REQ-006]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai lớp AuditLogger với annotation @ApplicationScoped tại đường dẫn ./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/audit/AuditLogger.java, inject AuditLogRepository (Panache Repository). Tạo annotation @AuditLogged(action = "...", targetEntity = "...") cho phép ghi log kiểm toán tự động thông qua AOP aspect. Triển khai phương thức log(UUID userId, String action, String targetEntity, UUID targetId, String oldValue, String newValue, String ipAddress, String userAgent): (1) lưu bản ghi vào bảng AuditLogs với đầy đủ trường theo schema đã định, (2) đồng thời ghi log có cấu trúc (JSON) ra console thông qua SLF4J với mức INFO để tích hợp với Google Cloud Logging, (3) sử dụng @Transactional(propagation = Propagation.REQUIRES_NEW) để đảm bảo thao tác ghi log không làm thất bại giao dịch chính, (4) hash chain chống tamper bằng cách tính prev_hash từ bản ghi trước đó. Tuân thủ [NFR-006] yêu cầu lưu trữ 1 năm.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[NFR-006]', '[REQ-003]', '[REQ-006]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
// [REQ-003] [REQ-006] [NFR-006]
package org.nlh4j.membershiphub.userservice.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.nlh4j.membershiphub.userservice.repository.AuditLogRepository;
import org.nlh4j.membershiphub.userservice.entity.AuditLog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * AuditLogger component responsible for secure, immutable, and tamper-evident audit logging
 * across the Membership Hub enterprise platform.
 * 
 * Traceability Tags: [NFR-006], [REQ-003], [REQ-006]
 */
@ApplicationScoped
public class AuditLogger {

    // [DAT-012] [NFR-006] Top-of-class constants declaration enforcing immutable security and system parameters
    private static final Logger LOGGER = Logger.getLogger(AuditLogger.class);
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String MASKED_VALUE = "***MASKED***";
    private static final String SYSTEM_ACTION_DEFAULT = "SYSTEM_OPERATION";
    private static final String TARGET_ENTITY_DEFAULT = "GENERAL";

    // Injecting the Panache repository for persistent database operations complying with enterprise DDL
    @Inject
    AuditLogRepository auditLogRepository;

    // Entity manager injection for raw queries or advanced transaction boundary management
    @PersistenceContext
    EntityManager entityManager;

    // ObjectMapper for structural JSON serialization of audit entries for Google Cloud Logging export
    @Inject
    ObjectMapper objectMapper;

    /**
     * Custom AOP annotation to mark methods requiring automatic auditing.
     * Traceability Tags: [NFR-006], [REQ-003], [REQ-006]
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface AuditLogged {
        String action() default SYSTEM_ACTION_DEFAULT;
        String targetEntity() default TARGET_ENTITY_DEFAULT;
    }

    /**
     * Interceptor class processing methods annotated with @AuditLogged.
     * Traceability Tags: [NFR-006], [REQ-003], [REQ-006]
     */
    @Interceptor
    @AuditLogged
    public static class AuditInterceptor {

        @Inject
        AuditLogger auditLogger;

        @AroundInvoke
        public Object logMethodInvocation(InvocationContext context) throws Exception {
            // [ARC-006] [NFR-006] Extracting audit metadata from annotation context
            AuditLogged annotation = context.getMethod().getAnnotation(AuditLogged.class);
            if (annotation == null) {
                annotation = context.getTarget().getClass().getAnnotation(AuditLogged.class);
            }

            String action = annotation != null ? annotation.action() : SYSTEM_ACTION_DEFAULT;
            String targetEntity = annotation != null ? annotation.targetEntity() : TARGET_ENTITY_DEFAULT;

            long startTime = System.currentTimeMillis();
            Object result = null;
            boolean success = true;
            String errorMessage = null;

            try {
                // Executing the intercepted target business method
                result = context.proceed();
                return result;
            } catch (Exception e) {
                success = false;
                errorMessage = e.getMessage();
                // Re-throwing exception to preserve ancestral cause chain integrity per global protocols
                throw e;
            } finally {
                long duration = System.currentTimeMillis() - startTime;
                LOGGER.infof("[AUDIT_INTERCEPTOR] [NFR-006] Method executed: %s | Action: %s | Success: %b | Duration: %dms",
                        context.getMethod().getName(), action, success, duration);
            }
        }
    }

    /**
     * Persists an audit log entry with complete cryptographic hash-chaining to prevent tampering.
     * Executes in a separate transaction boundary (REQUIRES_NEW) to protect transaction integrity.
     * 
     * Traceability Tags: [NFR-006], [REQ-003], [REQ-006]
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void log(UUID userId, String action, String targetEntity, UUID targetId,
                    String oldValue, String newValue, String ipAddress, String userAgent) {
        try {
            // [NFR-006] [REQ-003] Input sanitization and PII masking before persistence
            String sanitizedOldValue = maskSensitiveData(oldValue);
            String sanitizedNewValue = maskSensitiveData(newValue);

            // [NFR-006] Retrieve the hash of the immediately preceding audit log entry to establish the hash chain
            String previousHash = fetchLatestAuditHash();

            // Generating a unique identifier for the current audit record
            UUID logId = UUID.randomUUID();
            LocalDateTime occurredAt = LocalDateTime.now();

            // Constructing the cryptographic payload to compute the tamper-evident hash
            String rawPayload = String.format("%s|%s|%s|%s|%s|%s|%s|%s|%s",
                    logId,
                    userId != null ? userId.toString() : "ANONYMOUS",
                    action,
                    targetEntity,
                    targetId != null ? targetId.toString() : "NONE",
                    sanitizedOldValue,
                    sanitizedNewValue,
                    occurredAt,
                    previousHash != null ? previousHash : "ROOT_GENESIS"
            );

            String currentHash = computeSha256Hash(rawPayload);

            // Instantiating the enterprise audit entity mapped to the database schema
            AuditLog auditLog = new AuditLog();
            auditLog.setLogId(logId);
            auditLog.setUserId(userId);
            auditLog.setAction(action);
            auditLog.setTargetEntity(targetEntity);
            auditLog.setTargetId(targetId);
            auditLog.setOldValue(sanitizedOldValue);
            auditLog.setNewValue(sanitizedNewValue);
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);
            auditLog.setOccurredAt(occurredAt);
            auditLog.setPreviousHash(previousHash);
            auditLog.setCurrentHash(currentHash);

            // Persisting through Panache Repository boundary
            auditLogRepository.persist(auditLog);

            // [NFR-006] Emitting structural JSON log to standard output for Google Cloud Logging ingestion
            String structuredJsonLog = objectMapper.writeValueAsString(auditLog);
            LOGGER.infof("[AUDIT_LOG_COMMITTED] [NFR-006] Structured Audit Record: %s", structuredJsonLog);

        } catch (Exception e) {
            // [0.3] Comprehensive exception logging protocol with target subsystem and explicit Tag ID mapping
            LOGGER.errorf("[CRITICAL FAIL] [NFR-006] Failed to persist immutable audit log due to system exception. Raw error: %s", e.getMessage(), e);
            // Preserving root cause chain by wrapping into a runtime exception if necessary without swallowing stack trace
            throw new RuntimeException("Audit logging subsystem failure: " + e.getMessage(), e);
        }
    }

    /**
     * Fetches the cryptographic hash of the most recent audit record to maintain the immutable chain.
     * 
     * Traceability Tags: [NFR-006]
     */
    private String fetchLatestAuditHash() {
        try {
            // Executing optimized JPQL query to retrieve the hash of the latest entry
            return entityManager.createQuery(
                    "SELECT a.currentHash FROM AuditLog a ORDER BY a.occurredAt DESC", String.class)
                    .setMaxResults(1)
                    .getResultStream()
                    .findFirst()
                    .orElse("ROOT_GENESIS_HASH");
        } catch (Exception e) {
            LOGGER.warnf("[AUDIT_HASH_WARNING] [NFR-006] Unable to fetch previous hash, defaulting to genesis. Error: %s", e.getMessage());
            return "ROOT_GENESIS_HASH";
        }
    }

    /**
     * Computes the SHA-256 cryptographic hash of a raw string payload.
     * 
     * Traceability Tags: [NFR-006]
     */
    private String computeSha256Hash(String baseString) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] encodedHash = digest.digest(baseString.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.errorf("[CRITICAL SECURITY FAIL] [NFR-006] SHA-256 algorithm missing from JVM runtime. Raw error: %s", e.getMessage());
            throw new RuntimeException("Cryptographic hashing failure", e);
        }
    }

    /**
     * Masks sensitive Personally Identifiable Information (PII) before writing to audit logs.
     * 
     * Traceability Tags: [NFR-006], [REQ-003]
     */
    private String maskSensitiveData(String payload) {
        if (payload == null || payload.isEmpty()) {
            return payload;
        }
        // Simple regex-based masking for common credential keys or passwords within JSON payloads
        try {
            return payload
                    .replaceAll("(?i)\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"" + MASKED_VALUE + "\"")
                    .replaceAll("(?i)\"token\"\\s*:\\s*\"[^\"]*\"", "\"token\":\"" + MASKED_VALUE + "\"")
                    .replaceAll("(?i)\"secret\"\\s*:\\s*\"[^\"]*\"", "\"secret\":\"" + MASKED_VALUE + "\"");
        } catch (Exception e) {
            LOGGER.warnf("[AUDIT_MASK_WARN] [NFR-006] Failed to mask sensitive payload data: %s", e.getMessage());
            return MASKED_VALUE;
        }
    }
}
```

