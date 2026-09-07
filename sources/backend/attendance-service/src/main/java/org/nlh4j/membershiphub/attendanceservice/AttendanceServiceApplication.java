/**
 * [ARC-000] [REQ-012] Attendance Service Application Main Entry Point
 * Enterprise-grade Quarkus main class for attendance-service.
 * Configures production settings: banner disabled, HTTP port, health checks.
 */
package org.nlh4j.membershiphub.attendanceservice;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class AttendanceServiceApplication {

    public static void main(String[] args) {
        // Production hardening: disable banner, set HTTP port
        System.setProperty("quarkus.banner.enabled", "false");
        System.setProperty("quarkus.http.port", "8080");
        // Enable health endpoints (provided by SmallRye Health extension)
        System.setProperty("quarkus.smallrye.health.ui.path", "/q/health-ui");
        Quarkus.run(AttendanceServiceApplication.class, args);
    }
}