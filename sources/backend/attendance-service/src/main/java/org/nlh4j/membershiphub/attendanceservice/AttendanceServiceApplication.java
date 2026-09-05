package org.nlh4j.membershiphub.attendanceservice;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Enterprise Application Entrypoint for Attendance Service.
 * 
 * Traceability Tags:
 * - Architecture: [ARC-000]
 * - Requirement: [REQ-012]
 * - Non-Functional: [NFR-001], [NFR-003], [NFR-004], [NFR-005]
 */
@QuarkusMain
public class AttendanceServiceApplication {

    private static final Logger LOG = Logger.getLogger(AttendanceServiceApplication.class);

    @ConfigProperty(name = "quarkus.application.name", defaultValue = "attendance-service")
    String appName;

    public static void main(String... args) {
        LOG.info("Initializing Attendance Service Enterprise Runtime...");
        Quarkus.run(args);
    }
}