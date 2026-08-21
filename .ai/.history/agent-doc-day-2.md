# Day 2: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/docs/integrations/ai-chatbot.md
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DOCUMENT MATRIX INJECTION
*   Target Project Identity Safe Name: 
*   Enforced Java Package Prefix Base: org.nlh4j.saas.
*   Target Documentation Destination Path: `./sources/docs/integrations/ai-chatbot.md`


*   Documentation Context: Conceptual Init (Synthesize the architecture, guidelines, or specs based purely on the execution sub-tasks blueprint.)


### 📋 EXECUTION SUB-TASKS & DOCUMENT CONTENT TO WRITE
['Biên soạn tài liệu tích hợp chatbot AI theo chuẩn kỹ thuật doanh nghiệp. Tài liệu phải bao gồm: (1) Kiến trúc tổng quan hệ thống chatbot, sơ đồ luồng dữ liệu từ người dùng đến mô hình AI và quay lại; (2) Cấu hình mô hình AI: tên endpoint, khóa xác thực, tham số nhiệt độ, max_tokens, ngôn ngữ hỗ trợ; (3) Cơ chế chuyển tiếp hỗ trợ con người: điều kiện kích hoạt (confidence < 0.7), luồng tạo ticket, thông báo cho hỗ trợ viên; (4) Xử lý ngoại lệ: lỗi kết nối API AI, lỗi xác thực, timeout, giới hạn tần suất; (5) Hướng dẫn tích hợp frontend: cấu trúc payload request/response, xử lý trạng thái loading và lỗi, hiển thị tin nhắn đang soạn thảo. Tài liệu phải được lưu trữ tại `./sources/docs/integrations/ai-chatbot.md` với định dạng Markdown chuẩn.']

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

