package org.nlh4j.membershiphub.attendanceservice;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application bootstrap entry point for the Attendance Microservice.
 *
 * <p>Traceability Matrix Metadata:
 * <ul>
 *   <li>[ARC-000]: Multi-module backend root scaffolding and build descriptor compliance</li>
 *   <li>[REQ-012]: Real-time student QR attendance scanning and verification</li>
 * </ul>
 *
 * <p>Compliance Guardrails:
 * <ul>
 *   <li>Package Enforcement: {@code org.nlh4j.membershiphub.attendanceservice}</li>
 *   <li>Zero {@code com.example} references</li>
 *   <li>Standard Quarkus runtime lifecycle management</li>
 * </ul>
 */
@QuarkusMain
public class AttendanceServiceApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttendanceServiceApplication.class);

    /**
     * Standard JVM entry point delegating lifecycle execution to the Quarkus runtime.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String... args) {
        LOGGER.info("Starting AttendanceServiceApplication (attendance-service) via Quarkus runtime...");
        Quarkus.run(AttendanceApp.class, args);
    }

    /**
     * QuarkusApplication lifecycle implementation handling bootstrap verification
     * and graceful shutdown procedures.
     */
    public static class AttendanceApp implements QuarkusApplication {

        private static final Logger APP_LOGGER = LoggerFactory.getLogger(AttendanceApp.class);

        @Override
        public int run(String... args) throws Exception {
            APP_LOGGER.info("AttendanceServiceApplication initialized successfully.");
            APP_LOGGER.info("Attendance Service active profile, health check endpoints, and Kafka ingress ready.");
            Quarkus.waitForExit();
            APP_LOGGER.info("AttendanceServiceApplication shutting down gracefully.");
            return 0;
        }
    }
}