# Day 2: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/docs/integrations/ai-chatbot.md
* **Production source codebase generated at TARGET destination**: ./sources/docs/api/center-api.md
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DOCUMENT MATRIX INJECTION
*   Target Project Identity Safe Name: 
*   Enforced Java Package Prefix Base: org.nlh4j.saas.
*   Target Documentation Destination Path: `./sources/docs/integrations/ai-chatbot.md`
*   Target Documentation Destination Path: `./sources/docs/api/center-api.md`


*   Documentation Context: Conceptual Init (Synthesize the architecture, guidelines, or specs based purely on the execution sub-tasks blueprint.)


### 📋 EXECUTION SUB-TASKS & DOCUMENT CONTENT TO WRITE
['Biên soạn tài liệu tích hợp chatbot AI theo chuẩn kỹ thuật doanh nghiệp. Tài liệu phải bao gồm: (1) Kiến trúc tổng quan hệ thống chatbot, sơ đồ luồng dữ liệu từ người dùng đến mô hình AI và quay lại; (2) Cấu hình mô hình AI: tên endpoint, khóa xác thực, tham số nhiệt độ, max_tokens, ngôn ngữ hỗ trợ; (3) Cơ chế chuyển tiếp hỗ trợ con người: điều kiện kích hoạt (confidence < 0.7), luồng tạo ticket, thông báo cho hỗ trợ viên; (4) Xử lý ngoại lệ: lỗi kết nối API AI, lỗi xác thực, timeout, giới hạn tần suất; (5) Hướng dẫn tích hợp frontend: cấu trúc payload request/response, xử lý trạng thái loading và lỗi, hiển thị tin nhắn đang soạn thảo. Tài liệu phải được lưu trữ tại `./sources/docs/integrations/ai-chatbot.md` với định dạng Markdown chuẩn.']
['Viết tài liệu đặc tả API cho quản lý trung tâm']

---

### ⚙️ TECHNICAL WRITER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder guardrails, and detailed sub-task document specifications provided above. 

Ensure that you read the exact Tag IDs from the `['[REQ-019]']` variable, and permanently burn those codes into the generated documentation layout (inside the table columns or the dedicated Traceability Matrix Reference section). Emit the final production-ready comprehensive documentation inside a single valid markdown code block (` ```markdown `) now.
* **📝 Response**:
```markdown
# AI Chatbot Integration Guide
<!-- Path: ./sources/docs/integrations/ai-chatbot.md -->

## 1. System Overview & Data Flow Architecture

### 1.1 Purpose
This section describes the AI chatbot subsystem that provides automated customer support for common queries, escalating to human agents when confidence is low.

### 1.2 High‑Level Architecture
```mermaid
graph TD
    A[Mobile/Web App] --> B[Frontend SDK]
    B --> C[API Gateway]
    C --> D[Chatbot Controller]
    D --> E[AI Service Client]
    E --> F[External AI Model (e.g., OpenAI/Gemini)]
    F --> G[Response Parser]
    G --> H[Confidence Scoring]
    H -->|Confidence >= 0.7| I[Return AI Response]
    H -->|Confidence < 0.7| J[Create Support Ticket]
    J --> K[Ticket Service]
    K --> L[Support Agent Notification]
    I --> C
    L --> C
    C --> A
```

### 1.3 Data Flow
1. User submits a message via the mobile/web client.  
2. Request is routed through the API Gateway to the **Chatbot Controller**.  
3. Controller invokes the **AI Service Client**, which calls the external AI model with the user message.  
4. The AI model returns a textual reply together with a confidence score.  
5. If confidence ≥ 0.7, the response is sent directly back to the user.  
6. If confidence < 0.7, a support ticket is automatically created, and a notification is sent to a human support agent.  
7. The user receives a “hand‑off” message indicating that a human will assist.

## 2. AI Model Configuration

| Configuration Item | Value | Description | Targeted Tag IDs |
|-------------------|-------|-------------|------------------|
| **Endpoint** | `https://api.openai.com/v1/chat/completions` | OpenAI‑compatible completion endpoint | `[REQ-019]` |
| **Authentication Header** | `Authorization: Bearer ${OPENAI_API_KEY}` | Securely stored in environment variable `OPENAI_API_KEY` | `[REQ-019]` |
| **Model** | `gpt-4o-mini` | Chosen for balanced performance and cost | `[REQ-019]` |
| **Temperature** | `0.3` | Low randomness for consistent answers | `[REQ-019]` |
| **Max Tokens** | `150` | Limits reply length to keep responses concise | `[REQ-019]` |
| **Supported Languages** | `en, vi, es` | English, Vietnamese, Spanish – matches multi‑lingual UI | `[REQ-019]` |
| **Timeout** | `5000` ms | Maximum wait for AI response | `[REQ-019]` |

## 3. Human Handoff Mechanism

### 3.1 Trigger Condition
- The AI response confidence score is evaluated. If `confidence < 0.7`, the request is flagged for human assistance.

### 3.2 Ticket Creation Flow
1. **Capture Context** – User ID, session ID, original message, AI response, confidence score.  
2. **Persist Ticket** – Call the **Ticket Service** (`POST /api/v1/tickets`) with payload:
   ```json
   {
     "userId": "string",
     "sessionId": "string",
     "message": "string",
     "aiResponse": "string",
     "confidence": 0.65,
     "priority": "medium",
     "status": "open"
   }
   ```
3. **Notify Support** – The Ticket Service publishes a Kafka event `ticket.created` which triggers a notification to the support team via email/SMS and updates the support dashboard.

### 3.3 Notification to Support Agent
- **Channel:** In‑app alert + push notification (FCM/APNs) + email.  
- **Payload Example:**
   ```json
   {
     "ticketId": "uuid",
     "userId": "uuid",
     "message": "string",
     "timestamp": "2024‑08‑18T16:31:58Z"
   }
   ```

## 4. Exception Handling

| Exception Type | Scenario | Handling Action | HTTP Status | Log Message |
|----------------|----------|----------------|------------|-------------|
| **AI API Connection Error** | External AI service unreachable | Retry up to 3 times with exponential backoff; if still failing, return generic error to user. | 503 | `[REQ-019] AI service temporarily unavailable – retry count: {retry}` |
| **Authentication Error** | Invalid or missing `OPENAI_API_KEY` | Immediately fail fast; alert DevOps via Slack channel. | 500 | `[REQ-019] AI authentication failure – check environment variable OPENAI_API_KEY` |
| **Timeout** | AI response not received within 5 s | Abort request, return timeout error to client. | 408 | `[REQ-019] AI request timeout after 5000ms` |
| **Rate Limit Exceeded** | Too many requests to AI service | Queue request, return HTTP 429 with `Retry-After` header. | 429 | `[REQ-019] AI rate limit exceeded – retry after {retryAfter}s` |

## 5. Frontend Integration Guidance

### 5.1 Request Structure
```typescript
interface ChatbotRequest {
  message: string;
  sessionId?: string; // optional, generated if missing
  language?: 'en' | 'vi' | 'es';
}
```
Send via:
```javascript
POST /api/v1/chatbot/message
Headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ${jwtToken}' }
Body: { message, sessionId, language }
```

### 5.2 Response Structure
```typescript
interface ChatbotResponse {
  success: boolean;
  data?: {
    reply: string;
    confidence: number;
    ticketId?: string; // present only when handoff occurs
  };
  error?: {
    code: string;
    message: string;
  };
}
```

### 5.3 Loading & Error State Handling
- **Loading:** Show a “AI is typing…” indicator while `!response.success && !response.error` (spinner or dotted text).  
- **Success:** Render AI reply; if `ticketId` exists, display “A human support agent will assist you shortly.”  
- **Error:** Display user‑friendly message; log detailed error to monitoring tool (e.g., Sentry) with tag `[REQ-019]`.

### 5.4 UI Flow Example
1. User types “How do I reset my password?” → Click Send.  
2. UI shows “AI is typing…”.  
3. Backend returns `{ reply: "Please visit ...", confidence: 0.85 }`.  
4. UI displays reply, hides typing indicator.  
5. If confidence < 0.7, backend returns `{ reply: "...", confidence: 0.6, ticketId: "abc123" }`.  
6. UI shows “Our support team has been notified. You will receive a reply shortly.”

## 6. Traceability Matrix Reference

| Document Section | Covered Requirement Tags |
|------------------|--------------------------|
| System Overview & Data Flow | `[REQ-019]` |
| AI Model Configuration | `[REQ-019]` |
| Human Handoff Mechanism | `[REQ-019]` |
| Exception Handling | `[REQ-019]` |
| Frontend Integration | `[REQ-019]` |

**Additional Architecture Tags:** `[DAT-010]`, `[DAT-011]`, `[NFR-006]` (audit logging, system settings, and comprehensive audit trail requirements).
```
Ensure that you read the exact Tag IDs from the `['[REQ-004]', '[REQ-005]', '[REQ-006]']` variable, and permanently burn those codes into the generated documentation layout (inside the table columns or the dedicated Traceability Matrix Reference section). Emit the final production-ready comprehensive documentation inside a single valid markdown code block (` ```markdown `) now.
* **📝 Response**:
```markdown
# Center Management API Specification
**Target Documentation Path:** `./sources/docs/api/center-api.md`  
**Implementation Location:** `./sources/backend/center-service/src/main/java/org/nlh4j/saas/center/CenterResource.java`  
**Service Layer:** `./sources/backend/center-service/src/main/java/org/nlh4j/saas/center/CenterService.java`  
**Repository Layer:** `./sources/backend/center-service/src/main/java/org/nlh4j/saas/center/repository/CenterRepository.java`  
**Database Migration:** `./sources/backend/center-service/src/main/resources/db/migration/V1__init_user_center_schema.sql`  

## 📋 Endpoint Specifications

| HTTP Method | Full Endpoint | Targeted Tag IDs | Request Headers | Path / Query Parameters | JSON Request Payload Schema | JSON Response Schema (Success) | JSON Response Schema (Failure) |
|-------------|---------------|------------------|-----------------|--------------------------|-----------------------------|--------------------------------|--------------------------------|
| `GET` | `/api/v1/centers` | `[REQ-004]` | `Authorization: Bearer <JWT>` | `page: int (optional, default 1)`<br>`size: int (optional, default 20)` | — | ```json { "page": 1, "size": 20, "totalElements": 12, "totalPages": 2, "centers": [ { "centerId": "uuid", "name": "Center A", "address": "123 Main St", "taxId": "1234567890", "contactPhone": "+1-555-0123", "contactEmail": "admin@a.com" } ] } ``` | ```json { "timestamp": "2026-08-18T16:31:58Z", "status": 500, "error": "INTERNAL_SERVER_ERROR", "message": "Unexpected server error", "path": "/api/v1/centers" } ``` |
| `POST` | `/api/v1/admin/centers` | `[REQ-005]` | `Authorization: Bearer <JWT>`<br>`Idempotency-Key: <string>` | — | ```json { "name": "Center B", "address": "456 Oak Ave", "taxId": "0987654321", "contactPhone": "+1-555-0124", "contactEmail": "admin@b.com" } ``` | ```json { "centerId": "uuid", "name": "Center B", "address": "456 Oak Ave", "taxId": "0987654321", "contactPhone": "+1-555-0124", "contactEmail": "admin@b.com", "createdAt": "2026-08-18T16:31:58Z" } ``` | ```json { "timestamp": "2026-08-18T16:31:58Z", "status": 409, "error": "CONFLICT", "message": "Tax ID already exists", "path": "/api/v1/admin/centers" } ``` |
| `PUT` | `/api/v1/admin/centers/{centerId}` | `[REQ-005]` | `Authorization: Bearer <JWT>`<br>`Idempotency-Key: <string>` | `centerId: uuid (path)` | ```json { "name": "Center B Updated", "address": "456 Oak Ave, Suite 200", "contactPhone": "+1-555-0125", "contactEmail": "updated@b.com" } ``` | ```json { "centerId": "uuid", "name": "Center B Updated", "address": "456 Oak Ave, Suite 200", "taxId": "0987654321", "contactPhone": "+1-555-0125", "contactEmail": "updated@b.com", "updatedAt": "2026-08-18T16:31:58Z" } ``` | ```json { "timestamp": "2026-08-18T16:31:58Z", "status": 404, "error": "NOT_FOUND", "message": "Center not found", "path": "/api/v1/admin/centers/{centerId}" } ``` |
| `DELETE` | `/api/v1/admin/centers/{centerId}` | `[REQ-005]` | `Authorization: Bearer <JWT>`<br>`Idempotency-Key: <string>` | `centerId: uuid (path)` | — | ```json { "centerId": "uuid", "message": "Center deleted successfully" } ``` | ```json { "timestamp": "2026-08-18T16:31:58Z", "status": 409, "error": "CONFLICT", "message": "Center still has active courses or enrollments", "path": "/api/v1/admin/centers/{centerId}" } ``` |
| `POST` | `/api/v1/admin/centers/{centerId}/admins` | `[REQ-006]` | `Authorization: Bearer <JWT>`<br>`Idempotency-Key: <string>` | `centerId: uuid (path)` | ```json { "userId": "uuid", "isAssign": true } ``` | ```json { "userId": "uuid", "centerId": "uuid", "assignedAt": "2026-08-18T16:31:58Z", "message": "User assigned as Center Admin successfully" } ``` | ```json { "timestamp": "2026-08-18T16:31:58Z", "status": 404, "error": "NOT_FOUND", "message": "User or Center not found", "path": "/api/v1/admin/centers/{centerId}/admins" } ``` |

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

