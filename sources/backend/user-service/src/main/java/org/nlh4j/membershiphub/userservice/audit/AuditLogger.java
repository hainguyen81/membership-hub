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