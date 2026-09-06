package org.nlh4j.membershiphub.attendanceservice;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * Attendance Service Main Application Entry Point.
 * 
 * <p>Traceability Tags:
 * - [ARC-000]: Scaffolding & Build Descriptors
 * - [REQ-012]: Real-time QR Code Attendance Recording
 * </p>
 */
@QuarkusMain
public class AttendanceServiceApplication {

    public static void main(String... args) {
        Quarkus.run(args);
    }
}