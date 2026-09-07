package org.nlh4j.membershiphub.attendanceservice;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enterprise Main Application Entry Point for Attendance Service.
 *
 * <p>Traceability Matrix Tags:
 * <ul>
 *   <li>[ARC-000] - Microservices scaffolding and multi-module base architecture layout.</li>
 *   <li>[REQ-012] - QR payload ingestion, check-in validation, and attendance logging processing.</li>
 * </ul>
 *
 * <p>Compliance and Architectural Guardrails:
 * <ul>
 *   <li>Corporate package layout compliance: {@code org.nlh4j.membershiphub.attendanceservice}</li>
 *   <li>Workspace boundary: {@code ./sources/backend/attendance-service/}</li>
 *   <li>Strict zero reference to generic placeholders like {@code com.example}</li>
 *   <li>Handles clean shutdown signals and startup lifecycle validation for attendance pipelines</li>
 * </ul>
 */
@QuarkusMain
public class AttendanceServiceApplication implements QuarkusApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttendanceServiceApplication.class);

    /**
     * Standard Quarkus entry point invoking the execution lifecycle.
     *
     * @param args runtime CLI arguments passed during container bootstrap
     */
    public static void main(String... args) {
        LOGGER.info("Bootstrapping AttendanceServiceApplication [ARC-000, REQ-012]...");
        Quarkus.run(AttendanceServiceApplication.class, args);
    }

    /**
     * Executes the main application loop, handling startup logging, resource readiness,
     * and blocking on shutdown signals.
     *
     * @param args runtime application arguments
     * @return exit code integer (0 for successful graceful termination)
     */
    @Override
    public int run(String... args) {
        LOGGER.info("AttendanceServiceApplication has successfully initialized.");
        LOGGER.info("Active profile services: QR scanning engine, idempotency guards, and Kafka attendance event producers.");

        Quarkus.waitForExit();

        LOGGER.info("AttendanceServiceApplication is shutting down gracefully.");
        return 0;
    }
}