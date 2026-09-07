/*
 * Copyright (c) 2026 org.nlh4j.membershiphub
 * All rights reserved.
 *
 * Traceability Metadata Tags:
 * [ARC-000] - Scaffolding & Build Descriptors / Quarkus Main Application Architecture
 * [REQ-012] - Attendance QR Code Scan & Processing Ingestion Pipeline
 * [NFR-004] - Enterprise Cloud-Native Resiliency & Health Check Monitoring Standards
 * [NFR-005] - GraalVM Native Image & Lightweight Container Optimization (<500MB)
 */
package org.nlh4j.membershiphub.attendanceservice;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.jboss.logging.Logger;

/**
 * AttendanceServiceApplication serves as the main entry point for the attendance-service microservice.
 * Built on Quarkus 3.15 LTS runtime, optimized for high-throughput QR code scan ingestion and attendance tracking.
 * 
 * Enforces enterprise standards:
 * - Proper package declaration under org.nlh4j.membershiphub.attendanceservice
 * - Disabling startup banner in production via configuration or programmatic flags
 * - Integration with SmallRye Health endpoints (/q/health/live, /q/health/ready) for Kubernetes probes
 * 
 * @traceability [ARC-000], [REQ-012]
 */
@QuarkusMain
public class AttendanceServiceApplication {

    private static final Logger LOG = Logger.getLogger(AttendanceServiceApplication.class);

    public static void main(String... args) {
        LOG.info("Initializing Membership Hub - Attendance Service Microservice...");
        
        // Ensure production runtime configurations are honored
        // Tag [NFR-005]: Disable startup banner for lean production container images
        System.setProperty("quarkus.banner.enabled", "false");
        
        // Tag [NFR-004]: Initialize Quarkus with health check endpoints for Kubernetes probe readiness
        Quarkus.run(args);
    }
}