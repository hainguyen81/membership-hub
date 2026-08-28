package org.nlh4j.saas.membership_hub.promotion;

// ====================== ENTERPRISE IMPORT LAYER ======================
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.nlh4j.saas.membership_hub.auth.RbacService;
import org.nlh4j.saas.membership_hub.entity.Announcement;
import org.nlh4j.saas.membership_hub.entity.Promotion;
import org.nlh4j.saas.membership_hub.entity.Role;
import org.nlh4j.saas.membership_hub.entity.User;
import org.nlh4j.saas.membership_hub.exception.PromotionCodeAlreadyExistsException;
import org.nlh4j.saas.membership_hub.exception.UnauthorizedAccessException;
import org.nlh4j.saas.membership_hub.exception.ValidationException;
import org.nlh4j.saas.membership_hub.notification.NotificationService;
import org.nlh4j.saas.membership_hub.repository.AnnouncementRepository;
import org.nlh4j.saas.membership_hub.repository.PromotionRepository;
import org.nlh4j.saas.membership_hub.security.SecurityContext;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import io.quarkus.scheduler.Scheduled;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for managing promotions and system announcements.
 * Implements business logic for CRUD operations, input validation, XSS sanitization,
 * RBAC access control, and automatic expiration of announcements.
 *
 * @traceability [REQ-017], [REQ-018], [DAT-009]
 * @author Enterprise Core Engineering Team
 * @version 1.0.0
 */
@ApplicationScoped
public class AnnouncementService {

    // ====================== ENTERPRISE CONSTANTS (NO HARDCODED LITERALS IN LOGIC) ======================
    // [REQ-017] Promotion validation constants
    public static final int MIN_DISCOUNT_PERCENT = 0;
    public static final int MAX_DISCOUNT_PERCENT = 100;
    public static final int MAX_PROMO_CODE_LENGTH = 50;
    public static final int MAX_PROMO_DESCRIPTION_LENGTH = 500;
    // [REQ-018] Announcement validation constants
    public static final int MAX_ANNOUNCEMENT_TITLE_LENGTH = 150;
    public static final int MAX_ANNOUNCEMENT_CONTENT_LENGTH = 2000;
    // [EXC-003] Notification retry constants
    public static final int MAX_NOTIFICATION_RETRY_COUNT = 3;
    public static final long NOTIFICATION_RETRY_DELAY_MS = 300000; // 5 minutes
    // Scheduled job constants
    public static final String SCHEDULED_JOB_CRON = "0 0 0 * * ?"; // Run daily at midnight UTC
    // ====================== END OF CONSTANTS ======================

    // Enterprise standard SLF4J logger for audit and process tracing [NFR-006]
    private static final Logger LOG = Logger.getLogger(AnnouncementService.class);

    // Injected dependencies via Quarkus CDI [ARC-010]
    @Inject
    PromotionRepository promotionRepository;
    @Inject
    AnnouncementRepository announcementRepository;
    @Inject
    RbacService rbacService;
    @Inject
    NotificationService notificationService;
    @Inject
    SecurityContext securityContext;

    /**
     * Validates that the current user has the required role to access promotion/announcement management endpoints.
     * Only Center Admin and Manager roles are allowed per [REQ-017], [REQ-018].
     *
     * @param currentUser The currently authenticated user
     * @throws UnauthorizedAccessException if the user does not have the required role
     */
    private void validateRbac(User currentUser) {
        LOG.debug("[PROMOTION_SERVICE] [REQ-017] [REQ-018] Validating RBAC for user: {}", currentUser.getUserId());
        boolean hasAccess = rbacService.hasRole(currentUser, Role.CENTER_ADMIN, Role.MANAGER);
        if (!hasAccess) {
            LOG.warn("[PROMOTION_SERVICE] [REQ-017] [REQ-018] Unauthorized access attempt by user: {} with role: {}", currentUser.getUserId(), currentUser.getRole());
            throw new UnauthorizedAccessException("Access denied. Only Center Admin and Manager can manage promotions and announcements.");
        }
        LOG.debug("[PROMOTION_SERVICE] [REQ-017] [REQ-018] RBAC validation passed for user: {}", currentUser.getUserId());
    }

    /**
     * Sanitizes user input to prevent XSS attacks by removing malicious HTML/script tags [NFR-003].
     * Uses Jsoup with a basic whitelist to allow only safe HTML elements.
     *
     * @param input The raw user input string
     * @return The sanitized safe string
     */
    private String sanitizeInput(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        // Clean input with basic whitelist (allows only safe tags like b, i, p, etc.)
        return Jsoup.clean(input, Whitelist.basic());
    }

    /**
     * Validates promotion business rules per [REQ-017].
     *
     * @param promotion The promotion entity to validate
     * @throws ValidationException if any validation rule is violated
     */
    private void validatePromotion(Promotion promotion) {
        LOG.debug("[PROMOTION_SERVICE] [REQ-017] Validating promotion data for code: {}", promotion.getCode());
        // Validate discount percentage range [REQ-017]
        if (promotion.getDiscountPercent() < MIN_DISCOUNT_PERCENT || promotion.getDiscountPercent() > MAX_DISCOUNT_PERCENT) {
            LOG.warn("[PROMOTION_SERVICE] [REQ-017] Invalid discount percentage: {} for promo code: {}", promotion.getDiscountPercent(), promotion.getCode());
            throw new ValidationException("Discount percentage must be between " + MIN_DISCOUNT_PERCENT + " and " + MAX_DISCOUNT_PERCENT);
        }
        // Validate date range: end date must be >= start date if end date is provided [REQ-017]
        if (promotion.getEndDate() != null && promotion.getEndDate().isBefore(promotion.getStartDate())) {
            LOG.warn("[PROMOTION_SERVICE] [REQ-017] Invalid date range for promo code: {}: startDate={}, endDate={}", promotion.getCode(), promotion.getStartDate(), promotion.getEndDate());
            throw new ValidationException("End date must be greater than or equal to start date");
        }
        // Validate promo code length [REQ-017]
        if (promotion.getCode().length() > MAX_PROMO_CODE_LENGTH) {
            LOG.warn("[PROMOTION_SERVICE] [REQ-017] Promo code exceeds max length: {} for code: {}", MAX_PROMO_CODE_LENGTH, promotion.getCode());
            throw new ValidationException("Promotion code must not exceed " + MAX_PROMO_CODE_LENGTH + " characters");
        }
        // Validate description length if provided [REQ-017]
        if (promotion.getDescription() != null && promotion.getDescription().length() > MAX_PROMO_DESCRIPTION_LENGTH) {
            LOG.warn("[PROMOTION_SERVICE] [REQ-017] Promo description exceeds max length: {} for code: {}", MAX_PROMO_DESCRIPTION_LENGTH, promotion.getCode());
            throw new ValidationException("Promotion description must not exceed " + MAX_PROMO_DESCRIPTION_LENGTH + " characters");
        }
        LOG.debug("[PROMOTION_SERVICE] [REQ-017] Promotion validation passed for code: {}", promotion.getCode());
    }

    /**
     * Validates announcement business rules per [REQ-018].
     *
     * @param announcement The announcement entity to validate
     * @throws ValidationException if any validation rule is violated
     */
    private void validateAnnouncement(Announcement announcement) {
        LOG.debug("[PROMOTION_SERVICE] [REQ-018] Validating announcement data for title: {}", announcement.getTitle());
        // Validate title length and presence [REQ-018]
        if (announcement.getTitle() == null || announcement.getTitle().isBlank() || announcement.getTitle().length() > MAX_ANNOUNCEMENT_TITLE_LENGTH) {
            LOG.warn("[PROMOTION_SERVICE] [REQ-018] Invalid announcement title length: {} for title: {}", announcement.getTitle() != null ? announcement.getTitle().length() : 0, announcement.getTitle());
            throw new ValidationException("Announcement title is required and must not exceed " + MAX_ANNOUNCEMENT_TITLE_LENGTH + " characters");
        }
        // Validate content length and presence [REQ-018]
        if (announcement.getContent() == null || announcement.getContent().isBlank() || announcement.getContent().length() > MAX_ANNOUNCEMENT_CONTENT_LENGTH) {
            LOG.warn("[PROMOTION_SERVICE] [REQ-018] Invalid announcement content length: {} for title: {}", announcement.getContent() != null ? announcement.getContent().length() : 0, announcement.getTitle());
            throw new ValidationException("Announcement content is required and must not exceed " + MAX_ANNOUNCEMENT_CONTENT_LENGTH + " characters");
        }
        // Validate date range: end date must be >= start date if end date is provided [REQ-018]
        if (announcement.getEndDate() != null && announcement.getEndDate().isBefore(announcement.getStartDate())) {
            LOG.warn("[PROMOTION_SERVICE] [REQ-018] Invalid date range for announcement: {}: startDate={}, endDate={}", announcement.getTitle(), announcement.getStartDate(), announcement.getEndDate());
            throw new ValidationException("Announcement end date must be greater than or equal to start date");
        }
        LOG.debug("[PROMOTION_SERVICE] [REQ-018] Announcement validation passed for title: {}", announcement.getTitle());
    }

    // ====================== PROMOTION MANAGEMENT METHODS [REQ-017] ======================
    /**
     * Creates a new promotion with validated data and sanitized input.
     * Only accessible by Center Admin and Manager roles per [REQ-017].
     *
     * @param promotion The promotion entity to create
     * @return The created promotion entity
     * @throws UnauthorizedAccessException if the current user does not have permission
     * @throws ValidationException if the promotion data is invalid
     * @throws PromotionCodeAlreadyExistsException if the promo code already exists
     */
    @Transactional
    public Promotion createPromotion(Promotion promotion) {
        String operation = "CREATE_PROMOTION";
        User currentUser = securityContext.getCurrentUser();
        LOG.info("[PROMOTION_SERVICE] [REQ-017] Starting operation: {} for user: {}", operation, currentUser.getUserId());
        try {
            // Step 1: Validate RBAC access [ARC-001, ARC-002]
            validateRbac(currentUser);
            // Step 2: Sanitize all user input to prevent XSS [NFR-003]
            promotion.setCode(sanitizeInput(promotion.getCode()));
            if (promotion.getDescription() != null) {
                promotion.setDescription(sanitizeInput(promotion.getDescription()));
            }
            // Step 3: Validate business rules [REQ-017]
            validatePromotion(promotion);
            // Step 4: Check for duplicate promo code (unique constraint enforced at DB layer via prepared statements [NFR-003])
            if (promotionRepository.existsByCode(promotion.getCode())) {
                LOG.warn("[PROMOTION_SERVICE] [REQ-017] Duplicate promo code detected: {}", promotion.getCode());
                throw new PromotionCodeAlreadyExistsException("Promotion code already exists: " + promotion.getCode());
            }
            // Step 5: Set default active status
            promotion.setActive(true);
            // Step 6: Persist promotion using Hibernate prepared statements (prevents SQL injection [NFR-003])
            Promotion createdPromotion = promotionRepository.persist(promotion);
            // Step 7: Send notification to relevant users about new promotion [REQ-016]
            notificationService.sendPromotionNotification(createdPromotion);
            LOG.info("[PROMOTION_SERVICE] [REQ-017] Operation {} completed successfully. Created promotion ID: {}", operation, createdPromotion.getPromoId());
            return createdPromotion;
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-017] Operation {} failed for user: {}. Raw error: {}", operation, currentUser.getUserId(), e.getMessage(), e);
            // Re-throw custom exception with original cause to preserve stack trace [GLOBAL_GOVERNANCE_MATRIX 0.3]
            if (e instanceof PromotionCodeAlreadyExistsException || e instanceof ValidationException || e instanceof UnauthorizedAccessException) {
                throw e;
            }
            throw new RuntimeException("Failed to create promotion", e);
        }
    }

    /**
     * Updates an existing promotion with validated data and sanitized input.
     * Only accessible by Center Admin and Manager roles per [REQ-017].
     *
     * @param promoId The ID of the promotion to update
     * @param updatedPromotion The updated promotion data
     * @return The updated promotion entity
     * @throws EntityNotFoundException if the promotion does not exist
     * @throws UnauthorizedAccessException if the current user does not have permission
     * @throws ValidationException if the updated data is invalid
     * @throws PromotionCodeAlreadyExistsException if the new promo code conflicts with an existing one
     */
    @Transactional
    public Promotion updatePromotion(UUID promoId, Promotion updatedPromotion) {
        String operation = "UPDATE_PROMOTION";
        User currentUser = securityContext.getCurrentUser();
        LOG.info("[PROMOTION_SERVICE] [REQ-017] Starting operation: {} for user: {} for promo ID: {}", operation, currentUser.getUserId(), promoId);
        try {
            // Step 1: Validate RBAC access [ARC-001, ARC-002]
            validateRbac(currentUser);
            // Step 2: Fetch existing promotion from database (using prepared statement query [NFR-003])
            Promotion existingPromotion = promotionRepository.findById(promoId)
                    .orElseThrow(() -> {
                        LOG.warn("[PROMOTION_SERVICE] [REQ-017] Promotion not found for ID: {}", promoId);
                        return new EntityNotFoundException("Promotion not found with ID: " + promoId);
                    });
            // Step 3: Sanitize all user input to prevent XSS [NFR-003]
            updatedPromotion.setCode(sanitizeInput(updatedPromotion.getCode()));
            if (updatedPromotion.getDescription() != null) {
                updatedPromotion.setDescription(sanitizeInput(updatedPromotion.getDescription()));
            }
            // Step 4: Validate business rules [REQ-017]
            validatePromotion(updatedPromotion);
            // Step 5: Check for duplicate promo code if it's changed
            if (!existingPromotion.getCode().equals(updatedPromotion.getCode()) && promotionRepository.existsByCode(updatedPromotion.getCode())) {
                LOG.warn("[PROMOTION_SERVICE] [REQ-017] Duplicate promo code detected during update: {}", updatedPromotion.getCode());
                throw new PromotionCodeAlreadyExistsException("Promotion code already exists: " + updatedPromotion.getCode());
            }
            // Step 6: Update fields
            existingPromotion.setCode(updatedPromotion.getCode());
            existingPromotion.setDiscountPercent(updatedPromotion.getDiscountPercent());
            existingPromotion.setStartDate(updatedPromotion.getStartDate());
            existingPromotion.setEndDate(updatedPromotion.getEndDate());
            existingPromotion.setDescription(updatedPromotion.getDescription());
            existingPromotion.setActive(updatedPromotion.isActive());
            // Step 7: Persist updates using Hibernate prepared statements [NFR-003]
            Promotion updated = promotionRepository.persist(existingPromotion);
            // Step 8: Send notification about updated promotion [REQ-016]
            notificationService.sendPromotionUpdateNotification(updated);
            LOG.info("[PROMOTION_SERVICE] [REQ-017] Operation {} completed successfully for promo ID: {}", operation, promoId);
            return updated;
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-017] Operation {} failed for user: {} for promo ID: {}. Raw error: {}", operation, currentUser.getUserId(), promoId, e.getMessage(), e);
            if (e instanceof EntityNotFoundException || e instanceof PromotionCodeAlreadyExistsException || e instanceof ValidationException || e instanceof UnauthorizedAccessException) {
                throw e;
            }
            throw new RuntimeException("Failed to update promotion", e);
        }
    }

    /**
     * Deletes a promotion by ID.
     * Only accessible by Center Admin and Manager roles per [REQ-017].
     *
     * @param promoId The ID of the promotion to delete
     * @throws EntityNotFoundException if the promotion does not exist
     * @throws UnauthorizedAccessException if the current user does not have permission
     */
    @Transactional
    public void deletePromotion(UUID promoId) {
        String operation = "DELETE_PROMOTION";
        User currentUser = securityContext.getCurrentUser();
        LOG.info("[PROMOTION_SERVICE] [REQ-017] Starting operation: {} for user: {} for promo ID: {}", operation, currentUser.getUserId(), promoId);
        try {
            // Step 1: Validate RBAC access [ARC-001, ARC-002]
            validateRbac(currentUser);
            // Step 2: Check if promotion exists
            if (!promotionRepository.existsById(promoId)) {
                LOG.warn("[PROMOTION_SERVICE] [REQ-017] Promotion not found for deletion, ID: {}", promoId);
                throw new EntityNotFoundException("Promotion not found with ID: " + promoId);
            }
            // Step 3: Delete promotion using Hibernate prepared statement delete query [NFR-003]
            promotionRepository.deleteById(promoId);
            LOG.info("[PROMOTION_SERVICE] [REQ-017] Operation {} completed successfully for promo ID: {}", operation, promoId);
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-017] Operation {} failed for user: {} for promo ID: {}. Raw error: {}", operation, currentUser.getUserId(), promoId, e.getMessage(), e);
            if (e instanceof EntityNotFoundException || e instanceof UnauthorizedAccessException) {
                throw e;
            }
            throw new RuntimeException("Failed to delete promotion", e);
        }
    }

    /**
     * Retrieves all active promotions (current date is within start and end date, or end date is null).
     * Only accessible by Center Admin and Manager roles per [REQ-017].
     *
     * @return List of active promotions
     * @throws UnauthorizedAccessException if the current user does not have permission
     */
    @Transactional
    public List<Promotion> getActivePromotions() {
        String operation = "GET_ACTIVE_PROMOTIONS";
        User currentUser = securityContext.getCurrentUser();
        LOG.info("[PROMOTION_SERVICE] [REQ-017] Starting operation: {} for user: {}", operation, currentUser.getUserId());
        try {
            // Step 1: Validate RBAC access [ARC-001, ARC-002]
            validateRbac(currentUser);
            // Step 2: Fetch active promotions using parameterized query (prevents SQL injection [NFR-003])
            LocalDate currentDate = LocalDate.now();
            List<Promotion> activePromotions = promotionRepository.findActivePromotions(currentDate);
            LOG.info("[PROMOTION_SERVICE] [REQ-017] Operation {} completed successfully. Retrieved {} active promotions for user: {}", operation, activePromotions.size(), currentUser.getUserId());
            return activePromotions;
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-017] Operation {} failed for user: {}. Raw error: {}", operation, currentUser.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve active promotions", e);
        }
    }

    // ====================== ANNOUNCEMENT MANAGEMENT METHODS [REQ-018] ======================
    /**
     * Creates a new system announcement with validated data and sanitized input.
     * Only accessible by Center Admin and Manager roles per [REQ-018].
     *
     * @param announcement The announcement entity to create
     * @return The created announcement entity
     * @throws UnauthorizedAccessException if the current user does not have permission
     * @throws ValidationException if the announcement data is invalid
     */
    @Transactional
    public Announcement createAnnouncement(Announcement announcement) {
        String operation = "CREATE_ANNOUNCEMENT";
        User currentUser = securityContext.getCurrentUser();
        LOG.info("[PROMOTION_SERVICE] [REQ-018] Starting operation: {} for user: {}", operation, currentUser.getUserId());
        try {
            // Step 1: Validate RBAC access [ARC-001, ARC-002]
            validateRbac(currentUser);
            // Step 2: Sanitize all user input to prevent XSS [NFR-003]
            announcement.setTitle(sanitizeInput(announcement.getTitle()));
            announcement.setContent(sanitizeInput(announcement.getContent()));
            // Step 3: Validate business rules [REQ-018]
            validateAnnouncement(announcement);
            // Step 4: Set default active status
            announcement.setActive(true);
            // Step 5: Persist announcement using Hibernate prepared statements (prevents SQL injection [NFR-003])
            Announcement createdAnnouncement = announcementRepository.persist(announcement);
            // Step 6: Send notification to all users about new announcement [REQ-016]
            notificationService.sendAnnouncementNotification(createdAnnouncement);
            LOG.info("[PROMOTION_SERVICE] [REQ-018] Operation {} completed successfully. Created announcement ID: {}", operation, createdAnnouncement.getAnnouncementId());
            return createdAnnouncement;
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-018] Operation {} failed for user: {}. Raw error: {}", operation, currentUser.getUserId(), e.getMessage(), e);
            if (e instanceof ValidationException || e instanceof UnauthorizedAccessException) {
                throw e;
            }
            throw new RuntimeException("Failed to create announcement", e);
        }
    }

    /**
     * Updates an existing system announcement with validated data and sanitized input.
     * Only accessible by Center Admin and Manager roles per [REQ-018].
     *
     * @param announcementId The ID of the announcement to update
     * @param updatedAnnouncement The updated announcement data
     * @return The updated announcement entity
     * @throws EntityNotFoundException if the announcement does not exist
     * @throws UnauthorizedAccessException if the current user does not have permission
     * @throws ValidationException if the updated data is invalid
     */
    @Transactional
    public Announcement updateAnnouncement(UUID announcementId, Announcement updatedAnnouncement) {
        String operation = "UPDATE_ANNOUNCEMENT";
        User currentUser = securityContext.getCurrentUser();
        LOG.info("[PROMOTION_SERVICE] [REQ-018] Starting operation: {} for user: {} for announcement ID: {}", operation, currentUser.getUserId(), announcementId);
        try {
            // Step 1: Validate RBAC access [ARC-001, ARC-002]
            validateRbac(currentUser);
            // Step 2: Fetch existing announcement from database (using prepared statement query [NFR-003])
            Announcement existingAnnouncement = announcementRepository.findById(announcementId)
                    .orElseThrow(() -> {
                        LOG.warn("[PROMOTION_SERVICE] [REQ-018] Announcement not found for ID: {}", announcementId);
                        return new EntityNotFoundException("Announcement not found with ID: " + announcementId);
                    });
            // Step 3: Sanitize all user input to prevent XSS [NFR-003]
            updatedAnnouncement.setTitle(sanitizeInput(updatedAnnouncement.getTitle()));
            updatedAnnouncement.setContent(sanitizeInput(updatedAnnouncement.getContent()));
            // Step 4: Validate business rules [REQ-018]
            validateAnnouncement(updatedAnnouncement);
            // Step 5: Update fields
            existingAnnouncement.setTitle(updatedAnnouncement.getTitle());
            existingAnnouncement.setContent(updatedAnnouncement.getContent());
            existingAnnouncement.setStartDate(updatedAnnouncement.getStartDate());
            existingAnnouncement.setEndDate(updatedAnnouncement.getEndDate());
            existingAnnouncement.setActive(updatedAnnouncement.isActive());
            // Step 6: Persist updates using Hibernate prepared statements [NFR-003]
            Announcement updated = announcementRepository.persist(existingAnnouncement);
            // Step 7: Send notification about updated announcement [REQ-016]
            notificationService.sendAnnouncementUpdateNotification(updated);
            LOG.info("[PROMOTION_SERVICE] [REQ-018] Operation {} completed successfully for announcement ID: {}", operation, announcementId);
            return updated;
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-018] Operation {} failed for user: {} for announcement ID: {}. Raw error: {}", operation, currentUser.getUserId(), announcementId, e.getMessage(), e);
            if (e instanceof EntityNotFoundException || e instanceof ValidationException || e instanceof UnauthorizedAccessException) {
                throw e;
            }
            throw new RuntimeException("Failed to update announcement", e);
        }
    }

    /**
     * Deletes an announcement by ID.
     * Only accessible by Center Admin and Manager roles per [REQ-018].
     *
     * @param announcementId The ID of the announcement to delete
     * @throws EntityNotFoundException if the announcement does not exist
     * @throws UnauthorizedAccessException if the current user does not have permission
     */
    @Transactional
    public void deleteAnnouncement(UUID announcementId) {
        String operation = "DELETE_ANNOUNCEMENT";
        User currentUser = securityContext.getCurrentUser();
        LOG.info("[PROMOTION_SERVICE] [REQ-018] Starting operation: {} for user: {} for announcement ID: {}", operation, currentUser.getUserId(), announcementId);
        try {
            // Step 1: Validate RBAC access [ARC-001, ARC-002]
            validateRbac(currentUser);
            // Step 2: Check if announcement exists
            if (!announcementRepository.existsById(announcementId)) {
                LOG.warn("[PROMOTION_SERVICE] [REQ-018] Announcement not found for deletion, ID: {}", announcementId);
                throw new EntityNotFoundException("Announcement not found with ID: " + announcementId);
            }
            // Step 3: Delete announcement using Hibernate prepared statement delete query [NFR-003]
            announcementRepository.deleteById(announcementId);
            LOG.info("[PROMOTION_SERVICE] [REQ-018] Operation {} completed successfully for announcement ID: {}", operation, announcementId);
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-018] Operation {} failed for user: {} for announcement ID: {}. Raw error: {}", operation, currentUser.getUserId(), announcementId, e.getMessage(), e);
            if (e instanceof EntityNotFoundException || e instanceof UnauthorizedAccessException) {
                throw e;
            }
            throw new RuntimeException("Failed to delete announcement", e);
        }
    }

    /**
     * Retrieves all active announcements (current date is within start and end date, or end date is null, and is active).
     * Only accessible by Center Admin and Manager roles per [REQ-018].
     *
     * @return List of active announcements
     * @throws UnauthorizedAccessException if the current user does not have permission
     */
    @Transactional
    public List<Announcement> getActiveAnnouncements() {
        String operation = "GET_ACTIVE_ANNOUNCEMENTS";
        User currentUser = securityContext.getCurrentUser();
        LOG.info("[PROMOTION_SERVICE] [REQ-018] Starting operation: {} for user: {}", operation, currentUser.getUserId());
        try {
            // Step 1: Validate RBAC access [ARC-001, ARC-002]
            validateRbac(currentUser);
            // Step 2: Fetch active announcements using parameterized query (prevents SQL injection [NFR-003])
            LocalDate currentDate = LocalDate.now();
            List<Announcement> activeAnnouncements = announcementRepository.findActiveAnnouncements(currentDate);
            LOG.info("[PROMOTION_SERVICE] [REQ-018] Operation {} completed successfully. Retrieved {} active announcements for user: {}", operation, activeAnnouncements.size(), currentUser.getUserId());
            return activeAnnouncements;
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-018] Operation {} failed for user: {}. Raw error: {}", operation, currentUser.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve active announcements", e);
        }
    }

    // ====================== SCHEDULED JOB FOR AUTOMATIC ANNOUNCEMENT EXPIRATION [REQ-018] ======================
    /**
     * Scheduled job that runs daily at midnight to automatically deactivate expired announcements.
     * Ensures announcements are hidden after their end date per [REQ-018].
     * Runs with system privileges, no RBAC check required.
     */
    @Scheduled(cron = SCHEDULED_JOB_CRON)
    @Transactional
    public void deactivateExpiredAnnouncements() {
        String operation = "DEACTIVATE_EXPIRED_ANNOUNCEMENTS";
        LOG.info("[PROMOTION_SERVICE] [REQ-018] Starting scheduled operation: {}", operation);
        try {
            LocalDate currentDate = LocalDate.now();
            // Fetch all active announcements that have expired (end date < current date) using parameterized query [NFR-003]
            List<Announcement> expiredAnnouncements = announcementRepository.findExpiredActiveAnnouncements(currentDate);
            if (expiredAnnouncements.isEmpty()) {
                LOG.info("[PROMOTION_SERVICE] [REQ-018] No expired announcements found to deactivate");
                return;
            }
            // Deactivate all expired announcements
            for (Announcement announcement : expiredAnnouncements) {
                announcement.setActive(false);
                announcementRepository.persist(announcement);
                LOG.debug("[PROMOTION_SERVICE] [REQ-018] Deactivated expired announcement ID: {}, title: {}", announcement.getAnnouncementId(), announcement.getTitle());
            }
            LOG.info("[PROMOTION_SERVICE] [REQ-018] Scheduled operation {} completed successfully. Deactivated {} expired announcements", operation, expiredAnnouncements.size());
        } catch (Exception e) {
            LOG.error("[PROMOTION_SERVICE] [REQ-018] Scheduled operation {} failed. Raw error: {}", operation, e.getMessage(), e);
            // Throw runtime exception to trigger alert for operations team per [EXC-010]
            throw new RuntimeException("Failed to deactivate expired announcements", e);
        }
    }
}