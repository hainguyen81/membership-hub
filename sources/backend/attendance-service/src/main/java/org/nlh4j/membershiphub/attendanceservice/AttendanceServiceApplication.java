/**
 * Attendance Service Application entry point for Membership Hub.
 *
 * Tags: [ARC-000], [REQ-012]
 */
package org.nlh4j.membershiphub.attendanceservice;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class AttendanceServiceApplication {

    public static void main(String[] args) {
        // Disable banner in production
        System.setProperty("quarkus.banner.enabled", "false");
        // Set default HTTP port (can be overridden by environment)
        System.setProperty("quarkus.http.port", "8080");
        // Enable health check endpoints
        System.setProperty("quarkus.health.enabled", "true");
        System.setProperty("quarkus.health.liveness.enabled", "true");
        System.setProperty("quarkus.health.readiness.enabled", "true");

        Quarkus.run(args);
    }
}