# 📊 Scaffolding Architecture Documentation

## 📝 Overview
This document outlines the scaffolding architecture of the Membership Hub project, including the multi-module Maven structure, package naming conventions, and technology stack.

## 📁 Multi-Module Maven Structure
The project follows a multi-module Maven structure, with the root directory `./sources/backend` containing the parent `pom.xml` file. The four microservices are:

* `user-service`
* `center-service`
* `course-service`
* `attendance-service`

Each microservice has its own `pom.xml` file located in its respective directory.

## 📦 Package Naming Conventions
The Java package prefix base is `org.nlh4j.membershiphub`. Each microservice has its own sub-package:

* `org.nlh4j.membershiphub.userservice`
* `org.nlh4j.membershiphub.centerservice`
* `org.nlh4j.membershiphub.courseservice`
* `org.nlh4j.membershiphub.attendanceservice`

## 📊 Technology Stack
The project utilizes the following technology stack:

* **Backend:** Quarkus 3.15.1, Java 17 LTS, SmallRye Reactive Messaging (Kafka), Hibernate ORM with Panache, PostgreSQL JDBC 42.7.3, Flyway Migration 10.10.0, SmallRye JWT, RESTEasy Reactive Jackson.
* **Frontend & Mobile:** Next.js 14.2.15 (App Router), React Native 0.75.4 (Expo SDK 51), TypeScript 5.5, NativeWind, Zustand, Axios.
* **Messaging & Storage:** Apache Kafka, PostgreSQL 16 (Primary + Read Replica), Redis Cache.
* **Containerization & Cloud:** Docker (Multi-stage builds), Google Cloud Platform (GKE Autopilot, Cloud SQL, Secret Manager, Cloud KMS).

---

# 🌐 Cross-Platform Integrated Business Flows: Attendance & QR Ingestion

## 1. 📑 Enterprise Traceability Matrix Reference
This section maps the architectural components, asynchronous event pipelines, operational workflows, and fault-tolerance safeguards directly to their ancestral business, functional, architectural, and non-functional requirements.

| Targeted Tag ID | Requirement Classification | Functional & Technical Scope Summary | Target System Component / Implementation Path |
| :--- | :--- | :--- | :--- |
| `[REQ-012]` | Functional Requirement | Student QR attendance scan recording, decoding, and validation | `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/controller/AttendanceController.java` |
| `[REQ-013]` | Functional Requirement | Idempotent scan processing via composite key `(student_id, course_id, attendance_date)` | `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/service/AttendanceService.java` |
| `[ARC-007]` | Architecture Requirement | Real-time QR attendance processing pipeline, event propagation, and caching | `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/messaging/KafkaAttendanceProducer.java` |
| `[EXC-001]` | Exception Handling | Network drop during scan ingestion, client-side offline retry queue (3 attempts) | `./sources/frontend/web-app/src/lib/offline/cacheService.ts`, Mobile QR Scanner Component |
| `[EXC-002]` | Exception Handling | Duplicate scan suppression: return HTTP 200 with `duplicate: true` flag without duplicating records | `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/service/AttendanceService.java` |
| `[EXC-005]` | Exception Handling | Post-outage recovery: FIFO order ingestion queue re-synchronization | `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/messaging/AttendanceRetryConsumer.java` |
| `[DAT-004]` | Data Schema | Enrollment entity relational constraints and student course verification | `./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_enrollments_attendance.sql` |
| `[DAT-005]` | Data Schema | Attendance persistent record schema with composite uniqueness and indexing | `./sources/backend/attendance-service/src/main/resources/db/migration/V1__init_enrollments_attendance.sql` |
| `[NFR-001]` | Non-Functional Requirement | Ingestion performance: P95 latency < 200ms under 10,000 concurrent scans | `./sources/backend/attendance-service/src/main/resources/application.properties` |
| `[NFR-003]` | Security Requirement | JWT authentication, cryptographic signature checks, prepared statement SQLi protection | `./sources/backend/attendance-service/src/main/java/org/nlh4j/membershiphub/attendanceservice/security/JwtSecurityFilter.java` |
| `[DOC-001]` | Enterprise Documentation | Architectural specifications, C4 Container model, and operational runbooks | `./sources/docs/architecture/CROSS_PLATFORM_INTEGRATED_BUSINESS_FLOWS.md` |

---

## 2. 🏛️ C4 Container Architecture: Attendance Microservice Ecosystem `[ARC-007]`

The `attendance-service` operates as a high-throughput, horizontally scalable Quarkus 3.15 runtime node dedicated to decoding, validating, persisting, and publishing student attendance events. Below is the architectural flowchart governing the end-to-end QR scan ingestion pipeline across mobile clients, backend REST endpoints, and the Apache Kafka event broker network.