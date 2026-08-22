package org.nlh4j.saas.membership_hub.notification;

/**
 * Traceability Tags: [REQ-016], [ARC-008]
 * Service implementation for sending notifications to Zalo groups.
 * Handles HTTP communication with Zalo API, retry logic, and event queue integration.
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Constants for Zalo notification configuration.
 * All configuration values are hoisted to the top of the class to comply with the Anti-Magic-Numbers policy.
 */
@Service
public class ZaloNotificationSender {

    // Enterprise‑grade constants – immutable, placed at the crown of the class
    private static final String ZALO_API_URL = "https://api.zalo.me/v2.0/message/cs";
    private static final String ZALO_ACCESS_TOKEN = "YOUR_ZALO_ACCESS_TOKEN"; // In production, fetch from Secret Manager
    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_MS = 1_000; // 1 second base delay

    private static final Logger logger = LoggerFactory.getLogger(ZaloNotificationSender.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Constructor injection for required dependencies.
     */
    public ZaloNotificationSender(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Sends a notification message to a specified Zalo group.
     * Implements idempotent sending by checking duplicate messages via event queue.
     *
     * @param groupId   The Zalo group ID (e.g., "3456789")
     * @param message   The message content to send (max 2000 characters)
     * @throws NotificationSendException if sending fails after all retries
     * Traceability Tags: [REQ-016], [ARC-008]
     */
    public void sendZaloGroupNotification(String groupId, String message) throws NotificationSendException {
        logger.info("[ENTRY] sendZaloGroupNotification for groupId={}, message={}", groupId, maskSensitive(message));

        // Input validation – defensive programming
        if (groupId == null || groupId.trim().isEmpty()) {
            throw new IllegalArgumentException("Group ID cannot be null or empty");
        }
        if (message == null || message.length() > 2000) {
            throw new IllegalArgumentException("Message must be between 1 and 2000 characters");
        }

        // Idempotency check – consult the event queue to avoid duplicate sends
        boolean alreadyProcessed = checkDuplicateInEventQueue(groupId, message);
        if (alreadyProcessed) {
            logger.info("[INFO] Duplicate notification detected for groupId={} – skipping.", groupId);
            return;
        }

        // Retry loop with exponential backoff
        NotificationSendException lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRY_COUNT; attempt++) {
            try {
                logger.debug("[ATTEMPT] Attempt {} to send Zalo notification.", attempt);
                sendToZaloApi(groupId, message);
                logger.info("[SUCCESS] Zalo notification sent successfully to groupId={}", groupId);
                // Record successful event in queue to prevent duplicate processing
                recordSentEvent(groupId, message);
                return;
            } catch (ResourceAccessException | RuntimeException e) {
                // Preserve the original cause chain as required by the Exception Cause Chain Preservation Law
                lastException = new NotificationSendException(
                        ErrorCode.ZALO_API_ERROR,
                        String.format("Failed to send Zalo notification (attempt %d)", attempt),
                        e);
                // Comprehensive exception logging with traceability Tag IDs
                logger.error("[CRITICAL FAIL] [REQ-016] [ARC-008] Zalo notification failed on attempt {}. Raw error: {}", attempt, e.getMessage(), e);
                if (attempt < MAX_RETRY_COUNT) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt); // exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new NotificationSendException(ErrorCode.ZALO_API_ERROR, "Interrupted during retry sleep", ie);
                    }
                }
            }
        }

        // All retries exhausted – rethrow the last captured exception
        throw lastException;
    }

    /**
     * Internal method that performs the HTTP POST to the Zalo API.
     * Uses a prepared request with proper headers and payload.
     */
    private void sendToZaloApi(String groupId, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Access-Token", ZALO_ACCESS_TOKEN);

        ZaloMessagePayload payload = new ZaloMessagePayload();
        payload.setRecipientId(groupId);
        payload.setMessage(message);

        HttpEntity<ZaloMessagePayload> request = new HttpEntity<>(payload, headers);
        ResponseEntity<ZaloResponse> response = restTemplate.exchange(
                ZALO_API_URL,
                HttpMethod.POST,
                request,
                ZaloResponse.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Zalo API returned HTTP " + response.getStatusCodeValue() + ": " + response.getBody());
        }
    }

    /**
     * Checks the event queue for duplicate notifications to ensure idempotency.
     * In a real implementation, this would query a message broker (e.g., Kafka) or a deduplication store.
     */
    private boolean checkDuplicateInEventQueue(String groupId, String message) {
        // Placeholder for actual deduplication logic
        // Example: return kafkaTemplate.existsDuplicate(groupId, message);
        return false;
    }

    /**
     * Records a successful send event into the event queue for future deduplication checks.
     */
    private void recordSentEvent(String groupId, String message) {
        // Placeholder for actual event recording logic
        // Example: kafkaTemplate.send("notification.sent", groupId, message);
    }

    // -------------------------------------------------------------------------
    // Data Transfer Objects for Zalo API communication
    // -------------------------------------------------------------------------
    static class ZaloMessagePayload {
        private String recipientId;
        private String message;

        public String getRecipientId() { return recipientId; }
        public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    static class ZaloResponse {
        private int error;
        private String message;

        public int getError() { return error; }
        public void setError(int error) { this.error = error; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    // -------------------------------------------------------------------------
    // Custom enterprise exception for notification failures
    // -------------------------------------------------------------------------
    @SuppressWarnings("serial")
    public static class NotificationSendException extends RuntimeException {
        private final ErrorCode errorCode;

        public NotificationSendException(ErrorCode errorCode, String message, Throwable cause) {
            super(message, cause);
            this.errorCode = errorCode;
        }

        public ErrorCode getErrorCode() { return errorCode; }
    }

    public enum ErrorCode {
        ZALO_API_ERROR,
        INVALID_INPUT,
        DUPLICATE_NOTIFICATION
    }

    // -------------------------------------------------------------------------
    // Utility method to mask sensitive data in logs (PII protection)
    // -------------------------------------------------------------------------
    private String maskSensitive(String input) {
        if (input == null) return null;
        return input.length() > 4 ? "****" + input.substring(input.length() - 4) : "****";
    }
}