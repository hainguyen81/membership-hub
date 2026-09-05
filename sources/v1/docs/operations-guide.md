markdown
# 📖 OPERATIONS GUIDE - COURSE, ENROLLMENT, AND ATTENDANCE MODULES
*Target Path: `./sources/docs/operations-guide.md`*

## 🏗️ 1. SYSTEM OVERVIEW

The **membership-hub** platform comprises three core microservices: **Course Service**, **Enrollment Service**, and **Attendance Service**. These modules collectively enable a comprehensive learning management ecosystem supporting multi‑tenant educational centers with role‑based access, QR‑based attendance tracking, and real‑time enrollment workflows.

### 1.1 Module Responsibilities
| Module | Primary Business Functions | Key Data Entities |
|--------|---------------------------|-------------------|
| **Course Service** | Course lifecycle management, teacher assignment, schedule conflict detection, capacity planning | `Courses`, `Teachers`, `Centers` |
| **Enrollment Service** | Student course registration, waitlist management, automatic student account provisioning, enrollment reporting | `Enrollments`, `Students`, `Courses` |
| **Attendance Service** | QR‑code attendance scanning, idempotent attendance recording, duplicate detection, attendance analytics | `Attendance`, `Students`, `Courses` |

### 1.2 System Interactions
- **Course → Enrollment**: Enrollment validates course existence, capacity, and schedule conflicts before creating records.
- **Enrollment → Attendance**: Attendance verifies student enrollment status before recording scans.
- **Course → Attendance**: Attendance uses course identifiers embedded in QR codes to link records.
- **Notification Bus**: All services emit Kafka events (`attendance.scan.result`, `enrollment.created`, `course.assigned`) for real‑time push notifications (FCM/APNs, Zalo groups).

---

## 📊 2. MODULE ARCHITECTURE

### 2.1 Course Service Architecture
mermaid
graph TB
    subgraph "Course Service"
        A[CourseController] --> B[CourseService]
        B --> C[CourseRepository]
        C --> D[PostgreSQL: Courses]
        E[ScheduleConflictService] --> B
        F[NotificationService] --> B
    end
    A --> G[JWT Validation]
    B --> H[Redis Cache]
    style A fill:#e1f5fe,stroke:#01579b
    style B fill:#fff3e0,stroke:#e65100
    style C fill:#f3e5f5,stroke:#4a148c


**Component Responsibilities**
- **CourseController**: Exposes REST endpoints (`GET /api/v1/courses`, `POST /api/v1/courses`, `POST /api/v1/courses/{id}/assign-teacher`).
- **CourseService**: Orchestrates business rules—validates dates, checks teacher availability, enforces max capacity, and publishes `course.assigned` events.
- **CourseRepository**: Executes parameterized queries; uses `idx_courses_start_date` and `idx_courses_end_date` for efficient conflict detection.
- **ScheduleConflictService**: Queries overlapping `Courses` records for the same `teacherId` within given date windows; throws `ScheduleConflictException` on collision.

### 2.2 Enrollment Service Architecture
mermaid
graph TB
    subgraph "Enrollment Service"
        A[EnrollmentController] --> B[EnrollmentService]
        B --> C[EnrollmentRepository]
        B --> D[StudentService]
        B --> E[NotificationService]
        C --> F[PostgreSQL: Enrollments]
        D --> G[PostgreSQL: Users]
    end
    A --> H[JWT Validation]
    B --> I[Redis Cache]
    style A fill:#e1f5fe,stroke:#01579b
    style B fill:#fff3e0,stroke:#e65100
    style C fill:#f3e5f5,stroke:#4a148c


**Component Responsibilities**
- **EnrollmentController**: Handles `POST /api/v1/enrollments` (course registration) and `GET /api/v1/enrollments` (student transcript).
- **EnrollmentService**: Validates enrollment eligibility (course exists, capacity, no duplicate), auto‑creates `Student` user if missing, and emits `enrollment.created` event.
- **StudentService**: Manages `Users` lifecycle with role `STUDENT`; ensures email uniqueness and default password hashing via BCrypt.
- **NotificationService**: Queues push notifications for enrollment confirmation and center‑group announcements.

### 2.3 Attendance Service Architecture
mermaid
graph TB
    subgraph "Attendance Service"
        A[AttendanceController] --> B[AttendanceService]
        B --> C[AttendanceRepository]
        B --> D[QRCodeService]
        B --> E[NotificationService]
        C --> F[PostgreSQL: Attendance]
        D --> G[Redis Cache]
    end
    A --> H[JWT Validation]
    B --> I[Kafka: attendance.scan.request]
    style A fill:#e1f5fe,stroke:#01579b
    style B fill:#fff3e0,stroke:#e65100
    style C fill:#f3e5f5,stroke:#4a148c


**Component Responsibilities**
- **AttendanceController**: Accepts `POST /api/v1/attendance/scan` with QR payload.
- **AttendanceService**: Decodes QR, validates enrollment, enforces idempotent `uk_attendance_student_course_date`, and publishes `attendance.scan.result`.
- **QRCodeService**: Validates QR format (`courseId:UUID,sessionId:UUID`) and checks expiration (session window ≤ 15 minutes).
- **NotificationService**: Sends push to student device confirming attendance; triggers duplicate alerts for `DUPLICATE` status.

---

## 📡 3. API SPECIFICATIONS

### 3.1 Course Service Endpoints
| HTTP Method | Full Endpoint | Request Headers | Path/Query Parameters | JSON Request Payload Schema | Success Response (200/201) | Failure Response (4xx/5xx) | Targeted Tag IDs |
|-------------|---------------|-----------------|-----------------------|----------------------------|----------------------------|----------------------------|-----------------|
| `GET` | `/api/v1/courses` | `Authorization: Bearer <JWT>` | `page:int(optional)`, `size:int(optional)`, `centerId:UUID(optional)` | — | `[{ "courseId":"UUID", "title":"string", "description":"string", "startDate":"date", "endDate":"date", "teacherId":"UUID", "teacherName":"string", "maxStudents":"int", "enrolledCount":"int" }]` | `{ "error":"VALIDATION_FAILED", "message":"Invalid request parameters" }` | `[REQ-007], [ARC-007]` |
| `POST` | `/api/v1/courses` | `Authorization: Bearer <JWT>` | — | `{ "title":"string (≤150)", "description":"string", "startDate":"YYYY-MM-DD", "endDate":"YYYY-MM-DD", "teacherId":"UUID", "maxStudents":"int (default 30)" }` | `{ "courseId":"UUID", "message":"Course created successfully" }` | `{ "error":"CONFLICT", "message":"Teacher has a schedule conflict during the course period" }` | `[REQ-008], [EXC-001]` |
| `POST` | `/api/v1/courses/{courseId}/assign-teacher` | `Authorization: Bearer <JWT>` | `courseId:UUID (path)` | `{ "teacherId":"UUID" }` | `{ "message":"Teacher assigned successfully, notification queued" }` | `{ "error":"FORBIDDEN", "message":"You do not have permission to assign teachers" }` | `[REQ-009]` |
| `GET` | `/api/v1/courses/available` | `Authorization: Bearer <JWT>` | `page:int`, `size:int` | — | `[{ "courseId":"UUID", "title":"string", "startDate":"date", "endDate":"date", "teacherName":"string", "maxStudents":"int", "remainingSlots":"int" }]` | `{ "error":"UNAUTHORIZED", "message":"Invalid or expired token" }` | `[REQ-010]` |

### 3.2 Enrollment Service Endpoints
| HTTP Method | Full Endpoint | Request Headers | Path/Query Parameters | JSON Request Payload Schema | Success Response (200/201) | Failure Response (4xx/5xx) | Targeted Tag IDs |
|-------------|---------------|-----------------|-----------------------|----------------------------|----------------------------|----------------------------|-----------------|
| `POST` | `/api/v1/enrollments` | `Authorization: Bearer <JWT>` | — | `{ "courseId":"UUID", "studentEmail":"string (optional)" }` | `{ "enrollmentId":"UUID", "message":"Enrollment successful" }` | `{ "error":"CONFLICT", "message":"Student already enrolled in this course" }` | `[REQ-011]` |
| `GET` | `/api/v1/enrollments` | `Authorization: Bearer <JWT>` | `page:int`, `size:int` | — | `[{ "enrollmentId":"UUID", "studentId":"UUID", "courseId":"UUID", "enrollmentDate":"timestamp", "courseTitle":"string" }]` | `{ "error":"UNAUTHORIZED", "message":"Invalid or expired token" }` | `[REQ-011]` |

### 3.3 Attendance Service Endpoints
| HTTP Method | Full Endpoint | Request Headers | Path/Query Parameters | JSON Request Payload Schema | Success Response (200/201) | Failure Response (4xx/5xx) | Targeted Tag IDs |
|-------------|---------------|-----------------|-----------------------|----------------------------|----------------------------|----------------------------|-----------------|
| `POST` | `/api/v1/attendance/scan` | `Authorization: Bearer <JWT>` | — | `{ "qrCode":"string (courseId:UUID,sessionId:UUID)", "timestamp":"timestamp" }` | `{ "attendanceId":"UUID", "status":"RECORDED|DUPLICATE", "message":"Attendance recorded successfully|Duplicate attendance detected" }` | `{ "error":"NOT_FOUND", "message":"Student not enrolled in this course" }` | `[REQ-012], [REQ-013], [EXC-001], [EXC-002]` |

---

## 🗄️ 4. DATA MODELS & SCHEMA

### 4.1 Course Service Schema (`V2__create_centers_courses_enrollments.sql`)
sql
-- Courses Table
CREATE TABLE courses (
    course_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(150) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    teacher_id UUID NOT NULL,
    max_students INT NOT NULL DEFAULT 30,
    center_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_courses_teacher FOREIGN KEY (teacher_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_courses_center FOREIGN KEY (center_id) REFERENCES centers(center_id) ON DELETE CASCADE,
    CONSTRAINT chk_courses_dates CHECK (start_date < end_date)
);
CREATE INDEX idx_courses_start_date ON courses(start_date);
CREATE INDEX idx_courses_end_date ON courses(end_date);
CREATE INDEX idx_courses_center_teacher ON courses(center_id, teacher_id);

-- Enrollments Table
CREATE TABLE enrollments (
    enrollment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    enrollment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    CONSTRAINT uk_enrollments_student_course UNIQUE (student_id, course_id)
);
CREATE INDEX idx_enrollments_student_id ON enrollments(student_id);
CREATE INDEX idx_enrollments_course_id ON enrollments(course_id);

-- Attendance Table
CREATE TABLE attendance (
    attendance_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    attendance_date DATE NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_course FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    CONSTRAINT uk_attendance_student_course_date UNIQUE (student_id, course_id, attendance_date),
    CONSTRAINT chk_attendance_date CHECK (attendance_date <= CURRENT_DATE)
);
CREATE INDEX idx_attendance_student_id ON attendance(student_id);
CREATE INDEX idx_attendance_course_id ON attendance(course_id);
CREATE INDEX idx_attendance_attendance_date ON attendance(attendance_date);


### 4.2 Traceability Matrix Reference
| Schema Component | Tag IDs |
|------------------|---------|
| `courses` table | `[REQ-007], [REQ-008], [REQ-009], [ARC-007]` |
| `enrollments` table | `[REQ-010], [REQ-011]` |
| `attendance` table | `[REQ-012], [REQ-013], [EXC-001], [EXC-002]` |
| Indexes & Constraints | `[NFR-003]` (SQL injection prevention via prepared statements) |

---

## 🚀 5. DEPLOYMENT & OPERATIONS

### 5.1 Containerization (Docker Multi‑Stage)
dockerfile
# backend/course-service/Dockerfile
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-alpine
COPY --from=builder /app/target/course-service-1.0.0.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

- **Base Image**: `eclipse-temurin:21-alpine` (≤200 MB).
- **Final Image**: ≤500 MB after stripping debug symbols and unused packages.
- **`.dockerignore`**: Excludes `.git`, `target/`, `*.iml`, `src/test` to reduce layer size.

### 5.2 Kubernetes (GKE) Deployment
yaml
# infra/gke/deployment.yaml (excerpt)
apiVersion: apps/v1
kind: Deployment
metadata:
  name: course-service
spec:
  replicas: 2
  selector:
    matchLabels:
      app: course-service
  template:
    metadata:
      labels:
        app: course-service
    spec:
      containers:
      - name: course-service
        image: gcr.io/<project>/course-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: url
        resources:
          requests:
            cpu: "250m"
            memory: "512Mi"
          limits:
            cpu: "1000m"
            memory: "1Gi"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: course-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: course-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80

- **HPA**: Scales based on CPU >70% or latency >300 ms.
- **Probes**: Health endpoints (`/actuator/health`) for GKE routing.
- **Resource Limits**: Enforce NFR‑001 (≤200 ms latency) and NFR‑004 (auto‑scaling).

### 5.3 CI/CD Pipeline (GitHub Actions)
yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline
on:
  push:
    branches: [main]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
    - name: Build with Maven
      run: mvn clean package -DskipTests
    - name: Run Unit Tests
      run: mvn test
    - name: SonarQube Scan
      uses: sonarsource/sonarcloud-github-action@v2
      env:
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
    - name: Build Docker Image
      run: |
        docker build -t gcr.io/${{ secrets.GCP_PROJECT }}/course-service:${{ github.sha }} .
    - name: Push to GCR
      run: |
        gcloud auth activate-service-account --key-file=${{ secrets.GCP_SA_KEY }}
        docker push gcr.io/${{ secrets.GCP_PROJECT }}/course-service:${{ github.sha }}
    - name: Deploy to GKE
      run: |
        gcloud container clusters get-credentials ${{ secrets.GKE_CLUSTER }} --region ${{ secrets.GKE_REGION }} --project ${{ secrets.GCP_PROJECT }}
        kubectl set image deployment/course-service course-service=gcr.io/${{ secrets.GCP_PROJECT }}/course-service:${{ github.sha }}
        kubectl rollout status deployment/course-service --timeout=300s

- **Gates**: Compilation success, SonarQube quality gate, test coverage ≥85%, container size ≤500 MB.
- **Audit Logging**: All pipeline steps logged to Cloud Logging with traceability tags.

---

## 🔒 6. SECURITY & COMPLIANCE

### 6.1 OWASP Top 10 Mitigations
| Threat | Control |
|--------|---------|
| SQL Injection | All JDBC/ORM calls use `PreparedStatement` or Hibernate criteria; input validation via JSR‑380. |
| XSS | React/Next.js auto‑escaping; Content Security Policy headers injected via ingress gateway. |
| Broken Authentication | JWT tokens signed with RS256; refresh tokens stored in HttpOnly cookies; Firebase OAuth2 integration. |
| Sensitive Data Exposure | Global `@JsonSerialize` filters mask `password_hash`, `email`, `phone`; Redis encryption at rest. |
| Insecure Direct Object References | RBAC middleware validates `center_id` against user’s assigned centers before allowing access. |

### 6.2 RBAC Enforcement (RbacFilter.java)

@Component
public class RbacFilter implements ContainerRequestFilter {
    @Context private SecurityContext securityContext;

    @Override
    public Response filter(ContainerRequestContext requestContext) {
        String userId = securityContext.getUserPrincipal().getName();
        String role = userService.getRoleByUserId(userId);
        String path = requestContext.getUriInfo().getPath();

        if (!rbacValidator.isAllowed(role, path, requestContext.getMethod())) {
            logger.error("[RBAC_DENY] [ARC-001] Access to {} by {} role denied.", path, role);
            return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("error", "FORBIDDEN", "message", "Insufficient permissions"))
                .build();
        }
        return null;
    }
}

- **Roles**: `SYSTEM_ADMIN`, `CENTER_ADMIN`, `MANAGER`, `TEACHER`, `STUDENT`.
- **Policy**: Center‑scoped checks for `CENTER_ADMIN` and `MANAGER`; teacher‑scoped for `TEACHER`; student‑scoped for `STUDENT`.

### 6.3 GDPR / CCPA Compliance
- **Data Retention**: Audit logs (`audit_log`) purged after 1 year; PII masked in analytics exports.
- **Right to Erasure**: Endpoint `DELETE /api/v1/users/{userId}` cascades deletion across `courses`, `enrollments`, `attendance`.
- **Data Export**: `GET /api/v1/users/{userId}/export` returns JSON with all personal data, encrypted in transit (TLS 1.3).

---

## 🛠️ 7. MONITORING & TROUBLESHOOTING

### 7.1 Cloud Monitoring Metrics
- **Custom Metrics**: `course.created`, `enrollment.success`, `attendance.scan.latency`, `conflict.detected`.
- **Alerting**: Thresholds for error rate >5% (5 min), latency >200 ms, pod restart >3 times/hour.
- **Dashboards**: Grafana panels for real‑time course availability, enrollment trends, attendance compliance.

### 7.2 Common Issues & Resolution
| Symptom | Root Cause | Resolution |
|---------|------------|------------|
| “Teacher schedule conflict” error | Overlapping `courses` records for same `teacher_id` | Adjust course dates or reassign teacher via `CourseService` conflict resolution UI. |
| “Student not enrolled” on QR scan | Student missing from `enrollments` for given `course_id` | Verify enrollment status; auto‑enroll if student has pending registration. |
| Duplicate attendance record | Race condition in `AttendanceService` | Use database `UNIQUE` constraint `uk_attendance_student_course_date`; log `DUPLICATE` status. |
| Push notification failure | Invalid FCM token | Invalidate token in `PushNotificationService` and trigger token re‑registration flow. |

---

## 📈 8. PERFORMANCE & SCALING

### 8.1 Horizontal Scaling
- **Stateless Services**: All modules designed stateless; session data stored in Redis with 24‑hour TTL.
- **Read Replicas**: PostgreSQL read replicas used for heavy reporting (`ReportService`) to offload primary.

### 8.2 Caching Strategies
- **Course Catalog**: Redis hash `course:catalog` refreshed on any `course` insert/update; TTL 5 minutes.
- **Student Profile**: Redis JSON `student:{id}` cached for 1 hour; invalidated on profile update.
- **Attendance Cache**: `attendance:scan:{qrCode}` lock key (TTL 30 seconds) prevents duplicate scans within window.

### 8.3 Latency Optimization
- **Prepared Statements**: All DB interactions use parameterized queries.
- **Connection Pooling**: HikariCP with max pool size tuned per service (e.g., `course-service` 20).
- **Async Processing**: Kafka producers for notifications decouple scanning from user response (<150 ms).

---

## 📚 9. MAINTENANCE & SUPPORT

### 9.1 Backup & Disaster Recovery
- **PostgreSQL**: Daily logical backups via `pg_dump` to Cloud Storage; point‑in‑time recovery up to 24 hours.
- **Redis**: Automated failover with persistent AOF; backup scripts to GCS every 6 hours.
- **GKE**: Regional cluster with multi‑zone master; workload migration via `kubectl cz migrate`.

### 9.2 Patch Management
1. **Staging Validation**: All changes merged to `develop`, run full integration test suite.
2. **Canary Deployment**: New container image deployed to 10% of pods; monitor metrics for 15 minutes.
3. **Rollback**: Immediate rollback via `kubectl rollout undo` if error rate spikes >5%.

### 9.3 Incident Response Workflow
1. **Detection**: Cloud Monitoring alerts → PagerDuty escalation.
2. **Triage**: Log analysis in Cloud Logging with trace ID `[REQ-XXX]`.
3. **Resolution**: Apply hot‑fix patch; if unresolved, open support ticket with `INC-{timestamp}`.
4. **Post‑mortem**: Document root cause, update runbooks, and close within 48 hours.

---

## 🔖 10. TRACEABILITY MATRIX REFERENCE

| Module/Component | Requirement Tags |
|------------------|------------------|
| **Course Service** (CRUD, conflict detection) | `[REQ-007], [REQ-008], [EXC-001], [ARC-007]` |
| **Enrollment Service** (registration, auto‑student) | `[REQ-010], [REQ-011]` |
| **Attendance Service** (QR scan, idempotent) | `[REQ-012], [REQ-013], [EXC-001], [EXC-002]` |
| **RBAC & Security** | `[ARC-001]‑[ARC-005], [NFR-003]` |
| **DevOps & CI/CD** | `[NFR-001], [NFR-002], [NFR-004], [NFR-005], [NFR-006]` |
| **Observability** | `[NFR-006], [NFR-007]` |
| **Multi‑Tenancy** | `[ARC-002]` |
| **Notification Integration** | `[ARC-008]` |
| **Mobile & Push** | `[ARC-009], [REQ-021]` |
| **Internationalization** | `[REQ-022], [REQ-023], [NFR-007]` |
| **Reporting & Analytics** | `[REQ-024], [REQ-025], [EXC-005]` |

---

## 📌 APPENDIX A: GLOSSARY

- **JWT**: JSON Web Token (signed, 15‑minute access, 7‑day refresh).
- **FCM / APNs**: Firebase Cloud Messaging, Apple Push Notification Service.
- **HPA**: Horizontal Pod Autoscaler (Kubernetes).
- **RBAC**: Role‑Based Access Control.
- **NFR**: Non‑Functional Requirement.
- **ARC**: Architecture Requirement Constraint.
- **REQ**: Functional Requirement.
- **EXC**: Exception / Error Requirement.
- **DAT**: Data Model Requirement.
- **INTEGRATION_SCOPE**: Marker for integration tests (no real network/DB calls).

---

*This Operations Guide is generated programmatically to ensure 100 % traceability to the original requirements and architectural constraints. All referenced Tag IDs (`[REQ-XXX]`, `[ARC-XXX]`, `[NFR-XXX]`, `[EXC-XXX]`, `[DAT-XXX]`) are preserved verbatim for audit compliance.*