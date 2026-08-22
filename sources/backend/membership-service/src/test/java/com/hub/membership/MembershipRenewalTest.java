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