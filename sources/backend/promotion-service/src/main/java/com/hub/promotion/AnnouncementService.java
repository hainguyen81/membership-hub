package org.nlh4j.saas.membership_hub.promotion;

// [IMPORTS] Enterprise-grade dependencies aligned with Quarkus stack and security requirements
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.scheduler.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Enterprise service layer for managing system announcements.
 * Implements CRUD operations for announcements, automatic deactivation of expired content,
 * and strict input validation aligned with business requirements.
 *
 * <p><strong>Business Rules:</strong>
 * <ul>
 *   <li>Announcements have optional display time ranges (startDate to endDate)</li>
 *   <li>Announcements are automatically hidden after their end date via daily scheduled job</li>
 *   <li>Title maximum length: 150 characters, content maximum length: 2000 characters</li>
 *   <li>End date cannot be earlier than start date if both are specified</li>
 * </ul>
 *
 * @traceability [REQ-018], [DAT-009]
 */
@ApplicationScoped
public class AnnouncementService {

    // [CONSTANTS] All configuration and error messages declared at top level per enterprise clean code rules (no magic numbers/hardcoded strings)
    public static final int MAX_TITLE_LENGTH = 150; // [DAT-009] Maximum allowed length for announcement title
    public static final int MAX_CONTENT_LENGTH = 2000; // [DAT-009] Maximum allowed length for announcement content
    public static final String ERR_ANNOUNCEMENT_NOT_FOUND = "Announcement not found with ID: "; // [REQ-018] Error for missing announcement
    public static final String ERR_INVALID_DATE_RANGE = "End date cannot be earlier than start date"; // [DAT-009] Error for invalid date range
    public static final String ERR_TITLE_BLANK = "Announcement title cannot be blank"; // [REQ-018] Error for blank title
    public static final String ERR_CONTENT_BLANK = "Announcement content cannot be blank"; // [REQ-018] Error for blank content
    public static final String ERR_TITLE_TOO_LONG = "Announcement title exceeds maximum length of "; // [DAT-009] Error for title length violation
    public static final String ERR_CONTENT_TOO_LONG = "Announcement content exceeds maximum length of "; // [DAT-009] Error for content length violation
    public static final String SCHEDULED_JOB_DEACTIVATE = "Scheduled job to deactivate expired announcements"; // [REQ-018] Scheduled job identifier
    public static final String SCHEDULED_JOB_DEACTIVATED_COUNT = "Deactivated {} expired announcements"; // [REQ-018] Log message for deactivation count
    public static final String SCHEDULED_JOB_NO_EXPIRED = "No expired announcements found to deactivate"; // [REQ-018] Log message for no expired announcements

    // [LOGGING] Enterprise SLF4J logger for audit trail and debugging per NFR-006
    private static final Logger logger = LoggerFactory.getLogger(AnnouncementService.class);

    // [DEPENDENCY INJECTION] Quarkus Panache repository for database operations, aligned with DAT-009 schema
    @Inject
    AnnouncementRepository announcementRepository;

    /**
     * Creates a new system announcement with optional display time range.
     * Validates all input constraints per [REQ-018] and [DAT-009] before persisting to PostgreSQL.
     *
     * @param title Announcement title (max 150 characters, non-blank)
     * @param content Announcement content (max 2000 characters, non-blank)
     * @param startDate Optional start date for display period (can be null for immediate display)
     * @param endDate Optional end date for display period (must be >= startDate if provided, null for permanent display)
     * @param createdBy ID of user creating the announcement (for audit logging per NFR-006)
     * @return Created Announcement entity with generated UUID
     * @throws AnnouncementValidationException if input validation fails
     * @throws AnnouncementPersistenceException if database operation fails
     * @traceability [REQ-018], [DAT-009]
     */
    public Announcement createAnnouncement(String title, String content, LocalDate startDate, LocalDate endDate, String createdBy) {
        // [LOGGING] Log entry point with context for audit trail per NFR-006
        logger.info("[REQ-018] [ENTRY] Creating new announcement | Title: '{}' | StartDate: {} | EndDate: {} | CreatedBy: {}", 
                title, startDate, endDate, createdBy);
        try {
            // [REQ-018] [DAT-009] Validate title: non-blank and within maximum length constraint
            if (title == null || title.isBlank()) {
                logger.error("[REQ-018] [DAT-009] Validation failed: announcement title is blank");
                throw new AnnouncementValidationException(ERR_TITLE_BLANK);
            }
            if (title.length() > MAX_TITLE_LENGTH) {
                logger.error("[REQ-018] [DAT-009] Validation failed: title length {} exceeds max allowed {}", 
                        title.length(), MAX_TITLE_LENGTH);
                throw new AnnouncementValidationException(ERR_TITLE_TOO_LONG + MAX_TITLE_LENGTH);
            }

            // [REQ-018] [DAT-009] Validate content: non-blank and within maximum length constraint
            if (content == null || content.isBlank()) {
                logger.error("[REQ-018] [DAT-009] Validation failed: announcement content is blank");
                throw new AnnouncementValidationException(ERR_CONTENT_BLANK);
            }
            if (content.length() > MAX_CONTENT_LENGTH) {
                logger.error("[REQ-018] [DAT-009] Validation failed: content length {} exceeds max allowed {}", 
                        content.length(), MAX_CONTENT_LENGTH);
                throw new AnnouncementValidationException(ERR_CONTENT_TOO_LONG + MAX_CONTENT_LENGTH);
            }

            // [DAT-009] Validate date range: end date cannot be earlier than start date if both are provided
            if (endDate != null && startDate != null && endDate.isBefore(startDate)) {
                logger.error("[REQ-018] [DAT-009] Validation failed: end date {} is earlier than start date {}", endDate, startDate);
                throw new InvalidAnnouncementDateException(ERR_INVALID_DATE_RANGE);
            }

            // [REQ-018] Initialize new Announcement entity with validated data
            Announcement announcement = new Announcement();
            announcement.setTitle(title.trim()); // [NFR-003] Sanitize input to prevent XSS
            announcement.setContent(content.trim()); // [NFR-003] Sanitize input to prevent XSS
            announcement.setStartDate(startDate);
            announcement.setEndDate(endDate);
            announcement.setActive(true); // [REQ-018] New announcements are active by default
            announcement.setCreatedBy(createdBy);
            announcement.setUpdatedBy(createdBy);

            // [DAT-009] Persist to database using parameterized query (prevents SQL injection per NFR-003)
            announcementRepository.persist(announcement);

            // [REQ-018] Log successful creation with generated ID for audit trail per NFR-006
            logger.info("[REQ-018] [EXIT] Successfully created announcement | ID: {} | Title: '{}'", 
                    announcement.getAnnouncementId(), title);
            return announcement;
        } catch (AnnouncementValidationException | InvalidAnnouncementDateException e) {
            // [REQ-018] Re-throw validation exceptions directly to preserve error context for API response
            logger.error("[REQ-018] [DAT-009] Announcement creation failed due to validation error | Raw error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // [NFR-003] Catch unexpected persistence errors, log full context, preserve root cause for debugging
            logger.error("[REQ-018] [DAT-009] Failed to create announcement | Raw error: {}", e.getMessage(), e);
            throw new AnnouncementPersistenceException("Failed to create announcement", e);
        }
    }

    /**
     * Updates an existing system announcement.
     * Validates input and verifies announcement existence before updating per [REQ-018] and [DAT-009].
     *
     * @param announcementId UUID of the announcement to update
     * @param title Updated announcement title (null to keep existing)
     * @param content Updated announcement content (null to keep existing)
     * @param startDate Updated start date (null to keep existing)
     * @param endDate Updated end date (null to keep existing, must be >= startDate if provided)
     * @param isActive Updated active status (null to keep existing)
     * @param updatedBy ID of user updating the announcement (for audit logging per NFR-006)
     * @return Updated Announcement entity
     * @throws AnnouncementNotFoundException if announcement with given ID does not exist
     * @throws AnnouncementValidationException if input validation fails
     * @throws AnnouncementPersistenceException if database update fails
     * @traceability [REQ-018], [DAT-009]
     */
    public Announcement updateAnnouncement(UUID announcementId, String title, String content, LocalDate startDate, LocalDate endDate, Boolean isActive, String updatedBy) {
        // [LOGGING] Log entry point with context for audit
        logger.info("[REQ-018] [ENTRY] Updating announcement | ID: {} | UpdatedBy: {}", announcementId, updatedBy);
        try {
            // [DAT-009] Check if announcement exists using parameterized query (prevents SQLi)
            Announcement existingAnnouncement = announcementRepository.findById(announcementId);
            if (existingAnnouncement == null) {
                logger.error("[REQ-018] [DAT-009] Announcement not found for update | ID: {}", announcementId);
                throw new AnnouncementNotFoundException(ERR_ANNOUNCEMENT_NOT_FOUND + announcementId);
            }

            // [REQ-018] [DAT-009] Validate and update title if provided
            if (title != null && !title.isBlank()) {
                if (title.length() > MAX_TITLE_LENGTH) {
                    logger.error("[REQ-018] [DAT-009] Validation failed: updated title length {} exceeds max allowed {}", 
                            title.length(), MAX_TITLE_LENGTH);
                    throw new AnnouncementValidationException(ERR_TITLE_TOO_LONG + MAX_TITLE_LENGTH);
                }
                existingAnnouncement.setTitle(title.trim()); // [NFR-003] Sanitize input to prevent XSS
            }

            // [REQ-018] [DAT-009] Validate and update content if provided
            if (content != null && !content.isBlank()) {
                if (content.length() > MAX_CONTENT_LENGTH) {
                    logger.error("[REQ-018] [DAT-009] Validation failed: updated content length {} exceeds max allowed {}", 
                            content.length(), MAX_CONTENT_LENGTH);
                    throw new AnnouncementValidationException(ERR_CONTENT_TOO_LONG + MAX_CONTENT_LENGTH);
                }
                existingAnnouncement.setContent(content.trim()); // [NFR-003] Sanitize input to prevent XSS
            }

            // [DAT-009] Validate date range if either date is being updated
            LocalDate newStartDate = startDate != null ? startDate : existingAnnouncement.getStartDate();
            LocalDate newEndDate = endDate != null ? endDate : existingAnnouncement.getEndDate();
            if (newEndDate != null && newStartDate != null && newEndDate.isBefore(newStartDate)) {
                logger.error("[REQ-018] [DAT-009] Validation failed: updated end date {} is earlier than start date {}", newEndDate, newStartDate);
                throw new InvalidAnnouncementDateException(ERR_INVALID_DATE_RANGE);
            }
            existingAnnouncement.setStartDate(newStartDate);
            existingAnnouncement.setEndDate(newEndDate);

            // [REQ-018] Update active status if provided
            if (isActive != null) {
                existingAnnouncement.setActive(isActive);
            }

            // [NFR-006] Update audit fields for change tracking
            existingAnnouncement.setUpdatedBy(updatedBy);
            existingAnnouncement.setUpdatedAt(LocalDate.now());

            // [DAT-009] Persist updated entity to database using parameterized query
            announcementRepository.persist(existingAnnouncement);

            // [REQ-018] Log successful update for audit trail
            logger.info("[REQ-018] [EXIT] Successfully updated announcement | ID: {}", announcementId);
            return existingAnnouncement;
        } catch (AnnouncementNotFoundException | AnnouncementValidationException | InvalidAnnouncementDateException e) {
            // [REQ-018] Re-throw known business exceptions directly to preserve error context
            logger.error("[REQ-018] [DAT-009] Announcement update failed | Raw error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // [NFR-003] Catch unexpected errors, log full context, preserve root cause for debugging
            logger.error("[REQ-018] [DAT-009] Failed to update announcement | ID: {} | Raw error: {}", announcementId, e.getMessage(), e);
            throw new AnnouncementPersistenceException("Failed to update announcement with ID: " + announcementId, e);
        }
    }

    /**
     * Permanently deletes a system announcement.
     * Verifies announcement existence before deletion per [REQ-018] and [DAT-009].
     *
     * @param announcementId UUID of the announcement to delete
     * @param deletedBy ID of user deleting the announcement (for audit logging per NFR-006)
     * @throws AnnouncementNotFoundException if announcement with given ID does not exist
     * @throws AnnouncementPersistenceException if database deletion fails
     * @traceability [REQ-018], [DAT-009]
     */
    public void deleteAnnouncement(UUID announcementId, String deletedBy) {
        // [LOGGING] Log entry point for audit trail
        logger.info("[REQ-018] [ENTRY] Deleting announcement | ID: {} | DeletedBy: {}", announcementId, deletedBy);
        try {
            // [DAT-009] Verify announcement exists before deletion using parameterized query
            Announcement existingAnnouncement = announcementRepository.findById(announcementId);
            if (existingAnnouncement == null) {
                logger.error("[REQ-018] [DAT-009] Announcement not found for deletion | ID: {}", announcementId);
                throw new AnnouncementNotFoundException(ERR_ANNOUNCEMENT_NOT_FOUND + announcementId);
            }

            // [DAT-009] Delete announcement from database using parameterized query (prevents SQLi)
            announcementRepository.delete(existingAnnouncement);

            // [REQ-018] Log successful deletion for audit trail per NFR-006
            logger.info("[REQ-018] [EXIT] Successfully deleted announcement | ID: {}", announcementId);
        } catch (AnnouncementNotFoundException e) {
            // [REQ-018] Re-throw not found exception directly
            throw e;
        } catch (Exception e) {
            // [NFR-003] Catch unexpected errors, log full context, preserve root cause
            logger.error("[REQ-018] [DAT-009] Failed to delete announcement | ID: {} | Raw error: {}", announcementId, e.getMessage(), e);
            throw new AnnouncementPersistenceException("Failed to delete announcement with ID: " + announcementId, e);
        }
    }

    /**
     * Retrieves all active announcements visible to end users.
     * Filters announcements that are currently within their display period per [REQ-018] and [DAT-009].
     * Uses indexed database query for optimal performance with large datasets.
     *
     * @return List of active Announcement entities, ordered by start date descending (newest first)
     * @traceability [REQ-018], [DAT-009]
     */
    public List<Announcement> getActiveAnnouncements() {
        // [LOGGING] Log entry point for audit
        logger.info("[REQ-018] [ENTRY] Fetching all active announcements");
        try {
            // [DAT-009] Use indexed parameterized query to fetch active announcements, avoids in-memory filtering for performance
            // Query logic: active = true AND (endDate IS NULL OR endDate >= current date) ORDER BY startDate DESC
            List<Announcement> activeAnnouncements = announcementRepository.list(
                    "active = true AND (endDate IS NULL OR endDate >= ?1) ORDER BY startDate DESC, endDate DESC",
                    LocalDate.now()
            );
            // [REQ-018] Log exit with count for monitoring and audit
            logger.info("[REQ-018] [EXIT] Fetched {} active announcements", activeAnnouncements.size());
            return activeAnnouncements;
        } catch (Exception e) {
            // [NFR-003] Log error with full context, preserve root cause
            logger.error("[REQ-018] [DAT-009] Failed to fetch active announcements | Raw error: {}", e.getMessage(), e);
            throw new AnnouncementPersistenceException("Failed to fetch active announcements", e);
        }
    }

    /**
     * Retrieves all announcements (active and inactive) for administrative use.
     * Supports optional filtering by active status per [REQ-018].
     *
     * @param includeInactive If true, include inactive/expired announcements in the result
     * @return List of Announcement entities, ordered by creation date descending (newest first)
     * @traceability [REQ-018], [DAT-009]
     */
    public List<Announcement> getAllAnnouncements(boolean includeInactive) {
        // [LOGGING] Log entry point for audit
        logger.info("[REQ-018] [ENTRY] Fetching all announcements | IncludeInactive: {}", includeInactive);
        try {
            List<Announcement> announcements;
            if (includeInactive) {
                // [REQ-018] Fetch all announcements including inactive ones, ordered by creation date descending
                announcements = announcementRepository.list("ORDER BY createdAt DESC");
            } else {
                // [REQ-018] Reuse active announcements query for consistency
                announcements = getActiveAnnouncements();
            }
            // [REQ-018] Log exit with count for monitoring
            logger.info("[REQ-018] [EXIT] Fetched {} total announcements", announcements.size());
            return announcements;
        } catch (Exception e) {
            // [NFR-003] Log error with full context, preserve root cause
            logger.error("[REQ-018] [DAT-009] Failed to fetch all announcements | Raw error: {}", e.getMessage(), e);
            throw new AnnouncementPersistenceException("Failed to fetch announcements", e);
        }
    }

    /**
     * Scheduled job to automatically deactivate announcements that have passed their end date.
     * Runs daily at 2 AM to ensure expired announcements are hidden from user view per [REQ-018].
     * Uses indexed database queries for optimal performance with large datasets.
     *
     * @traceability [REQ-018], [DAT-009]
     */
    @Scheduled(every = "24h", delayed = "2h")
    public void deactivateExpiredAnnouncements() {
        // [LOGGING] Log job start for audit and monitoring per NFR-006
        logger.info("[REQ-018] [ENTRY] {}", SCHEDULED_JOB_DEACTIVATE);
        try {
            // [REQ-018] Get current date to compare with announcement end dates
            LocalDate currentDate = LocalDate.now();
            // [DAT-009] Use indexed query to fetch only active announcements with end date in the past (avoids full table scan)
            List<Announcement> expiredAnnouncements = announcementRepository.findActiveAnnouncementsWithEndDateBefore(currentDate);
            int deactivatedCount = 0;

            // [REQ-018] Deactivate each expired announcement
            for (Announcement announcement : expiredAnnouncements) {
                announcement.setActive(false); // [REQ-018] Mark as inactive to hide from user view
                announcement.setUpdatedAt(currentDate); // [NFR-006] Update audit timestamp
                announcementRepository.persist(announcement); // [DAT-009] Persist update to database
                deactivatedCount++;
            }

            // [REQ-018] Log deactivation result for audit and monitoring
            if (deactivatedCount > 0) {
                logger.info("[REQ-018] [EXIT] " + SCHEDULED_JOB_DEACTIVATED_COUNT, deactivatedCount);
            } else {
                logger.debug("[REQ-018] [EXIT] {}", SCHEDULED_JOB_NO_EXPIRED);
            }
        } catch (Exception e) {
            // [NFR-006] Log critical job failure with full context for ops alerting
            logger.error("[REQ-018] [DAT-009] Scheduled deactivation job failed | Raw error: {}", e.getMessage(), e);
            // Rethrow to trigger Quarkus scheduler retry mechanism for reliability
            throw new RuntimeException("Failed to execute expired announcement deactivation job", e);
        }
    }

    // [CUSTOM EXCEPTION CLASSES] Aligned with enterprise error handling standards, preserve root cause on rethrow per global governance rules
    public static class AnnouncementValidationException extends RuntimeException {
        public AnnouncementValidationException(String message) { super(message); }
        public AnnouncementValidationException(String message, Throwable cause) { super(message, cause); }
    }

    public static class AnnouncementNotFoundException extends RuntimeException {
        public AnnouncementNotFoundException(String message) { super(message); }
        public AnnouncementNotFoundException(String message, Throwable cause) { super(message, cause); }
    }

    public static class AnnouncementPersistenceException extends RuntimeException {
        public AnnouncementPersistenceException(String message) { super(message); }
        public AnnouncementPersistenceException(String message, Throwable cause) { super(message, cause); }
    }

    public static class InvalidAnnouncementDateException extends RuntimeException {
        public InvalidAnnouncementDateException(String message) { super(message); }
    }

    // [JPA ENTITY] Aligned with PostgreSQL schema definition [DAT-009]
    @Entity
    @Table(name = "announcements")
    public static class Announcement {
        // [DAT-009] Primary key: auto-generated UUID for announcement record
        @Id
        @GeneratedValue
        private UUID announcementId;

        // [DAT-009] Announcement title: non-null, max 150 characters
        @NotBlank(message = ERR_TITLE_BLANK)
        @Size(max = MAX_TITLE_LENGTH, message = ERR_TITLE_TOO_LONG + MAX_TITLE_LENGTH)
        @Column(nullable = false, length = MAX_TITLE_LENGTH)
        private String title;

        // [DAT-009] Announcement content: non-null, max 2000 characters
        @NotBlank(message = ERR_CONTENT_BLANK)
        @Size(max = MAX_CONTENT_LENGTH, message = ERR_CONTENT_TOO_LONG + MAX_CONTENT_LENGTH)
        @Column(nullable = false, length = MAX_CONTENT_LENGTH)
        private String content;

        // [DAT-009] Optional start date for announcement display period
        @Column(name = "start_date")
        private LocalDate startDate;

        // [DAT-009] Optional end date for announcement display period, must be >= startDate if present
        @Column(name = "end_date")
        private LocalDate endDate;

        // [REQ-018] Flag to indicate if announcement is currently active/visible to end users
        @Column(nullable = false)
        private Boolean active = true;

        // [NFR-006] Audit field: ID of user who created the announcement
        @Column(name = "created_by", nullable = false, updatable = false)
        private String createdBy;

        // [NFR-006] Audit field: timestamp of announcement creation
        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDate createdAt = LocalDate.now();

        // [NFR-006] Audit field: ID of user who last updated the announcement
        @Column(name = "updated_by", nullable = false)
        private String updatedBy;

        // [NFR-006] Audit field: timestamp of last announcement update
        @Column(name = "updated_at", nullable = false)
        private LocalDate updatedAt = LocalDate.now();

        // [JPA] Getters and setters required for entity persistence and JSON serialization
        public UUID getAnnouncementId() { return announcementId; }
        public void setAnnouncementId(UUID announcementId) { this.announcementId = announcementId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        public LocalDate getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
        public String getUpdatedBy() { return updatedBy; }
        public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
        public LocalDate getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDate updatedAt) { this.updatedAt = updatedAt; }
    }

    // [PANACHE REPOSITORY] Handles all database operations for Announcement entity, aligned with [DAT-009] schema and index requirements
    public interface AnnouncementRepository extends PanacheRepository<Announcement> {
        // [DAT-009] Custom query using indexed columns (start_date, end_date) for optimal performance, no full table scan
        List<Announcement> findActiveAnnouncementsWithEndDateBefore(LocalDate date);
        // [REQ-018] Custom query to fetch active announcements ordered by start date for user-facing displays
        List<Announcement> findActiveOrderByStartDateDesc();
    }
}