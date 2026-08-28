markdown
# 📚 COURSE MODULE ARCHITECTURE DOCUMENTATION
*Target Destination: `./sources/docs/course-module-architecture.md`*

## 🏗️ 1. SYSTEM OVERVIEW

### 1.1 Architectural Overview
The Course Module represents a core component of the membership-hub enterprise system, providing comprehensive course management functionality within a multi-tenant architecture. The module implements a robust, scalable solution for course lifecycle management, student enrollment, attendance tracking, and real-time scheduling conflict resolution.

### 1.2 Key System Characteristics
- **Multi-Tenant Architecture**: Each center operates within isolated data boundaries while sharing common application logic
- **Event-Driven Processing**: Kafka-based asynchronous communication for decoupling services and ensuring eventual consistency
- **Real-Time Conflict Resolution**: Automated teacher schedule conflict detection and prevention
- **Idempotent Operations**: Guaranteed data consistency for attendance recording and enrollment processing
- **Role-Based Access Control**: Granular permissions across all course-related operations

### 1.3 System Boundaries

┌─────────────────────────────────────────────────────────────────┐
│                    COURSE MODULE (Backend)                      │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐    ┌─────────────────┐                    │
│  │ Course Service  │    │ Enrollment      │                    │
│  │                 │    │ Service         │                    │
│  │ - CRUD Operations│   │ - Enrollment    │                    │
│  │ - Schedule      │    │ - Student Card  │                    │
│  │   Conflict      │    │   Management    │                    │
│  │   Detection     │    │ - Payment       │                    │
│  └─────────────────┘    │ Integration     │                    │
│                         └─────────────────┘                    │
│  ┌─────────────────┐    ┌─────────────────┐                    │
│  │ Attendance      │    │ Notification    │                    │
│  │ Service         │    │ Service         │                    │
│  │ - QR Processing │    │ - Multi-Channel │                    │
│  │ - Idempotent    │    │   Delivery      │                    │
│  │   Recording     │    │ - Retry Logic   │                    │
│  └─────────────────┘    └─────────────────┘                    │
└─────────────────────────────────────────────────────────────────┘
         │                               │
         ▼                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SHARED INFRASTRUCTURE                         │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐    ┌─────────────────┐                    │
│  │ PostgreSQL      │    │ Redis           │                    │
│  │ - Courses       │    │ - Session Cache │                    │
│  │ - Enrollments   │    │ - Rate Limiting │                    │
│  │ - Attendance    │    │ - User Sessions │                    │
│  │ - Notifications │    │                 │                    │
│  └─────────────────┘    └─────────────────┘                    │
│  ┌─────────────────┐    ┌─────────────────┐                    │
│  │ Kafka           │    │ Firebase        │                    │
│  │ - Course Events │    │ - Auth          │                    │
│  │ - Attendance    │    │ - Notifications │                    │
│  │   Events        │    │                 │                    │
│  └─────────────────┘    └─────────────────┘                    │
└─────────────────────────────────────────────────────────────────┘


## 📋 2. API SPECIFICATIONS

### 2.1 Course Management APIs

| HTTP Method | Full Endpoint | Request Headers | Path/Query Parameters | JSON Request Payload Schema | JSON Response Schema (Success) | JSON Response Schema (Failure) | Targeted Tag IDs |
|-------------|---------------|-----------------|----------------------|----------------------------|--------------------------------|--------------------------------|------------------|
| `GET` | `/api/v1/courses` | `Authorization: Bearer <JWT>` | `centerId: UUID (optional)`<br>`page: int (optional, default: 1)`<br>`size: int (optional, default: 20)` | N/A | `[{<br>  "courseId": "UUID",<br>  "title": "STRING",<br>  "description": "STRING",<br>  "startDate": "DATE (YYYY-MM-DD)",<br>  "endDate": "DATE (YYYY-MM-DD)",<br>  "teacherId": "UUID",<br>  "teacherName": "STRING",<br>  "maxStudents": "INT",<br>  "enrolledCount": "INT"<br>}]` | `{<br>  "error": "VALIDATION_FAILED",<br>  "message": "Invalid query parameters"<br>}` | `[REQ-007], [DAT-004], [ARC-007]` |
| `POST` | `/api/v1/courses` | `Authorization: Bearer <JWT>`<br>`Content-Type: application/json` | N/A | `{<br>  "title": "STRING (required, max 150 chars)",<br>  "description": "STRING (optional)",<br>  "startDate": "DATE (required, YYYY-MM-DD)",<br>  "endDate": "DATE (required, YYYY-MM-DD)",<br>  "teacherId": "UUID (required)",<br>  "maxStudents": "INT (optional, default: 30)"<br>}` | `{<br>  "courseId": "UUID",<br>  "message": "Course created successfully"<br>}` | `{<br>  "error": "CONFLICT",<br>  "message": "Teacher schedule conflict detected"<br>}` | `[REQ-008], [EXC-001], [DAT-004], [ARC-007]` |
| `PUT` | `/api/v1/courses/{courseId}` | `Authorization: Bearer <JWT>`<br>`Content-Type: application/json` | `courseId: UUID (path parameter)` | `{<br>  "title": "STRING (optional)",<br>  "description": "STRING (optional)",<br>  "startDate": "DATE (optional)",<br>  "endDate": "DATE (optional)",<br>  "teacherId": "UUID (optional)",<br>  "maxStudents": "INT (optional)"<br>}` | `{<br>  "courseId": "UUID",<br>  "message": "Course updated successfully"<br>}` | `{<br>  "error": "NOT_FOUND",<br>  "message": "Course not found"<br>}` | `[REQ-008], [DAT-004], [ARC-007]` |
| `DELETE` | `/api/v1/courses/{courseId}` | `Authorization: Bearer <JWT>` | `courseId: UUID (path parameter)` | N/A | `{<br>  "message": "Course deleted successfully"<br>}` | `{<br>  "error": "FORBIDDEN",<br>  "message": "Insufficient permissions or course has active enrollments"<br>}` | `[REQ-008], [DAT-004], [ARC-007]` |
| `POST` | `/api/v1/courses/{courseId}/assign-teacher` | `Authorization: Bearer <JWT>`<br>`Content-Type: application/json` | `courseId: UUID (path parameter)` | `{<br>  "teacherId": "UUID (required)"<br>}` | `{<br>  "message": "Teacher assigned successfully, notification queued"<br>}` | `{<br>  "error": "CONFLICT",<br>  "message": "Teacher schedule conflict detected"<br>}` | `[REQ-009], [EXC-001], [DAT-004], [ARC-007]` |

### 2.2 Enrollment Management APIs

| HTTP Method | Full Endpoint | Request Headers | Path/Query Parameters | JSON Request Payload Schema | JSON Response Schema (Success) | JSON Response Schema (Failure) | Targeted Tag IDs |
|-------------|---------------|-----------------|----------------------|----------------------------|--------------------------------|--------------------------------|------------------|
| `GET` | `/api/v1/courses/available` | `Authorization: Bearer <JWT>` | `page: int (optional)`<br>`size: int (optional)` | N/A | `[{<br>  "courseId": "UUID",<br>  "title": "STRING",<br>  "startDate": "DATE",<br>  "endDate": "DATE",<br>  "teacherName": "STRING",<br>  "maxStudents": "INT",<br>  "remainingSlots": "INT"<br>}]` | `{<br>  "error": "UNAUTHORIZED",<br>  "message": "Invalid or expired JWT token"<br>}` | `[REQ-010], [DAT-005], [ARC-007]` |
| `POST` | `/api/v1/enrollments` | `Authorization: Bearer <JWT>`<br>`Content-Type: application/json` | N/A | `{<br>  "courseId": "UUID (required)",<br>  "studentEmail": "STRING (optional, for auto-creation)"<br>}` | `{<br>  "enrollmentId": "UUID",<br>  "message": "Enrollment successful"<br>}` | `{<br>  "error": "CONFLICT",<br>  "message": "Student already enrolled in this course"<br>}` | `[REQ-011], [DAT-005], [ARC-007]` |

### 2.3 Attendance Management APIs

| HTTP Method | Full Endpoint | Request Headers | Path/Query Parameters | JSON Request Payload Schema | JSON Response Schema (Success) | JSON Response Schema (Failure) | Targeted Tag IDs |
|-------------|---------------|-----------------|----------------------|----------------------------|--------------------------------|--------------------------------|------------------|
| `POST` | `/api/v1/attendance/scan` | `Authorization: Bearer <JWT>`<br>`Content-Type: application/json` | N/A | `{<br>  "qrCode": "STRING (required, contains courseId and sessionId)",<br>  "timestamp": "TIMESTAMP (required, ISO 8601 format)"<br>}` | `{<br>  "attendanceId": "UUID",<br>  "status": "RECORDED | DUPLICATE",<br>  "message": "Attendance recorded successfully | Already recorded today"<br>}` | `{<br>  "error": "UNAUTHORIZED",<br>  "message": "Invalid JWT token"<br>}` | `[REQ-012], [REQ-013], [EXC-001], [EXC-002], [DAT-006], [ARC-007]` |

## 🏗️ 3. SYSTEM ARCHITECTURE DIAGRAM

mermaid
graph TB
    subgraph "Frontend Layer"
        WebApp[Web Application]
        MobileApp[Mobile Application]
    end
    
    subgraph "API Gateway"
        Auth[Authentication Service]
        RBAC[RBAC Filter]
    end
    
    subgraph "Course Service"
        CourseCtrl[Course Controller]
        CourseSrv[Course Service]
        CourseRepo[Course Repository]
    end
    
    subgraph "Enrollment Service"
        EnrollCtrl[Enrollment Controller]
        EnrollSrv[Enrollment Service]
        EnrollRepo[Enrollment Repository]
    end
    
    subgraph "Attendance Service"
        AttendCtrl[Attendance Controller]
        AttendSrv[Attendance Service]
        AttendRepo[Attendance Repository]
    end
    
    subgraph "Shared Services"
        NotifSrv[Notification Service]
        CardSrv[Membership Card Service]
        ChatbotSrv[Chatbot Service]
        ReportSrv[Report Service]
    end
    
    subgraph "Data Layer"
        PostgreSQL[(PostgreSQL Database)]
        Redis[(Redis Cache)]
        Kafka[(Kafka Broker)]
    end
    
    subgraph "External Integrations"
        Firebase[Firebase Auth]
        ZaloAPI[Zalo API]
        FCM[Firebase Cloud Messaging]
        APNs[Apple Push Notification Service]
    end
    
    WebApp --> Auth
    MobileApp --> Auth
    Auth --> RBAC
    
    CourseCtrl --> CourseSrv
    CourseSrv --> CourseRepo
    CourseSrv --> Kafka
    CourseSrv --> NotifSrv
    
    EnrollCtrl --> EnrollSrv
    EnrollSrv --> EnrollRepo
    EnrollSrv --> CardSrv
    EnrollSrv --> Kafka
    EnrollSrv --> NotifSrv
    
    AttendCtrl --> AttendSrv
    AttendSrv --> AttendRepo
    AttendSrv --> Kafka
    AttendSrv --> NotifSrv
    
    NotifSrv --> FCM
    NotifSrv --> APNs
    NotifSrv --> ZaloAPI
    
    CardSrv --> PostgreSQL
    ChatbotSrv --> PostgreSQL
    ReportSrv --> PostgreSQL
    ReportSrv --> Redis
    
    CourseRepo --> PostgreSQL
    EnrollRepo --> PostgreSQL
    AttendRepo --> PostgreSQL
    
    Kafka --> PostgreSQL
    
    style CourseSrv fill:#e1f5fe,stroke:#01579b
    style EnrollSrv fill:#f3e5f5,stroke:#4a148c
    style AttendSrv fill:#e8f5e8,stroke:#1b5e20
    style NotifSrv fill:#fff3e0,stroke:#e65100


## 🗄️ 4. DATABASE SCHEMA

### 4.1 Core Course Module Tables

sql
-- Courses Table [DAT-004]
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
    FOREIGN KEY (teacher_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (center_id) REFERENCES centers(center_id) ON DELETE CASCADE,
    CHECK (start_date < end_date),
    CHECK (max_students > 0)
);

-- Create indexes for courses table
CREATE INDEX idx_courses_teacher_id ON courses(teacher_id);
CREATE INDEX idx_courses_center_id ON courses(center_id);
CREATE INDEX idx_courses_date_range ON courses(start_date, end_date);
CREATE INDEX idx_courses_center_date ON courses(center_id, start_date, end_date);

-- Enrollments Table [DAT-005]
CREATE TABLE enrollments (
    enrollment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    enrollment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELLED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    UNIQUE (student_id, course_id),
    CHECK (enrollment_date <= CURRENT_DATE)
);

-- Create indexes for enrollments table
CREATE INDEX idx_enrollments_student_id ON enrollments(student_id);
CREATE INDEX idx_enrollments_course_id ON enrollments(course_id);
CREATE INDEX idx_enrollments_status ON enrollments(status);
CREATE INDEX idx_enrollments_date ON enrollments(enrollment_date);

-- Attendance Table [DAT-006]
CREATE TABLE attendance (
    attendance_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    attendance_date DATE NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    qr_code_hash VARCHAR(64) NOT NULL,
    location VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    UNIQUE (student_id, course_id, attendance_date),
    CHECK (attendance_date <= CURRENT_DATE)
);

-- Create indexes for attendance table
CREATE INDEX idx_attendance_student_id ON attendance(student_id);
CREATE INDEX idx_attendance_course_id ON attendance(course_id);
CREATE INDEX idx_attendance_date ON attendance(attendance_date);
CREATE INDEX idx_attendance_student_course_date ON attendance(student_id, course_id, attendance_date);


### 4.2 Supporting Tables

sql
-- Student Cards Table [DAT-007]
CREATE TABLE student_cards (
    card_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    issue_date DATE NOT NULL DEFAULT CURRENT_DATE,
    validity_days INT NOT NULL CHECK (validity_days > 0),
    remaining_days INT NOT NULL CHECK (remaining_days >= 0),
    expiry_date DATE NOT NULL,
    payment_transaction_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CHECK (remaining_days <= validity_days)
);

-- Create indexes for student_cards table
CREATE INDEX idx_student_cards_student_id ON student_cards(student_id);
CREATE INDEX idx_student_cards_expiry_date ON student_cards(expiry_date);

-- Notifications Table [DAT-008]
CREATE TABLE notifications (
    notification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(user_id) ON DELETE CASCADE,
    group_zalo VARCHAR(255),
    message TEXT NOT NULL CHECK (length(message) <= 2000),
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivered BOOLEAN NOT NULL DEFAULT FALSE,
    retry_count INT NOT NULL DEFAULT 0 CHECK (retry_count BETWEEN 0 AND 3),
    notification_type VARCHAR(50) NOT NULL CHECK (notification_type IN ('COURSE_ASSIGNMENT', 'ENROLLMENT_CONFIRMATION', 'ATTENDANCE_REMINDER', 'CARD_RENEWAL', 'PROMOTION', 'ANNOUNCEMENT')),
    related_entity_type VARCHAR(50),
    related_entity_id UUID
);

-- Create indexes for notifications table
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_delivered ON notifications(delivered);
CREATE INDEX idx_notifications_type ON notifications(notification_type);
CREATE INDEX idx_notifications_sent_at ON notifications(sent_at);

-- Promotions Table [DAT-009]
CREATE TABLE promotions (
    promo_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL,
    discount_percent SMALLINT NOT NULL CHECK (discount_percent BETWEEN 0 AND 100),
    start_date DATE,
    end_date DATE,
    description TEXT,
    max_uses INT DEFAULT 0 CHECK (max_uses >= 0),
    current_uses INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (end_date IS NULL OR end_date >= start_date),
    CHECK (current_uses <= max_uses)
);

-- Create indexes for promotions table
CREATE INDEX idx_promotions_code ON promotions(code);
CREATE INDEX idx_promotions_active ON promotions(start_date, end_date) WHERE end_date IS NULL OR end_date >= CURRENT_DATE;
CREATE INDEX idx_promotions_usage ON promotions(current_uses, max_uses);

-- Announcements Table [DAT-009]
CREATE TABLE announcements (
    announcement_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(150) NOT NULL CHECK (length(title) <= 150),
    content TEXT NOT NULL CHECK (length(content) <= 2000),
    start_date DATE,
    end_date DATE,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (end_date IS NULL OR end_date >= start_date),
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Create indexes for announcements table
CREATE INDEX idx_announcements_active ON announcements(start_date, end_date) WHERE end_date IS NULL OR end_date >= CURRENT_DATE;
CREATE INDEX idx_announcements_created_by ON announcements(created_by);


## 📡 5. KAFKA EVENT PIPELINES

### 5.1 Course Management Events

json
{
  "topic": "course.events",
  "partitions": 3,
  "retention.ms": 604800000,
  "message.types": [
    {
      "type": "COURSE_CREATED",
      "payload": {
        "courseId": "UUID",
        "title": "STRING",
        "centerId": "UUID",
        "teacherId": "UUID",
        "startDate": "DATE",
        "endDate": "DATE",
        "timestamp": "TIMESTAMP"
      }
    },
    {
      "type": "COURSE_UPDATED",
      "payload": {
        "courseId": "UUID",
        "changes": "JSONB",
        "timestamp": "TIMESTAMP"
      }
    },
    {
      "type": "COURSE_DELETED",
      "payload": {
        "courseId": "UUID",
        "deletedBy": "UUID",
        "timestamp": "TIMESTAMP"
      }
    },
    {
      "type": "TEACHER_ASSIGNED",
      "payload": {
        "courseId": "UUID",
        "teacherId": "UUID",
        "previousTeacherId": "UUID",
        "timestamp": "TIMESTAMP"
      }
    }
  ]
}


### 5.2 Enrollment Events

json
{
  "topic": "enrollment.events",
  "partitions": 2,
  "retention.ms": 2592000000,
  "message.types": [
    {
      "type": "ENROLLMENT_CREATED",
      "payload": {
        "enrollmentId": "UUID",
        "studentId": "UUID",
        "courseId": "UUID",
        "enrollmentDate": "TIMESTAMP",
        "timestamp": "TIMESTAMP"
      }
    },
    {
      "type": "ENROLLMENT_CANCELLED",
      "payload": {
        "enrollmentId": "UUID",
        "studentId": "UUID",
        "courseId": "UUID",
        "cancelledBy": "UUID",
        "timestamp": "TIMESTAMP"
      }
    }
  ]
}


### 5.3 Attendance Events

json
{
  "topic": "attendance.events",
  "partitions": 3,
  "retention.ms": 2592000000,
  "message.types": [
    {
      "type": "ATTENDANCE_RECORDED",
      "payload": {
        "attendanceId": "UUID",
        "studentId": "UUID",
        "courseId": "UUID",
        "attendanceDate": "DATE",
        "timestamp": "TIMESTAMP",
        "qrCodeHash": "STRING"
      }
    },
    {
      "type": "ATTENDANCE_DUPLICATE",
      "payload": {
        "attendanceId": "UUID",
        "studentId": "UUID",
        "courseId": "UUID",
        "attendanceDate": "DATE",
        "timestamp": "TIMESTAMP"
      }
    }
  ]
}


### 5.4 Notification Events

json
{
  "topic": "notification.events",
  "partitions": 2,
  "retention.ms": 604800000,
  "message.types": [
    {
      "type": "NOTIFICATION_CREATED",
      "payload": {
        "notificationId": "UUID",
        "userId": "UUID",
        "type": "STRING",
        "message": "STRING",
        "timestamp": "TIMESTAMP"
      }
    },
    {
      "type": "NOTIFICATION_SENT",
      "payload": {
        "notificationId": "UUID",
        "deliveryStatus": "SENT | FAILED",
        "deliveredAt": "TIMESTAMP",
        "timestamp": "TIMESTAMP"
      }
    }
  ]
}


## 🚀 6. DEPLOYMENT GUIDELINES

### 6.1 Docker Configuration

dockerfile
# Multi-stage build for Course Module
FROM eclipse-temurin:21-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Copy Maven configuration
COPY pom.xml .
COPY .mvn/ .mvn/

# Download dependencies
RUN mvn dependency:go-offline

# Copy source code
COPY src/ src/

# Build application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Install required system packages
RUN apk add --no-cache curl

# Set working directory
WORKDIR /app

# Create non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copy application from builder
COPY --from=builder /app/target/*.jar app.jar

# Change ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=production"]


### 6.2 Kubernetes Deployment

yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: course-service
  labels:
    app: course-service
    version: v1.0.0
spec:
  replicas: 3
  selector:
    matchLabels:
      app: course-service
  template:
    metadata:
      labels:
        app: course-service
        version: v1.0.0
    spec:
      containers:
      - name: course-service
        image: gcr.io/your-project/course-service:v1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: datasource-url
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: datasource-username
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: datasource-password
        - name: KAFKA_BOOTSTRAP_SERVERS
          value: "kafka:9092"
        - name: REDIS_HOST
          value: "redis"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: course-service
spec:
  selector:
    app: course-service
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: ClusterIP
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
  minReplicas: 3
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
  behavior:
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
      - type: Percent
        value: 100
        periodSeconds: 60
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 100
        periodSeconds: 300


## 🔒 7. SECURITY & COMPLIANCE

### 7.1 OWASP Top 10 Mitigations

| Threat | Mitigation Strategy | Implementation |
|--------|-------------------|----------------|
| SQL Injection | Prepared Statements | All Hibernate queries use parameterized queries |
| Cross-Site Scripting | Input Sanitization | Frontend auto-escaping + CSP headers |
| Broken Authentication | JWT with Refresh Tokens | Firebase Auth integration + custom JWT |
| Sensitive Data Exposure | Encryption + Masking | AES-256 encryption + log data masking |
| XML External Entities | Disabled External Entities | Spring XML configuration disabled |
| Broken Access Control | RBAC Middleware | Centralized permission checking |
| Security Misconfiguration | Hardening Guides | Minimal Docker images + secure headers |
| Insecure Deserialization | Safe Deserialization | Jackson with type safety |
| Using Components with Known Vulnerabilities | Dependency Scanning | Automated vulnerability scanning |
| Insufficient Logging & Monitoring | Comprehensive Logging | Structured logging + audit trails |

### 7.2 RBAC Implementation


@Component
public class RbacMiddleware {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private EnrollmentService enrollmentService;
    
    public boolean checkCourseAccess(UUID courseId, String userRole, UUID userId) {
        switch (userRole) {
            case "SYSTEM_ADMIN":
                return true; // Full access
            case "CENTER_ADMIN":
                return courseService.isCourseInManagedCenter(courseId, userId);
            case "MANAGER":
                return courseService.isCourseInManagedCenter(courseId, userId) && 
                       !courseService.isCourseScheduled(courseId);
            case "TEACHER":
                return courseService.isTeacherAssignedToCourse(courseId, userId);
            case "STUDENT":
                return enrollmentService.isStudentEnrolledInCourse(courseId, userId);
            default:
                return false;
        }
    }
}


## 📊 8. MONITORING & OBSERVABILITY

### 8.1 Metrics & Monitoring

yaml
# Prometheus Configuration
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'course-service'
    static_configs:
      - targets: ['course-service:8080']
    metrics_path: '/actuator/prometheus'
    metric_relabel_configs:
      - source_labels: [__name__]
        target_label: service
        replacement: course-service
      - source_labels: [__name__]
        target_label: version
        replacement: v1.0.0


### 8.2 Logging Configuration

yaml
# Logback Configuration
<appender name="console" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
        <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        <charset>UTF-8</charset>
    </encoder>
</appender>

<logger name="com.membershiphub.course" level="INFO" additivity="false">
    <appender-ref ref="console" />
</logger>

<root level="WARN">
    <appender-ref ref="console" />
</root>


## 📋 9. TRACEABILITY MATRIX REFERENCE

| Module/Component | Requirement Tags | Description |
|------------------|------------------|-------------|
| Course Controller | `[REQ-007], [REQ-008], [REQ-009]` | REST API endpoints for course management |
| Course Service | `[REQ-007], [REQ-008], [REQ-009], [EXC-001]` | Business logic for course operations |
| Course Repository | `[DAT-004]` | Database access layer for courses |
| Enrollment Controller | `[REQ-010], [REQ-011]` | REST API endpoints for enrollment |
| Enrollment Service | `[REQ-010], [REQ-011], [DAT-005]` | Business logic for enrollment management |
| Enrollment Repository | `[DAT-005]` | Database access layer for enrollments |
| Attendance Controller | `[REQ-012], [REQ-013], [EXC-001], [EXC-002]` | REST API endpoints for attendance |
| Attendance Service | `[REQ-012], [REQ-013], [EXC-001], [EXC-002], [DAT-006]` | Business logic for attendance management |
| Attendance Repository | `[DAT-006]` | Database access layer for attendance |
| Notification Service | `[REQ-016], [EXC-003], [DAT-008]` | Multi-channel notification delivery |
| Kafka Event Bus | `[ARC-007]` | Asynchronous event processing |
| RBAC Middleware | `[ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]` | Role-based access control |
| Security Configuration | `[NFR-003]` | OWASP Top 10 mitigations |


This comprehensive documentation provides a complete technical specification for the Course Module, including all the required traceability mappings, API specifications, system architecture, database schemas, deployment guidelines, and security measures. The documentation follows enterprise standards and provides detailed information for developers, DevOps engineers, and system administrators.