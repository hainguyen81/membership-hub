# 🏛️ CENTRAL MONITORING & LOGGING ARCHITECTURE
## 📊 1. SYSTEM OVERVIEW & CORE STACK TRACEABILITY

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
| `center-service` | [ARC-000], [REQ-004], [REQ-005] |
| `course-service` | [ARC-000], [REQ-007], [REQ-008] |
| `attendance-service` | [ARC-000], [REQ-012], [REQ-013], [ARC-007], [EXC-001], [EXC-002], [EXC-005], [DOC-001] |
| `notification-service` | [ARC-000], [REQ-016], [REQ-021] |

## 📁 2. ATTENDANCE SERVICE & QR DECODING ARCHITECTURE

### ⚙️ 2.1. ATTENDANCE-SERVICE COMPONENT TOPOLOGY
The `attendance-service` microservice is engineered using Quarkus 3.15 LTS and Hibernate ORM Panache to manage real-time attendance tracking via QR code scanning. The physical module path is `./sources/backend/attendance-service/`.

### ⚙️ 2.2. QR SCAN PROCESSING ARCHITECTURE
The attendance processing pipeline is designed for high availability and idempotency, ensuring that duplicate scans do not result in multiple attendance records.

#### 2.2.1. Processing Flowchart