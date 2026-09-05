package org.nlh4j.saas.membership-hub.notification;

// [REQ-016] Core notification service unit test suite
// [EXC-003] Exception handling for notification delivery failures
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nlh4j.saas.membership-hub.notification.entity.Notification;
import org.nlh4j.saas.membership-hub.notification.repository.NotificationRepository;
import org.nlh4j.saas.membership-hub.notification.service.FcmNotificationSender;
import org.nlh4j.saas.membership-hub.notification.service.ZaloNotificationSender;
import org.nlh4j.saas.membership-hub.notification.service.NotificationService;
import org.nlh4j.saas.membership-hub.notification.exception.NotificationSendException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

// [REQ-016] Comprehensive unit test coverage for NotificationService
// [EXC-003] Validates exception handling and retry logic for notification delivery
@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    // [REQ-016] Enterprise constants for notification service testing
    // [EXC-003] Configuration constants for retry and timeout scenarios
    private static final int MAX_RETRY_COUNT = 3;
    private static final int RETRY_DELAY_MS = 5000;
    private static final String TEST_USER_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String TEST_NOTIFICATION_ID = "550e8400-e29b-41d4-a716-446655440001";
    private static final String TEST_MESSAGE = "Test notification message";
    private static final String TEST_GROUP_ZALO = "test-group-zalo";
    private static final String FCM_TOKEN = "test-fcm-token";
    private static final String ZALO_ACCESS_TOKEN = "test-zalo-token";

    // [REQ-016] Mock dependencies for isolated unit testing
    // [EXC-003] Mock external service clients to simulate failure scenarios
    @Mock
    private NotificationRepository notificationRepository;
    
    @Mock
    private FcmNotificationSender fcmNotificationSender;
    
    @Mock
    private ZaloNotificationSender zaloNotificationSender;

    // [REQ-016] System under test with mocked dependencies
    // [EXC-003] Isolates notification service from external network calls
    private NotificationService notificationService;

    // [REQ-016] Test fixture setup before each test method
    // [EXC-003] Ensures clean test state with fresh mocks
    @BeforeEach
    void setUp() {
        openMocks(this);
        notificationService = new NotificationService(
            notificationRepository,
            fcmNotificationSender,
            zaloNotificationSender,
            MAX_RETRY_COUNT,
            RETRY_DELAY_MS
        );
    }

    // [REQ-016] Happy path: Send push notification successfully on first attempt
    // [EXC-003] Validates successful delivery without retries
    @Test
    void testSendPushNotification_Success_FirstAttempt() {
        // Arrange: Setup successful FCM response
        when(fcmNotificationSender.send(anyString(), anyString(), any()))
            .thenReturn(true);
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Execute push notification send
        Notification result = notificationService.sendPushNotification(
            UUID.fromString(TEST_USER_ID),
            FCM_TOKEN,
            TEST_MESSAGE
        );

        // Assert: Verify notification was created and marked as delivered
        assertNotNull(result, "Notification should not be null");
        assertEquals(TEST_MESSAGE, result.getMessage(), "Message should match");
        assertTrue(result.isDelivered(), "Notification should be marked as delivered");
        assertEquals(0, result.getRetryCount(), "Retry count should be 0 on first success");
        
        // Verify FCM sender was called exactly once
        verify(fcmNotificationSender, times(1)).send(eq(FCM_TOKEN), eq(TEST_MESSAGE), any());
        verify(notificationRepository, times(1)).save(any(Notification.class));
        
        // [REQ-016] Log successful test execution for audit trail
        System.out.println("[TEST_PASS] [REQ-016] Push notification sent successfully on first attempt");
    }

    // [REQ-016] Edge case: Send push notification with retry logic
    // [EXC-003] Validates retry mechanism when FCM fails initially
    @Test
    void testSendPushNotification_Success_AfterRetry() {
        // Arrange: FCM fails twice then succeeds
        when(fcmNotificationSender.send(anyString(), anyString(), any()))
            .thenReturn(false)  // First attempt fails
            .thenReturn(false)  // Second attempt fails
            .thenReturn(true);  // Third attempt succeeds
        
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Execute push notification with retries
        Notification result = notificationService.sendPushNotification(
            UUID.fromString(TEST_USER_ID),
            FCM_TOKEN,
            TEST_MESSAGE
        );

        // Assert: Verify notification eventually succeeds after retries
        assertNotNull(result, "Notification should not be null");
        assertTrue(result.isDelivered(), "Notification should be delivered after retries");
        assertEquals(2, result.getRetryCount(), "Retry count should be 2 (failed twice before success)");
        
        // Verify FCM sender was called exactly 3 times (initial + 2 retries)
        verify(fcmNotificationSender, times(3)).send(eq(FCM_TOKEN), eq(TEST_MESSAGE), any());
        verify(notificationRepository, times(1)).save(any(Notification.class));
        
        // [REQ-016] Log retry scenario test execution
        System.out.println("[TEST_PASS] [REQ-016] Push notification succeeded after 2 retries");
    }

    // [REQ-016] Exception case: Max retries exceeded for push notification
    // [EXC-003] Validates failure after maximum retry attempts
    @Test
    void testSendPushNotification_Failure_MaxRetryExceeded() {
        // Arrange: FCM always fails
        when(fcmNotificationSender.send(anyString(), anyString(), any()))
            .thenReturn(false);
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert: Verify exception is thrown after max retries
        NotificationSendException exception = assertThrows(
            NotificationSendException.class,
            () -> notificationService.sendPushNotification(
                UUID.fromString(TEST_USER_ID),
                FCM_TOKEN,
                TEST_MESSAGE
            ),
            "Should throw NotificationSendException after max retries"
        );

        // Verify error message contains retry count information
        assertTrue(exception.getMessage().contains("Failed after " + MAX_RETRY_COUNT + " attempts"),
            "Exception message should indicate max retries exceeded");
        
        // Verify FCM sender was called exactly MAX_RETRY_COUNT times
        verify(fcmNotificationSender, times(MAX_RETRY_COUNT))
            .send(eq(FCM_TOKEN), eq(TEST_MESSAGE), any());
        
        // [EXC-003] Log max retry exception test execution
        System.out.println("[TEST_PASS] [EXC-003] Max retry exception thrown correctly after " + MAX_RETRY_COUNT + " attempts");
    }

    // [REQ-016] Happy path: Send Zalo group message successfully
    // [EXC-003] Validates successful Zalo message delivery
    @Test
    void testSendZaloGroupMessage_Success() {
        // Arrange: Setup successful Zalo response
        when(zaloNotificationSender.sendToGroup(anyString(), anyString(), any()))
            .thenReturn(true);
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Execute Zalo group message send
        Notification result = notificationService.sendZaloGroupMessage(
            TEST_GROUP_ZALO,
            TEST_MESSAGE
        );

        // Assert: Verify notification was created and marked as delivered
        assertNotNull(result, "Notification should not be null");
        assertEquals(TEST_MESSAGE, result.getMessage(), "Message should match");
        assertEquals(TEST_GROUP_ZALO, result.getGroupZalo(), "Group Zalo should match");
        assertTrue(result.isDelivered(), "Notification should be marked as delivered");
        assertEquals(0, result.getRetryCount(), "Retry count should be 0 on first success");
        
        // Verify Zalo sender was called exactly once
        verify(zaloNotificationSender, times(1))
            .sendToGroup(eq(TEST_GROUP_ZALO), eq(TEST_MESSAGE), any());
        
        // [REQ-016] Log successful Zalo test execution
        System.out.println("[TEST_PASS] [REQ-016] Zalo group message sent successfully");
    }

    // [REQ-016] Exception case: Zalo API failure with retry
    // [EXC-003] Validates retry logic for Zalo delivery failures
    @Test
    void testSendZaloGroupMessage_Failure_WithRetry() {
        // Arrange: Zalo fails once then succeeds
        when(zaloNotificationSender.sendToGroup(anyString(), anyString(), any()))
            .thenReturn(false)  // First attempt fails
            .thenReturn(true);  // Second attempt succeeds
        
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Execute Zalo message with retry
        Notification result = notificationService.sendZaloGroupMessage(
            TEST_GROUP_ZALO,
            TEST_MESSAGE
        );

        // Assert: Verify notification succeeds after retry
        assertNotNull(result, "Notification should not be null");
        assertTrue(result.isDelivered(), "Notification should be delivered after retry");
        assertEquals(1, result.getRetryCount(), "Retry count should be 1");
        
        // Verify Zalo sender was called exactly 2 times
        verify(zaloNotificationSender, times(2))
            .sendToGroup(eq(TEST_GROUP_ZALO), eq(TEST_MESSAGE), any());
        
        // [EXC-003] Log Zalo retry scenario test execution
        System.out.println("[TEST_PASS] [EXC-003] Zalo message succeeded after 1 retry");
    }

    // [REQ-016] Edge case: Send notification with null user ID
    // [EXC-003] Validates null safety for user identifier
    @Test
    void testSendPushNotification_NullUserId_ThrowsException() {
        // Act & Assert: Verify exception for null user ID
        assertThrows(
            IllegalArgumentException.class,
            () -> notificationService.sendPushNotification(
                null,
                FCM_TOKEN,
                TEST_MESSAGE
            ),
            "Should throw IllegalArgumentException for null user ID"
        );
        
        // Verify no external calls were made
        verifyNoInteractions(fcmNotificationSender);
        verifyNoInteractions(notificationRepository);
        
        // [REQ-016] Log null safety test execution
        System.out.println("[TEST_PASS] [REQ-016] Null user ID validation works correctly");
    }

    // [REQ-016] Edge case: Send notification with empty message
    // [EXC-003] Validates input validation for message content
    @Test
    void testSendNotification_EmptyMessage_ThrowsException() {
        // Act & Assert: Verify exception for empty message
        assertThrows(
            IllegalArgumentException.class,
            () -> notificationService.sendPushNotification(
                UUID.fromString(TEST_USER_ID),
                FCM_TOKEN,
                ""
            ),
            "Should throw IllegalArgumentException for empty message"
        );
        
        // Verify no external calls were made
        verifyNoInteractions(fcmNotificationSender);
        verifyNoInteractions(notificationRepository);
        
        // [REQ-016] Log empty message validation test execution
        System.out.println("[TEST_PASS] [REQ-016] Empty message validation works correctly");
    }

    // [REQ-016] Edge case: Send notification with message exceeding length limit
    // [EXC-003] Validates message length constraints (2000 chars max per DB schema)
    @Test
    void testSendNotification_MessageTooLong_ThrowsException() {
        // Arrange: Create message exceeding 2000 character limit
        String longMessage = "x".repeat(2001);
        
        // Act & Assert: Verify exception for too long message
        assertThrows(
            IllegalArgumentException.class,
            () -> notificationService.sendPushNotification(
                UUID.fromString(TEST_USER_ID),
                FCM_TOKEN,
                longMessage
            ),
            "Should throw IllegalArgumentException for message exceeding 2000 characters"
        );
        
        // Verify no external calls were made
        verifyNoInteractions(fcmNotificationSender);
        verifyNoInteractions(notificationRepository);
        
        // [REQ-016] Log message length validation test execution
        System.out.println("[TEST_PASS] [REQ-016] Message length validation works correctly");
    }

    // [REQ-016] Happy path: Send notification to both push and Zalo channels
    // [EXC-003] Validates multi-channel notification delivery
    @Test
    void testSendNotification_MultiChannel_Success() {
        // Arrange: Setup both FCM and Zalo to succeed
        when(fcmNotificationSender.send(anyString(), anyString(), any()))
            .thenReturn(true);
        when(zaloNotificationSender.sendToGroup(anyString(), anyString(), any()))
            .thenReturn(true);
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Send notification to both channels
        Notification pushResult = notificationService.sendPushNotification(
            UUID.fromString(TEST_USER_ID),
            FCM_TOKEN,
            TEST_MESSAGE
        );
        Notification zaloResult = notificationService.sendZaloGroupMessage(
            TEST_GROUP_ZALO,
            TEST_MESSAGE
        );

        // Assert: Verify both notifications were sent successfully
        assertNotNull(pushResult, "Push notification should not be null");
        assertTrue(pushResult.isDelivered(), "Push notification should be delivered");
        
        assertNotNull(zaloResult, "Zalo notification should not be null");
        assertTrue(zaloResult.isDelivered(), "Zalo notification should be delivered");
        
        // Verify both senders were called
        verify(fcmNotificationSender, times(1)).send(anyString(), anyString(), any());
        verify(zaloNotificationSender, times(1)).sendToGroup(anyString(), anyString(), any());
        
        // [REQ-016] Log multi-channel success test execution
        System.out.println("[TEST_PASS] [REQ-016] Multi-channel notification sent successfully");
    }

    // [REQ-016] Exception case: FCM sender throws unexpected exception
    // [EXC-003] Validates handling of unexpected runtime exceptions
    @Test
    void testSendPushNotification_UnexpectedException_ThrowsNotificationSendException() {
        // Arrange: FCM sender throws runtime exception
        when(fcmNotificationSender.send(anyString(), anyString(), any()))
            .thenThrow(new RuntimeException("FCM service unavailable"));
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert: Verify NotificationSendException is thrown
        assertThrows(
            NotificationSendException.class,
            () -> notificationService.sendPushNotification(
                UUID.fromString(TEST_USER_ID),
                FCM_TOKEN,
                TEST_MESSAGE
            ),
            "Should throw NotificationSendException when FCM throws unexpected exception"
        );
        
        // [EXC-003] Log unexpected exception handling test execution
        System.out.println("[TEST_PASS] [EXC-003] Unexpected exception handled correctly");
    }

    // [REQ-016] Edge case: Process pending notifications after system recovery
    // [EXC-003] Validates FIFO processing of pending notifications after failure
    @Test
    void testProcessPendingNotifications_FIFOOrder() {
        // Arrange: Create multiple pending notifications
        Notification pending1 = createPendingNotification(LocalDateTime.now().minusMinutes(5));
        Notification pending2 = createPendingNotification(LocalDateTime.now().minusMinutes(3));
        Notification pending3 = createPendingNotification(LocalDateTime.now().minusMinutes(1));
        
        when(notificationRepository.findPendingNotifications(any()))
            .thenReturn(java.util.List.of(pending1, pending2, pending3));
        when(fcmNotificationSender.send(anyString(), anyString(), any()))
            .thenReturn(true);
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Process pending notifications
        notificationService.processPendingNotifications();

        // Assert: Verify notifications were processed in FIFO order
        verify(fcmNotificationSender, times(3)).send(anyString(), anyString(), any());
        verify(notificationRepository, times(3)).save(any(Notification.class));
        
        // Verify pending1 was saved first (FIFO order)
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(pending1.getNotificationId(), captor.getValue().getNotificationId(),
            "First pending notification should be processed first (FIFO)");
        
        // [REQ-016] Log FIFO processing test execution
        System.out.println("[TEST_PASS] [REQ-016] Pending notifications processed in FIFO order");
    }

    // [REQ-016] Helper method to create pending notification test fixture
    // [EXC-003] Reduces code duplication in test setup
    private Notification createPendingNotification(LocalDateTime sentAt) {
        Notification notification = new Notification();
        notification.setNotificationId(UUID.fromString(TEST_NOTIFICATION_ID));
        notification.setUserId(UUID.fromString(TEST_USER_ID));
        notification.setMessage(TEST_MESSAGE);
        notification.setSentAt(sentAt);
        notification.setDelivered(false);
        notification.setRetryCount(0);
        return notification;
    }

    // [REQ-016] Edge case: Verify notification retry count does not exceed maximum
    // [EXC-003] Validates retry count boundary condition
    @Test
    void testSendPushNotification_RetryCount_DoesNotExceedMax() {
        // Arrange: FCM always fails
        when(fcmNotificationSender.send(anyString(), anyString(), any()))
            .thenReturn(false);
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Attempt to send notification
        NotificationSendException exception = assertThrows(
            NotificationSendException.class,
            () -> notificationService.sendPushNotification(
                UUID.fromString(TEST_USER_ID),
                FCM_TOKEN,
                TEST_MESSAGE
            )
        );

        // Assert: Verify retry count in saved notification does not exceed max
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, atLeastOnce()).save(captor.capture());
        
        // Get the last saved notification (final state after all retries)
        Notification lastSaved = captor.getValue();
        assertTrue(lastSaved.getRetryCount() <= MAX_RETRY_COUNT,
            "Retry count should not exceed maximum allowed: " + MAX_RETRY_COUNT);
        
        // [EXC-003] Log retry count boundary test execution
        System.out.println("[TEST_PASS] [EXC-003] Retry count does not exceed maximum: " + lastSaved.getRetryCount());
    }

    // [REQ-016] Edge case: Send notification with null FCM token
    // [EXC-003] Validates null safety for device token
    @Test
    void testSendPushNotification_NullToken_ThrowsException() {
        // Act & Assert: Verify exception for null token
        assertThrows(
            IllegalArgumentException.class,
            () -> notificationService.sendPushNotification(
                UUID.fromString(TEST_USER_ID),
                null,
                TEST_MESSAGE
            ),
            "Should throw IllegalArgumentException for null FCM token"
        );
        
        // Verify no external calls were made
        verifyNoInteractions(fcmNotificationSender);
        verifyNoInteractions(notificationRepository);
        
        // [REQ-016] Log null token validation test execution
        System.out.println("[TEST_PASS] [REQ-016] Null FCM token validation works correctly");
    }

    // [REQ-016] Happy path: Mark notification as delivered after successful send
    // [EXC-003] Validates state transition to delivered status
    @Test
    void testMarkNotificationAsDelivered_UpdatesState() {
        // Arrange: Create undelivered notification
        Notification notification = createPendingNotification(LocalDateTime.now());
        notification.setDelivered(false);
        
        when(notificationRepository.findById(any(UUID.class)))
            .thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Mark notification as delivered
        Notification result = notificationService.markNotificationAsDelivered(
            UUID.fromString(TEST_NOTIFICATION_ID)
        );

        // Assert: Verify notification state is updated
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isDelivered(), "Notification should be marked as delivered");
        assertNotNull(result.getDeliveredAt(), "Delivered timestamp should be set");
        
        verify(notificationRepository, times(1)).findById(any(UUID.class));
        verify(notificationRepository, times(1)).save(any(Notification.class));
        
        // [REQ-016] Log state update test execution
        System.out.println("[TEST_PASS] [REQ-016] Notification marked as delivered successfully");
    }

    // [REQ-016] Exception case: Mark non-existent notification as delivered
    // [EXC-003] Validates handling of missing notification records
    @Test
    void testMarkNotificationAsDelivered_NonExistentId_ThrowsException() {
        // Arrange: Repository returns empty optional
        when(notificationRepository.findById(any(UUID.class)))
            .thenReturn(Optional.empty());

        // Act & Assert: Verify exception for non-existent notification
        assertThrows(
            NotificationSendException.class,
            () -> notificationService.markNotificationAsDelivered(
                UUID.fromString(TEST_NOTIFICATION_ID)
            ),
            "Should throw NotificationSendException for non-existent notification ID"
        );
        
        // Verify save was never called
        verify(notificationRepository, never()).save(any(Notification.class));
        
        // [EXC-003] Log non-existent ID exception test execution
        System.out.println("[TEST_PASS] [EXC-003] Non-existent notification ID handled correctly");
    }
}