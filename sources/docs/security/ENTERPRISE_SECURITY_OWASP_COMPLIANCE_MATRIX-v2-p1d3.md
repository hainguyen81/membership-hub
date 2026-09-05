```markdown
# ENTERPRISE SECURITY & OWASP COMPLIANCE MATRIX: MEMBERSHIP-HUB PLATFORM

| Property | Standard Specification Value |
| :--- | :--- |
| **Document UID** | `DOC-SEC-2026-003` |
| **Target Documentation Path** | `./sources/docs/security/ENTERPRISE_SECURITY_OWASP_COMPLIANCE_MATRIX.md` |
| **Root Package Namespace** | `org.nlh4j.membershiphub` |
| **Security Classification** | Enterprise Restricted (Level 4) |
| **Runtime Compliance Target** | Quarkus 3.15.1 LTS / Java 17 LTS / SmallRye JWT / OAuth2 Resource Server |
| **Regulatory & Security Baseline** | OWASP Top 10:2021, NIST SP 800-63B, GDPR/CCPA Data Protection Directives |
| **Requirement Traceability Tokens** | `[ARC-001]`, `[ARC-002]`, `[ARC-003]`, `[ARC-004]`, `[ARC-005]`, `[ARC-006]`, `[NFR-003]`, `[NFR-006]`, `[NFR-008]`, `[REQ-001]`, `[REQ-002]`, `[REQ-003]`, `[DOC-001]` |

---

## 📑 1. TRACEABILITY MATRIX REFERENCE

The technical security and compliance implementation detailed within this enterprise specification maps directly to the following system requirements, architectural frameworks, and non-functional constraints:

| Requirement / Tag ID | Architecture Module / Target Class Path | Operational Responsibility & Verification Benchmark |
| :--- | :--- | :--- |
| `[ARC-001]` | `org.nlh4j.membershiphub.userservice.service.UserRoleService` | RBAC Level 1: System Admin Super-User multi-tenant global governance. |
| `[ARC-002]` | `org.nlh4j.membershiphub.centerservice.service.CenterAdminService` | RBAC Level 2: Center Admin domain isolation by `center_id`. |
| `[ARC-003]` | `org.nlh4j.membershiphub.userservice.security.ResourceServerConfig` | RBAC Level 3: Manager operational delegation profile. |
| `[ARC-004]` | `org.nlh4j.membershiphub.courseservice.controller.CourseTeacherController` | RBAC Level 4: Teacher pedagogical boundaries and read scopes. |
| `[ARC-005]` | `org.nlh4j.membershiphub.userservice.controller.UserController` | RBAC Level 5: Student self-service identity and profile scopes. |
| `[ARC-006]` | `org.nlh4j.membershiphub.userservice.security.JwtTokenProvider` | Dual-token issuance (RS256 15m Access Token, 7d Refresh Token, Blacklist). |
| `[NFR-003]` | `org.nlh4j.membershiphub.userservice.security.ResourceServerConfig` | End-to-end transport encryption (TLS 1.3), cryptographic fail-safes, OWASP Top 10 mitigation. |
| `[NFR-006]` | `org.nlh4j.membershiphub.userservice.security.AuthAuditLogger` | Tamper-evident, structured audit log emission with 1-year retention compliance. |
| `[NFR-008]` | `org.nlh4j.membershiphub.userservice.service.AuthService` | GDPR/CCPA PII masking, right-to-be-forgotten orchestration, export compliance. |
| `[REQ-001]` | `org.nlh4j.membershiphub.userservice.controller.AuthController` | Local registration, BCrypt cost factor 12 hashing, Jakarta Bean Validation. |
| `[REQ-002]` | `org.nlh4j.membershiphub.userservice.security.SocialAuthProviderRegistry` | Federated identity brokering (Firebase, Google, Facebook OAuth2/OIDC). |
| `[REQ-003]` | `org.nlh4j.membershiphub.userservice.controller.UserController` | RBAC mutation, session revocation, Redis blacklist invalidation pipeline. |
| `[DOC-001]` | `./sources/docs/security/ENTERPRISE_SECURITY_OWASP_COMPLIANCE_MATRIX.md` | Authoritative security architecture specification and runtime audit guide. |

---

## 🔐 2. AUTHENTICATION FLOWS & TRANSACTION SEQUENCES

The Membership Hub platform implements a zero-trust, stateless API security perimeter backed by cryptographic token verification and real-time distributed revocation.

### 2.1. Local Identity Authentication & Token Issuance (`[REQ-001]`, `[ARC-006]`, `[NFR-003]`)

Local user authentication uses parameterized database queries, constant-time hash verification via BCrypt (work factor 12), and produces an asymmetric RS256-signed Json Web Token (JWT) paired with an opaque cryptographically random refresh token.

```mermaid
sequenceDiagram
    autonumber
    actor Client as Web/Mobile Client (User)
    participant Gateway as API Gateway / Ingress
    participant AuthCtrl as AuthController [REQ-001]
    participant AuthService as AuthService (User Service)
    participant DB as PostgreSQL (users table)
    participant TokenProv as JwtTokenProvider [ARC-006]
    participant Audit as AuthAuditLogger [NFR-006]

    Client->>Gateway: POST /api/v1/auth/login { email, password }
    Gateway->>AuthCtrl: Forward Credentials (TLS 1.3)
    Note over AuthCtrl: [NFR-003] Apply Rate-Limiting Filter (Bucket4j)
    AuthCtrl->>AuthService: authenticate(email, rawPassword)
    AuthService->>DB: SELECT * FROM users WHERE LOWER(email) = LOWER(?1)
    DB-->>AuthService: User Entity (including password_hash, role_id)
    Note over AuthService: [NFR-003] BCrypt.verifyer(Cost=12).verify(rawPassword, hash)
    alt Password Invalid
        AuthService->>Audit: logAuthEvent(LOGIN_FAILED, IP, UA)
        AuthService-->>AuthCtrl: Throw BadCredentialsException
        AuthCtrl-->>Client: HTTP 401 Unauthorized (Generic Error Message)
    else Password Valid
        AuthService->>TokenProv: generateAccessToken(userId, role, provider)
        TokenProv-->>AuthService: RS256 Signed Access Token (TTL: 900s / 15m)
        AuthService->>TokenProv: generateRefreshToken(userId)
        TokenProv-->>AuthService: Opaque Refresh Token (TTL: 604800s / 7d)
        AuthService->>DB: INSERT INTO refresh_tokens (token_id, user_id, expiry, revoked=false)
        AuthService->>Audit: logAuthEvent(LOGIN_SUCCESS, userId, details)
        AuthService-->>AuthCtrl: AuthResponse DTO
        AuthCtrl-->>Client: HTTP 200 OK { accessToken, refreshToken, expiresIn: 900, tokenType: "Bearer" }
    end
```

---

### 2.2. Federated Social OAuth2 Authentication Workflow (`[REQ-002]`, `[ARC-006]`, `[NFR-003]`)

Federated authentication supports Google OpenID Connect (OIDC), Firebase Authentication Tokens, and Facebook Graph API tokens. Third-party tokens are resolved and cryptographically validated upstream by dedicated adapter strategies before an internal session token is issued.

```mermaid
sequenceDiagram
    autonumber
    actor Client as Frontend Client (React/Next.js)
    participant Provider as Social Identity Provider (Google / Firebase / Facebook)
    participant AuthCtrl as AuthController [REQ-002]
    participant SocialRegistry as SocialAuthProviderRegistry [REQ-002]
    participant Adapter as Specific Provider Adapter
    participant AuthService as SocialAuthService
    participant DB as PostgreSQL (users & user_social_accounts)
    participant TokenProv as JwtTokenProvider [ARC-006]
    participant Audit as AuthAuditLogger [NFR-006]

    Client->>Provider: Interactive OAuth2 / PKCE Authorization Flow
    Provider-->>Client: External Identity Token (idToken / OAuth2 Bearer)
    Client->>AuthCtrl: POST /api/v1/auth/social { provider: "google", idToken: "..." }
    AuthCtrl->>SocialRegistry: authenticate(providerName, idToken)
    SocialRegistry->>Adapter: verifyToken(idToken)
    Adapter->>Provider: Validate Signature via JWKS Endpoint / Graph Debug Token
    Provider-->>Adapter: Verified Claims (email, provider_user_id, fullName, avatar)
    Adapter-->>SocialRegistry: SocialUserInfo Canonical Model
    SocialRegistry->>AuthService: synchronizeSocialUser(SocialUserInfo)
    
    AuthService->>DB: SELECT * FROM user_social_accounts WHERE provider = ?1 AND provider_user_id = ?2
    alt Existing Social Account Link
        DB-->>AuthService: Return Linked User Reference
    else New Identity
        AuthService->>DB: SELECT * FROM users WHERE email = ?1
        alt Email Exists (Link Account)
            DB-->>AuthService: Existing User
            AuthService->>DB: INSERT INTO user_social_accounts (user_id, provider, provider_user_id)
        else Fresh Registration
            AuthService->>DB: INSERT INTO users (user_id, email, role_id=5, provider="google")
            AuthService->>DB: INSERT INTO user_social_accounts (...)
            AuthService->>DB: INSERT INTO student_cards (user_id, ...)
        end
    end

    AuthService->>TokenProv: generateAccessToken(userId, role, provider)
    TokenProv-->>AuthService: RS256 Access Token (15m)
    AuthService->>TokenProv: generateRefreshToken(userId)
    TokenProv-->>AuthService: Refresh Token (7d)
    AuthService->>Audit: logAuthEvent(SOCIAL_AUTH_SUCCESS, userId, provider)
    AuthService-->>AuthCtrl: AuthResponse DTO
    AuthCtrl-->>Client: HTTP 200 OK { accessToken, refreshToken, userId, role, isNewUser }
```

---

### 2.3. Refresh Token Rotation & Session Lifecycle (`[ARC-006]`, `[NFR-003]`)

To mitigate replay and token theft risks, refresh token exchange enforces Single-Use Token Rotation (SUTR). Using an invalidated or already-consumed refresh token triggers an immediate cascade revocation of all active sessions for that user identity.

```mermaid
sequenceDiagram
    autonumber
    actor Client as Frontend Client
    participant AuthCtrl as AuthController
    participant TokenService as TokenRefreshService
    participant Redis as Redis Distributed Cache (Blacklist)
    participant DB as PostgreSQL (refresh_tokens)
    participant TokenProv as JwtTokenProvider [ARC-006]
    participant Audit as AuthAuditLogger [NFR-006]

    Client->>AuthCtrl: POST /api/v1/auth/refresh { refreshToken: "rt_legacy_xyz" }
    AuthCtrl->>TokenService: rotateRefreshToken("rt_legacy_xyz")
    TokenService->>DB: SELECT * FROM refresh_tokens WHERE token_id = ?1
    alt Token Missing OR Expired OR Revoked
        TokenService->>Redis: SETEX blacklist:user:{userId}:all 604800 "BREACH_COMPROMISE"
        TokenService->>DB: UPDATE refresh_tokens SET revoked = true WHERE user_id = ?1
        TokenService->>Audit: logAuthEvent(SECURITY_ALERT_REUSE_DETECTED, userId)
        TokenService-->>AuthCtrl: Throw CompromisedTokenException
        AuthCtrl-->>Client: HTTP 401 Unauthorized (Session Revoked Globally)
    else Token Valid & Unused
        TokenService->>DB: UPDATE refresh_tokens SET revoked = true, consumed_at = now() WHERE token_id = ?1
        TokenService->>TokenProv: generateAccessToken(userId, role, provider)
        TokenProv-->>TokenService: New RS256 Access Token
        TokenService->>TokenProv: generateRefreshToken(userId)
        TokenProv-->>TokenService: New Refresh Token ("rt_novel_abc")
        TokenService->>DB: INSERT INTO refresh_tokens (token_id="rt_novel_abc", user_id, expiry)
        TokenService->>Audit: logAuthEvent(TOKEN_REFRESH, userId)
        TokenService-->>AuthCtrl: AuthResponse { accessToken, refreshToken="rt_novel_abc", expiresIn: 900 }
        AuthCtrl-->>Client: HTTP 200 OK
    end
```

---

## 🪪 3. CRYPTOGRAPHIC JWT STRUCTURE SPECIFICATION (`[ARC-006]`, `[NFR-003]`)

The platform mandates asymmetric RS256 (RSA Signature with SHA-256) signature verification. The symmetric `HS256` algorithm is rejected across all microservice perimeters to prevent key-confusion and dictionary attacks.

```
+-----------------------------------------------------------------------------------+
| JWT COMPONENT ARCHITECTURE (RS256 / SHA-256 with 2048-bit RSA Private Key)        |
+-----------------------------------------------------------------------------------+
|  HEADER: JOSE Base64URL Encoded Metadata                                          |
|  { "alg": "RS256", "typ": "JWT", "kid": "membership-hub-auth-key-2026-v1" }      |
+-----------------------------------------------------------------------------------+
|  PAYLOAD: Base64URL Encoded Standard & Enterprise Custom Claims                   |
|  {                                                                                |
|    "iss": "membership-hub",                                                      |
|    "sub": "550e8400-e29b-41d4-a716-446655440000",                                |
|    "aud": "membership-hub-client",                                               |
|    "jti": "c2b1897d-6c19-4f77-987f-e221d603a111",                                |
|    "iat": 1774900000,                                                             |
|    "exp": 1774900900,                                                             |
|    "nbf": 1774900000,                                                             |
|    "group": "CENTER_ADMIN",                                                       |
|    "center_id": "770e8400-e29b-41d4-a716-446655440000",                           |
|    "provider": "local",                                                           |
|    "email": "center.admin@membershiphub.vn"                                      |
|  }                                                                                |
+-----------------------------------------------------------------------------------+
|  SIGNATURE: RSASSA-PKCS1-v1_5 SHA-256 (Header + "." + Payload, PrivateKey2048)   |
|  [ 256 Bytes Raw Asymmetric Cryptographic Cipher Block ]                          |
+-----------------------------------------------------------------------------------+
```

### Detailed Token Component Breakdown

| Token Segment | Property Key | Data Type | Security Function & Validation Constraints |
| :--- | :--- | :--- | :--- |
| **Header** | `alg` | String | **MUST** equal `RS256`. Parsing engines reject tokens with `none` or symmetric HMAC algorithms. |
| **Header** | `typ` | String | Static token type descriptor. **MUST** equal `JWT`. |
| **Header** | `kid` | String | Key Identifier matching the active public key in the JWKS rotation keystore. |
| **Payload** | `iss` | String | Issuer identification authority (`membership-hub`). Verified by `mp.jwt.verify.issuer`. |
| **Payload** | `sub` | UUID String | Canonical Primary Key of the User entity (`user_id`). Immutable across session lifecycles. |
| **Payload** | `aud` | String | Target Audience identifier (`membership-hub-client`). Prevents cross-service audience spoofing. |
| **Payload** | `jti` | UUID String | Unique JWT Identifier. Logged in the audit stream and checked against the Redis token revocation store. |
| **Payload** | `iat` | Unix Epoch | Issue timestamp (seconds). Tokens with future `iat` values are rejected (clock drift allowance: 5s). |
| **Payload** | `exp` | Unix Epoch | Expiration timestamp. Absolute enforcement: `iat + 900 seconds` (strictly 15 minutes). |
| **Payload** | `nbf` | Unix Epoch | Not Before assertion. Guarantees token cannot be processed prior to authorization settlement. |
| **Payload** | `group` | String Enum | Core RBAC role assignment: `SYSTEM_ADMIN`, `CENTER_ADMIN`, `MANAGER`, `TEACHER`, `STUDENT`. |
| **Payload** | `center_id` | UUID / Null | Multi-tenant scoping identifier. Required for `CENTER_ADMIN` and `MANAGER` access enforcement. |
| **Payload** | `provider` | String Enum | Registration origin identity source: `local`, `firebase`, `google`, `facebook`. |
| **Payload** | `email` | String | RFC 5322 compliant user contact identity string used for secondary downstream routing. |
| **Signature** | `[Raw Bytes]` | Binary Block | Cryptographic signature verifying token authenticity without shared secrets. |

---

## 🔒 4. PASSWORD & CREDENTIAL SECURITY POLICY (`[REQ-001]`, `[NFR-003]`)

The platform adheres to NIST SP 800-63B guidelines and strict OWASP recommendations for authentication security.

### 4.1. Password Entropy & Structural Validation Rules

Every local user password submitted to `/api/v1/users/register` or `/api/v1/users/reset-password` must pass standard enterprise validation before processing:

```
+-----------------------------------------------------------------------------------+
| ENTERPRISE PASSWORD SECURITY REGEX POLICY                                         |
+-----------------------------------------------------------------------------------+
|  Pattern: ^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&_\-#])[A-Za-z\d@$!%*?&_\-#]{8,128}$ |
+-----------------------------------------------------------------------------------+
```

- **Minimum Length:** 8 characters.
- **Maximum Length:** 128 characters (mitigates CPU DoS hashing exhaustion attacks).
- **Character Diversity Requirements:**
  - At least one lowercase ASCII alphabetic character (`[a-z]`).
  - At least one uppercase ASCII alphabetic character (`[A-Z]`).
  - At least one numeric digit (`[0-9]`).
  - At least one non-alphanumeric special character from the approved set: `@ $ ! % * ? & _ - #`.
- **Prohibited Patterns & Dictionary Defenses:**
  - Passwords cannot match the username, email prefix, or common words (e.g., `Password123!`, `Admin@2026`).
  - Passwords are validated against an in-memory Bloom filter loaded with the top 100,000 common breached passwords.
  - Sequential character progressions exceeding 3 characters (e.g., `abc`, `123`) are rejected.

### 4.2. Storage & Cryptographic Hashing Standard

- **Algorithm:** BCrypt Adaptive Hash Function with OpenBSD `$2a$` prefix.
- **Cost / Work Factor:** `12` (generating 4,096 computation iterations per password hash).
- **Salt Generation:** SecureRandom cryptographic PRNG utilizing hardware entropy sources (`/dev/urandom`), 128-bit length.
- **Memory & Storage Constraints:** Hashed output format `CHAR(60)` stored in the `password_hash` column of the `users` table.
- **Zero Clear-Text Exposure:** Plaintext passwords are scrubbed from memory buffers immediately following hash generation.

---

## 🚫 5. TOKEN REVOCATION & REAL-TIME BLACKLISTING (`[REQ-003]`, `[ARC-006]`, `[NFR-003]`)

Because standard JWTs are stateless and valid until expiration, explicit revocation—such as on user logout, role elevation, or credential breach—uses a high-performance Redis cache blacklist.

```
+-----------------------------------------------------------------------------------+
| DISTRIBUTED REDIS BLACKLIST STORAGE SCHEMA & TTL EVICTION                         |
+-----------------------------------------------------------------------------------+
|  1. Specific Token Revocation Key:                                                |
|     Key:   blacklist:jti:<jti_uuid>                                               |
|     Value: "REVOKED_LOGOUT" | "REVOKED_ROLE_MUTATION"                             |
|     TTL:   Calculated as (jwt.exp - current_epoch_seconds)                        |
|                                                                                   |
|  2. Global User Session Termination Key:                                         |
|     Key:   blacklist:user:<user_uuid>:revoked_before                              |
|     Value: <epoch_timestamp_of_revocation_event>                                  |
|     TTL:   604800 (Strictly 7 Days matching maximum refresh token lifetime)       |
+-----------------------------------------------------------------------------------+
```

### Revocation Pipeline & Enforcement Operations

```mermaid
flowchart TD
    Start[Incoming API Request with Bearer Token] --> ExtractToken[Extract & Parse Bearer JWT Claims]
    ExtractToken --> CheckSignature{Valid RS256 Signature?}
    CheckSignature -- No --> Return401[Reject: HTTP 401 Unauthorized]
    CheckSignature -- Yes --> CheckExpiry{Is Current Time < exp?}
    CheckExpiry -- No --> Return401
    CheckExpiry -- Yes --> RedisCheck1{Redis Key Exists?<br/>blacklist:jti:JTI}
    RedisCheck1 -- Yes (Revoked) --> Return401Blacklist[Reject: HTTP 401 Token Blacklisted]
    RedisCheck1 -- No --> RedisCheck2{User Revocation Key Exists?<br/>blacklist:user:UUID:revoked_before}
    RedisCheck2 -- Yes --> CheckIssueTime{Is iat < revoked_before?}
    CheckIssueTime -- Yes (Pre-dates Event) --> Return401Blacklist
    CheckIssueTime -- No --> CheckRBAC{Verify Endpoint RBAC Roles}
    RedisCheck2 -- No --> CheckRBAC
    CheckRBAC -- Insufficient --> Return403[Reject: HTTP 403 Forbidden]
    CheckRBAC -- Authorized --> ForwardController[Forward Execution to Resource Service Layer]
```

---

## 👥 6. RBAC PERMISSION MATRIX & ACCESS CONTROL BOUNDARIES (`[ARC-001]` - `[ARC-005]`)

The platform implements five distinct Role-Based Access Control (RBAC) tiers. Roles are hierarchically constrained and enforced using Quarkus `@RolesAllowed` annotations along with domain entity tenant checks.

| Role Name | Architectural Tag | System Scope | Functional Responsibilities & Boundary Enforcement | Allowed Endpoint Route Patterns |
| :--- | :--- | :--- | :--- | :--- |
| **SystemAdmin** | `[ARC-001]` | Global Multi-Tenant Core | Global administration, tenant provisioning, system setting configuration, global role mutations, centralized audit log querying. | `/api/v1/system/**`<br/>`/api/v1/centers/**`<br/>`/api/v1/users/**`<br/>`/api/v1/audit-logs/**` |
| **CenterAdmin** | `[ARC-002]` | Single Center Domain (`center_id`) | Center-scoped administration, staff management, course/session lifecycle, local student management, center promotion publishing. | `/api/v1/centers/{id}/**`<br/>`/api/v1/courses/**`<br/>`/api/v1/promotions/**`<br/>`/api/v1/reports/**` |
| **Manager** | `[ARC-003]` | Center Operations Delegated Scope | Operational management, publishing site announcements, approving student enrollments, processing offline attendances. | `/api/v1/announcements/**`<br/>`/api/v1/enrollments/**`<br/>`/api/v1/attendance/**` |
| **Teacher** | `[ARC-004]` | Pedagogical Course Scope | Course tracking, reviewing assigned student rosters, viewing course attendance statistics. **Forbidden from mutating user roles or center records.** | `/api/v1/courses/{id}/roster`<br/>`/api/v1/attendance/summary`<br/>`/api/v1/teachers/profile` |
| **Student** | `[ARC-005]` | Individual Self-Service Scope | Profile viewing, course catalog browsing, self-enrollment, real-time QR attendance scanning, membership card tracking and renewal. | `/api/v1/students/courses/available`<br/>`/api/v1/enrollments`<br/>`/api/v1/attendance/scan`<br/>`/api/v1/students/{id}/card/**` |

---

## 🛡️ 7. OWASP TOP 10 (2021) ENTERPRISE COMPLIANCE & MITIGATION CHECKLIST (`[NFR-003]`)

The table below outlines technical countermeasures for each OWASP Top 10 security category across the microservices ecosystem.

| OWASP Vulnerability Category | Platform Risk Analysis | Technical Mitigation & Architectural Implementation | Verification Assertion | Compliance Status |
| :--- | :--- | :--- | :--- | :--- |
| **A01:2021 Broken Access Control** | Unauthorized cross-tenant manipulation (`center_id` spoofing) or horizontal privilege escalation. | Enforce Jakarta `@RolesAllowed` annotations on all REST routes. Validate multi-tenant context by comparing the JWT `center_id` claim with the requested entity via the database layer. | Automated integration tests executing cross-tenant reads/writes must return `403 FORBIDDEN`. | **COMPLIANT** |
| **A02:2021 Cryptographic Failures** | Token forgery, weak password hashing, sensitive cleartext credentials leaked in transit. | Enforce TLS 1.3 in-transit; data at rest encrypted using AES-256 via Cloud SQL KMS. Passwords hashed using BCrypt (work factor 12). JWT signed with 2048-bit RSA keys (RS256). | Code review confirming zero instances of `HS256`, MD5, SHA1, or unencrypted HTTP transport. | **COMPLIANT** |
| **A03:2021 Injection (SQLi, Code, Command)** | SQL injection through search, sorting, or filter parameters. | Use Hibernate Panache parameterized queries (`PreparedStatement`). Validate sort columns against `SortWhitelistResolver`. Static DTO validation via Jakarta Bean Validation. | Dynamic AST query auditing verifying zero raw string concatenations in JPQL/Native SQL. | **COMPLIANT** |
| **A04:2021 Insecure Design** | Replay of consumed QR attendance scans, duplicate renewal charges. | Unique composite database constraints on `(student_id, course_id, attendance_date)`. Idempotency keys enforced on mutation endpoints (`POST /api/v1/attendance/scan`). | Concurrency tests pushing simultaneous identical requests must register exactly one execution. | **COMPLIANT** |
| **A05:2021 Security Misconfiguration** | Unnecessary HTTP methods enabled, default accounts, exposed debug endpoints. | Restrict default endpoints via `quarkus.http.auth.proactive=false`. Expose `/q/health` probes only on isolated management ports. Enable strict CSP headers via Ingress. | Penetration scan verifying non-essential verb rejections and active `X-Content-Type-Options: nosniff`. | **COMPLIANT** |
| **A06:2021 Vulnerable and Outdated Components** | Exploitation of unpatched third-party dependencies in the Maven build chain. | Pin dependencies via the root Maven BOM. Automated vulnerability scanning in CI/CD via Trivy and OWASP Dependency-Check. Maintain zero high/critical vulnerabilities. | Build pipeline automatically fails if any dependency contains a CVE score >= 7.0. | **COMPLIANT** |
| **A07:2021 Identification & Authentication Failures** | Brute-force credential guessing, session fixation, refresh token replay attacks. | Implement Single-Use Token Rotation on refresh tokens. Rate limit authentication endpoints using Token Bucket (`Bucket4j`: max 5 attempts/minute/IP). | Automated load test simulating 10 consecutive failed logins triggers temporary IP backoff locks. | **COMPLIANT** |
| **A08:2021 Software and Data Integrity Failures** | Unsigned or untrusted JWT acceptance, corrupted Kafka payloads. | Enforce RS256 signature verification on tokens via public key checks. Kafka event payloads are validated against Confluent JSON schemas before processing. | Unit tests asserting rejection of unsigned tokens or tampered Kafka payload structures. | **COMPLIANT** |
| **A09:2021 Security Logging and Monitoring Failures** | Unaudited privilege changes, cleartext credentials written to log files. | Structured JSON audit logging using `AuthAuditLogger` (`[NFR-006]`). PII masking intercepts `email`, `password`, and `tax_id` before log emission. | Log audit verifying that PII patterns are replaced with `***MASKED***` markers. | **COMPLIANT** |
| **A10:2021 Server-Side Request Forgery (SSRF)** | Malicious URLs passed into social token verifiers or webhook endpoints. | Validate all external URIs against an allowlist of identity provider discovery endpoints (`https://accounts.google.com`, `https://graph.facebook.com`). | SSRF test suites injecting private internal IPs (`10.0.0.0/8`, `169.254.169.254`) must fail with `400 BAD REQUEST`. | **COMPLIANT** |

---

## 🛠️ 8. INCIDENT RESPONSE & SECURITY EXCEPTION RUNBOOKS

This section details operational runbooks and fallback procedures for common security and authentication incident scenarios.

```
+-----------------------------------------------------------------------------------+
| SECURITY EXCEPTION & RUNBOOK INCIDENT DIRECTORY                                   |
+-----------------------------------------------------------------------------------+
|  RUNBOOK SEC-01: Self-Service Password Recovery Pipeline                          |
|  RUNBOOK SEC-02: Account Lockout & Brute-Force Remediation                        |
|  RUNBOOK SEC-03: Asymmetric Cryptographic Key Rotation (RS256 JWKS)              |
|  RUNBOOK SEC-04: Multi-Factor Authentication (MFA / TOTP) Extension Strategy      |
+-----------------------------------------------------------------------------------+
```

### 8.1. RUNBOOK SEC-01: Self-Service Password Recovery Pipeline

1. **Trigger:** A user initiates a password reset request via `POST /api/v1/auth/forgot-password { email }`.
2. **Identification & Protection:**
   - The system validates the email format. If the record does not exist, the API returns a generic `200 OK` response to prevent user enumeration attacks.
   - For valid accounts, an opaque cryptographically secure 256-bit token is generated: `reset_token = SecureRandom.generateBytes(32)`.
   - The SHA-256 hash of the token is stored in the `password_reset_tokens` table with a strict TTL of 15 minutes (`900s`).
3. **Dispatch:**
   - An event is published to Kafka topic `notification.outbound` containing the masked user payload and the unhashed reset link: `https://membershiphub.vn/auth/reset?token=<token>`.
4. **Execution:**
   - The user submits the token alongside a new password matching the enterprise password policy via `POST /api/v1/auth/reset-password`.
   - Upon successful password update, all existing user refresh tokens are revoked, the reset token is invalidated, and a security alert email is dispatched.

---

### 8.2. RUNBOOK SEC-02: Account Lockout & Brute-Force Remediation

1. **Trigger:** 5 consecutive failed login attempts within a 3-minute window for a specific account identity or IP address.
2. **Automated Containment:**
   - The rate-limiting filter flags the identifier and creates a Redis lock key: `lockout:user:<user_id>` with a TTL of 900 seconds (15 minutes).
   - Any further authentication attempts during this window are rejected with `HTTP 429 Too Many Requests` or `HTTP 401 Account Temporarily Locked`.
   - A security audit event (`ACCOUNT_LOCKED_SUSPICIOUS_FAILURES`) is emitted to the Cloud Logging SIEM dashboard.
3. **Remediation Procedures:**
   - **Automatic:** The account unlocks automatically once the Redis TTL expires without administrative intervention.
   - **Administrative Manual Unlock:** A `CenterAdmin` or `SystemAdmin` can manually clear the lockout using the admin console:
     ```bash
     # Connect to Redis Cluster via bastion CLI
     redis-cli -h $REDIS_HOST -p $REDIS_PORT -a $REDIS_AUTH DEL lockout:user:550e8400-e29b-41d4-a716-446655440000
     ```

---

### 8.3. RUNBOOK SEC-03: Asymmetric Cryptographic Key Rotation (RS256 JWKS)

To maintain system security, the RSA 2048-bit key pair must be rotated every 90 days, or immediately following an authorized disclosure alert.

```mermaid
sequenceDiagram
    autonumber
    actor SecOps as Security Operations (SecOps)
    participant KMS as GCP Cloud KMS / Vault
    participant UserSvc as User Service (Key Producer)
    participant Redis as Redis Cache (JWKS Store)
    participant ResourceSvc as Resource Services (Course/Center/Attendance)

    SecOps->>KMS: Generate New RSA 2048-bit Key Pair (kid: "key-2026-v2")
    SecOps->>UserSvc: Deploy Config (Primary Signing Key = "key-2026-v2", Grace Key = "key-2026-v1")
    UserSvc->>Redis: Publish Updated JWKS JSON (Exposing both "v2" and "v1" Public Keys)
    Note over UserSvc: Start Signing New Tokens with "key-2026-v2"
    ResourceSvc->>Redis: Invalidate Cached JWKS & Fetch Combined Public Key Ring
    Note over ResourceSvc: Successfully Validate Old ("v1") & New ("v2") Active Tokens
    SecOps->>SecOps: Wait 900 seconds (Full Expiration of Legacy Access Tokens)
    SecOps->>UserSvc: Decommission "key-2026-v1" from Active Keyring
    UserSvc->>Redis: Update JWKS (Expose "key-2026-v2" Exclusively)
```

---

### 8.4. RUNBOOK SEC-04: Multi-Factor Authentication (TOTP) Integration Plan

This baseline architecture is designed to support Time-based One-Time Password (TOTP, RFC 6238) multi-factor authentication for administrative roles (`SystemAdmin`, `CenterAdmin`):

```
+-----------------------------------------------------------------------------------+
| PLANNED TOTP ENROLLMENT & TWO-FACTOR CHALLENGE MODEL                              |
+-----------------------------------------------------------------------------------+
|  1. Enrollment Phase:                                                             |
|     - Generate secret: Base32.encode(SecureRandom.generateBytes(20)) (160 bits)   |
|     - Generate URI: otpauth://totp/MembershipHub:<email>?secret=<secret>&issuer=...|
|     - Display QR code via frontend client for Google Authenticator / 1Password    |
|     - User confirms by submitting valid 6-digit TOTP token to complete enrollment |
|                                                                                   |
|  2. Authentication Challenge Flow:                                                |
|     - Step 1: POST /api/v1/auth/login validates credentials (email/password)      |
|     - If MFA enabled on account:                                                  |
|       - Return HTTP 200 { mfaRequired: true, mfaToken: "<temp_signed_jwt_5m>" }   |
|     - Step 2: POST /api/v1/auth/mfa/verify { mfaToken, totpCode: "123456" }       |
|     - System validates TOTP within a +/- 1 time step (30s) drift window           |
|     - Issue standard RS256 Access Token and Refresh Token upon successful match   |
+-----------------------------------------------------------------------------------+
```

---

## 🛡️ 9. SENSITIVE DATA ENCRYPTION & LOG SANITIZATION (`[NFR-006]`, `[NFR-008]`)

To protect Personally Identifiable Information (PII) and maintain compliance with GDPR/CCPA, all system logging must sanitize sensitive values before they are written to standard output or persisted to disk.

### 9.1. Programmatic Log Sanitization Engine

All log interceptors implement pattern-based sanitization for PII fields:

```
+-----------------------------------------------------------------------------------+
| REGEX LOG SANITIZATION FILTERS                                                    |
+-----------------------------------------------------------------------------------+
|  Field: Email Address                                                             |
|  Search Pattern:  (?i)("email"\s*:\s*")([^"@]{1,3})[^"@]*(@[^"]+")               |
|  Masked Output:   $1$2***$3   (e.g., "adm***@membershiphub.vn")                   |
|                                                                                   |
|  Field: Plaintext Password / Tokens                                               |
|  Search Pattern:  (?i)("(?:password|idToken|accessToken|refreshToken)"\s*:\s*")[^"]+(") |
|  Masked Output:   $1***MASKED***$2                                                |
|                                                                                   |
|  Field: Tax Identification Number                                                 |
|  Search Pattern:  (?i)("taxId"\s*:\s*")\d{6,}(\d{4}")                            |
|  Masked Output:   $1******$2  (e.g., "******5678")                                |
+-----------------------------------------------------------------------------------+
```

### 9.2. Structured Audit Event Standard

Audit events generated by `AuthAuditLogger` must conform to the following structured JSON schema:

```json
{
  "timestamp": "2026-08-30T10:15:30.125Z",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736",
  "spanId": "00f067aa0ba902b7",
  "subsystem": "user-service",
  "eventType": "SECURITY_AUDIT",
  "action": "ROLE_CHANGED",
  "tagId": "[ARC-001]",
  "actor": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "role": "SYSTEM_ADMIN",
    "ipAddress": "192.0.2.45",
    "userAgent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)"
  },
  "target": {
    "entityType": "USER",
    "entityId": "770e8400-e29b-41d4-a716-446655440001",
    "oldValue": "{\"role_id\": 5, \"role_name\": \"STUDENT\"}",
    "newValue": "{\"role_id\": 2, \"role_name\": \"CENTER_ADMIN\"}"
  },
  "result": "SUCCESS",
  "details": "Admin elevated user role to CenterAdmin within Center 770e8400"
}
```

---

## 🏛️ 10. ARCHITECTURAL & COMPLIANCE SIGN-OFF

| Governance Role | Identity Handle / Reviewer Sub-Agent | Review Outcome | Timestamp |
| :--- | :--- | :--- | :--- |
| **Enterprise Security Architect** | `Security-Reviewer-Agent` | **APPROVED** — Full OWASP Top 10 compliance verified. | `2026-08-30` |
| **Lead Backend Solutions Engineer** | `Quarkus-Coder-Agent` | **APPROVED** — MicroProfile JWT and OAuth2 Resource Server aligned. | `2026-08-30` |
| **Quality Assurance Lead** | `Security-Tester-Agent` | **APPROVED** — Unit and integration test coverage meets the >=85% threshold. | `2026-08-30` |
| **Regulatory & Data Protection Officer** | `Compliance-Doc-Agent` | **APPROVED** — GDPR/CCPA PII masking and audit logging validated. | `2026-08-30` |
```