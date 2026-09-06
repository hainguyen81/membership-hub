/**
 * Attendance Service Application - Core runtime entry point for attendance management microservice.
 *
 * Tags: [ARC-000] [REQ-012] [NFR-001] [NFR-003] [NFR-004]
 *
 * Review: Validates Quarkus main configuration, ensures package org.nlh4j.membershiphub.attendanceservice,
 * disables banner for production, configures HTTP port, host, and health check endpoints.
 */
package org.nlh4j.membershiphub.attendanceservice;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

@QuarkusMain
public class AttendanceServiceApplication {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    public static void main(String[] args) {
        // Production hardening: disable banner, set HTTP port and host, enable health checks
        System.setProperty("quarkus.banner.enabled", "false");
        System.setProperty("quarkus.http.port", "8080");
        System.setProperty("quarkus.http.host", "0.0.0.0");
        // Enable SmallRye Health endpoints (automatically provided by Quarkus)
        System.setProperty("quarkus.smallrye-health.root-path", "/q/health");
        System.setProperty("quarkus.smallrye-health.liveness-path", "/q/health/live");
        System.setProperty("quarkus.smallrye-health.readiness-path", "/q/health/ready");

        logger.info("Starting Attendance Service Application (membership-hub) on port 8080 with health checks enabled.");

        Quarkus.run(args);
    }
}