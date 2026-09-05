# 🏛️ Central Endpoint API Contract & Scaffolding Architecture Specifications

## 📊 Document Control & Metadata
| Category | Technical Specification |
| :--- | :--- |
| **Document ID** | ARCH-20260829223421-SPEC |
| **Project Identity** | membership-hub |
| **Package Prefix Base** | `org.nlh4j.membershiphub` |
| **Target Storage Path** | `./sources/docs/architecture/CENTRAL_ENDPOINT_API_CONTRACT_SPECS.md` |
| **Associated Traceability Tags** | `[ARC-000]`, `[ARC-006]`, `[ARC-007]`, `[ARC-008]`, `[ARC-009]`, `[REQ-012]`, `[REQ-013]`, `[EXC-001]`, `[EXC-002]`, `[EXC-005]`, `[DAT-004]`, `[DAT-005]`, `[NFR-001]`, `[NFR-003]`, `[DOC-001]` |
| **Compliance Standard** | Enterprise Microservices REST API & Multi-Module Maven Blueprint |

---

## 🏗️ 1. ARCHITECTURAL SCAFFOLDING & MULTI-MODULE MAVEN TOPOLOGY

The `membership-hub` enterprise backend is engineered as a robust, enterprise-grade multi-module Maven reactor adhering strictly to clean architecture and SOLID principles. Below is the exact directory tree and physical path layout for the backend parent reactor and its four core microservices (`user-service`, `center-service`, `course-service`, `attendance-service`), alongside the Next.js 14 frontend workspace.

---

## 🕒 8. ATTENDANCE-SERVICE & QR PAYLOAD DECODER ARCHITECTURE SPECIFICATION (`[REQ-012]`, `[REQ-013]`, `[ARC-007]`, `[DOC-001]`)

### 8.1 Overview & System Context
The `attendance-service` is an isolated, high-throughput microservice responsible for ingesting, validating, and recording real-time QR code attendance scans from mobile clients (`org.nlh4j.membershiphub.attendanceservice`). It guarantees idempotent execution across distributed nodes, offline retry resilience, and strict FIFO recovery following network partitions (`[EXC-001]`, `[EXC-002]`, `[EXC-005]`).

### 8.2 C4 Container Architecture Components
- **`AttendanceController`**: Exposes REST endpoint `POST /api/v1/attendance/scan` with Bearer token authentication and idempotency key validation (`[REQ-012]`).
- **`QrPayloadDecoder`**: Decodes base64-encoded QR payloads into structured Java records containing `studentId`, `courseId`, and timestamp constraints (`[REQ-012]`).
- **`AttendanceService`**: Core transactional business engine executing enrollment verification, composite uniqueness checks, and attendance persistence (`[REQ-013]`, `[ARC-007]`).
- **`AttendanceRepository`**: Panache repository managing relational operations against PostgreSQL partition-ready tables (`[DAT-004]`, `[DAT-005]`).
- **`KafkaAttendanceProducer`**: SmallRye Reactive Messaging publisher pushing successful scan audit events to Kafka topic `attendance-events` (`[ARC-008]`).

### 8.3 QR Scan Processing Flowchart & Sequence Architecture