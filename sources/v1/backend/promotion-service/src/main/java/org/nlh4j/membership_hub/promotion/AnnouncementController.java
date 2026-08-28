package org.nlh4j.saas.membership_hub.promotion;

// [IMPORTS LAYER - ENTERPRISE DEPENDENCY COMPLIANCE]
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Context;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.DefaultValue;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.nlh4j.saas.membership_hub.promotion.dto.AnnouncementRequestDTO;
import org.nlh4j.saas.membership_hub.promotion.dto.AnnouncementResponseDTO;
import org.nlh4j.saas.membership_hub.promotion.dto.PromotionRequestDTO;
import org.nlh4j.saas.membership_hub.promotion.dto.PromotionResponseDTO;
import org.nlh4j.saas.membership_hub.promotion.entity.Announcement;
import org.nlh4j.saas.membership_hub.promotion.entity.Promotion;
import org.nlh4j.saas.membership_hub.promotion.exception.AnnouncementNotFoundException;
import org.nlh4j.saas.membership_hub.promotion.exception.DuplicateAnnouncementException;
import org.nlh4j.saas.membership_hub.promotion.exception.DuplicatePromoCodeException;
import org.nlh4j.saas.membership_hub.promotion.exception.InvalidPromotionDataException;
import org.nlh4j.saas.membership_hub.promotion.exception.ValidationException;
import org.nlh4j.saas.membership_hub.promotion.service.AnnouncementService;
import org.nlh4j.saas.membership_hub.promotion.service.PromotionService;
import org.nlh4j.saas.membership_hub.security.CustomUserDetails;
import org.nlh4j.saas.membership_hub.security.SecurityContext;
import org.owasp.encoder.Encode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for managing system announcements and promotions
 * <p>
 * Exposes CRUD endpoints for announcements (REQ-018) and promotions (REQ-017),
 * enforces RBAC access control, input validation, XSS sanitization, and idempotency for mutation operations.
 * All database operations use Hibernate prepared statements to prevent SQL injection per NFR-003.
 * Active record filtering and auto-hide of expired announcements are handled by the service layer scheduled job.
 * </p>
 * @traceability [REQ-017], [REQ-018], [DAT-009]
 * @since 1.0
 * @author Membership Hub Engineering Team
 */
@ApplicationScoped
@Path("/api/v1")
public class AnnouncementController implements Serializable {

    private static final long serialVersionUID = 1L;

    // [CONSTANTS LAYER - ANTI-MAGIC NUMBERS COMPLIANCE [0.2]]
    // All business rule and configuration constants are declared at class crown level, no hardcoded values in operational logic
    public static final String TRACEABILITY_TAGS = "[REQ-017], [REQ-018], [DAT-009]";
    public static final String SUBSYSTEM_NAME = "Promotion-Announcement-Service"; // For audit logging [0.3]
    public static final int MAX_ANNOUNCEMENT_TITLE_LENGTH = 150; // Per DAT-009 schema constraint
    public static final int MAX_ANNOUNCEMENT_CONTENT_LENGTH = 2000; // Per DAT-009 schema constraint
    public static final int MAX_PROMO_CODE_LENGTH = 50; // Per DAT-009 schema constraint
    public static final int MAX_PROMO_DESCRIPTION_LENGTH = 2000; // Per DAT-009 schema constraint
    public static final int MIN_PROMO_DISCOUNT_PERCENT = 0; // Per REQ-017 business rule
    public static final int MAX_PROMO_DISCOUNT_PERCENT = 100; // Per REQ-017 business rule
    public static final int DEFAULT_PAGE_SIZE = 20; // Default pagination size for list endpoints
    public static final int MAX_PAGE_SIZE = 100; // Maximum allowed page size to prevent abuse
    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key"; // Enterprise standard header for mutation idempotency [1.0]

    // [LOGGER LAYER - ENTERPRISE AUDIT COMPLIANCE [0.3]]
    private static final Logger logger = Logger.getLogger(AnnouncementController.class);

    // [DEPENDENCY INJECTION LAYER - QUARKUS NATIVE]
    @Inject
    AnnouncementService announcementService; // Business logic for announcement operations
    @Inject
    PromotionService promotionService; // Business logic for promotion operations
    @Inject
    SecurityContext securityContext; // Quarkus native security context for user identity extraction
    @Context
    HttpHeaders httpHeaders; // JAX-RS context for accessing request headers (idempotency key)

    // [INNER DTO LAYER - REQUEST/RESPONSE DATA CONTRACTS]
    // DTO for announcement creation/update requests, enforces input validation constraints
    public static class AnnouncementRequestDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @NotBlank(message = "Announcement title is required")
        @Size(max = MAX_ANNOUNCEMENT_TITLE_LENGTH, message = "Title cannot exceed " + MAX_ANNOUNCEMENT_TITLE_LENGTH + " characters")
        private String title;

        @NotBlank(message = "Announcement content is required")
        @Size(max = MAX_ANNOUNCEMENT_CONTENT_LENGTH, message = "Content cannot exceed " + MAX_ANNOUNCEMENT_CONTENT_LENGTH + " characters")
        private String content;

        @PastOrPresent(message = "Start date cannot be in the future")
        private LocalDate startDate;

        @FutureOrPresent(message = "End date cannot be in the past")
        private LocalDate endDate;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    }

    // DTO for promotion creation/update requests, enforces business rule validation
    public static class PromotionRequestDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @NotBlank(message = "Promo code is required")
        @Size(max = MAX_PROMO_CODE_LENGTH, message = "Promo code cannot exceed " + MAX_PROMO_CODE_LENGTH + " characters")
        @Pattern(regexp = "^[A-Z0-9]+$", message = "Promo code must contain only uppercase letters and numbers")
        private String code;

        @NotNull(message = "Discount percent is required")
        @Min(value = MIN_PROMO_DISCOUNT_PERCENT, message = "Discount percent cannot be less than " + MIN_PROMO_DISCOUNT_PERCENT)
        @Max(value = MAX_PROMO_DISCOUNT_PERCENT, message = "Discount percent cannot exceed " + MAX_PROMO_DISCOUNT_PERCENT)
        private Integer discountPercent;

        @PastOrPresent(message = "Start date cannot be in the future")
        private LocalDate startDate;

        @FutureOrPresent(message = "End date cannot be in the past")
        private LocalDate endDate;

        @Size(max = MAX_PROMO_DESCRIPTION_LENGTH, message = "Description cannot exceed " + MAX_PROMO_DESCRIPTION_LENGTH + " characters")
        private String description;

        // Getters and setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public Integer getDiscountPercent() { return discountPercent; }
        public void setDiscountPercent(Integer discountPercent) { this.discountPercent = discountPercent; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    // DTO for announcement response payloads, prevents overposting by exposing only safe fields
    public static class AnnouncementResponseDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private UUID announcementId;
        private String title;
        private String content;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Constructor to map entity to DTO
        public AnnouncementResponseDTO(Announcement announcement) {
            this.announcementId = announcement.getAnnouncementId();
            this.title = announcement.getTitle();
            this.content = announcement.getContent();
            this.startDate = announcement.getStartDate();
            this.endDate = announcement.getEndDate();
            this.createdAt = announcement.getCreatedAt();
            this.updatedAt = announcement.getUpdatedAt();
        }

        // Getters
        public UUID getAnnouncementId() { return announcementId; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
    }

    // DTO for promotion response payloads, prevents overposting by exposing only safe fields
    public static class PromotionResponseDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private UUID promoId;
        private String code;
        private Integer discountPercent;
        private LocalDate startDate;
        private LocalDate endDate;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Constructor to map entity to DTO
        public PromotionResponseDTO(Promotion promotion) {
            this.promoId = promotion.getPromoId();
            this.code = promotion.getCode();
            this.discountPercent = promotion.getDiscountPercent();
            this.startDate = promotion.getStartDate();
            this.endDate = promotion.getEndDate();
            this.description = promotion.getDescription();
            this.createdAt = promotion.getCreatedAt();
            this.updatedAt = promotion.getUpdatedAt();
        }

        // Getters
        public UUID getPromoId() { return promoId; }
        public String getCode() { return code; }
        public Integer getDiscountPercent() { return discountPercent; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public String getDescription() { return description; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
    }

    // [INNER EXCEPTION LAYER - BUSINESS EXCEPTION CONTRACTS]
    // Custom validation exception for input rule violations
    public static class ValidationException extends Exception {
        private static final long serialVersionUID = 1L;
        public ValidationException(String message) { super(message); }
    }

    // Custom exception for duplicate promo code conflicts
    public static class DuplicatePromoCodeException extends Exception {
        private static final long serialVersionUID = 1L;
        public DuplicatePromoCodeException(String message) { super(message); }
    }

    // Custom exception for duplicate announcement conflicts
    public static class DuplicateAnnouncementException extends Exception {
        private static final long serialVersionUID = 1L;
        public DuplicateAnnouncementException(String message) { super(message); }
    }

    // Custom exception for invalid promotion data (discount range, date order)
    public static class InvalidPromotionDataException extends Exception {
        private static final long serialVersionUID = 1L;
        public InvalidPromotionDataException(String message) { super(message); }
    }

    // [UTILITY METHOD LAYER - SECURITY & IDENTITY]
    /**
     * Sanitizes user input to prevent XSS attacks per OWASP Top 10 compliance [NFR-003]
     * Uses OWASP Java HTML Sanitizer to remove malicious scripts and tags from input
     * @param input the raw input string from user request
     * @return sanitized string safe for storage and display
     */
    private String sanitizeInput(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        // Encode HTML special characters to neutralize XSS payloads
        return Encode.forHtml(input);
    }

    /**
     * Retrieves the current authenticated user ID from the Quarkus security context
     * @return UUID of the current authenticated user
     * @throws WebApplicationException with 401 status if user is not authenticated
     */
    private UUID getCurrentUserId() {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            // Log unauthenticated access attempt per audit requirements [NFR-006]
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Unauthenticated access attempt to promotion/announcement endpoint");
            throw new WebApplicationException("User not authenticated", Response.Status.UNAUTHORIZED);
        }
        // Cast to project-specific CustomUserDetails to extract user ID and roles
        return ((CustomUserDetails) securityContext.getUserPrincipal()).getUserId();
    }

    // ==============================================
    // ANNOUNCEMENT ENDPOINTS (REQ-018)
    // ==============================================

    /**
     * Retrieves all active announcements visible to all authenticated users
     * Active announcements are those where current date is between start_date and end_date, or end_date is null
     * @return list of active announcements with 200 OK status
     * @traceability [REQ-018]
     */
    @GET
    @Path("/announcements/active")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getActiveAnnouncements() {
        try {
            // Entry audit log per enterprise logging requirements [0.3]
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Fetching active announcements for public access");
            // Service layer handles filtering of active records and scheduled auto-hide of expired announcements
            List<AnnouncementResponseDTO> activeAnnouncements = announcementService.getActiveAnnouncements();
            // Exit audit log with result count
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Fetched {} active announcements", activeAnnouncements.size());
            return Response.ok(activeAnnouncements).build();
        } catch (Exception e) {
            // Comprehensive error logging with required 3 context keys [0.3]
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Failed to fetch active announcements. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while fetching active announcements", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves all announcements (including inactive/expired) for admin users (Center Admin, Manager)
     * Supports pagination with page and size query parameters
     * @param page page number (default 1)
     * @param size page size (default 20, max 100)
     * @return paginated list of all announcements with 200 OK status
     * @throws WebApplicationException with 400 if pagination parameters are invalid, 403 if user lacks permissions
     * @traceability [REQ-018]
     */
    @GET
    @Path("/announcements")
    @RolesAllowed({"CENTER_ADMIN", "MANAGER"}) // RBAC enforcement per ARC-001 to ARC-005
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllAnnouncements(@QueryParam("page") @DefaultValue("1") int page, @QueryParam("size") @DefaultValue(String.valueOf(DEFAULT_PAGE_SIZE)) int size) {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Fetching all announcements for admin user: {}", getCurrentUserId());
            // Validate pagination parameters to prevent abuse
            if (page < 1) {
                throw new ValidationException("Page number must be at least 1");
            }
            if (size < 1 || size > MAX_PAGE_SIZE) {
                throw new ValidationException("Page size must be between 1 and " + MAX_PAGE_SIZE);
            }
            // Service layer uses prepared statements for paginated queries to prevent SQL injection [NFR-003]
            List<AnnouncementResponseDTO> announcements = announcementService.getAllAnnouncements(page, size);
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Fetched {} announcements (page {}, size {})", announcements.size(), page, size);
            return Response.ok(announcements).build();
        } catch (ValidationException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Invalid pagination parameters. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Invalid pagination: " + e.getMessage(), Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Failed to fetch all announcements. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while fetching announcements", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Creates a new system announcement (admin only: Center Admin, Manager)
     * Enforces input sanitization, idempotency, and business rule validation
     * @param request announcement creation request DTO
     * @return created announcement with 201 CREATED status
     * @throws WebApplicationException with 400 for invalid input, 409 for duplicate, 403 for insufficient permissions
     * @traceability [REQ-018]
     */
    @POST
    @Path("/announcements")
    @RolesAllowed({"CENTER_ADMIN", "MANAGER"}) // RBAC enforcement per ARC-001 to ARC-005
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAnnouncement(@Valid AnnouncementRequestDTO request) {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Creating new announcement for user: {}", getCurrentUserId());
            // Step 1: Sanitize input to prevent XSS attacks per OWASP Top 10 [NFR-003]
            String sanitizedTitle = sanitizeInput(request.getTitle());
            String sanitizedContent = sanitizeInput(request.getContent());
            // Step 2: Defense-in-depth validation for length constraints
            if (sanitizedTitle.length() > MAX_ANNOUNCEMENT_TITLE_LENGTH) {
                throw new ValidationException("Title exceeds maximum allowed length of " + MAX_ANNOUNCEMENT_TITLE_LENGTH + " characters");
            }
            if (sanitizedContent.length() > MAX_ANNOUNCEMENT_CONTENT_LENGTH) {
                throw new ValidationException("Content exceeds maximum allowed length of " + MAX_ANNOUNCEMENT_CONTENT_LENGTH + " characters");
            }
            // Step 3: Validate idempotency key to prevent duplicate mutation requests per enterprise governance [1.0]
            String idempotencyKey = httpHeaders.getHeaderString(IDEMPOTENCY_KEY_HEADER);
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                throw new ValidationException("Idempotency-Key header is required for mutation operations");
            }
            // Step 4: Call service layer to create announcement (uses Hibernate prepared statements to prevent SQL injection [NFR-003])
            AnnouncementResponseDTO response = announcementService.createAnnouncement(sanitizedTitle, sanitizedContent, request.getStartDate(), request.getEndDate(), idempotencyKey);
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Announcement created successfully with ID: {}", response.getAnnouncementId());
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (ConstraintViolationException e) {
            // Handle Jakarta validation errors with detailed field-level messages
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Announcement creation failed due to invalid input. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            String validationErrors = e.getConstraintViolations().stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.joining(", "));
            throw new WebApplicationException("Validation failed: " + validationErrors, Response.Status.BAD_REQUEST);
        } catch (DuplicateAnnouncementException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Announcement creation failed due to duplicate. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Duplicate announcement: " + e.getMessage(), Response.Status.CONFLICT);
        } catch (Exception e) {
            // Preserve original exception cause chain per enterprise exception handling rules [0.3]
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Announcement creation failed due to system error. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while creating announcement", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Updates an existing announcement (admin only: Center Admin, Manager)
     * @param id UUID of the announcement to update
     * @param request updated announcement data
     * @return updated announcement with 200 OK status
     * @throws WebApplicationException with 400 for invalid input, 404 if announcement not found, 403 for insufficient permissions
     * @traceability [REQ-018]
     */
    @PUT
    @Path("/announcements/{id}")
    @RolesAllowed({"CENTER_ADMIN", "MANAGER"}) // RBAC enforcement per ARC-001 to ARC-005
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAnnouncement(@PathParam("id") UUID id, @Valid AnnouncementRequestDTO request) {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Updating announcement ID: {} for user: {}", id, getCurrentUserId());
            // Sanitize input to prevent XSS
            String sanitizedTitle = sanitizeInput(request.getTitle());
            String sanitizedContent = sanitizeInput(request.getContent());
            // Validate idempotency key for mutation operation
            String idempotencyKey = httpHeaders.getHeaderString(IDEMPOTENCY_KEY_HEADER);
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                throw new ValidationException("Idempotency-Key header is required for mutation operations");
            }
            // Call service to update announcement
            AnnouncementResponseDTO response = announcementService.updateAnnouncement(id, sanitizedTitle, sanitizedContent, request.getStartDate(), request.getEndDate(), idempotencyKey);
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Announcement ID: {} updated successfully", id);
            return Response.ok(response).build();
        } catch (ConstraintViolationException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Announcement update failed due to invalid input. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            String validationErrors = e.getConstraintViolations().stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.joining(", "));
            throw new WebApplicationException("Validation failed: " + validationErrors, Response.Status.BAD_REQUEST);
        } catch (AnnouncementNotFoundException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Announcement not found for update. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Announcement not found with ID: " + id, Response.Status.NOT_FOUND);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Announcement update failed. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while updating announcement", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes an announcement (admin only: Center Admin, Manager)
     * @param id UUID of the announcement to delete
     * @return 200 OK with success message
     * @throws WebApplicationException with 404 if announcement not found, 403 for insufficient permissions
     * @traceability [REQ-018]
     */
    @DELETE
    @Path("/announcements/{id}")
    @RolesAllowed({"CENTER_ADMIN", "MANAGER"}) // RBAC enforcement per ARC-001 to ARC-005
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAnnouncement(@PathParam("id") UUID id) {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Deleting announcement ID: {} for user: {}", id, getCurrentUserId());
            // Validate idempotency key for mutation operation
            String idempotencyKey = httpHeaders.getHeaderString(IDEMPOTENCY_KEY_HEADER);
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                throw new ValidationException("Idempotency-Key header is required for mutation operations");
            }
            // Call service to delete announcement
            announcementService.deleteAnnouncement(id, idempotencyKey);
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Announcement ID: {} deleted successfully", id);
            return Response.ok("{\"message\": \"Announcement deleted successfully\"}").build();
        } catch (AnnouncementNotFoundException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Announcement not found for deletion. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Announcement not found with ID: " + id, Response.Status.NOT_FOUND);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Announcement deletion failed. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while deleting announcement", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    // ==============================================
    // PROMOTION ENDPOINTS (REQ-017)
    // ==============================================

    /**
     * Retrieves all active promotions visible to all authenticated users
     * Active promotions are those where current date is between start_date and end_date, or end_date is null
     * @return list of active promotions with 200 OK status
     * @traceability [REQ-017]
     */
    @GET
    @Path("/promotions/active")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getActivePromotions() {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Fetching active promotions for public access");
            // Service layer filters active records and enforces business rules for validity periods
            List<PromotionResponseDTO> activePromotions = promotionService.getActivePromotions();
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Fetched {} active promotions", activePromotions.size());
            return Response.ok(activePromotions).build();
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Failed to fetch active promotions. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while fetching active promotions", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves all promotions (including inactive/expired) for admin users (Center Admin, Manager)
     * Supports pagination with page and size query parameters
     * @param page page number (default 1)
     * @param size page size (default 20, max 100)
     * @return paginated list of all promotions with 200 OK status
     * @throws WebApplicationException with 400 for invalid pagination, 403 for insufficient permissions
     * @traceability [REQ-017]
     */
    @GET
    @Path("/promotions")
    @RolesAllowed({"CENTER_ADMIN", "MANAGER"}) // RBAC enforcement per ARC-001 to ARC-005
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllPromotions(@QueryParam("page") @DefaultValue("1") int page, @QueryParam("size") @DefaultValue(String.valueOf(DEFAULT_PAGE_SIZE)) int size) {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Fetching all promotions for admin user: {}", getCurrentUserId());
            // Validate pagination parameters
            if (page < 1) {
                throw new ValidationException("Page number must be at least 1");
            }
            if (size < 1 || size > MAX_PAGE_SIZE) {
                throw new ValidationException("Page size must be between 1 and " + MAX_PAGE_SIZE);
            }
            // Service layer uses prepared statements for paginated queries to prevent SQL injection [NFR-003]
            List<PromotionResponseDTO> promotions = promotionService.getAllPromotions(page, size);
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Fetched {} promotions (page {}, size {})", promotions.size(), page, size);
            return Response.ok(promotions).build();
        } catch (ValidationException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Invalid pagination parameters. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Invalid pagination: " + e.getMessage(), Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Failed to fetch all promotions. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while fetching promotions", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Creates a new promotion (admin only: Center Admin, Manager)
     * Enforces business rules: discount 0-100, end date >= start date, unique promo code
     * @param request promotion creation request DTO
     * @return created promotion with 201 CREATED status
     * @throws WebApplicationException with 400 for invalid input, 409 for duplicate code, 403 for insufficient permissions
     * @traceability [REQ-017]
     */
    @POST
    @Path("/promotions")
    @RolesAllowed({"CENTER_ADMIN", "MANAGER"}) // RBAC enforcement per ARC-001 to ARC-005
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPromotion(@Valid PromotionRequestDTO request) {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Creating new promotion for user: {}", getCurrentUserId());
            // Step 1: Sanitize input to prevent XSS attacks per OWASP Top 10 [NFR-003]
            String sanitizedCode = sanitizeInput(request.getCode()).toUpperCase(); // Promo codes are standardized to uppercase
            String sanitizedDescription = request.getDescription() != null ? sanitizeInput(request.getDescription()) : null;
            // Step 2: Defense-in-depth validation for business rules
            if (request.getDiscountPercent() < MIN_PROMO_DISCOUNT_PERCENT || request.getDiscountPercent() > MAX_PROMO_DISCOUNT_PERCENT) {
                throw new InvalidPromotionDataException("Discount percent must be between " + MIN_PROMO_DISCOUNT_PERCENT + " and " + MAX_PROMO_DISCOUNT_PERCENT);
            }
            if (request.getEndDate() != null && request.getStartDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
                throw new InvalidPromotionDataException("End date cannot be before start date");
            }
            // Step 3: Validate idempotency key to prevent duplicate mutation requests per enterprise governance [1.0]
            String idempotencyKey = httpHeaders.getHeaderString(IDEMPOTENCY_KEY_HEADER);
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                throw new ValidationException("Idempotency-Key header is required for mutation operations");
            }
            // Step 4: Call service layer to create promotion (uses Hibernate prepared statements to prevent SQL injection [NFR-003])
            PromotionResponseDTO response = promotionService.createPromotion(sanitizedCode, request.getDiscountPercent(), request.getStartDate(), request.getEndDate(), sanitizedDescription, idempotencyKey);
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Promotion created successfully with code: {}", response.getCode());
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (ConstraintViolationException e) {
            // Handle Jakarta validation errors
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion creation failed due to invalid input. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            String validationErrors = e.getConstraintViolations().stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.joining(", "));
            throw new WebApplicationException("Validation failed: " + validationErrors, Response.Status.BAD_REQUEST);
        } catch (DuplicatePromoCodeException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion creation failed due to duplicate code. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Duplicate promo code: " + e.getMessage(), Response.Status.CONFLICT);
        } catch (InvalidPromotionDataException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion creation failed due to invalid data. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Invalid promotion data: " + e.getMessage(), Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            // Preserve original exception cause chain per enterprise exception handling rules [0.3]
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion creation failed due to system error. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while creating promotion", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Updates an existing promotion (admin only: Center Admin, Manager)
     * @param id UUID of the promotion to update
     * @param request updated promotion data
     * @return updated promotion with 200 OK status
     * @throws WebApplicationException with 400 for invalid input, 404 if promotion not found, 409 for duplicate code, 403 for insufficient permissions
     * @traceability [REQ-017]
     */
    @PUT
    @Path("/promotions/{id}")
    @RolesAllowed({"CENTER_ADMIN", "MANAGER"}) // RBAC enforcement per ARC-001 to ARC-005
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePromotion(@PathParam("id") UUID id, @Valid PromotionRequestDTO request) {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Updating promotion ID: {} for user: {}", id, getCurrentUserId());
            // Sanitize input to prevent XSS
            String sanitizedCode = sanitizeInput(request.getCode()).toUpperCase();
            String sanitizedDescription = request.getDescription() != null ? sanitizeInput(request.getDescription()) : null;
            // Validate business rules
            if (request.getDiscountPercent() < MIN_PROMO_DISCOUNT_PERCENT || request.getDiscountPercent() > MAX_PROMO_DISCOUNT_PERCENT) {
                throw new InvalidPromotionDataException("Discount percent must be between " + MIN_PROMO_DISCOUNT_PERCENT + " and " + MAX_PROMO_DISCOUNT_PERCENT);
            }
            if (request.getEndDate() != null && request.getStartDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
                throw new InvalidPromotionDataException("End date cannot be before start date");
            }
            // Validate idempotency key for mutation operation
            String idempotencyKey = httpHeaders.getHeaderString(IDEMPOTENCY_KEY_HEADER);
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                throw new ValidationException("Idempotency-Key header is required for mutation operations");
            }
            // Call service to update promotion
            PromotionResponseDTO response = promotionService.updatePromotion(id, sanitizedCode, request.getDiscountPercent(), request.getStartDate(), request.getEndDate(), sanitizedDescription, idempotencyKey);
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Promotion ID: {} updated successfully", id);
            return Response.ok(response).build();
        } catch (ConstraintViolationException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion update failed due to invalid input. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            String validationErrors = e.getConstraintViolations().stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.joining(", "));
            throw new WebApplicationException("Validation failed: " + validationErrors, Response.Status.BAD_REQUEST);
        } catch (DuplicatePromoCodeException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion update failed due to duplicate code. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Duplicate promo code: " + e.getMessage(), Response.Status.CONFLICT);
        } catch (InvalidPromotionDataException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion update failed due to invalid data. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Invalid promotion data: " + e.getMessage(), Response.Status.BAD_REQUEST);
        } catch (AnnouncementNotFoundException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion not found for update. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Promotion not found with ID: " + id, Response.Status.NOT_FOUND);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion update failed. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while updating promotion", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a promotion (admin only: Center Admin, Manager)
     * @param id UUID of the promotion to delete
     * @return 200 OK with success message
     * @throws WebApplicationException with 404 if promotion not found, 403 for insufficient permissions
     * @traceability [REQ-017]
     */
    @DELETE
    @Path("/promotions/{id}")
    @RolesAllowed({"CENTER_ADMIN", "MANAGER"}) // RBAC enforcement per ARC-001 to ARC-005
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePromotion(@PathParam("id") UUID id) {
        try {
            logger.info("[PROCESS] " + TRACEABILITY_TAGS + " Deleting promotion ID: {} for user: {}", id, getCurrentUserId());
            // Validate idempotency key for mutation operation
            String idempotencyKey = httpHeaders.getHeaderString(IDEMPOTENCY_KEY_HEADER);
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                throw new ValidationException("Idempotency-Key header is required for mutation operations");
            }
            // Call service to delete promotion
            promotionService.deletePromotion(id, idempotencyKey);
            logger.info("[SUCCESS] " + TRACEABILITY_TAGS + " Promotion ID: {} deleted successfully", id);
            return Response.ok("{\"message\": \"Promotion deleted successfully\"}").build();
        } catch (AnnouncementNotFoundException e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion not found for deletion. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage());
            throw new WebApplicationException("Promotion not found with ID: " + id, Response.Status.NOT_FOUND);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] " + TRACEABILITY_TAGS + " Promotion deletion failed. Subsystem: " + SUBSYSTEM_NAME + ", Raw error: {}", e.getMessage(), e);
            throw new WebApplicationException("System error occurred while deleting promotion", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}