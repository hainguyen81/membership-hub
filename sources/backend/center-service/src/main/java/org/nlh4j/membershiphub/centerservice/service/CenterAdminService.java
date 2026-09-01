// [REQ-006] [ARC-002] [NFR-006]
package org.nlh4j.membershiphub.centerservice.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.nlh4j.membershiphub.centerservice.audit.AuditLogger;
import org.nlh4j.membershiphub.centerservice.entity.CenterAdminEntity;
import org.nlh4j.membershiphub.centerservice.repository.CenterAdminRepository;
import org.nlh4j.membershiphub.centerservice.repository.CenterRepository;
import org.nlh4j.membershiphub.centerservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Enterprise Service Component for Center Administrator Assignments.
 * Manages the binding and unbinding of Center Administrators to specific centers
 * enforcing strict RBAC security boundaries, ACID transactions, audit logging,
 * and asynchronous event dispatching via Apache Kafka.
 * 
 * @traceability [REQ-006], [ARC-002], [NFR-006]
 */
@ApplicationScoped
public class CenterAdminService {

    // [REQ-006] Top-of-class immutable constants declaration law compliance
    private static final Logger LOGGER = LoggerFactory.getLogger(CenterAdminService.class);
    private static final short SYSTEM_ADMIN_ROLE_ID = 1;
    private static final short CENTER_ADMIN_ROLE_ID = 2;
    private static final short STUDENT_ROLE_ID = 5;
    private static final String ACTION_ASSIGNED = "CENTER_ADMIN_ASSIGNED";
    private static final String ACTION_UNASSIGNED = "CENTER_ADMIN_UNASSIGNED";
    private static final String TARGET_ENTITY_TYPE = "CENTER_ADMIN";

    @Inject
    CenterRepository centerRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    CenterAdminRepository centerAdminRepository;

    @Inject
    AuditLogger auditLogger;

    @Inject
    @Channel("center-events")
    Emitter<Map<String, Object>> centerEventsEmitter;

    /**
     * Assigns a user as a Center Administrator for a specified center.
     * Executes under strict ACID transaction boundaries and validates system administrator privileges.
     * 
     * @param centerId     the target center unique identifier
     * @param userId       the target user unique identifier to be promoted
     * @param actingUserId the system administrator performing the action
     * @traceability [REQ-006], [ARC-002], [NFR-006]
     */
    @Transactional
    public void assignAdmin(UUID centerId, UUID userId, UUID actingUserId) {
        LOGGER.info("[PROCESS] [REQ-006] Initiating center admin assignment. Center ID: {}, Target User ID: {}, Acting Admin ID: {}", 
                centerId, userId, actingUserId);

        try {
            // Step 1: Validate that the acting user holds SystemAdmin privileges [ARC-002]
            short actingUserRole = userRepository.findUserRoleId(actingUserId)
                    .orElseThrow(() -> new ForbiddenException("[ARC-002] Acting user identity could not be verified."));
            
            if (actingUserRole != SYSTEM_ADMIN_ROLE_ID) {
                LOGGER.warn("[SECURITY FAIL] [ARC-002] Unauthorized admin assignment attempt by User ID: {}", actingUserId);
                throw new ForbiddenException("[ARC-002] Access denied: Only System Administrators can assign Center Admins.");
            }

            // Step 2: Validate the existence of the target center in the database
            boolean centerExists = centerRepository.existsById(centerId);
            if (!centerExists) {
                LOGGER.error("[DATA FAIL] [REQ-006] Center not found for ID: {}", centerId);
                throw new NotFoundException("[REQ-006] Target center record does not exist.");
            }

            // Step 3: Validate the existence of the target user in the database
            boolean userExists = userRepository.existsById(userId);
            if (!userExists) {
                LOGGER.error("[DATA FAIL] [REQ-006] User not found for ID: {}", userId);
                throw new NotFoundException("[REQ-006] Target user record does not exist.");
            }

            // Step 4: Update the user's role to CenterAdmin (role_id = 2) using parameterized query
            int updatedRows = userRepository.updateUserRoleId(userId, CENTER_ADMIN_ROLE_ID);
            if (updatedRows == 0) {
                LOGGER.error("[DB FAIL] [REQ-006] Failed to update role for User ID: {}", userId);
                throw new IllegalStateException("[REQ-006] Database mutation failed during user role elevation.");
            }

            // Step 5: Persist the association in the CenterAdmins mapping table (composite primary key)
            CenterAdminEntity mappingEntity = new CenterAdminEntity(centerId, userId, actingUserId, Instant.now());
            centerAdminRepository.persist(mappingEntity);

            // Step 6: Record an immutable audit log entry for compliance [NFR-006]
            Map<String, String> auditDetails = new HashMap<>();
            auditDetails.put("centerId", centerId.toString());
            auditDetails.put("assignedUserId", userId.toString());
            auditDetails.put("assignedBy", actingUserId.toString());
            auditLogger.logAuditEvent(actingUserId, ACTION_ASSIGNED, TARGET_ENTITY_TYPE, centerId, auditDetails);

            // Step 7: Dispatch an asynchronous event to the Kafka topic 'center-events' [ARC-008]
            Map<String, Object> kafkaPayload = new HashMap<>();
            kafkaPayload.put("eventType", ACTION_ASSIGNED);
            kafkaPayload.put("centerId", centerId.toString());
            kafkaPayload.put("userId", userId.toString());
            kafkaPayload.put("assignedBy", actingUserId.toString());
            kafkaPayload.put("timestamp", Instant.now().toString());

            centerEventsEmitter.send(kafkaPayload);
            LOGGER.info("[SUCCESS] [REQ-006] Center Admin successfully assigned. Center ID: {}, User ID: {}", centerId, userId);

        } catch (ForbiddenException | NotFoundException e) {
            // Preserve standard business validation exceptions without wrapping
            throw e;
        } catch (Exception e) {
            // [ARC-007] Comprehensive exception auditing law enforcement
            LOGGER.error("[CRITICAL FAIL] [REQ-006] Center admin assignment failed due to system exception. Raw error: {}", e.getMessage(), e);
            throw new RuntimeException("[REQ-006] Unexpected error occurred while assigning center administrator.", e);
        }
    }

    /**
     * Revokes Center Administrator privileges from a user for a specified center.
     * Reverts user role to Student if no other center affiliations remain.
     * 
     * @param centerId     the target center unique identifier
     * @param userId       the target user unique identifier to be demoted
     * @param actingUserId the system administrator performing the action
     * @traceability [REQ-006], [ARC-002], [NFR-006]
     */
    @Transactional
    public void unassignAdmin(UUID centerId, UUID userId, UUID actingUserId) {
        LOGGER.info("[PROCESS] [REQ-006] Initiating center admin unassignment. Center ID: {}, Target User ID: {}, Acting Admin ID: {}", 
                centerId, userId, actingUserId);

        try {
            // Step 1: Validate SystemAdmin privileges for the acting user [ARC-002]
            short actingUserRole = userRepository.findUserRoleId(actingUserId)
                    .orElseThrow(() -> new ForbiddenException("[ARC-002] Acting user identity could not be verified."));
            
            if (actingUserRole != SYSTEM_ADMIN_ROLE_ID) {
                LOGGER.warn("[SECURITY FAIL] [ARC-002] Unauthorized admin unassignment attempt by User ID: {}", actingUserId);
                throw new ForbiddenException("[ARC-002] Access denied: Only System Administrators can unassign Center Admins.");
            }

            // Step 2: Delete the mapping record from the CenterAdmins association table
            boolean removed = centerAdminRepository.deleteByCenterAndUser(centerId, userId);
            if (!removed) {
                LOGGER.warn("[DATA WARN] [REQ-006] No active association found between Center ID: {} and User ID: {}", centerId, userId);
                throw new NotFoundException("[REQ-006] The specified user is not an administrator for this center.");
            }

            // Step 3: Check if the user is assigned to any other centers
            long remainingCenterCount = centerAdminRepository.countByUserId(userId);
            if (remainingCenterCount == 0) {
                // If no remaining center affiliations exist, revert role back to Student (role_id = 5)
                userRepository.updateUserRoleId(userId, STUDENT_ROLE_ID);
                LOGGER.info("[PROCESS] [REQ-006] User ID: {} has no remaining center assignments; role reverted to Student.", userId);
            }

            // Step 4: Record audit log entry for unassignment [NFR-006]
            Map<String, String> auditDetails = new HashMap<>();
            auditDetails.put("centerId", centerId.toString());
            auditDetails.put("unassignedUserId", userId.toString());
            auditDetails.put("unassignedBy", actingUserId.toString());
            auditLogger.logAuditEvent(actingUserId, ACTION_UNASSIGNED, TARGET_ENTITY_TYPE, centerId, auditDetails);

            // Step 5: Dispatch asynchronous unassignment event to Kafka [ARC-008]
            Map<String, Object> kafkaPayload = new HashMap<>();
            kafkaPayload.put("eventType", ACTION_UNASSIGNED);
            kafkaPayload.put("centerId", centerId.toString());
            kafkaPayload.put("userId", userId.toString());
            kafkaPayload.put("unassignedBy", actingUserId.toString());
            kafkaPayload.put("timestamp", Instant.now().toString());

            centerEventsEmitter.send(kafkaPayload);
            LOGGER.info("[SUCCESS] [REQ-006] Center Admin successfully unassigned. Center ID: {}, User ID: {}", centerId, userId);

        } catch (ForbiddenException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("[CRITICAL FAIL] [REQ-006] Center admin unassignment failed due to system exception. Raw error: {}", e.getMessage(), e);
            throw new RuntimeException("[REQ-006] Unexpected error occurred while unassigning center administrator.", e);
        }
    }
}