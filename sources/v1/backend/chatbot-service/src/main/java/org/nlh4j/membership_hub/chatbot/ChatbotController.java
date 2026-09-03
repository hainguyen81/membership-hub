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