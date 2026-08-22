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
package org.nlh4j.saas.membership-hub.membership;

// ====================== ENTERPRISE IMPORTS ======================
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import org.jboss.resteasy.reactive.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.nlh4j.saas.membership-hub.membership.service.MembershipService;
import org.nlh4j.saas.membership-hub.membership.exception.MembershipCardNotFoundException;
import org.nlh4j.saas.membership-hub.membership.exception.MembershipServiceException;

/**
 * REST Controller for digital membership card operations.
 * Provides endpoints for retrieving membership card details including remaining validity period for authenticated users.
 * 
 * <p>Complies with enterprise RBAC policies, audit logging requirements, and OWASP security standards.</p>
 * 
 * @traceability [REQ-014], [DAT-007]
 * @since 1.0
 */
@Path("/api/membership")
public class MembershipController {

    // ====================== TOP-OF-CLASS ENTERPRISE CONSTANTS (NO MAGIC NUMBERS/STRINGS) ======================
    // [REQ-014] API endpoint path configuration
    public static final String API_MEMBERSHIP_CARD_PATH = "/card";
    // [DAT-007] Standardized error message constants
    public static final String ERROR_USER_NOT_AUTHENTICATED = "User is not authenticated or JWT token is invalid";
    public static final String ERROR_MEMBERSHIP_CARD_NOT_FOUND = "No active membership card found for the authenticated user";
    public static final String ERROR_INTERNAL_SERVER = "Internal server error occurred while processing membership card request";
    // [NFR-006] Audit log message templates
    public static final String LOG_ENTRY_GET_CARD = "Entering GET {} endpoint | Authenticated User ID: {}";
    public static final String LOG_EXIT_GET_CARD_SUCCESS = "Exiting GET {} endpoint | Successfully retrieved membership card for User ID: {}";
    public static final String LOG_ERROR_GET_CARD = "[CRITICAL FAIL] [REQ-014] [DAT-007] Error in GET {} endpoint | User ID: {} | Raw Error: {}";

    // ====================== ENTERPRISE LOGGING FRAMEWORK (Slf4j/Logback) ======================
    private static final Logger logger = LoggerFactory.getLogger(MembershipController.class);

    // ====================== DEPENDENCY INJECTION (SOLID SRP COMPLIANCE) ======================
    @Inject
    MembershipService membershipService;

    // ====================== REST API ENDPOINTS ======================
    /**
     * Retrieves the digital membership card details for the currently authenticated user.
     * Returns card validity information including total validity days, remaining days, and expiry date.
     * 
     * <p>Accessible to all authenticated user roles per RBAC policy [ARC-001]: Student, Teacher, Manager, Center Admin, System Admin.</p>
     * 
     * @param securityContext JAX-RS security context containing authenticated JWT user principal
     * @return RestResponse containing membership card details or structured error response
     * @traceability [REQ-014], [DAT-007]
     */
    @GET
    @Path(API_MEMBERSHIP_CARD_PATH)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"Student", "Teacher", "Manager", "Center Admin", "System Admin"})
    public RestResponse<MembershipCardResponse> getMembershipCard(@Context SecurityContext securityContext) {
        // Extract authenticated user ID from JWT token subject
        String authenticatedUserId = extractAuthenticatedUserId(securityContext);
        
        // [NFR-006] Log entry point with user ID for audit tracing
        logger.info(LOG_ENTRY_GET_CARD, API_MEMBERSHIP_CARD_PATH, authenticatedUserId);
        
        try {
            // Delegate business logic to service layer (adheres to Single Responsibility Principle)
            MembershipCardResponse membershipCard = membershipService.getMembershipCardByUserId(authenticatedUserId);
            
            // [NFR-006] Log successful exit with user ID
            logger.info(LOG_EXIT_GET_CARD_SUCCESS, API_MEMBERSHIP_CARD_PATH, authenticatedUserId);
            
            // Return 200 OK response with membership card data
            return RestResponse.ok(membershipCard);
        } 
        // Handle specific business exception for missing membership card [EXC-XXX]
        catch (MembershipCardNotFoundException e) {
            // [0.3] Log error with required 3 context keys: subsystem name, raw error message, tracking tag ID
            logger.error(LOG_ERROR_GET_CARD, API_MEMBERSHIP_CARD_PATH, authenticatedUserId, e.getMessage(), e);
            // Return 404 Not Found with standardized error payload
            return RestResponse.status(
                RestResponse.Status.NOT_FOUND,
                new ErrorResponse("MEMBERSHIP_CARD_NOT_FOUND", ERROR_MEMBERSHIP_CARD_NOT_FOUND)
            );
        }
        // Handle all unexpected runtime exceptions
        catch (Exception e) {
            // [0.3] Log critical error with full context and tag ID for centralized log aggregation
            logger.error(LOG_ERROR_GET_CARD, API_MEMBERSHIP_CARD_PATH, authenticatedUserId, e.getMessage(), e);
            // Return 500 Internal Server Error with generic message (avoid leaking sensitive system details)
            return RestResponse.status(
                RestResponse.Status.INTERNAL_SERVER_ERROR,
                new ErrorResponse("INTERNAL_SERVER_ERROR", ERROR_INTERNAL_SERVER)
            );
        }
    }

    // ====================== PRIVATE HELPER METHODS ======================
    /**
     * Extracts the authenticated user ID from the JWT token principal in the security context.
     * Validates authentication status before returning the user identifier.
     * 
     * @param securityContext JAX-RS security context from the incoming request
     * @return Authenticated user ID (UUID string from JWT subject)
     * @throws MembershipServiceException if user is not authenticated or principal is missing
     */
    private String extractAuthenticatedUserId(SecurityContext securityContext) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            // [NFR-006] Log authentication failure for audit
            logger.warn("[AUTH FAIL] [REQ-014] Unauthenticated access attempt to membership card endpoint");
            throw new MembershipServiceException(ERROR_USER_NOT_AUTHENTICATED, "UNAUTHENTICATED");
        }
        // JWT token subject is stored as user ID in the principal name per [ARC-006]
        return securityContext.getUserPrincipal().getName();
    }

    // ====================== RESPONSE DTOs (IMMUTABLE RECORDS) ======================
    /**
     * Immutable record representing membership card details returned to API clients.
     * Aligns with the student_cards table schema defined in [DAT-007].
     * 
     * @param cardId Unique identifier of the membership card (UUID)
     * @param studentId Unique identifier of the associated student (UUID)
     * @param issueDate Date the card was issued (format: YYYY-MM-DD)
     * @param validityDays Total number of days the card is valid for from issue date
     * @param remainingDays Number of days remaining before card expiry (>= 0)
     * @param expiryDate Date the card expires (format: YYYY-MM-DD)
     */
    public record MembershipCardResponse(
        String cardId,
        String studentId,
        String issueDate,
        int validityDays,
        int remainingDays,
        String expiryDate
    ) {}

    /**
     * Immutable record representing standardized error response payload.
     * Ensures consistent error format across all membership service endpoints.
     * 
     * @param errorCode Machine-readable error code for client-side handling
     * @param message Human-readable error message for end users
     */
    public record ErrorResponse(String errorCode, String message) {}
}