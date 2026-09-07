# 🏛️ CENTRAL MONITORING, LOGGING & ATTENDANCE ARCHITECTURE
## 📊 1. SYSTEM OVERVIEW & CORE TRACEABILITY

### ⚙️ 1.1. ARCHITECTURAL SCOPE & TECHNICAL STACK
The Membership Hub project adheres to a multi-module Maven architecture with the base package prefix `org.nlh4j.membershiphub`. The project consists of 5 microservices:
- `user-service`
- `center-service`
- `course-service`
- `attendance-service`
- `notification-service`

### 📊 1.2. TRACEABILITY MATRIX REFERENCE
| Module | Targeted Tag IDs |
| --- | --- |
| `user-service` | [ARC-000], [REQ-001], [REQ-002] |
| `center-service` | [ARC-000], [REQ-005], [REQ-006] |
| `course-service` | [ARC-000], [REQ-007], [REQ-008] |
| `attendance-service` | [ARC-000], [REQ-012], [REQ-013], [ARC-007], [EXC-001], [EXC-002], [EXC-005], [DOC-001] |
| `notification-service` | [ARC-000], [REQ-016], [REQ-021] |

## 📁 2. ATTENDANCE SERVICE & QR DECODING ARCHITECTURE

### ⚙️ 2.1. ATTENDANCE-SERVICE COMPONENT TOPOLOGY
The `attendance-service` microservice is engineered using Quarkus 3.15 LTS and Hibernate ORM Panache to manage real-time attendance tracking via QR code scanning [ARC-007]. The physical module path is `./sources/backend/attendance-service/`.

#### 2.1.1. Component Breakdown
The service is composed of the following core components:

| Component | Package Path | Targeted Tag IDs | Responsibility |
| :--- | :--- | :--- | :--- |
| `AttendanceController` | `org.nlh4j.membershiphub.attendanceservice.controller` | [REQ-012], [ARC-007] | REST endpoint handler for QR scan ingestion (`POST /api/v1/attendance/scan`) |
| `AttendanceService` | `org.nlh4j.membershiphub.attendanceservice.service` | [REQ-013], [EXC-002] | Business logic for attendance validation, idempotency checks, and persistence |
| `QrPayloadDecoder` | `org.nlh4j.membershiphub.attendanceservice.service` | [REQ-012] | Decodes and validates base64-encoded QR payloads |
| `AttendanceRepository` | `org.nlh4j.membershiphub.attendanceservice.repository` | [DAT-004] | Panache repository for database operations |
| `KafkaAttendanceProducer` | `org.nlh4j.membershiphub.attendanceservice.messaging` | [ARC-008] | Publishes attendance events to Kafka topics (`attendance-events`) |

#### 2.1.2. C4 Container Diagram & QR Scan Workflow
The following Mermaid flowchart illustrates the step-by-step processing workflow of an incoming QR scan request, mapping directly to the system requirements [REQ-012], [REQ-013], [ARC-007], [EXC-001], [EXC-002], and [EXC-005].