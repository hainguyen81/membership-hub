# Day 2: model kilo-auto/free - API Endpoint https://api.kilo.ai/api/gateway
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/membership-service/src/test/java/com/hub/membership/MembershipRenewalTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Test Component Destination Path: `./sources/backend/membership-service/src/test/java/com/hub/membership/MembershipRenewalTest.java` (Must map to sources/backend/ or sources/frontend/)


### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/membership-service/src/test/java/com/hub/membership/MembershipRenewalTest.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Viết bộ kiểm thử đơn vị cho chức năng gia hạn thẻ']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` structure. Ensure that you read the exact Tag IDs from the `['[REQ-015]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership-hub.membership.service;

// [REQ-015] Membership renewal functionality unit test suite
// Enterprise test suite for membership card renewal with payment integration
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nlh4j.saas.membership-hub.membership.entity.MembershipCard;
import org.nlh4j.saas.membership-hub.membership.entity.StudentCard;
import org.nlh4j.saas.membership-hub.membership.exception.MembershipException;
import org.nlh4j.saas.membership-hub.membership.repository.MembershipCardRepository;
import org.nlh4j.saas.membership-hub.membership.repository.StudentCardRepository;
import org.nlh4j.saas.membership-hub.membership.service.MembershipService;
import org.nlh4j.saas.membership-hub.membership.service.PaymentGatewayService;
import org.nlh4j.saas.membership-hub.membership.service.NotificationService;
import org.nlh4j.saas.membership-hub.user.entity.User;
import org.nlh4j.saas.membership-hub.user.repository.UserRepository;
import org.nlh4j.saas.membership-hub.util.DateUtil;
import org.nlh4j.saas.membership-hub.util.ValidationUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// [REQ-015] Unit test suite for membership card renewal functionality
// [NFR-003] Test coverage requirement >= 85%
// [EXC-003] Exception handling validation for notification failures
@ExtendWith(MockitoExtension.class)
class MembershipRenewalTest {

    // [CONST-001] Test constants following enterprise anti-magic-numbers policy
    private static final int DEFAULT_VALIDITY_DAYS = 30;
    private static final int MAX_RENEWAL_DAYS = 365;
    private static final BigDecimal RENEWAL_FEE = new BigDecimal("100.00");
    private static final UUID TEST_USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID TEST_CARD_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    private static final String VALID_PAYMENT_TRANSACTION_ID = "TXN-" + System.currentTimeMillis();
    private static final String INVALID_PAYMENT_TRANSACTION_ID = "INVALID-TXN-001";

    // [MOCK-001] Mock dependencies following enterprise isolation rules
    @Mock
    private MembershipCardRepository membershipCardRepository;
    
    @Mock
    private StudentCardRepository studentCardRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PaymentGatewayService paymentGatewayService;
    
    @Mock
    private NotificationService notificationService;
    
    @Mock
    private DateUtil dateUtil;
    
    @Mock
    private ValidationUtil validationUtil;

    // [SUT-001] System under test with dependency injection
    @InjectMocks
    private MembershipService membershipService;

    private MembershipCard testMembershipCard;
    private StudentCard testStudentCard;
    private User testUser;

    // [SETUP-001] Test data initialization following enterprise test patterns
    @BeforeEach
    void setUp() {
        // Initialize test user
        testUser = new User();
        testUser.setUserId(TEST_USER_ID);
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setRoleId((short) 5); // Student role

        // Initialize test membership card (active)
        testMembershipCard = new MembershipCard();
        testMembershipCard.setCardId(TEST_CARD_ID);
        testMembershipCard.setUserId(TEST_USER_ID);
        testMembershipCard.setIssueDate(LocalDate.now().minusDays(10));
        testMembershipCard.setValidityDays(DEFAULT_VALIDITY_DAYS);
        testMembershipCard.setRemainingDays(20);
        testMembershipCard.setExpiryDate(LocalDate.now().plusDays(20));
        testMembershipCard.setStatus("ACTIVE");

        // Initialize test student card
        testStudentCard = new StudentCard();
        testStudentCard.setCardId(TEST_CARD_ID);
        testStudentCard.setStudentId(TEST_USER_ID);
        testStudentCard.setIssueDate(LocalDate.now().minusDays(10));
        testStudentCard.setValidityDays(DEFAULT_VALIDITY_DAYS);
        testStudentCard.setRemainingDays(20);
    }

    // [TEST-001] Happy path: Successful membership renewal with valid payment
    // [REQ-015] Verify membership renewal with payment integration
    @Test
    void renewMembership_Success_WithValidPayment() {
        // [ARRANGE] Setup test data and mocks
        when(membershipCardRepository.findByUserId(TEST_USER_ID))
            .thenReturn(Optional.of(testMembershipCard));
        when(paymentGatewayService.verifyPayment(anyString(), any(BigDecimal.class)))
            .thenReturn(true);
        when(userRepository.findById(TEST_USER_ID))
            .thenReturn(Optional.of(testUser));
        when(dateUtil.calculateNewExpiryDate(any(LocalDate.class), anyInt()))
            .thenReturn(LocalDate.now().plusDays(DEFAULT_VALIDITY_DAYS));
        when(membershipCardRepository.save(any(MembershipCard.class)))
            .thenReturn(testMembershipCard);
        when(studentCardRepository.save(any(StudentCard.class)))
            .thenReturn(testStudentCard);
        doNothing().when(notificationService).sendRenewalConfirmation(any(UUID.class), anyString());

        // [ACT] Execute renewal
        MembershipCard result = membershipService.renewMembership(
            TEST_USER_ID, 
            DEFAULT_VALIDITY_DAYS, 
            VALID_PAYMENT_TRANSACTION_ID
        );

        // [ASSERT] Verify successful renewal
        assertNotNull(result, "Renewed membership card should not be null");
        assertEquals(DEFAULT_VALIDITY_DAYS, result.getRemainingDays(), 
            "Remaining days should be updated to renewal period");
        assertEquals(LocalDate.now().plusDays(DEFAULT_VALIDITY_DAYS), result.getExpiryDate(),
            "Expiry date should be extended by renewal period");
        
        // [VERIFY] Verify payment verification was called
        verify(paymentGatewayService, times(1))
            .verifyPayment(VALID_PAYMENT_TRANSACTION_ID, RENEWAL_FEE);
        
        // [VERIFY] Verify notification was sent
        verify(notificationService, times(1))
            .sendRenewalConfirmation(TEST_USER_ID, "Membership renewed successfully");
        
        // [LOG] Audit trail for successful renewal
        System.out.println("[AUDIT] [REQ-015] Membership renewal test passed for user: " + TEST_USER_ID);
    }

    // [TEST-002] Edge case: Renewal with maximum allowed days
    // [REQ-015] Verify renewal with maximum validity period
    @Test
    void renewMembership_Success_WithMaximumDays() {
        // [ARRANGE] Setup for maximum renewal
        when(membershipCardRepository.findByUserId(TEST_USER_ID))
            .thenReturn(Optional.of(testMembershipCard));
        when(paymentGatewayService.verifyPayment(anyString(), any(BigDecimal.class)))
            .thenReturn(true);
        when(dateUtil.calculateNewExpiryDate(any(LocalDate.class), eq(MAX_RENEWAL_DAYS)))
            .thenReturn(LocalDate.now().plusDays(MAX_RENEWAL_DAYS));
        when(membershipCardRepository.save(any(MembershipCard.class)))
            .thenReturn(testMembershipCard);
        when(notificationService.sendRenewalConfirmation(any(UUID.class), anyString()))
            .thenReturn(true);

        // [ACT] Execute renewal with maximum days
        MembershipCard result = membershipService.renewMembership(
            TEST_USER_ID, 
            MAX_RENEWAL_DAYS, 
            VALID_PAYMENT_TRANSACTION_ID
        );

        // [ASSERT] Verify maximum renewal
        assertNotNull(result);
        assertEquals(MAX_RENEWAL_DAYS, result.getRemainingDays());
        assertEquals(LocalDate.now().plusDays(MAX_RENEWAL_DAYS), result.getExpiryDate());
        
        // [LOG] Audit trail
        System.out.println("[AUDIT] [REQ-015] Maximum renewal test passed for user: " + TEST_USER_ID);
    }

    // [TEST-003] Exception case: Payment verification failure
    // [REQ-015] Verify payment failure handling
    @Test
    void renewMembership_Failure_InvalidPayment() {
        // [ARRANGE] Setup payment failure scenario
        when(membershipCardRepository.findByUserId(TEST_USER_ID))
            .thenReturn(Optional.of(testMembershipCard));
        when(paymentGatewayService.verifyPayment(anyString(), any(BigDecimal.class)))
            .thenReturn(false);

        // [ACT & ASSERT] Verify exception is thrown
        MembershipException exception = assertThrows(MembershipException.class, () -> {
            membershipService.renewMembership(
                TEST_USER_ID, 
                DEFAULT_VALIDITY_DAYS, 
                INVALID_PAYMENT_TRANSACTION_ID
            );
        });

        // [ASSERT] Verify exception details
        assertEquals("Payment verification failed", exception.getMessage());
        assertEquals("PAYMENT_VERIFICATION_FAILED", exception.getErrorCode());
        
        // [VERIFY] Verify no database changes occurred
        verify(membershipCardRepository, never()).save(any());
        verify(notificationService, never()).sendRenewalConfirmation(any(), anyString());
        
        // [LOG] Error audit trail
        System.out.println("[AUDIT] [REQ-015] Payment failure test passed for user: " + TEST_USER_ID);
    }

    // [TEST-004] Exception case: Membership card not found
    // [REQ-015] Verify handling when user has no membership card
    @Test
    void renewMembership_Failure_CardNotFound() {
        // [ARRANGE] Setup no card scenario
        when(membershipCardRepository.findByUserId(TEST_USER_ID))
            .thenReturn(Optional.empty());

        // [ACT & ASSERT] Verify exception is thrown
        MembershipException exception = assertThrows(MembershipException.class, () -> {
            membershipService.renewMembership(
                TEST_USER_ID, 
                DEFAULT_VALIDITY_DAYS, 
                VALID_PAYMENT_TRANSACTION_ID
            );
        });

        // [ASSERT] Verify exception details
        assertEquals("Membership card not found for user", exception.getMessage());
        assertEquals("CARD_NOT_FOUND", exception.getErrorCode());
        
        // [VERIFY] Verify no external calls were made
        verify(paymentGatewayService, never()).verifyPayment(anyString(), any(BigDecimal.class));
        verify(notificationService, never()).sendRenewalConfirmation(any(), anyString());
        
        // [LOG] Error audit trail
        System.out.println("[AUDIT] [REQ-015] Card not found test passed for user: " + TEST_USER_ID);
    }

    // [TEST-005] Exception case: Database error during renewal
    // [REQ-015] Verify database error handling
    @Test
    void renewMembership_Failure_DatabaseError() {
        // [ARRANGE] Setup database failure scenario
        when(membershipCardRepository.findByUserId(TEST_USER_ID))
            .thenReturn(Optional.of(testMembershipCard));
        when(paymentGatewayService.verifyPayment(anyString(), any(BigDecimal.class)))
            .thenReturn(true);
        when(membershipCardRepository.save(any(MembershipCard.class)))
            .thenThrow(new RuntimeException("Database connection timeout"));

        // [ACT & ASSERT] Verify exception is thrown
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            membershipService.renewMembership(
                TEST_USER_ID, 
                DEFAULT_VALIDITY_DAYS, 
                VALID_PAYMENT_TRANSACTION_ID
            );
        });

        // [ASSERT] Verify exception details
        assertEquals("Database connection timeout", exception.getMessage());
        
        // [VERIFY] Verify notification was not sent due to failure
        verify(notificationService, never()).sendRenewalConfirmation(any(), anyString());
        
        // [LOG] Error audit trail
        System.out.println("[AUDIT] [REQ-015] Database error test passed for user: " + TEST_USER_ID);
    }

    // [TEST-006] Exception case: Notification service failure
    // [EXC-003] Verify notification retry mechanism and failure handling
    @Test
    void renewMembership_NotificationFailure_RetryMechanism() {
        // [ARRANGE] Setup notification failure with retry
        when(membershipCardRepository.findByUserId(TEST_USER_ID))
            .thenReturn(Optional.of(testMembershipCard));
        when(paymentGatewayService.verifyPayment(anyString(), any(BigDecimal.class)))
            .thenReturn(true);
        when(dateUtil.calculateNewExpiryDate(any(LocalDate.class), anyInt()))
            .thenReturn(LocalDate.now().plusDays(DEFAULT_VALIDITY_DAYS));
        when(membershipCardRepository.save(any(MembershipCard.class)))
            .thenReturn(testMembershipCard);
        when(studentCardRepository.save(any(StudentCard.class)))
            .thenReturn(testStudentCard);
        
        // Simulate notification failure
        doThrow(new RuntimeException("FCM service unavailable"))
            .doNothing() // Succeed on retry
            .when(notificationService).sendRenewalConfirmation(any(UUID.class), anyString());

        // [ACT] Execute renewal
        MembershipCard result = membershipService.renewMembership(
            TEST_USER_ID, 
            DEFAULT_VALIDITY_DAYS, 
            VALID_PAYMENT_TRANSACTION_ID
        );

        // [ASSERT] Verify renewal succeeded despite notification failure
        assertNotNull(result);
        assertEquals(DEFAULT_VALIDITY_DAYS, result.getRemainingDays());
        
        // [VERIFY] Verify notification was retried (2 attempts: 1 failure + 1 success)
        verify(notificationService, times(2))
            .sendRenewalConfirmation(any(UUID.class), anyString());
        
        // [LOG] Audit trail for retry mechanism
        System.out.println("[AUDIT] [EXC-003] Notification retry test passed for user: " + TEST_USER_ID);
    }

    // [TEST-007] Edge case: Renewal with zero days (validation test)
    // [REQ-015] Verify input validation for renewal days
    @Test
    void renewMembership_Failure_InvalidRenewalDays() {
        // [ARRANGE] Setup validation failure
        when(validationUtil.isValidRenewalDays(0))
            .thenReturn(false);

        // [ACT & ASSERT] Verify validation exception
        MembershipException exception = assertThrows(MembershipException.class, () -> {
            membershipService.renewMembership(
                TEST_USER_ID, 
                0, 
                VALID_PAYMENT_TRANSACTION_ID
            );
        });

        // [ASSERT] Verify validation error
        assertEquals("Invalid renewal days", exception.getMessage());
        assertEquals("VALIDATION_FAILED", exception.getErrorCode());
        
        // [VERIFY] Verify no processing occurred
        verify(membershipCardRepository, never()).findByUserId(any());
        verify(paymentGatewayService, never()).verifyPayment(anyString(), any(BigDecimal.class));
        
        // [LOG] Validation audit trail
        System.out.println("[AUDIT] [REQ-015] Invalid renewal days validation test passed");
    }

    // [TEST-008] Edge case: Renewal for expired card
    // [REQ-015] Verify renewal handling for expired membership
    @Test
    void renewMembership_Success_ExpiredCardRenewal() {
        // [ARRANGE] Setup expired card scenario
        testMembershipCard.setExpiryDate(LocalDate.now().minusDays(5));
        testMembershipCard.setRemainingDays(0);
        testMembershipCard.setStatus("EXPIRED");
        
        when(membershipCardRepository.findByUserId(TEST_USER_ID))
            .thenReturn(Optional.of(testMembershipCard));
        when(paymentGatewayService.verifyPayment(anyString(), any(BigDecimal.class)))
            .thenReturn(true);
        when(dateUtil.calculateNewExpiryDate(any(LocalDate.class), anyInt()))
            .thenReturn(LocalDate.now().plusDays(DEFAULT_VALIDITY_DAYS));
        when(membershipCardRepository.save(any(MembershipCard.class)))
            .thenReturn(testMembershipCard);
        when(notificationService.sendRenewalConfirmation(any(UUID.class), anyString()))
            .thenReturn(true);

        // [ACT] Execute renewal for expired card
        MembershipCard result = membershipService.renewMembership(
            TEST_USER_ID, 
            DEFAULT_VALIDITY_DAYS, 
            VALID_PAYMENT_TRANSACTION_ID
        );

        // [ASSERT] Verify renewal reactivated expired card
        assertNotNull(result);
        assertEquals(DEFAULT_VALIDITY_DAYS, result.getRemainingDays());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(LocalDate.now().plusDays(DEFAULT_VALIDITY_DAYS), result.getExpiryDate());
        
        // [LOG] Audit trail for expired card renewal
        System.out.println("[AUDIT] [REQ-015] Expired card renewal test passed for user: " + TEST_USER_ID);
    }

    // [TEST-009] Edge case: Concurrent renewal attempt (idempotency)
    // [REQ-015] Verify idempotent behavior for concurrent renewal requests
    @Test
    void renewMembership_Idempotency_ConcurrentRequests() {
        // [ARRANGE] Setup for concurrent request simulation
        when(membershipCardRepository.findByUserId(TEST_USER_ID))
            .thenReturn(Optional.of(testMembershipCard));
        when(paymentGatewayService.verifyPayment(anyString(), any(BigDecimal.class)))
            .thenReturn(true);
        when(membershipCardRepository.save(any(MembershipCard.class)))
            .thenReturn(testMembershipCard);
        when(notificationService.sendRenewalConfirmation(any(UUID.class), anyString()))
            .thenReturn(true);

        // [ACT] Simulate concurrent renewal requests with same transaction ID
        MembershipCard result1 = membershipService.renewMembership(
            TEST_USER_ID, 
            DEFAULT_VALIDITY_DAYS, 
            VALID_PAYMENT_TRANSACTION_ID
        );
        
        // Second request with same transaction ID should be idempotent
        MembershipCard result2 = membershipService.renewMembership(
            TEST_USER_ID, 
            DEFAULT_VALIDITY_DAYS, 
            VALID_PAYMENT_TRANSACTION_ID
        );

        // [ASSERT] Verify both requests return same result (idempotent)
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.getCardId(), result2.getCardId());
        assertEquals(result1.getRemainingDays(), result2.getRemainingDays());
        
        // [VERIFY] Verify payment was verified only once (idempotency)
        verify(paymentGatewayService, times(1))
            .verifyPayment(VALID_PAYMENT_TRANSACTION_ID, RENEWAL_FEE);
        
        // [LOG] Idempotency audit trail
        System.out.println("[AUDIT] [REQ-015] Concurrent renewal idempotency test passed for user: " + TEST_USER_ID);
    }

    // [TEST-010] Edge case: Renewal with custom validity days within bounds
    // [REQ-015] Verify custom renewal period within allowed range
    @Test
    void renewMembership_Success_CustomValidityDays() {
        // [ARRANGE] Setup custom renewal period (60 days)
        int customDays = 60;
        when(membershipCardRepository.findByUserId(TEST_USER_ID))
            .thenReturn(Optional.of(testMembershipCard));
        when(paymentGatewayService.verifyPayment(anyString(), any(BigDecimal.class)))
            .thenReturn(true);
        when(dateUtil.calculateNewExpiryDate(any(LocalDate.class), eq(customDays)))
            .thenReturn(LocalDate.now().plusDays(customDays));
        when(membershipCardRepository.save(any(MembershipCard.class)))
            .thenReturn(testMembershipCard);
        when(notificationService.sendRenewalConfirmation(any(UUID.class), anyString()))
            .thenReturn(true);

        // [ACT] Execute renewal with custom days
        MembershipCard result = membershipService.renewMembership(
            TEST_USER_ID, 
            customDays, 
            VALID_PAYMENT_TRANSACTION_ID
        );

        // [ASSERT] Verify custom renewal period
        assertNotNull(result);
        assertEquals(customDays, result.getRemainingDays());
        assertEquals(LocalDate.now().plusDays(customDays), result.getExpiryDate());
        
        // [LOG] Custom renewal audit trail
        System.out.println("[AUDIT] [REQ-015] Custom validity days renewal test passed for user: " + TEST_USER_ID);
    }
}
```

# Day 2: model kilo-auto/free - API Endpoint https://api.kilo.ai/api/gateway
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/notification-service/src/test/java/com/hub/notification/NotificationServiceTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Test Component Destination Path: `./sources/backend/notification-service/src/test/java/com/hub/notification/NotificationServiceTest.java` (Must map to sources/backend/ or sources/frontend/)


### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/notification-service/src/test/java/com/hub/notification/NotificationServiceTest.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Viết bộ kiểm thử đơn vị cho dịch vụ thông báo cốt lõi']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` structure. Ensure that you read the exact Tag IDs from the `['[REQ-016]', '[EXC-003]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
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
```

