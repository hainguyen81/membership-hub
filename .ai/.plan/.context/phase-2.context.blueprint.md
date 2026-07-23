# PHASE 2 CONTEXT BLUEPRINT: membership-hub

## 1. Phase Operational Scope & Objectives
Implement the core business logic, authentication system (internal email/password and external Firebase/Google/Facebook), role‑based access control (RBAC) with tenant‑scoped permissions, and comprehensive unit/integration testing. Deliver production‑ready Java/Quarkus services that enforce OWASP best‑practice security (multi‑tenant `tenant_id` isolation, AES‑256 PII encryption, parameterized queries) and provide full test coverage for all new components.

## 2. Allowed Technical Scope & Directory Boundaries
- **Backend source roots**
  - `./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/` – all domain, service, auth, rbac, and configuration classes.
  - `./sources/backend/src/main/resources/` – Quarkus application.yml, OpenAPI/Swagger config, external provider credentials.
  - `./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/` – unit and integration test suites.
- **Documentation roots**
  - `./sources/backend/docs/` – authentication flow, RBAC spec, core API docs.
- **No frontend or mobile assets** are part of Phase 2; all work is confined to the backend Java stack.

## 3. Dedicated Sub-Agent Functional Directives
- **coder** – Build the authentication services (internal & external adapters), RBAC infrastructure, and core business services (Course, Enrollment, Attendance, Notification). Inject OWASP compliance (tenant isolation, AES‑256 encryption, parameterized queries) directly into each service.
- **tester** – Write unit tests for every new service/class and a single integration test that validates end‑to‑end workflows. Use the strict `<source>;<test>` pair syntax for unit tests and `INTEGRATION_SCOPE;<test>` for integration tests.
- **reviewer** – Perform static code analysis, security linting, and compiler error fixes on each individual Java source file targeted by the coder. Ensure OWASP rule adherence.
- **doc** – Produce concise technical documentation for authentication flow, RBAC specifications, and core API usage, stored under `./sources/backend/docs/`.
- **docker**, **GCP**, **GKE** – Not required for Phase 2; their agents remain idle.

## 4. Phase Definition of Done (DoD)
- All authentication endpoints (internal login, Firebase/Google/Facebook SSO) functional with JWT issuance and validation.
- Role enum, permission matrix, and tenant‑scoped AuthorizationService fully implemented and secured.
- Core services (CourseService, EnrollmentService, AttendanceService, NotificationService) expose CRUD operations, support QR‑based attendance, and encrypt PII using AES‑256.
- Unit test coverage ≥ 80 % for each new service; all unit tests pass.
- One integration test validates the complete user journey (auth → enrollment → attendance → notification) and passes.
- Every Java source file reviewed by the reviewer agent; OWASP compliance confirmed.
- Documentation files (auth‑flow.md, rbac‑spec.md, api‑core.md) created and placed under `./sources/backend/docs/`.

## 5. DAY‑BY‑DAY ARCHITECTURAL EXECUTION LOGS

### DAY 1: Establish Authentication Infrastructure
#### SUB-TASK 1.1: Implement Internal and External Authentication Services
**Assigned Sub-Agent:** coder
**Targeted Components & Technical Requirements:**
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/auth/InternalAuthService.java
    *   **Architectural Requirements:**
        *   Provide `authenticate(String email, String password)` returning JWT token; enforce tenant‑scoped `tenant_id` validation.
        *   Use parameterized queries for user lookup; hash passwords with bcrypt.
        *   Apply AES‑256 encryption for any PII fields stored in token claims.
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/auth/ExternalAuthAdapter.java
    *   **Architectural Requirements:**
        *   Implement `authenticateWithFirebase(String idToken)`, `authenticateWithGoogle(String accessToken)`, `authenticateWithFacebook(String longLivedToken)`.
        *   Validate provider tokens against their public keys; map provider user IDs to internal `User` entities with tenant isolation.
        *   Store provider‑specific refresh tokens encrypted with AES‑256.
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/security/SecurityConfig.java
    *   **Architectural Requirements:**
        *   Configure Quarkus JWT realm; enforce role‑based permission checks via `AuthorizationService`.
        *   Enable CORS for allowed origins; enforce HTTPS in production.

#### SUB-TASK 1.2: Write Unit Tests for Authentication Services
**Assigned Sub-Agent:** tester
**Targeted Components & Technical Requirements:**
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/auth/InternalAuthService.java;./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/auth/InternalAuthServiceTest.java
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/auth/ExternalAuthAdapter.java;./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/auth/ExternalAuthAdapterTest.java

#### SUB-TASK 1.3: Review Authentication Source Files
**Assigned Sub-Agent:** reviewer
**Targeted Components & Technical Requirements:**
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/auth/InternalAuthService.java
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/auth/ExternalAuthAdapter.java

#### SUB-TASK 1.4: Document Authentication Flow
**Assigned Sub-Agent:** doc
**Targeted Components & Technical Requirements:**
*   **Target Path:** ./sources/backend/docs/auth-flow.md
    *   **Architectural Requirements:**
        *   Include sequence diagram for internal login, external provider redirect, JWT issuance, and tenant scoping.
        *   Note OWASP A01 (broken authentication) mitigations applied.

### DAY 2: Build Role‑Based Access Control (RBAC) Core
#### SUB-TASK 2.1: Define Role, Permission, and Authorization Service
**Assigned Sub-Agent:** coder
**Targeted Components & Technical Requirements:**
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/rbac/Role.java
    *   **Architectural Requirements:**
        *   Enum constants for SYSTEM_ADMIN, ADMIN, MANAGER, TEACHER, STUDENT.
        *   Attach tenant‑scope flag (`boolean global`) to differentiate system vs center roles.
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/rbac/Permission.java
    *   **Architectural Requirements:**
        *   Define granular permissions (e.g., COURSE_READ, COURSE_WRITE, ENROLLMENT_CREATE, ATTENDANCE_RECORD).
        *   Store permissions in a tenant‑isolated lookup table; use parameterized queries.
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/rbac/AuthorizationService.java
    *   **Architectural Requirements:**
        *   Method `hasPermission(User user, Permission perm)` enforcing tenant‑id check.
        *   Implement AES‑256 encryption for any sensitive permission metadata.
        *   Log all authorization decisions for audit (OWASP A09: security misconfiguration).

#### SUB-TASK 2.2: Write Unit Tests for RBAC Components
**Assigned Sub-Agent:** tester
**Targeted Components & Technical Requirements:**
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/rbac/Role.java;./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/rbac/RoleTest.java
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/rbac/Permission.java;./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/rbac/PermissionTest.java
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/rbac/AuthorizationService.java;./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/rbac/AuthorizationServiceTest.java

#### SUB-TASK 2.3: Review Authorization Service
**Assigned Sub-Agent:** reviewer
**Targeted Components & Technical Requirements:**
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/rbac/AuthorizationService.java

#### SUB-TASK 2.4: Document RBAC Specification
**Assigned Sub-Agent:** doc
**Targeted Components & Technical Requirements:**
*   **Target Path:** ./sources/backend/docs/rbac-spec.md
    *   **Architectural Requirements:**
        *   Detail role hierarchy, tenant scoping, and permission matrix.
        *   Highlight OWASP A08 (software integrity) controls applied.

### DAY 3: Implement Core Business Services
#### SUB-TASK 3.1: Build Core Service Layer (Course, Enrollment, Attendance, Notification)
**Assigned Sub-Agent:** coder
**Targeted Components & Technical Requirements:**
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/service/CourseService.java
    *   **Architectural Requirements:**
        *   CRUD for courses with overlap detection; enforce tenant isolation.
        *   Use parameterized queries; encrypt course pricing fields with AES‑256.
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/service/EnrollmentService.java
    *   **Architectural Requirements:**
        *   Manage student enrollment, assign points (10 per enrollment), link to teacher.
        *   Validate enrollment windows; log enrollment events for audit.
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/service/AttendanceService.java
    *   **Architectural Requirements:**
        *   Process QR scan events; maintain daily attendance flag per student.
        *   Compute and expose remaining validity days for student cards.
        *   Store attendance records with tenant scoping; use prepared statements.
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/service/NotificationService.java
    *   **Architectural Requirements:**
        *   Send notifications via Zalo group and mobile push; integrate with external notification APIs.
        *   Ensure message payloads are sanitized (OWASP A03: injection).
        *   Log all outbound notifications for traceability.

#### SUB-TASK 3.2: Write Unit Tests for Core Services
**Assigned Sub-Agent:** tester
**Targeted Components & Technical Requirements:**
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/service/CourseService.java;./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/service/CourseServiceTest.java
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/service/EnrollmentService.java;./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/service/EnrollmentServiceTest.java
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/service/AttendanceService.java;./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/service/AttendanceServiceTest.java
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/service/NotificationService.java;./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/service/NotificationServiceTest.java

#### SUB-TASK 3.3: Review CourseService Implementation
**Assigned Sub-Agent:** reviewer
**Targeted Components & Technical Requirements:**
*   **Target Path:** ./sources/backend/src/main/java/org/nlh4j/saas/membershiphub/service/CourseService.java

### DAY 4: Integration Testing & Documentation Finalization
#### SUB-TASK 4.1: Execute End‑to‑End Integration Test
**Assigned Sub-Agent:** tester
**Targeted Components & Technical Requirements:**
*   **Target Path:** INTEGRATION_SCOPE;./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/IntegrationTest.java
    *   **Architectural Requirements:**
        *   Simulate full user journey: internal sign‑up → login → course enrollment → QR attendance → notification receipt.
        *   Validate tenant isolation across all steps; assert JWT claims contain correct `tenant_id`.
        *   Verify that all service responses are sanitized (OWASP A03) and audit logs are populated.

#### SUB-TASK 4.2: Produce Core API Documentation
**Assigned Sub-Agent:** doc
**Targeted Components & Technical Requirements:**
*   **Target Path:** ./sources/backend/docs/api-core.md
    *   **Architectural Requirements:**
        *   Document REST endpoints for authentication, RBAC, courses, enrollments, attendance, notifications.
        *   Include request/response schemas, tenant‑scope notes, and security constraints (JWT, AES‑256).

#### SUB-TASK 4.3: Review Integration Test Suite
**Assigned Sub-Agent:** reviewer
**Targeted Components & Technical Requirements:**
*   **Target Path:** ./sources/backend/src/test/java/org/nlh4j/saas/membershiphub/IntegrationTest.java

--- END OF PHASE 2 ---