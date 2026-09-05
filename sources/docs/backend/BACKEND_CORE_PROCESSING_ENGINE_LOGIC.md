# 📊 Scaffolding Architecture of Membership Hub
## 📁 Overview
The Membership Hub project is structured as a multi-module Maven project with a root directory `./sources/backend` containing four microservices: `user-service`, `center-service`, `course-service`, and `attendance-service`. The Java package prefix base is `org.nlh4j.membershiphub`.

## 📁 Backend Scaffolding Details
### 📂 Multi-Module Maven Structure
(Existing structure preserved)

---

## 📁 Attendance Service Architecture
### 📂 Overview
The `attendance-service` is a critical component of the Membership Hub, responsible for real-time QR-based attendance tracking. It ensures high availability and data consistency through an idempotent processing engine.

### 📂 C4 Container Components
| Component | Responsibility | Targeted Tag IDs |
| :--- | :--- | :--- |
| `AttendanceController` | REST entry point for QR scan requests | [REQ-012], [ARC-007] |
| `QrPayloadDecoder` | Decodes base64 payload to extract studentID and courseID | [REQ-012] |
| `AttendanceService` | Orchestrates validation, idempotency check, and persistence | [REQ-013], [ARC-007] |
| `AttendanceRepository` | PostgreSQL interaction with composite unique constraints | [REQ-013] |
| `KafkaAttendanceProducer` | Publishes attendance events to Kafka for downstream processing | [ARC-007] |

### 📂 QR Scan Processing Flow
The following flowchart illustrates the robust processing pipeline for attendance scans, including fault tolerance and idempotency mechanisms.