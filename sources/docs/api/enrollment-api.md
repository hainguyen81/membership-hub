markdown
# Enrollment API Documentation

## Overview
The Enrollment API module provides endpoints for students to browse available courses and enroll in them. It automatically creates a Student user account if one does not exist for the provided email. The module enforces business rules such as preventing duplicate enrollments and respecting course capacity. All endpoints require JWT authentication and adhere to the RBAC policies defined in the system.

## Traceability Matrix Reference
| Module/Component | Associated Tag IDs |
|------------------|--------------------|
| `org.nlh4j.saas.enrollment.EnrollmentController` | [REQ-010], [REQ-011], [ARC-007] |
| `org.nlh4j.saas.enrollment.EnrollmentService` | [REQ-010], [REQ-011], [ARC-007] |
| `org.nlh4j.saas.enrollment.model.Enrollment` | [DAT-005] |
| `org.nlh4j.saas.enrollment.repository.EnrollmentRepository` | [DAT-005] |
| Kafka Event: `notification.course.assignment` | [ARC-008] |
| Kafka Event: `attendance.scan.result` | [ARC-008] |

## Endpoints

| HTTP Method | Full Endpoint | Request Headers | Path/Query Parameters | JSON Request Payload Schema | JSON Response Schema (Success) | JSON Response Schema (Failure) | Targeted Tag IDs |
|-------------|---------------|-----------------|-----------------------|-----------------------------|--------------------------------|--------------------------------|------------------|
| GET | `/api/v1/courses/available` | `Authorization: Bearer <JWT>` | `page (optional, default 1)`<br>`size (optional, default 20)` | — | json [{<br>  "courseId": "string (UUID)",<br>  "title": "string",<br>  "startDate": "YYYY-MM-DD",<br>  "endDate": "YYYY-MM-DD",<br>  "teacherName": "string",<br>  "maxStudents": "int",<br>  "remainingSlots": "int"<br>}] | json {<br>  "error": "VALIDATION_FAILED",<br>  "message": "List of validation errors"<br>} (400)<br>json {<br>  "error": "UNAUTHORIZED",<br>  "message": "Invalid or expired JWT"<br>} (401)<br>json {<br>  "error": "FORBIDDEN",<br>  "message": "Insufficient permissions"<br>} (403)<br>json {<br>  "error": "INTERNAL_SERVER_ERROR",<br>  "message": "Unexpected server error"<br>} (500) | [REQ-010], [ARC-007] |
| POST | `/api/v1/enrollments` | `Authorization: Bearer <JWT>` | — | json {<br>  "courseId": "string (UUID)",<br>  "studentEmail": "string (optional)"<br>} | json {<br>  "enrollmentId": "string (UUID)",<br>  "courseId": "string (UUID)",<br>  "studentId": "string (UUID)",<br>  "enrollmentDate": "string (ISO 8601)",<br>  "message": "string"<br>} (201) | json {<br>  "error": "VALIDATION_FAILED",<br>  "message": "Course not found or already enrolled"<br>} (400)<br>json {<br>  "error": "UNAUTHORIZED",<br>  "message": "Invalid or expired JWT"<br>} (401)<br>json {<br>  "error": "FORBIDDEN",<br>  "message": "Insufficient permissions"<br>} (403)<br>json {<br>  "error": "CONFLICT",<br>  "message": "Course capacity reached"<br>} (409)<br>json {<br>  "error": "INTERNAL_SERVER_ERROR",<br>  "message": "Unexpected server error"<br>} (500) | [REQ-011], [ARC-007] |

## Database Schema (Enrollment) – Tag `[DAT-005]`
sql
CREATE TABLE enrollments (
    enrollment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    course_id UUID NOT NULL REFERENCES courses(course_id) ON DELETE CASCADE,
    enrollment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_enrollment_student_course UNIQUE (student_id, course_id)
);

*Indexes:* `CREATE INDEX idx_enrollments_student_id ON enrollments(student_id);`<br>
*Indexes:* `CREATE INDEX idx_enrollments_course_id ON enrollments(course_id);`

## Kafka Event Pipelines
| Event Topic | Description | Produced By | Consumed By | Tag IDs |
|-------------|-------------|-------------|-------------|---------|
| `notification.course.assignment` | Triggers notification to assigned teacher and center admin when a student enrolls. | `EnrollmentService` | `NotificationService` | [ARC-008] |
| `attendance.scan.result` | Propagates attendance events to downstream analytics. | `AttendanceService` | `ReportService` | [ARC-008] |

## Error Handling & Logging
- All validation errors return `400 BAD REQUEST` with a detailed list of invalid fields.
- Authentication failures return `401 UNAUTHORIZED`.
- Authorization violations return `403 FORBIDDEN`.
- Conflict errors (e.g., duplicate enrollment) return `409 CONFLICT`.
- Unexpected server errors return `500 INTERNAL SERVER ERROR` with a generic message; detailed stack traces are logged internally and reported to the audit log system.
- Every request and response is logged with correlation ID and traceability Tag IDs for audit purposes.

## Security Considerations
- All endpoints enforce JWT authentication and RBAC checks (see `[ARC-001]`–`[ARC-005]`).
- Input validation is performed using Jakarta Bean Validation; SQL injection is prevented via Hibernate’s built-in prepared statement handling.
- Sensitive data (e.g., user passwords) are masked in logs using the centralized logging interceptor.
- HTTPS/TLS 1.3 is required for all API communications.

## Versioning & Compatibility
- Current API version: `v1`.
- Future breaking changes will be versioned as `v2` and documented in a migration guide.