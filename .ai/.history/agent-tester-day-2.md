# Day 2: model kilo-auto/free - API Endpoint https://api.kilo.ai/api/gateway
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/chatbot-service/src/main/java/org/nlh4j/membership_hub/chatbot/ChatbotService.java
# Day 2: model openai/gpt-oss-20b:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/membership-hub/src/test/java/com/membershiphub/rest/CenterResourceTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Test Component Destination Path: `./sources/backend/chatbot-service/src/main/java/org/nlh4j/membership_hub/chatbot/ChatbotService.java` (Must map to sources/backend/ or sources/frontend/)


### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/chatbot-service/src/main/java/org/nlh4j/membership_hub/chatbot/ChatbotService.java. 
*   Target Test Component Destination Path: `./sources/backend/membership-hub/src/test/java/com/membershiphub/rest/CenterResourceTest.java` (Must map to sources/backend/ or sources/frontend/)


### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/membership-hub/src/test/java/com/membershiphub/rest/CenterResourceTest.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Viết bộ kiểm thử đơn vị sử dụng JUnit 5 và Mockito để kiểm tra toàn bộ logic của ChatbotService. Các trường hợp test bắt buộc: (1) Truy vấn hợp lệ với confidence >= 0.7, kiểm tra trả về reply từ AI và escalate = false; (2) Truy vấn có confidence = 0.5, kiểm tra escalate = true và ticket hỗ trợ được tạo thông qua EscalationService; (3) Request thiếu trường message hoặc sessionId, kiểm tra ném ra IllegalArgumentException với thông báo rõ ràng; (4) Request với JWT token hết hạn, kiểm tra trả về lỗi 401 Unauthorized; (5) Kiểm tra phương thức ghi log audit được gọi đúng với đầy đủ tham số. Đảm bảo độ bao phủ mã đạt trên 90% bằng JaCoCo và tất cả test case pass trên môi trường CI/CD.']
['Viết bộ kiểm thử đơn vị và tích hợp cho API quản lý trung tâm']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` structure. Ensure that you read the exact Tag IDs from the `['[REQ-019]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership_hub.chatbot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nlh4j.saas.membership_hub.chatbot.exception.UnauthorizedException;
import org.nlh4j.saas.membership_hub.chatbot.model.AIResponse;
import org.nlh4j.saas.membership_hub.chatbot.service.AIServiceClient;
import org.nlh4j.saas.membership_hub.chatbot.service.AuditService;
import org.nlh4j.saas.membership_hub.chatbot.service.EscalationService;
import org.nlh4j.saas.membership_hub.chatbot.service.JwtUtils;
import org.nlh4j.saas.membership_hub.chatbot.ChatbotService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit test suite for ChatbotService validating all business logic requirements
 * @verifies [REQ-019]
 */
@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    // Mock external dependencies to isolate ChatbotService business logic
    @Mock
    private AIServiceClient aiServiceClient;

    @Mock
    private EscalationService escalationService;

    @Mock
    private AuditService auditService;

    @Mock
    private JwtUtils jwtUtils;

    // Inject mocked dependencies into the service under test
    @InjectMocks
    private ChatbotService chatbotService;

    /**
     * @verifies [REQ-019] Valid user query with AI confidence >= 0.7 returns AI reply without escalation
     * Happy path test for high-confidence AI response flow
     */
    @Test
    void processMessage_ValidHighConfidence_ReturnsAIReplyWithoutEscalation() {
        // Arrange: Initialize test data and mock behavior
        String validMessage = "Lịch học hôm nay của tôi là gì?";
        String validSessionId = "session-123e4567-e89b-12d3-a456-426614174000";
        String validJwt = "valid-jwt-token";
        String testUserId = "user-123e4567-e89b-12d3-a456-426614174000";

        // Mock JWT validation to return valid token and extract user ID
        org.mockito.Mockito.when(jwtUtils.validateToken(validJwt)).thenReturn(true);
        org.mockito.Mockito.when(jwtUtils.getUserIdFromToken(validJwt)).thenReturn(testUserId);

        // Mock AI service to return high confidence response (meets 0.7 threshold)
        AIResponse aiResponse = new AIResponse("Lịch học hôm nay của bạn là Toán 18h tại phòng A101", 0.8, false);
        org.mockito.Mockito.when(aiServiceClient.getAIResponse(validMessage, validSessionId)).thenReturn(aiResponse);

        // Act: Execute the method under test
        var result = chatbotService.processMessage(validMessage, validSessionId, validJwt);

        // Assert: Validate response payload and side effects
        assertThat(result.getReply()).isEqualTo("Lịch học hôm nay của bạn là Toán 18h tại phòng A101");
        assertThat(result.getConfidence()).isEqualTo(0.8);
        assertThat(result.isEscalate()).isFalse();

        // Verify escalation service is never invoked for high confidence queries
        verify(escalationService, never()).createSupportTicket(any(), any(), any());

        // Verify audit log is called with correct compliance parameters per NFR-006
        verify(auditService).logAudit(
                eq(testUserId),
                eq("CHATBOT_QUERY_PROCESSED"),
                eq("Chatbot query processed successfully with confidence 0.8, no escalation required"),
                org.mockito.ArgumentMatchers.anyMap()
        );
    }

    /**
     * @verifies [REQ-019] Low confidence query (0.5 < 0.7 threshold) triggers escalation and support ticket creation
     * Edge case test for AI confidence below threshold requiring human intervention
     */
    @Test
    void processMessage_LowConfidence_TriggersEscalationAndCreatesTicket() {
        // Arrange: Setup test data for low confidence scenario
        String validMessage = "Tôi muốn hủy khóa học nhưng không thấy nút hủy";
        String validSessionId = "session-456e7890-e89b-12d3-a456-426614174000";
        String validJwt = "valid-jwt-token-2";
        String testUserId = "user-456e7890-e89b-12d3-a456-426614174000";

        // Mock JWT validation
        org.mockito.Mockito.when(jwtUtils.validateToken(validJwt)).thenReturn(true);
        org.mockito.Mockito.when(jwtUtils.getUserIdFromToken(validJwt)).thenReturn(testUserId);

        // Mock AI service to return low confidence response (below 0.7 threshold)
        AIResponse aiResponse = new AIResponse("Tôi không chắc về yêu cầu này, sẽ chuyển cho hỗ trợ viên", 0.5, true);
        org.mockito.Mockito.when(aiServiceClient.getAIResponse(validMessage, validSessionId)).thenReturn(aiResponse);

        // Act: Execute the method under test
        var result = chatbotService.processMessage(validMessage, validSessionId, validJwt);

        // Assert: Validate escalation flag and support ticket creation
        assertThat(result.isEscalate()).isTrue();
        assertThat(result.getReply()).contains("chuyển cho hỗ trợ viên");

        // Verify escalation service creates support ticket with correct parameters
        verify(escalationService).createSupportTicket(eq(validSessionId), eq(validMessage), eq(testUserId));

        // Verify audit log records the escalation event for compliance
        verify(auditService).logAudit(
                eq(testUserId),
                eq("CHATBOT_ESCALATION_TRIGGERED"),
                eq("Chatbot query escalated to human support due to low confidence 0.5"),
                org.mockito.ArgumentMatchers.anyMap()
        );
    }

    /**
     * @verifies [REQ-019] Missing required input fields (message/sessionId) throws clear IllegalArgumentException
     * Negative path test for input validation failure
     */
    @Test
    void processMessage_MissingRequiredFields_ThrowsIllegalArgumentException() {
        String validJwt = "valid-jwt-token";
        String validSessionId = "session-789e0123-e89b-12d3-a456-426614174000";

        // Test case 3a: Null message input
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class,
                () -> chatbotService.processMessage(null, validSessionId, validJwt));
        assertThat(exception1.getMessage()).isEqualTo("Message and sessionId are required fields");

        // Test case 3b: Empty message input
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class,
                () -> chatbotService.processMessage("", validSessionId, validJwt));
        assertThat(exception2.getMessage()).isEqualTo("Message and sessionId are required fields");

        // Test case 3c: Null sessionId input
        IllegalArgumentException exception3 = assertThrows(IllegalArgumentException.class,
                () -> chatbotService.processMessage("Test message", null, validJwt));
        assertThat(exception3.getMessage()).isEqualTo("Message and sessionId are required fields");

        // Test case 3d: Empty sessionId input
        IllegalArgumentException exception4 = assertThrows(IllegalArgumentException.class,
                () -> chatbotService.processMessage("Test message", "", validJwt));
        assertThat(exception4.getMessage()).isEqualTo("Message and sessionId are required fields");

        // Verify no external services are invoked for invalid input
        verify(aiServiceClient, never()).getAIResponse(any(), any());
        verify(escalationService, never()).createSupportTicket(any(), any(), any());
        verify(auditService, never()).logAudit(any(), any(), any(), any());
    }

    /**
     * @verifies [REQ-019] Expired/invalid JWT token returns 401 Unauthorized error
     * Negative path test for authentication failure
     */
    @Test
    void processMessage_ExpiredJwtToken_ThrowsUnauthorizedException() {
        // Arrange: Setup test data for expired token scenario
        String validMessage = "Test message";
        String validSessionId = "session-012e3456-e89b-12d3-a456-426614174000";
        String expiredJwt = "expired-jwt-token";

        // Mock JWT validation to return false (expired/invalid token)
        org.mockito.Mockito.when(jwtUtils.validateToken(expiredJwt)).thenReturn(false);

        // Act & Assert: Verify UnauthorizedException is thrown (maps to HTTP 401)
        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> chatbotService.processMessage(validMessage, validSessionId, expiredJwt));
        assertThat(exception.getMessage()).isEqualTo("JWT token is expired or invalid");

        // Verify no external business logic services are called for unauthenticated requests
        verify(aiServiceClient, never()).getAIResponse(any(), any());
        verify(escalationService, never()).createSupportTicket(any(), any(), any());
        verify(auditService, never()).logAudit(any(), any(), any(), any());
    }

    /**
     * @verifies [REQ-019] Audit log is called with complete correct parameters for all valid requests
     * Compliance test for NFR-006 audit logging requirements
     */
    @Test
    void processMessage_ValidRequest_AuditLogCalledWithCorrectParameters() {
        // Arrange: Setup test data for audit verification
        String validMessage = "Câu hỏi về giá khóa học";
        String validSessionId = "session-112e3456-e89b-12d3-a456-426614174000";
        String validJwt = "valid-jwt-token-3";
        String testUserId = "user-112e3456-e89b-12d3-a456-426614174000";
        double testConfidence = 0.9;
        boolean testEscalate = false;

        // Mock dependencies
        org.mockito.Mockito.when(jwtUtils.validateToken(validJwt)).thenReturn(true);
        org.mockito.Mockito.when(jwtUtils.getUserIdFromToken(validJwt)).thenReturn(testUserId);
        AIResponse aiResponse = new AIResponse("Giá khóa học là 2 triệu/tháng", testConfidence, testEscalate);
        org.mockito.Mockito.when(aiServiceClient.getAIResponse(validMessage, validSessionId)).thenReturn(aiResponse);

        // Act: Execute the method under test
        chatbotService.processMessage(validMessage, validSessionId, validJwt);

        // Assert: Verify audit log contains all required compliance fields
        verify(auditService).logAudit(
                eq(testUserId), // User identifier from validated JWT
                eq("CHATBOT_QUERY_PROCESSED"), // Standardized audit action type
                eq("Chatbot query processed with confidence: " + testConfidence + ", escalated: " + testEscalate), // Detailed action description
                org.mockito.ArgumentMatchers.argMap -> {
                    // Verify metadata contains all mandatory audit fields per NFR-006
                    assertThat(argMap).containsEntry("sessionId", validSessionId);
                    assertThat(argMap).containsEntry("confidence", testConfidence);
                    assertThat(argMap).containsEntry("escalate", testEscalate);
                    assertThat(argMap).containsEntry("messagePreview", validMessage.substring(0, Math.min(50, validMessage.length())));
                    assertThat(argMap).containsKey("timestamp");
                    assertThat(argMap).containsKey("ipAddress");
                }
        );
    }
}
```

# Day 2: model kilo-auto/free - API Endpoint https://api.kilo.ai/api/gateway
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/chatbot-service/src/test/java/org/nlh4j/membership_hub/chatbot/ChatbotServiceTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Test Component Destination Path: `./sources/backend/chatbot-service/src/test/java/org/nlh4j/membership_hub/chatbot/ChatbotServiceTest.java` (Must map to sources/backend/ or sources/frontend/)


### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/chatbot-service/src/test/java/org/nlh4j/membership_hub/chatbot/ChatbotServiceTest.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Viết bộ kiểm thử đơn vị sử dụng JUnit 5 và Mockito để kiểm tra toàn bộ logic của ChatbotService. Các trường hợp test bắt buộc: (1) Truy vấn hợp lệ với confidence >= 0.7, kiểm tra trả về reply từ AI và escalate = false; (2) Truy vấn có confidence = 0.5, kiểm tra escalate = true và ticket hỗ trợ được tạo thông qua EscalationService; (3) Request thiếu trường message hoặc sessionId, kiểm tra ném ra IllegalArgumentException với thông báo rõ ràng; (4) Request với JWT token hết hạn, kiểm tra trả về lỗi 401 Unauthorized; (5) Kiểm tra phương thức ghi log audit được gọi đúng với đầy đủ tham số. Đảm bảo độ bao phủ mã đạt trên 90% bằng JaCoCo và tất cả test case pass trên môi trường CI/CD.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` structure. Ensure that you read the exact Tag IDs from the `['[REQ-019]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
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
Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` structure. Ensure that you read the exact Tag IDs from the `['[REQ-004]', '[REQ-005]', '[REQ-006]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership-hub.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestSecurity;
import io.quarkus.test.common.QuarkusTestResource;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Integration tests for {@link CenterResource} covering CRUD operations and RBAC enforcement.
 *
 * @verifies [REQ-004], [REQ-005], [REQ-006]
 */
@QuarkusTest
@QuarkusTestResource(PostgreSQLContainer.class)
public class CenterResourceTest {

    /* --------------------------------------------------------------------- */
    /*  Constants – API endpoints and test payloads                           */
    /* --------------------------------------------------------------------- */
    private static final String BASE_URL = "/api/v1";
    private static final String CENTERS_ENDPOINT = BASE_URL + "/centers";
    private static final String ADMIN_CENTERS_ENDPOINT = BASE_URL + "/admin/centers";
    private static final String ADMIN_CENTER_ADMINS_ENDPOINT = BASE_URL + "/admin/centers/%s/admins";

    /* --------------------------------------------------------------------- */
    /*  Helper – Build a JSON payload for a center                            */
    /* --------------------------------------------------------------------- */
    private String centerPayload(String name, String address, String taxId,
                                String phone, String email) {
        return String.format(
                "{\"name\":\"%s\",\"address\":\"%s\",\"taxId\":\"%s\",\"contactPhone\":\"%s\",\"contactEmail\":\"%s\"}",
                name, address, taxId, phone, email);
    }

    /* --------------------------------------------------------------------- */
    /*  Happy Path – Create a center                                           */
    /* --------------------------------------------------------------------- */
    @Test
    @TestSecurity(authorizationEnabled = true, user = "systemAdmin", roles = {"system-admin"})
    @DisplayName("Create Center – Success [REQ-004]")
    void testCreateCenterSuccess() {
        // Arrange – valid center data
        String payload = centerPayload("Alpha Center", "123 Main St", "1234567890",
                "555-0100", "alpha@example.com");

        // Act – POST to create center
        given()
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .post(ADMIN_CENTERS_ENDPOINT)
        .then()
                // Assert – HTTP 201 and returned fields match input
                .statusCode(201)
                .body("name", equalTo("Alpha Center"))
                .body("taxId", equalTo("1234567890"));
    }

    /* --------------------------------------------------------------------- */
    /*  Edge Case – Duplicate taxId conflict                                  */
    /* --------------------------------------------------------------------- */
    @Test
    @TestSecurity(authorizationEnabled = true, user = "systemAdmin", roles = {"system-admin"})
    @DisplayName("Create Center – Duplicate Tax ID [REQ-005]")
    void testCreateCenterDuplicateTaxId() {
        // Arrange – create first center
        String payload1 = centerPayload("Beta Center", "456 Elm St", "9876543210",
                "555-0200", "beta@example.com");
        given()
                .contentType(ContentType.JSON)
                .body(payload1)
        .when()
                .post(ADMIN_CENTERS_ENDPOINT)
        .then()
                .statusCode(201);

        // Act – attempt to create second center with same taxId
        String payload2 = centerPayload("Gamma Center", "789 Oak St", "9876543210",
                "555-0300", "gamma@example.com");
        given()
                .contentType(ContentType.JSON)
                .body(payload2)
        .when()
                .post(ADMIN_CENTERS_ENDPOINT)
        .then()
                // Assert – HTTP 409 Conflict
                .statusCode(409)
                .body("error", equalTo("TAX_ID_CONFLICT"));
    }

    /* --------------------------------------------------------------------- */
    /*  Happy Path – Retrieve centers list                                    */
    /* --------------------------------------------------------------------- */
    @Test
    @TestSecurity(authorizationEnabled = true, user = "systemAdmin", roles = {"system-admin"})
    @DisplayName("Get Centers – List [REQ-004]")
    void testGetCenters() {
        // Arrange – ensure at least one center exists
        String payload = centerPayload("Delta Center", "1010 Maple St", "1112223334",
                "555-0400", "delta@example.com");
        given()
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .post(ADMIN_CENTERS_ENDPOINT)
        .then()
                .statusCode(201);

        // Act – GET list
        given()
        .when()
                .get(CENTERS_ENDPOINT)
        .then()
                // Assert – list contains the newly created center
                .statusCode(200)
                .body("name", hasItem("Delta Center"));
    }

    /* --------------------------------------------------------------------- */
    /*  Happy Path – Update a center                                           */
    /* --------------------------------------------------------------------- */
    @Test
    @TestSecurity(authorizationEnabled = true, user = "systemAdmin", roles = {"system-admin"})
    @DisplayName("Update Center – Success [REQ-005]")
    void testUpdateCenterSuccess() {
        // Arrange – create center to update
        String payloadCreate = centerPayload("Epsilon Center", "2020 Birch St", "5556667778",
                "555-0500", "epsilon@example.com");
        String centerId = given()
                .contentType(ContentType.JSON)
                .body(payloadCreate)
        .when()
                .post(ADMIN_CENTERS_ENDPOINT)
        .then()
                .statusCode(201)
                .extract()
                .path("centerId");

        // Act – update center name
        String payloadUpdate = "{\"name\":\"Epsilon Center Updated\",\"address\":\"2020 Birch St\",\"taxId\":\"5556667778\",\"contactPhone\":\"555-0500\",\"contactEmail\":\"epsilon@example.com\"}";
        given()
                .contentType(ContentType.JSON)
                .body(payloadUpdate)
        .when()
                .put(ADMIN_CENTERS_ENDPOINT + "/" + centerId)
        .then()
                // Assert – HTTP 200 and updated name
                .statusCode(200)
                .body("name", equalTo("Epsilon Center Updated"));
    }

    /* --------------------------------------------------------------------- */
    /*  Happy Path – Delete a center                                           */
    /* --------------------------------------------------------------------- */
    @Test
    @TestSecurity(authorizationEnabled = true, user = "systemAdmin", roles = {"system-admin"})
    @DisplayName("Delete Center – Success [REQ-006]")
    void testDeleteCenterSuccess() {
        // Arrange – create center to delete
        String payload = centerPayload("Zeta Center", "3030 Cedar St", "9998887776",
                "555-0600", "zeta@example.com");
        String centerId = given()
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .post(ADMIN_CENTERS_ENDPOINT)
        .then()
                .statusCode(201)
                .extract()
                .path("centerId");

        // Act – delete center
        given()
        .when()
                .delete(ADMIN_CENTERS_ENDPOINT + "/" + centerId)
        .then()
                // Assert – HTTP 204 No Content
                .statusCode(204);

        // Verify – center no longer appears in list
        given()
        .when()
                .get(CENTERS_ENDPOINT)
        .then()
                .statusCode(200)
                .body("centerId", not(hasItem(centerId)));
    }

    /* --------------------------------------------------------------------- */
    /*  Happy Path – Assign Center Admin                                       */
    /* --------------------------------------------------------------------- */
    @Test
    @TestSecurity(authorizationEnabled = true, user = "systemAdmin", roles = {"system-admin"})
    @DisplayName("Assign Center Admin – Success [REQ-006]")
    void testAssignCenterAdminSuccess() {
        // Arrange – create center
        String payloadCenter = centerPayload("Eta Center", "4040 Walnut St", "2223334445",
                "555-0700", "eta@example.com");
        String centerId = given()
                .contentType(ContentType.JSON)
                .body(payloadCenter)
        .when()
                .post(ADMIN_CENTERS_ENDPOINT)
        .then()
                .statusCode(201)
                .extract()
                .path("centerId");

        // Act – assign a user (userId is arbitrary for test)
        String assignPayload = "{\"userId\":\"123e4567-e89b-12d3-a456-426614174000\",\"isAssign\":true}";
        given()
                .contentType(ContentType.JSON)
                .body(assignPayload)
        .when()
                .post(String.format(ADMIN_CENTER_ADMINS_ENDPOINT, centerId))
        .then()
                // Assert – HTTP 200 OK
                .statusCode(200)
                .body("message", equalTo("Thao tác phân quyền trung tâm thành công"));
    }

    /* --------------------------------------------------------------------- */
    /*  Exception Case – Unauthorized access                                 */
    /* --------------------------------------------------------------------- */
    @Test
    @TestSecurity(authorizationEnabled = true, user = "student", roles = {"student"})
    @DisplayName("Create Center – Unauthorized [REQ-006]")
    void testCreateCenterUnauthorized() {
        // Arrange – valid payload
        String payload = centerPayload("Theta Center", "5050 Spruce St", "4445556667",
                "555-0800", "theta@example.com");

        // Act – attempt to create center as a student
        given()
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .post(ADMIN_CENTERS_ENDPOINT)
        .then()
                // Assert – HTTP 403 Forbidden
                .statusCode(403);
    }

    /* --------------------------------------------------------------------- */
    /*  Exception Case – Delete non-existent center                          */
    /* --------------------------------------------------------------------- */
    @Test
    @TestSecurity(authorizationEnabled = true, user = "systemAdmin", roles = {"system-admin"})
    @DisplayName("Delete Center – Not Found [REQ-006]")
    void testDeleteCenterNotFound() {
        // Act – delete a random UUID
        given()
        .when()
                .delete(ADMIN_CENTERS_ENDPOINT + "/00000000-0000-0000-0000-000000000000")
        .then()
                // Assert – HTTP 404 Not Found
                .statusCode(404);
    }
}
```

