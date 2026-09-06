package org.nlh4j.membershiphub.attendanceservice;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.jboss.logging.Logger;

/**
 * Enterprise Application Main Entrypoint for attendance-service.
 * <p>
 * Architectural Traceability & Compliance Codes:
 * <ul>
 *   <li>[ARC-000] Backend Architecture Scaffolding & Multi-Module Standards</li>
 *   <li>[REQ-012] QR Code Attendance Record Ingestion & Event Dispatching</li>
 * </ul>
 * </p>
 */
@QuarkusMain
public class AttendanceServiceApplication implements QuarkusApplication {

    private static final Logger LOG = Logger.getLogger(AttendanceServiceApplication.class);

    /**
     * Standard Quarkus Main Bootstrap Execution.
     *
     * @param args Command line arguments passed during startup.
     */
    public static void main(String... args) {
        Quarkus.run(AttendanceServiceApplication.class, args);
    }

    /**
     * Application runtime loop and lifecycle governance.
     *
     * @param args Execution arguments.
     * @return Exit code indicator.
     * @throws Exception In case of critical startup anomaly.
     */
    @Override
    public int run(String... args) throws Exception {
        LOG.info("Initializing Attendance Service [org.nlh4j.membershiphub.attendanceservice]...");
        LOG.info("Active Traceability Bounds: [ARC-000], [REQ-012]");
        LOG.info("Attendance microservice is running. Press Ctrl+C to terminate.");
        
        Quarkus.waitForExit();
        return 0;
    }
}