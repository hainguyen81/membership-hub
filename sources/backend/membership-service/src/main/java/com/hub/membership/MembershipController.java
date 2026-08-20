package org.nlh4j.saas.membership_hub.membership;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/**
 * REST controller responsible for handling membership card renewal requests.
 *
 * <p>All business logic for renewing a membership card is delegated to {@link MembershipService}.
 * Payment verification is performed via {@link PaymentService}. The controller ensures that
 * the authenticated user is identified, the payment transaction is validated, and the
 * membership card is updated accordingly.</p>
 *
 * @traceability [REQ-015]
 */
@RestController
@RequestMapping(MembershipController.BASE_PATH)
@Validated
public class MembershipController {

    /* --------------------------------------------------------------------- */
    /*  Constants – niceties for readability and maintainability            */
    /* --------------------------------------------------------------------- */

    /** Base path for all membership-related endpoints. */
    public static final String BASE_PATH = "/api/membership";

    /** Endpoint for card renewal. */
    public static final String ENDPOINT_RENEW = "/renew";

    /** Success message for a successful renewal. */
    public static final String MSG_RENEW_SUCCESS = "Membership card renewed successfully";

    /** Error message when payment verification fails. */
    public static final String MSG_PAYMENT_FAILED = "Payment verification failed";

    /** Log message prefix for processing steps. */
    public static final String LOG_PREFIX = "[PROCESS]";

    /** Log message prefix for warnings. */
    public static final String LOG_WARN = "[WARN]";

    /* --------------------------------------------------------------------- */
    /*  Logger – used for audit trail and debugging.                         */
    /* --------------------------------------------------------------------- */

    private static final Logger logger = LoggerFactory.getLogger(MembershipController.class);

    /* --------------------------------------------------------------------- */
    /*  Dependencies – injected by Spring.                                  */
    /* --------------------------------------------------------------------- */

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private PaymentService paymentService;

    /* --------------------------------------------------------------------- */
    /*  DTOs – request and response payloads.                               */
    /* --------------------------------------------------------------------- */

    /**
     * Request payload for membership card renewal.
     */
    public static class RenewRequest {

        /** Number of days to extend the membership. Must be at least 1. */
        @NotNull(message = "renewalDays must not be null")
        @Min(value = 1, message = "renewalDays must be at least 1")
        private Integer renewalDays;

        /** Identifier of the payment transaction. Must not be blank. */
        @NotBlank(message = "paymentTransactionId must not be blank")
        private String paymentTransactionId;

        // Getters and setters
        public Integer getRenewalDays() {
            return renewalDays;
        }

        public void setRenewalDays(Integer renewalDays) {
            this.renewalDays = renewalDays;
        }

        public String getPaymentTransactionId() {
            return paymentTransactionId;
        }

        public void setPaymentTransactionId(String paymentTransactionId) {
            this.paymentTransactionId = paymentTransactionId;
        }
    }

    /**
     * Response payload after a successful renewal.
     */
    public static class RenewResponse {

        /** Unique identifier of the membership card. */
        private UUID cardId;

        /** Remaining days after renewal. */
        private Integer remainingDays;

        /** Expiry date of the membership card. */
        private LocalDate expiryDate;

        // Getters and setters
        public UUID getCardId() {
            return cardId;
        }

        public void setCardId(UUID cardId) {
            this.cardId = cardId;
        }

        public Integer getRemainingDays() {
            return remainingDays;
        }

        public void setRemainingDays(Integer remainingDays) {
            this.remainingDays = remainingDays;
        }

        public LocalDate getExpiryDate() {
            return expiryDate;
        }

        public void setExpiryDate(LocalDate expiryDate) {
            this.expiryDate = expiryDate;
        }
    }

    /* --------------------------------------------------------------------- */
    /*  REST endpoint – POST /api/membership/renew                           */
    /* --------------------------------------------------------------------- */

    /**
     * Handles membership card renewal requests.
     *
     * @param request validated request payload
     * @return {@link ResponseEntity} containing {@link RenewResponse}
     */
    @PostMapping(ENDPOINT_RENEW)
    public ResponseEntity<RenewResponse> renewMembership(@Valid @RequestBody RenewRequest request) {
        // Retrieve the authenticated user's ID from the security context
        UUID userId = getCurrentUserId();

        // Log the initiation of the renewal process
        logger.info("{} Initiating renewal for userId: {}", LOG_PREFIX, userId);

        // Verify the payment transaction before proceeding
        boolean paymentVerified = paymentService.verifyTransaction(request.getPaymentTransactionId());
        if (!paymentVerified) {
            // Mask sensitive transaction ID in logs
            logger.warn("{} Payment verification failed for transactionId: {}", LOG_WARN, maskString(request.getPaymentTransactionId()));
            throw new IllegalArgumentException(MSG_PAYMENT_FAILED);
        }

        // Perform the renewal via the membership service
        MembershipCard updatedCard = membershipService.renewCard(userId, request.getRenewalDays());

        // Prepare the response payload
        RenewResponse response = new RenewResponse();
        response.setCardId(updatedCard.getCardId());
        response.setRemainingDays(updatedCard.getRemainingDays());
        response.setExpiryDate(updatedCard.getExpiryDate());

        // Log successful completion
        logger.info("{} Renewal completed for userId: {}", LOG_PREFIX, userId);

        return ResponseEntity.ok(response);
    }

    /* --------------------------------------------------------------------- */
    /*  Helper methods – niceties for security and logging.                 */
    /* --------------------------------------------------------------------- */

    /**
     * Retrieves the current authenticated user's UUID from the security context.
     *
     * @return UUID of the authenticated user
     * @throws IllegalStateException if the user is not authenticated
     */
    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) {
            // Assuming the username is the UUID string
            return UUID.fromString(((UserDetails) principal).getUsername());
        } else if (principal instanceof String) {
            return UUID.fromString((String) principal);
        } else {
            throw new IllegalStateException("Unable to retrieve user ID from principal");
        }
    }

    /**
     * Masks a sensitive string for logging purposes.
     *
     * @param value the original string
     * @return a masked representation (e.g., "****")
     */
    private String maskString(String value) {
        return (value == null || value.isEmpty()) ? "****" : "****";
    }

    /* --------------------------------------------------------------------- */
    /*  Placeholder interfaces and classes – to be replaced by actual        */
    /*  implementations in the real codebase.                                */
    /* --------------------------------------------------------------------- */

    /**
     * Service interface for membership card operations.
     */
    public interface MembershipService {
        /**
         * Renews the membership card for the specified user.
         *
         * @param userId      the user identifier
         * @param renewalDays number of days to extend
         * @return the updated membership card
         */
        MembershipCard renewCard(UUID userId, int renewalDays);
    }

    /**
     * Service interface for payment verification.
     */
    public interface PaymentService {
        /**
         * Verifies that the provided transaction ID corresponds to a successful payment.
         *
         * @param transactionId the payment transaction identifier
         * @return true if the payment is verified; false otherwise
         */
        boolean verifyTransaction(String transactionId);
    }

    /**
     * Domain model representing a membership card.
     */
    public static class MembershipCard {
        private UUID cardId;
        private int remainingDays;
        private LocalDate expiryDate;

        // Getters and setters
        public UUID getCardId() {
            return cardId;
        }

        public void setCardId(UUID cardId) {
            this.cardId = cardId;
        }

        public int getRemainingDays() {
            return remainingDays;
        }

        public void setRemainingDays(int remainingDays) {
            this.remainingDays = remainingDays;
        }

        public LocalDate getExpiryDate() {
            return expiryDate;
        }

        public void setExpiryDate(LocalDate expiryDate) {
            this.expiryDate = expiryDate;
        }
    }
}