/**
 * Attendance Service Application Entry Point.
 * Traceability Tags: [ARC-000], [REQ-012]
 *
 * This class serves as the primary bootstrap for the attendance-service microservice
 * within the Membership Hub platform. It initializes the Quarkus runtime,
 * provides structured logging for operational visibility, and defines immutable
 * configuration constants at the class level to satisfy enterprise anti‑magic‑number
 * and clean‑code mandates.
 *
 * The application is responsible for exposing the attendance‑scan REST endpoints,
 * coordinating with Kafka for asynchronous event publishing, and ensuring that
 * all startup and shutdown phases are audited via SLF4J logging.
 *
 * @author Enterprise Code Generator
 * @version 1.0.0 (Membership Hub Baseline)
 * @since 2026‑08‑29
 */
package org.nlh4j.membershiphub.attendanceservice;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.jboss.logging.Logger;

/**
 * Top‑of‑class immutable constants – all configuration values are hoisted here
 * to comply with the Anti‑Magic‑Numbers policy and to guarantee a single source of
 * truth for the attendance service runtime.
 *
 * Traceability Tags: [ARC-000], [REQ-012]
 */
final class AttendanceServiceConstants {

    /** Logical name of the application – used for logging correlation. */
    public static final String APPLICATION_NAME = "membership-hub-attendance-service";

    /** Version identifier for the service – useful for health‑checks and metrics. */
    public static final String APPLICATION_VERSION = "1.0.0";

    /** Default timeout (in milliseconds) for Kafka producer operations. */
    public static final int KAFKA_PRODUCER_TIMEOUT_MS = 5_000;

    /** Maximum retry attempts for idempotent attendance scan processing. */
    public static final int MAX_ATTENDANCE_RETRY_ATTEMPTS = 3;

    /** Private constructor – constants‑only class. */
    private AttendanceServiceConstants() {
        // Prevent instantiation
    }
}

/**
 * Main application class that bootstraps the Quarkus runtime for the attendance
 * service. This class follows the enterprise logging and traceability mandates:
 *   - Entry/exit logging at INFO level.
 *   - Structured log messages that include the application name and version.
 *   - Traceability Tag IDs embedded in Javadoc for audit purposes.
 *
 * Traceability Tags: [ARC-000], [REQ-012]
 */
@QuarkusMain
public class AttendanceServiceApplication {

    /** SLF4J logger – used for all operational and audit logging. */
    private static final Logger logger = Logger.getLogger(AttendanceServiceApplication.class);

    /**
     * Entry point for the Quarkus application.
     *
     * @param args command‑line arguments (currently unused – reserved for future
     *             extensions such as external configuration injection).
     */
    public static void main(final String[] args) {
        logger.info("[ENTRY] Starting {} version {} – attendance service bootstrap.",
                AttendanceServiceConstants.APPLICATION_NAME,
                AttendanceServiceConstants.APPLICATION_VERSION);

        try {
            // Launch the Quarkus runtime. Quarkus.main() blocks until the application
            // is terminated (e.g., via SIGTERM). Any uncaught exception during startup
            // will be logged here and propagate as a non‑zero exit code.
            Quarkus.main(args);
        } catch (final Exception e) {
            // Comprehensive error‑level logging per enterprise mandate – include the
            // target module subsystem, raw exception message, and traceability Tag ID.
            logger.error("[CRITICAL FAIL] [ARC-012] Attendance service bootstrap failed due to unexpected error. Raw error: {}", e.getMessage(), e);
            // Re‑throw to ensure the JVM exits with a non‑zero status code.
            throw e;
        } finally {
            logger.info("[EXIT] Attendance service application has terminated.");
        }
    }
}