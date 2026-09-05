# 🏢 ENTERPRISE SYSTEM ARCHITECTURE BLUEPRINT: MEMBERSHIP HUB
* Target Project Identity Safe Name: `membership-hub`
* Enforced Java Package Prefix Base: `org.nlh4j.membershiphub`
* Target Documentation Destination Path: `./sources/docs/architecture/ENTERPRISE_SYSTEM_ARCHITECTURE_BLUEPRINT.md`
* Associated Traceability Tags: `[ARC-000]`, `[ARC-006]`, `[ARC-007]`, `[ARC-008]`, `[ARC-009]`, `[REQ-012]`, `[REQ-013]`, `[REQ-024]`, `[REQ-025]`, `[EXC-001]`, `[EXC-002]`, `[EXC-005]`, `[DAT-004]`, `[DAT-005]`, `[DAT-006]`, `[NFR-001]`, `[NFR-003]`, `[NFR-004]`, `[DOC-001]`

---

## 1. 🏗️ SYSTEM ARCHITECTURE OVERVIEW & SCAFFOLDING BLUEPRINT

### 1.1. Architectural Intent & Scope
The **Membership Hub** enterprise platform is a distributed, multi-tenant microservices ecosystem engineered for real-time member management and QR-based attendance tracking [ARC-007]. To enforce strict modularity, separation of concerns, and independent scalability, the backend architecture is divided into isolated microservices (`user-service`, `center-service`, `course-service`, `attendance-service`, `report-service`, `dashboard-service`) managed under a unified Maven Multi-Module parent descriptor. The frontend tier is powered by Next.js 14 utilizing the App Router paradigm, providing responsive server-side rendered interfaces and mobile-ready layout wrappers [ARC-009].

### 1.2. Maven Multi-Module Directory Tree
The entire backend codebase is structured under `./sources/backend/` conforming strictly to the enterprise package naming convention `org.nlh4j.membershiphub.<service-name>` [ARC-000].

---

## 2. ⚡ ATTENDANCE SERVICE & QR DECODING ARCHITECTURE BLUEPRINT

### 2.1. Component Architecture & Microservice Decomposition
The `attendance-service` is an independent Quarkus microservice responsible for ingesting, validating, and persisting real-time student attendance scans originating from mobile client devices. The architecture encapsulates the following layers within the `org.nlh4j.membershiphub.attendanceservice` package space:
- **`AttendanceController`**: Exposes REST endpoints (`POST /api/v1/attendance/scan`) handling encrypted or base64-encoded QR payloads with integrated idempotency key headers [REQ-012], [REQ-013], [ARC-007].
- **`AttendanceService`**: Core domain logic orchestrating payload decoding, enrollment verification against `course-service`, idempotency checking, and persistence transaction boundaries.
- **`QrPayloadDecoder`**: Utility component responsible for parsing base64 structures, verifying cryptographic checksums, and extracting `studentId` and `courseId`.
- **`AttendanceRepository`**: Panache-based data access layer interacting with PostgreSQL `attendance` partition tables.
- **`KafkaAttendanceProducer`**: SmallRye Reactive Messaging publisher pushing `attendance-recorded` events to the Kafka broker for downstream notification fan-out [ARC-008].

### 2.2. QR Scan Processing Flow Diagram
The following Mermaid.js sequence and flowchart models illustrate the end-to-end execution of a QR attendance scan request, incorporating failure recovery and idempotency guarantees [EXC-001], [EXC-002], [EXC-005]: