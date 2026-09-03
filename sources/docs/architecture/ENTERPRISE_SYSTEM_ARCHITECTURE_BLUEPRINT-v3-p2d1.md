```markdown
# 🏛️ Enterprise System Architecture Blueprint: User Registration & Authentication Subsystem

## 📊 Document Control & Traceability Metadata
- **Document Title:** Enterprise System Architecture Blueprint - User Registration & Authentication Module
- **System Identity Safe Name:** membership-hub
- **Enforced Java Package Prefix Base:** `org.nlh4j.membershiphub`
- **Target Documentation Destination Path:** `./sources/docs/architecture/ENTERPRISE_SYSTEM_ARCHITECTURE_BLUEPRINT.md`
- **Associated Target Task:** Daily Session Git Branch Flow - Phase 2 Day 1: `features/development-phase-2-day-1`
- **Active Traceability Tag IDs:** `// [REQ-001]`, `// [DOC-001]`, `// [ARC-006]`, `// [NFR-003]`, `// [EXC-004]`

---

## 1. EXECUTIVE SUMMARY & SYSTEM ARCHITECTURE OVERVIEW

The **membership-hub** platform is an enterprise-grade, multi-center membership management ecosystem operating on a distributed microservices architecture. Designed for high availability, fault tolerance, and strict multi-tenancy isolation (`[ARC-002]`), the platform decouples business domains into specialized services: `user-service`, `center-service`, `course-service`, and `attendance-service`. 

This document serves as the authoritative blueprint for the **User Registration and Authentication Subsystem**, focusing specifically on the RESTful endpoint implementation (`POST /api/v1/users/register`), Jakarta Bean Validation constraints, Bouncy Castle BCrypt password hashing, SmallRye JWT token issuance (`[ARC-006]`), and enterprise security hardening (`[NFR-003]`).

```mermaid
architecture-beta
    group enterprise(cloud)[Membership Hub Enterprise Cloud]

    service apiGateway(internet)[API Gateway / Ingress NGINX] in enterprise
    service userService(server)[User Service (Quarkus 3.15)] in enterprise
    service db(database)[PostgreSQL 16 Primary DB] in enterprise
    service redis(database)[Redis Cache & Blacklist] in enterprise

    apiGateway:R --> L: userService
    userService:R --> L: db
    userService:B --> T: redis
```

---

## 2. SEQUENCE DIAGRAM: USER REGISTRATION & JWT ISSUANCE FLOW

The following Mermaid sequence diagram illustrates the end-to-end transaction flow when a client submits a registration request to `POST /api/v1/users/register`, detailing request validation, database persistence, password hashing, and token issuance (`[REQ-001]`, `[DOC-001]`, `[ARC-006]`).

```mermaid
sequenceDiagram
    autonumber
    actor Client as Mobile App / Next.js Frontend
    participant Gateway as API Gateway (Ingress NGINX)
    participant UserCtrl as AuthController (/api/v1/users/register)
    participant Val as Jakarta Bean Validation
    participant Service as AuthService
    participant DB as PostgreSQL (Users Table)
    participant JWT as JwtTokenProvider (RS256)

    Client->>Gateway: POST /api/v1/users/register (JSON Payload)
    Note over Client,Gateway: Payload contains email, password, fullName, agreedToTerms
    Gateway->>UserCtrl: Forward HTTP POST Request
    UserCtrl->>Val: Validate request payload against constraints
    
    alt Validation Fails (Weak Password / Invalid Email / Terms Unchecked)
        Val-->>UserCtrl: ConstraintViolationException
        UserCtrl-->>Client: HTTP 400 Bad Request (VALIDATION_FAILED)
    else Validation Succeeds
        Val-->>UserCtrl: Validation Passed
        UserCtrl->>Service: register(RegisterRequest)
        
        Service->>DB: Check if email already exists (LOWER(email))
        alt Email Already Exists
            DB-->>Service: Duplicate Record Found
            Service-->>UserCtrl: DataIntegrityViolationException
            UserCtrl-->>Client: HTTP 409 Conflict (EMAIL_ALREADY_EXISTS)
        else Email is Unique
            DB-->>Service: Email Available
            Service->>Service: Hash password via BCrypt (Cost Factor 12)
            Service->>DB: INSERT INTO users (user_id, email, password_hash, full_name, role_id)
            DB-->>Service: Persistence Confirmed
            
            Service->>JWT: generateAccessToken(userId, role, provider)
            JWT-->>Service: Signed JWT Access Token (15m TTL)
            Service->>JWT: generateRefreshToken(userId)
            JWT-->>Service: Refresh Token (7d TTL)
            
            Service-->>UserCtrl: AuthResponse DTO (tokens, userId, role)
            UserCtrl-->>Client: HTTP 201 Created + AuthResponse JSON
        end
    end
```

---

## 3. API SPECIFICATION: USER REGISTRATION ENDPOINT (`[REQ-001]`, `[DOC-001]`)

### 3.1. Endpoint Metadata
- **HTTP Method:** `POST`
- **Target URL Path:** `/api/v1/users/register`
- **Consumes:** `application/json`
- **Produces:** `application/json`
- **Authentication Required:** None (`@PermitAll`) — Public registration route.
- **Rate Limiting:** Maximum 5 requests per minute per IP address (enforced via Bucket4j Redis filter).
- **Targeted Tag IDs:** `[REQ-001]`, `[DOC-001]`, `[ARC-006]`, `[NFR-003]`, `[EXC-004]`

### 3.2. Request Headers
| Header Name | Type | Mandatory | Description | Targeted Tag ID |
| :--- | :--- | :--- | :--- | :--- |
| `Content-Type` | String | Yes | Must be `application/json`. | `[REQ-001]` |
| `Accept` | String | Yes | Must be `application/json`. | `[REQ-001]` |
| `X-Forwarded-For` | String | Yes | Client IP address for rate limiting and audit logging. | `[NFR-006]` |

### 3.3. JSON Request Payload Schema
```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "RegisterRequest",
  "type": "object",
  "required": ["email", "password", "fullName", "agreedToTerms"],
  "properties": {
    "email": {
      "type": "string",
      "format": "email",
      "maxLength": 255,
      "description": "Unique email address used for authentication."
    },
    "password": {
      "type": "string",
      "minLength": 8,
      "maxLength": 128,
      "description": "Strong password requiring at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character."
    },
    "fullName": {
      "type": "string",
      "maxLength": 100,
      "description": "Full legal name of the registering user."
    },
    "agreedToTerms": {
      "type": "boolean",
      "enum": [true],
      "description": "Mandatory flag indicating agreement with platform Terms of Service."
    }
  }
}
```

### 3.4. JSON Response Schemas (Success & Failure)

#### 🟢 Success Response (HTTP 201 Created)
```json
{
  "timestamp": "2026-08-29T22:34:21Z",
  "status": 201,
  "accessToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6Im1lbWJlcnNoaXAtaHViLWtleS0xIn0.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJncm91cCI6IlN0dWRlbnQiLCJpc3MiOiJtZW1iZXJzaGlwLWh1YiIsImF1ZCI6Im1lbWJlcnNoaXAtaHViLWNsaWVudCIsImV4cCI6MTc4Mjc3MDQ2MX0.signature_hash_bytes...",
  "refreshToken": "rt_8f3kd92kd73h1s9a0b2c3d4e5f6g7h8i",
  "expiresIn": 900,
  "tokenType": "Bearer",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "role": "Student"
}
```

#### 🔴 Failure Response: Validation Failed (HTTP 400 Bad Request)
```json
{
  "timestamp": "2026-08-29T22:34:21Z",
  "status": 400,
  "errorCode": "VALIDATION_FAILED",
  "message": "Input validation failed for registration request",
  "errors": [
    {
      "field": "password",
      "message": "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt",
      "rejectedValue": "weak123"
    },
    {
      "field": "agreedToTerms",
      "message": "Người dùng phải đồng ý với điều khoản dịch vụ",
      "rejectedValue": false
    }
  ],
  "path": "/api/v1/users/register",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef0123456789"
}
```

#### 🔴 Failure Response: Duplicate Email Conflict (HTTP 409 Conflict)
```json
{
  "timestamp": "2026-08-29T22:34:21Z",
  "status": 409,
  "errorCode": "EMAIL_ALREADY_EXISTS",
  "message": "Địa chỉ email 'user@example.com' đã được đăng ký trong hệ thống",
  "path": "/api/v1/users/register",
  "traceId": "b2c3d4e5-f678-90ab-cdef-0123456789ab"
}
```

### 3.5. Practical cURL Example
```bash
curl -X POST "https://api.membershiphub.vn/api/v1/users/register" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "email": "student.nguyen@membershiphub.vn",
    "password": "Str0ng!Password2026",
    "fullName": "Nguyen Van Student",
    "agreedToTerms": true
  }'
```

---

## 4. ERROR CODE MAPPING & LOCAL EXCEPTION HANDLING (`[EXC-004]`)

To guarantee predictable client-side integration and centralized logging aggregation, all operational errors within the user registration subsystem are intercepted by `GlobalExceptionHandler` and mapped to standardized HTTP statuses and application error codes:

| Exception Class | Trigger Condition | HTTP Status | Error Code (`errorCode`) | Targeted Tag ID |
| :--- | :--- | :--- | :--- | :--- |
| `MethodArgumentNotValidException` | Bean validation fails on DTO fields (email format, password strength). | `400 Bad Request` | `VALIDATION_FAILED` | `[EXC-004]` |
| `ConstraintViolationException` | Database unique constraint violation or service-level assertion failure. | `400 Bad Request` | `CONSTRAINT_VIOLATION` | `[EXC-004]` |
| `DataIntegrityViolationException` | Duplicate email insertion attempted at PostgreSQL persistence layer. | `409 Conflict` | `EMAIL_ALREADY_EXISTS` | `[EXC-004]` |
| `RateLimitExceededException` | IP address exceeds 5 registration requests per minute. | `429 Too Many Requests` | `RATE_LIMIT_EXCEEDED` | `[NFR-001]` |
| `Exception` (Fallback) | Unhandled runtime exceptions or database connectivity drops. | `500 Internal Server Error` | `INTERNAL_SERVER_ERROR` | `[EXC-004]` |

---

## 5. SECURITY HARDENING & RBAC CROSS-REFERENCE (`[NFR-003]`, `[ARC-001]`)

1. **Password Hashing:** Passwords are never stored in cleartext. They are hashed using **BCrypt** with a computational cost factor of `12` via Bouncy Castle (`[NFR-003]`).
2. **JWT Signature & Claims:** Access tokens are signed using **RS256** (RSA 2048-bit private key). The payload explicitly encodes `sub` (User UUID), `group` (Assigned Role), `iss` (`membership-hub`), and `aud` (`membership-hub-client`) with a hard 15-minute expiration window (`[ARC-006]`).
3. **RBAC Integration:** Newly registered users are automatically assigned `role_id = 5` corresponding to the **Student** role, granting them baseline permissions in accordance with the enterprise Role-Based Access Control matrix (`[ARC-001]`, `[ARC-005]`).
4. **Audit Logging:** Every registration attempt (success or failure) triggers an asynchronous audit record written to the `audit_logs` table via `AuthAuditLogger`, recording IP address, User-Agent, and timestamp, retained for 1 year in compliance with `[NFR-006]`.

---

## 6. TRACEABILITY MATRIX REFERENCE

| Requirement Code | Architectural Component / File Path | Compliance Objective |
| :--- | :--- | :--- |
| `[REQ-001]` | `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/AuthController.java` | User registration REST endpoint implementation. |
| `[DOC-001]` | `./sources/docs/architecture/ENTERPRISE_SYSTEM_ARCHITECTURE_BLUEPRINT.md` | Comprehensive architectural documentation and OpenAPI specifications. |
| `[ARC-006]` | `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtTokenProvider.java` | JWT token generation, RS256 signing, and validation. |
| `[NFR-003]` | `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/ResourceServerConfig.java` | Enterprise security baseline, TLS enforcement, and BCrypt hashing. |
| `[EXC-004]` | `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandler.java` | Centralized exception mapping and standard error DTO formatting. |
| `[ARC-001]` | `./sources/backend/user-service/src/main/resources/db/migration/V1__init_users_and_roles.sql` | 5-tier RBAC role definitions and database foreign key constraints. |
| `[NFR-006]` | `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/AuthAuditLogger.java` | Audit logging for authentication events retained for 1 year. |
```