markdown
# REST API Reference Documentation
## Membership Hub System
| Property | Value |
| :--- | :--- |
| **Document Version** | 1.0 |
| **Last Updated** | 2026-08-18 |
| **Target Destination Path** | `./sources/docs/api/rest-api-reference.md` |
| **Enforced Java Package Prefix** | `org.nlh4j.saas` |
| **Covered Traceability Tags** | [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006], [ARC-007], [ARC-008], [ARC-009], [ARC-010], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009] |

---

## Table of Contents
1. [Overview](#1-overview)
2. [Global API Conventions](#2-global-api-conventions)
3. [API Endpoint Specifications](#3-api-endpoint-specifications)
   1. [Authentication & OAuth2 APIs](#31-authentication--oauth2-apis)
   2. [User & Role Management APIs](#32-user--role-management-apis)
   3. [Center Management APIs](#33-center-management-apis)
   4. [Course Management APIs](#34-course-management-apis)
   5. [Enrollment APIs](#35-enrollment-apis)
   6. [Attendance QR APIs](#36-attendance-qr-apis)
   7. [Membership Card APIs](#37-membership-card-apis)
   8. [Notification APIs](#38-notification-apis)
   9. [Promotion & Announcement APIs](#39-promotion--announcement-apis)
   10. [Chatbot AI API](#310-chatbot-ai-api)
   11. [Report & Dashboard APIs](#311-report--dashboard-apis)
4. [Traceability Matrix Reference](#4-traceability-matrix-reference)
5. [Error Code Catalog](#5-error-code-catalog)
6. [Security & Compliance Guidelines](#6-security--compliance-guidelines)
7. [Appendix](#7-appendix)

---

## 1. Overview
The Membership Hub is a multi-tenant membership management system built on a 3-tier architecture: Next.js frontend, Quarkus Java backend, and PostgreSQL database, deployed on Google Kubernetes Engine (GKE) [ARC-010]. This document provides a complete reference for all public and internal REST APIs, including request/response schemas, authentication requirements, error codes, and compliance mappings.

All APIs are versioned under the `/api/v1` base path, enforce role-based access control (RBAC) with 5 predefined user roles [ARC-001, ARC-002, ARC-003, ARC-004, ARC-005], and support idempotent request processing for mutation operations [REQ-012, REQ-013]. All endpoints are mapped to their source requirement, architecture, data, and non-functional requirement tags for full traceability.

---

## 2. Global API Conventions
### 2.1 Base Configuration
| Property | Value |
| :--- | :--- |
| **API Base URL** | `https://api.membership-hub.com/api/v1` |
| **Request Format** | JSON (UTF-8 encoded) |
| **Response Format** | JSON (UTF-8 encoded) |
| **Pagination Defaults** | `page=1`, `size=20` (max `size=100`) |
| **Rate Limit** | 1000 requests per minute per authenticated user [NFR-001] |

### 2.2 Authentication
All protected endpoints require a valid JWT access token in the `Authorization` header:

Authorization: Bearer <JWT_ACCESS_TOKEN>

Access tokens expire after 15 minutes; refresh tokens (stored in HttpOnly cookies) expire after 7 days [ARC-006, NFR-003].

### 2.3 Idempotency Requirement
All mutation endpoints (`POST`, `PUT`, `DELETE`) require a unique `Idempotency-Key` header (UUID v4 format). The system stores keys for 24 hours and returns identical responses for duplicate requests with the same key to prevent duplicate processing [REQ-012, REQ-013, NFR-004].

### 2.4 Standard Error Response Structure
All error responses follow this schema:
json
{
  "error": "ERROR_CODE_STRING",
  "message": "Human-readable error description",
  "traceId": "UUID for request tracing",
  "timestamp": "ISO 8601 timestamp"
}


---

## 3. API Endpoint Specifications
All endpoints are grouped by business domain. The `Targeted Tag IDs` column maps each endpoint to its source requirements and constraints.

---

### 3.1 Authentication & OAuth2 APIs
Backend Service Path: `./sources/backend/auth-service/src/main/java/org/nlh4j/saas/auth/`
| HTTP Method | Full Endpoint | Targeted Tag IDs | Authentication Required | Request Parameters | Request Payload Schema | Response Payload (Success / Error) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/auth/register` | [REQ-001], [EXC-004], [ARC-006] | No | N/A | json { "email": "string (required, valid email format)", "password": "string (required, min 8 chars: uppercase, lowercase, number, special char)", "fullName": "string (required, max 100 chars)", "provider": "string (optional, enum: local/firebase/google/facebook, default: local)" }  | **Success 201**: json { "userId": "uuid", "email": "string", "role": "string (Student/Teacher)", "accessToken": "string (JWT, 15min expiry)", "refreshToken": "string (7day expiry)" } <br>**Error 400**: `VALIDATION_FAILED` (invalid input fields)<br>**Error 409**: `EMAIL_EXISTS` (email already registered) |
| `POST` | `/auth/login` | [REQ-001], [EXC-004], [ARC-006] | No | N/A | json { "email": "string (required)", "password": "string (required)" }  | **Success 200**: Same as register response<br>**Error 401**: `INVALID_CREDENTIALS` (wrong email/password) |
| `POST` | `/auth/oauth2/{provider}` | [REQ-002], [EXC-004], [ARC-006] | No | Path param: `provider` (enum: firebase/google/facebook) | json { "authCode": "string (required, OAuth2 authorization code from provider)" }  | **Success 200**: Same as register response<br>**Error 401**: `OAUTH2_AUTH_FAILED` (invalid auth code or provider error) |
| `POST` | `/auth/refresh` | [REQ-001], [ARC-006] | No (requires valid refresh token in HttpOnly cookie) | N/A | N/A | **Success 200**: json { "accessToken": "string (new JWT, 15min expiry)", "refreshToken": "string (new refresh token, 7day expiry)" } <br>**Error 401**: `INVALID_REFRESH_TOKEN` (expired or invalid refresh token) |
| `POST` | `/auth/logout` | [REQ-001], [ARC-006] | Yes (any authenticated user) | Header: `Idempotency-Key` (required) | N/A | **Success 200**: json { "message": "Logged out successfully" } <br>**Error 401**: `INVALID_TOKEN` |

---

### 3.2 User & Role Management APIs
Backend Service Path: `./sources/backend/user-service/src/main/java/org/nlh4j/saas/user/`
| HTTP Method | Full Endpoint | Targeted Tag IDs | Authentication Required | Request Parameters | Request Payload Schema | Response Payload (Success / Error) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/admin/users` | [REQ-003], [ARC-001] | System Admin | Query params: `role` (optional, enum: System Admin/Center Admin/Manager/Teacher/Student), `search` (optional, search by name/email), `page`, `size` | N/A | **Success 200**: json [ { "userId": "uuid", "email": "string", "fullName": "string", "role": "string", "provider": "string", "createdAt": "timestamp" } ] <br>**Error 403**: `PERMISSION_DENIED` |
| `PUT` | `/admin/users/{userId}/role` | [REQ-003], [ARC-001] | System Admin | Path param: `userId` (uuid)<br>Header: `Idempotency-Key` (required) | json { "roleId": "smallint (required, valid role ID from roles table)" }  | **Success 200**: json { "message": "Role updated successfully" } <br>**Error 403**: `PERMISSION_DENIED`<br>**Error 404**: `USER_NOT_FOUND` |

---

### 3.3 Center Management APIs
Backend Service Path: `./sources/backend/center-service/src/main/java/org/nlh4j/saas/center/`
| HTTP Method | Full Endpoint | Targeted Tag IDs | Authentication Required | Request Parameters | Request Payload Schema | Response Payload (Success / Error) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/centers` | [REQ-004], [ARC-002] | Any authenticated user | Query params: `page`, `size` | N/A | **Success 200**: json [ { "centerId": "uuid", "name": "string", "address": "string", "taxId": "string", "contactPhone": "string", "contactEmail": "string" } ]  |
| `POST` | `/admin/centers` | [REQ-005], [ARC-002] | System Admin | Header: `Idempotency-Key` (required) | json { "name": "string (required, max 100 chars)", "address": "string (required, max 255 chars)", "taxId": "string (required, 10-13 digits, unique)", "contactPhone": "string (optional, max 20 chars)", "contactEmail": "string (optional, valid email format)" }  | **Success 201**: Full center object<br>**Error 409**: `TAX_ID_CONFLICT` (tax ID already exists) |
| `PUT` | `/admin/centers/{centerId}` | [REQ-005], [ARC-002] | System Admin | Path param: `centerId` (uuid)<br>Header: `Idempotency-Key` (required) | Same as POST /admin/centers (all fields optional) | **Success 200**: Updated center object<br>**Error 404**: `CENTER_NOT_FOUND`<br>**Error 409**: `TAX_ID_CONFLICT` |
| `DELETE` | `/admin/centers/{centerId}` | [REQ-005], [ARC-002] | System Admin | Path param: `centerId` (uuid)<br>Header: `Idempotency-Key` (required) | N/A | **Success 200**: json { "message": "Center deleted successfully" } <br>**Error 409**: `CENTER_HAS_ACTIVE_RESOURCES` (center has active courses/enrollments) |
| `POST` | `/admin/centers/{centerId}/admins` | [REQ-006], [ARC-002] | System Admin | Path param: `centerId` (uuid)<br>Header: `Idempotency-Key` (required) | json { "userId": "uuid (required)", "isAssign": "boolean (required, true to assign, false to unassign)" }  | **Success 200**: json { "message": "Center admin assignment updated successfully" } <br>**Error 404**: `USER_NOT_FOUND` / `CENTER_NOT_FOUND` |

---

### 3.4 Course Management APIs
Backend Service Path: `./sources/backend/course-service/src/main/java/org/nlh4j/saas/course/`
| HTTP Method | Full Endpoint | Targeted Tag IDs | Authentication Required | Request Parameters | Request Payload Schema | Response Payload (Success / Error) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/courses` | [REQ-007], [ARC-007] | Any authenticated user | Query params: `centerId` (optional, filter by center), `page`, `size` | N/A | **Success 200**: json [ { "courseId": "uuid", "title": "string", "description": "string", "startDate": "date (YYYY-MM-DD)", "endDate": "date (YYYY-MM-DD)", "teacherId": "uuid", "teacherName": "string", "maxStudents": "int", "enrolledCount": "int" } ]  |
| `POST` | `/courses` | [REQ-008], [EXC-001], [ARC-007] | System Admin / Center Admin | Header: `Idempotency-Key` (required) | json { "title": "string (required, max 150 chars)", "description": "string (optional)", "startDate": "date (required, YYYY-MM-DD)", "endDate": "date (required, YYYY-MM-DD, must be > startDate)", "teacherId": "uuid (required, valid user with Teacher role)", "maxStudents": "int (optional, default 30, min 1)" }  | **Success 201**: json { "courseId": "uuid", "message": "Course created successfully" } <br>**Error 409**: `SCHEDULE_CONFLICT` (teacher has overlapping course schedule) |
| `PUT` | `/courses/{courseId}` | [REQ-008], [EXC-001], [ARC-007] | System Admin / Center Admin | Path param: `courseId` (uuid)<br>Header: `Idempotency-Key` (required) | Same as POST /courses (all fields optional) | **Success 200**: Updated course object<br>**Error 404**: `COURSE_NOT_FOUND`<br>**Error 409**: `SCHEDULE_CONFLICT` |
| `DELETE` | `/courses/{courseId}` | [REQ-008], [ARC-007] | System Admin / Center Admin | Path param: `courseId` (uuid)<br>Header: `Idempotency-Key` (required) | N/A | **Success 200**: json { "message": "Course deleted successfully" } <br>**Error 409**: `COURSE_HAS_ACTIVE_ENROLLMENTS` |
| `POST` | `/courses/{courseId}/assign-teacher` | [REQ-009], [ARC-007] | System Admin / Center Admin | Path param: `courseId` (uuid)<br>Header: `Idempotency-Key` (required) | json { "teacherId": "uuid (required, valid user with Teacher role)" }  | **Success 200**: json { "message": "Teacher assigned successfully, notification queued" } <br>**Error 409**: `SCHEDULE_CONFLICT` |

---

### 3.5 Enrollment APIs
Backend Service Path: `./sources/backend/enrollment-service/src/main/java/org/nlh4j/saas/enrollment/`
| HTTP Method | Full Endpoint | Targeted Tag IDs | Authentication Required | Request Parameters | Request Payload Schema | Response Payload (Success / Error) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/courses/available` | [REQ-010], [ARC-007] | Student | Query params: `page`, `size` | N/A | **Success 200**: json [ { "courseId": "uuid", "title": "string", "startDate": "date", "endDate": "date", "teacherName": "string", "maxStudents": "int", "remainingSlots": "int" } ] <br>(Excludes courses the student is already enrolled in) |
| `POST` | `/enrollments` | [REQ-010], [REQ-011], [ARC-007] | Student | Header: `Idempotency-Key` (required) | json { "courseId": "uuid (required)", "studentEmail": "string (optional, used to auto-create Student account if not exists)" }  | **Success 201**: json { "enrollmentId": "uuid", "message": "Enrolled successfully" } <br>**Error 400**: `COURSE_FULL` (no remaining slots)<br>**Error 409**: `ALREADY_ENROLLED` (student already enrolled in course) |

---

### 3.6 Attendance QR APIs
Backend Service Path: `./sources/backend/attendance-service/src/main/java/org/nlh4j/saas/attendance/`
| HTTP Method | Full Endpoint | Targeted Tag IDs | Authentication Required | Request Parameters | Request Payload Schema | Response Payload (Success / Error) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/attendance/scan` | [REQ-012], [REQ-013], [EXC-001], [EXC-002], [ARC-007] | Student | Header: `Idempotency-Key` (required) | json { "qrCode": "string (required, encoded payload containing courseId and sessionId)", "timestamp": "string (required, ISO 8601 timestamp of scan)" }  | **Success 200**: json { "attendanceId": "uuid", "status": "RECORDED | DUPLICATE", "message": "Attendance recorded successfully | Attendance already recorded for this session" } <br>**Error 400**: `INVALID_QR_CODE` (malformed QR payload)<br>**Error 403**: `NOT_ENROLLED_IN_COURSE` (student not enrolled in scanned course) |

---

### 3.7 Membership Card APIs
Backend Service Path: `./sources/backend/membership-service/src/main/java/org/nlh4j/saas/membership/`
| HTTP Method | Full Endpoint | Targeted Tag IDs | Authentication Required | Request Parameters | Request Payload Schema | Response Payload (Success / Error) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/membership/card` | [REQ-014], [DAT-007] | Student (own card) / Admin | N/A | N/A | **Success 200**: json { "cardId": "uuid", "studentId": "uuid", "issueDate": "date (YYYY-MM-DD)", "validityDays": "int", "remainingDays": "int", "expiryDate": "date (YYYY-MM-DD)" } <br>**Error 404**: `CARD_NOT_FOUND` |
| `POST` | `/membership/renew` | [REQ-015], [DAT-007] | Student (own card) / Admin | Header: `Idempotency-Key` (required) | json { "renewalDays": "int (required, > 0)", "paymentTransactionId": "string (required, valid payment gateway transaction ID)" }  | **Success 200**: json { "cardId": "uuid", "remainingDays": "int", "expiryDate": "date (YYYY-MM-DD)" } <br>**Error 402**: `PAYMENT_FAILED` (invalid or failed payment transaction) |

---

### 3.8 Notification APIs
Backend Service Path: `./sources/backend/notification-service/src/main/java/org/nlh4j/saas/notification/`
| HTTP Method | Full Endpoint | Targeted Tag IDs | Authentication Required | Request Parameters | Request Payload Schema | Response Payload (Success / Error) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/notifications/send` | [REQ-016], [EXC-003], [ARC-008] | System Admin / Center Admin / Manager | Header: `Idempotency-Key` (required) | json { "userId": "uuid (optional, target specific user)", "groupZalo": "string (optional, target Zalo group)", "message": "string (required, max 2000 chars)" }  | **Success 202**: json { "message": "Notification queued", "notificationId": "uuid" } <br>**Error 502**: `PUSH_DELIVERY_FAILED` (failed after 3 retries) |
| `POST` | `/notifications/register-token` | [REQ-021], [EXC-003] | Any authenticated user | N/A | json { "deviceToken": "string (required, FCM/APNs device token)", "platform": "string (required, enum: FCM/APNs)" }  | **Success 200**: json { "status": "registered" } <br>**Error 400**: `INVALID_DEVICE_TOKEN` |

---

### 3.9 Promotion & Announcement APIs
Backend Service Path: `./sources/backend/promotion-service/src/main/java/org/nlh4j/saas/promotion/`
| HTTP Method | Full Endpoint | Targeted Tag IDs | Authentication Required | Request Parameters | Request Payload Schema | Response Payload (Success / Error) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/promotions` | [REQ-017], [DAT-009] | Any authenticated user | Query params: `activeOnly` (optional, boolean, default true) | N/A | **Success 200**: json [ { "promoId": "uuid", "code": "string", "discountPercent": "int (0-100)", "startDate": "date", "endDate": "date", "description": "string" } ] <br>(Only returns promotions with valid date range if `activeOnly=true`) |
| `POST` | `/promotions` | [REQ-017], [DAT-009] | System Admin / Center Admin | Header: `Idempotency-Key` (required) | json { "code": "string (required, unique, max 50 chars)", "discountPercent": "int (required, 0-100)", "startDate": "date (optional)", "endDate": "date (optional, must be >= startDate)", "description": "string (optional)" }  | **Success 201**: json { "promoId": "uuid" } <br>**Error 400**: `INVALID_PROMOTION_DATA` (invalid date range or discount percent) |
| `GET` | `/announcements` | [REQ-018], [DAT-009] | Any authenticated user | Query params: `activeOnly` (optional, boolean, default true) | N/A | **Success 200**: json [ { "announcementId": "uuid", "title": "string (max 150 chars)", "content": "string (max 2000 chars)", "startDate": "date", "endDate": "date" } ] <br>(Only returns announcements with valid date range if `activeOnly=true`) |
| `POST` | `/announcements` | [REQ-018], [DAT-009] | System Admin / Center Admin / Manager | Header: `Idempotency-Key` (required) | json { "title": "string (required, max 150 chars)", "content": "string (required, max 2000 chars)", "startDate": "date (optional)", "endDate": "date (optional, must be >= startDate)" }  | **Success 201**: json { "announcementId": "uuid" } <br>**Error 400**: `INVALID_ANNOUNCEMENT_DATA` |

---

### 3.10 Chatbot AI API
Backend Service Path: `./sources/backend/chatbot-service/src/main/java/org/nlh4j/saas/chatbot/`
| HTTP Method | Full Endpoint | Targeted Tag IDs | Authentication Required | Request Parameters | Request Payload Schema | Response Payload (Success / Error) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/chatbot/message` | [REQ-019], [ARC-009] | Any authenticated user | N/A | json { "message": "string (required, user query)", "sessionId": "uuid (required, unique session ID for conversation context)" }  | **Success 200**: json { "reply": "string (AI generated response)", "confidence": "float (0.0-1.0, AI response confidence score)", "escalate": "boolean (true if confidence < 0.7, triggers human support handoff)" } <br>**Error 400**: `MISSING_REQUIRED_FIELDS`<br>**Error 401**: `UNAUTHENTICATED` |

---

### 3.11 Report & Dashboard APIs
Backend Service Path: `./sources/backend/report-service/src/main/java/org/nlh4j/saas/report/`
| HTTP Method | Full Endpoint | Targeted Tag IDs | Authentication Required | Request Parameters | Request Payload Schema | Response Payload (Success / Error) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/reports/attendance/csv` | [REQ-024], [EXC-005], [NFR-001] | Center Admin / System Admin | Query params: `centerId` (required, uuid), `startDate` (required, YYYY-MM-DD), `endDate` (required, YYYY-MM-DD, must be >= startDate) | N/A | **Success 200**: CSV file with columns: `StudentName, CourseName, AttendanceDate, Status`<br>**Error 403**: `PERMISSION_DENIED`<br>**Error 400**: `INVALID_DATE_RANGE` |
| `GET` | `/dashboard/enrollment` | [REQ-025], [EXC-005], [NFR-001] | Center Admin / System Admin | Query params: `centerId` (required, uuid) | N/A | **Success 200**: json { "totalStudents": "int (total enrolled students in center)", "activeCourses": "int (courses currently in progress)", "upcomingSessions": "int (sessions in next 7 days)" } <br>**Error 403**: `PERMISSION_DENIED` |

---

## 4. Traceability Matrix Reference
This section maps all system components, data schemas, and event pipelines to their corresponding requirement, architecture, data, and non-functional requirement tags.

| Component / Artifact | Description | Mapped Traceability Tags |
| :--- | :--- | :--- |
| Authentication Service | JWT/OAuth2 flow, token issuance, refresh token management | [REQ-001], [REQ-002], [ARC-006] |
| RBAC Middleware | Role-based access control, 5 user roles, permission enforcement | [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005] |
| Course Service | Course CRUD, schedule conflict validation, teacher assignment | [REQ-007], [REQ-008], [REQ-009], [EXC-001], [ARC-007] |
| Enrollment Service | Student course enrollment, auto-account creation for new students | [REQ-010], [REQ-011], [ARC-007] |
| Attendance Service | QR code scan processing, idempotent attendance recording, offline queue handling | [REQ-012], [REQ-013], [EXC-001], [EXC-002], [ARC-007] |
| Membership Service | Digital membership card management, renewal, payment integration | [REQ-014], [REQ-015], [DAT-007] |
| Notification Service | Multi-channel notifications (FCM/APNs, Zalo), retry logic, delivery tracking | [REQ-016], [REQ-021], [EXC-003], [ARC-008] |
| Promotion & Announcement Service | Promotion and announcement CRUD, auto-hide expired content | [REQ-017], [REQ-018], [DAT-009] |
| Chatbot Service | AI query processing, human support escalation logic | [REQ-019], [ARC-009] |
| Report Service | Attendance CSV export, real-time enrollment dashboard, post-outage FIFO processing | [REQ-024], [REQ-025], [EXC-005], [NFR-001] |
| PostgreSQL Schema | 11 business entity tables (users, roles, centers, courses, enrollments, attendance, student_cards, notifications, promotions, announcements, audit_log, system_settings), unique constraints, indexes | [DAT-001], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011] |
| Kafka Event Broker | Event pipelines for attendance scans, course assignments, notifications, system events | [ARC-008], [ARC-009] |
| GKE Infrastructure | Kubernetes deployment, HPA auto-scaling, health probes, rolling updates, backup configuration | [NFR-002], [NFR-004], [NFR-009] |
| Docker Images | Multi-stage builds, final image size <500MB, minimal base images (alpine/distroless) | [NFR-005] |
| CI/CD Pipeline | GitHub Actions, automated testing (coverage >=85%), security scanning, automated deployment | [NFR-004], [NFR-005], [NFR-006] |
| GDPR/CCPA Compliance | User data deletion, JSON data export, marketing consent management | [NFR-008] |
| Audit Logging System | Sensitive action logging, 1 year retention, PII masking | [NFR-006], [NFR-003] |
| i18n & SEO System | Multi-language support (en/vi/es), hreflang tags, locale detection | [REQ-022], [REQ-023], [NFR-007] |

---

## 5. Error Code Catalog
All error codes are mapped to their corresponding HTTP status, description, and traceability tags.

| HTTP Status | Error Code | Description | Related Tags |
| :--- | :--- | :--- | :--- |
| 400 | `VALIDATION_FAILED` | Invalid input fields (email format, password strength, missing required fields) | [REQ-001], [EXC-004] |
| 400 | `INVALID_QR_CODE` | Malformed QR code payload for attendance scan | [REQ-012], [EXC-001] |
| 400 | `SCHEDULE_CONFLICT` | Assigned teacher has overlapping course schedule | [REQ-008], [EXC-001] |
| 400 | `COURSE_FULL` | Course has reached maximum student capacity | [REQ-011], [ARC-007] |
| 400 | `INVALID_PROMOTION_DATA` | Invalid promotion date range or discount percent (0-100) | [REQ-017], [DAT-009] |
| 400 | `INVALID_ANNOUNCEMENT_DATA` | Invalid announcement date range | [REQ-018], [DAT-009] |
| 400 | `MISSING_REQUIRED_FIELDS` | Missing required fields in chatbot request | [REQ-019] |
| 401 | `INVALID_CREDENTIALS` | Wrong email/password for local login | [REQ-001], [ARC-006] |
| 401 | `OAUTH2_AUTH_FAILED` | Invalid OAuth2 authorization code or provider error | [REQ-002], [EXC-004] |
| 401 | `INVALID_TOKEN` | Expired or invalid JWT access token | [ARC-006] |
| 401 | `INVALID_REFRESH_TOKEN` | Expired or invalid refresh token | [ARC-006] |
| 401 | `UNAUTHENTICATED` | No valid authentication token provided | [ARC-006] |
| 403 | `PERMISSION_DENIED` | User lacks required role for requested endpoint | [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005] |
| 403 | `NOT_ENROLLED_IN_COURSE` | Student not enrolled in the course for attendance scan | [REQ-012], [EXC-001] |
| 404 | `USER_NOT_FOUND` | Requested user ID does not exist | [REQ-003] |
| 404 | `CENTER_NOT_FOUND` | Requested center ID does not exist | [REQ-005] |
| 404 | `COURSE_NOT_FOUND` | Requested course ID does not exist | [REQ-008] |
| 404 | `CARD_NOT_FOUND` | Student membership card not found | [REQ-014] |
| 409 | `TAX_ID_CONFLICT` | Center tax ID already exists in the system | [REQ-005], [ARC-002] |
| 409 | `ATTENDANCE_DUPLICATE` | Attendance already recorded for student/course/date | [REQ-013], [EXC-002] |
| 409 | `ALREADY_ENROLLED` | Student already enrolled in requested course | [REQ-011], [ARC-007] |
| 409 | `COURSE_HAS_ACTIVE_ENROLLMENTS` | Cannot delete course with active student enrollments | [REQ-008] |
| 409 | `CENTER_HAS_ACTIVE_RESOURCES` | Cannot delete center with active courses or enrollments | [REQ-005] |
| 402 | `PAYMENT_FAILED` | Membership renewal payment transaction failed | [REQ-015] |
| 502 | `PUSH_DELIVERY_FAILED` | Push notification delivery failed after 3 retry attempts | [REQ-016], [EXC-003], [REQ-021] |
| 503 | `SERVICE_UNAVAILABLE` | System under maintenance or recovering from outage | [EXC-005], [NFR-002] |

---

## 6. Security & Compliance Guidelines
All APIs adhere to the following security and compliance requirements mapped to non-functional tags:
1. **Transport Security**: All API traffic is encrypted via TLS 1.3 [NFR-003]
2. **Data at Rest Encryption**: All sensitive data (passwords, PII, payment info) is encrypted using AES-256 [NFR-003]
3. **OWASP Top 10 Mitigation**:
   - SQL Injection prevention via prepared statements and parameterized queries in Hibernate ORM [NFR-003]
   - XSS prevention via automatic input sanitization and JSX auto-escaping in Next.js frontend [NFR-003]
   - CSRF prevention via anti-CSRF tokens for all state-changing requests [NFR-003]
   - Sensitive data masking in logs and API responses (email, phone, address masked as `***MASKED***`) [NFR-003, NFR-006]
4. **RBAC Enforcement**: All endpoints have role-based access control enforced at both API gateway and service layers, with no privilege escalation paths [ARC-001, ARC-002, ARC-003, ARC-004, ARC-005]
5. **GDPR/CCPA Compliance**:
   - User data deletion on request
   - Full user data export in JSON format
   - Marketing consent management for all notification channels [NFR-008]
6. **Audit Logging**: All sensitive actions (role changes, attendance scans, notification sends, payment transactions) are logged with timestamp, user ID, and action details, stored for 1 year [NFR-006]

---

## 7. Appendix
### 7.1 API Versioning
- Current stable API version: `v1`
- Deprecation policy: Old versions are supported for 6 months after a new version release, with advance notification to all API consumers
- Versioning is enforced via the `/api/v1` base path

### 7.2 Support Contact
For API integration support, contact the backend team at `backend-support@membership-hub.com` or open a ticket in the internal Jira project `MEM-HUB-API`.

### 7.3 Change Log
| Version | Date | Changes |
| :--- | :--- | :--- |
| 1.0 | 2026-08-18 | Initial release of REST API reference for Membership Hub v1.0 |