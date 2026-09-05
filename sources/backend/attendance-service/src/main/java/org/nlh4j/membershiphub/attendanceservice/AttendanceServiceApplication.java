/*
 * Copyright (c) 2026 org.nlh4j.membershiphub
 * All rights reserved.
 * 
 * Traceability Tags:
 * - [ARC-000]: Scaffolding & Build Descriptors cho toàn bộ hệ thống
 * - [REQ-012]: Ghi nhận điểm danh QR cho sinh viên
 */
package org.nlh4j.membershiphub.attendanceservice;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.jboss.logging.Logger;

/**
 * AttendanceServiceApplication serves as the main entry point for the Attendance Microservice
 * within the membership-hub enterprise platform. It bootstraps the Quarkus runtime,
 * configures production health checks, and initializes reactive messaging endpoints.
 * 
 * @author Enterprise Architecture System (SA Agent)
 * @version 1.0.0
 * @since 2026-08-29
 */
@QuarkusMain
public class AttendanceServiceApplication {

    private static final Logger LOG = Logger.getLogger(AttendanceServiceApplication.class);

    /**
     * Main entry point for the attendance-service microservice.
     * Delegates startup execution to the Quarkus runtime engine.
     *
     * @param args Command line arguments passed during container bootstrap.
     */
    public static void main(String[] args) {
        LOG.info("Initializing Attendance Service Microservice [membership-hub - attendance-service]...");
        
        // Execute Quarkus runtime container bootstrap
        Quarkus.run(args);
    }
}