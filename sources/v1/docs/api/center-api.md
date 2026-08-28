markdown
# Center Management API Specification
**Target Documentation Path:** `./sources/docs/api/center-api.md`  
**Implementation Location:** `./sources/backend/center-service/src/main/java/org/nlh4j/saas/center/CenterResource.java`  
**Service Layer:** `./sources/backend/center-service/src/main/java/org/nlh4j/saas/center/CenterService.java`  
**Repository Layer:** `./sources/backend/center-service/src/main/java/org/nlh4j/saas/center/repository/CenterRepository.java`  
**Database Migration:** `./sources/backend/center-service/src/main/resources/db/migration/V1__init_user_center_schema.sql`  

## 📋 Endpoint Specifications

| HTTP Method | Full Endpoint | Targeted Tag IDs | Request Headers | Path / Query Parameters | JSON Request Payload Schema | JSON Response Schema (Success) | JSON Response Schema (Failure) |
|-------------|---------------|------------------|-----------------|--------------------------|-----------------------------|--------------------------------|--------------------------------|
| `GET` | `/api/v1/centers` | `[REQ-004]` | `Authorization: Bearer <JWT>` | `page: int (optional, default 1)`<br>`size: int (optional, default 20)` | — | json { "page": 1, "size": 20, "totalElements": 12, "totalPages": 2, "centers": [ { "centerId": "uuid", "name": "Center A", "address": "123 Main St", "taxId": "1234567890", "contactPhone": "+1-555-0123", "contactEmail": "admin@a.com" } ] }  | json { "timestamp": "2026-08-18T16:31:58Z", "status": 500, "error": "INTERNAL_SERVER_ERROR", "message": "Unexpected server error", "path": "/api/v1/centers" }  |
| `POST` | `/api/v1/admin/centers` | `[REQ-005]` | `Authorization: Bearer <JWT>`<br>`Idempotency-Key: <string>` | — | json { "name": "Center B", "address": "456 Oak Ave", "taxId": "0987654321", "contactPhone": "+1-555-0124", "contactEmail": "admin@b.com" }  | json { "centerId": "uuid", "name": "Center B", "address": "456 Oak Ave", "taxId": "0987654321", "contactPhone": "+1-555-0124", "contactEmail": "admin@b.com", "createdAt": "2026-08-18T16:31:58Z" }  | json { "timestamp": "2026-08-18T16:31:58Z", "status": 409, "error": "CONFLICT", "message": "Tax ID already exists", "path": "/api/v1/admin/centers" }  |
| `PUT` | `/api/v1/admin/centers/{centerId}` | `[REQ-005]` | `Authorization: Bearer <JWT>`<br>`Idempotency-Key: <string>` | `centerId: uuid (path)` | json { "name": "Center B Updated", "address": "456 Oak Ave, Suite 200", "contactPhone": "+1-555-0125", "contactEmail": "updated@b.com" }  | json { "centerId": "uuid", "name": "Center B Updated", "address": "456 Oak Ave, Suite 200", "taxId": "0987654321", "contactPhone": "+1-555-0125", "contactEmail": "updated@b.com", "updatedAt": "2026-08-18T16:31:58Z" }  | json { "timestamp": "2026-08-18T16:31:58Z", "status": 404, "error": "NOT_FOUND", "message": "Center not found", "path": "/api/v1/admin/centers/{centerId}" }  |
| `DELETE` | `/api/v1/admin/centers/{centerId}` | `[REQ-005]` | `Authorization: Bearer <JWT>`<br>`Idempotency-Key: <string>` | `centerId: uuid (path)` | — | json { "centerId": "uuid", "message": "Center deleted successfully" }  | json { "timestamp": "2026-08-18T16:31:58Z", "status": 409, "error": "CONFLICT", "message": "Center still has active courses or enrollments", "path": "/api/v1/admin/centers/{centerId}" }  |
| `POST` | `/api/v1/admin/centers/{centerId}/admins` | `[REQ-006]` | `Authorization: Bearer <JWT>`<br>`Idempotency-Key: <string>` | `centerId: uuid (path)` | json { "userId": "uuid", "isAssign": true }  | json { "userId": "uuid", "centerId": "uuid", "assignedAt": "2026-08-18T16:31:58Z", "message": "User assigned as Center Admin successfully" }  | json { "timestamp": "2026-08-18T16:31:58Z", "status": 404, "error": "NOT_FOUND", "message": "User or Center not found", "path": "/api/v1/admin/centers/{centerId}/admins" }  |

## 🔍 Traceability Matrix Reference

| Component / Artifact | Associated Requirement Tags | Description |
|----------------------|----------------------------|-------------|
| **CenterResource** (`./sources/backend/center-service/src/main/java/org/nlh4j/saas/center/CenterResource.java`) | `[REQ-004]`, `[REQ-005]`, `[REQ-006]` | Exposes all CRUD and admin assignment endpoints for Center management. |
| **CenterService** (`./sources/backend/center-service/src/main/java/org/nlh4j/saas/center/CenterService.java`) | `[REQ-004]`, `[REQ-005]`, `[REQ-006]` | Implements business logic for center operations, validation, and RBAC checks. |
| **CenterRepository** (`./sources/backend/center-service/src/main/java/org/nlh4j/saas/center/repository/CenterRepository.java`) | `[REQ-004]`, `[REQ-005]`, `[REQ-006]` | Provides JPA access to the `centers` table using prepared statements for SQL injection protection. |
| **Database Migration V1** (`./sources/backend/center-service/src/main/resources/db/migration/V1__init_user_center_schema.sql`) | `[DAT-001]`, `[DAT-003]` | Creates `roles`, `users`, and `centers` tables with constraints (unique taxId, foreign keys). |
| **Kafka Topic** `center.management.events` | `[ARC-008]` | Emits events for center creation, update, deletion, and admin assignment for downstream notification services. |
| **RBAC Middleware** (`./sources/backend/auth/src/main/java/com/hub/security/RbacMiddleware.java`) | `[ARC-001]`, `[ARC-002]`, `[ARC-003]`, `[ARC-004]`, `[ARC-005]` | Enforces role‑based access control for all Center endpoints (System Admin, Center Admin). |
| **Security Controls** | `[NFR-003]`, `[NFR-006]` | Enforces TLS 1.3, prepared statements, input validation, and PII masking in logs. |
| **Logging & Auditing** | `[NFR-006]` | All center operations are logged at INFO level with Tag IDs; errors logged at ERROR with full context. |
| **Exception Handling** | `[EXC-004]` | Validation failures (e.g., duplicate taxId) produce structured error payloads with clear messages. |
| **Idempotency** | `[NFR-003]` | All mutation endpoints require an `Idempotency-Key` header to guarantee exactly‑once processing. |

## 📌 Implementation Notes

- **SQL Injection Prevention:** All repository methods use Spring Data JPA’s derived queries or native `Query` annotations with parameter binding, ensuring prepared statements.
- **RBAC Enforcement:** The `RbacMiddleware` validates the JWT‑derived role against a per‑endpoint permission matrix before request processing.
- **Idempotency:** An `Idempotency-Key` header is required for all `POST`, `PUT`, and `DELETE` operations. The service layer checks this key against a Redis store (TTL 24 h) to guarantee duplicate‑request safety.
- **Audit Logging:** Each request is logged at `INFO` on entry and exit. Errors are logged at `ERROR` with the format:  
  `logger.error("[CRITICAL_FAIL] [ARC-007] Center operation failed due to ... Raw error: {}", e.getMessage())`.
- **Error Payloads:** All error responses follow a consistent structure: `timestamp`, `status`, `error`, `message`, `path`.
- **Security Headers:** The API gateway injects `Content-Security-Policy`, `Strict‑Transport‑Security`, and `X‑Frame‑Options` headers to enforce CSP and prevent XSS.
- **Multi‑Tenant Isolation:** Center resources are scoped by `centerId` in the repository methods; queries automatically filter by the authenticated user’s assigned center (for Center Admin roles).

## 🔧 Deployment & Runtime

- **Container Image:** Built via multi‑stage Docker (see `./sources/infra/docker/Dockerfile`) targeting `distroless/java21` with a final size < 500 MB.
- **Kubernetes Deployment:** Defined in `./sources/infra/gke/deployment.yaml` with resource limits (`cpu: 500m`, `memory: 1Gi`), liveness/readiness probes, and HPA based on CPU > 70 % or request latency > 300 ms.
- **Observability:** Integrated with GCP Cloud Logging and Cloud Monitoring; all center API logs are exported to Cloud Logging with structured metadata for real‑time dashboards.
- **Backup & Recovery:** PostgreSQL backups run daily via a CronJob (see `./sources/infra/gcp/monitoring-backup.yaml`) with point‑in‑time recovery up to 24 hours.

---

*This documentation is auto‑generated to satisfy enterprise traceability, security, and operational requirements. All referenced Tag IDs (`[REQ-004]`, `[REQ-005]`, `[REQ-006]`, etc.) are permanently embedded for auditability and compliance.*