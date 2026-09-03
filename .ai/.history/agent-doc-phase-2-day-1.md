# Day 1: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/docs/architecture/ENTERPRISE_SYSTEM_ARCHITECTURE_BLUEPRINT.md
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DOCUMENT MATRIX INJECTION
*   Target Project Identity Safe Name: 
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Documentation Destination Path: `./sources/docs/architecture/ENTERPRISE_SYSTEM_ARCHITECTURE_BLUEPRINT.md`




*   Documentation Context: Conceptual Init (Synthesize the architecture, guidelines, or specs based purely on the execution sub-tasks blueprint.)


### 📋 EXECUTION SUB-TASKS & DOCUMENT CONTENT TO WRITE
['Cập nhật tệp tài liệu API tổng hợp ./sources/docs/api/user-center-contracts.md bổ sung mục mô tả endpoint POST /api/v1/users/register với các thành phần: mô tả nghiệp vụ chi tiết, bảng mã lỗi HTTP 400 (validation failed), 409 (EMAIL_ALREADY_EXISTS), schema request/response đầy đủ, ví dụ curl thực tế với payload JSON, ghi chú bảo mật (rate limiting 5 lần/phút), liên kết chéo đến ma trận RBAC. Đảm bảo tài liệu chứa sơ đồ Mermaid sequenceDiagram thể hiện luồng đăng ký từ client đến server và phản hồi JWT.']

---

### ⚙️ TECHNICAL WRITER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder guardrails, and detailed sub-task document specifications provided above.


Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[DOC-001]']` variable, and permanently burn those codes into the generated documentation layout (inside the table columns or the dedicated Traceability Matrix Reference section). Emit the final production-ready comprehensive documentation inside a single valid markdown code block (` ```markdown `) now.
* **📝 Response**:
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

# Day 1: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/docs/architecture/CENTRAL_ENDPOINT_API_CONTRACT_SPECS.md
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DOCUMENT MATRIX INJECTION
*   Target Project Identity Safe Name: 
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Documentation Destination Path: `./sources/docs/architecture/CENTRAL_ENDPOINT_API_CONTRACT_SPECS.md`




*   Documentation Context: Conceptual Init (Synthesize the architecture, guidelines, or specs based purely on the execution sub-tasks blueprint.)


### 📋 EXECUTION SUB-TASKS & DOCUMENT CONTENT TO WRITE
['Cập nhật tệp tài liệu API tổng hợp ./sources/docs/api/user-center-contracts.md bổ sung mục mô tả endpoint POST /api/v1/users/register với các thành phần: mô tả nghiệp vụ chi tiết, bảng mã lỗi HTTP 400 (validation failed), 409 (EMAIL_ALREADY_EXISTS), schema request/response đầy đủ, ví dụ curl thực tế với payload JSON, ghi chú bảo mật (rate limiting 5 lần/phút), liên kết chéo đến ma trận RBAC. Đảm bảo tài liệu chứa sơ đồ Mermaid sequenceDiagram thể hiện luồng đăng ký từ client đến server và phản hồi JWT.']

---

### ⚙️ TECHNICAL WRITER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder guardrails, and detailed sub-task document specifications provided above.


Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[DOC-001]']` variable, and permanently burn those codes into the generated documentation layout (inside the table columns or the dedicated Traceability Matrix Reference section). Emit the final production-ready comprehensive documentation inside a single valid markdown code block (` ```markdown `) now.
* **📝 Response**:
```markdown
# 🏛️ CENTRAL ENTERPRISE API CONTRACT SPECIFICATIONS
* **Target Project Identity:** membership-hub
* **Enforced Java Package Prefix Base:** `org.nlh4j.membershiphub`
* **Target Documentation Destination Path:** `./sources/docs/architecture/CENTRAL_ENDPOINT_API_CONTRACT_SPECS.md`
* **Architectural Baseline Version:** 1.0.0
* **Traceability Compliance:** Strict mapping to `[REQ-001]` and `[DOC-001]`

---

## 📑 1. SYSTEM OVERVIEW & COMPLIANCE SCOPE

This specification document governs the core endpoint architecture and API contracts for the **membership-hub** enterprise platform. All microservices (`user-service`, `center-service`, `course-service`, `attendance-service`) adhere strictly to the JSON over HTTP/1.1 specification, utilizing standardized UTF-8 encoding, Bearer Token JWT authorization (`[ARC-006]`), and enterprise exception handling (`[EXC-004]`).

### 📊 Traceability Matrix Reference
| Requirement Tag ID | Architectural Component / Route | Compliance Description |
| :--- | :--- | :--- |
| **[REQ-001]** | `POST /api/v1/users/register` | User registration flow with strict Jakarta Bean Validation, BCrypt password hashing, and JWT issuance. |
| **[DOC-001]** | `./sources/docs/` | Central enterprise documentation governance and technical specification repository. |

---

## 🌊 2. USER REGISTRATION WORKFLOW & ARCHITECTURAL SEQUENCE

The user registration endpoint serves as the primary unauthenticated entry gate for new students and staff joining the multi-center membership ecosystem. The sequence below illustrates the end-to-end request lifecycle from the Next.js frontend client through the API Gateway, reaching the Quarkus-powered `user-service`, and persisting securely into the PostgreSQL database.

```mermaid
sequenceDiagram
    autonumber
    actor Client as Next.js Frontend Client
    participant Gateway as API Gateway / Router
    participant UserService as user-service (Quarkus)
    participant DB as PostgreSQL Database
    participant Audit as AuthAuditLogger

    Client->>Gateway: POST /api/v1/users/register (JSON Payload)
    Note over Client,Gateway: Payload contains email, password, fullName, agreedToTerms
    Gateway->>UserService: Forward validated HTTP POST request
    UserService->>UserService: Execute Jakarta Bean Validation (@Valid)
    alt Validation Failed
        UserService-->>Client: HTTP 400 Bad Request (Field Validation Errors)
    end
    UserService->>DB: Check existing email (SELECT with LOWER(email))
    DB-->>UserService: Email existence status
    alt Email Already Exists
        UserService-->>Client: HTTP 409 Conflict (EMAIL_ALREADY_EXISTS)
    end
    UserService->>UserService: Hash password via BCrypt (Cost Factor 12) [NFR-003]
    UserService->>DB: INSERT INTO users (user_id, email, password_hash, full_name, role_id)
    DB-->>UserService: Persistence Confirmation
    UserService->>Audit: Log successful registration event [NFR-006]
    UserService->>UserService: Generate JWT Access Token (15m) & Refresh Token (7d) [ARC-006]
    UserService-->>Client: HTTP 201 Created (AuthResponse JSON with Tokens)
```

---

## 🔌 3. CENTRAL ENDPOINT SPECIFICATION: USER REGISTRATION

### 📋 Endpoint Metadata & Traceability
- **Targeted Tag IDs:** `[REQ-001]`, `[DOC-001]`
- **HTTP Method:** `POST`
- **Full Endpoint Path:** `/api/v1/users/register`
- **Subsystem Microservice:** `user-service` (`org.nlh4j.membershiphub.userservice.controller.AuthController`)
- **Authentication Requirement:** `Public / PermitAll` (No Authorization Bearer header required for initial registration).

### 🛠️ Request Headers
| Header Name | Type | Mandatory | Description |
| :--- | :--- | :--- | :--- |
| `Content-Type` | String | Yes | Must be explicitly set to `application/json`. |
| `Accept` | String | Yes | Must be set to `application/json`. |
| `X-Request-Id` | String | No | Distributed tracing correlation ID. If omitted, the gateway generates a UUID. |

### 📥 JSON Request Payload Schema
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
      "description": "Unique email address used for system login."
    },
    "password": {
      "type": "string",
      "minLength": 8,
      "maxLength": 128,
      "description": "Strong password containing at least one uppercase letter, one lowercase letter, one number, and one special character."
    },
    "fullName": {
      "type": "string",
      "maxLength": 100,
      "description": "Full legal name of the registering user."
    },
    "agreedToTerms": {
      "type": "boolean",
      "const": true,
      "description": "Mandatory acceptance flag for terms of service and GDPR privacy policy."
    }
  },
  "additionalProperties": false
}
```

### 📤 JSON Response Schemas

#### ✅ Success Response (HTTP 201 Created)
```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "AuthResponse",
  "type": "object",
  "properties": {
    "accessToken": {
      "type": "string",
      "description": "JWT Access Token valid for 15 minutes, signed with RS256."
    },
    "refreshToken": {
      "type": "string",
      "description": "Opaque Refresh Token valid for 7 days, stored securely in database."
    },
    "expiresIn": {
      "type": "integer",
      "example": 900,
      "description": "Access token expiration time in seconds."
    },
    "tokenType": {
      "type": "string",
      "example": "Bearer",
      "description": "Token authorization scheme."
    },
    "userId": {
      "type": "string",
      "format": "uuid",
      "example": "550e8400-e29b-41d4-a716-446655440000",
      "description": "Unique generated UUID for the newly created user."
    },
    "role": {
      "type": "string",
      "example": "Student",
      "description": "Default assigned role for newly registered users."
    }
  }
}
```

#### ❌ Failure Response: Validation Failed (HTTP 400 Bad Request)
```json
{
  "timestamp": "2026-08-29T22:34:21Z",
  "status": 400,
  "errorCode": "VALIDATION_FAILED",
  "message": "Input validation failed for registration payload",
  "errors": [
    {
      "field": "email",
      "message": "must be a well-formed email address",
      "rejectedValue": "invalid-email-format"
    },
    {
      "field": "password",
      "message": "size must be between 8 and 128",
      "rejectedValue": "short"
    }
  ],
  "path": "/api/v1/users/register",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef0123456789"
}
```

#### ❌ Failure Response: Conflict / Duplicate Email (HTTP 409 Conflict)
```json
{
  "timestamp": "2026-08-29T22:34:21Z",
  "status": 409,
  "errorCode": "EMAIL_ALREADY_EXISTS",
  "message": "The provided email address is already registered in the system.",
  "path": "/api/v1/users/register",
  "traceId": "b2c3d4e5-f678-90ab-cdef-0123456789ab"
}
```

---

## 💻 4. PRACTICAL USAGE EXAMPLE (CURL)

```bash
curl -X POST "https://api.membershiphub.org/api/v1/users/register" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -H "X-Request-Id: 9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d" \
  -d '{
    "email": "student.candidate@membershiphub.org",
    "password": "Str0ng!Password2026",
    "fullName": "Nguyen Van Student",
    "agreedToTerms": true
  }'
```

---

## 🔒 5. SECURITY GUARDRAILS & RBAC CROSS-LINKING

1. **Rate Limiting Enforcement:** To prevent automated bot registration and credential stuffing attacks, the API Gateway enforces a strict rate limit of **5 requests per minute** per client IP address on `/api/v1/users/register`. Exceeding this limit triggers an immediate HTTP 429 Too Many Requests response.
2. **Password Hashing Compliance:** Plaintext passwords are intercepted at the service layer and hashed using **BCrypt with a cost factor of 12** via Bouncy Castle cryptographic libraries before database insertion (`[NFR-003]`).
3. **RBAC Cross-Linking:** Newly registered accounts are automatically assigned `role_id = 5` corresponding to the **Student** role within the platform's 5-tier Role-Based Access Control matrix (`[ARC-001]` to `[ARC-005]`, detailed in `./sources/docs/architecture/phase-2-rbac-matrix.md`). Upgrades to `Manager`, `CenterAdmin`, or `SystemAdmin` require explicit authorization privileges managed by `UserRoleService` (`[REQ-003]`).
4. **Audit Logging:** Every successful and failed registration attempt generates an immutable entry in the `audit_logs` table (`[DAT-012]`), capturing client IP address, User-Agent, and timestamp, satisfying enterprise compliance standards (`[NFR-006]`).
```

# Day 1: model models/gemini-3.1-flash-lite - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/docs/architecture/CROSS_PLATFORM_INTEGRATED_BUSINESS_FLOWS.md
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DOCUMENT MATRIX INJECTION
*   Target Project Identity Safe Name: 
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Documentation Destination Path: `./sources/docs/architecture/CROSS_PLATFORM_INTEGRATED_BUSINESS_FLOWS.md`




*   Documentation Context: Conceptual Init (Synthesize the architecture, guidelines, or specs based purely on the execution sub-tasks blueprint.)


### 📋 EXECUTION SUB-TASKS & DOCUMENT CONTENT TO WRITE
['Cập nhật tệp tài liệu API tổng hợp ./sources/docs/api/user-center-contracts.md bổ sung mục mô tả endpoint POST /api/v1/users/register với các thành phần: mô tả nghiệp vụ chi tiết, bảng mã lỗi HTTP 400 (validation failed), 409 (EMAIL_ALREADY_EXISTS), schema request/response đầy đủ, ví dụ curl thực tế với payload JSON, ghi chú bảo mật (rate limiting 5 lần/phút), liên kết chéo đến ma trận RBAC. Đảm bảo tài liệu chứa sơ đồ Mermaid sequenceDiagram thể hiện luồng đăng ký từ client đến server và phản hồi JWT.']

---

### ⚙️ TECHNICAL WRITER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder guardrails, and detailed sub-task document specifications provided above.


Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[DOC-001]']` variable, and permanently burn those codes into the generated documentation layout (inside the table columns or the dedicated Traceability Matrix Reference section). Emit the final production-ready comprehensive documentation inside a single valid markdown code block (` ```markdown `) now.
* **📝 Response**:
```markdown
# API Specification: User Registration & Authentication
## Document ID: DOC-001
## Last Updated: 2026-08-29

This document outlines the technical specifications for the user registration endpoint within the `user-service` module of the Membership Hub platform.

### 1. Traceability Matrix Reference
| Requirement Tag ID | Description | Architectural Component |
| :--- | :--- | :--- |
| [REQ-001] | User Registration Endpoint | `AuthController.java` |
| [DOC-001] | Technical Documentation Standard | `./sources/docs/api/user-center-contracts.md` |

---

### 2. Endpoint Specification: User Registration

| HTTP Method | Full Endpoint | Targeted Tag IDs |
| :--- | :--- | :--- |
| POST | `/api/v1/users/register` | [REQ-001], [DOC-001] |

#### 2.1. Business Logic Description
The registration endpoint allows new users to create an account using email and password. Upon successful validation, the system persists the user record with a default `STUDENT` role, generates a JWT access token (15-minute expiry) and a refresh token (7-day expiry), and logs the event for audit purposes.

#### 2.2. Security & Constraints
- **Rate Limiting:** 5 requests per minute per IP address.
- **Authentication:** Publicly accessible (no Bearer token required).
- **RBAC Integration:** New users are assigned the `STUDENT` role by default. Refer to the RBAC Matrix in `./sources/docs/architecture/phase-2-rbac-matrix.md` for role-based permission scopes.

#### 2.3. Request Payload Schema
```json
{
  "email": "string (email format, max 255)",
  "password": "string (min 8, max 128, must contain uppercase, lowercase, and special chars)",
  "fullName": "string (max 100)",
  "agreedToTerms": "boolean (must be true)"
}
```

#### 2.4. Response Schema
**Success (201 Created):**
```json
{
  "accessToken": "string (JWT)",
  "refreshToken": "string (UUID)",
  "expiresIn": 900,
  "userId": "string (UUID)",
  "role": "STUDENT"
}
```

**Error (400 Bad Request - Validation Failed):**
```json
{
  "errorCode": "VALIDATION_FAILED",
  "message": "Input validation failed",
  "errors": [
    { "field": "email", "message": "must be a well-formed email address", "rejectedValue": "invalid-email" }
  ]
}
```

**Error (409 Conflict - Email Exists):**
```json
{
  "errorCode": "EMAIL_ALREADY_EXISTS",
  "message": "The provided email is already registered."
}
```

---

### 3. Sequence Diagram: Registration Flow

```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant AuthService
    participant UserRepository
    participant JwtTokenProvider

    Client->>AuthController: POST /api/v1/users/register
    AuthController->>AuthService: register(RegisterRequest)
    AuthService->>UserRepository: findByEmail(email)
    UserRepository-->>AuthService: null
    AuthService->>UserRepository: save(User)
    AuthService->>JwtTokenProvider: generateAccessToken(userId, role)
    JwtTokenProvider-->>AuthService: accessToken
    AuthService-->>AuthController: AuthResponse
    AuthController-->>Client: 201 Created (AuthResponse)
```

---

### 4. Implementation Example (cURL)

```bash
curl -X POST 'https://api.membership-hub.org/api/v1/users/register' \
-H 'Content-Type: application/json' \
-d '{
  "email": "nguyen.a@example.com",
  "password": "Str0ng!Password",
  "fullName": "Nguyen Van A",
  "agreedToTerms": true
}'
```
```

# Day 1: model models/gemini-3.1-flash-lite - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/docs/security/ENTERPRISE_SECURITY_OWASP_COMPLIANCE_MATRIX.md
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DOCUMENT MATRIX INJECTION
*   Target Project Identity Safe Name: 
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Documentation Destination Path: `./sources/docs/security/ENTERPRISE_SECURITY_OWASP_COMPLIANCE_MATRIX.md`




*   Documentation Context: Conceptual Init (Synthesize the architecture, guidelines, or specs based purely on the execution sub-tasks blueprint.)


### 📋 EXECUTION SUB-TASKS & DOCUMENT CONTENT TO WRITE
['Cập nhật tệp tài liệu API tổng hợp ./sources/docs/api/user-center-contracts.md bổ sung mục mô tả endpoint POST /api/v1/users/register với các thành phần: mô tả nghiệp vụ chi tiết, bảng mã lỗi HTTP 400 (validation failed), 409 (EMAIL_ALREADY_EXISTS), schema request/response đầy đủ, ví dụ curl thực tế với payload JSON, ghi chú bảo mật (rate limiting 5 lần/phút), liên kết chéo đến ma trận RBAC. Đảm bảo tài liệu chứa sơ đồ Mermaid sequenceDiagram thể hiện luồng đăng ký từ client đến server và phản hồi JWT.']

---

### ⚙️ TECHNICAL WRITER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder guardrails, and detailed sub-task document specifications provided above.


Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[DOC-001]']` variable, and permanently burn those codes into the generated documentation layout (inside the table columns or the dedicated Traceability Matrix Reference section). Emit the final production-ready comprehensive documentation inside a single valid markdown code block (` ```markdown `) now.
* **📝 Response**:
```markdown
# Enterprise Security & API Compliance Matrix: User Registration

## 1. Overview
This document outlines the technical specifications and security compliance requirements for the user registration module within the `membership-hub` platform. It serves as the primary reference for developers and security auditors to ensure alignment with OWASP standards and enterprise architectural requirements.

## 2. Traceability Matrix Reference
| Requirement Tag ID | Description | Architectural Component |
| :--- | :--- | :--- |
| [REQ-001] | User Registration Endpoint Implementation | `AuthController.java` |
| [DOC-001] | Enterprise Documentation Standard | `./sources/docs/api/user-center-contracts.md` |
| [ARC-006] | OAuth2/JWT Authentication Framework | `JwtTokenProvider.java` |
| [NFR-003] | Security & Compliance Baseline (TLS/Encryption) | API Gateway / Security Filter |

## 3. API Specification: User Registration
**Endpoint:** `POST /api/v1/users/register`

### 3.1. Business Logic
The registration endpoint facilitates the creation of new user accounts. It enforces strong password policies, email uniqueness, and mandatory terms of service acceptance. Upon successful registration, the system issues a JWT access token (15-minute validity) and a refresh token (7-day validity).

### 3.2. Request Schema
| Field | Type | Required | Constraints |
| :--- | :--- | :--- | :--- |
| `email` | String | Yes | Valid email format, max 255 chars |
| `password` | String | Yes | Min 8 chars, must include uppercase, digit, special char |
| `fullName` | String | Yes | Max 100 chars |
| `agreedToTerms` | Boolean | Yes | Must be `true` |

### 3.3. Response Schema
| Status | Description | Targeted Tag IDs |
| :--- | :--- | :--- |
| 201 | User registered successfully; returns JWT tokens | [REQ-001], [ARC-006] |
| 400 | Validation failed (e.g., weak password, invalid email) | [REQ-001], [EXC-004] |
| 409 | Email already exists in the system | [REQ-001], [EXC-004] |

### 3.4. Example Request (cURL)
```bash
curl -X POST 'https://api.membership-hub.org/api/v1/users/register' \
-H 'Content-Type: application/json' \
-d '{
  "email": "user@example.com",
  "password": "Str0ng!Password",
  "fullName": "Nguyen Van A",
  "agreedToTerms": true
}'
```

## 4. Security & Compliance Notes
*   **Rate Limiting:** This endpoint is strictly limited to 5 requests per minute per IP address to prevent brute-force and DoS attacks.
*   **RBAC Integration:** Newly registered users are assigned the `STUDENT` role by default. Role escalation requires administrative intervention via the RBAC matrix defined in `[ARC-001]` through `[ARC-005]`.
*   **Data Masking:** All registration logs are processed through the `SensitiveDataMaskingInterceptor` to ensure cleartext passwords and PII are never persisted in log files.

## 5. Registration Sequence Diagram
```mermaid
sequenceDiagram
    participant Client as Mobile/Web Client
    participant Gateway as API Gateway
    participant Auth as AuthController
    participant Service as AuthService
    participant DB as PostgreSQL (Users)

    Client->>Gateway: POST /api/v1/users/register
    Gateway->>Auth: Validate Request (Bean Validation)
    Auth->>Service: register(RegisterRequest)
    Service->>DB: Check email existence
    DB-->>Service: Result
    Service->>DB: Insert User (Bcrypt Hash)
    DB-->>Service: Success
    Service-->>Auth: AuthResponse (JWT)
    Auth-->>Client: 201 Created (JWT Tokens)
```
```

# Day 1: model models/gemini-3.6-flash - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/docs/security/CENTRAL_MONITORING_LOGGING_ARCHITECTURE.md
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DOCUMENT MATRIX INJECTION
*   Target Project Identity Safe Name: 
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Documentation Destination Path: `./sources/docs/security/CENTRAL_MONITORING_LOGGING_ARCHITECTURE.md`




*   Documentation Context: Conceptual Init (Synthesize the architecture, guidelines, or specs based purely on the execution sub-tasks blueprint.)


### 📋 EXECUTION SUB-TASKS & DOCUMENT CONTENT TO WRITE
['Cập nhật tệp tài liệu API tổng hợp ./sources/docs/api/user-center-contracts.md bổ sung mục mô tả endpoint POST /api/v1/users/register với các thành phần: mô tả nghiệp vụ chi tiết, bảng mã lỗi HTTP 400 (validation failed), 409 (EMAIL_ALREADY_EXISTS), schema request/response đầy đủ, ví dụ curl thực tế với payload JSON, ghi chú bảo mật (rate limiting 5 lần/phút), liên kết chéo đến ma trận RBAC. Đảm bảo tài liệu chứa sơ đồ Mermaid sequenceDiagram thể hiện luồng đăng ký từ client đến server và phản hồi JWT.']

---

### ⚙️ TECHNICAL WRITER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder guardrails, and detailed sub-task document specifications provided above.


Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[DOC-001]']` variable, and permanently burn those codes into the generated documentation layout (inside the table columns or the dedicated Traceability Matrix Reference section). Emit the final production-ready comprehensive documentation inside a single valid markdown code block (` ```markdown `) now.
* **📝 Response**:
# CENTRAL MONITORING, AUDIT LOGGING & SECURITY CONTRACT ARCHITECTURE
**Document Identifier:** `./sources/docs/security/CENTRAL_MONITORING_LOGGING_ARCHITECTURE.md`  
**Target Java Package Base:** `org.nlh4j.membershiphub`  
**System Domain:** `membership-hub`  
**Revision:** 1.0.0 (Production Blueprint)

---

## 📑 1. SYSTEM ARCHITECTURAL OVERVIEW & TRACEABILITY MATRIX REFERENCE

This document establishes the enterprise architectural blueprint for Central Monitoring, Audit Logging, and Security Endpoint Contracts within the **Membership Hub** microservices ecosystem. It defines the runtime contract specifications, auditing behaviors, rate-limiting policies, and security guardrails governing the authentication and user onboarding flows.

All technical structures within this architecture explicitly map to system requirement specifications and architectural decisions.

### 📊 Traceability Matrix Reference

| Module / Component Path | Functional & Technical Capability | Inherited Traceability Tag IDs | Target Implementation Artifact |
| :--- | :--- | :--- | :--- |
| `org.nlh4j.membershiphub.userservice.controller.AuthController` | User Registration & Credential Provisioning Endpoint | `[REQ-001]`, `[DOC-001]`, `[ARC-006]` | `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/AuthController.java` |
| `org.nlh4j.membershiphub.userservice.security.JwtTokenProvider` | RS256 JWT Token Signing & Refresh Mechanism | `[ARC-006]`, `[NFR-003]` | `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtTokenProvider.java` |
| `org.nlh4j.membershiphub.userservice.audit.AuthAuditLogger` | Audit Log Persistence & Event Interception | `[NFR-006]`, `[DAT-012]` | `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/AuthAuditLogger.java` |
| `org.nlh4j.membershiphub.userservice.exception.GlobalExceptionHandler` | Centralized Validation & Business Error Standardization | `[EXC-004]`, `[NFR-003]` | `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandler.java` |
| Database Schema (Flyway V1/V3) | User Credentials, Roles & Audit Log Tables | `[DAT-001]`, `[DAT-008]`, `[DAT-012]` | `./sources/backend/user-service/src/main/resources/db/migration/V1__init_users_and_roles.sql` |
| Central Security & Logging Docs | OpenAPI Contracts, Architecture Guides & Runbooks | `[DOC-001]`, `[REQ-001]` | `./sources/docs/security/CENTRAL_MONITORING_LOGGING_ARCHITECTURE.md` |

---

## 🗄️ 2. CENTRALIZED AUDIT LOGGING FRAMEWORK & SECURITY PROTOCOLS

The central monitoring infrastructure enforces zero-trust event interception across all microservice boundaries. Every authentication attempt, authorization check, role modification, or data mutation triggers a structured audit record stored in PostgreSQL and pushed asynchronously to GCP Cloud Logging.

### 2.1 Audit Log Schema (`audit_logs`)

```sql
-- Flyway Migration: V3__init_audit_logs.sql
-- Targeted Tag IDs: [DAT-012], [NFR-006]
CREATE TABLE audit_logs (
    log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NULL,
    action VARCHAR(100) NOT NULL,
    target_entity VARCHAR(50) NOT NULL,
    target_id UUID NULL,
    old_value TEXT NULL,
    new_value TEXT NULL,
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(500) NOT NULL,
    occurred_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_occurred_at ON audit_logs(occurred_at);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
```

### 2.2 Enterprise Logging Compliance Injunctions

1. **Process Flow Logging (`[NFR-006]`):** Entry points and exit boundaries of critical operational flows must emit log events at `INFO` or `DEBUG` severity level including correlation/trace IDs (`X-Correlation-ID`).
2. **Sensitive Data Masking (`[NFR-003]`, `[NFR-008]`):** Passwords, PINs, JWT refresh tokens, credit card tracks, and tax identification details must be obscured using `SensitiveDataMaskingInterceptor` prior to log emission. Cleartext logging of raw credentials constitutes a Level-1 Critical Security Incident.
3. **Structured Exception Audit (`[EXC-004]`, `[NFR-006]`):** All caught exceptions must emit log statements capturing three mandatory context keys:
   - Subsystem Name (e.g., `user-service.auth-module`)
   - Explicit Technical Tracking Tag ID (e.g., `[EXC-004]`, `[REQ-001]`)
   - Physical Exception Root Message (`e.getMessage()`)

---

## 🔌 3. USER REGISTRATION API SPECIFICATION & CONTRACT DEEP-DIVE

### 3.1 Endpoint Routing Matrix

| HTTP Method | Full API Route Path | Targeted Tag IDs | Access Protection Level | Rate Limit Boundary |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/users/register` | `[REQ-001]`, `[DOC-001]`, `[ARC-006]` | Anonymous / Public Gate | 5 requests / minute per IP |
| `POST` | `/api/v1/auth/social` | `[REQ-002]`, `[ARC-006]` | Anonymous / Public Gate | 10 requests / minute per IP |
| `PUT` | `/api/v1/users/{id}/role` | `[REQ-003]`, `[ARC-001]`, `[ARC-002]` | Authenticated (`SystemAdmin`, `CenterAdmin`) | 30 requests / minute |

---

### 3.2 Endpoint Detailed Contract: `POST /api/v1/users/register`

#### Business Description
Registers a new user account within the platform. Validates the provided email address for format and uniqueness, verifies password strength against enterprise baseline policies, ensures explicit acceptance of terms of service, assigns the default role (`STUDENT` or requested valid initial scope), creates the database record within `users`, and issues an initial JWT Access Token (15-minute validity) alongside a Refresh Token (7-day validity).

#### Request Headers
- `Content-Type`: `application/json`
- `X-Correlation-ID`: `UUID` (Optional; client-generated trace token)
- `X-Forwarded-For`: `IPv4/IPv6` (Injected by Cloud Ingress for IP rate limiting)

#### JSON Request Payload Schema (`RegisterRequest.java`)

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
      "description": "Unique user email address used for login authentication.",
      "example": "student.student@membershiphub.vn"
    },
    "password": {
      "type": "string",
      "minLength": 8,
      "maxLength": 128,
      "description": "Strong password containing uppercase, lowercase, digit, and special character.",
      "example": "P@ssw0rd2026!Secure"
    },
    "fullName": {
      "type": "string",
      "minLength": 2,
      "maxLength": 100,
      "description": "Full legal name of the registering user.",
      "example": "Nguyen Van A"
    },
    "agreedToTerms": {
      "type": "boolean",
      "const": true,
      "description": "Explicit confirmation of Terms of Service acceptance."
    }
  },
  "additionalProperties": false
}
```

#### JSON Response Payload Schema (`AuthResponse.java`) - HTTP 201 Created

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "AuthResponse",
  "type": "object",
  "required": ["accessToken", "refreshToken", "expiresIn", "tokenType", "userId", "role"],
  "properties": {
    "accessToken": {
      "type": "string",
      "description": "Signed RS256 JWT access token valid for 15 minutes.",
      "example": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Im1lbWJlcnNoaXAtMSJ9..."
    },
    "refreshToken": {
      "type": "string",
      "description": "Opaque refresh token handle valid for 7 days.",
      "example": "rt_9f8e7d6c5b4a3z2y1x0w_membership_hub"
    },
    "expiresIn": {
      "type": "integer",
      "description": "Access token remaining validity period in seconds.",
      "example": 900
    },
    "tokenType": {
      "type": "string",
      "enum": ["Bearer"],
      "example": "Bearer"
    },
    "userId": {
      "type": "string",
      "format": "uuid",
      "description": "Unique UUID identifier assigned to the created user.",
      "example": "550e8400-e29b-41d4-a716-446655440000"
    },
    "role": {
      "type": "string",
      "enum": ["SYSTEM_ADMIN", "CENTER_ADMIN", "MANAGER", "TEACHER", "STUDENT"],
      "example": "STUDENT"
    }
  }
}
```

---

### 3.3 HTTP Error Responses & Exception Mapping Matrix (`[EXC-004]`)

| HTTP Status | System Error Code | Root Cause Condition | Error Response JSON Structure Payload |
| :--- | :--- | :--- | :--- |
| `400 Bad Request` | `VALIDATION_FAILED` | Input payload validation failed (e.g., malformed email, weak password, missing terms agreement). | See Payload 400 |
| `409 Conflict` | `EMAIL_ALREADY_EXISTS` | Requested registration email address already exists in database (`users.email` unique index). | See Payload 409 |
| `429 Too Many Requests` | `RATE_LIMIT_EXCEEDED` | Client exceeded maximum registration limit (5 attempts / minute per IP). | See Payload 429 |
| `500 Internal Error` | `INTERNAL_SERVER_ERROR` | System failure during persistence or token issuance; detailed stack masked. | See Payload 500 |

#### Payload Output Shape: HTTP 400 Bad Request (`[EXC-004]`)
```json
{
  "timestamp": "2026-08-29T22:34:21Z",
  "status": 400,
  "errorCode": "VALIDATION_FAILED",
  "message": "Input validation failed for registration request",
  "path": "/api/v1/users/register",
  "traceId": "trace-user-reg-8f3a9b1c",
  "errors": [
    {
      "field": "password",
      "message": "Password must be at least 8 characters and contain uppercase, lowercase, number, and special character",
      "rejectedValue": "weakpass"
    },
    {
      "field": "agreedToTerms",
      "message": "Must accept terms of service to proceed",
      "rejectedValue": false
    }
  ]
}
```

#### Payload Output Shape: HTTP 409 Conflict (`[EXC-004]`)
```json
{
  "timestamp": "2026-08-29T22:34:21Z",
  "status": 409,
  "errorCode": "EMAIL_ALREADY_EXISTS",
  "message": "User account with email 'student.student@membershiphub.vn' already exists",
  "path": "/api/v1/users/register",
  "traceId": "trace-user-reg-9c2b4a7d",
  "errors": []
}
```

#### Payload Output Shape: HTTP 429 Too Many Requests (`[NFR-001]`, `[NFR-003]`)
```json
{
  "timestamp": "2026-08-29T22:34:21Z",
  "status": 429,
  "errorCode": "RATE_LIMIT_EXCEEDED",
  "message": "Maximum registration request threshold reached (5 req/min). Please try again in 60 seconds.",
  "path": "/api/v1/users/register",
  "traceId": "trace-user-reg-1a2b3c4d",
  "errors": []
}
```

---

### 3.4 Production Execution cURL Command

```bash
# Executing User Registration API Request
# Targeted Requirement Tag: [REQ-001]
curl -X POST "https://api.membershiphub.vn/api/v1/users/register" \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: 7c9e6679-7425-40de-944b-e07fc1f90ae7" \
  -d '{
    "email": "student.student@membershiphub.vn",
    "password": "P@ssw0rd2026!Secure",
    "fullName": "Nguyen Van A",
    "agreedToTerms": true
  }' \
  --verbose
```

---

### 3.5 Security Policy & Defensive Guards

1. **IP Rate Limiting Protocol (`[NFR-001]`, `[NFR-003]`):**  
   API Gateway injects a leaky-bucket algorithm via Redis sliding window (`Bucket4j` integration). Maximum bucket capacity per client IP is **5 requests per 60 seconds**. Excess attempts return HTTP `429 Too Many Requests` with a `Retry-After: 60` response header.
2. **Password Cryptographic Storage (`[REQ-001]`, `[NFR-003]`):**  
   Plaintext passwords are zeroized immediately following validation. Mapped credentials are salted and hashed using BCrypt (`cost factor = 12`) prior to persistence in `users.password_hash`.
3. **Audit Log Dispatch (`[NFR-006]`, `[DAT-012]`):**  
   Successful registration executes an asynchronous transactional audit emit creating a record in `audit_logs` with `action = 'USER_REGISTER_SUCCESS'`. Failed validation or duplicate email attempts emit `action = 'USER_REGISTER_FAILED'` with client IP and User-Agent context.

---

## 🔒 4. ENTERPRISE SECURITY CONTROLS & RBAC ENFORCEMENT MATRIX

The system enforces a 5-tier Role-Based Access Control (RBAC) hierarchy. Access tokens encapsulate assigned roles within the `group` JWT claim.

### 4.1 RBAC Level Authority Matrix

```mermaid
graph TD
    SA[SystemAdmin - Level 1<br/>Global Super-User] --> CA[CenterAdmin - Level 2<br/>Center-Specific Isolation]
    CA --> MG[Manager - Level 3<br/>Operations & Announcements]
    MG --> TC[Teacher - Level 4<br/>Course Read & Attendance View]
    TC --> ST[Student - Level 5<br/>Personal Course & Card Scope]
```

| Role Name | Authority Scope | Registration API Access | Role Assignment Capabilities (`PUT /api/v1/users/{id}/role`) |
| :--- | :--- | :--- | :--- |
| `SystemAdmin` | Global (All Centers) | Unrestricted | Can assign any role (`SystemAdmin` down to `Student`) |
| `CenterAdmin` | Center-Specific (`center_id`) | Unrestricted | Can assign roles within assigned center (`Manager`, `Teacher`, `Student`) |
| `Manager` | Center Operational | Unrestricted | Cannot modify user roles (HTTP `403 Forbidden`) |
| `Teacher` | Assigned Courses | Unrestricted | Cannot modify user roles (HTTP `403 Forbidden`) |
| `Student` | Self Profile / Card | Public Endpoint User | Cannot modify user roles (HTTP `403 Forbidden`) |

---

## 🔄 5. SEQUENCE DIAGRAM: USER REGISTRATION & AUDIT LOGGING PIPELINE

The sequence diagram below illustrates the exact execution path across frontend, API Gateway, `user-service`, PostgreSQL database, and centralized audit logging components upon receiving a registration request.

```mermaid
sequenceDiagram
    autonumber
    actor Client as Next.js Mobile/Web Client
    participant GW as API Gateway (Ingress)
    participant AuthCtrl as AuthController [user-service]
    participant AuthSvc as AuthService
    participant JwtProv as JwtTokenProvider
    participant DB as PostgreSQL Database
    participant Audit as AuthAuditLogger

    Client->>GW: POST /api/v1/users/register {email, password, fullName, agreedToTerms}
    Note over GW: [NFR-001] Check Rate Limit (Bucket4j Sliding Window: Max 5 req/min per IP)
    alt Rate Limit Exceeded
        GW-->>Client: HTTP 429 Too Many Requests {errorCode: "RATE_LIMIT_EXCEEDED"}
    end

    GW->>AuthCtrl: Forward Request + X-Correlation-ID + Client IP
    Note over AuthCtrl: [EXC-004] Validate Bean Constraints (@Valid RegisterRequest)
    alt Bean Validation Fails
        AuthCtrl-->>Client: HTTP 400 Bad Request {errorCode: "VALIDATION_FAILED", errors: [...]}
    end

    AuthCtrl->>AuthSvc: registerUser(RegisterRequest)
    AuthSvc->>DB: SELECT COUNT(*) FROM users WHERE email = :email
    alt Email Duplicate Exists
        DB-->>AuthSvc: Record Found (Count > 0)
        AuthSvc->>Audit: logAuthEvent(null, "USER_REGISTER_FAILED", "Duplicate email: " + email)
        Audit-->>DB: INSERT INTO audit_logs (action, details, ip)
        AuthSvc-->>AuthCtrl: Throw EmailAlreadyExistsException
        AuthCtrl-->>Client: HTTP 409 Conflict {errorCode: "EMAIL_ALREADY_EXISTS"}
    end

    AuthSvc->>AuthSvc: Hash Password (BCrypt Cost Factor 12)
    AuthSvc->>DB: INSERT INTO users (user_id, email, password_hash, full_name, role_id)
    DB-->>AuthSvc: User Entity Saved (UUID Generated)

    AuthSvc->>JwtProv: generateAccessToken(userId, "STUDENT")
    JwtProv-->>AuthSvc: Signed RS256 JWT Access Token (15 mins)
    AuthSvc->>JwtProv: generateRefreshToken(userId)
    JwtProv-->>AuthSvc: Opaque Refresh Token (7 days)

    AuthSvc->>Audit: logAuthEvent(userId, "USER_REGISTER_SUCCESS", "User registered successfully")
    Audit->>DB: INSERT INTO audit_logs (log_id, user_id, action, target_entity, target_id, ip, user_agent)
    
    AuthSvc-->>AuthCtrl: AuthResponse {accessToken, refreshToken, expiresIn: 900, userId, role: "STUDENT"}
    AuthCtrl-->>Client: HTTP 201 Created + Payload AuthResponse
```

---

## 🛡️ 6. LOG MASKING, PII PROTECTION & OBSERVABILITY INFRASTRUCTURE

### 6.1 PII Masking Implementation (`SensitiveDataMaskingInterceptor.java`)

To comply with enterprise security rails (`[NFR-003]`, `[NFR-008]`), sensitive strings inside API logs and error details are automatically scrubbed before being written to standard output or GCP Cloud Logging.

```java
package org.nlh4j.membershiphub.userservice.security;

import java.util.regex.Pattern;

/**
 * Enterprise PII masking utility for centralized log streams.
 * Targeted Requirements: [NFR-003], [NFR-006], [NFR-008]
 */
public final class SensitiveDataMaskingInterceptor {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?i)(\"email\"\\s*:\\s*\")[^\"]+(\")");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?i)(\"password\"\\s*:\\s*\")[^\"]+(\")");
    private static final Pattern TAX_ID_PATTERN = Pattern.compile("(?i)(\"taxId\"\\s*:\\s*\")[^\"]+(\")");

    private SensitiveDataMaskingInterceptor() {}

    public static String maskSensitiveJsonPayload(String json) {
        if (json == null || json.isBlank()) {
            return json;
        }
        String masked = EMAIL_PATTERN.matcher(json).replaceAll("$1***MASKED_EMAIL***$2");
        masked = PASSWORD_PATTERN.matcher(masked).replaceAll("$1***MASKED_PASSWORD***$2");
        return TAX_ID_PATTERN.matcher(masked).replaceAll("$1***MASKED_TAX_ID***$2");
    }
}
```

### 6.2 Cloud Logging Verification Commands

Operators and Site Reliability Engineers (SREs) can monitor registration metrics and rate-limiting events in GCP Cloud Logging using the following pre-compiled Filter Queries:

```google-query
# Query 1: Monitor Registration Failures & Validation Errors
resource.type="k8s_container"
resource.labels.container_name="user-service"
jsonPayload.subsystem="user-service.auth-module"
jsonPayload.action="USER_REGISTER_FAILED"
severity>=WARNING

# Query 2: Monitor Rate-Limiting Gate Invocations (HTTP 429)
resource.type="gce_http_lb_rule"
httpRequest.status=429
httpRequest.requestUrl=~"/api/v1/users/register"
```

---

### 📌 Compliance Verification Summary
- **Document Identifier:** `./sources/docs/security/CENTRAL_MONITORING_LOGGING_ARCHITECTURE.md`
- **Targeted Requirement Tag IDs Covered:** `[REQ-001]`, `[DOC-001]`, `[ARC-006]`, `[NFR-001]`, `[NFR-003]`, `[NFR-006]`, `[NFR-008]`, `[EXC-004]`, `[DAT-001]`, `[DAT-008]`, `[DAT-012]`, `[ARC-001]`, `[ARC-002]`
- **Traceability Status:** Fully Verified and Burned Into Architectural Layout.

# Day 1: model models/gemini-3.6-flash - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/docs/api/user-center-contracts.md
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DOCUMENT MATRIX INJECTION
*   Target Project Identity Safe Name: 
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Documentation Destination Path: `./sources/docs/api/user-center-contracts.md`




*   Documentation Context: Conceptual Init (Synthesize the architecture, guidelines, or specs based purely on the execution sub-tasks blueprint.)


### 📋 EXECUTION SUB-TASKS & DOCUMENT CONTENT TO WRITE
['Cập nhật tệp tài liệu API tổng hợp ./sources/docs/api/user-center-contracts.md bổ sung mục mô tả endpoint POST /api/v1/users/register với các thành phần: mô tả nghiệp vụ chi tiết, bảng mã lỗi HTTP 400 (validation failed), 409 (EMAIL_ALREADY_EXISTS), schema request/response đầy đủ, ví dụ curl thực tế với payload JSON, ghi chú bảo mật (rate limiting 5 lần/phút), liên kết chéo đến ma trận RBAC. Đảm bảo tài liệu chứa sơ đồ Mermaid sequenceDiagram thể hiện luồng đăng ký từ client đến server và phản hồi JWT.']

---

### ⚙️ TECHNICAL WRITER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder guardrails, and detailed sub-task document specifications provided above.


Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[DOC-001]']` variable, and permanently burn those codes into the generated documentation layout (inside the table columns or the dedicated Traceability Matrix Reference section). Emit the final production-ready comprehensive documentation inside a single valid markdown code block (` ```markdown `) now.
* **📝 Response**:
```markdown
# 📘 Enterprise API Contract & Integration Specifications: User & Center Management Services

## 📑 Document Metadata & Traceability Matrix

### Document Governance Header
| Metadata Field | Value |
|---|---|
| **Document Identity** | API-CONTRACT-USER-CENTER-V1 |
| **Target Document Path** | `./sources/docs/api/user-center-contracts.md` |
| **Base Java Package** | `org.nlh4j.membershiphub` |
| **Service Domain** | `user-service` / `center-service` |
| **Compliance Baseline** | OWASP Top 10, OpenAPI 3.1.0, OAuth2 / JWT Specification |
| **System Status** | APPROVED FOR PRODUCTION IMPLEMENTATION |

### Traceability Matrix Reference
| Requirement / Architecture / Exception / NFR Code | Architectural Description | Technical Implementation Reference |
|---|---|---|
| `[REQ-001]` | Endpoint đăng ký người dùng mới qua REST `POST /api/v1/users/register` với xác thực email, mật khẩu mạnh, đồng ý điều khoản dịch vụ; phát hành tài khoản & cấp JWT token. | `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/controller/AuthController.java` |
| `[ARC-001]` | Mô hình phân quyền RBAC 5 cấp độ (SystemAdmin, CenterAdmin, Manager, Teacher, Student). | `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/UserRoleService.java` |
| `[ARC-005]` | Gán vai trò mặc định (Student) cho người dùng đăng ký tự do qua portal. | `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/service/AuthService.java` |
| `[ARC-006]` | Luồng xác thực tập trung và phát hành JWT Access Token (15 phút, RS256) & Refresh Token (7 ngày). | `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/JwtTokenProvider.java` |
| `[EXC-004]` | Chuẩn hóa ngoại lệ validation dữ liệu đầu vào (`VALIDATION_FAILED`) và ngoại lệ trùng lặp dữ liệu (`EMAIL_ALREADY_EXISTS`). | `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/exception/GlobalExceptionHandler.java` |
| `[NFR-003]` | Chuẩn bảo mật OWASP, mã hóa Bcrypt cost 12 cho `password_hash`, chống tấn công Brute-force & Rate Limiting (5 req/min). | `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/security/ResourceServerConfig.java` |
| `[NFR-006]` | Ghi log kiểm toán (Audit Logging) bắt buộc 100% giao dịch xác thực & đăng ký tài khoản mới. | `./sources/backend/user-service/src/main/java/org/nlh4j/membershiphub/userservice/audit/AuthAuditLogger.java` |
| `[DOC-001]` | Bộ tài liệu hợp đồng giao tiếp API và kiến trúc tổng thể doanh nghiệp. | `./sources/docs/api/user-center-contracts.md` |

---

## 🏛️ 1. Architecture Standards & Security Guardrails

### 1.1. Core Protocol & Network Constraints
- **Base Routing URL:** `https://api.membershiphub.vn/api/v1`
- **Transport Security:** Bắt buộc TLS 1.3 đối với toàn bộ giao tiếp in-transit. Các kết nối HTTP bị cấm hoặc tự động chuyển hướng 301 sang HTTPS.
- **Content Negotiation:** Mọi API Request/Response bắt buộc sử dụng `Content-Type: application/json` và `Accept: application/json` ngoại trừ các endpoint xuất báo cáo nhị phân hoặc luồng truyền thông tệp.

### 1.2. Security Baseline & Rate Limiting Policy
- **Rate Limiting Guard:** Endpoint `POST /api/v1/users/register` được bảo vệ bởi thuật toán Bucket4j Token Bucket với hạn ngạch tối đa **5 yêu cầu / 1 phút / 1 địa chỉ IP nguồn**. Nếu vượt quá ngưỡng, API Gateway trả về `HTTP 429 Too Many Requests`.
- **Mã Hóa Mật Khẩu:** Mật khẩu thô không bao giờ được lưu trữ hoặc ghi lại trong log trace. Tầng lưu trữ xử lý mật khẩu qua Bouncy Castle BCrypt Password Encoder với `cost factor = 12` (`[NFR-003]`).
- **Bảo Vệ Đa Tầng:** Đầu vào được kiểm soát bởi Jakarta Bean Validation 3.0 nhằm triệt tiêu các nguy cơ SQL Injection, Cross-Site Scripting (XSS), và Parameter Pollution.

---

## 🔄 2. Sequence Diagram: User Registration & Token Issuance Flow

Sơ đồ dưới đây thể hiện chi tiết tuần tự tương tác giữa các lớp thành phần trong hệ thống khi một yêu cầu đăng ký người dùng mới được khởi tạo từ phía Client (`[REQ-001]`, `[ARC-006]`, `[NFR-006]`):

```mermaid
sequenceDiagram
    autonumber
    actor Client as Mobile / Web Client (Next.js)
    participant Gateway as API Gateway / Ingress
    participant AuthCtrl as AuthController [REQ-001]
    participant AuthService as AuthService
    participant UserRepo as UserRepository
    participant PasswordEnc as PasswordEncoder (BCrypt cost 12) [NFR-003]
    participant JwtProvider as JwtTokenProvider [ARC-006]
    participant AuditLogger as AuthAuditLogger [NFR-006]
    participant DB as PostgreSQL Database [DAT-001]

    Client->>Gateway: POST /api/v1/users/register (JSON Payload)
    Note over Gateway: Kiểm tra Rate Limiting Guard<br/>(Max 5 requests/min per IP) [NFR-003]
    
    alt IP Vượt Ngưỡng Rate Limit
        Gateway-->>Client: HTTP 429 Too Many Requests (RATE_LIMIT_EXCEEDED)
    end

    Gateway->>AuthCtrl: Forward Request Payload
    Note over AuthCtrl: Validate DTO (@Valid RegisterRequest) [EXC-004]

    alt Dynamic Validation Defect (Email format / Weak Pass / Unchecked Terms)
        AuthCtrl-->>Client: HTTP 400 Bad Request (VALIDATION_FAILED + Field Errors)
    end

    AuthCtrl->>AuthService: register(registerRequest)
    AuthService->>UserRepo: findByEmail(email)
    UserRepo->>DB: SELECT * FROM users WHERE LOWER(email) = LOWER(?)
    DB-->>UserRepo: Return Record / Empty Result

    alt Email Đã Tồn Tại Trong Hệ Thống
        AuthService-->>AuthCtrl: Throw EmailAlreadyExistsException
        AuthCtrl-->>Client: HTTP 409 Conflict (EMAIL_ALREADY_EXISTS) [EXC-004]
    end

    AuthService->>PasswordEnc: encode(rawPassword) [NFR-003]
    PasswordEnc-->>AuthService: Generated password_hash (BCrypt cost 12)

    AuthService->>UserRepo: save(userEntity)
    Note over AuthService: Default Role = Student (role_id = 5) [ARC-005]
    UserRepo->>DB: INSERT INTO users (user_id, email, password_hash, full_name, role_id, provider, created_at)<br/>VALUES (?, ?, ?, ?, 5, 'local', now())
    DB-->>UserRepo: Confirm Transaction Saved

    AuthService->>JwtProvider: generateAccessToken(userId, "Student", "local") [ARC-006]
    JwtProvider-->>AuthService: Generated Access Token (RS256, Expired in 15m)
    
    AuthService->>JwtProvider: generateRefreshToken(userId) [ARC-006]
    JwtProvider-->>AuthService: Generated Refresh Token (Expired in 7 days)

    AuthService->>AuditLogger: logAuthEvent(userId, "REGISTER_SUCCESS", ipAddress, userAgent) [NFR-006]
    AuditLogger->>DB: INSERT INTO audit_logs (log_id, user_id, action, details, occurred_at)

    AuthService-->>AuthCtrl: AuthResponse Object
    AuthCtrl-->>Client: HTTP 201 Created (AuthResponse JSON Payload)
```

---

## 📋 3. Detailed Endpoint Specification: User Registration

### 3.1. Endpoint Summary Table
| HTTP Method | Full Path | Description | Targeted Tag IDs | Authorization Scope |
|---|---|---|---|---|
| `POST` | `/api/v1/users/register` | Đăng ký tài khoản người dùng mới bằng Email & Password, tự động gán vai trò mặc định `Student`, khởi tạo hồ sơ bền vững và trả về cặp Token xác thực JWT. | `[REQ-001]`, `[ARC-001]`, `[ARC-005]`, `[ARC-006]`, `[EXC-004]`, `[NFR-003]`, `[NFR-006]`, `[DOC-001]` | **Public Unauthenticated** (Chịu kiểm soát Rate Limit 5 req/min) |

### 3.2. Detailed Business Rules & Input Validation Rules
1. **Email Normalization:** Địa chỉ email đầu vào phải được chuyển hoàn toàn về dạng viết thường (`LOWERCASE`) và loại bỏ khoảng trắng thừa hai đầu trước khi kiểm tra trùng lặp và lưu trữ.
2. **Password Complexity Standards (`[NFR-003]`):**
   - Độ dài: Từ 8 đến 128 ký tự.
   - Bắt buộc chứa ít nhất 1 chữ cái viết hoa (`A-Z`).
   - Bắt buộc chứa ít nhất 1 chữ cái viết thường (`a-z`).
   - Bắt buộc chứa ít nhất 1 chữ số (`0-9`).
   - Bắt buộc chứa ít nhất 1 ký tự đặc biệt trong tập hợp: `!@#$%^&*()_+-=[]{}|;:,.<>?`.
3. **Terms Agreement Injunction:** Trường `agreedToTerms` bắt buộc phải mang giá trị `true`. Nếu là `false` hoặc `null`, giao dịch bị từ chối lập tức ở lớp Validation.
4. **Default Role Isolation (`[ARC-005]`):** Mọi tài khoản đăng ký công khai qua endpoint này được ấn định duy nhất vai trò `Student` (`role_id = 5`). Việc gán các vai trò quản trị khác (`SystemAdmin`, `CenterAdmin`, `Manager`, `Teacher`) qua endpoint này bị cấm tuyệt đối.
5. **Audit Event Trigger (`[NFR-006]`):** Ghi lại nhật ký an ninh hệ thống trong bảng `audit_logs` bao gồm IP nguồn, User-Agent, thời điểm và kết quả xử lý.

### 3.3. Request Headers Specification
| Header Name | Type | Required | Standard / Format | Description & Purpose | Targeted Tag IDs |
|---|---|---|---|---|---|
| `Content-Type` | String | Yes | `application/json` | Định dạng dữ liệu truyền tải của Payload. | `[REQ-001]` |
| `Accept` | String | Yes | `application/json` | Định dạng phản hồi mong muốn từ Server. | `[REQ-001]` |
| `X-Forwarded-For` | String | No | IPv4 / IPv6 Address | Địa chỉ IP thực của Client truyền qua Proxy/Load Balancer phục vụ Rate Limiting & Audit Log. | `[NFR-003]`, `[NFR-006]` |
| `User-Agent` | String | No | Free-form String | Thông tin trình duyệt/thiết bị Client phục vụ Audit Logging. | `[NFR-006]` |

### 3.4. JSON Request Payload Schema (`RegisterRequest`)

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "RegisterRequest",
  "type": "object",
  "required": ["email", "password", "fullName", "agreedToTerms"],
  "additionalProperties": false,
  "properties": {
    "email": {
      "type": "string",
      "format": "email",
      "maxLength": 255,
      "description": "Địa chỉ email duy nhất đăng ký tài khoản hệ thống (RFC 5322 standard).",
      "example": "student.nguyen@membershiphub.vn"
    },
    "password": {
      "type": "string",
      "minLength": 8,
      "maxLength": 128,
      "pattern": "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]).{8,128}$",
      "description": "Mật khẩu người dùng tuân thủ chính sách mật khẩu mạnh OWASP.",
      "example": "P@ssw0rd2026!"
    },
    "fullName": {
      "type": "string",
      "minLength": 2,
      "maxLength": 100,
      "description": "Họ và tên đầy đủ của người dùng.",
      "example": "Nguyễn Văn An"
    },
    "agreedToTerms": {
      "type": "boolean",
      "enum": [true],
      "description": "Cờ xác nhận đồng ý với Điều khoản Sử dụng và Chính sách Bảo mật.",
      "example": true
    }
  }
}
```

### 3.5. JSON Response Schemas

#### 3.5.1. Success Response (HTTP 201 Created)
Trả về khi tài khoản được khởi tạo thành công và phát hành chuỗi mã xác thực JWT.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "AuthResponse",
  "type": "object",
  "required": ["accessToken", "refreshToken", "expiresIn", "tokenType", "userId", "role"],
  "additionalProperties": false,
  "properties": {
    "accessToken": {
      "type": "string",
      "description": "Mã xác thực JWT Access Token (Thuật toán RS256, thời hạn 15 phút).",
      "example": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Im1lbWJlcnNoaXAtaHViLWtleSJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJncm91cCI6IlN0dWRlbnQiLCJpc3MiOiJtZW1iZXJzaGlwLWh1YiIsImV4cCI6MTc3MTk0ODAwMH0.signature..."
    },
    "refreshToken": {
      "type": "string",
      "description": "Mã cấp lại Token (Refresh Token, thời hạn 7 ngày).",
      "example": "rt_9f8e7d6c5b4a3f2e1d0c9b8a7f6e5d4c3b2a1f0e"
    },
    "expiresIn": {
      "type": "integer",
      "description": "Thời gian sống của Access Token tính theo giây (900 giây = 15 phút).",
      "example": 900
    },
    "tokenType": {
      "type": "string",
      "description": "Loại định dạng Token xác thực.",
      "example": "Bearer"
    },
    "userId": {
      "type": "string",
      "format": "uuid",
      "description": "Mã định danh duy nhất (UUIDv4) của người dùng vừa được tạo.",
      "example": "550e8400-e29b-41d4-a716-446655440000"
    },
    "role": {
      "type": "string",
      "description": "Tên vai trò người dùng được gán trong hệ thống.",
      "example": "Student"
    }
  }
}
```

#### 3.5.2. HTTP Status Error Codes Matrix (`[EXC-004]`, `[NFR-003]`)

| HTTP Status | Internal Error Code | Trigger Condition / Root Cause | Target Schema | Targeted Tag IDs |
|---|---|---|---|---|
| `400 Bad Request` | `VALIDATION_FAILED` | Một hoặc nhiều trường đầu vào vi phạm quy tắc Bean Validation (Email sai format, mật khẩu yếu, chưa đồng ý điều khoản). | `ValidationErrorResponse` | `[REQ-001]`, `[EXC-004]` |
| `409 Conflict` | `EMAIL_ALREADY_EXISTS` | Địa chỉ email cung cấp đã tồn tại trong cơ sở dữ liệu hệ thống. | `StandardErrorResponse` | `[REQ-001]`, `[EXC-004]` |
| `429 Too Many Requests` | `RATE_LIMIT_EXCEEDED` | Yêu cầu bị chặn do địa chỉ IP gửi vượt quá 5 lượt / 1 phút. | `StandardErrorResponse` | `[NFR-003]` |
| `500 Internal Server Error` | `INTERNAL_SERVER_ERROR` | Lỗi phát sinh ngoài dự kiến từ phía Server (Lỗi kết nối DB, lỗi mã hóa token). Không lộ stack trace. | `StandardErrorResponse` | `[EXC-004]`, `[NFR-003]` |

##### Structural Error Payload Schemas & Examples

###### Example 1: HTTP 400 Bad Request (`VALIDATION_FAILED`)
```json
{
  "timestamp": "2026-08-29T22:34:21Z",
  "status": 400,
  "errorCode": "VALIDATION_FAILED",
  "message": "Dữ liệu yêu cầu không hợp lệ. Vui lòng kiểm tra lại các trường thông tin.",
  "path": "/api/v1/users/register",
  "traceId": "trace-a1b2c3d4-e5f6-7890",
  "errors": [
    {
      "field": "email",
      "message": "Địa chỉ email không đúng định dạng chuẩn RFC 5322",
      "rejectedValue": "student-invalid-email-format"
    },
    {
      "field": "password",
      "message": "Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt",
      "rejectedValue": "weakpass"
    },
    {
      "field": "agreedToTerms",
      "message": "Bạn phải đánh dấu đồng ý với Điều khoản Dịch vụ trước khi tiếp tục",
      "rejectedValue": false
    }
  ]
}
```

###### Example 2: HTTP 409 Conflict (`EMAIL_ALREADY_EXISTS`)
```json
{
  "timestamp": "2026-08-29T22:34:21Z",
  "status": 409,
  "errorCode": "EMAIL_ALREADY_EXISTS",
  "message": "Địa chỉ email 'student.nguyen@membershiphub.vn' đã được sử dụng bởi một tài khoản khác.",
  "path": "/api/v1/users/register",
  "traceId": "trace-f9e8d7c6-b5a4-3210",
  "errors": []
}
```

###### Example 3: HTTP 429 Too Many Requests (`RATE_LIMIT_EXCEEDED`)
```json
{
  "timestamp": "2026-08-29T22:34:21Z",
  "status": 429,
  "errorCode": "RATE_LIMIT_EXCEEDED",
  "message": "Bạn đã gửi quá số lượng yêu cầu cho phép (Tối đa 5 lần/phút). Vui lòng thử lại sau.",
  "path": "/api/v1/users/register",
  "traceId": "trace-87654321-abcd-ef01",
  "errors": []
}
```

---

## 💻 4. Executable Real-World cURL Examples

### 4.1. Standard Successful Registration (HTTP 201 Created)
```bash
curl -i -X POST "https://api.membershiphub.vn/api/v1/users/register" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -H "X-Forwarded-For: 203.0.113.195" \
  -H "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)" \
  -d '{
    "email": "student.nguyen@membershiphub.vn",
    "password": "P@ssw0rd2026!",
    "fullName": "Nguyễn Văn An",
    "agreedToTerms": true
  }'
```

### 4.2. Invalid Payload Request (Testing HTTP 400 Validation Error)
```bash
curl -i -X POST "https://api.membershiphub.vn/api/v1/users/register" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "email": "bad-email-format",
    "password": "123",
    "fullName": "",
    "agreedToTerms": false
  }'
```

### 4.3. Duplicate Email Request (Testing HTTP 409 Conflict Error)
```bash
curl -i -X POST "https://api.membershiphub.vn/api/v1/users/register" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "email": "student.nguyen@membershiphub.vn",
    "password": "P@ssw0rd2026!",
    "fullName": "Nguyễn Văn An Duplicate",
    "agreedToTerms": true
  }'
```

---

## 🔗 5. Cross-References & RBAC Security Integration

Endpoint `POST /api/v1/users/register` liên kết chặt chẽ với ma trận phân quyền hệ thống và kiến trúc bảo mật tổng thể:

### 5.1. Enterprise RBAC Alignment (`[ARC-001]`, `[ARC-005]`)
- **Default Hierarchy Placement:** Mọi người dùng khởi tạo qua công khai đều được đưa trực tiếp vào **Level 5: Student** trong Ma trận RBAC 5 Cấp độ.
- **Strict Role Boundary:** Endpoint đăng ký không tiếp nhận bất kỳ tham số vai trò nào từ phía Client. Mọi hành vi cố tình truyền các trường giả mạo vai trò (`role`, `roleId`) đều bị loại bỏ tự động bởi `additionalProperties: false` tại JSON Schema validator.
- **Administrative Elevation Path:** Việc nâng cấp vai trò người dùng từ `Student` lên `CenterAdmin`, `Manager`, hoặc `Teacher` bắt buộc phải được thực hiện bởi quản trị viên qua endpoint chuyên biệt `PUT /api/v1/users/{id}/role` (`[REQ-003]`, `[ARC-002]`).

### 5.2. Token Lifecycle & Session Revocation (`[ARC-006]`)
- Khi một vai trò người dùng được điều chỉnh bởi Admin ở thời điểm tương lai, toàn bộ JWT Refresh Token đã phát hành từ bước đăng ký này sẽ bị thu hồi ngay lập tức thông qua danh sách Redis Blacklist nhằm đảm bảo an toàn truy cập tức thì.

---
*Tài liệu này được soạn thảo và ban hành bởi Kiến Trúc Sư Hệ Thống Doanh Nghiệp. Mọi sự thay đổi về cấu trúc API Contract phải được thông qua quy trình Review Kiến trúc Kỹ thuật.*
```

