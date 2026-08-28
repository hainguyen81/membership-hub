package org.nlh4j.membership_hub.chatbot;

// [REQ-019] Enterprise import dependencies for chatbot service testing
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nlh4j.membership_hub.chatbot.dto.AIResponse;
import org.nlh4j.membership_hub.chatbot.dto.AuditLogEntry;
import org.nlh4j.membership_hub.chatbot.dto.ChatRequest;
import org.nlh4j.membership_hub.chatbot.dto.ChatResponse;
import org.nlh4j.membership_hub.chatbot.exception.ChatbotException;
import org.nlh4j.membership_hub.chatbot.service.AIServiceClient;
import org.nlh4j.membership_hub.chatbot.service.AuditLogService;
import org.nlh4j.membership_hub.chatbot.service.ChatbotService;
import org.nlh4j.membership_hub.chatbot.service.EscalationService;
import org.nlh4j.membership_hub.chatbot.service.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Comprehensive test suite for ChatbotService validating all business requirements for AI chatbot integration
 * @verifies [REQ-019]
 */
@ExtendWith(MockitoExtension.class)
public class ChatbotServiceTest {
    // ==================== TOP-OF-CLASS ENTERPRISE CONSTANTS (NO HARDCODED LITERALS IN METHODS) ====================
    // [REQ-019] Traceability tag constant for audit and logging compliance
    public static final String TAG_REQ_019 = "[REQ-019]";
    // AI confidence threshold for escalation to human support (business rule: <0.7 triggers escalation)
    public static final float CONFIDENCE_ESCALATION_THRESHOLD = 0.7f;
    // Test data constants (isolated for reusability and maintainability)
    public static final String TEST_USER_ID = "123e4567-e89b-12d3-a456-426614174000";
    public static final String TEST_SESSION_ID = "123e4567-e89b-12d3-a456-426614174001";
    public static final String TEST_VALID_CHAT_MESSAGE = "How do I renew my membership card?";
    public static final String TEST_AI_HIGH_CONFIDENCE_REPLY = "You can renew your membership card in the Membership section of the app.";
    public static final float TEST_HIGH_CONFIDENCE_SCORE = 0.85f;
    public static final float TEST_LOW_CONFIDENCE_SCORE = 0.5f;
    // Error message constants (centralized for consistency)
    public static final String ERROR_MISSING_MESSAGE_FIELD = "Missing required field: message";
    public static final String ERROR_MISSING_SESSION_ID_FIELD = "Missing required field: sessionId";
    public static final String ERROR_EXPIRED_JWT_TOKEN = "JWT token expired or invalid";
    // Audit action constants (aligned with enterprise audit logging standards [NFR-006])
    public static final String AUDIT_ACTION_SUCCESSFUL_CHAT = "CHAT_MESSAGE_PROCESSED";
    public static final String AUDIT_ACTION_ESCALATION = "CHAT_ESCALATED_TO_SUPPORT";
    public static final String AUDIT_ACTION_PROCESSING_ERROR = "CHAT_PROCESSING_ERROR";

    // Enterprise SLF4J logger instance per logging mandate [0.3]
    private static final Logger logger = LoggerFactory.getLogger(ChatbotServiceTest.class);

    // Mocked external dependencies (unit testing isolation: no real network/DB calls)
    @Mock
    private AIServiceClient mockAiServiceClient;
    @Mock
    private EscalationService mockEscalationService;
    @Mock
    private AuditLogService mockAuditLogService;
    @Mock
    private JwtTokenProvider mockJwtTokenProvider;

    // System under test (SUT)
    private ChatbotService chatbotService;

    // ==================== TEST LIFECYCLE SETUP ====================
    /**
     * Initialize test environment and SUT before each test case
     * @verifies [REQ-019]
     */
    @BeforeEach
    void initializeTestEnvironment() {
        // [REQ-019] Inject mocked dependencies into ChatbotService constructor
        chatbotService = new ChatbotService(mockAiServiceClient, mockEscalationService, mockAuditLogService, mockJwtTokenProvider);
        logger.info("{} [TEST_START] Initialized ChatbotService test environment with mocked dependencies", TAG_REQ_019);
    }

    // ==================== TEST CASE 1: HAPPY PATH - HIGH CONFIDENCE AI RESPONSE ====================
    /**
     * Validate valid chat message with AI confidence >= 0.7 returns AI response directly without escalation
     * Business rule: High confidence AI responses are served directly to users to reduce support load
     * @verifies [REQ-019]
     */
    @Test
    void testProcessValidChatMessage_HighConfidence_ReturnsAiResponseWithoutEscalation() {
        logger.info("{} [TEST_START] Launching test case 1: Valid high confidence chat message processing", TAG_REQ_019);
        // Given: Valid request with authenticated user and high confidence AI response
        ChatRequest validRequest = new ChatRequest(TEST_VALID_CHAT_MESSAGE, TEST_SESSION_ID);
        UUID testUserId = UUID.fromString(TEST_USER_ID);
        when(mockJwtTokenProvider.validateToken(any(String.class))).thenReturn(true);
        when(mockJwtTokenProvider.getUserIdFromToken(any(String.class))).thenReturn(testUserId);
        when(mockAiServiceClient.getAIResponse(eq(TEST_VALID_CHAT_MESSAGE), eq(testUserId)))
                .thenReturn(new AIResponse(TEST_AI_HIGH_CONFIDENCE_REPLY, TEST_HIGH_CONFIDENCE_SCORE));

        // When: Process the chat message through the service
        ChatResponse response = chatbotService.processChatMessage(validRequest, "valid-jwt-token");

        // Then: Validate response content and no escalation triggered
        // [REQ-019] Assert AI reply is returned correctly
        assertThat(response.getReply()).isEqualTo(TEST_AI_HIGH_CONFIDENCE_REPLY)
                .as("AI reply should match expected response for high confidence query");
        // [REQ-019] Assert escalation flag is false for high confidence
        assertThat(response.isEscalate()).isFalse()
                .as("Escalation flag should be false for confidence >= 0.7");
        // [REQ-019] Verify escalation service is never called for high confidence responses
        verify(mockEscalationService, never()).createSupportTicket(any(), any(), any())
                .as("Escalation service should not be triggered for high confidence responses");
        // [REQ-019] Verify audit log is called with correct parameters for successful processing
        ArgumentCaptor<AuditLogEntry> auditLogCaptor = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(mockAuditLogService).logAudit(auditLogCaptor.capture())
                .as("Audit log must be called for all chat processing operations per NFR-006");
        AuditLogEntry capturedAudit = auditLogCaptor.getValue();
        assertThat(capturedAudit.getUserId()).isEqualTo(testUserId)
                .as("Audit log must contain correct user ID");
        assertThat(capturedAudit.getAction()).isEqualTo(AUDIT_ACTION_SUCCESSFUL_CHAT)
                .as("Audit log action must match successful processing");
        assertThat(capturedAudit.getDetails()).contains(TEST_VALID_CHAT_MESSAGE, String.valueOf(TEST_HIGH_CONFIDENCE_SCORE))
                .as("Audit log must contain request context and confidence score");
        assertThat(capturedAudit.getTagId()).isEqualTo(TAG_REQ_019)
                .as("Audit log must include traceability tag for compliance");
        logger.info("{} [TEST_PASS] Test case 1 passed: High confidence response returned without escalation", TAG_REQ_019);
    }

    // ==================== TEST CASE 2: LOW CONFIDENCE TRIGGERS HUMAN SUPPORT ESCALATION ====================
    /**
     * Validate chat message with AI confidence < 0.7 triggers escalation to human support with ticket creation
     * Business rule: Low confidence responses are escalated to human support to ensure user satisfaction
     * @verifies [REQ-019]
     */
    @Test
    void testProcessChatMessage_LowConfidence_TriggersEscalationAndTicketCreation() {
        logger.info("{} [TEST_START] Launching test case 2: Low confidence chat message triggers escalation", TAG_REQ_019);
        // Given: Valid request with low confidence AI response
        ChatRequest validRequest = new ChatRequest(TEST_VALID_CHAT_MESSAGE, TEST_SESSION_ID);
        UUID testUserId = UUID.fromString(TEST_USER_ID);
        String lowConfidenceReply = "I'm not sure how to help with that request. I will connect you to a support agent.";
        when(mockJwtTokenProvider.validateToken(any(String.class))).thenReturn(true);
        when(mockJwtTokenProvider.getUserIdFromToken(any(String.class))).thenReturn(testUserId);
        when(mockAiServiceClient.getAIResponse(eq(TEST_VALID_CHAT_MESSAGE), eq(testUserId)))
                .thenReturn(new AIResponse(lowConfidenceReply, TEST_LOW_CONFIDENCE_SCORE));

        // When: Process the chat message through the service
        ChatResponse response = chatbotService.processChatMessage(validRequest, "valid-jwt-token");

        // Then: Validate escalation is triggered and support ticket is created
        // [REQ-019] Assert escalation flag is true for low confidence
        assertThat(response.isEscalate()).isTrue()
                .as("Escalation flag should be true for confidence < 0.7");
        assertThat(response.getReply()).isEqualTo(lowConfidenceReply)
                .as("Response should contain low confidence notification");
        // [REQ-019] Verify escalation service creates a support ticket with correct parameters
        verify(mockEscalationService).createSupportTicket(eq(testUserId), eq(UUID.fromString(TEST_SESSION_ID)), eq(TEST_VALID_CHAT_MESSAGE))
                .as("Escalation service must create support ticket for low confidence responses");
        // [REQ-019] Verify audit log is called with escalation action
        ArgumentCaptor<AuditLogEntry> auditLogCaptor = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(mockAuditLogService).logAudit(auditLogCaptor.capture())
                .as("Audit log must record escalation event");
        AuditLogEntry capturedAudit = auditLogCaptor.getValue();
        assertThat(capturedAudit.getAction()).isEqualTo(AUDIT_ACTION_ESCALATION)
                .as("Audit log action must match escalation event");
        assertThat(capturedAudit.getDetails()).contains(String.valueOf(TEST_LOW_CONFIDENCE_SCORE))
                .as("Audit log must contain confidence score for escalation");
        logger.info("{} [TEST_PASS] Test case 2 passed: Low confidence response triggered escalation and ticket creation", TAG_REQ_019);
    }

    // ==================== TEST CASE 3: EXCEPTION CASE - MISSING REQUIRED FIELDS ====================
    /**
     * Validate chat request missing required fields (message/sessionId) throws IllegalArgumentException
     * Business rule: All required fields must be present to process a chat request
     * @verifies [REQ-019]
     */
    @Test
    void testProcessChatMessage_MissingRequiredFields_ThrowsIllegalArgumentException() {
        logger.info("{} [TEST_START] Launching test case 3: Missing required fields validation", TAG_REQ_019);
        // Test case 3a: Missing message field
        ChatRequest requestMissingMessage = new ChatRequest(null, TEST_SESSION_ID);
        IllegalArgumentException exceptionMissingMessage = assertThrows(IllegalArgumentException.class,
                () -> chatbotService.processChatMessage(requestMissingMessage, "valid-jwt-token"),
                "Missing message should throw IllegalArgumentException");
        assertThat(exceptionMissingMessage.getMessage()).isEqualTo(ERROR_MISSING_MESSAGE_FIELD)
                .as("Exception message must clearly indicate missing field");
        // Verify audit log is called for validation error
        verify(mockAuditLogService).logAudit(any(AuditLogEntry.class))
                .as("Audit log must record validation errors");
        logger.info("{} [TEST_PASS] Test case 3a passed: Missing message throws IllegalArgumentException", TAG_REQ_019);

        // Test case 3b: Missing sessionId field
        ChatRequest requestMissingSessionId = new ChatRequest(TEST_VALID_CHAT_MESSAGE, null);
        IllegalArgumentException exceptionMissingSessionId = assertThrows(IllegalArgumentException.class,
                () -> chatbotService.processChatMessage(requestMissingSessionId, "valid-jwt-token"),
                "Missing sessionId should throw IllegalArgumentException");
        assertThat(exceptionMissingSessionId.getMessage()).isEqualTo(ERROR_MISSING_SESSION_ID_FIELD)
                .as("Exception message must clearly indicate missing field");
        // Verify audit log is called for second validation error
        verify(mockAuditLogService, org.mockito.Mockito.times(2)).logAudit(any(AuditLogEntry.class))
                .as("Audit log must be called for every validation error");
        logger.info("{} [TEST_PASS] Test case 3b passed: Missing sessionId throws IllegalArgumentException", TAG_REQ_019);

        // Test case 3c: Empty message field (edge case)
        ChatRequest requestEmptyMessage = new ChatRequest("", TEST_SESSION_ID);
        IllegalArgumentException exceptionEmptyMessage = assertThrows(IllegalArgumentException.class,
                () -> chatbotService.processChatMessage(requestEmptyMessage, "valid-jwt-token"),
                "Empty message should throw IllegalArgumentException");
        assertThat(exceptionEmptyMessage.getMessage()).isEqualTo(ERROR_MISSING_MESSAGE_FIELD)
                .as("Empty message is treated as missing required field");
        logger.info("{} [TEST_PASS] Test case 3c passed: Empty message throws IllegalArgumentException", TAG_REQ_019);
    }

    // ==================== TEST CASE 4: EXCEPTION CASE - EXPIRED JWT TOKEN ====================
    /**
     * Validate chat request with expired JWT token returns 401 Unauthorized error
     * Business rule: All chat requests must be authenticated with a valid JWT token
     * @verifies [REQ-019]
     */
    @Test
    void testProcessChatMessage_ExpiredJwtToken_ReturnsUnauthorizedError() {
        logger.info("{} [TEST_START] Launching test case 4: Expired JWT token validation", TAG_REQ_019);
        // Given: Valid request but expired/invalid JWT token
        ChatRequest validRequest = new ChatRequest(TEST_VALID_CHAT_MESSAGE, TEST_SESSION_ID);
        when(mockJwtTokenProvider.validateToken(any(String.class))).thenThrow(new ChatbotException(401, ERROR_EXPIRED_JWT_TOKEN));

        // When: Attempt to process the chat message with invalid JWT
        ChatbotException thrownException = assertThrows(ChatbotException.class,
                () -> chatbotService.processChatMessage(validRequest, "expired-jwt-token"),
                "Expired JWT should throw ChatbotException with 401 status");

        // Then: Validate 401 Unauthorized error is returned
        assertThat(thrownException.getStatusCode()).isEqualTo(401)
                .as("Expired JWT must return 401 Unauthorized status code");
        assertThat(thrownException.getMessage()).isEqualTo(ERROR_EXPIRED_JWT_TOKEN)
                .as("Error message must clearly indicate JWT expiration");
        // Verify no downstream services are called for invalid JWT
        verify(mockAiServiceClient, never()).getAIResponse(any(), any())
                .as("AI service should not be called for unauthenticated requests");
        verify(mockEscalationService, never()).createSupportTicket(any(), any(), any())
                .as("Escalation service should not be called for unauthenticated requests");
        // Verify audit log is called with error action
        ArgumentCaptor<AuditLogEntry> auditLogCaptor = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(mockAuditLogService).logAudit(auditLogCaptor.capture())
                .as("Audit log must record authentication failure");
        AuditLogEntry capturedAudit = auditLogCaptor.getValue();
        assertThat(capturedAudit.getAction()).isEqualTo(AUDIT_ACTION_PROCESSING_ERROR)
                .as("Audit log action must match authentication error");
        assertThat(capturedAudit.getDetails()).contains("JWT validation failed")
                .as("Audit log must contain error context");
        logger.info("{} [TEST_PASS] Test case 4 passed: Expired JWT returns 401 Unauthorized", TAG_REQ_019);
    }

    // ==================== TEST CASE 5: AUDIT LOG COMPLIANCE VALIDATION ====================
    /**
     * Validate audit log is called with all required parameters for compliance with enterprise audit standards [NFR-006]
     * Business rule: All chat processing operations must be logged with full context for security auditing
     * @verifies [REQ-019]
     */
    @Test
    void testAuditLogIsCalledWithAllRequiredParameters_ForSuccessfulOperations() {
        logger.info("{} [TEST_START] Launching test case 5: Audit log compliance validation", TAG_REQ_019);
        // Given: Valid high confidence request
        ChatRequest validRequest = new ChatRequest(TEST_VALID_CHAT_MESSAGE, TEST_SESSION_ID);
        UUID testUserId = UUID.fromString(TEST_USER_ID);
        when(mockJwtTokenProvider.validateToken(any(String.class))).thenReturn(true);
        when(mockJwtTokenProvider.getUserIdFromToken(any(String.class))).thenReturn(testUserId);
        when(mockAiServiceClient.getAIResponse(eq(TEST_VALID_CHAT_MESSAGE), eq(testUserId)))
                .thenReturn(new AIResponse(TEST_AI_HIGH_CONFIDENCE_REPLY, TEST_HIGH_CONFIDENCE_SCORE));

        // When: Process the chat message
        chatbotService.processChatMessage(validRequest, "valid-jwt-token");

        // Then: Validate all mandatory audit log fields are present and correct
        ArgumentCaptor<AuditLogEntry> auditLogCaptor = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(mockAuditLogService).logAudit(auditLogCaptor.capture())
                .as("Audit log must be called for every chat processing operation");
        AuditLogEntry capturedAudit = auditLogCaptor.getValue();
        // Validate all mandatory audit fields per enterprise audit standards [NFR-006]
        assertThat(capturedAudit.getAuditId()).isNotNull()
                .as("Audit log must have unique audit ID");
        assertThat(capturedAudit.getUserId()).isEqualTo(testUserId)
                .as("Audit log must contain authenticated user ID");
        assertThat(capturedAudit.getAction()).isEqualTo(AUDIT_ACTION_SUCCESSFUL_CHAT)
                .as("Audit log must have correct action type");
        assertThat(capturedAudit.getTimestamp()).isNotNull()
                .as("Audit log must have timestamp of operation");
        assertThat(capturedAudit.getDetails()).isNotNull().isNotEmpty()
                .as("Audit log must contain operation context details");
        assertThat(capturedAudit.getTagId()).isEqualTo(TAG_REQ_019)
                .as("Audit log must include traceability tag for requirement compliance");
        logger.info("{} [TEST_PASS] Test case 5 passed: Audit log contains all required compliance parameters", TAG_REQ_019);
    }

    // ==================== EDGE CASE TEST: EMPTY AI RESPONSE HANDLING ====================
    /**
     * Edge case: AI returns empty reply with high confidence, validate graceful handling
     * @verifies [REQ-019]
     */
    @Test
    void testProcessChatMessage_HighConfidence_EmptyAiReply_HandledGracefully() {
        logger.info("{} [TEST_START] Launching edge case: Empty AI reply with high confidence", TAG_REQ_019);
        // Given: Valid request, AI returns empty reply with high confidence
        ChatRequest validRequest = new ChatRequest(TEST_VALID_CHAT_MESSAGE, TEST_SESSION_ID);
        UUID testUserId = UUID.fromString(TEST_USER_ID);
        when(mockJwtTokenProvider.validateToken(any(String.class))).thenReturn(true);
        when(mockJwtTokenProvider.getUserIdFromToken(any(String.class))).thenReturn(testUserId);
        when(mockAiServiceClient.getAIResponse(eq(TEST_VALID_CHAT_MESSAGE), eq(testUserId)))
                .thenReturn(new AIResponse("", TEST_HIGH_CONFIDENCE_SCORE));

        // When: Process the chat message
        ChatResponse response = chatbotService.processChatMessage(validRequest, "valid-jwt-token");

        // Then: Validate empty reply is handled without escalation
        assertThat(response.getReply()).isEqualTo("")
                .as("Empty AI reply should be returned as-is without modification");
        assertThat(response.isEscalate()).isFalse()
                .as("Empty high confidence reply should not trigger escalation");
        verify(mockEscalationService, never()).createSupportTicket(any(), any(), any())
                .as("Escalation service should not be called for empty high confidence replies");
        logger.info("{} [TEST_PASS] Edge case passed: Empty AI reply handled gracefully", TAG_REQ_019);
    }
}