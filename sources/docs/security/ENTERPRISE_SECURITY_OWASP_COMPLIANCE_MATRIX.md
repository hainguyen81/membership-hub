# 🏛️ MEMBERSHIP HUB ENTERPRISE SECURITY & OWASP COMPLIANCE MATRIX
*(Conceptual Architecture Documentation for Membership Hub)*

## 📊 DOCUMENT TRACEABILITY METADATA

| Document ID | Version | Date | Author | Tags |
|-------------|---------|------|--------|------|
| ARCH-DOC-001 | 1.2 | 2026/08/29 | Kiến Trúc Sư Hệ Thống | [ARC-000], [DOC-001], [REQ-012], [REQ-013], [ARC-007], [EXC-001], [EXC-002], [EXC-005], [NFR-003] |

## 📁 1. SYSTEM OVERVIEW & ARCHITECTURE

### ⚙️ 1.1. Technology Stack & Ecosystem
- **Backend:** Quarkus 3.15.1, Java 17 LTS, RESTEasy Reactive, Hibernate ORM Panache, SmallRye Kafka.
- **Frontend:** Next.js 14.2.15, TypeScript 5.5, React 18.3.1.
- **Database:** PostgreSQL 16 with B-Tree and Gist Indexes.

### 🌊 1.2. Modular Architecture
- Multi-module Maven project under package root: `org.nlh4j.membershiphub`
  * `membership-hub-backend` (root)
  * `user-service`
  * `center-service`
  * `course-service`
  * `attendance-service`

## 📁 2. SCAFFOLDING ARCH & ATTENDANCE MICROSERVICE ARCHITECTURE

### ⚙️ 2.1. Attendance Service C4 Container Blueprint
The `attendance-service` microservice is engineered for high-concurrency real-time QR attendance check-ins, guaranteeing data idempotency, offline resilience, and out-of-order queue recovery [ARC-007], [REQ-012], [REQ-013].

- **REST Controller Layer (`AttendanceController.java`):** Exposes high-throughput endpoints such as `POST /api/v1/attendance/scan`. It validates inbound JSON payloads against rigorous Bean Validation rules and verifies JWT bearer tokens via RESTEasy Reactive filters [NFR-003].
- **Payload Decoder Component (`QrPayloadDecoder.java`):** Decodes Base64-encoded QR payloads originating from mobile scanners, extracting structural metadata including `studentId`, `courseId`, and cryptographic timestamp stamps [REQ-012].
- **Service Layer (`AttendanceService.java`):** Encapsulates core business validation logic, cross-checking course enrollments, validating active student card status, and managing idempotency checks.
- **Repository Layer (`AttendanceRepository.java`):** Interfaces with PostgreSQL via Hibernate Panache, enforcing composite unique constraints (`student_id, course_id, attendance_date`) to prevent duplicate records [DAT-004].
- **Kafka Producer Component (`KafkaAttendanceProducer.java`):** Publishes downstream domain events (`attendance-recorded`) to Kafka topic partitions for real-time analytics and notification fan-outs [ARC-008].

### 🔄 2.2. QR Code Attendance Scan Processing Flow Diagram
The following Mermaid flowchart delineates the end-to-end execution sequence and fault-tolerance gates during an attendance scan transaction, incorporating retry logic for network instability [EXC-001], [EXC-002], [EXC-005]: