/**
 * Attendance Service Application entry point for Membership Hub.
 *
 * Tags: [ARC-000], [REQ-012]
 */
package org.nlh4j.membershiphub.attendanceservice;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.quarkus.runtime.QuarkusApplication;

@QuarkusMain
public class AttendanceServiceApplication {

    public static void main(String[] args) {
        // Disable Quarkus banner in production
        System.setProperty("quarkus.banner.enabled", "false");
        // Ensure default HTTP port for containerized deployments
        if (System.getProperty("quarkus.http.port") == null) {
            System.setProperty("quarkus.http.port", "8080");
        }
        // Run Quarkus application with health check support
        Quarkus.run(args, (QuarkusApplication) () -> {
            System.out.println("Attendance Service Application started successfully.");
        });
    }
}