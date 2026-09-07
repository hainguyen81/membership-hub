# BACKEND CORE PROCESSING ENGINE LOGIC — Attendance Service
**Document ID:** DOC-ATT-001  
**Project:** membership-hub  
**Version:** 1.2  
**Last Updated:** 2026/08/29  
**Author:** Enterprise System Architect (SA Agent)  
**Status:** Production Ready  

---

## 1. Executive Summary

The `attendance-service` is a critical microservice within the Membership Hub ecosystem, responsible for processing real-time QR-based attendance scans from mobile applications. It enforces strict idempotency guarantees, implements resilient retry mechanisms for network failures, and integrates with Apache Kafka for asynchronous event propagation. This document provides a comprehensive architectural overview, processing flow diagrams, API contracts, database schema mappings, and traceability references aligned with enterprise governance standards.

---

## 2. System Context & Scope

### 2.1 Service Overview
- **Service Name:** `attendance-service`
- **Java Package Base:** `org.nlh4j.membershiphub.attendanceservice`
- **Runtime Stack:** Quarkus 3.15.1 LTS, Hibernate ORM with Panache, SmallRye Reactive Messaging Kafka, RESTEasy Reactive
- **Database:** PostgreSQL 16 (primary), Redis (cache/session)
- **Messaging Layer:** Apache Kafka (topics: `attendance.scan.requested`, `attendance.events`)
- **Security Model:** JWT Bearer Token Authentication via OAuth2 Resource Server

### 2.2 Core Responsibilities
| Responsibility | Description | Traceability Tag ID |
|----------------|-------------|---------------------|
| QR Payload Decoding | Decode base64-encoded QR payloads containing studentID and courseID | `[REQ-012]`, `[ARC-007]` |
| Enrollment Validation | Verify student is enrolled in the specified course | `[REQ-012]`, `[ARC-007]` |
| Idempotency Enforcement | Prevent duplicate attendance records using composite unique keys | `[REQ-013]`, `[EXC-002]` |
| Attendance Persistence | Store attendance records with timestamp and metadata | `[REQ-012]`, `[DAT-005]` |
| Kafka Event Publishing | Emit `attendance-recorded` events for downstream consumers | `[ARC-008]` |
| Fault Tolerance | Handle network drops with local queue and FIFO recovery | `[EXC-001]`, `[EXC-005]` |
| Documentation Compliance | Maintain structural traceability and architectural metadata | `[DOC-001]` |

---

## 3. C4 Container Diagram & Architecture

The following section outlines the container-level architecture of the `attendance-service` along with its structural components: `AttendanceController`, `AttendanceService`, `AttendanceRepository`, `KafkaAttendanceProducer`, and `QrPayloadDecoder`.

### 3.1 Component Breakdown
- **`AttendanceController`**: Exposes REST endpoints (`POST /api/v1/attendance/scan`) and manages HTTP request validation, authentication context propagation, and error response mapping.
- **`AttendanceService`**: Orchestrates core business logic, coordinating payload decoding, enrollment checks, idempotency verification, and database persistence.
- **`QrPayloadDecoder`**: Utility component responsible for safely decoding Base64 strings and validating internal JSON schema structures (`studentId`, `courseId`, `timestamp`).
- **`AttendanceRepository`**: Panache-based database abstraction layer interacting with PostgreSQL table `attendance` utilizing composite unique indexes.
- **`KafkaAttendanceProducer`**: SmallRye Reactive Messaging component publishing `attendance-recorded` events to the Kafka broker for downstream notification and reporting consumers.

### 3.2 QR Scan Processing Flowchart
The following Mermaid diagram details the end-to-end processing pipeline for real-time QR attendance scanning: