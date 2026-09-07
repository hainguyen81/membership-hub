package org.nlh4j.membershiphub.attendanceservice;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * Attendance Service Application entry point.
 * 
 * Traceability Tags:
 * - [ARC-000]: Scaffolding & Build Descriptors
 * - [REQ-012]: Real-time QR attendance check-in recording
 * 
 * Compliance & Infrastructure Guardrails:
 * - Package prefix: org.nlh4j.membershiphub.attendanceservice
 * - Production-ready configuration validation (banner disabled, health checks enabled)
 */
@QuarkusMain
public class AttendanceServiceApplication {

    /**
     * Main entry point for the Attendance Service microservice.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Quarkus.run(args);
    }
}