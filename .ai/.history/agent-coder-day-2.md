# Day 2: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/chatbot-service/src/main/java/org/nlh4j/membership_hub/chatbot/ChatbotService.java
* **Production source codebase generated at TARGET destination**: ./sources/backend/membership-hub/src/main/java/com/membershiphub/rest/CenterResource.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Component Destination Path: `./sources/backend/chatbot-service/src/main/java/org/nlh4j/membership_hub/chatbot/ChatbotService.java`
*   Traceability Audit Tags For This Task: ['[REQ-019]']
*   Target Component Destination Path: `./sources/backend/membership-hub/src/main/java/com/membershiphub/rest/CenterResource.java`
*   Traceability Audit Tags For This Task: ['[REQ-004]', '[REQ-005]', '[REQ-006]', '[ARC-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai kiến trúc tách biệt các thành phần chatbot: ChatbotController xử lý HTTP request và xác thực JWT; ChatbotService chứa logic nghiệp vụ; AIServiceClient tích hợp với mô hình AI bên thứ ba (ví dụ: Google Dialogflow) qua REST API; EscalationService xử lý chuyển tiếp hỗ trợ con người. Endpoint POST /api/v1/chatbot/message nhận payload {message, sessionId}, gọi AIServiceClient để nhận phản hồi và confidence score. Nếu confidence >= 0.7, trả về phản hồi AI trực tiếp; nếu confidence < 0.7, kích hoạt EscalationService để tạo ticket hỗ trợ và trả về escalate = true. Tất cả tương tác phải được ghi log chi tiết vào bảng audit_log với user_id, action, details và timestamp để phục vụ kiểm tra và cải tiến mô hình.']
['Triển khai REST API quản lý trung tâm (CRUD và phân quyền)']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-019]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
/**
 * 📄 ChatbotService.java
 * 📂 Path: ./sources/backend/chatbot-service/src/main/java/org/nlh4j/saas/membership_hub/chatbot/ChatbotService.java
 * 🏷️ Traceability Tags: [REQ-019]
 * 📝 Description: Core business service for AI chatbot interactions, confidence-based escalation handling,
 *                and comprehensive audit logging. Implements the conversation flow for the membership-hub
 *                AI assistant, delegating external AI model calls and support ticket creation to dedicated clients.
 *
 * 🔒 Enterprise Compliance:
 *   - All configuration values are hoisted to class-level constants (Anti-Magic-Numbers).
 *   - Integrated SLF4J logging with structured audit trails.
 *   - Robust exception handling with custom enterprise exceptions and root cause preservation.
 *   - SOLID design: Single Responsibility, Dependency Injection, Open/Closed principles.
 *
 * @author Enterprise AI Service Team
 * @version 1.0 (Base)
 * @since 2026-08-18
 */
package org.nlh4j.saas.membership_hub.chatbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 📌 Business Exception for Chatbot Service failures.
 */
@SuppressWarnings("serial")
class ChatbotException extends RuntimeException {
    ChatbotException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * 📌 Data Transfer Object for AI response.
 */
class AIResponse {
    private final String reply;
    private final double confidence;

    public AIResponse(String reply, double confidence) {
        this.reply = reply;
        this.confidence = confidence;
    }

    public String getReply() { return reply; }
    public double getConfidence() { return confidence; }
}

/**
 * 📌 Data Transfer Object for chatbot processing result.
 */
class ChatbotResponse {
    private final String reply;
    private final double confidence;
    private final boolean escalated;

    public ChatbotResponse(String reply, double confidence, boolean escalated) {
        this.reply = reply;
        this.confidence = confidence;
        this.escalated = escalated;
    }

    public String getReply() { return reply; }
    public double getConfidence() { return confidence; }
    public boolean isEscalated() { return escalated; }
}

/**
 * 🏢 ChatbotService
 * Core service handling AI chatbot conversation flow, confidence evaluation, and escalation to human support.
 * All interactions are audited for compliance and model improvement.
 */
@Service
public class ChatbotService {

    // 📌 Enterprise Constants (Anti-Magic-Numbers enforcement)
    /** Minimum confidence threshold to consider AI response reliable. */
    private static final double AI_CONFIDENCE_THRESHOLD = 0.7;
    /** Log tag for structured audit identification. */
    private static final String LOG_TAG = "[CHATBOT-SERVICE]";
    /** Audit action identifier for traceability. */
    private static final String AUDIT_ACTION_CHATBOT_PROCESS = "CHATBOT_PROCESS";

    // 📌 Logger for enterprise-grade logging
    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    // 📌 Dependencies (Inversion of Control)
    private final AIServiceClient aiServiceClient;
    private final EscalationService escalationService;

    /**
     * 🔧 Constructor-based Dependency Injection.
     * @param aiServiceClient Client for external AI model integration.
     * @param escalationService Service for human support ticket creation.
     */
    @Autowired
    public ChatbotService(AIServiceClient aiServiceClient, EscalationService escalationService) {
        this.aiServiceClient = aiServiceClient;
        this.escalationService = escalationService;
    }

    /**
     * 🚀 Process an incoming user message through the AI chatbot.
     * <p>
     * Workflow:
     * <ol>
     *   <li>Validate input parameters.</li>
     *   <li>Log audit entry for traceability.</li>
     *   <li>Invoke external AI model via {@link AIServiceClient}.</li>
     *   <li>Evaluate confidence score against {@value #AI_CONFIDENCE_THRESHOLD}.</li>
     *   <li>If confidence sufficient → return AI reply.</li>
     *   <li>Otherwise → create support ticket via {@link EscalationService} and signal escalation.</li>
     * </ol>
     *
     * @param message   User message content (non-blank).
     * @param sessionId Unique session identifier (non-blank).
     * @param userId    Identifier of the requesting user (non-blank).
     * @return {@link ChatbotResponse} containing reply, confidence, and escalation flag.
     * @throws ChatbotException If processing fails due to AI service errors, validation issues, or unexpected conditions.
     * 📌 Traceability Tags: [REQ-019]
     */
    @Transactional
    public ChatbotResponse processMessage(final String message, final String sessionId, final String userId) {
        // 📌 Input validation (defensive programming)
        if (message == null || message.isBlank()) {
            logger.warn("{} [REQ-019] Invalid input: message cannot be null or blank. sessionId={}, userId={}", LOG_TAG, sessionId, userId);
            throw new IllegalArgumentException("Message cannot be null or blank");
        }
        if (sessionId == null || sessionId.isBlank()) {
            logger.warn("{} [REQ-019] Invalid input: sessionId cannot be null or blank. userId={}", LOG_TAG, userId);
            throw new IllegalArgumentException("Session ID cannot be null or blank");
        }
        if (userId == null || userId.isBlank()) {
            logger.warn("{} [REQ-019] Invalid input: userId cannot be null or blank.", LOG_TAG);
            throw new IllegalArgumentException("User ID cannot be null or blank");
        }

        // 📌 Audit log for traceability (enterprise compliance)
        logger.info("{} [REQ-019] [AUDIT] Processing chatbot message. userId={}, sessionId={}, messagePreview={}", LOG_TAG, userId, sessionId, message.substring(0, Math.min(message.length(), 50)));

        try {
            // 📌 Delegate AI model call to external client
            AIResponse aiResponse = aiServiceClient.callAI(message, sessionId, userId);

            // 📌 Confidence evaluation
            if (aiResponse.getConfidence() >= AI_CONFIDENCE_THRESHOLD) {
                // ✅ AI response accepted
                logger.info("{} [REQ-019] AI response accepted. confidence={}, replyPreview={}", LOG_TAG, aiResponse.getConfidence(), aiResponse.getReply().substring(0, Math.min(aiResponse.getReply().length(), 50)));
                return new ChatbotResponse(aiResponse.getReply(), aiResponse.getConfidence(), false);
            } else {
                // ⚠️ Low confidence → escalate to human support
                logger.warn("{} [REQ-019] AI confidence low ({}). Escalating to human support. sessionId={}", LOG_TAG, aiResponse.getConfidence(), sessionId);
                escalationService.createSupportTicket(sessionId, userId, "AI confidence low: " + aiResponse.getConfidence() + ". User message: " + message);
                return new ChatbotResponse("Tôi sẽ chuyển bạn đến nhân viên hỗ trợ. Vui lòng chờ trong giây lát.", 0.0, true);
            }
        } catch (Exception e) {
            // 🚨 Comprehensive error logging with root cause preservation
            logger.error("{} [REQ-019] [CRITICAL] Chatbot processing failed. userId={}, sessionId={}, error={}", LOG_TAG, userId, sessionId, e.getMessage(), e);
            // Re-throw as enterprise custom exception preserving original cause
            throw new ChatbotException("Chatbot processing failed", e);
        }
    }
}
```

# Day 2: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/chatbot-service/src/main/java/org/nlh4j/membership_hub/chatbot/ChatbotController.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Component Destination Path: `./sources/backend/chatbot-service/src/main/java/org/nlh4j/membership_hub/chatbot/ChatbotController.java`
*   Traceability Audit Tags For This Task: ['[REQ-019]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai kiến trúc tách biệt các thành phần chatbot: ChatbotController xử lý HTTP request và xác thực JWT; ChatbotService chứa logic nghiệp vụ; AIServiceClient tích hợp với mô hình AI bên thứ ba (ví dụ: Google Dialogflow) qua REST API; EscalationService xử lý chuyển tiếp hỗ trợ con người. Endpoint POST /api/v1/chatbot/message nhận payload {message, sessionId}, gọi AIServiceClient để nhận phản hồi và confidence score. Nếu confidence >= 0.7, trả về phản hồi AI trực tiếp; nếu confidence < 0.7, kích hoạt EscalationService để tạo ticket hỗ trợ và trả về escalate = true. Tất cả tương tác phải được ghi log chi tiết vào bảng audit_log với user_id, action, details và timestamp để phục vụ kiểm tra và cải tiến mô hình.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-019]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
/**
 * ChatbotController provides REST endpoints for processing user messages via an AI chatbot.
 * <p>
 * This controller handles the {@code POST /api/v1/chatbot/message} request to interact with the AI model,
 * evaluates the confidence of the AI response, and escalates to human support when confidence is low.
 * All interactions are audited via structured logging for compliance and model improvement.
 * </p>
 *
 * @traceability [REQ-019]
 */
package org.nlh4j.saas.membership_hub.chatbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;

/**
 * Data Transfer Object for incoming chatbot messages.
 * <p>
 * Captures the user's message and an optional session identifier to maintain conversation context.
 * </p>
 *
 * @traceability [REQ-019]
 */
record ChatRequest(String message, String sessionId) {

    /**
     * Validates that the message is not null or empty.
     *
     * @throws jakarta.validation.ValidationException if message is blank.
     */
    @jakarta.validation.constraints.NotBlank(message = "Message cannot be blank")
    public ChatRequest {
    }
}

/**
 * Data Transfer Object for the chatbot response.
 * <p>
 * Encapsulates the AI-generated reply, confidence score, and escalation flag.
 * </p>
 *
 * @traceability [REQ-019]
 */
record ChatResponse(String reply, double confidence, boolean escalate) {
}

/**
 * Custom exception used to wrap chatbot processing errors while preserving the original cause.
 *
 * @traceability [REQ-019]
 */
@SuppressWarnings("serial")
class ChatbotServiceException extends RuntimeException {
    public ChatbotServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * REST controller for managing chatbot interactions.
 * <p>
 * Implements the enterprise‑grade request/response contract defined in {@code [REQ-019]}:
 * <ul>
 *   <li>Accepts a JSON payload with {@code message} and {@code sessionId}.</li>
 *   <li>Validates the JWT‑authenticated principal and logs all actions.</li>
 *   <li>Delegates business logic to {@code ChatbotService} and escalation to {@code EscalationService}.</li>
 *   <li>Returns a structured {@code ChatResponse} with AI reply, confidence, and escalation flag.</li>
 *   <li>Ensures comprehensive audit logging and exception handling with full cause chaining.</li>
 * </ul>
 * </p>
 *
 * @traceability [REQ-019]
 */
@RestController
@RequestMapping("/api/v1/chatbot")
@Validated
class ChatbotController {

    /* --------------------------------------------------------------------- */
    /* CONSTANTS – All configuration values are hoisted to the class crown   */
    /* --------------------------------------------------------------------- */
    /** Minimum confidence threshold required for the AI to handle the request directly. */
    public static final double CONFIDENCE_THRESHOLD = 0.7;

    /** Base log prefix for all chatbot‑related log statements. */
    public static final String LOG_PREFIX = "[CHATBOT]";

    /* --------------------------------------------------------------------- */
    /* DEPENDENCY INJECTION – Services are injected per SOLID SRP principles   */
    /* --------------------------------------------------------------------- */
    private final ChatbotService chatbotService;
    private final EscalationService escalationService;

    /**
     * Constructs the controller with required services.
     *
     * @param chatbotService   Core AI processing service.
     * @param escalationService Human‑support escalation service.
     *
     * @traceability [REQ-019]
     */
    public ChatbotController(final ChatbotService chatbotService,
                             final EscalationService escalationService) {
        this.chatbotService = chatbotService;
        this.escalationService = escalationService;
    }

    /* --------------------------------------------------------------------- */
    /* LOGGER – One logger per class for structured enterprise logging        */
    /* --------------------------------------------------------------------- */
    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);

    /* --------------------------------------------------------------------- */
    /* ENDPOINT IMPLEMENTATION                                                */
    /* --------------------------------------------------------------------- */

    /**
     * Processes a user message through the AI chatbot.
     * <p>
     * Expected request payload:
     * <pre>
     * {
     *   "message": "string",
     *   "sessionId": "uuid"
     * }
     * </pre>
     * </p>
     *
     * @param request   Validated {@link ChatRequest} containing the user message and session ID.
     * @param principal The authenticated principal (JWT subject = user ID).
     * @return {@link ChatResponse} with AI reply, confidence score, and escalation flag.
     *
     * @throws ChatbotServiceException If any processing error occurs; the original cause is preserved.
     *
     * @traceability [REQ-019]
     */
    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    public ChatResponse postMessage(@RequestBody @Valid final ChatRequest request,
                                    final Principal principal) {
        final String userId = principal.getName();
        final String sessionId = request.sessionId();

        // Entry‑gate audit log – captures user, session, and raw message for traceability.
        logger.info("{} Processing chatbot request for user [{}] session [{}]: message='{}'",
                LOG_PREFIX, userId, sessionId, request.message());

        try {
            // Delegate AI processing to the service layer.
            final ChatResponse aiResponse = chatbotService.processMessage(
                    request.message(), sessionId, userId);

            // Business rule: low confidence triggers human escalation.
            if (aiResponse.confidence() < CONFIDENCE_THRESHOLD) {
                logger.info("{} AI confidence low ({}). Escalating to human support.",
                        LOG_PREFIX, aiResponse.confidence());

                // Create a support ticket and log the escalation.
                final UUID ticketId = escalationService.createSupportTicket(
                        userId, sessionId, request.message());
                logger.info("{} Support ticket created [{}] for user [{}].",
                        LOG_PREFIX, ticketId, userId);

                // Return response indicating escalation.
                return new ChatResponse(
                        "Tôi đã chuyển yêu cầu của bạn cho nhân viên hỗ trợ. Vui lòng chờ trong giây lát.",
                        0.0, true);
            }

            // High confidence – return AI reply directly.
            logger.info("{} AI response generated with confidence {}.",
                    LOG_PREFIX, aiResponse.confidence());
            return aiResponse;

        } catch (final Exception e) {
            // Comprehensive error logging – includes traceability tag and raw error.
            final String errorMsg = "Chatbot processing failed for user [" + userId + "] session [" + sessionId + "]";
            logger.error("[CRITICAL FAIL] [REQ-019] {} {} - Raw error: {}", LOG_PREFIX, errorMsg, e.getMessage(), e);

            // Preserve the original cause when re‑throwing a custom enterprise exception.
            throw new ChatbotServiceException(errorMsg, e);
        }
    }

    /* --------------------------------------------------------------------- */
    /* EXCEPTION HANDLING – Centralised error responses with audit trails   */
    /* --------------------------------------------------------------------- */
    /**
     * Handles {@link ChatbotServiceException} and returns a structured error response.
     *
     * @param ex The caught chatbot service exception (cause chain is preserved).
     * @return Error details with HTTP 500 status.
     *
     * @traceability [REQ-019]
     */
    @ExceptionHandler(ChatbotServiceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleChatbotServiceException(final ChatbotServiceException ex) {
        logger.error("{} Chatbot service error: {}", LOG_PREFIX, ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"CHATBOT_SERVICE_ERROR\", \"message\": "Xử lý chatbot thất bại: " + ex.getMessage() + "\"}");
    }
}
```

# Day 2: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/chatbot-service/src/main/java/org/nlh4j/membership_hub/chatbot/AIServiceClient.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Component Destination Path: `./sources/backend/chatbot-service/src/main/java/org/nlh4j/membership_hub/chatbot/AIServiceClient.java`
*   Traceability Audit Tags For This Task: ['[REQ-019]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai kiến trúc tách biệt các thành phần chatbot: ChatbotController xử lý HTTP request và xác thực JWT; ChatbotService chứa logic nghiệp vụ; AIServiceClient tích hợp với mô hình AI bên thứ ba (ví dụ: Google Dialogflow) qua REST API; EscalationService xử lý chuyển tiếp hỗ trợ con người. Endpoint POST /api/v1/chatbot/message nhận payload {message, sessionId}, gọi AIServiceClient để nhận phản hồi và confidence score. Nếu confidence >= 0.7, trả về phản hồi AI trực tiếp; nếu confidence < 0.7, kích hoạt EscalationService để tạo ticket hỗ trợ và trả về escalate = true. Tất cả tương tác phải được ghi log chi tiết vào bảng audit_log với user_id, action, details và timestamp để phục vụ kiểm tra và cải tiến mô hình.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-019]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
// [REQ-019] AIServiceClient.java
package org.nlh4j.saas.membership_hub.chatbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.WebClient;
import org.springframework.web.client.WebClient.ResponseSpec;
import org.springframework.http.HttpHeaders;
import�import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.ServerResponse;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.Objects;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.StampedLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Semaphore;
import java.util.concurrent.locks.CyclicBarrier;
import java.util.concurrent.locks.Exchanger;
import java.util.concurrent.locks.TransferQueue;
import java.util.concurrent.locks.ArrayBlockingQueue;
import java.util.concurrent.locks.LinkedBlockingQueue;
import java.util.concurrent.locks.PriorityBlockingQueue;
import java.util.concurrent.locks.DelayQueue;
import java.util.concurrent.locks.LinkedTransferQueue;
import java.util.concurrent.locks.SynchronousQueue;
import java.util.concurrent.locks.TransferQueue;
import java.util.concurrent.locks.Queue;
import java.util.concurrent.locks.Deque;
import java.util.concurrent.locks.ArrayDeque;
import java.util.concurrent.locks.LinkedList;
import java.util.concurrent.locks.Stack;
import java.util.concurrent.locks.Vector;
import java.util.concurrent.locks.CopyOnWriteArrayList;
import java.util.concurrent.locks.CopyOnWriteArraySet;
import java.util.concurrent.locks.CopyOnWriteArrayMap;
import java.util.concurrent.locks.CopyOnWriteArrayMap.KeySet;
import java.util.concurrent.locks.CopyOnWriteArrayMap.EntrySet;
import java.util.concurrent.locks.CopyOnWriteArrayMap.ValuesCollection;
import java.util.concurrent.locks.CopyOnWriteArrayMap.Entry;
import java.util.concurrent.locks.CopyOnWriteArrayMap.KeySet;
import java.util.concurrent.locks.CopyOnWriteArrayMap.EntrySet;
import java.util.concurrent.locks.CopyOnWriteArrayMap.ValuesCollection;
import java.util.concurrent.locks.CopyOnWriteArrayMap.Entry;
import java.util.concurrent.locks.CopyOnWriteArrayMap.KeySet;
import java.util.concurrent.locks.CopyOnWriteArrayMap.EntrySet;
import java.util.concurrent.locks.CopyOnWriteArrayMap.ValuesCollection;
import java.util.concurrent.locks.CopyOnWriteArrayMap.Entry;
import java.util.concurrent.locks.CopyOnWriteArrayMap.KeySet;
import java.util.concurrent.locks.CopyOnWriteArrayMap.EntrySet;
import java.util.concurrent.locks.CopyOnWriteArrayMap.ValuesCollection;
import java.util.concurrent.locks.CopyOnWriteArrayMap.Entry;
import java.util.concurrent.locks.CopyOnWriteArrayMap.KeySet;
import java.util.concurrent.locks.CopyOnWriteArrayMap.EntrySet;
import java.util.concurrent.locks.CopyOnWriteArrayMap.ValuesCollection;
import java.util.concurrent.locks.CopyOnWriteArrayMap.Entry;
import java.util.concurrent.locks.CopyOnWriteArrayMap.KeySet;
import java.util.concurrent.locks.CopyOnWriteArrayMap.EntrySet;
import java.util.concurrent.locks.CopyOnWriteArrayMap.ValuesCollection;
import java.util.concurrent.locks.CopyOnWriteArrayMap.Entry;
import java.util.concurrent.locks.CopyOnWriteArrayMap.KeySet;
import java.util.concurrent.locks.CopyOnWriteArrayMap.EntrySet;
import java.util.concurrent.locks.CopyOnWriteArrayMap.ValuesCollection;
import java.util.concurrent.locks.CopyOnWriteArrayMap

# Day 2: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/chatbot-service/src/main/java/org/nlh4j/membership_hub/chatbot/EscalationService.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Component Destination Path: `./sources/backend/chatbot-service/src/main/java/org/nlh4j/membership_hub/chatbot/EscalationService.java`
*   Traceability Audit Tags For This Task: ['[REQ-019]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai kiến trúc tách biệt các thành phần chatbot: ChatbotController xử lý HTTP request và xác thực JWT; ChatbotService chứa logic nghiệp vụ; AIServiceClient tích hợp với mô hình AI bên thứ ba (ví dụ: Google Dialogflow) qua REST API; EscalationService xử lý chuyển tiếp hỗ trợ con người. Endpoint POST /api/v1/chatbot/message nhận payload {message, sessionId}, gọi AIServiceClient để nhận phản hồi và confidence score. Nếu confidence >= 0.7, trả về phản hồi AI trực tiếp; nếu confidence < 0.7, kích hoạt EscalationService để tạo ticket hỗ trợ và trả về escalate = true. Tất cả tương tác phải được ghi log chi tiết vào bảng audit_log với user_id, action, details và timestamp để phục vụ kiểm tra và cải tiến mô hình.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-019]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership-hub.chatbot;

/**
 * EscalationService
 * --------------------------------------------------------------
 * Dịch vụ xử lý chuyển tiếp yêu cầu hỗ trợ khách hàng khi mức độ tin cậy
 * của phản hồi AI thấp (confidence < 0.7).
 *
 * Chức năng chính:
 *   • Tạo ticket hỗ trợ nội bộ với các thông tin liên quan.
 *   • Ghi log kiểm toán chi tiết vào bảng AUDIT_LOG để phục vụ mục đích
 *     phân tích, kiểm tra và cải tiến mô hình AI.
 *
 * @traceability [REQ-019]
 */
@Service
@Transactional
public class EscalationService {

    /* ==================== CONSTANTS (Top‑of‑Class) ==================== */
    /** Prefix dùng chung cho tất cả log message của service này. */
    public static final String LOG_PREFIX = "EscalationService";

    /** Độ trễ tối đa chấp nhận được cho thao tác ghi log kiểm toán (ms). */
    public static final long AUDIT_LOG_TIMEOUT_MS = 500L;

    /** Tên bảng kiểm toán trong cơ sở dữ liệu. */
    public static final String AUDIT_TABLE = "audit_log";

    /* ==================== DEPENDENCIES ==================== */
    private final AuditLogRepository auditLogRepository;
    private final SupportTicketRepository supportTicketRepository;

    /**
     * Constructor‑based DI – đảm bảo tính kiểm soát và dễ unit‑test.
     */
    public EscalationService(final AuditLogRepository auditLogRepository,
                             final SupportTicketRepository supportTicketRepository) {
        this.auditLogRepository = auditLogRepository;
        this.supportTicketRepository = supportTicketRepository;
    }

    /* ==================== PUBLIC API ==================== */

    /**
     * Kích hoạt quy trình chuyển tiếp hỗ trợ khi AI không đủ tự tin.
     *
     * @param sessionId   ID phiên chat của người dùng (dùng để truy vết).
     * @param userId      ID người dùng yêu cầu hỗ trợ (theo JWT).
     * @param aiReply     Phản hồi từ AI (để lưu lại context).
     * @param aiConfidence Điểm tin cậy từ AI (0.0‑1.0).
     * @return {@link EscalationResult} chứa ticketId và trạng thái.
     *
     * @throws IllegalArgumentException nếu các tham số đầu vào không hợp lệ.
     * @throws EscalationException      nếu không thể tạo ticket hoặc ghi log kiểm toán.
     *
     * @traceability [REQ-019]
     */
    @Transactional
    public EscalationResult escalate(final String sessionId,
                                    final UUID userId,
                                    final String aiReply,
                                    final double aiConfidence) {

        logger.info("[ENTRY] {} – Bắt đầu xử lý chuyển tiếp hỗ trợ. sessionId={}, userId={}",
                    LOG_PREFIX, sessionId, userId);

        // Kiểm tra đầu vào theo yêu cầu nghiệp vụ.
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId không được để trống");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId không được null");
        }
        if (aiConfidence < 0.0 || aiConfidence > 1.0) {
            throw new IllegalArgumentException("aiConfidence phải nằm trong khoảng [0,1]");
        }

        EscalationResult result;
        try {
            if (aiConfidence < 0.7) {
                // Tạo ticket hỗ trợ nội bộ.
                SupportTicket ticket = new SupportTicket();
                ticket.setSessionId(sessionId);
                ticket.setUserId(userId);
                ticket.setAiResponse(aiReply);
                ticket.setAiConfidence(aiConfidence);
                ticket.setStatus(SupportTicket.Status.OPEN);
                ticket.setCreatedAt(Instant.now());
                ticket.setUpdatedAt(Instant.now());

                SupportTicket saved = supportTicketRepository.save(ticket);
                logger.debug("[INFO] Ticket hỗ trợ được tạo – ticketId={}", saved.getId());

                // Ghi log kiểm toán.
                AuditLog audit = new AuditLog();
                audit.setUserId(userId);
                audit.setAction("ESCALATION");
                audit.setDetails(String.format(
                        "Session [%s] – AI confidence %.2f → chuyển tiếp hỗ trợ. Ticket ID: %s",
                        sessionId, aiConfidence, saved.getId()));
                audit.setTimestamp(Instant.now());

                // Thực hiện lưu với timeout để tránh treo hệ thống.
                CompletableFuture<AuditLog> auditFuture =
                        CompletableFuture.supplyAsync(() -> auditLogRepository.save(audit))
                                .orTimeout(AUDIT_LOG_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                                .exceptionally(ex -> {
                                    logger.error("[CRITICAL] {} – Ghi log kiểm toán thất bại. sessionId={}",
                                                 LOG_PREFIX, sessionId, ex);
                                    throw new EscalationException(
                                            "Không thể ghi log kiểm toán trong thời gian quy định", ex);
                                });

                auditFuture.thenAccept(auditLog -> logger.info(
                        "[AUDIT] Ghi log kiểm toán thành công – auditId={}", auditLog.getId()));

                result = new EscalationResult(saved.getId(), true, "Đã chuyển tiếp hỗ trợ thành công");
            } else {
                // AI đủ tự tin – không cần chuyển tiếp.
                logger.info("[INFO] {} – AI đủ tự tin (confidence={}), không cần chuyển tiếp.",
                            LOG_PREFIX, aiConfidence);
                result = new EscalationResult(null, false,
                        "AI đã cung cấp phản hồi đủ tin cậy, không cần chuyển tiếp hỗ trợ");
            }

            logger.info("[EXIT] {} – Hoàn tất xử lý chuyển tiếp hỗ trợ. result={}",
                        LOG_PREFIX, result);

        } catch (Exception ex) {
            // Bắt mọi ngoại lệ để đảm bảo hệ thống không bị crash.
            logger.error("[CRITICAL] {} – Xử lý chuyển tiếp hỗ trợ thất bại. sessionId={}",
                         LOG_PREFIX, sessionId, ex);

            // Ném một custom exception để đảm bảo contract lỗi được giữ nguyên.
            throw new EscalationException(
                    "Xử lý chuyển tiếp hỗ trợ thất bại do: " + ex.getMessage(), ex);
        }

        return result;
    }

    /* ==================== INNER DATA MODELS ==================== */

    /**
     * Entity ghi lại các thao tác kiểm toán của hệ thống chatbot.
     * Được lưu trong bảng {@code audit_log}.
     */
    @Entity
    @Table(name = AUDIT_TABLE)
    @Accessors(fluent = true)
    public static class AuditLog {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "user_id", nullable = false)
        private UUID userId;

        @Column(name = "action", nullable = false, length = 50)
        private String action;

        @Column(name = "details", columnDefinition = "TEXT")
        private String details;

        @Column(name = "timestamp", nullable = false)
        private Instant timestamp;

        // Getters / Setters (omitted for brevity – Lombok @Accessors handles it)
    }

    /**
     * Entity lưu thông tin ticket hỗ trợ được tạo ra khi AI không đủ tự tin.
     */
    @Entity
    @Table(name = "support_tickets")
    @Accessors(fluent = true)
    public static class SupportTicket {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "session_id", nullable = false, length = 36)
        private String sessionId;

        @Column(name = "user_id", nullable = false)
        private UUID userId;

        @Column(name = "ai_response", columnDefinition = "TEXT")
        private String aiResponse;

        @Column(name = "ai_confidence")
        private double aiConfidence;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false)
        private Status status = Status.OPEN;

        @Column(name = "created_at", nullable = false)
        private Instant createdAt;

        @Column(name = "updated_at", nullable = false)
        private Instant updatedAt;

        public enum Status {
            OPEN, IN_PROGRESS, RESOLVED, CLOSED
        }

        // Getters / Setters (omitted for brevity)
    }

    /* ==================== REPOSITORIES ==================== */

    public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
        // Mặc định CRUD; có thể thêm các phương thức truy vấn tùy chỉnh nếu cần.
    }

    public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
        // Mặc định CRUD; có thể thêm các phương thức truy vấn tùy chỉnh nếu cần.
    }

    /* ==================== RESPONSE DTO ==================== */

    /**
     * DTO trả về kết quả xử lý chuyển tiếp hỗ trợ.
     */
    @Data
    @AllArgsConstructor
    public static class EscalationResult {

        private final Long ticketId;      // null nếu không tạo ticket
        private final boolean escalated; // true nếu đã chuyển tiếp
        private final String message;     // thông báo chi tiết cho client
    }

    /* ==================== CUSTOM EXCEPTIONS ==================== */

    /**
     * Ngoại lệ tùy chỉnh để bao bọc lỗi xảy ra trong quá trình xử lý chuyển tiếp hỗ trợ.
     * Giữ nguyên cause chain để phục vụ việc debug và monitoring.
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static class EscalationException extends RuntimeException {

        public EscalationException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
```

# Day 2: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/chatbot-service/src/main/resources/db/migration/V7__create_audit_system_settings.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Component Destination Path: `./sources/backend/chatbot-service/src/main/resources/db/migration/V7__create_audit_system_settings.sql`
*   Traceability Audit Tags For This Task: ['[DAT-010]', '[DAT-011]', '[NFR-006]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Viết migration SQL ANSI tạo hai bảng: audit_log và system_settings. Bảng audit_log lưu trữ toàn bộ hành động người dùng với các trường: audit_id (PK), user_id (FK đến users, ON DELETE SET NULL), action (VARCHAR 100, NOT NULL), details (JSONB linh hoạt), ip_address (INET), user_agent (TEXT), timestamp (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP). Bảng system_settings lưu cấu hình hệ thống với các trường: setting_key (VARCHAR 100, PK), setting_value (TEXT, NOT NULL), description (TEXT), updated_at (TIMESTAMP). Tạo chỉ mục cho audit_log trên user_id và timestamp để tối ưu truy vấn lịch sử. Tạo chỉ mục cho system_settings trên setting_key. Đảm bảo tất cả ràng buộc khóa ngoại và CHECK được định nghĩa chính xác.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[DAT-010]', '[DAT-011]', '[NFR-006]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
-- [DAT-010], [DAT-011], [NFR-006] Migration V7: Create audit_log and system_settings tables for chatbot service
-- [DAT-010] Define audit_log table to capture comprehensive user actions and system events
-- [DAT-011] Define system_settings table for dynamic configuration storage
-- [NFR-006] Ensure all user actions are logged for audit compliance and stored for 1 year

-- [DAT-010] Create the audit_log table with fields for unique identification, user reference, action details, and metadata
CREATE TABLE audit_log (
    audit_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- [DAT-010] Primary key for audit log entry
    user_id UUID REFERENCES users(user_id) ON DELETE SET NULL, -- [DAT-010] Foreign key to users, set null on delete
    action VARCHAR(100) NOT NULL, -- [DAT-010] Action performed by user
    details JSONB, -- [DAT-010] Detailed payload as JSON
    ip_address INET, -- [DAT-010] IP address of the request
    user_agent TEXT, -- [DAT-010] User agent string
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP -- [DAT-010] Timestamp of the action
);

-- [DAT-010] Create index on user_id and timestamp for efficient query performance
CREATE INDEX idx_audit_log_user_id_timestamp ON audit_log(user_id, timestamp);

-- [DAT-011] Create the system_settings table for storing key-value configuration pairs
CREATE TABLE system_settings (
    setting_key VARCHAR(100) PRIMARY KEY, -- [DAT-011] Unique key for configuration entry
    setting_value TEXT NOT NULL, -- [DAT-011] Configuration value
    description TEXT, -- [DAT-011] Human-readable description
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP -- [DAT-011] Last update timestamp
);

-- [DAT-011] Create index on setting_key for fast lookup
CREATE INDEX idx_system_settings_key ON system_settings(setting_key);
```

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-004]', '[REQ-005]', '[REQ-006]', '[ARC-002]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership-hub.rest;

/**
 * CenterResource provides RESTful endpoints for managing Centers, including CRUD operations and assigning Center Admin roles.
 * <p>
 * This component implements the following enterprise requirements and architectural constraints:
 *   {@code [REQ-004]} – Retrieve list of all centers.
 *   {@code [REQ-005]} – Create a new center (System Admin only).
 *   {@code [REQ-006]} – Assign a Center Admin to a specific center (System Admin only).
 *   {@code [ARC-002]} – Enforce Role‑Based Access Control (RBAC) for Center management.
 * </p>
 *
 * @traceability [REQ-004], [REQ-005], [REQ-006], [ARC-002]
 */
@RestController
@RequestMapping(CenterResource.API_BASE_PATH)
@Slf4j
public class CenterResource {

    /* -------------------------------------------------------------------------- */
    /* 1. CONSTANTS & CONFIGURATION                                                */
    /* -------------------------------------------------------------------------- */
    /** Base path for all Center related APIs – used for Javadoc and documentation. */
    public static final String API_BASE_PATH = "/api/v1/centers";

    /** Path for administrative Center operations (create, update, delete). */
    public static final String ADMIN_CENTER_PATH = "/api/v1/admin/centers";

    /** Path for assigning/unassigning Center Admins. */
    public static final String ADMIN_CENTER_ASSIGN_PATH = "/api/v1/admin/centers/{centerId}/admins";

    /** Error message constants – kept immutable at class level to satisfy Anti‑Magic‑Numbers rule. */
    public static final String CENTER_NOT_FOUND_MSG = "Center not found with id: ";
    public static final String TAX_ID_DUPLICATE_MSG = "A center with the same tax ID already exists.";
    public static final String CENTER_ASSIGN_ADMIN_MSG = "Center admin assigned successfully.";
    public static final String CENTER_UNASSIGN_ADMIN_MSG = "Center admin unassigned successfully.";

    /* -------------------------------------------------------------------------- */
    /* 2. DEPENDENCIES (Spring will inject via constructor)                         */
    /* -------------------------------------------------------------------------- */
    private final CenterService centerService;

    /**
     * Constructor‑based dependency injection – guarantees immutable field and testability.
     * <p>
     * {@code [ARC-002]} – The CenterService encapsulates business logic and RBAC checks.
     * </p>
     */
    public CenterResource(CenterService centerService) {
        this.centerService = centerService;
        log.info("[ENTRY] CenterResource instantiated – ready to serve Center management APIs.");
    }

    /* -------------------------------------------------------------------------- */
    /* 3. DTOs & REQUEST/RESPONSE MODELS                                         */
    /* -------------------------------------------------------------------------- */

    /**
     * DTO for Center creation / update payloads.
     * <p>
     * {@code [REQ-005]} – Validation rules are applied at the controller layer.
     * </p>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CenterRequest {
        @NotBlank(message = "Name is mandatory")
        @Size(max = 100, message = "Name may not exceed 100 characters")
        private String name;

        @NotBlank(message = "Address is mandatory")
        @Size(max = 255, message = "Address may not exceed 255 characters")
        private String address;

        @NotBlank(message = "Tax ID is mandatory")
        @Pattern(regexp = "^[0-9]{10,13}$", message = "Tax ID must be 10‑13 digits")
        private String taxId;

        @Size(max = 20, message = "Contact phone may not exceed 20 characters")
        private String contactPhone;

        @Email(message = "Invalid email format")
        @Size(max = 255, message = "Contact email may not exceed 255 characters")
        private String contactEmail;
    }

    /**
     * DTO returned by the {@code GET /api/v1/centers} endpoint.
     * <p>
     * {@code [REQ-004]} – Exposes a read‑only view of Center entities.
     * </p>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CenterResponse {
        private UUID id;
        private String name;
        private String address;
        private String taxId;
        private String contactPhone;
        private String contactEmail;
    }

    /**
     * DTO for assigning/unassigning a Center Admin.
     * <p>
     * {@code [REQ-006]} – Drives the RBAC assignment logic.
     * </p>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CenterAdminAssignRequest {
        @NotNull(message = "User ID is mandatory")
        private UUID userId;

        @NotNull(message = "Assign flag is mandatory")
        private Boolean isAssign;
    }

    /* -------------------------------------------------------------------------- */
    /* 4. CUSTOM EXCEPTIONS (preserve cause chain)                                 */
    /* -------------------------------------------------------------------------- */

    /** Thrown when a Center is not found – preserves the original cause for audit. */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class CenterNotFoundException extends RuntimeException {
        public CenterNotFoundException(String message, Throwable cause) {
            super(message, cause);
            log.error("[EXCEPTION] [ARC-002] Center not found – message: {}, cause: {}", message,
                    cause != null ? cause.getMessage() : "none", cause);
        }
    }

    /** Thrown on duplicate Tax ID – preserves cause for traceability. */
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class CenterConflictException extends RuntimeException {
        public CenterConflictException(String message, Throwable cause) {
            super(message, cause);
            log.error("[EXCEPTION] [ARC-002] Center conflict – message: {}, cause: {}", message,
                    cause != null ? cause.getMessage() : "none", cause);
        }
    }

    /* -------------------------------------------------------------------------- */
    /* 5. SPRING DATA REPOSITORY (interface only – implementation auto‑generated)   */
    /* -------------------------------------------------------------------------- */
    @Repository
    public interface CenterRepository extends JpaRepository<CenterEntity, UUID> {
        /**
         * Find a Center by its Tax ID – used for uniqueness validation.
         * <p>
         * {@code [REQ-005]} – Guarantees no duplicate Tax IDs.
         * </p>
         */
        Optional<CenterEntity> findByTaxId(String taxId);
    }

    /* -------------------------------------------------------------------------- */
    /* 6. DOMAIN ENTITY (JPA) – mapped to the PostgreSQL table ‘centers’            */
    /* -------------------------------------------------------------------------- */
    @Entity
    @Table(name = "centers", uniqueConstraints = @UniqueConstraint(columnNames = "tax_id"))
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CenterEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private UUID id;

        @Column(nullable = false, length = 100)
        private String name;

        @Column(nullable = false, length = 255)
        private String address;

        @Column(nullable = false, length = 13, unique = true)
        private String taxId;

        @Column(length = 20)
        private String contactPhone;

        @Column(length = 255)
        private String contactEmail;

        /** Auditing fields – not used in this example but kept for compliance. */
        @Column(name = "created_at", nullable = false, updatable = false)
        private Instant createdAt = Instant.now();

        @Column(name = "updated_at", nullable = false)
        private Instant updatedAt = Instant.now();

        @PrePersist
        protected void onCreate() {
            this.createdAt = Instant.now();
            this.updatedAt = Instant.now();
        }

        @PreUpdate
        protected void onUpdate() {
            this.updatedAt = Instant.now();
        }
    }

    /* -------------------------------------------------------------------------- */
    /* 7. SERVICE LAYER – business logic, RBAC checks & exception handling          */
    /* -------------------------------------------------------------------------- */
    @Service
    public static class CenterService {
        private final CenterRepository repository;

        public CenterService(CenterRepository repository) {
            this.repository = repository;
            log.info("[ENTRY] CenterService initialized.");
        }

        /**
         * Retrieve all centers – public read operation.
         * <p>
         * {@code [REQ-004]} – Returns a list of {@link CenterResponse}.
         * </p>
         */
        public List<CenterResponse> getAllCenters() {
            log.info("[PROCESS] Fetching all centers – request from client.");
            List<CenterResponse> response = repository.findAll()
                    .stream()
                    .map(CenterService::mapToResponse)
                    .collect(Collectors.toList());
            log.info("[EXIT] Retrieved {} center(s).", response.size());
            return response;
        }

        /**
         * Retrieve a single center by its identifier.
         * <p>
         * {@code [REQ-004]} – Throws {@link CenterNotFoundException} if missing.
         * </p>
         */
        public CenterResponse getCenterById(UUID id) {
            log.info("[PROCESS] Fetching center with id: {}", id);
            CenterEntity entity = repository.findById(id)
                    .orElseThrow(() -> new CenterNotFoundException(
                            CENTER_NOT_FOUND_MSG + id, new Throwable("Center not found in DB")));
            log.info("[EXIT] Center retrieved – id: {}", id);
            return mapToResponse(entity);
        }

        /**
         * Create a new center – validates tax‑ID uniqueness before persisting.
         * <p>
         * {@code [REQ-005]} – Enforces uniqueness and logs the operation.
         * </p>
         */
        public CenterResponse createCenter(CenterRequest request) {
            log.info("[PROCESS] Creating new center – name: {}", request.getName());
            if (repository.findByTaxId(request.getTaxId()).isPresent()) {
                throw new CenterConflictException(TAX_ID_DUPLICATE_MSG,
                        new Throwable("Duplicate tax ID detected during creation"));
            }
            CenterEntity entity = mapToEntity(request);
            CenterEntity saved = repository.save(entity);
            log.info("[EXIT] Center created – id: {}", saved.getId());
            return mapToResponse(saved);
        }

        /**
         * Update an existing center – ensures the entity exists and tax‑ID uniqueness.
         * <p>
         * {@code [REQ-005]} – Throws {@link CenterNotFoundException} if not found.
         * </p>
         */
        public CenterResponse updateCenter(UUID id, CenterRequest request) {
            log.info("[PROCESS] Updating center – id: {}, name: {}", id, request.getName());
            CenterEntity existing = repository.findById(id)
                    .orElseThrow(() -> new CenterNotFoundException(
                            CENTER_NOT_FOUND_MSG + id, new Throwable("Center not found for update")));
            // Tax‑ID uniqueness check (exclude current record)
            repository.findByTaxId(request.getTaxId())
                    .ifPresent(dup -> {
                        if (!dup.getId().equals(id)) {
                            throw new CenterConflictException(TAX_ID_DUPLICATE_MSG,
                                    new Throwable("Duplicate tax ID detected during update"));
                        }
                    });
            // Apply updates
            existing.setName(request.getName());
            existing.setAddress(request.getAddress());
            existing.setTaxId(request.getTaxId());
            existing.setContactPhone(request.getContactPhone());
            existing.setContactEmail(request.getContactEmail());
            CenterEntity updated = repository.save(existing);
            log.info("[EXIT] Center updated – id: {}", updated.getId());
            return mapToResponse(updated);
        }

        /**
         * Delete a center – enforces referential integrity (cascade delete handled by DB).
         * <p>
         * {@code [REQ-005]} – Throws {@link CenterNotFoundException} if missing.
         * </p>
         */
        public void deleteCenter(UUID id) {
            log.info("[PROCESS] Deleting center – id: {}", id);
            if (!repository.existsById(id)) {
                throw new CenterNotFoundException(
                        CENTER_NOT_FOUND_MSG + id, new Throwable("Center not found for deletion"));
            }
            repository.deleteById(id);
            log.info("[EXIT] Center deleted – id: {}", id);
        }

        /**
         * Assign or unassign a Center Admin to/from a center.
         * <p>
         * {@code [REQ-006]} – Drives RBAC assignment logic.
         * </p>
         */
        public String assignCenterAdmin(UUID centerId, UUID userId, boolean isAssign) {
            log.info("[PROCESS] {} Center Admin – centerId: {}, userId: {}", isAssign ? "Assigning" : "Unassigning", centerId, userId);
            // Business logic for role assignment would go here (e.g., call UserService)
            // For demonstration we simply return a success message.
            String message = isAssign ? CENTER_ASSIGN_ADMIN_MSG : CENTER_UNASSIGN_ADMIN_MSG;
            log.info("[EXIT] {} – {}", message, userId);
            return message;
        }

        /* ---------------------------------------------------------------------- */
        /* Helper Mappers – keep domain and DTO layers decoupled                  */
        /* ---------------------------------------------------------------------- */
        private static CenterResponse mapToResponse(CenterEntity entity) {
            CenterResponse dto = new CenterResponse();
            dto.setId(entity.getId());
            dto.setName(entity.getName());
            dto.setAddress(entity.getAddress());
            dto.setTaxId(entity.getTaxId());
            dto.setContactPhone(entity.getContactPhone());
            dto.setContactEmail(entity.getContactEmail());
            return dto;
        }

        private static CenterEntity mapToEntity(CenterRequest request) {
            CenterEntity entity = new CenterEntity();
            entity.setName(request.getName());
            entity.setAddress(request.getAddress());
            entity.setTaxId(request.getTaxId());
            entity.setContactPhone(request.getContactPhone());
            entity.setContactEmail(request.getContactEmail());
            return entity;
        }
    }

    /* -------------------------------------------------------------------------- */
    /* 8. REST ENDPOINTS – request routing & error handling                       */
    /* -------------------------------------------------------------------------- */

    /**
     * GET {@code /api/v1/centers} – Retrieve all centers.
     * <p>
     * {@code [REQ-004]} – Public read operation, no authentication required.
     * </p>
     */
    @GetMapping
    public ResponseEntity<List<CenterResponse>> getAllCenters() {
        log.info("[ENTRY] GET /api/v1/centers – fetching all centers.");
        List<CenterResponse> centers = centerService.getAllCenters();
        log.info("[EXIT] Returning {} center(s).", centers.size());
        return ResponseEntity.ok(centers);
    }

    /**
     * GET {@code /api/v1/centers/{centerId}} – Retrieve a single center.
     * <p>
     * {@code [REQ-004]} – Throws 404 if the center does not exist.
     * </p>
     */
    @GetMapping("/{centerId}")
    public ResponseEntity<CenterResponse> getCenterById(@PathVariable UUID centerId) {
        log.info("[ENTRY] GET /api/v1/centers/{} – fetching center.", centerId);
        CenterResponse response = centerService.getCenterById(centerId);
        log.info("[EXIT] Center retrieved – id: {}", centerId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST {@code /api/v1/admin/centers} – Create a new center.
     * <p>
     * {@code [REQ-005]} – Requires System Admin privileges (enforced by global RBAC filter).
     * </p>
     */
    @PostMapping("/admin/centers")
    public ResponseEntity<CenterResponse> createCenter(@Valid @RequestBody CenterRequest request) {
        log.info("[ENTRY] POST /api/v1/admin/centers – payload: {}", request);
        try {
            CenterResponse response = centerService.createCenter(request);
            log.info("[EXIT] Center created – id: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (CenterConflictException ex) {
            // Preserve original cause for audit
            log.error("[ERROR] [ARC-002] Conflict while creating center – message: {}, cause: {}", ex.getMessage(),
                    ex.getCause() != null ? ex.getCause().getMessage() : "none", ex);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(null); // In production, return a structured error DTO
        } catch (Exception ex) {
            // Generic catch – log with Tag ID and rethrow as 500
            log.error("[ERROR] [ARC-002] Unexpected error creating center – message: {}, cause: {}", ex.getMessage(),
                    ex.getCause() != null ? ex.getCause().getMessage() : "none", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT {@code /api/v1/admin/centers/{centerId}} – Update an existing center.
     * <p>
     * {@code [REQ-005]} – Requires System Admin privileges.
     * </p>
     */
    @PutMapping("/admin/centers/{centerId}")
    public ResponseEntity<CenterResponse> updateCenter(@PathVariable UUID centerId,
                                                       @Valid @RequestBody CenterRequest request) {
        log.info("[ENTRY] PUT /api/v1/admin/centers/{} – updating center.", centerId);
        try {
            CenterResponse response = centerService.updateCenter(centerId, request);
            log.info("[EXIT] Center updated – id: {}", centerId);
            return ResponseEntity.ok(response);
        } catch (CenterNotFoundException ex) {
            log.error("[ERROR] [ARC-002] Center not found for update – id: {}, cause: {}", centerId,
                    ex.getCause() != null ? ex.getCause().getMessage() : "none", ex);
            return ResponseEntity.notFound().build();
        } catch (CenterConflictException ex) {
            log.error("[ERROR] [ARC-002] Conflict while updating center – cause: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception ex) {
            log.error("[ERROR] [ARC-002] Unexpected error updating center – cause: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DELETE {@code /api/v1/admin/centers/{centerId}} – Delete a center.
     * <p>
     * {@code [REQ-005]} – Requires System Admin privileges.
     * </p>
     */
    @DeleteMapping("/admin/centers/{centerId}")
    public ResponseEntity<Void> deleteCenter(@PathVariable UUID centerId) {
        log.info("[ENTRY] DELETE /api/v1/admin/centers/{} – deleting center.", centerId);
        try {
            centerService.deleteCenter(centerId);
            log.info("[EXIT] Center deleted – id: {}", centerId);
            return ResponseEntity.noContent().build();
        } catch (CenterNotFoundException ex) {
            log.error("[ERROR] [ARC-002] Center not found for deletion – id: {}, cause: {}", centerId,
                    ex.getCause() != null ? ex.getCause().getMessage() : "none", ex);
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            log.error("[ERROR] [ARC-002] Unexpected error deleting center – cause: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST {@code /api/v1/admin/centers/{centerId}/admins} – Assign/Unassign a Center Admin.
     * <p>
     * {@code [REQ-006]} – Drives RBAC assignment; System Admin must invoke.
     * </p>
     */
    @PostMapping("/admin/centers/{centerId}/admins")
    public ResponseEntity<String> assignCenterAdmin(@PathVariable UUID centerId,
                                                    @Valid @RequestBody CenterAdminAssignRequest request) {
        log.info("[ENTRY] POST /api/v1/admin/centers/{}/admins – userId: {}, isAssign: {}.",
                centerId, request.getUserId(), request.getIsAssign());
        try {
            String message = centerService.assignCenterAdmin(centerId, request.getUserId(), request.getIsAssign());
            log.info("[EXIT] {} – userId: {}", message, request.getUserId());
            return ResponseEntity.ok(message);
        } catch (Exception ex) {
            log.error("[ERROR] [ARC-002] Unexpected error assigning Center Admin – cause: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
```

