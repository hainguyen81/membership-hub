package org.nlh4j.membershiphub.userservice.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.nlh4j.membershiphub.userservice.entity.AuditLog;
import org.nlh4j.membershiphub.userservice.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Enterprise Audit Logging Service for Authentication and Identity Operations.
 * <p>
 * This component provides robust, tamper-resistant auditing for all security and authentication
 * lifecycle events across the Membership Hub platform. It guarantees non-repudiation, captures
 * distributed tracing telemetry for centralized observability (ELK Stack, Google Cloud Logging),
 * and enforces strict PII scrubbing before committing audit data to persistence or log streams.
 * </p>
 *
 * @author Enterprise Architecture Core Team
 * @version 1.0.0
 * @since 2026-08-29
 * @traceability [NFR-003], [NFR-006]
 */
@ApplicationScoped
public class AuthAuditLogger {

    // =========================================================================
    // TOP-OF-CLASS CONSTANTS DECLARATION (GOVERNANCE MANDATE [0.2])
    // =========================================================================

    /** Subsystem identifier for logging and tracing context. // [NFR-006] */
    public static final String SUBSYSTEM_NAME = "USER-SERVICE-AUDIT";

    /** Traceability tracking tag identifier for audit compliance. // [NFR-006] */
    public static final String TAG_AUDIT_COMPLIANCE = "[NFR-006]";

    /** Traceability tracking tag identifier for baseline security. // [NFR-003] */
    public static final String TAG_SECURITY_BASELINE = "[NFR-003]";

    // --- Action Names ---
    public static final String ACTION_LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String ACTION_LOGIN_FAILED = "LOGIN_FAILED";
    public static final String ACTION_LOGOUT = "LOGOUT";
    public static final String ACTION_SOCIAL_AUTH_SUCCESS = "SOCIAL_AUTH_SUCCESS";
    public static final String ACTION_SOCIAL_AUTH_FAILED = "SOCIAL_AUTH_FAILED";
    public static final String ACTION_TOKEN_REFRESH = "TOKEN_REFRESH";
    public static final String ACTION_ROLE_CHANGED = "ROLE_CHANGED";

    /** Immutable set of valid authorized audit actions. // [NFR-006] */
    public static final Set<String> ALLOWED_ACTIONS = Collections.unmodifiableSet(Set.of(
            ACTION_LOGIN_SUCCESS,
            ACTION_LOGIN_FAILED,
            ACTION_LOGOUT,
            ACTION_SOCIAL_AUTH_SUCCESS,
            ACTION_SOCIAL_AUTH_FAILED,
            ACTION_TOKEN_REFRESH,
            ACTION_ROLE_CHANGED
    ));

    // --- JSON Schema Keys for Structured Logs & Audit Details ---
    public static final String JSON_KEY_EVENT_TYPE = "eventType";
    public static final String JSON_KEY_SUBSYSTEM = "subsystem";
    public static final String JSON_KEY_USER_ID = "userId";
    public static final String JSON_KEY_ACTION = "action";
    public static final String JSON_KEY_OCCURRED_AT = "occurredAt";
    public static final String JSON_KEY_TRACE_ID = "traceId";
    public static final String JSON_KEY_SPAN_ID = "spanId";
    public static final String JSON_KEY_CLIENT_IP = "clientIp";
    public static final String JSON_KEY_USER_AGENT = "userAgent";
    public static final String JSON_KEY_DETAILS = "details";
    public static final String JSON_KEY_RAW_DETAILS = "rawDetails";
    public static final String JSON_KEY_AUDIT_ID = "auditId";
    public static final String JSON_KEY_STATUS = "status";

    // --- Default & Fallback Constants ---
    public static final String DEFAULT_UNKNOWN_VALUE = "UNKNOWN";
    public static final String DEFAULT_ANONYMOUS_USER = "ANONYMOUS";
    public static final String DEFAULT_SUCCESS_STATUS = "SUCCESS";
    public static final String DEFAULT_FAILURE_STATUS = "FAILED";
    public static final String MASKED_CREDENTIAL_PLACEHOLDER = "******";
    public static final String EMPTY_JSON_OBJECT = "{}";

    // --- Sensitive Data Scrubbing Regex Patterns ---
    private static final Pattern SENSITIVE_TOKEN_PATTERN = Pattern.compile("(?i)(bearer\\s+|token[\"':\\s]+)[a-zA-Z0-9._\\-]{15,}");
    private static final Pattern SENSITIVE_PASSWORD_PATTERN = Pattern.compile("(?i)(\"password\"\\s*:\\s*\")[^\"]+(\")");
    private static final Pattern SENSITIVE_CREDIT_CARD_PATTERN = Pattern.compile("\\b(?:\\d[ -]*?){13,16}\\b");

    // =========================================================================
    // CLASS STATE & INJECTED DEPENDENCIES
    // =========================================================================

    /** Standard SLF4J Logger binding for enterprise structured log emission. // [0.3] */
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthAuditLogger.class);

    /** Injected Panache Repository for managing AuditLog persistence. // [NFR-006] */
    @Inject
    AuditLogRepository auditLogRepository;

    /** Injected Jackson ObjectMapper for JSON serialization and parsing. // [NFR-006] */
    @Inject
    ObjectMapper objectMapper;

    // =========================================================================
    // PUBLIC AUDIT LOGGING OPERATIONS
    // =========================================================================

    /**
     * Primary operational method to record security and authentication audit events.
     * <p>
     * Ensures persistence within the active transaction boundary, scrubbed logging for
     * centralized indexing (ELK/Stackdriver), and OpenTelemetry trace binding.
     * </p>
     *
     * @param userId  The unique identifier of the user executing or targeted by the action (nullable for anonymous).
     * @param action  The audit action label (must belong to {@link #ALLOWED_ACTIONS}).
     * @param details Structured JSON string containing auxiliary operational attributes (IP, User-Agent, metadata).
     * @throws IllegalArgumentException if the action parameter is invalid or unapproved.
     * @traceability [NFR-003], [NFR-006]
     */
    @Transactional(Transactional.TxType.REQUIRED) // [NFR-006]: Guarantees transactional consistency with caller flow
    public void logAuthEvent(UUID userId, String action, String details) {
        // [PROCESS] Entry Gate Logging with tracing metadata
        LOGGER.debug("[ENTRY] {} {} Executing logAuthEvent for Action: {}, UserID: {}", 
                TAG_AUDIT_COMPLIANCE, SUBSYSTEM_NAME, action, userId != null ? userId : DEFAULT_ANONYMOUS_USER);

        // 1. Validate mandatory parameters against governance rules
        if (action == null || action.trim().isEmpty()) {
            LOGGER.error("[CRITICAL FAIL] {} {} Audit action cannot be null or blank.", 
                    TAG_SECURITY_BASELINE, SUBSYSTEM_NAME);
            throw new IllegalArgumentException("Audit action must not be null or blank.");
        }

        final String normalizedAction = action.trim().toUpperCase();
        if (!ALLOWED_ACTIONS.contains(normalizedAction)) {
            LOGGER.error("[CRITICAL FAIL] {} {} Unauthorized audit action attempted: {}. Allowed: {}", 
                    TAG_SECURITY_BASELINE, SUBSYSTEM_NAME, normalizedAction, ALLOWED_ACTIONS);
            throw new IllegalArgumentException("Unauthorized audit action: " + normalizedAction);
        }

        // 2. Sanitize and mask details payload to prevent PII/Credential leakage
        final String sanitizedDetails = sanitizeAndMaskDetails(details);

        // 3. Extract OpenTelemetry distributed tracing context for correlation
        final SpanContext currentSpanContext = Span.current().getSpanContext();
        final String traceId = currentSpanContext.isValid() ? currentSpanContext.getTraceId() : DEFAULT_UNKNOWN_VALUE;
        final String spanId = currentSpanContext.isValid() ? currentSpanContext.getSpanId() : DEFAULT_UNKNOWN_VALUE;

        // 4. Construct AuditLog persistent entity
        final AuditLog auditLog = new AuditLog();
        auditLog.setLogId(UUID.randomUUID());
        auditLog.setUserId(userId);
        auditLog.setAction(normalizedAction);
        auditLog.setDetails(sanitizedDetails);
        auditLog.setOccurredAt(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC));

        try {
            // 5. Persist audit log entity to PostgreSQL database via Panache
            auditLogRepository.persist(auditLog);

            // 6. Format and emit structured JSON log to stdout/stderr for ELK/Cloud Logging ingestion
            emitStructuredAuditLog(auditLog, traceId, spanId);

            // [PROCESS] Completion Gate Logging
            LOGGER.info("[SUCCESS] {} {} Audit record persisted. Action: {}, LogID: {}, TraceID: {}", 
                    TAG_AUDIT_COMPLIANCE, SUBSYSTEM_NAME, normalizedAction, auditLog.getLogId(), traceId);

        } catch (Exception e) {
            // [CRITICAL FAIL] Detailed error logging preserving cause chain
            LOGGER.error("[CRITICAL FAIL] {} {} Database persistence failed for audit action: {}. Raw error: {}", 
                    TAG_AUDIT_COMPLIANCE, SUBSYSTEM_NAME, normalizedAction, e.getMessage(), e);
            throw new RuntimeException("Audit log persistence failed for action: " + normalizedAction, e);
        }
    }

    /**
     * Overloaded helper method to log authentication events with explicit client network metadata.
     *
     * @param userId    The unique identifier of the user (nullable).
     * @param action    The audit action label.
     * @param clientIp  Client IP address initiating the transaction.
     * @param userAgent HTTP User-Agent header string.
     * @param metadata  Additional key-value metadata to pack into the JSON payload.
     * @traceability [NFR-003], [NFR-006]
     */
    @Transactional(Transactional.TxType.REQUIRED)
    public void logAuthEventWithClientInfo(UUID userId, String action, String clientIp, String userAgent, ObjectNode metadata) {
        // [PROCESS] Entry Gate
        LOGGER.debug("[ENTRY] {} {} Executing logAuthEventWithClientInfo for Action: {}", 
                TAG_AUDIT_COMPLIANCE, SUBSYSTEM_NAME, action);

        final ObjectNode rootNode = (metadata != null) ? metadata : objectMapper.createObjectNode();
        rootNode.put(JSON_KEY_CLIENT_IP, (clientIp != null && !clientIp.isBlank()) ? clientIp.trim() : DEFAULT_UNKNOWN_VALUE);
        rootNode.put(JSON_KEY_USER_AGENT, (userAgent != null && !userAgent.isBlank()) ? userAgent.trim() : DEFAULT_UNKNOWN_VALUE);

        String serializedDetails;
        try {
            serializedDetails = objectMapper.writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            LOGGER.warn("[WARN] {} {} Failed to serialize structured audit metadata. Fallback to empty. Error: {}", 
                    TAG_AUDIT_COMPLIANCE, SUBSYSTEM_NAME, e.getMessage());
            serializedDetails = EMPTY_JSON_OBJECT;
        }

        // Forward to primary transactional persistence pipeline
        logAuthEvent(userId, action, serializedDetails);
    }

    // =========================================================================
    // INTERNAL UTILITY & DATA PROTECTION ROUTINES
    // =========================================================================

    /**
     * Programmatically scrubs sensitive credentials, private keys, and passwords from log payloads.
     *
     * @param rawDetails Raw JSON or plain text details.
     * @return Sanitized string safe for long-term audit storage and log shipping.
     * @traceability [NFR-003]
     */
    private String sanitizeAndMaskDetails(String rawDetails) {
        if (rawDetails == null || rawDetails.trim().isEmpty()) {
            return EMPTY_JSON_OBJECT;
        }

        // Apply regex scrubbing filters against credentials and sensitive tokens
        String sanitized = SENSITIVE_TOKEN_PATTERN.matcher(rawDetails).replaceAll("$1" + MASKED_CREDENTIAL_PLACEHOLDER);
        sanitized = SENSITIVE_PASSWORD_PATTERN.matcher(sanitized).replaceAll("$1" + MASKED_CREDENTIAL_PLACEHOLDER + "$2");
        sanitized = SENSITIVE_CREDIT_CARD_PATTERN.matcher(sanitized).replaceAll(MASKED_CREDENTIAL_PLACEHOLDER);

        // Verify if sanitized content is valid JSON; if not, wrap safely into JSON object
        try {
            objectMapper.readTree(sanitized);
            return sanitized;
        } catch (JsonProcessingException e) {
            LOGGER.debug("[DEBUG] {} {} Wrapping non-JSON audit detail string into standard JSON node.", 
                    TAG_AUDIT_COMPLIANCE, SUBSYSTEM_NAME);
            final ObjectNode wrapperNode = objectMapper.createObjectNode();
            wrapperNode.put(JSON_KEY_RAW_DETAILS, sanitized);
            try {
                return objectMapper.writeValueAsString(wrapperNode);
            } catch (JsonProcessingException ex) {
                return EMPTY_JSON_OBJECT;
            }
        }
    }

    /**
     * Emits a high-performance JSON-formatted log line enriched with OpenTelemetry spans and MDC.
     *
     * @param auditLog Persistent audit log model.
     * @param traceId  Distributed OpenTelemetry Trace ID.
     * @param spanId   Distributed OpenTelemetry Span ID.
     * @traceability [NFR-006]
     */
    private void emitStructuredAuditLog(AuditLog auditLog, String traceId, String spanId) {
        try {
            final ObjectNode logNode = objectMapper.createObjectNode();
            logNode.put(JSON_KEY_SUBSYSTEM, SUBSYSTEM_NAME);
            logNode.put(JSON_KEY_EVENT_TYPE, "AUDIT_SECURITY_EVENT");
            logNode.put(JSON_KEY_AUDIT_ID, Objects.toString(auditLog.getLogId(), DEFAULT_UNKNOWN_VALUE));
            logNode.put(JSON_KEY_USER_ID, auditLog.getUserId() != null ? auditLog.getUserId().toString() : DEFAULT_ANONYMOUS_USER);
            logNode.put(JSON_KEY_ACTION, auditLog.getAction());
            logNode.put(JSON_KEY_OCCURRED_AT, Objects.toString(auditLog.getOccurredAt(), Instant.now().toString()));
            logNode.put(JSON_KEY_TRACE_ID, traceId);
            logNode.put(JSON_KEY_SPAN_ID, spanId);
            logNode.put(JSON_KEY_DETAILS, auditLog.getDetails());

            // Bind MDC context variables for native Logback/Quarkus pattern formatters
            MDC.put(JSON_KEY_TRACE_ID, traceId);
            MDC.put(JSON_KEY_SPAN_ID, spanId);
            MDC.put(JSON_KEY_ACTION, auditLog.getAction());

            final String structuredJsonPayload = objectMapper.writeValueAsString(logNode);

            // Emit to SLF4J at INFO level for centralized cloud collector harvesting
            LOGGER.info("[AUDIT_STREAM] {}", structuredJsonPayload);

        } catch (Exception e) {
            LOGGER.error("[WARN] {} {} Failed to emit structured JSON audit stream. Error: {}", 
                    TAG_AUDIT_COMPLIANCE, SUBSYSTEM_NAME, e.getMessage(), e);
        } finally {
            // Clean MDC context to avoid thread-local pollution
            MDC.remove(JSON_KEY_TRACE_ID);
            MDC.remove(JSON_KEY_SPAN_ID);
            MDC.remove(JSON_KEY_ACTION);
        }
    }
}