markdown
# Authentication API Specification
## Document Metadata
| Field | Value |
|-------|-------|
| Document ID | AUTH-API-SPEC-001 |
| Project Name | membership-hub |
| Version | 1.0 (Base) |
| Last Updated | 2026-08-18 |
| Owner | Senior Backend Architect |
| Status | Approved for Development |
| Target File Path | ./sources/docs/auth-api-spec.md |

---

## 1. Traceability Matrix Reference
This document is strictly mapped to the following enterprise requirement and architecture tags, with 100% coverage of all specified authentication functionality:
| Tag ID | Requirement/Architecture Description | Mapped Scope in This Document |
|--------|---------------------------------------|-------------------------------|
| [REQ-001] | User registration with email/password and JWT token issuance | Local authentication endpoints (register, login, token refresh, logout) and password security constraints |
| [REQ-002] | OAuth2 integration (Firebase, Google, Facebook) | OAuth2 authentication flow, provider validation, and error handling for third-party auth |
| [ARC-006] | Centralized authentication system with JWT (15min access token, 7day refresh token) and Firebase Auth integration | All authentication flows, token management logic, security constraints, and integration with Firebase Auth |

---

## 2. Constants & Configuration Reference
All fixed configuration values used across the authentication API are defined below to adhere to enterprise anti-magic-number and clean code policies:
| Constant Name | Value | Description | Mapped Tag |
|---------------|-------|-------------|------------|
| `ACCESS_TOKEN_EXPIRY_MS` | 900000 | JWT access token expiration time (15 minutes) | [ARC-006] |
| `REFRESH_TOKEN_EXPIRY_MS` | 604800000 | JWT refresh token expiration time (7 days) | [ARC-006] |
| `MIN_PASSWORD_LENGTH` | 8 | Minimum required length for user passwords | [REQ-001] |
| `PASSWORD_COMPLEXITY_REGEX` | `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$` | Regex pattern for valid passwords (requires 1 lowercase, 1 uppercase, 1 number, 1 special character) | [REQ-001] |
| `ALLOWED_OAUTH_PROVIDERS` | `local, firebase, google, facebook` | Valid values for authentication provider field | [REQ-001], [REQ-002] |
| `MAX_EMAIL_LENGTH` | 255 | Maximum allowed length for user email addresses | [REQ-001] |
| `MAX_FULL_NAME_LENGTH` | 100 | Maximum allowed length for user full names | [REQ-001] |
| `BCRYPT_ROUNDS` | 12 | Number of hashing rounds for password encryption | [REQ-001], [NFR-003] |
| `JWT_SIGNING_ALGORITHM` | RS256 | Algorithm used to sign JWT tokens | [ARC-006], [NFR-003] |

---

## 3. API Endpoint Specifications
All endpoints require TLS 1.3 encryption for all requests. All request/response payloads use JSON format.

### 3.1 Register New User (Local Authentication)
| Field | Specification |
|-------|---------------|
| **HTTP Method** | POST |
| **Full Endpoint** | `/api/v1/auth/register` |
| **Targeted Tag IDs** | [REQ-001], [ARC-006] |
| **Request Headers** | `Content-Type: application/json` |
| **Request Payload Schema** | <pre>{
  "email": "string (required, valid email format, max 255 characters)",
  "password": "string (required, min 8 characters, matches PASSWORD_COMPLEXITY_REGEX)",
  "fullName": "string (required, max 100 characters)",
  "provider": "string (optional, default: 'local', allowed values: local, firebase, google, facebook)"
}</pre> |
| **Success Response (201 Created)** | <pre>{
  "userId": "uuid (unique user identifier)",
  "email": "string (registered user email)",
  "role": "string (default: 'Student', assigned user role)",
  "accessToken": "string (JWT access token, expires in 15 minutes)",
  "refreshToken": "string (JWT refresh token, expires in 7 days)"
}</pre> |
| **Error Response (400 Bad Request)** | <pre>{
  "error": "VALIDATION_INPUT_INVALID",
  "message": "List of invalid input fields",
  "details": {
    "email": "Invalid email format",
    "password": "Password does not meet complexity requirements"
  }
}</pre> |
| **Error Response (409 Conflict)** | <pre>{
  "error": "EMAIL_ALREADY_EXISTS",
  "message": "Email is already registered in the system"
}</pre> |

---

### 3.2 User Login (Local Authentication)
| Field | Specification |
|-------|---------------|
| **HTTP Method** | POST |
| **Full Endpoint** | `/api/v1/auth/login` |
| **Targeted Tag IDs** | [REQ-001], [ARC-006] |
| **Request Headers** | `Content-Type: application/json` |
| **Request Payload Schema** | <pre>{
  "email": "string (required, registered user email)",
  "password": "string (required, user password)"
}</pre> |
| **Success Response (200 OK)** | Same as 3.1 Success Response |
| **Error Response (400 Bad Request)** | <pre>{
  "error": "VALIDATION_INPUT_INVALID",
  "message": "Missing required fields: email, password"
}</pre> |
| **Error Response (401 Unauthorized)** | <pre>{
  "error": "INVALID_CREDENTIALS",
  "message": "Invalid email or password"
}</pre> |

---

### 3.3 OAuth2 Authentication (Third-Party Providers)
| Field | Specification |
|-------|---------------|
| **HTTP Method** | POST |
| **Full Endpoint** | `/api/v1/auth/oauth2/{provider}` |
| **Targeted Tag IDs** | [REQ-002], [ARC-006] |
| **Path Parameters** | `provider` (string, required, allowed values: firebase, google, facebook) |
| **Request Headers** | `Content-Type: application/json` |
| **Request Payload Schema** | <pre>{
  "authCode": "string (required, OAuth2 authorization code from the third-party provider)"
}</pre> |
| **Success Response (200 OK)** | Same as 3.1 Success Response |
| **Error Response (400 Bad Request)** | <pre>{
  "error": "VALIDATION_INPUT_INVALID",
  "message": "Missing required field: authCode"
}</pre> |
| **Error Response (401 Unauthorized)** | <pre>{
  "error": "OAUTH2_AUTH_FAILED",
  "message": "Failed to authenticate with {provider}: invalid authorization code"
}</pre> |
| **Error Response (404 Not Found)** | <pre>{
  "error": "PROVIDER_NOT_SUPPORTED",
  "message": "OAuth provider {provider} is not supported"
}</pre> |

---

### 3.4 Refresh Access Token
| Field | Specification |
|-------|---------------|
| **HTTP Method** | POST |
| **Full Endpoint** | `/api/v1/auth/refresh` |
| **Targeted Tag IDs** | [ARC-006] |
| **Request Headers** | `Content-Type: application/json` |
| **Request Payload Schema** | <pre>{
  "refreshToken": "string (required, valid unexpired refresh token)"
}</pre> |
| **Success Response (200 OK)** | <pre>{
  "accessToken": "string (new JWT access token, expires in 15 minutes)",
  "refreshToken": "string (rotated refresh token, expires in 7 days)",
  "expiresIn": 900
}</pre> |
| **Error Response (401 Unauthorized)** | <pre>{
  "error": "INVALID_REFRESH_TOKEN",
  "message": "Invalid or expired refresh token"
}</pre> |
| **Error Response (401 Unauthorized)** | <pre>{
  "error": "TOKEN_REVOKED",
  "message": "Refresh token has been revoked by user logout"
}</pre> |

---

### 3.5 User Logout
| Field | Specification |
|-------|---------------|
| **HTTP Method** | POST |
| **Full Endpoint** | `/api/v1/auth/logout` |
| **Targeted Tag IDs** | [ARC-006] |
| **Request Headers** | `Authorization: Bearer <valid_access_token>`<br>`Content-Type: application/json` |
| **Request Payload Schema** | <pre>{
  "refreshToken": "string (required, refresh token to revoke)"
}</pre> |
| **Success Response (200 OK)** | <pre>{
  "message": "Logout successful, refresh token revoked"
}</pre> |
| **Error Response (401 Unauthorized)** | <pre>{
  "error": "UNAUTHORIZED",
  "message": "Invalid or missing access token"
}</pre> |

---

## 4. Error Code Reference
All authentication-specific error codes are listed below, with their corresponding HTTP status codes and mapped requirement tags:
| Error Code | HTTP Status | Description | Mapped Tag |
|------------|-------------|-------------|------------|
| `VALIDATION_INPUT_INVALID` | 400 | One or more request fields fail validation (invalid format, missing required field, etc.) | [REQ-001], [REQ-002] |
| `EMAIL_ALREADY_EXISTS` | 409 | User with the provided email is already registered | [REQ-001] |
| `INVALID_CREDENTIALS` | 401 | Provided email/password combination is incorrect | [REQ-001] |
| `OAUTH2_AUTH_FAILED` | 401 | Failed to exchange OAuth2 authorization code with the third-party provider | [REQ-002] |
| `PROVIDER_NOT_SUPPORTED` | 404 | Requested OAuth2 provider is not enabled in the system | [REQ-002] |
| `INVALID_REFRESH_TOKEN` | 401 | Provided refresh token is invalid, expired, or malformed | [ARC-006] |
| `TOKEN_REVOKED` | 401 | Refresh token has been revoked via logout or admin action | [ARC-006] |
| `UNAUTHORIZED` | 401 | Missing or invalid access token in request headers | [ARC-006] |

---

## 5. Implementation Reference
All authentication API implementation files are located under the `org.nlh4j.saas.auth` Java package, per project naming conventions:
| Component | File Path | Mapped Tag |
|-----------|-----------|------------|
| Authentication REST Resource | `./sources/backend/auth-service/src/main/java/com/hub/auth/AuthResource.java` | [REQ-001], [REQ-002], [ARC-006] |
| Authentication Business Service | `./sources/backend/auth-service/src/main/java/com/hub/auth/AuthService.java` | [REQ-001], [ARC-006] |
| OAuth2 Integration Service | `./sources/backend/auth-service/src/main/java/com/hub/auth/OAuth2Service.java` | [REQ-002], [EXC-004] |
| JWT Token Management Service | `./sources/backend/auth-service/src/main/java/com/hub/auth/TokenService.java` | [ARC-006] |
| RBAC Access Filter | `./sources/backend/auth-service/src/main/java/com/hub/auth/RbacFilter.java` | [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005] |

---

## 6. Security & Compliance Notes
1. All authentication requests must be sent over TLS 1.3 encrypted connections; plaintext HTTP requests are rejected.
2. User passwords are hashed using BCrypt with 12 hashing rounds before storage; plaintext passwords are never stored or logged.
3. JWT access tokens are signed with RS256 algorithm and contain minimal user claims (userId, role, provider) to reduce token size.
4. Refresh tokens are stored encrypted in the PostgreSQL database and are rotated on every refresh request to prevent token reuse.
5. All authentication-related actions (register, login, logout, role change) are logged to the audit log system with user ID, timestamp, and action details, stored for 1 year per [NFR-006].
6. Sensitive data (email, password, tokens) is automatically masked in all application logs using PII masking interceptor, per [NFR-003] and [NFR-006].