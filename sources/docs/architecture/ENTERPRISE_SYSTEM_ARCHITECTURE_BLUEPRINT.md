```markdown
# [DOC-001] ENTERPRISE SYSTEM ARCHITECTURE BLUEPRINT & API SPECIFICATION
**Project Identity:** membership-hub  
**Base Package Namespace:** `org.nlh4j.membershiphub`  
**Target Document Destination:** `./sources/docs/architecture/ENTERPRISE_SYSTEM_ARCHITECTURE_BLUEPRINT.md`  
**Associated Traceability Tags:** `[REQ-007]`, `[REQ-008]`, `[DOC-001]`, `[ARC-000]`, `[NFR-001]`

---

## 1. EXECUTIVE SYSTEM ARCHITECTURE OVERVIEW

The **membership-hub** platform is engineered as a high-throughput, multi-tenant enterprise microservices ecosystem designed to support real-time user management, center administration, course scheduling, enrollment tracking, and QR-based attendance scanning [ARC-000]. The architecture strictly separates business domains into independent deployable microservices (`user-service`, `center-service`, `course-service`, `attendance-service`) communicating via RESTful APIs and asynchronous Apache Kafka event streams.

### 1.1 Architectural Principles & Stack Matrix
- **Runtime Core:** Quarkus 3.15.1 LTS optimized for GraalVM native image execution, delivering sub-millisecond startup times and low memory footprints [NFR-005].
- **Persistence Layer:** PostgreSQL 16 relational database with Flyway-managed schema migrations, strict Foreign Key referential integrity, composite unique indexing, and GiST exclusion constraints for schedule conflict prevention [DAT-001]–[DAT-012].
- **Security & Authorization:** OAuth2 Resource Server enforcing JSON Web Tokens (JWT) with 15-minute access token expiry, 7-day refresh token rotation, and role-based access control (RBAC) across 5 distinct security tiers [ARC-006], [NFR-003].
- **Event-Driven Architecture (EDA):** Apache Kafka event broker handling outbound notifications, enrollment updates, and teacher assignments via an Outbox pattern [ARC-008].

```mermaid
graph TB
    subgraph Client Tier ["Client Tier (Next.js 14 / Mobile App)"]
        Web[Next.js Web Portal]
        Mobile[React Native App]
    end

    subgraph API Gateway Layer ["API Gateway / Ingress Layer"]
        Gateway[NGINX Ingress & JWT Auth Filter]
    end

    subgraph Microservices Backend ["Enterprise Microservices (Quarkus 3.15 LTS)"]
        UserService["user-service<br/><i>[REQ-001]–[REQ-003]</i>"]
        CenterService["center-service<br/><i>[REQ-004]–[REQ-006]</i>"]
        CourseService["course-service<br/><i>[REQ-007]–[REQ-011]</i>"]
        AttendanceService["attendance-service<br/><i>[REQ-012]–[REQ-016]</i>"]
    end

    subgraph Event Broker ["Event Streaming Layer"]
        Kafka[(Apache Kafka Cluster<br/><i>[ARC-008]</i>)]
    end

    subgraph Data Tier ["Persistence Tier (PostgreSQL 16)"]
        DB[(PostgreSQL Primary & Read Replica<br/><i>[DAT-ALL]</i>)]
    end

    Web --> Gateway
    Mobile --> Gateway
    
    Gateway --> UserService
    Gateway --> CenterService
    Gateway --> CourseService
    Gateway --> AttendanceService

    UserService --> DB
    CenterService --> DB
    CourseService --> DB
    AttendanceService --> DB

    CourseService -.->|Kafka Events| Kafka
    AttendanceService -.->|Kafka Events| Kafka
    Kafka -.-> NotificationWorker[notification-service]
```

---

## 2. TRACEABILITY MATRIX REFERENCE

The following matrix establishes strict mapping between architectural components, database entities, API endpoints, and the governing system requirement tokens.

| Requirement ID | Architectural Component | Module Path | Database / Storage Target | Description / Compliance Scope |
| :--- | :--- | :--- | :--- | :--- |
| **[REQ-007]** | Course Browsing Controller | `./sources/backend/course-service/` | `courses` table | Secure REST endpoint returning paginated course listings for verified users. |
| **[REQ-008]** | Course Management & Overlap Check | `./sources/backend/course-service/` | `courses` table (GiST Index) | CRUD operations with database-level exclusion constraints preventing teacher schedule overlaps. |
| **[REQ-009]** | Teacher Assignment Service | `./sources/backend/course-service/` | `course_teacher_mapping` | Assign/unassign teachers to courses with Kafka event emission. |
| **[REQ-010]** | Student Course Discovery | `./sources/backend/course-service/` | `courses`, `enrollments` | Filter available courses excluding existing student enrollments. |
| **[REQ-011]** | Student Enrollment Engine | `./sources/backend/course-service/` | `enrollments` table | Register students, auto-create accounts if missing, publish Kafka events. |
| **[ARC-000]** | Multi-Module Maven Root | `./sources/backend/pom.xml` | N/A | Parent build descriptor managing 4 Quarkus microservices. |
| **[ARC-006]** | OAuth2 / JWT Security | `./sources/backend/user-service/` | Redis (Blacklist) | Token generation, RS256 validation, and role augmentors. |
| **[NFR-001]** | High Performance API Gateway | `./sources/infra/k8s/` | PostgreSQL Indexing | P95 latency < 200ms under 10,000 concurrent users. |
| **[DOC-001]** | Enterprise System Documentation | `./sources/docs/` | Documentation Repo | Comprehensive technical specifications, C4 models, and OpenAPI definitions. |

---

## 3. COURSE MANAGEMENT ARCHITECTURE & PERSISTENCE

The `course-service` microservice manages the lifecycle of academic courses, instructor allocations, and student enrollment eligibility [REQ-007]–[REQ-011]. 

### 3.1 Database Persistence & Overlap Prevention
To prevent scheduling conflicts at the database layer without relying solely on application-level locks, `course-service` implements PostgreSQL GiST indexing combined with exclusion constraints [DAT-003]:

```sql
-- [REQ-008] [DAT-003] Database Exclusion Constraint for Teacher Schedule Overlap
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE courses
    ADD CONSTRAINT ex_teacher_schedule_no_overlap
    EXCLUDE USING gist (
        teacher_id WITH =,
        daterange(start_date, end_date, '[]') &&
    )
    WHERE (teacher_id IS NOT NULL);
```

### 3.2 CRUD & Persistence Sequence Flow

```mermaid
sequenceDiagram
    autonumber
    participant Client as Next.js Frontend
    participant Gateway as API Gateway
    participant CourseSvc as course-service (Quarkus)
    participant DB as PostgreSQL Database
    participant Kafka as Apache Kafka Broker

    Client->>Gateway: POST /api/v1/courses (Bearer Token)
    Gateway->>CourseSvc: Forward validated request [REQ-008]
    CourseSvc->>CourseSvc: Validate Bean constraints (CourseCreateRequest)
    CourseSvc->>DB: INSERT INTO courses (...) [DAT-003]
    alt Schedule Conflict Detected
        DB-->>CourseSvc: Exclude violation (ex_teacher_schedule_no_overlap)
        CourseSvc-->>Client: HTTP 409 Conflict (SCHEDULE_CONFLICT)
    else Success
        DB-->>CourseSvc: Persisted Course entity
        CourseSvc->>Kafka: Publish course-created event [ARC-008]
        CourseSvc-->>Client: HTTP 201 Created (CourseResponse)
    end
```

---

## 4. OPENAPI 3.1 SPECIFICATION: COURSE SERVICE

The complete OpenAPI 3.1 contract for the Course Service API is formalized below, documenting endpoints, payloads, and error mappings.

```yaml
openapi: 3.1.0
info:
  title: Membership Hub - Course Service API
  version: 1.0.0
  description: REST API contracts for course browsing, creation, updates, and instructor assignments. Governed by [REQ-007], [REQ-008], [REQ-009], [REQ-010], [REQ-011].
servers:
  - url: https://api.membershiphub.vn/api/v1
    description: Production Environment Gateway
components:
  securitySchemes:
    BearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
  schemas:
    CourseCreateRequest:
      type: object
      required:
        - title
        - startDate
        - endDate
        - teacherId
        - centerId
      properties:
        title:
          type: string
          maxLength: 150
          example: "Advanced Enterprise Java Development"
        description:
          type: string
          example: "Comprehensive Quarkus 3.15 and Hibernate Panache course."
        startDate:
          type: string
          format: date
          example: "2024-09-01"
        endDate:
          type: string
          format: date
          example: "2024-12-01"
        teacherId:
          type: string
          format: uuid
          example: "550e8400-e29b-41d4-a716-446655440000"
        centerId:
          type: string
          format: uuid
          example: "770e8400-e29b-41d4-a716-446655440000"
        maxStudents:
          type: integer
          minimum: 1
          default: 30
          example: 30
    CourseResponse:
      type: object
      properties:
        courseId:
          type: string
          format: uuid
        title:
          type: string
        description:
          type: string
        startDate:
          type: string
          format: date
        endDate:
          type: string
          format: date
        teacherId:
          type: string
          format: uuid
        centerId:
          type: string
          format: uuid
        maxStudents:
          type: integer
        createdAt:
          type: string
          format: date-time
    ErrorResponse:
      type: object
      properties:
        timestamp:
          type: string
          format: date-time
        status:
          type: integer
        errorCode:
          type: string
        message:
          type: string
        path:
          type: string
paths:
  /courses:
    get:
      summary: List all courses with pagination [REQ-007]
      security:
        - BearerAuth: []
      parameters:
        - name: page
          in: query
          schema:
            type: integer
            default: 0
        - name: size
          in: query
          schema:
            type: integer
            default: 20
      responses:
        '200':
          description: Paginated list of courses retrieved successfully.
          content:
            application/json:
              schema:
                type: object
                properties:
                  content:
                    type: array
                    items:
                      $ref: '#/components/schemas/CourseResponse'
                  totalElements:
                    type: integer
                  totalPages:
                    type: integer
        '401':
          description: Unauthorized - Bearer token missing or expired.
        '403':
          description: Forbidden - Insufficient role privileges [REQ-003].
    post:
      summary: Create a new course with teacher overlap validation [REQ-008]
      security:
        - BearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CourseCreateRequest'
      responses:
        '201':
          description: Course successfully created.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/CourseResponse'
        '400':
          description: Validation failed (e.g. endDate < startDate, missing fields).
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '403':
          description: Insufficient privileges (Center Admin or System Admin required).
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '409':
          description: Schedule conflict detected (Teacher already booked during date range).
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
```

---

## 5. EXAMPLE CURL COMMANDS & USAGE

### 5.1 Retrieve Paginated Courses (`[REQ-007]`)
```bash
curl -X GET "https://api.membershiphub.vn/api/v1/courses?page=0&size=10" \
     -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIs..." \
     -H "Content-Type: application/json"
```

### 5.2 Create Course with Overlap Check (`[REQ-008]`)
```bash
curl -X POST "https://api.membershiphub.vn/api/v1/courses" \
     -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIs..." \
     -H "Content-Type: application/json" \
     -d '{
       "title": "Advanced Enterprise Java Development",
       "description": "Quarkus 3.15 LTS deep dive",
       "startDate": "2024-09-01",
       "endDate": "2024-12-01",
       "teacherId": "550e8400-e29b-41d4-a716-446655440000",
       "centerId": "770e8400-e29b-41d4-a716-446655440000",
       "maxStudents": 30
     }'
```

---
*End of Enterprise System Architecture Blueprint & API Specification — membership-hub [DOC-001].*
```