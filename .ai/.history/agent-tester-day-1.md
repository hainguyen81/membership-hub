# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/membership-service/src/test/java/com/hub/membership/MembershipServiceTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Test Component Destination Path: `./sources/backend/membership-service/src/test/java/com/hub/membership/MembershipServiceTest.java` (Must map to sources/backend/ or sources/frontend/)


### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/membership-service/src/test/java/com/hub/membership/MembershipServiceTest.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Viết bộ kiểm thử đơn vị cho logic nghiệp vụ thẻ hội viên']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` structure. Ensure that you read the exact Tag IDs from the `['[REQ-014]', '[DAT-007]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership-hub.membership;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Unit test suite for MembershipService business logic.
 * Verifies requirements [REQ-014] and data model [DAT-007] for membership cards.
 *
 * @verifies [REQ-014] [DAT-007]
 */
@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(MembershipServiceTest.class);

    // -------------------------------------------------------------------------
    // Test Constants – all literal values are hoisted to the class crown per policy
    // -------------------------------------------------------------------------
    private static final UUID TEST_STUDENT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final UUID TEST_CARD_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final int VALIDITY_DAYS = 30;
    private static final int ADDITIONAL_DAYS = 15;
    private static final String PAYMENT_TX_ID = "pay-2024-0815-001";

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private PaymentGatewayService paymentGatewayService;

    @InjectMocks
    private MembershipService membershipService;

    @BeforeEach
    void setUp() {
        logger.info("[TEST_START] [REQ-014][DAT-007] Initializing MembershipService unit test environment");
        Mockito.reset(membershipRepository, paymentGatewayService);
    }

    @AfterEach
    void tearDown() {
        logger.info("[TEST_END] [REQ-014][DAT-007] Completed MembershipService unit test execution");
    }

    // -------------------------------------------------------------------------
    // Happy Path Tests
    // -------------------------------------------------------------------------

    /**
     * @verifies [REQ-014] [DAT-007]
     * Test retrieval of an existing membership card.
     */
    @Test
    @DisplayName("Test retrieve membership card for existing student [REQ-014][DAT-007]")
    void testGetMembershipCardInfo_Success() {
        // Arrange – build a realistic card entity matching DAT-007 schema
        MembershipCard expectedCard = MembershipCard.builder()
                .cardId(TEST_CARD_ID)
                .studentId(TEST_STUDENT_ID)
                .issueDate(LocalDate.now().minusDays(5))
                .validityDays(VALIDITY_DAYS)
                .remainingDays(VALIDITY_DAYS - 5)
                .createdAt(LocalDate.now().minusDays(5))
                .updatedAt(LocalDate.now())
                .build();

        when(membershipRepository.findByStudentId(TEST_STUDENT_ID))
                .thenReturn(Optional.of(expectedCard));

        // Act
        MembershipCard result = membershipService.getMembershipCardInfo(TEST_STUDENT_ID);

        // Assert – verify business contract and data integrity
        assertNotNull(result, "Membership card should not be null");
        assertEquals(TEST_CARD_ID, result.getCardId());
        assertEquals(TEST_STUDENT_ID, result.getStudentId());
        assertEquals(VALIDITY_DAYS - 5, result.getRemainingDays());
        verify(membershipRepository).findByStudentId(TEST_STUDENT_ID);
        logger.debug("[REQ-014][DAT-007] Membership card retrieved successfully for student {}", TEST_STUDENT_ID);
    }

    // -------------------------------------------------------------------------
    // Edge & Boundary Tests
    // -------------------------------------------------------------------------

    /**
     * @verifies [REQ-014] [DAT-007]
     * Test retrieval when the student has no membership card.
     */
    @Test
    @DisplayName("Test retrieve membership card for non‑existent student returns empty [REQ-014][DAT-007]")
    void testGetMembershipCardInfo_NotFound() {
        // Arrange – repository returns empty optional
        when(membershipRepository.findByStudentId(TEST_STUDENT_ID))
                .thenReturn(Optional.empty());

        // Act
        MembershipCard result = membershipService.getMembershipCardInfo(TEST_STUDENT_ID);

        // Assert – verify graceful handling of missing entity
        assertNull(result, "Membership card should be null when student not found");
        verify(membershipRepository).findByStudentId(TEST_STUDENT_ID);
        logger.debug("[REQ-014][DAT-007] No membership card found for student {}", TEST_STUDENT_ID);
    }

    /**
     * @verifies [REQ-014] [DAT-007]
     * Test renewal with a successful payment transaction.
     */
    @Test
    @DisplayName("Test renew membership with valid payment [REQ-014][DAT-007]")
    void testRenewMembership_Success() {
        // Arrange – existing card and successful payment gateway response
        MembershipCard existingCard = MembershipCard.builder()
                .cardId(TEST_CARD_ID)
                .studentId(TEST_STUDENT_ID)
                .issueDate(LocalDate.now().minusDays(10))
                .validityDays(VALIDITY_DAYS)
                .remainingDays(VALIDITY_DAYS - 10)
                .createdAt(LocalDate.now().minusDays(10))
                .updatedAt(LocalDate.now())
                .build();

        when(membershipRepository.findByStudentId(TEST_STUDENT_ID))
                .thenReturn(Optional.of(existingCard));
        when(paymentGatewayService.charge(anyString(), anyInt()))
                .thenReturn(true);
        when(membershipRepository.save(any(MembershipCard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        MembershipCard renewedCard = membershipService.renewMembership(
                TEST_STUDENT_ID, ADDITIONAL_DAYS, PAYMENT_TX_ID);

        // Assert – verify card updated with new remaining days
        assertNotNull(renewedCard);
        assertEquals(TEST_STUDENT_ID, renewedCard.getStudentId());
        assertEquals(VALIDITY_DAYS - 10 + ADDITIONAL_DAYS, renewedCard.getRemainingDays());
        assertTrue(renewedCard.getRemainingDays() > 0);
        verify(paymentGatewayService).charge(PAYMENT_TX_ID, ADDITIONAL_DAYS * 1000); // unit price per day
        verify(membershipRepository).save(renewedCard);
        logger.debug("[REQ-014][DAT-007] Membership renewed successfully for student {}", TEST_STUDENT_ID);
    }

    /**
     * @verifies [REQ-014] [DAT-007]
     * Test renewal when the payment gateway rejects the transaction.
     */
    @Test
    @DisplayName("Test renew membership with failed payment [REQ-014][DAT-007]")
    void testRenewMembership_PaymentFailed() {
        // Arrange – repository returns card, payment gateway returns false
        when(membershipRepository.findByStudentId(TEST_STUDENT_ID))
                .thenReturn(Optional.of(MembershipCard.builder()
                        .studentId(TEST_STUDENT_ID)
                        .validityDays(VALIDITY_DAYS)
                        .remainingDays(VALIDITY_DAYS)
                        .build()));

        when(paymentGatewayService.charge(PAYMENT_TX_ID, ADDITIONAL_DAYS * 1000))
                .thenReturn(false);

        // Act & Assert – verify exception thrown and no card saved
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                membershipService.renewMembership(TEST_STUDENT_ID, ADDITIONAL_DAYS, PAYMENT_TX_ID));

        assertTrue(ex.getMessage().contains("Payment failed"));
        verify(paymentGatewayService).charge(PAYMENT_TX_ID, ADDITIONAL_DAYS * 1000);
        verify(membershipRepository, never()).save(any());
        logger.debug("[REQ-014][DAT-007] Membership renewal blocked due to payment failure for student {}", TEST_STUDENT_ID);
    }

    /**
     * @verifies [REQ-014] [DAT-007]
     * Test renewal with zero additional days (edge case).
     */
    @Test
    @DisplayName("Test renew membership with zero additional days [REQ-014][DAT-007]")
    void testRenewMembership_ZeroDays() {
        // Arrange – zero additional days, payment still processed
        when(membershipRepository.findByStudentId(TEST_STUDENT_ID))
                .thenReturn(Optional.of(MembershipCard.builder()
                        .studentId(TEST_STUDENT_ID)
                        .validityDays(VALIDITY_DAYS)
                        .remainingDays(VALIDITY_DAYS)
                        .build()));
        when(paymentGatewayService.charge(PAYMENT_TX_ID, 0))
                .thenReturn(true);
        when(membershipRepository.save(any(MembershipCard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        MembershipCard result = membershipService.renewMembership(TEST_STUDENT_ID, 0, PAYMENT_TX_ID);

        // Assert – remaining days unchanged
        assertNotNull(result);
        assertEquals(VALIDITY_DAYS, result.getRemainingDays());
        verify(paymentGatewayService).charge(PAYMENT_TX_ID, 0);
        verify(membershipRepository).save(result);
        logger.debug("[REQ-014][DAT-007] Membership renewal with zero days processed for student {}", TEST_STUDENT_ID);
    }

    /**
     * @verifies [REQ-014] [DAT-007]
     * Test renewal with negative additional days – should raise IllegalArgumentException.
     */
    @Test
    @DisplayName("Test renew membership with negative additional days throws IllegalArgumentException [REQ-014][DAT-007]")
    void testRenewMembership_NegativeDays() {
        // Arrange – repository returns card, negative days passed
        when(membershipRepository.findByStudentId(TEST_STUDENT_ID))
                .thenReturn(Optional.of(MembershipCard.builder()
                        .studentId(TEST_STUDENT_ID)
                        .validityDays(VALIDITY_DAYS)
                        .remainingDays(VALIDITY_DAYS)
                        .build()));

        // Act & Assert – verify validation exception
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                membershipService.renewMembership(TEST_STUDENT_ID, -5, PAYMENT_TX_ID));

        assertTrue(ex.getMessage().contains("Additional days must be positive"));
        verify(membershipRepository, never()).save(any());
        logger.debug("[REQ-014][DAT-007] Negative additional days rejected for student {}", TEST_STUDENT_ID);
    }

    /**
     * @verifies [REQ-014] [DAT-007]
     * Test calculation of remaining days for an expired card.
     */
    @Test
    @DisplayName("Test calculate remaining days for expired card [REQ-014][DAT-007]")
    void testCalculateRemainingDays_Expired() {
        // Arrange – card with zero remaining days
        MembershipCard expiredCard = MembershipCard.builder()
                .studentId(TEST_STUDENT_ID)
                .validityDays(VALIDITY_DAYS)
                .remainingDays(0)
                .build();

        when(membershipRepository.findByStudentId(TEST_STUDENT_ID))
                .thenReturn(Optional.of(expiredCard));

        // Act
        int remaining = membershipService.calculateRemainingDays(TEST_STUDENT_ID);

        // Assert – verify zero remaining days
        assertEquals(0, remaining);
        verify(membershipRepository).findByStudentId(TEST_STUDENT_ID);
        logger.debug("[REQ-014][DAT-007] Remaining days calculation for expired card for student {}", TEST_STUDENT_ID);
    }
}
```

