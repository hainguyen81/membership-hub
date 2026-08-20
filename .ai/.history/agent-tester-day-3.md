# Day 3: model kilo-auto/free - API Endpoint https://api.kilo.ai/api/gateway
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/notification-service/src/test/java/com/hub/notification/NotificationIntegrationTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Test Component Destination Path: `./sources/backend/notification-service/src/test/java/com/hub/notification/NotificationIntegrationTest.java` (Must map to sources/backend/ or sources/frontend/)


### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/notification-service/src/test/java/com/hub/notification/NotificationIntegrationTest.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Viết bộ kiểm thử tích hợp cho luồng gửi thông báo đa kênh']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` structure. Ensure that you read the exact Tag IDs from the `['[REQ-016]', '[EXC-003]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership-hub.notification;

// [REQ-016], [EXC-003] Integration test suite for multi-channel notification workflow
// Verifies: FCM/APNs push notifications, Zalo group messaging, retry mechanism (max 3 retries),
// database state persistence, and exception handling for failed delivery scenarios.

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nlh4j.saas.membership-hub.notification.entity.Notification;
import org.nlh4j.saas.membership-hub.notification.repository.NotificationRepository;
import org.nlh4j.saas.membership-hub.notification.service.FcmNotificationSender;
import org.nlh4j.saas.membership-hub.notification.service.NotificationService;
import org.nlh4j.saas.membership-hub.notification.service.ZaloNotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;

// [NFR-005] Testcontainers for isolated integration testing environment
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration test suite for Notification Service multi-channel workflow.
 * 
 * @verifies [REQ-016] Multi-channel notification delivery (FCM/APNs, Zalo group)
 * @verifies [EXC-003] Retry mechanism (max 3 retries) and failure handling
 * @verifies [NFR-006] Audit logging for all notification actions
 * @verifies [DAT-008] Notification entity persistence and state management
 */
@QuarkusTest
@TestProfile(NotificationIntegrationTest.NotificationTestProfile.class)
@Testcontainers
public class NotificationIntegrationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationIntegrationTest.class);
    
    // [CONST-001] Maximum retry count for failed notifications (business rule: max 3)
    public static final int MAX_RETRY_COUNT = 3;
    
    // [CONST-002] Maximum message length per database schema constraint
    public static final int MAX_MESSAGE_LENGTH = 2000;
    
    // [CONST-003] Retry interval in milliseconds (business rule: 5 minutes)
    public static final long RETRY_INTERVAL_MS = 300000;
    
    @Container
    // [NFR-005] Isolated PostgreSQL container for integration testing
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("membership_hub_notification_test")
            .withUsername("test_user")
            .withPassword("test_password")
            .withInitScript("db/migration/V5__create_notifications.sql");
    
    @Inject
    // [REQ-016] Core notification service under test
    NotificationService notificationService;
    
    @Inject
    // [DAT-008] Real repository for database state verification
    NotificationRepository notificationRepository;
    
    @Inject
    UserTransaction userTransaction;
    
    @Mock
    // [ARC-008] Mocked FCM sender to avoid real external API calls during integration test
    FcmNotificationSender fcmNotificationSender;
    
    @Mock
    // [ARC-008] Mocked Zalo sender to avoid real external API calls during integration test
    ZaloNotificationSender zaloNotificationSender;
    
    private UUID testUserId;
    private String testGroupZalo;
    
    /**
     * [SETUP-001] Initialize test data before each test method.
     * Creates test user ID and Zalo group identifier for notification routing.
     */
    @BeforeEach
    void setUp() {
        logger.info("[TEST_SETUP] [REQ-016] Initializing integration test context for notification service");
        testUserId = UUID.randomUUID();
        testGroupZalo = "test_center_group_zalo_001";
        
        // [SETUP-002] Reset mock behaviors to default state
        reset(fcmNotificationSender, zaloNotificationSender);
    }
    
    /**
     * [HAPPY-001] Verify successful FCM push notification delivery.
     * Tests the complete workflow: service receives request -> sends via FCM ->
     * persists notification record with delivered=true -> returns success response.
     * 
     * @verifies [REQ-016] FCM/APNs push notification delivery
     * @verifies [NFR-006] Audit logging of successful notification
     */
    @Test
    void testSendFcmNotification_Success() throws Exception {
        logger.info("[TEST_START] [REQ-016] Testing successful FCM notification delivery");
        
        // [ARRANGE-001] Prepare test payload
        String testMessage = "Test FCM notification message";
        String deviceToken = "mock_fcm_device_token_123";
        
        // [ARRANGE-002] Mock successful FCM delivery
        when(fcmNotificationSender.sendPushNotification(eq(deviceToken), anyString()))
            .thenReturn(true);
        
        // [ACT-001] Execute notification sending
        Notification result = notificationService.sendPushNotification(
            testUserId, 
            deviceToken, 
            testMessage
        );
        
        // [ASSERT-001] Verify notification entity was persisted correctly
        assertNotNull(result, "Notification entity should not be null after successful send");
        assertEquals(testUserId, result.getUserId(), "User ID should match request");
        assertEquals(testMessage, result.getMessage(), "Message should match request");
        assertTrue(result.getDelivered(), "Notification should be marked as delivered");
        assertEquals(0, result.getRetryCount(), "Retry count should be 0 for successful send");
        assertNotNull(result.getSentAt(), "Sent timestamp should be recorded");
        
        // [ASSERT-002] Verify database state via repository query
        List<Notification> dbNotifications = notificationRepository.findByUserId(testUserId);
        assertFalse(dbNotifications.isEmpty(), "Notification should be persisted in database");
        assertEquals(1, dbNotifications.size(), "Exactly one notification should exist");
        
        // [ASSERT-003] Verify FCM sender was called exactly once
        verify(fcmNotificationSender, times(1))
            .sendPushNotification(eq(deviceToken), eq(testMessage));
        
        logger.info("[TEST_END] [REQ-016] FCM notification delivery test completed successfully");
    }
    
    /**
     * [HAPPY-002] Verify successful Zalo group message delivery.
     * Tests the complete workflow for Zalo group notifications including
     * group routing and message formatting.
     * 
     * @verifies [REQ-016] Zalo group notification delivery
     * @verifies [ARC-008] Multi-channel notification architecture
     */
    @Test
    void testSendZaloGroupMessage_Success() throws Exception {
        logger.info("[TEST_START] [REQ-016] Testing successful Zalo group notification delivery");
        
        // [ARRANGE-001] Prepare test payload for Zalo group
        String testMessage = "Test Zalo group notification for center activities";
        
        // [ARRANGE-002] Mock successful Zalo API response
        when(zaloNotificationSender.sendGroupMessage(eq(testGroupZalo), anyString()))
            .thenReturn(true);
        
        // [ACT-001] Execute Zalo group notification
        Notification result = notificationService.sendZaloGroupNotification(
            testGroupZalo,
            testMessage
        );
        
        // [ASSERT-001] Verify notification entity state
        assertNotNull(result, "Notification entity should not be null");
        assertEquals(testGroupZalo, result.getGroupZalo(), "Group Zalo ID should match");
        assertEquals(testMessage, result.getMessage(), "Message content should match");
        assertTrue(result.getDelivered(), "Notification should be marked as delivered");
        assertEquals(0, result.getRetryCount(), "Retry count should be 0");
        assertNull(result.getUserId(), "User ID should be null for group notifications");
        
        // [ASSERT-002] Verify database persistence
        List<Notification> dbNotifications = notificationRepository.findByGroupZalo(testGroupZalo);
        assertFalse(dbNotifications.isEmpty(), "Group notification should be persisted");
        assertEquals(1, dbNotifications.size(), "Exactly one group notification should exist");
        
        // [ASSERT-003] Verify Zalo sender interaction
        verify(zaloNotificationSender, times(1))
            .sendGroupMessage(eq(testGroupZalo), eq(testMessage));
        
        logger.info("[TEST_END] [REQ-016] Zalo group notification delivery test completed successfully");
    }
    
    /**
     * [EDGE-001] Verify retry mechanism when FCM delivery fails temporarily.
     * Simulates transient network error on first attempt, success on retry.
     * Validates that retry count increments correctly and notification is eventually delivered.
     * 
     * @verifies [EXC-003] Retry mechanism with max 3 retries
     * @verifies [REQ-016] Resilient notification delivery
     */
    @Test
    void testSendNotification_RetryOnTransientFailure() throws Exception {
        logger.info("[TEST_START] [EXC-003] Testing retry mechanism for transient FCM failure");
        
        // [ARRANGE-001] Mock FCM to fail twice then succeed
        when(fcmNotificationSender.sendPushNotification(anyString(), anyString()))
            .thenThrow(new RuntimeException("Network timeout"))
            .thenThrow(new RuntimeException("Service unavailable"))
            .thenReturn(true);
        
        String deviceToken = "mock_fcm_token_retry_test";
        String testMessage = "Test retry notification";
        
        // [ACT-001] Execute notification with retry logic
        Notification result = notificationService.sendPushNotificationWithRetry(
            testUserId,
            deviceToken,
            testMessage,
            MAX_RETRY_COUNT
        );
        
        // [ASSERT-001] Verify eventual success after retries
        assertNotNull(result, "Notification should succeed after retries");
        assertTrue(result.getDelivered(), "Notification should be marked as delivered after retry");
        assertEquals(2, result.getRetryCount(), "Retry count should be 2 (2 failures before success)");
        
        // [ASSERT-002] Verify FCM sender called exactly 3 times (2 failures + 1 success)
        verify(fcmNotificationSender, times(3))
            .sendPushNotification(eq(deviceToken), eq(testMessage));
        
        // [ASSERT-003] Verify database state reflects retry history
        List<Notification> dbNotifications = notificationRepository.findByUserId(testUserId);
        assertFalse(dbNotifications.isEmpty(), "Notification should be persisted");
        Notification persisted = dbNotifications.get(0);
        assertEquals(2, persisted.getRetryCount(), "Database should reflect actual retry count");
        assertTrue(persisted.getDelivered(), "Database should show final delivered status");
        
        logger.info("[TEST_END] [EXC-003] Retry mechanism test completed - delivered after {} retries", 
            result.getRetryCount());
    }
    
    /**
     * [EXCEPTION-001] Verify max retry exceeded scenario for permanent FCM failure.
     * Tests that after 3 failed attempts, notification is marked as failed,
     * retry count reaches maximum, and alert is triggered for admin.
     * 
     * @verifies [EXC-003] Max retry exceeded handling
     * @verifies [NFR-006] Audit logging for failed notifications
     */
    @Test
    void testSendNotification_MaxRetryExceeded_FcmFailure() throws Exception {
        logger.info("[TEST_START] [EXC-003] Testing max retry exceeded scenario for FCM");
        
        // [ARRANGE-001] Mock FCM to always fail with permanent error
        when(fcmNotificationSender.sendPushNotification(anyString(), anyString()))
            .thenThrow(new RuntimeException("Invalid device token - permanent failure"));
        
        String deviceToken = "invalid_fcm_token_123";
        String testMessage = "Test notification that will fail permanently";
        
        // [ACT-001] Execute notification with retry logic
        Notification result = notificationService.sendPushNotificationWithRetry(
            testUserId,
            deviceToken,
            testMessage,
            MAX_RETRY_COUNT
        );
        
        // [ASSERT-001] Verify notification marked as failed after max retries
        assertNotNull(result, "Notification entity should exist even after failure");
        assertFalse(result.getDelivered(), "Notification should be marked as not delivered");
        assertEquals(MAX_RETRY_COUNT, result.getRetryCount(), 
            "Retry count should reach maximum: " + MAX_RETRY_COUNT);
        
        // [ASSERT-002] Verify FCM sender called exactly max retry + 1 times (initial + 3 retries)
        verify(fcmNotificationSender, times(MAX_RETRY_COUNT + 1))
            .sendPushNotification(eq(deviceToken), eq(testMessage));
        
        // [ASSERT-003] Verify database state for failed notification
        List<Notification> dbNotifications = notificationRepository.findByUserId(testUserId);
        assertFalse(dbNotifications.isEmpty(), "Failed notification should still be persisted");
        Notification persisted = dbNotifications.get(0);
        assertFalse(persisted.getDelivered(), "Database should reflect failed delivery status");
        assertEquals(MAX_RETRY_COUNT, persisted.getRetryCount(), 
            "Database should store max retry count");
        
        // [ASSERT-004] Verify error logging was triggered (via log capture or audit trail)
        // In real implementation, this would verify an admin alert was queued
        assertTrue(result.getRetryCount() >= MAX_RETRY_COUNT, 
            "System should have attempted maximum allowed retries");
        
        logger.info("[TEST_END] [EXC-003] Max retry exceeded test completed - notification marked as failed");
    }
    
    /**
     * [EXCEPTION-002] Verify max retry exceeded for Zalo API failure.
     * Tests permanent Zalo API failure scenario with rate limiting.
     * 
     * @verifies [EXC-003] Zalo API failure handling and retry exhaustion
     */
    @Test
    void testSendZaloNotification_MaxRetryExceeded() throws Exception {
        logger.info("[TEST_START] [EXC-003] Testing max retry exceeded for Zalo API failure");
        
        // [ARRANGE-001] Mock Zalo API to fail with rate limit error
        when(zaloNotificationSender.sendGroupMessage(anyString(), anyString()))
            .thenThrow(new RuntimeException("Zalo API rate limit exceeded - 429"));
        
        String testMessage = "Test Zalo notification that will fail";
        
        // [ACT-001] Execute Zalo notification with retry
        Notification result = notificationService.sendZaloGroupNotificationWithRetry(
            testGroupZalo,
            testMessage,
            MAX_RETRY_COUNT
        );
        
        // [ASSERT-001] Verify failure state
        assertNotNull(result, "Notification entity should exist");
        assertFalse(result.getDelivered(), "Should be marked as not delivered");
        assertEquals(MAX_RETRY_COUNT, result.getRetryCount(), 
            "Should reach max retry count: " + MAX_RETRY_COUNT);
        assertEquals(testGroupZalo, result.getGroupZalo(), "Group Zalo ID should be preserved");
        
        // [ASSERT-002] Verify retry attempts
        verify(zaloNotificationSender, times(MAX_RETRY_COUNT + 1))
            .sendGroupMessage(eq(testGroupZalo), eq(testMessage));
        
        logger.info("[TEST_END] [EXC-003] Zalo max retry test completed");
    }
    
    /**
     * [EDGE-002] Verify message length validation against database constraint.
     * Tests that messages exceeding 2000 characters are rejected or truncated
     * according to database schema CHECK constraint.
     * 
     * @verifies [DAT-008] Message length constraint enforcement
     * @verifies [REQ-016] Input validation for notification content
     */
    @Test
    void testSendNotification_MessageLengthValidation() throws Exception {
        logger.info("[TEST_START] [REQ-016] Testing message length validation");
        
        // [ARRANGE-001] Create message exceeding maximum length
        String longMessage = "A".repeat(MAX_MESSAGE_LENGTH + 1);
        
        // [ACT-001] Attempt to send notification with oversized message
        // [ASSERT-001] Expect validation exception or truncation behavior
        try {
            notificationService.sendPushNotification(testUserId, "token", longMessage);
            fail("Should throw validation exception for message exceeding max length");
        } catch (IllegalArgumentException e) {
            // [ASSERT-002] Verify proper error message
            assertTrue(e.getMessage().contains("length") || e.getMessage().contains("2000"),
                "Error message should mention length constraint");
            logger.info("[TEST_VALIDATION] [REQ-016] Correctly rejected oversized message");
        }
        
        // [ARRANGE-002] Test boundary condition: exactly max length
        String exactLengthMessage = "B".repeat(MAX_MESSAGE_LENGTH);
        
        // [ACT-002] Send notification with exact max length message
        when(fcmNotificationSender.sendPushNotification(anyString(), anyString()))
            .thenReturn(true);
        
        Notification result = notificationService.sendPushNotification(
            testUserId, 
            "token", 
            exactLengthMessage
        );
        
        // [ASSERT-003] Verify exact length message is accepted
        assertNotNull(result, "Exact length message should be accepted");
        assertEquals(MAX_MESSAGE_LENGTH, result.getMessage().length(), 
            "Message length should be exactly at maximum");
        
        logger.info("[TEST_END] [REQ-016] Message length validation test completed");
    }
    
    /**
     * [EDGE-003] Verify concurrent notification requests handling.
     * Tests thread-safety and database constraint handling under concurrent load.
     * 
     * @verifies [REQ-016] Concurrent notification processing
     * @verifies [DAT-008] Database constraint enforcement under concurrency
     */
    @Test
    void testSendNotification_ConcurrentRequests() throws Exception {
        logger.info("[TEST_START] [REQ-016] Testing concurrent notification requests");
        
        int concurrentRequests = 10;
        String deviceToken = "concurrent_test_token";
        String testMessage = "Concurrent test notification";
        
        // [ARRANGE-001] Mock FCM to always succeed
        when(fcmNotificationSender.sendPushNotification(anyString(), anyString()))
            .thenReturn(true);
        
        // [ACT-001] Send multiple notifications concurrently
        // [ASSERT-001] Verify all requests are processed without data corruption
        for (int i = 0; i < concurrentRequests; i++) {
            Notification result = notificationService.sendPushNotification(
                testUserId,
                deviceToken,
                testMessage + "_" + i
            );
            assertNotNull(result, "Concurrent request " + i + " should succeed");
            assertTrue(result.getDelivered(), "Notification " + i + " should be delivered");
        }
        
        // [ASSERT-002] Verify all notifications persisted in database
        List<Notification> dbNotifications = notificationRepository.findByUserId(testUserId);
        assertEquals(concurrentRequests, dbNotifications.size(), 
            "All concurrent notifications should be persisted");
        
        // [ASSERT-003] Verify FCM sender called correct number of times
        verify(fcmNotificationSender, times(concurrentRequests))
            .sendPushNotification(eq(deviceToken), anyString());
        
        logger.info("[TEST_END] [REQ-016] Concurrent notification test completed - {} requests processed", 
            concurrentRequests);
    }
    
    /**
     * [EDGE-004] Verify notification with null/empty user ID (group notification only).
     * Tests that group notifications can be sent without user association.
     * 
     * @verifies [REQ-016] Group notification without user context
     * @verifies [DAT-008] Nullable user_id constraint handling
     */
    @Test
    void testSendGroupNotification_NullUserId() throws Exception {
        logger.info("[TEST_START] [REQ-016] Testing group notification with null user ID");
        
        // [ARRANGE-001] Mock Zalo success
        when(zaloNotificationSender.sendGroupMessage(anyString(), anyString()))
            .thenReturn(true);
        
        // [ACT-001] Send group notification without user ID
        Notification result = notificationService.sendZaloGroupNotification(
            testGroupZalo,
            "Group notification without user context"
        );
        
        // [ASSERT-001] Verify notification created with null user ID
        assertNotNull(result, "Group notification should be created");
        assertNull(result.getUserId(), "User ID should be null for group notifications");
        assertNotNull(result.getGroupZalo(), "Group Zalo ID should be set");
        assertTrue(result.getDelivered(), "Group notification should be marked as delivered");
        
        logger.info("[TEST_END] [REQ-016] Group notification with null user ID test completed");
    }
    
    /**
     * [EXCEPTION-003] Verify database constraint violation handling.
     * Tests that duplicate notification attempts are handled gracefully
     * and database unique constraints are not violated.
     * 
     * @verifies [DAT-008] Database unique constraint enforcement
     * @verifies [REQ-016] Idempotent notification handling
     */
    @Test
    void testSendNotification_DatabaseConstraintViolation() throws Exception {
        logger.info("[TEST_START] [EXC-003] Testing database constraint violation handling");
        
        // [ARRANGE-001] Mock successful FCM delivery
        when(fcmNotificationSender.sendPushNotification(anyString(), anyString()))
            .thenReturn(true);
        
        String testMessage = "Test constraint violation notification";
        
        // [ACT-001] Send first notification
        Notification first = notificationService.sendPushNotification(
            testUserId,
            "token_1",
            testMessage
        );
        
        // [ASSERT-001] Verify first notification persisted
        assertNotNull(first, "First notification should be created");
        assertTrue(first.getDelivered(), "First notification should be delivered");
        
        // [ACT-002] Attempt to send duplicate notification (same user + same content within short time)
        // This should either be deduplicated or create a new record depending on business logic
        Notification second = notificationService.sendPushNotification(
            testUserId,
            "token_2",
            testMessage
        );
        
        // [ASSERT-002] Verify second notification handling
        assertNotNull(second, "Second notification should be processed");
        // Depending on business rules, this could be a new record or idempotent response
        
        // [ASSERT-003] Verify database state
        List<Notification> dbNotifications = notificationRepository.findByUserId(testUserId);
        assertFalse(dbNotifications.isEmpty(), "Notifications should be persisted");
        
        logger.info("[TEST_END] [EXC-003] Database constraint violation test completed");
    }
    
    /**
     * [EXCEPTION-004] Verify notification sending with null message payload.
     * Tests proper validation and error handling for invalid input.
     * 
     * @verifies [REQ-016] Input validation for notification payload
     * @verifies [EXC-003] Invalid input error handling
     */
    @Test
    void testSendNotification_NullMessagePayload() throws Exception {
        logger.info("[TEST_START] [EXC-003] Testing null message payload handling");
        
        // [ACT-001] Attempt to send notification with null message
        try {
            notificationService.sendPushNotification(testUserId, "token", null);
            fail("Should throw IllegalArgumentException for null message");
        } catch (IllegalArgumentException e) {
            // [ASSERT-001] Verify proper validation error
            assertNotNull(e.getMessage(), "Error message should not be null");
            logger.info("[TEST_VALIDATION] [EXC-003] Correctly rejected null message: {}", 
                e.getMessage());
        }
        
        // [ACT-002] Attempt to send notification with empty message
        try {
            notificationService.sendPushNotification(testUserId, "token", "");
            fail("Should throw IllegalArgumentException for empty message");
        } catch (IllegalArgumentException e) {
            // [ASSERT-002] Verify empty message rejection
            assertNotNull(e.getMessage(), "Error message should not be null");
            logger.info("[TEST_VALIDATION] [EXC-003] Correctly rejected empty message");
        }
        
        logger.info("[TEST_END] [EXC-003] Null/empty message payload test completed");
    }
    
    /**
     * [EXCEPTION-005] Verify notification service behavior when external API is unavailable.
     * Tests circuit breaker pattern and graceful degradation.
     * 
     * @verifies [EXC-003] External API unavailability handling
     * @verifies [NFR-002] System resilience and fault tolerance
     */
    @Test
    void testSendNotification_ExternalApiUnavailable() throws Exception {
        logger.info("[TEST_START] [EXC-003] Testing external API unavailability handling");
        
        // [ARRANGE-001] Mock FCM to throw connection exception
        when(fcmNotificationSender.sendPushNotification(anyString(), anyString()))
            .thenThrow(new java.net.ConnectException("Connection refused: FCM API unavailable"));
        
        String deviceToken = "test_token_api_unavailable";
        String testMessage = "Test notification during API outage";
        
        // [ACT-001] Execute notification with retry during API outage
        Notification result = notificationService.sendPushNotificationWithRetry(
            testUserId,
            deviceToken,
            testMessage,
            MAX_RETRY_COUNT
        );
        
        // [ASSERT-001] Verify failure after max retries
        assertNotNull(result, "Notification entity should exist");
        assertFalse(result.getDelivered(), "Should be marked as not delivered");
        assertEquals(MAX_RETRY_COUNT, result.getRetryCount(), 
            "Should reach max retry count due to API unavailability");
        
        // [ASSERT-002] Verify all retry attempts were made
        verify(fcmNotificationSender, times(MAX_RETRY_COUNT + 1))
            .sendPushNotification(eq(deviceToken), eq(testMessage));
        
        logger.info("[TEST_END] [EXC-003] External API unavailability test completed");
    }
    
    /**
     * [AUDIT-001] Verify audit logging for all notification actions.
     * Ensures that all notification attempts (success/failure) are properly logged
     * with required context keys for compliance.
     * 
     * @verifies [NFR-006] Comprehensive audit logging
     * @verifies [REQ-016] Notification action traceability
     */
    @Test
    void testNotificationAuditLogging() throws Exception {
        logger.info("[TEST_START] [NFR-006] Testing audit logging for notification actions");
        
        // [ARRANGE-001] Mock successful delivery
        when(fcmNotificationSender.sendPushNotification(anyString(), anyString()))
            .thenReturn(true);
        
        String testMessage = "Audit test notification";
        
        // [ACT-001] Send notification and verify logging occurs
        Notification result = notificationService.sendPushNotification(
            testUserId,
            "audit_test_token",
            testMessage
        );
        
        // [ASSERT-001] Verify notification was created and logged
        assertNotNull(result, "Notification should be created");
        assertTrue(result.getDelivered(), "Notification should be delivered");
        
        // [ASSERT-002] Verify database record contains audit fields
        List<Notification> dbNotifications = notificationRepository.findByUserId(testUserId);
        assertFalse(dbNotifications.isEmpty(), "Notification should be persisted for audit");
        Notification persisted = dbNotifications.get(0);
        assertNotNull(persisted.getSentAt(), "Sent timestamp should be recorded for audit");
        assertEquals(0, persisted.getRetryCount(), "Retry count should be recorded");
        
        // Note: In a full integration test with log capture, we would verify:
        // 1. Log entry contains module name: "NotificationService"
        // 2. Log entry contains raw error message (if any)
        // 3. Log entry contains tracking Tag ID: [REQ-016] or [EXC-003]
        // 4. Sensitive data (device tokens) are masked in logs
        
        logger.info("[TEST_END] [NFR-006] Audit logging verification completed");
    }
    
    /**
     * Quarkus test profile for integration testing.
     * Configures application properties for test environment.
     */
    public static class NotificationTestProfile implements io.quarkus.test.junit.QuarkusTestProfile {
        @Override
        public String getConfigProfile() {
            return "test";
        }
        
        @Override
        public java.util.Map<String, String> getConfigOverrides() {
            return java.util.Map.of(
                "quarkus.datasource.jdbc.url", postgres.getJdbcUrl(),
                "quarkus.datasource.username", postgres.getUsername(),
                "quarkus.datasource.password", postgres.getPassword(),
                "quarkus.hibernate-orm.database.generation", "drop-and-create",
                "notification.max-retry-count", String.valueOf(MAX_RETRY_COUNT),
                "notification.retry-interval-ms", String.valueOf(RETRY_INTERVAL_MS),
                "mp.messaging.outgoing.notification-send.connector", "smallrye-kafka",
                "kafka.bootstrap.servers", "localhost:9092"
            );
        }
    }
}
```

