/**
 * Attendance Service Application entry point for Membership Hub.
 *
 * Tags: [ARC-000], [REQ-012]
 */
package org.nlh4j.membershiphub.attendanceservice;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * Entry point for the Attendance Service application.
 * <p>
 * This class initializes the Quarkus runtime with essential configuration settings:
 * - Disables the startup banner for production environments.
 * - Sets the default HTTP port (can be overridden by environment variables).
 * - Enables health check endpoints for liveness and readiness probes.
 *
 * @author Membership Hub Team
 * @version 1.1 .1
 * @since 2024-08-29
 */
@QuarkusMain
public class AttendanceServiceApplication {

    public static void main(String[] args) {
        // Disable banner in production to reduce log noise and improve startup performance
        System.setProperty("quarkus.banner.enabled", "false");
        // Set default HTTP port; can be overridden by environment (e.g., Docker/Kubernetes)
        System.setProperty("quarkus.http.port", "8080");
        // Enable Quarkus health checks for monitoring and external tooling
        System.setProperty("quarkus.health.enabled", "true");
        System.setProperty("quarkus.health.liveness.enabled", "true");
        System.setProperty("quarkus.health.readiness.enabled", "true");

        Quarkus.run(args);
    }
}