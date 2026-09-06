# BACKEND CORE PROCESSING ENGINE LOGIC — Attendance Service
**Document ID:** DOC-ATT-001  
**Project:** membership-hub  
**Version:** 1.0  
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
| Responsibility | Description |
|----------------|-------------|
| QR Payload Decoding | Decode base64-encoded QR payloads containing studentID and courseID |
| Enrollment Validation | Verify student is enrolled in the specified course |
| Idempotency Enforcement | Prevent duplicate attendance records using composite unique keys |
| Attendance Persistence | Store attendance records with timestamp and metadata |
| Kafka Event Publishing | Emit `attendance-recorded` events for downstream consumers |
| Fault Tolerance | Handle network drops with local queue and FIFO recovery |

---

## 3. C4 Container Diagram