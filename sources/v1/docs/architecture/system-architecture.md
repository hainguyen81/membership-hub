markdown
# System Architecture Document - membership-hub
**Document ID:** ARCH-20260818163158  
**Version:** 1.0 (Base)  
**Date:** 2026/08/18 16:31:58  
**Author:** Enterprise System Architect (SA Agent)  
**Traceability Tag Coverage:** [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006], [ARC-007], [ARC-008], [ARC-009], [ARC-010], [REQ-001] to [REQ-025], [EXC-001] to [EXC-005], [DAT-001] to [DAT-011], [NFR-001] to [NFR-009]

---

## Table of Contents
1. [Overall System Architecture](#1-overall-system-architecture)
2. [Traceability Matrix Reference](#2-traceability-matrix-reference)
3. [Microservices Architecture Breakdown](#3-microservices-architecture-breakdown)
4. [Role-Based Access Control (RBAC) Matrix](#4-role-based-access-control-rbac-matrix)
5. [Core Business Data Flow Diagrams](#5-core-business-data-flow-diagrams)
6. [Technology Stack & Dependency Versions](#6-technology-stack--dependency-versions)
7. [GCP & GKE Infrastructure Architecture](#7-gcp--gke-infrastructure-architecture)
8. [Security & Compliance Framework](#8-security--compliance-framework)
9. [Cross-Cutting Concerns](#9-cross-cutting-concerns)
10. [Appendix: File Path Reference Map](#10-appendix-file-path-reference-map)

---

## 1. Overall System Architecture
The membership-hub system follows a 3-tier microservices architecture deployed on Google Kubernetes Engine (GKE), with strict multi-tenancy isolation, event-driven communication via Apache Kafka, and compliance with enterprise security and performance standards.

mermaid
flowchart TD
    subgraph Frontend Layer [FRONTEND LAYER [ARC-010]]
        direction LR
        A[Next.js Web App<br/>./sources/frontend/web-app<br/>[REQ-022, REQ-023, NFR-007]] -->|REST/gRPC over TLS 1.3| B[React Native Mobile App<br/>./sources/frontend/mobile-app<br/>[REQ-020, REQ-021, ARC-009]]
    end

    subgraph Backend Microservices Layer [BACKEND MICROSERVICES LAYER [ARC-010]]
        direction LR
        C[Auth Service<br/>org.nlh4j.saas.auth<br/>./sources/backend/auth-service<br/>[REQ-001, REQ-002, ARC-006]] 
        D[User & Role Service<br/>org.nlh4j.saas.user<br/>./sources/backend/user-service<br/>[REQ-003, ARC-001]]
        E[Center Service<br/>org.nlh4j.saas.center<br/>./sources/backend/center-service<br/>[REQ-004, REQ-005, REQ-006, ARC-002]]
        F[Course Service<br/>org.nlh4j.saas.course<br/>./sources/backend/course-service<br/>[REQ-007, REQ-008, REQ-009, ARC-007]]
        G[Enrollment Service<br/>org.nlh4j.saas.enrollment<br/>./sources/backend/enrollment-service<br/>[REQ-010, REQ-011, ARC-007]]
        H[Attendance Service<br/>org.nlh4j.saas.attendance<br/>./sources/backend/attendance-service<br/>[REQ-012, REQ-013, EXC-001, EXC-002, ARC-007]]
        I[Membership Service<br/>org.nlh4j.saas.membership<br/>./sources/backend/membership-service<br/>[REQ-014, REQ-015, DAT-007]]
        J[Notification Service<br/>org.nlh4j.saas.notification<br/>./sources/backend/notification-service<br/>[REQ-016, EXC-003, ARC-008]]
        K[Promotion & Announcement Service<br/>org.nlh4j.saas.promotion<br/>./sources/backend/promotion-service<br/>[REQ-017, REQ-018, DAT-009]]
        L[Chatbot Service<br/>org.nlh4j.saas.chatbot<br/>./sources/backend/chatbot-service<br/>[REQ-019]]
        M[Report Service<br/>org.nlh4j.saas.report<br/>./sources/backend/report-service<br/>[REQ-024, REQ-025]]
    end

    subgraph Data Layer [DATA LAYER [ARC-010]]
        direction LR
        N[(PostgreSQL 15<br/>Primary + Read Replica DB<br/>./sources/backend/migrations<br/>[DAT-001 to DAT-011, NFR-009])]
        O[(Redis 7.0<br/>Session & Offline Cache<br/>[ARC-009, NFR-004])]
        P[Apache Kafka 3.7.0<br/>Event Broker<br/>[ARC-008, ARC-007]]
    end

    subgraph Infrastructure Layer [INFRASTRUCTURE LAYER [ARC-010]]
        direction LR
        Q[GKE Standard Cluster<br/>./sources/infra/gke<br/>[NFR-002, NFR-004]]
        R[Terraform IaC<br/>./sources/infra/terraform<br/>[NFR-001, NFR-002, NFR-009]]
        S[Docker Multi-stage Builds<br/>./sources/infra/docker<br/>[NFR-005]]
        T[GCP Managed Services<br/>Firebase Auth, Cloud SQL, Cloud Logging, Secret Manager<br/>[ARC-006, NFR-002, NFR-003, NFR-006]]
    end

    %% Service Interactions
    A -->|REST API Calls| C & D & E & F & G & H & I & J & K & L & M
    B -->|REST API Calls| C & D & E & F & G & H & I & J & K & L & M
    C & D & E & F & G & H & I & J & K & L & M -->|Publish/Subscribe Events| P
    C & D & E & F & G & H & I & J & K & L & M -->|JDBC Prepared Statements| N
    C & D & E & F & G & H & I & J & K & L & M -->|Cache Operations| O
    R -->|Provision| Q & T
    S -->|Build Optimized Images| Q
    Q -->|Run Workloads| C & D & E & F & G & H & I & J & K & L & M
    T -->|Integrate| C & D & E & F & G & H & I & J & K & L & M


---

## 2. Traceability Matrix Reference
All architectural components, event pipelines, and data schemas are mapped directly to project requirement, architecture, and non-functional requirement tags for full auditability.

| Architectural Component | Component Purpose | Mapped Requirement/Architecture/Non-Functional Tags |
|-------------------------|------------------|-----------------------------------------------------|
| Auth Service (org.nlh4j.saas.auth) | OAuth2/JWT authentication, token refresh, logout flow | [REQ-001], [REQ-002], [ARC-006], [NFR-003] |
| RBAC Middleware (org.nlh4j.saas.security.RbacMiddleware) | Global role-based access control enforcement for all API endpoints | [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [NFR-003] |
| Center Service (org.nlh4j.saas.center) | Center CRUD operations, Center Admin assignment, tax ID uniqueness validation | [REQ-004], [REQ-005], [REQ-006], [ARC-002], [DAT-003] |
| Course Service (org.nlh4j.saas.course) | Course CRUD, teacher schedule conflict validation, teacher assignment | [REQ-007], [REQ-008], [REQ-009], [ARC-007], [EXC-001], [DAT-004] |
| Enrollment Service (org.nlh4j.saas.enrollment) | Student course enrollment, auto-provisioning of Student accounts for new users | [REQ-010], [REQ-011], [ARC-007], [DAT-005] |
| Attendance Service (org.nlh4j.saas.attendance) | QR code attendance processing, idempotency enforcement, offline queue sync, FIFO post-outage processing | [REQ-012], [REQ-013], [EXC-001], [EXC-002], [ARC-007], [DAT-006] |
| Membership Service (org.nlh4j.saas.membership) | Membership card display, renewal logic, payment gateway integration | [REQ-014], [REQ-015], [ARC-009], [DAT-007] |
| Notification Service (org.nlh4j.saas.notification) | Multi-channel notification dispatch (FCM/APNs/Zalo), retry logic, delivery tracking | [REQ-016], [REQ-021], [EXC-003], [ARC-008], [DAT-008] |
| Promotion & Announcement Service (org.nlh4j.saas.promotion) | Promotion CRUD, announcement management, auto-hiding of expired content | [REQ-017], [REQ-018], [ARC-008], [DAT-009] |
| Chatbot Service (org.nlh4j.saas.chatbot) | AI chatbot integration, human support escalation for low-confidence responses | [REQ-019], [DAT-011] |
| Report Service (org.nlh4j.saas.report) | Attendance CSV export, real-time enrollment dashboard, post-outage data reconciliation | [REQ-024], [REQ-025], [EXC-005], [DAT-010] |
| Kafka Topic: attendance.scan.request | Queue for incoming QR attendance scan requests from mobile devices | [REQ-012], [ARC-007], [EXC-001] |
| Kafka Topic: attendance.scan.result | Fan-out event for attendance scan results to notification and report services | [REQ-012], [REQ-016], [ARC-007], [ARC-008] |
| Kafka Topic: notification.course.assignment | Event trigger for teacher assignment notifications | [REQ-009], [REQ-016], [ARC-008] |
| Kafka Topic: notification.enrollment.confirmation | Event trigger for enrollment confirmation notifications | [REQ-011], [REQ-016], [ARC-008] |
| PostgreSQL Schema: users, roles | Core user identity and role master data storage | [REQ-001], [REQ-002], [REQ-003], [DAT-001], [ARC-001] |
| PostgreSQL Schema: centers | Center master data storage with tax ID uniqueness constraint | [REQ-004], [REQ-005], [REQ-006], [DAT-003], [ARC-002] |
| PostgreSQL Schema: courses, enrollments | Course metadata and student enrollment data storage | [REQ-007], [REQ-008], [REQ-009], [REQ-010], [REQ-011], [DAT-004], [DAT-005], [ARC-007] |
| PostgreSQL Schema: attendance | Attendance record storage with unique constraint (studentId + courseId + attendanceDate) for idempotency | [REQ-012], [REQ-013], [EXC-001], [EXC-002], [DAT-006], [ARC-007] |
| PostgreSQL Schema: student_cards | Membership card data storage with validity and remaining days tracking | [REQ-014], [REQ-015], [DAT-007], [ARC-009] |
| PostgreSQL Schema: notifications | Notification delivery tracking, retry count, and failure state storage | [REQ-016], [EXC-003], [DAT-008], [ARC-008] |
| PostgreSQL Schema: promotions, announcements | Time-bound promotion and announcement data storage with active status filtering | [REQ-017], [REQ-018], [DAT-009], [ARC-008] |
| PostgreSQL Schema: audit_log, system_settings | Immutable audit logging for all sensitive actions and system configuration storage | [NFR-006], [DAT-010], [DAT-011] |
| Redis Cache | Session storage, offline attendance queue, rate limiting, hot data caching | [ARC-009], [NFR-004], [EXC-001] |
| GKE Horizontal Pod Autoscaler (HPA) | Auto-scaling of backend and frontend pods based on CPU >70% or request latency >300ms | [NFR-002], [NFR-004] |
| PostgreSQL Automated Backup | Daily full backups with Point-in-Time Recovery (PITR) support for 24 hours | [NFR-009] |
| GCP Cloud Audit Logs | Immutable audit trail for all user actions, infrastructure changes, and API calls, retained for 1 year | [NFR-006], [NFR-003] |

---

## 3. Microservices Architecture Breakdown
All backend services are built with Quarkus 3.15.1 (Java 21), follow single-responsibility principle, and communicate via REST APIs and asynchronous Kafka events. All Java packages use the enforced prefix `org.nlh4j.saas`.

| Service Name | Package Path | Core Responsibilities | Dependencies | Mapped Tags |
|--------------|-------------|------------------------|--------------|-------------|
| Auth Service | org.nlh4j.saas.auth | Email/password authentication, OAuth2 integration (Firebase/Google/Facebook), JWT token generation/refresh, logout | Firebase Admin SDK, SmallRye JWT, BCrypt, User Service | [REQ-001], [REQ-002], [ARC-006], [NFR-003] |
| User & Role Service | org.nlh4j.saas.user | User CRUD, role assignment, RBAC permission validation | Auth Service, PostgreSQL | [REQ-003], [ARC-001], [DAT-001] |
| Center Service | org.nlh4j.saas.center | Center CRUD, Center Admin assignment, tax ID uniqueness validation | User Service, PostgreSQL | [REQ-004], [REQ-005], [REQ-006], [ARC-002], [DAT-003] |
| Course Service | org.nlh4j.saas.course | Course CRUD, teacher schedule conflict validation, teacher assignment, course listing | User Service, Enrollment Service, PostgreSQL, Kafka | [REQ-007], [REQ-008], [REQ-009], [ARC-007], [EXC-001], [DAT-004] |
| Enrollment Service | org.nlh4j.saas.enrollment | Student course enrollment, auto-provisioning of Student accounts, enrollment capacity validation | Course Service, User Service, PostgreSQL, Kafka | [REQ-010], [REQ-011], [ARC-007], [DAT-005] |
| Attendance Service | org.nlh4j.saas.attendance | QR code attendance processing, idempotency enforcement, offline queue sync, post-outage FIFO processing | Course Service, Enrollment Service, PostgreSQL, Kafka, Redis | [REQ-012], [REQ-013], [EXC-001], [EXC-002], [ARC-007], [DAT-006] |
| Membership Service | org.nlh4j.saas.membership | Membership card display, remaining days calculation, renewal logic, payment integration | User Service, PostgreSQL, Payment Gateway | [REQ-014], [REQ-015], [ARC-009], [DAT-007] |
| Notification Service | org.nlh4j.saas.notification | Multi-channel notification dispatch (FCM/APNs/Zalo), retry logic (max 3 attempts), delivery tracking | Firebase Admin SDK, Zalo API, PostgreSQL, Kafka | [REQ-016], [REQ-021], [EXC-003], [ARC-008], [DAT-008] |
| Promotion & Announcement Service | org.nlh4j.saas.promotion | Promotion CRUD, announcement management, auto-hiding of expired content | PostgreSQL, Kafka | [REQ-017], [REQ-018], [ARC-008], [DAT-009] |
| Chatbot Service | org.nlh4j.saas.chatbot | AI chatbot integration, human support escalation for low-confidence responses | Third-party AI API, User Service | [REQ-019], [DAT-011] |
| Report Service | org.nlh4j.saas.report | Attendance CSV export, real-time enrollment dashboard, post-outage data reconciliation | Attendance Service, Enrollment Service, PostgreSQL | [REQ-024], [REQ-025], [EXC-005], [DAT-010] |

### Service Interaction Patterns
1. **Synchronous Communication:** Frontend applications call backend services via REST APIs for real-time operations (e.g., login, course listing, attendance scan). All API calls use JWT bearer token authentication and are protected by the global RBAC middleware.
2. **Asynchronous Communication:** Cross-service events (e.g., enrollment success, course assignment, attendance confirmation) are published to Apache Kafka topics for decoupled, fault-tolerant processing. This ensures no data loss during service outages and enables fan-out to multiple downstream consumers (e.g., notification service, report service).
3. **Data Access:** All database access uses Hibernate ORM with prepared statements to eliminate SQL injection risks. Read-heavy workloads (e.g., course listing, dashboard metrics) use PostgreSQL read replicas to reduce load on the primary database.

---

## 4. Role-Based Access Control (RBAC) Matrix
The system implements 5 predefined roles with strict permission isolation, enforced at both the API gateway and service layers. All permissions are mapped to the core RBAC architecture tags [ARC-001] to [ARC-005].

| Permission | System Admin | Center Admin | Manager | Teacher | Student | Mapped ARC Tags |
|------------|-------------|--------------|---------|---------|---------|-----------------|
| **User Management** | | | | | | [ARC-001] |
| Create/edit/delete users | ✅ Full Access | ❌ No Access | ❌ No Access | ❌ No Access | ❌ No Access (edit own profile only) | |
| Assign/change user roles | ✅ Full Access | ❌ No Access | ❌ No Access | ❌ No Access | ❌ No Access | |
| View user list (all centers) | ✅ Full Access | ✅ Own center only | ✅ Own center only | ❌ No Access | ❌ No Access | |
| **Center Management** | | | | | | [ARC-002] |
| Create/edit/delete centers | ✅ Full Access | ❌ No Access | ❌ No Access | ❌ No Access | ❌ No Access | |
| Assign Center Admin | ✅ Full Access | ❌ No Access | ❌ No Access | ❌ No Access | ❌ No Access | |
| View center details | ✅ Full Access | ✅ Own center only | ✅ Own center only | ✅ Assigned centers only | ✅ All centers (read-only) | |
| **Course Management** | | | | | | [ARC-003], [ARC-004] |
| Create/edit/delete courses | ✅ Full Access | ✅ Own center only | ❌ No Access | ❌ No Access | ❌ No Access | |
| Assign teachers to courses | ✅ Full Access | ✅ Own center only | ❌ No Access | ❌ No Access | ❌ No Access | |
| View course list | ✅ Full Access | ✅ Own center only | ✅ Own center only | ✅ Assigned courses only | ✅ Available courses only | |
| **Enrollment Management** | | | | | | [ARC-003], [ARC-004], [ARC-005] |
| Enroll in courses | ❌ No Access | ❌ No Access | ❌ No Access | ❌ No Access | ✅ Own enrollment only | |
| Manage student enrollments | ✅ Full Access | ✅ Own center only | ✅ Own center only | ✅ Assigned courses only | ❌ No Access | |
| **Attendance Management** | | | | | | [ARC-004], [ARC-005] |
| Record attendance | ❌ No Access | ❌ No Access | ❌ No Access | ✅ Assigned courses only | ✅ Own attendance (enrolled courses) | |
| View attendance reports | ✅ Full Access | ✅ Own center only | ✅ Own center only | ✅ Assigned courses only | ✅ Own attendance only | |
| **Membership Management** | | | | | | [ARC-005] |
| View membership card | ✅ Full Access | ✅ Own center students | ✅ Own center students | ❌ No Access | ✅ Own card only | |
| Renew membership | ❌ No Access | ❌ No Access | ❌ No Access | ❌ No Access | ✅ Own renewal only | |
| **Notification Management** | | | | | | [ARC-008] |
| Send notifications | ✅ Full Access | ✅ Own center only | ✅ Own center only | ❌ No Access | ❌ No Access | |
| Receive notifications | ✅ Full Access | ✅ Own center | ✅ Own center | ✅ Assigned courses | ✅ Personal notifications | |
| **Promotion & Announcement Management** | | | | | | [ARC-008] |
| Create/edit/delete promotions | ✅ Full Access | ✅ Own center only | ✅ Own center only | ❌ No Access | ❌ No Access | |
| View active promotions | ✅ Full Access | ✅ Own center | ✅ Own center | ✅ Own center | ✅ All active promotions | |
| **Report Access** | | | | | | [ARC-007] |
| View attendance reports | ✅ Full Access | ✅ Own center only | ✅ Own center only | ✅ Assigned courses only | ✅ Own attendance only | |
| View enrollment dashboard | ✅ Full Access | ✅ Own center only | ✅ Own center only | ✅ Assigned courses only | ❌ No Access | |
| **Chatbot Access** | | | | | | [REQ-019] |
| Access chatbot support | ✅ Full Access | ✅ Full Access | ✅ Full Access | ✅ Full Access | ✅ Full Access | |

---

## 5. Core Business Data Flow Diagrams
All core business flows are designed for idempotency, fault tolerance, and compliance with enterprise requirements.

### 5.1 OAuth2/JWT Authentication Flow [ARC-006, REQ-001, REQ-002]
mermaid
sequenceDiagram
    actor U as User
    participant F as Frontend (Web/Mobile)
    participant A as Auth Service [ARC-006]
    participant FB as Firebase Authentication [ARC-006]
    participant DB as PostgreSQL [DAT-001]
    participant R as Redis [ARC-009]

    U->>F: Select login method (Email/OAuth2)
    alt Email/Password Login
        F->>A: POST /api/v1/auth/login (email, password)
        A->>DB: Validate user credentials (bcrypt hash check)
        DB-->>A: User record + role ID
    else OAuth2 Login (Google/Facebook)
        F->>A: POST /api/v1/auth/oauth2/{provider} (auth code)
        A->>FB: Exchange auth code for user info
        FB-->>A: User profile (email, name, provider)
        A->>DB: Create/update user record if not exists
        DB-->>A: User record + role ID
    end
    A->>A: Generate JWT Access Token (15min expiry) + Refresh Token (7day expiry)
    A->>DB: Store encrypted refresh token
    A-->>F: Return JWT tokens + user role info
    F->>R: Store session data (JWT, user preferences)
    F-->>U: Redirect to role-based home screen


### 5.2 QR Code Attendance Flow [REQ-012, REQ-013, EXC-001, EXC-002, ARC-007]
mermaid
sequenceDiagram
    actor S as Student
    participant M as Mobile App [REQ-012]
    participant A as Attendance Service [REQ-013, EXC-001, EXC-002]
    participant K as Kafka [ARC-007]
    participant DB as PostgreSQL [DAT-006]
    participant N as Notification Service [REQ-016]

    S->>M: Scan course QR code
    M->>M: Parse QR payload (courseId, sessionId)
    M->>A: POST /api/v1/attendance/scan (JWT, courseId, sessionId, timestamp)
    A->>DB: Check unique constraint (studentId, courseId, attendanceDate)
    alt No existing attendance record
        A->>DB: Insert new attendance record
        A->>K: Publish attendance.scan.result event (SUCCESS)
        K->>N: Trigger confirmation push notification to student
        A-->>M: Return 200 OK {status: RECORDED, attendanceId}
    else Existing attendance record
        A->>K: Publish attendance.scan.result event (DUPLICATE)
        A-->>M: Return 200 OK {status: DUPLICATE, message: "Already recorded for this session"}
    end
    alt Network error during scan [EXC-001]
        M->>M: Store request in offline queue (Redis)
        M-->>S: Show "Saved offline, will sync when connected"
        Note over M,A: Auto-retry when network restored, process requests in FIFO order
    end


### 5.3 Multi-Channel Notification Flow [REQ-016, EXC-003, ARC-008]
mermaid
flowchart LR
    A[System Event Trigger<br/>[REQ-016, ARC-008]] -->|Publish to Kafka Topic| B(Kafka Event Broker<br/>Topics: attendance.confirmed, course.assigned, announcement.created)
    B -->|Consume Event| C[Notification Service<br/>[REQ-016, EXC-003]]
    C -->|Send Push Notification| D[FCM/APNs Gateway<br/>[REQ-021]]
    C -->|Send Group Message| E[Zalo Official Account API<br/>[ARC-008]]
    C -->|Store Delivery Record| F[(PostgreSQL notifications table<br/>[DAT-008])]
    D -->|Deliver| G[User Device (Mobile/Web)]
    E -->|Deliver| H[Zalo Group Chat]
    C -->|Retry up to 3x on failure (5min interval)| C
    C -->|Alert admin if final failure| I[System Admin Dashboard]


### 5.4 Course Enrollment Flow [REQ-010, REQ-011, ARC-007]
mermaid
sequenceDiagram
    actor St as Student
    participant F as Frontend Web [REQ-010]
    participant E as Enrollment Service [REQ-011, ARC-007]
    participant C as Course Service [REQ-007, ARC-007]
    participant U as User Service [REQ-001, ARC-001]
    participant DB as PostgreSQL [DAT-005]
    participant N as Notification Service [REQ-016]

    St->>F: Browse available courses
    F->>C: GET /api/v1/courses/available (JWT)
    C->>DB: Query active courses, exclude courses already enrolled by student
    DB-->>C: List of available courses with remaining slots
    C-->>F: Return course list
    St->>F: Select course to enroll
    F->>E: POST /api/v1/enrollments (courseId, studentEmail)
    E->>DB: Check if student user exists
    alt Student does not exist
        E->>U: Create new Student user account
        U->>DB: Insert user record with default Student role
        DB-->>U: Return new userId
    end
    E->>DB: Check course capacity (maxStudents vs current enrolled count)
    alt Course has available slots
        E->>DB: Insert enrollment record (enforce unique constraint studentId + courseId)
        E->>N: Publish enrollment success event
        N->>St: Send confirmation notification (push + Zalo group)
        E-->>F: Return 201 Created {enrollmentId, message: "Enrolled successfully"}
    else Course full
        E-->>F: Return 409 Conflict {error: "COURSE_FULL", message: "No available slots"}
    end


### 5.5 Membership Card Renewal Flow [REQ-014, REQ-015, ARC-009]
mermaid
sequenceDiagram
    actor St as Student
    participant F as Frontend Web [REQ-015]
    participant M as Membership Service [REQ-014, REQ-015]
    participant P as Payment Gateway
    participant DB as PostgreSQL [DAT-007]
    participant N as Notification Service [REQ-016]

    St->>F: View membership card details
    F->>M: GET /api/membership/card (JWT)
    M->>DB: Query student_cards record for authenticated user
    DB-->>M: Return card details (remainingDays, expiryDate, validityDays)
    M-->>F: Display card with remaining days and expiry date
    St->>F: Select renewal period, initiate payment
    F->>P: Redirect to secure payment gateway
    P-->>F: Return payment transaction result
    alt Payment successful
        F->>M: POST /api/membership/renew (renewalDays, paymentTransactionId)
        M->>DB: Update student_cards (remainingDays += renewalDays, issue_date = CURRENT_DATE)
        M->>N: Publish renewal success event
        N->>St: Send confirmation notification
        M-->>F: Return 200 OK {remainingDays, newExpiryDate}
    else Payment failed
        M-->>F: Return 402 Payment Required {error: "PAYMENT_FAILED", message: "Payment transaction failed"}
    end


---

## 6. Technology Stack & Dependency Versions [ARC-010]
All technology versions are pinned to ensure consistent behavior across development, staging, and production environments.

| Layer | Technology | Version | Purpose | Mapped Tags |
|-------|------------|---------|---------|-------------|
| Backend Runtime | Quarkus | 3.15.1 (Java 21) | Microservice runtime, native image support, fast boot time | [ARC-010] |
| Backend ORM | Hibernate ORM | 6.4.4 | Database object mapping, transaction management, prepared statement generation | [ARC-010], [NFR-003] |
| Database Driver | PostgreSQL JDBC | 42.7.3 | Secure PostgreSQL connectivity with TLS 1.3 support | [ARC-010], [NFR-003] |
| Event Broker Client | Apache Kafka Client | 3.7.0 | Asynchronous event processing, decoupled service communication | [ARC-007], [ARC-008] |
| Authentication | Firebase Admin SDK | 9.2.0 | OAuth2 integration, user identity management, token validation | [ARC-006] |
| Password Hashing | BCrypt | 0.10.8 | Secure password hashing with work factor 12 | [NFR-003] |
| JWT Processing | SmallRye JWT | 3.15.1 | JWT token generation, validation, refresh flow implementation | [ARC-006] |
| Connection Pooling | HikariCP | 5.0.1 | High-performance database connection pool management | [ARC-010] |
| Frontend Web | Next.js | 14.1.0 (React 18.2.0) | Server-side rendered web application, SEO support, multi-language routing | [ARC-010], [REQ-023], [NFR-007] |
| Mobile Framework | React Native | 0.73.2 | Cross-platform mobile application development for iOS/Android | [ARC-010], [REQ-020] |
| State Management | Redux Toolkit | 2.0.1 | Global state management for frontend applications | [ARC-010] |
| Data Fetching | React Query | 5.17.0 | Server state management, caching, background refetching, offline support | [ARC-010], [ARC-009] |
| Internationalization | i18next | 23.7.0 | Multi-language support (English, Vietnamese, Spanish) with locale persistence | [REQ-022], [NFR-007] |
| HTTP Client | Axios | 1.6.7 | API request handling with interceptors for JWT injection and error handling | [ARC-010] |
| Frontend Auth | Firebase SDK | 10.7.0 | Client-side authentication, push notification registration, token management | [ARC-006], [REQ-021] |
| Caching | Redis | 7.0 | Session storage, offline attendance queue, rate limiting, hot data caching | [ARC-009], [NFR-004] |
| Containerization | Docker | 24.0.7 | Multi-stage image builds, environment consistency, layer caching | [NFR-005] |
| Infrastructure as Code | Terraform | 1.7.0 | GCP resource provisioning, version-controlled infrastructure, drift detection | [NFR-001], [NFR-002], [NFR-009] |
| Orchestration | Kubernetes (GKE) | 1.28.0 | Container orchestration, auto-scaling, high availability, rolling updates | [NFR-002], [NFR-004] |
| CI/CD | GitHub Actions | Latest | Automated build, test, security scanning, deployment pipeline | [NFR-004], [NFR-006] |

---

## 7. GCP & GKE Infrastructure Architecture [NFR-001, NFR-002, NFR-004, NFR-009]
All infrastructure is provisioned via Terraform as code, with strict network isolation and least-privilege access controls.

mermaid
flowchart TD
    subgraph GCP Project [membership-hub-prod [NFR-001]]
        subgraph VPC Network [Private VPC [NFR-003]]
            direction LR
            A[Public Subnet<br/>Ingress Nginx Controller] -->|TLS 1.3 Termination| B[Private Subnet<br/>GKE Cluster]
            B -->|Private IP Only, No Public IP| C[(Cloud SQL PostgreSQL 15<br/>High Availability Instance<br/>[NFR-003, NFR-009])]
            B -->|Private IP Only| D[(Memorystore Redis 7.0<br/>Cluster Mode [ARC-009, NFR-004])]
            B -->|Private IP Only| E[Cloud Storage Bucket<br/>Encrypted at Rest (AES-256)<br/>Report CSV Storage [NFR-009]]
        end
        F[Firebase Project<br/>Authentication, FCM, Crashlytics<br/>[ARC-006, REQ-021]] -->|Secure API Calls over TLS 1.3| B
        G[Cloud Logging & Monitoring<br/>[NFR-002, NFR-006]] -->|Collect Logs/Metrics| B
        H[Secret Manager<br/>[NFR-003]] -->|Inject Secrets at Runtime| B
        I[Cloud Backup Vault<br/>Cross-region Replication [NFR-009]] -->|Daily Automated Backups| C
        J[Cloud CDN<br/>[NFR-001]] -->|Cache Static Assets| A
    end

    subgraph GKE Cluster [GKE Standard Cluster [NFR-002]]
        direction LR
        K[Ingress Nginx Controller] -->|Route Traffic| L[Backend Microservices Pods<br/>Quarkus Java 21, Distroless Base Image]
        K -->|Route Traffic| M[Frontend Web Pod<br/>Next.js 14.1.0, Alpine Base Image]
        L -->|Autoscale| N[Horizontal Pod Autoscaler (HPA)<br/>CPU >70% / Request Latency >300ms [NFR-004]]
        M -->|Autoscale| N
        O[Pod Anti-Affinity Rules<br/>[NFR-002]] -->|Spread Across 3 Availability Zones| L & M
        P[Liveness/Readiness Probes<br/>[NFR-002]] -->|Health Checks| L & M
        Q[Resource Limits<br/>CPU/Memory Requests + Limits [NFR-004]] -->|Enforce Resource Quotas| L & M
    end

    I -->|Cross-region Replication| I2[DR Region GKE Cluster<br/>Failover Target [NFR-002]]


### Infrastructure Provisioning Steps [NFR-001, NFR-002, NFR-009]
1. **Network Setup:** Create private VPC with separate public/private subnets, configure firewall rules to only allow ingress from approved CIDR ranges, disable public IP assignment for all compute resources.
2. **GCP Service Provisioning:** Create Cloud SQL PostgreSQL instance with high availability, enable automated daily backups with PITR, create Redis cluster, create Cloud Storage bucket for report storage, set up Firebase project with OAuth2 providers enabled.
3. **GKE Cluster Setup:** Create GKE standard cluster with private nodes, enable Workload Identity for service account authentication, configure HPA, pod anti-affinity, and resource limits for all workloads.
4. **Secret Management:** Store all sensitive credentials (database passwords, API keys, JWT secrets) in GCP Secret Manager, inject secrets into pods at runtime via SecretProviderClass.
5. **CI/CD Pipeline Setup:** Configure GitHub Actions with automated build, test, security scanning, and deployment to GKE, with mandatory approval gates for production deployments.

---

## 8. Security & Compliance Framework [NFR-003]
All security controls are implemented to comply with OWASP Top 10, GDPR, and CCPA requirements.

| Security Control | Implementation | Mapped Tags |
|------------------|----------------|-------------|
| Data in Transit Encryption | TLS 1.3 enforced for all API calls, internal service communication, and database connections | [NFR-003] |
| Data at Rest Encryption | AES-256 encryption for PostgreSQL, Redis, and Cloud Storage | [NFR-003] |
| Authentication | Firebase OAuth2 (Google/Facebook/Email) with JWT access tokens (15min expiry) and refresh tokens (7day expiry) stored in HttpOnly cookies | [ARC-006], [NFR-003] |
| Authorization | Global RBAC middleware with role-based permission validation for all API endpoints, center-level isolation for Center Admin and Manager roles | [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [NFR-003] |
| Injection Prevention | All database queries use prepared statements with parameterized inputs, no dynamic SQL string concatenation | [NFR-003] |
| XSS Prevention | Frontend uses JSX auto-escaping, CSP headers configured on Ingress Gateway, all user input sanitized before storage/display | [NFR-003] |
| CSRF Prevention | CSRF tokens required for all state-changing API requests (POST/PUT/DELETE) | [NFR-003] |
| Sensitive Data Masking | PII fields (email, phone, address) are automatically masked in logs and API responses via `@JsonSerialize` annotations | [NFR-003], [NFR-006] |
| Audit Logging | All sensitive actions (role changes, attendance scans, notification sends) are logged with timestamp, user ID, and action details, retained for 1 year | [NFR-006] |
| Idempotency | Attendance scan endpoint enforces idempotency via database unique constraint, all mutation APIs support idempotency keys | [REQ-012], [REQ-013], [NFR-003] |
| Least Privilege Access | Service accounts are granted only the minimum required permissions for their target resources, no wildcard roles assigned | [NFR-003] |

---

## 9. Cross-Cutting Concerns
### 9.1 Logging & Monitoring [NFR-006]
- All services use SLF4J/Logback for structured logging, with log entries including timestamp, service name, log level, and traceability tag IDs.
- Logs are shipped to GCP Cloud Logging, with retention set to 1 year for audit compliance.
- Metrics (request latency, error rate, pod resource usage) are collected via GCP Cloud Monitoring, with alerts configured for SLA violations (uptime <99.9%, API latency >200ms).

### 9.2 Caching Strategy [ARC-009, NFR-004]
- Session data (JWT, user preferences) is cached in Redis with 24-hour TTL.
- Hot data (course lists, active promotions) is cached in Redis with LRU eviction policy when memory is full.
- Offline attendance requests are queued in Redis when the mobile app loses network connectivity, with automatic FIFO sync when connectivity is restored.

### 9.3 Error Handling & Retry Logic [EXC-001, EXC-002, EXC-003, EXC-005]
- All external API calls (Firebase, Zalo, Payment Gateway) implement retry logic with exponential backoff, maximum 3 retry attempts.
- Post-outage recovery processes all pending attendance requests in FIFO order to ensure no data loss.
- All exceptions are logged with full context (service name, error message, traceability tag ID) and re-thrown with the original root cause preserved for debugging.

---

## 10. Appendix: File Path Reference Map
All source code, configuration, and documentation files follow the project's standardized directory structure:

| Component Type | File Path | Mapped Tags |
|---------------|-----------|-------------|
| Database Migrations | ./sources/backend/migrations/V1__create_users_roles.sql | [DAT-001] |
| | ./sources/backend/migrations/V2__create_centers_courses_enrollments.sql | [DAT-003], [DAT-004], [DAT-005] |
| | ./sources/backend/migrations/V3__create_attendance.sql | [DAT-006] |
| | ./sources/backend/migrations/V4__create_student_cards.sql | [DAT-007] |
| | ./sources/backend/migrations/V5__create_notifications.sql | [DAT-008] |
| | ./sources/backend/migrations/V6__create_promotions_announcements.sql | [DAT-009] |
| | ./sources/backend/migrations/V7__create_audit_system_settings.sql | [DAT-010], [DAT-011] |
| Backend Services | ./sources/backend/auth-service/src/main/java/org/nlh4j/saas/auth/* | [REQ-001], [REQ-002], [ARC-006] |
| | ./sources/backend/user-service/src/main/java/org/nlh4j/saas/user/* | [REQ-003], [ARC-001] |
| | ./sources/backend/center-service/src/main/java/org/nlh4j/saas/center/* | [REQ-004], [REQ-005], [REQ-006], [ARC-002] |
| | ./sources/backend/course-service/src/main/java/org/nlh4j/saas/course/* | [REQ-007], [REQ-008], [REQ-009], [ARC-007] |
| | ./sources/backend/enrollment-service/src/main/java/org/nlh4j/saas/enrollment/* | [REQ-010], [REQ-011], [ARC-007] |
| | ./sources/backend/attendance-service/src/main/java/org/nlh4j/saas/attendance/* | [REQ-012], [REQ-013], [EXC-001], [EXC-002], [ARC-007] |
| | ./sources/backend/membership-service/src/main/java/org/nlh4j/saas/membership/* | [REQ-014], [REQ-015], [ARC-009] |
| | ./sources/backend/notification-service/src/main/java/org/nlh4j/saas/notification/* | [REQ-016], [EXC-003], [ARC-008] |
| | ./sources/backend/promotion-service/src/main/java/org/nlh4j/saas/promotion/* | [REQ-017], [REQ-018], [ARC-008] |
| | ./sources/backend/chatbot-service/src/main/java/org/nlh4j/saas/chatbot/* | [REQ-019] |
| | ./sources/backend/report-service/src/main/java/org/nlh4j/saas/report/* | [REQ-024], [REQ-025], [EXC-005] |
| Frontend Applications | ./sources/frontend/web-app/src/app/[locale]/* | [REQ-022], [REQ-023], [NFR-007] |
| | ./sources/frontend/mobile-app/src/main/java/com/hub/mobile/* | [REQ-020], [REQ-021], [ARC-009] |
| Infrastructure | ./sources/infra/docker/Dockerfile | [NFR-005] |
| | ./sources/infra/terraform/main.tf | [NFR-001], [NFR-002], [NFR-009] |
| | ./sources/infra/gke/deployment.yaml | [NFR-002], [NFR-004] |
| | ./sources/infra/gke/hpa.yaml | [NFR-004] |
| Documentation | ./sources/docs/api/auth-api-spec.md | [REQ-001], [REQ-002], [ARC-006] |
| | ./sources/docs/api/course-service-api-spec.md | [REQ-007], [REQ-008], [REQ-009], [ARC-007] |
| | ./sources/docs/api/attendance-service-api-spec.md | [REQ-012], [REQ-013], [EXC-001], [EXC-002], [ARC-007] |
| | ./sources/docs/operations/i18n-seo-guide.md | [REQ-022], [REQ-023], [NFR-007] |
| | ./sources/docs/operations/deployment-monitoring.md | [NFR-002], [NFR-006], [NFR-009] |

---

## Document Approval
| Role | Name | Signature | Date |
|------|------|-----------|------|
| Enterprise System Architect | SA Agent | [Signed] | 2026/08/18 |
| Technical Lead | [Pending] | [Pending] | [Pending] |
| DevOps Lead | [Pending] | [Pending] | [Pending] |
| Security Officer | [Pending] | [Pending] | [Pending] |