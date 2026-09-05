package org.nlh4j.saas.membership_hub.center;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.nlh4j.saas.membership_hub.center.model.Center;
import org.nlh4j.saas.membership_hub.center.repository.CenterRepository;

import java.util.List;
import java.util.UUID;

/**
 * Service class for managing Center entities with CRUD operations and RBAC compliance.
 * Traceability Tags: [REQ-005], [ARC-002]
 */
@Service
public class CenterService {

    private static final Logger logger = LoggerFactory.getLogger(CenterService.class);

    /* --------------------------------------------------------------------- */
    /* CONSTANTS – Anti‑Magic‑Numbers enforcement – all configuration values   */
    /* --------------------------------------------------------------------- */
    /** Error message template when a center is not found. */
    public static final String CENTER_NOT_FOUND = "Center not found with id: %s";
    /** Error message template when a duplicate tax ID is supplied. */
    public static final String CENTER_DUPLICATE_TAX_ID = "Duplicate tax ID: %s";
    /** Error message template when a delete is attempted on a center with related data. */
    public static final String CENTER_DELETE_WITH_ASSOCIATED_DATA = "Attempt to delete center with associated enrollments or courses: %s";

    /* --------------------------------------------------------------------- */
    /* DEPENDENCY INJECTION – Repository layer for data access               */
    /* --------------------------------------------------------------------- */
    @Autowired
    private CenterRepository centerRepository;

    /**
     * Create a new Center.
     * Traceability Tags: [REQ-005], [ARC-002]
     *
     * @param center Center entity to create
     * @return created Center
     */
    @Transactional
    public Center createCenter(Center center) {
        logger.info("[ENTRY] createCenter called with centerName: {}", center.getName());
        try {
            // Validate tax ID uniqueness – prevents duplicate centers
            if (centerRepository.existsByTaxId(center.getTaxId())) {
                String errorMsg = String.format(CENTER_DUPLICATE_TAX_ID, center.getTaxId());
                logger.error("[ERROR] [REQ-005] [ARC-002] Duplicate tax ID encountered: {}", errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }
            Center saved = centerRepository.save(center);
            logger.debug("[DEBUG] Center created with id: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            // Comprehensive exception logging – preserves original cause and Tag IDs
            logger.error("[ERROR] [REQ-005] [ARC-002] Failed to create center. Raw error: {}", e.getMessage(), e);
            throw e;
        } finally {
            logger.info("[EXIT] createCenter completed");
        }
    }

    /**
     * Retrieve a Center by its ID.
     * Traceability Tags: [REQ-005], [ARC-002]
     *
     * @param id Center UUID
     * @return Center or throws IllegalArgumentException if not found
     */
    @Transactional(readOnly = true)
    public Center getCenterById(UUID id) {
        logger.info("[ENTRY] getCenterById called with id: {}", id);
        try {
            return centerRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMsg = String.format(CENTER_NOT_FOUND, id);
                    logger.error("[ERROR] [REQ-005] [ARC-002] Center not found: {}", errorMsg);
                    return new IllegalArgumentException(errorMsg);
                });
        } catch (Exception e) {
            logger.error("[ERROR] [REQ-005] [ARC-002] Error retrieving center. Raw error: {}", e.getMessage(), e);
            throw e;
        } finally {
            logger.info("[EXIT] getCenterById completed");
        }
    }

    /**
     * Update an existing Center.
     * Traceability Tags: [REQ-005], [ARC-002]
     *
     * @param center Center entity with updated data
     * @return updated Center
     */
    @Transactional
    public Center updateCenter(Center center) {
        logger.info("[ENTRY] updateCenter called with id: {}", center.getId());
        try {
            Center existing = getCenterById(center.getId());
            // Apply updates to mutable fields
            existing.setName(center.getName());
            existing.setAddress(center.getAddress());
            existing.setTaxId(center.getTaxId());
            existing.setContactPhone(center.getContactPhone());
            existing.setContactEmail(center.getContactEmail());
            Center updated = centerRepository.save(existing);
            logger.debug("[DEBUG] Center updated with id: {}", updated.getId());
            return updated;
        } catch (Exception e) {
            logger.error("[ERROR] [REQ-005] [ARC-002] Failed to update center. Raw error: {}", e.getMessage(), e);
            throw e;
        } finally {
            logger.info("[EXIT] updateCenter completed");
        }
    }

    /**
     * Delete a Center by its ID.
     * Traceability Tags: [REQ-005], [ARC-002]
     *
     * @param id Center UUID to delete
     */
    @Transactional
    public void deleteCenter(UUID id) {
        logger.info("[ENTRY] deleteCenter called with id: {}", id);
        try {
            Center center = getCenterById(id);
            // Business rule: cannot delete if there are associated courses or enrollments
            if (!center.getCourses().isEmpty() || !center.getEnrollments().isEmpty()) {
                String errorMsg = String.format(CENTER_DELETE_WITH_ASSOCIATED_DATA, id);
                logger.error("[ERROR] [REQ-005] [ARC-002] Cannot delete center with associated data: {}", errorMsg);
                throw new IllegalStateException(errorMsg);
            }
            centerRepository.deleteById(id);
            logger.debug("[DEBUG] Center deleted with id: {}", id);
        } catch (Exception e) {
            logger.error("[ERROR] [REQ-005] [ARC-002] Failed to delete center. Raw error: {}", e.getMessage(), e);
            throw e;
        } finally {
            logger.info("[EXIT] deleteCenter completed");
        }
    }

    /**
     * Retrieve all Centers.
     * Traceability Tags: [REQ-005], [ARC-002]
     *
     * @return list of all Centers
     */
    @Transactional(readOnly = true)
    public List<Center> findAllCenters() {
        logger.info("[ENTRY] findAllCenters called");
        try {
            List<Center> centers = centerRepository.findAll();
            logger.debug("[DEBUG] Retrieved {} centers", centers.size());
            return centers;
        } catch (Exception e) {
            logger.error("[ERROR] [REQ-005] [ARC-002] Failed to retrieve all centers. Raw error: {}", e.getMessage(), e);
            throw e;
        } finally {
            logger.info("[EXIT] findAllCenters completed");
        }
    }
}