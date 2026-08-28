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