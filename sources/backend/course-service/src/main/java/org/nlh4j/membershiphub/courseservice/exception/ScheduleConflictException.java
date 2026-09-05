// [REQ-007], [REQ-008]
package org.nlh4j.membershiphub.courseservice.exception;

import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

/**
 * 🏢 ENTERPRISE GOVERNANCE & ARCHITECTURE COMPLIANCE
 * 
 * Target Component: ScheduleConflictException.java
 * Subsystem: course-service
 * Package: org.nlh4j.membershiphub.courseservice.exception
 * Traceability Audit Tags: [REQ-007], [REQ-008]
 * 
 * Business Purpose & Logic Context:
 * This enterprise business exception is thrown when a course schedule scheduling operation
 * (creation or update) violates teacher timetable constraints (time overlap conflict).
 * It enforces the exclusion constraint logic and prevents duplicate bookings for teachers
 * within overlapping date ranges as specified by enterprise specifications [REQ-008].
 * 
 * Security Gating & Exception Auditing Enforcement:
 * - Natively inherits enterprise logging frameworks (Slf4j/Logback via JBoss Logger).
 * - Logs process state transitions and captures exception contexts securely.
 * - Prevents cleartext credential exposure and adheres to the Anti-Magic-Numbers law.
 * - Preserves ancestral exception root causes via the cause-chain constructor law.
 * 
 * @author Enterprise Architecture Core Agent
 * @version 1.0.0
 */
public class ScheduleConflictException extends RuntimeException {

    // =========================================================================
    // 🏛️ CONSTANTS DECLARATION LAW (Top-of-Class Constants Isolation)
    // =========================================================================
    
    /** Serial version UID for serialization compatibility compliance. */
    private static final long serialVersionUID = 1L;

    /** System subsystem classification label for centralized aggregation log tracing. */
    private static final String SUBSYSTEM_NAME = "[COURSE_SERVICE_SUBSYSTEM]";

    /** Enterprise standard tracking tag identifier mapping to architectural requirements. */
    private static final String TRACEABILITY_TAGS = "[REQ-007], [REQ-008]";

    /** Default enterprise error classification code for schedule conflict exceptions. */
    public static final String ERROR_CODE = "SCHEDULE_CONFLICT_ERROR";

    /** Standardized enterprise logging handler instance mapped to this subsystem. */
    private static final Logger LOGGER = Logger.getLogger(ScheduleConflictException.class);

    // =========================================================================
    // 📊 INSTANCE FIELD ATTRIBUTES
    // =========================================================================

    /** Machine-readable enterprise error code for downstream API contract parsing. */
    private final String errorCode;

    // =========================================================================
    // 🛠️ CONSTRUCTORS WITH AUDIT LOGGING & CAUSE PRESERVATION
    // =========================================================================

    /**
     * Constructs a new ScheduleConflictException with a designated detail message.
     * Automatically triggers audit-level enterprise logging complying with [0.3].
     *
     * @param message Detailed descriptive explanation of the scheduling conflict.
     */
    public ScheduleConflictException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
        
        // [0.3] Enterprise Logging & Exception Auditing Law: Log exact subsystem, message, and tag ID
        LOGGER.error(String.format("%s [ERROR_CODE: %s] %s Schedule conflict detected: %s",
                SUBSYSTEM_NAME, this.errorCode, TRACEABILITY_TAGS, message));
    }

    /**
     * Constructs a new ScheduleConflictException with a detailed message and preserves
     * the ancestral root-cause exception object to satisfy the Exception Cause Chain Preservation Law.
     *
     * @param message Descriptive context text explaining the failure boundary.
     * @param cause   The original physical caught exception object (root cause).
     */
    public ScheduleConflictException(String message, Throwable cause) {
        // [0.3] Forwarding original physical caught exception object inside wrapper constructor
        super(message, cause);
        this.errorCode = ERROR_CODE;
        
        // [0.3] Enterprise Logging & Exception Auditing Law: Centralized cloud aggregation trace logging
        LOGGER.error(String.format("%s [ERROR_CODE: %s] %s Schedule conflict execution failed with raw error: %s",
                SUBSYSTEM_NAME, this.errorCode, TRACEABILITY_TAGS, 
                cause != null ? cause.getMessage() : "Unknown Root Cause"), cause);
    }

    /**
     * Constructs a new ScheduleConflictException with a specific enterprise error code,
     * detail message, and root cause exception preservation.
     *
     * @param errorCode Custom enterprise error classification code.
     * @param message   Descriptive context text explaining the failure boundary.
     * @param cause     The original physical caught exception object.
     */
    public ScheduleConflictException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode != null ? errorCode : ERROR_CODE;
        
        // [0.3] Enterprise Logging & Exception Auditing Law: Comprehensive exception logging
        LOGGER.error(String.format("%s [ERROR_CODE: %s] %s Custom schedule conflict exception triggered. Raw error: %s",
                SUBSYSTEM_NAME, this.errorCode, TRACEABILITY_TAGS,
                cause != null ? cause.getMessage() : message), cause);
    }

    // =========================================================================
    // 📦 GETTERS & UTILITY MAPPING METHODS
    // =========================================================================

    /**
     * Retrieves the specific machine-readable enterprise error code associated with this exception.
     *
     * @return The immutable error code string.
     */
    public String getErrorCode() {
        return this.errorCode;
    }
}