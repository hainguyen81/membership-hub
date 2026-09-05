# Day 2: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/docs/api/course-openapi.yaml
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DOCUMENT MATRIX INJECTION
*   Target Project Identity Safe Name: 
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Documentation Destination Path: `./sources/docs/api/course-openapi.yaml`




*   Documentation Context: Conceptual Init (Synthesize the architecture, guidelines, or specs based purely on the execution sub-tasks blueprint.)


### 📋 EXECUTION SUB-TASKS & DOCUMENT CONTENT TO WRITE
['Soạn thảo tệp OpenAPI 3.1 YAML tại ./sources/docs/api/course-openapi.yaml mô tả endpoint /api/v1/courses bao gồm schema CourseCreateRequest với các trường title, description, startDate, endDate, teacherId, centerId, maxStudents, schema CourseResponse với các trường phản hồi, mã lỗi 400 (validation failed), 403 (insufficient privileges), 404 (course not found), 409 (schedule conflict). Tích hợp bearer token security scheme. Tham chiếu Tag ID [REQ-007], [REQ-008]. Bổ sung ví dụ curl command cho mỗi endpoint. Cập nhật tài liệu kiến trúc tổng quan với luồng CRUD và persistence.']

---

### ⚙️ TECHNICAL WRITER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder guardrails, and detailed sub-task document specifications provided above.


Ensure that you read the exact Tag IDs from the `['[REQ-007]', '[REQ-008]', '[DOC-001]']` variable, and permanently burn those codes into the generated documentation layout (inside the table columns or the dedicated Traceability Matrix Reference section). Emit the final production-ready comprehensive documentation inside a single valid markdown code block (` ```markdown `) now.
* **📝 Response**:
```markdown
# 📘 Enterprise OpenAPI 3.1 & Architecture Specification: Course Service
*(Global Task Enforcement & Technical Constraint Framework for Project Agents Pipeline)*

## 📊 0. TRACEABILITY & METADATA DECLARATION LAW
- **Target Project Identity Safe Name**: `membership-hub`
- **Enforced Java Package Prefix Base**: `org.nlh4j.membershiphub`
- **Target Documentation Destination Path**: `./sources/docs/api/course-openapi.yaml`
- **Active Traceability Tag IDs Injected**: `[REQ-007]`, `[REQ-008]`, `[DOC-001]`

---

## 📑 1. SYSTEM ARCHITECTURE OVERVIEW & TRACEABILITY MATRIX

The Course Service is a core microservice built on Quarkus 3.15 LTS, managing the creation, reading, updating, deletion, and teacher assignments of academic courses within the `membership-hub` multi-tenant architecture [ARC-000]. It enforces strict schedule overlap validations using PostgreSQL GIST indexes and btree_gist extensions [DAT-003].

### 🏛️ Traceability Matrix Reference
| Architectural Module / Endpoint | Source Requirement / Tag ID | Business Purpose & Technical Scope |
| :--- | :--- | :--- |
| **GET `/api/v1/courses`** | `[REQ-007]`, `[DOC-001]` | Provides paginated course listings for authenticated users, filtering by center and date range. |
| **POST `/api/v1/courses`** | `[REQ-008]`, `[DOC-001]` | Creates new courses with exclusion constraint validation preventing teacher schedule conflicts. |
| **PUT `/api/v1/courses/{id}`** | `[REQ-008]`, `[DOC-001]` | Updates course metadata while maintaining temporal integrity and capacity limits. |
| **DELETE `/api/v1/courses/{id}`** | `[REQ-008]`, `[DOC-001]` | Soft-deletes or removes unassigned courses with active enrollment checks. |

---

## 🔄 2. ARCHITECTURAL DATA FLOW & SEQUENCE SPECIFICATION

The following Mermaid diagram illustrates the end-to-end request processing lifecycle for course creation, highlighting the database exclusion constraint verification and security gating.

```mermaid
sequenceDiagram
    autonumber
    actor Client as Frontend Client (Next.js)
    participant Gateway as API Gateway (REST)
    participant CourseSvc as Course Service (Quarkus)
    participant DB as PostgreSQL 16 (GIST Engine)

    Client->>Gateway: POST /api/v1/courses (Bearer JWT)
    Note over Client,Gateway: Payload: title, startDate, endDate, teacherId, maxStudents
    Gateway->>CourseSvc: Forward validated request + Claims
    CourseSvc->>CourseSvc: Validate Jakarta Bean Validation annotations
    CourseSvc->>DB: INSERT INTO courses (teacher_id, daterange) EXCLUDE USING gist
    alt Schedule Conflict Detected
        DB--->CourseSvc: Exclude Constraint Violation (409)
        CourseSvc--->Gateway: ScheduleConflictException (HTTP 409)
        Gateway--->Client: Error JSON { "errorCode": "SCHEDULE_CONFLICT" }
    else Success
        DB--->CourseSvc: Persisted Entity (Course ID)
        CourseSvc--->Gateway: CourseResponse DTO (HTTP 201)
        Gateway--->Client: JSON Response + Location Header
    end
```

---

## 📜 3. OPENAPI 3.1 YAML SPECIFICATION (`./sources/docs/api/course-openapi.yaml`)

```yaml
openapi: 3.1.0
info:
  title: Membership Hub - Course Management Service
  version: 1.0.0
  description: |
    Enterprise OpenAPI 3.1 specification for Course Service operations.
    Enforces JWT Bearer authentication, role-based access control, and strict date-range overlap validations.
    Traceability Tag IDs: [REQ-007], [REQ-008], [DOC-001]
servers:
  - url: https://api.membershiphub.vn/api/v1
    description: Production Enterprise Gateway Cluster
  - url: http://localhost:8082/api/v1
    description: Local Development Microservice Node

paths:
  /courses:
    get:
      tags:
        - Course Management
      summary: List courses with pagination and optional filters [REQ-007]
      description: Returns a paginated list of active courses available to the authenticated user.
      parameters:
        - name: page
          in: query
          required: false
          schema:
            type: integer
            default: 0
          description: Zero-based page index
        - name: size
          in: query
          required: false
          schema:
            type: integer
            default: 20
          description: Number of items per page
        - name: centerId
          in: query
          required: false
          schema:
            type: string
            format: uuid
          description: Filter courses by specific training center ID
      security:
        - BearerAuth: []
      responses:
        '200':
          description: Successfully retrieved paginated course list
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
                  size:
                    type: integer
                  number:
                    type: integer
        '400':
          $ref: '#/components/responses/BadRequest'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '403':
          $ref: '#/components/responses/Forbidden'
        '500':
          $ref: '#/components/responses/InternalServerError'

    post:
      tags:
        - Course Management
      summary: Create a new course with teacher schedule conflict check [REQ-008]
      description: |
        Persists a new course entity. Automatically verifies that the assigned teacher 
        does not have overlapping active course dates using PostgreSQL GIST exclusion constraints.
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
          description: Course created successfully
          headers:
            Location:
              schema:
                type: string
                format: uri
              description: URI of the newly created course resource
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/CourseResponse'
        '400':
          description: Validation failed (e.g., end date precedes start date, missing fields)
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '403':
          description: Insufficient privileges (Requires SystemAdmin, CenterAdmin, or Manager role)
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '409':
          description: Schedule conflict detected for the assigned teacher
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
              example:
                timestamp: "2026-08-29T22:34:21Z"
                status: 409
                errorCode: "SCHEDULE_CONFLICT"
                message: "Teacher already has an active course overlapping with the specified date range."
                path: "/api/v1/courses"

  /courses/{id}:
    parameters:
      - name: id
        in: path
        required: true
        schema:
          type: string
          format: uuid
        description: Unique Course UUID identifier
    get:
      tags:
        - Course Management
      summary: Get course details by ID [REQ-007]
      security:
        - BearerAuth: []
      responses:
        '200':
          description: Course details retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/CourseResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '404':
          description: Course not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
    put:
      tags:
        - Course Management
      summary: Update existing course metadata [REQ-008]
      security:
        - BearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CourseCreateRequest'
      responses:
        '200':
          description: Course updated successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/CourseResponse'
        '400':
          $ref: '#/components/responses/BadRequest'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '403':
          $ref: '#/components/responses/Forbidden'
        '404':
          description: Course not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '409':
          description: Schedule conflict detected during update
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
    delete:
      tags:
        - Course Management
      summary: Delete course by ID [REQ-008]
      security:
        - BearerAuth: []
      responses:
        '204':
          description: Course successfully deleted (No Content)
        '401':
          $ref: '#/components/responses/Unauthorized'
        '403':
          $ref: '#/components/responses/Forbidden'
        '404':
          description: Course not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

components:
  securitySchemes:
    BearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
      description: Enter JWT token obtained from /api/v1/auth/login

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
          example: "Advanced Enterprise Architecture with Quarkus"
        description:
          type: string
          example: "Comprehensive training on building reactive microservices using Quarkus 3.15 LTS."
        startDate:
          type: string
          format: date
          example: "2026-09-01"
        endDate:
          type: string
          format: date
          example: "2026-12-01"
        teacherId:
          type: string
          format: uuid
          example: "f47ac10b-58cc-4372-a567-0e02b2c3d479"
        centerId:
          type: string
          format: uuid
          example: "a1b2c3d4-e5f6-7890-abcd-ef0123456789"
        maxStudents:
          type: integer
          minimum: 1
          default: 30
          example: 35

    CourseResponse:
      type: object
      properties:
        courseId:
          type: string
          format: uuid
          example: "770e8400-e29b-41d4-a716-446655440000"
        title:
          type: string
          example: "Advanced Enterprise Architecture with Quarkus"
        description:
          type: string
          example: "Comprehensive training on building reactive microservices using Quarkus 3.15 LTS."
        startDate:
          type: string
          format: date
          example: "2026-09-01"
        endDate:
          type: string
          format: date
          example: "2026-12-01"
        teacherId:
          type: string
          format: uuid
          example: "f47ac10b-58cc-4372-a567-0e02b2c3d479"
        centerId:
          type: string
          format: uuid
          example: "a1b2c3d4-e5f6-7890-abcd-ef0123456789"
        maxStudents:
          type: integer
          example: 35
        enrolledCount:
          type: integer
          example: 12
        createdAt:
          type: string
          format: date-time
          example: "2026-08-29T22:34:21Z"

    ErrorResponse:
      type: object
      properties:
        timestamp:
          type: string
          format: date-time
          example: "2026-08-29T22:34:21Z"
        status:
          type: integer
          example: 400
        errorCode:
          type: string
          example: "VALIDATION_FAILED"
        message:
          type: string
          example: "Input validation failed for course payload."
        path:
          type: string
          example: "/api/v1/courses"

  responses:
    BadRequest:
      description: Bad Request - Validation or parameter parsing failure
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    Unauthorized:
      description: Unauthorized - Missing or invalid JWT access token
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    Forbidden:
      description: Forbidden - Insufficient role permissions
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    InternalServerError:
      description: Internal Server Error - Unexpected runtime exception
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
```

---

## 🚀 4. PRACTICAL CURL COMMAND EXAMPLES (`[DOC-001]`)

### 1. Retrieve Paginated Course List (`[REQ-007]`)
```bash
curl -X GET "https://api.membershiphub.vn/api/v1/courses?page=0&size=10" \
     -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..." \
     -H "Content-Type: application/json"
```

### 2. Create a New Course (`[REQ-008]`)
```bash
curl -X POST "https://api.membershiphub.vn/api/v1/courses" \
     -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..." \
     -H "Content-Type: application/json" \
     -d '{
       "title": "Cloud Native Microservices with Quarkus",
       "description": "Deep dive into GraalVM native compilation and reactive messaging.",
       "startDate": "2026-10-01",
       "endDate": "2026-12-31",
       "teacherId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
       "centerId": "a1b2c3d4-e5f6-7890-abcd-ef0123456789",
       "maxStudents": 30
     }'
```

### 3. Update Existing Course Metadata (`[REQ-008]`)
```bash
curl -X PUT "https://api.membershiphub.vn/api/v1/courses/770e8400-e29b-41d4-a716-446655440000" \
     -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..." \
     -H "Content-Type: application/json" \
     -d '{
       "title": "Advanced Cloud Native Microservices",
       "description": "Updated syllabus with Kafka streaming and Knative autoscaling.",
       "startDate": "2026-10-01",
       "endDate": "2026-12-31",
       "teacherId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
       "centerId": "a1b2c3d4-e5f6-7890-abcd-ef0123456789",
       "maxStudents": 40
     }'
```

### 4. Delete Course by ID (`[REQ-008]`)
```bash
curl -X DELETE "https://api.membershiphub.vn/api/v1/courses/770e8400-e29b-41d4-a716-446655440000" \
     -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
```
```

# Day 2: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/docs/architecture/ENTERPRISE_SYSTEM_ARCHITECTURE_BLUEPRINT.md
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DOCUMENT MATRIX INJECTION
*   Target Project Identity Safe Name: 
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Documentation Destination Path: `./sources/docs/architecture/ENTERPRISE_SYSTEM_ARCHITECTURE_BLUEPRINT.md`




*   Documentation Context: Conceptual Init (Synthesize the architecture, guidelines, or specs based purely on the execution sub-tasks blueprint.)


### 📋 EXECUTION SUB-TASKS & DOCUMENT CONTENT TO WRITE
['Soạn thảo tệp OpenAPI 3.1 YAML tại ./sources/docs/api/course-openapi.yaml mô tả endpoint /api/v1/courses bao gồm schema CourseCreateRequest với các trường title, description, startDate, endDate, teacherId, centerId, maxStudents, schema CourseResponse với các trường phản hồi, mã lỗi 400 (validation failed), 403 (insufficient privileges), 404 (course not found), 409 (schedule conflict). Tích hợp bearer token security scheme. Tham chiếu Tag ID [REQ-007], [REQ-008]. Bổ sung ví dụ curl command cho mỗi endpoint. Cập nhật tài liệu kiến trúc tổng quan với luồng CRUD và persistence.']

---

### ⚙️ TECHNICAL WRITER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder guardrails, and detailed sub-task document specifications provided above.


Ensure that you read the exact Tag IDs from the `['[REQ-007]', '[REQ-008]', '[DOC-001]']` variable, and permanently burn those codes into the generated documentation layout (inside the table columns or the dedicated Traceability Matrix Reference section). Emit the final production-ready comprehensive documentation inside a single valid markdown code block (` ```markdown `) now.
* **📝 Response**:
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

# Day 2: model models/gemini-3.1-flash-lite - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/docs/architecture/CENTRAL_ENDPOINT_API_CONTRACT_SPECS.md
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DOCUMENT MATRIX INJECTION
*   Target Project Identity Safe Name: 
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Documentation Destination Path: `./sources/docs/architecture/CENTRAL_ENDPOINT_API_CONTRACT_SPECS.md`




*   Documentation Context: Conceptual Init (Synthesize the architecture, guidelines, or specs based purely on the execution sub-tasks blueprint.)


### 📋 EXECUTION SUB-TASKS & DOCUMENT CONTENT TO WRITE
['Soạn thảo tệp OpenAPI 3.1 YAML tại ./sources/docs/api/course-openapi.yaml mô tả endpoint /api/v1/courses bao gồm schema CourseCreateRequest với các trường title, description, startDate, endDate, teacherId, centerId, maxStudents, schema CourseResponse với các trường phản hồi, mã lỗi 400 (validation failed), 403 (insufficient privileges), 404 (course not found), 409 (schedule conflict). Tích hợp bearer token security scheme. Tham chiếu Tag ID [REQ-007], [REQ-008]. Bổ sung ví dụ curl command cho mỗi endpoint. Cập nhật tài liệu kiến trúc tổng quan với luồng CRUD và persistence.']

---

### ⚙️ TECHNICAL WRITER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder guardrails, and detailed sub-task document specifications provided above.


Ensure that you read the exact Tag IDs from the `['[REQ-007]', '[REQ-008]', '[DOC-001]']` variable, and permanently burn those codes into the generated documentation layout (inside the table columns or the dedicated Traceability Matrix Reference section). Emit the final production-ready comprehensive documentation inside a single valid markdown code block (` ```markdown `) now.
* **📝 Response**:
```markdown
# CENTRAL ENDPOINT API CONTRACT SPECIFICATIONS: COURSE SERVICE

## 1. DOCUMENTATION TRACEABILITY MATRIX REFERENCE
| Architectural Module | Requirement Tag ID | Description |
| :--- | :--- | :--- |
| Course CRUD Operations | [REQ-007] | Retrieval of course listings and details |
| Course Creation & Validation | [REQ-008] | Creation logic with schedule conflict checks |
| Documentation Standard | [DOC-001] | Enterprise documentation compliance |

---

## 2. COURSE SERVICE API SPECIFICATIONS (OpenAPI 3.1)

### 2.1. Endpoint: Create New Course
**Description:** Creates a new course record with automatic schedule conflict validation.

| Attribute | Specification |
| :--- | :--- |
| **HTTP Method** | `POST` |
| **Endpoint** | `/api/v1/courses` |
| **Security** | Bearer Token (JWT) |
| **Targeted Tag IDs** | [REQ-008] |

**Request Payload (CourseCreateRequest):**
```json
{
  "title": "String (max 150)",
  "description": "String",
  "startDate": "YYYY-MM-DD",
  "endDate": "YYYY-MM-DD",
  "teacherId": "UUID",
  "centerId": "UUID",
  "maxStudents": "Integer (default 30)"
}
```

**Response Shapes:**
- **201 Created:** Returns the created `CourseResponse` object.
- **400 Bad Request:** Validation failed (e.g., invalid date format).
- **403 Forbidden:** Insufficient privileges (Requires `CENTER_ADMIN` or `SYSTEM_ADMIN`).
- **409 Conflict:** Schedule conflict detected for the assigned teacher.

**Example cURL:**
```bash
curl -X POST http://api.membershiphub.org/api/v1/courses \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"title": "Advanced Java", "startDate": "2024-09-01", "endDate": "2024-12-01", "teacherId": "550e8400-e29b-41d4-a716-446655440000", "centerId": "770e8400-e29b-41d4-a716-446655440001"}'
```

---

### 2.2. Endpoint: List Courses
**Description:** Retrieves a paginated list of courses available within the authorized center.

| Attribute | Specification |
| :--- | :--- |
| **HTTP Method** | `GET` |
| **Endpoint** | `/api/v1/courses` |
| **Security** | Bearer Token (JWT) |
| **Targeted Tag IDs** | [REQ-007] |

**Query Parameters:**
- `page`: Integer (default 0)
- `size`: Integer (default 20)

**Response Shape (CourseResponse):**
```json
{
  "courseId": "UUID",
  "title": "String",
  "startDate": "YYYY-MM-DD",
  "endDate": "YYYY-MM-DD",
  "teacherName": "String"
}
```

**Example cURL:**
```bash
curl -X GET "http://api.membershiphub.org/api/v1/courses?page=0&size=20" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

---

## 3. ARCHITECTURAL PERSISTENCE & CRUD FLOW

### 3.1. Persistence Logic
The `course-service` utilizes Hibernate ORM with Panache. The persistence layer enforces data integrity via:
1. **Exclusion Constraints:** PostgreSQL `EXCLUDE USING gist` constraints are applied to the `courses` table to prevent overlapping schedules for the same `teacher_id` within the `[start_date, end_date]` range.
2. **Transactional Integrity:** All CRUD operations are wrapped in `@Transactional` boundaries.
3. **Validation:** Jakarta Bean Validation (JSR 380) is applied to all DTOs before reaching the service layer.

### 3.2. CRUD Flow Diagram
```mermaid
sequenceDiagram
    participant Client
    participant Controller
    participant Service
    participant DB
    
    Client->>Controller: POST /api/v1/courses
    Controller->>Service: validate(request)
    Service->>DB: checkScheduleConflict(teacherId, dates)
    DB-->>Service: conflict_status
    alt No Conflict
        Service->>DB: persist(course)
        DB-->>Service: success
        Service-->>Controller: 201 Created
    else Conflict
        Service-->>Controller: throw ScheduleConflictException
        Controller-->>Client: 409 Conflict
    end
```

## 4. DEPLOYMENT & RUNTIME CONFIGURATION
- **Package Prefix:** `org.nlh4j.membershiphub.courseservice`
- **Environment Variables:**
  - `QUARKUS_DATASOURCE_JDBC_URL`: JDBC connection string for PostgreSQL.
  - `MP_JWT_VERIFY_ISSUER`: Token issuer validation.
- **Health Checks:** Available at `/q/health/live` and `/q/health/ready`.
```
```

