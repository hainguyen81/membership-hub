package org.nlh4j.membershiphub.attendanceservice;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.jboss.logging.Logger;

/**
 * Main application entry point for the Attendance Service in Membership Hub.
 *
 * Traceability Tags:
 * [ARC-000] - Microservice scaffolding and multi-module Quarkus application setup.
 * [REQ-012] - Real-time QR attendance check-in workflow and processing initialization.
 * [ARC-007] - QR-based event streaming, idempotency enforcement, and attendance tracking.
 * [EXC-001] - Fault tolerance and retry handling for disconnected network environments.
 * [EXC-002] - Duplicate attendance validation in same calendar day idempotency window.
 * [EXC-005] - Orderly FIFO replay of buffered attendance events after network restoration.
 * [NFR-001] - Sub-200ms latency target and resource-optimized runtime lifecycle.
 * [NFR-003] - Zero-trust security enforcement, auditability, and token validation baseline.
 * [NFR-004] - Cloud-native elasticity, failover mechanisms, and Kubernetes readiness.
 * [NFR-005] - Lightweight container footprint optimization for GraalVM/JVM native image.
 */
@QuarkusMain
public class AttendanceServiceApplication {

    private static final Logger LOGGER = Logger.getLogger(AttendanceServiceApplication.class);

    /**
     * Standard main method delegated to Quarkus runtime lifecycle manager.
     *
     * @param args runtime command-line arguments.
     */
    public static void main(String... args) {
        LOGGER.info("Bootstrapping AttendanceServiceApplication [ARC-000, REQ-012]...");
        Quarkus.run(AttendanceAppRunner.class, args);
    }

    /**
     * Internal QuarkusApplication runner to gracefully manage initialization,
     * audit logging, and shutdown signals.
     */
    public static class AttendanceAppRunner implements QuarkusApplication {

        private static final Logger RUNNER_LOGGER = Logger.getLogger(AttendanceAppRunner.class);

        @Override
        public int run(String... args) {
            RUNNER_LOGGER.info("=================================================================");
            RUNNER_LOGGER.info("Membership Hub :: Attendance Microservice successfully launched.");
            RUNNER_LOGGER.info("Active Traceability Anchors: [ARC-000, REQ-012, ARC-007, EXC-001]");
            RUNNER_LOGGER.info("Listening for real-time QR scans, Kafka streams & HTTP traffic.");
            RUNNER_LOGGER.info("=================================================================");

            Quarkus.waitForExit();

            RUNNER_LOGGER.info("AttendanceServiceApplication shutting down gracefully.");
            return 0;
        }
    }
}