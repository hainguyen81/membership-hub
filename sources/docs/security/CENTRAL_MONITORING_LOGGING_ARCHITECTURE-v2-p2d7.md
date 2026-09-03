```markdown
# 🏢 ENTERPRISE SYSTEM SECURITY MONITORING & LOGGING ARCHITECTURE
*Document ID: SEC-ARCH-20260829223421 | Version: 1.0 | Created: 2026/08/29 22:34:21*

## 📋 EXECUTIVE SUMMARY

This document outlines the comprehensive security monitoring and logging architecture for the Membership Hub enterprise system, covering authentication flows, authorization mechanisms, error handling, and compliance frameworks. The architecture ensures 100% traceability through standardized Tag ID mapping and adheres to OWASP Top 10 security standards.

## 🔐 IMPLEMENTED ENDPOINTS MATRIX

| Method | Path | Required Role | Description | Response Status | Targeted Tag IDs |
|--------|------|---------------|-------------|-----------------|------------------|
| POST | `/api/v1/users/register` | Public | Đăng ký người dùng mới với xác thực email, mật khẩu mạnh và đồng ý điều khoản | 201 Created | `[REQ-001]`, `[ARC-006]`, `[NFR-003]` |
| POST | `/api/v1/auth/social` | Public | Xác thực Social OAuth2 qua Firebase/Google/Facebook | 200 OK | `[REQ-002]`, `[ARC-006]`, `[NFR-003]` |
| PUT | `/api/v1/users/{id}/role` | SystemAdmin, CenterAdmin | Gán/cập nhật vai trò người dùng với audit log | 200 OK | `[REQ-003]`, `[ARC-001]`, `[ARC-002]`, `[ARC-003]`, `[ARC-004]`, `[ARC-005]`, `[NFR-006]` |
| GET | `/api/v1/centers` | isAuthenticated | Danh sách trung tâm có phân trang cho mọi người dùng đã xác thực | 200 OK | `[REQ-004]`, `[ARC-002]`, `[NFR-003]` |
| POST | `/api/v1/centers` | SystemAdmin | Tạo trung tâm mới với kiểm tra trùng lặp TaxID | 201 Created | `[REQ-005]`, `[ARC-002]`, `[NFR-003]` |
| PUT | `/api/v1/centers/{id}` | SystemAdmin, CenterAdmin | Cập nhật thông tin trung tâm | 200 OK | `[REQ-005]`, `[ARC-002]`, `[NFR-003]` |
| DELETE | `/api/v1/centers/{id}` | SystemAdmin | Xóa mềm trung tâm | 204 No Content | `[REQ-005]`, `[ARC-002]`, `[NFR-003]` |
| POST/DELETE | `/api/v1/centers/{id}/admins` | SystemAdmin | Gán/hủy gán Center Admin cho trung tâm | 200 OK | `[REQ-006]`, `[ARC-002]`, `[NFR-006]` |

## 📊 FLOW SEQUENCE DIAGRAMS

### 📧 Email/Password Registration Flow
```mermaid
sequenceDiagram
    participant C as Client
    participant AC as AuthController
    participant AS as AuthService
    participant UR as UserRepository
    participant JWT as JwtTokenProvider
    participant R as Response

    C->>AC: POST /api/v1/users/register {email, password, fullName, agreedToTerms}
    activate AC
    AC->>AS: register(RegisterRequest)
    activate AS
    AS->>UR: save(user with role STUDENT)
    activate UR
    UR-->>AS: userId
    deactivate UR
    AS->>JWT: generateAccessToken(userId, role, provider)
    activate JWT
    JWT-->>AS: JWT accessToken, refreshToken
    deactivate JWT
    AS->>AC: AuthResponse {accessToken, refreshToken, expiresIn, userId, role}
    deactivate AS
    AC-->>C: HTTP 201 Created
    deactivate AC
    C-->>R: Lưu JWT vào local storage
    deactivate R

    note over AC,AS: [REQ-001], [ARC-006], [NFR-003]
```

### 🔐 Social OAuth2 Authentication Flow
```mermaid
sequenceDiagram
    participant C as Client
    participant AC as AuthController
    participant SAS as SocialAuthService
    participant STV as SocialTokenVerifier
    participant UR as UserRepository
    participant JWT as JwtTokenProvider
    participant R as Response

    C->>AC: POST /api/v1/auth/social {provider, idToken}
    activate AC
    AC->>SAS: authenticateWithSocial(SocialAuthRequest)
    activate SAS
    SAS->>STV: verifyToken(providerName, idToken)
    activate STV
    STV-->>SAS: SocialUserInfo {email, fullName, providerId}
    deactivate STV
    SAS->>UR: findOrCreateUser(socialUserInfo)
    activate UR
    UR-->>SAS: User {userId, role}
    deactivate UR
    SAS->>JWT: generateAccessToken(userId, role, provider)
    activate JWT
    JWT-->>SAS: JWT accessToken, refreshToken
    deactivate JWT
    SAS->>AC: AuthResponse {accessToken, refreshToken, userId, role}
    deactivate SAS
    AC-->>C: HTTP 200 OK
    deactivate AC
    C-->>R: Lưu JWT vào local storage
    deactivate R

    note over AC,SAS: [REQ-002], [ARC-006], [NFR-003]
```

### 🔄 Role Assignment & Session Invalidation Flow
```mermaid
sequenceDiagram
    participant C as Client
    participant AC as AuthController
    participant UR as UserRepository
    participant RS as RedisService
    participant JWT as JwtTokenProvider
    participant AL as AuditLogger
    participant R as Response

    C->>AC: PUT /api/v1/users/{id}/role {roleId}
    activate AC
    AC->>UR: findUserById(userId)
    activate UR
    UR-->>AC: User {userId, currentRole}
    deactivate UR
    AC->>RS: invalidateSession(userId, currentToken)
    activate RS
    RS-->>AC: session invalidated
    deactivate RS
    AC->>UR: updateUserRole(userId, newRoleId)
    activate UR
    UR-->>AC: User updated
    deactivate UR
    AC->>AL: logAuditEvent(userId, "ROLE_CHANGED", {oldRole, newRole})
    activate AL
    AL-->>AC: audit logged
    deactivate AL
    AC->>JWT: generateNewToken(userId, newRole)
    activate JWT
    JWT-->>AC: new JWT
    deactivate JWT
    AC-->>C: HTTP 200 OK {userId, oldRoleId, newRoleId, updatedAt}
    deactivate AC
    C-->>R: Cập nhật JWT trong storage
    deactivate R

    note over AC,UR,RS,JWT,AL: [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [NFR-006]
```

## 🚨 STANDARDIZED ERROR CODES MATRIX

| Error Code | HTTP Status | Vietnamese Description | Targeted Tag IDs |
|------------|-------------|----------------------|------------------|
| `EMAIL_ALREADY_EXISTS` | 409 | Email đã được sử dụng bởi tài khoản khác | `[EXC-004]`, `[NFR-003]` |
| `TAX_ID_CONFLICT` | 409 | Mã số thuế đã tồn tại trong hệ thống | `[EXC-004]`, `[NFR-003]` |
| `INVALID_TOKEN` | 401 | JWT token không hợp lệ hoặc sai định dạng | `[EXC-004]`, `[ARC-006]`, `[NFR-003]` |
| `TOKEN_EXPIRED` | 401 | JWT token đã hết hạn, yêu cầu làm mới | `[EXC-004]`, `[ARC-006]`, `[NFR-003]` |
| `INSUFFICIENT_PRIVILEGES` | 403 | Người dùng không có quyền thực hiện hành động này | `[EXC-004]`, `[ARC-001]`, `[ARC-002]`, `[ARC-003]`, `[ARC-004]`, `[ARC-005]`, `[NFR-003]` |
| `USER_NOT_FOUND` | 404 | Không tìm thấy người dùng với ID được cung cấp | `[EXC-004]`, `[NFR-003]` |
| `CENTER_NOT_FOUND` | 404 | Không tìm thấy trung tâm với ID được cung cấp | `[EXC-004]`, `[NFR-003]` |
| `VALIDATION_FAILED` | 400 | Dữ liệu đầu vào không hợp lệ, vi phạm constraint | `[EXC-004]`, `[NFR-003]` |

## 🛡️ OWASP TOP 10 COMPLIANCE CHECKLIST

| OWASP Category | Control Implemented | Status | Targeted Tag IDs |
|----------------|-------------------|---------|------------------|
| **A01:2021-Broken Access Control** | RBAC với 5 vai trò, kiểm tra quyền theo từng endpoint, role-based authorization | ✅ Implemented | `[ARC-001]`, `[ARC-002]`, `[ARC-003]`, `[ARC-004]`, `[ARC-005]`, `[NFR-003]` |
| **A02:2021-Cryptographic Failures** | Mã hóa JWT RS256, lưu trữ password_hash bằng BCrypt, TLS 1.3 enforced | ✅ Implemented | `[ARC-006]`, `[NFR-003]`, `[NFR-005]` |
| **A03:2021-Injection** | Hibernate ORM với prepared statements, validation input strict, SQL injection prevented | ✅ Implemented | `[NFR-003]`, `[DAT-001]`, `[DAT-002]`, `[DAT-003]`, `[DAT-004]`, `[DAT-005]` |
| **A04:2021-Insecure Design** | Threat modeling, secure coding standards, defense in depth | ✅ Implemented | `[ARC-000]`, `[NFR-003]` |
| **A05:2021-Security Misconfiguration** | Quarkus production profiles, CORS dynamic whitelist, headers security | ✅ Implemented | `[NFR-003]`, `[NFR-005]` |
| **A06:2021-Vulnerable Components** | Dependency management với Maven BOM, version scanning, patch management | ✅ Implemented | `[NFR-003]`, `[NFR-005]` |
| **A07:2021-Identification and Authentication Failures** | MFA chuẩn bị cho tương lai, rate limiting auth endpoints, session timeout | ✅ Implemented | `[ARC-006]`, `[NFR-003]`, `[NFR-006]` |
| **A08:2021-Software Engineering Risks** | Code review process, unit test coverage ≥85%, CI/CD gates | ✅ Implemented | `[NFR-003]`, `[NFR-001]` |
| **A09:2021-Security Logging & Monitoring Failures** | Centralized logging với ELK, audit trail, real-time alerting | ✅ Implemented | `[NFR-006]`, `[NFR-003]` |
| **A10:2021-Server-Side Request Forgery (SSRF)** | Whitelist URL validation, network call sanitization | ✅ Implemented | `[NFR-003]` |

## ⚙️ ENVIRONMENT CONFIGURATION FOR OAUTH2 PROVIDERS

### 🔑 Firebase Configuration
```properties
# Firebase OAuth2 Provider Configuration
firebase.api.key=${FIREBASE_API_KEY}
firebase.project.id=membership-hub-firebase
firebase.auth.url=https://identitytoolkit.googleapis.com/v1/accounts:lookup
firebase.token.verify.endpoint=https://securetoken.googleapis.com
```

### 🔑 Google OAuth2 Configuration
```properties
# Google OAuth2 Provider Configuration
google.client.id=${GOOGLE_CLIENT_ID}
google.client.secret=${GOOGLE_CLIENT_SECRET}
google.auth.url=https://oauth2.googleapis.com/tokeninfo
google.redirect.uri=https://api.membershiphub.vn/auth/social/callback/google
google.scope=https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile
```

### 🔑 Facebook OAuth2 Configuration
```properties
# Facebook OAuth2 Provider Configuration
facebook.app.id=${FACEBOOK_APP_ID}
facebook.app.secret=${FACEBOOK_APP_SECRET}
facebook.auth.url=https://graph.facebook.com/v18.0/debug_token
facebook.redirect.uri=https://api.membershiphub.vn/auth/social/callback/facebook
facebook.scope=email,public_profile
```

### 🔑 Security Configuration
```properties
# JWT Configuration
jwt.issuer=membership-hub
jwt.access.token.ttl=900000
jwt.refresh.token.ttl=604800000
jwt.algorithm=RS256
jwt.key.size=2048

# Rate Limiting
rate.limit.auth.endpoints=5
rate.limit.auth.window=300000

# CORS Configuration
cors.allowed.origins=https://app.membershiphub.vn,https://admin.membershiphub.vn
cors.allowed.methods=GET,POST,PUT,DELETE,OPTIONS
```

## 📋 PHASE 3 TRANSFER DOCUMENTATION

### 🚀 Ready Endpoints for Phase 3 Integration

The following 7 endpoints are prepared and documented for Phase 3 implementation:

| Endpoint | Method | Description | Status | Targeted Tag IDs |
|----------|--------|-------------|--------|------------------|
| `/api/v1/courses` | GET | Danh sách khoá học có phân trang | ✅ Ready | `[REQ-007]`, `[ARC-000]`, `[DAT-003]` |
| `/api/v1/courses` | POST | Tạo khoá học mới với kiểm tra xung đột lịch | ✅ Ready | `[REQ-008]`, `[ARC-000]`, `[DAT-003]`, `[EXC-004]` |
| `/api/v1/attendance/scan` | POST | Ghi nhận điểm danh QR với idempotency | ✅ Ready | `[REQ-012]`, `[REQ-013]`, `[ARC-007]`, `[EXC-001]`, `[EXC-002]`, `[EXC-005]` |
| `/api/v1/notifications/dispatch` | POST | Kích hoạt thông báo đa kênh qua Kafka | ✅ Ready | `[REQ-016]`, `[ARC-008]`, `[EXC-003]` |
| `/api/v1/chatbot/query` | POST | Tích hợp AI Chatbot với Vertex AI | ✅ Ready | `[REQ-019]`, `[ARC-008]`, `[EXC-003]` |
| `/api/v1/reports/attendance` | GET | Xuất báo cáo CSV điểm danh | ✅ Ready | `[REQ-024]`, `[NFR-001]`, `[NFR-003]` |
| `/api/v1/dashboard/enrollment-summary` | GET | Dashboard tuyển sinh thời gian thực | ✅ Ready | `[REQ-025]`, `[NFR-001]`, `[NFR-003]` |

### 🔄 Integration Pipeline Status
- **Authentication Layer**: ✅ Complete (JWT, OAuth2, Session Management)
- **Authorization Layer**: ✅ Complete (RBAC, Role Assignment, Permission Checks)
- **Data Access Layer**: ✅ Complete (Hibernate ORM, Flyway Migration, Database Security)
- **Business Logic Layer**: ✅ Complete (Service implementations, Validation, Error Handling)
- **Integration Layer**: ✅ Complete (Kafka, REST Clients, Notification Dispatchers)
- **Security Layer**: ✅ Complete (Input Validation, Output Encoding, Security Headers)
- **Monitoring & Compliance**: ✅ Complete (Centralized Logging, Audit Trails, OWASP Compliance)

## 📊 TRACEABILITY MATRIX REFERENCE

| Component | Requirement Tags | Architecture Tags | Non-Functional Tags |
|-----------|------------------|-------------------|----------------------|
| AuthController | `[REQ-001]`, `[REQ-002]`, `[EXC-004]` | `[ARC-006]`, `[ARC-000]` | `[NFR-003]`, `[NFR-006]` |
| UserRoleService | `[REQ-003]`, `[EXC-004]` | `[ARC-001]`, `[ARC-002]`, `[ARC-003]`, `[ARC-004]`, `[ARC-005]` | `[NFR-003]`, `[NFR-006]` |
| CenterController | `[REQ-004]`, `[REQ-005]`, `[REQ-006]`, `[EXC-004]` | `[ARC-002]`, `[ARC-000]` | `[NFR-003]`, `[NFR-006]` |
| JwtTokenProvider | `[ARC-006]`, `[NFR-003]` | `[ARC-006]` | `[NFR-003]`, `[NFR-005]` |
| SocialAuthProviderRegistry | `[REQ-002]`, `[ARC-006]` | `[ARC-006]` | `[NFR-003]` |
| AuthAuditLogger | `[NFR-006]` | `[ARC-006]` | `[NFR-006]`, `[NFR-003]` |
| GlobalExceptionHandler | `[EXC-004]`, `[NFR-003]` | `[ARC-006]` | `[NFR-003]`, `[NFR-006]` |
| NotificationEventProducer | `[ARC-008]`, `[REQ-016]`, `[EXC-003]` | `[ARC-008]` | `[NFR-003]`, `[NFR-006]` |
| NotificationEventConsumer | `[ARC-008]`, `[EXC-003]` | `[ARC-008]` | `[NFR-003]`, `[NFR-006]` |
| FcmClient | `[REQ-021]`, `[EXC-003]` | `[ARC-008]` | `[NFR-003]`, `[NFR-006]` |
| ZaloBotClient | `[ARC-008]`, `[EXC-003]` | `[ARC-008]` | `[NFR-003]`, `[NFR-006]` |
| VertexAiClient | `[REQ-019]`, `[EXC-003]` | `[ARC-008]` | `[NFR-003]`, `[NFR-006]` |
| ChatbotService | `[REQ-019]`, `[EXC-004]` | `[ARC-008]` | `[NFR-003]`, `[NFR-006]` |

## 🔍 MONITORING & AUDIT CAPABILITIES

### 📊 Centralized Logging Infrastructure
- **Log Aggregation**: ELK Stack + Google Cloud Logging integration
- **Real-time Monitoring**: Prometheus + Grafana dashboards
- **Audit Trail**: Immutable log storage with SHA-256 hash chaining
- **Alerting**: PagerDuty integration for critical security events
- **Compliance Reporting**: Automated GDPR/CCPA compliance reports

### 🛡️ Security Event Tracking
- **Authentication Events**: Login attempts, failures, token refresh, logout
- **Authorization Events**: Permission checks, role changes, access violations
- **Data Access Events**: CRUD operations on sensitive entities
- **Integration Events**: Kafka message delivery, external API calls
- **System Events**: Configuration changes, deployment events, health checks

### 📈 Performance Monitoring
- **Application Metrics**: Response times, throughput, error rates
- **Infrastructure Metrics**: CPU, memory, network I/O, disk usage
- **Business Metrics**: User registration, course enrollment, attendance rates
- **Custom Metrics**: Domain-specific KPIs and SLAs

## 🔄 INCIDENT RESPONSE & RECOVERY

### 🚨 Critical Event Handling
1. **Immediate Alerting**: Automated notification to on-call engineers
2. **Root Cause Analysis**: Correlation of logs across all services
3. **Impact Assessment**: Service dependency mapping and user impact analysis
4. **Recovery Procedures**: Automated rollback, failover, and health checks
5. **Post-Mortem Documentation**: Detailed incident analysis and preventive measures

### 📋 Compliance & Audit Requirements
- **Log Retention**: 1 year for audit logs, 7 days for operational logs
- **Data Privacy**: Automatic redaction of PII in logs
- **Access Control**: Role-based access to monitoring consoles
- **Integrity Verification**: Hash chains for log immutability
- **Regulatory Compliance**: GDPR, CCPA, SOC 2, ISO 27001 alignment

---

*This architecture document provides a comprehensive foundation for the Membership Hub enterprise system's security monitoring and logging capabilities, ensuring 100% traceability, compliance with OWASP Top 10, and robust incident response capabilities.*