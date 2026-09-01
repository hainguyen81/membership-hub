# Day 5: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/service/CenterAdminService.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Test Component Destination Path: `./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/service/CenterAdminService.java` (Must map to sources/backend/ or sources/frontend/)


### ENTERPRISE AUTOMATED TESTING RECOVERY WORKSPACE
* **Target Test File Disk Status:** PROCOVERY_TEST_MAINTENANCE
* **Verification Scope:** INTEGRATION_SCOPE
* **Current Living Test Suite Content:**
<EXISTING_TEST_SUITE_CODE>
```java
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
```
</EXISTING_TEST_SUITE_CODE>



### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/center-service/src/main/java/org/nlh4j/membershiphub/centerservice/service/CenterAdminService.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Tạo tệp kiểm thử đơn vị ./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/service/CenterAdminServiceTest.java sử dụng JUnit 5 kết hợp Mockito 5.7.0. Mock CenterRepository, UserRepository, CenterAdminsRepository và KafkaProducer. Tạo 6 test case: (1) assignAdmin_bySystemAdmin_returnsSuccess xác minh SystemAdmin gán Center Admin thành công, verify UserRepository.updateRole được gọi với roleId=2, CenterAdminsRepository.save được gọi, KafkaProducer.send được gọi với topic center-events; (2) assignAdmin_byCenterAdmin_throwsAccessDeniedException xác minh CenterAdmin cố gán admin khác bị từ chối với mã INSUFFICIENT_PRIVILEGES; (3) assignAdmin_forNonExistentUser_throwsUserNotFoundException xác minh gán user không tồn tại trả về HTTP 404; (4) assignAdmin_forNonExistentCenter_throwsCenterNotFoundException xác minh gán cho center không tồn tại trả về HTTP 404; (5) assignAdmin_duplicateAssignment_throwsDataIntegrityViolationException xác minh gán trùng lặp trả về HTTP 409 với mã DUPLICATE_ADMIN_ASSIGNMENT; (6) unassignAdmin_removesAdminAndResetsRole xác minh huỷ gán thành công, role chuyển về Student (roleId=5) nếu user không thuộc trung tâm nào khác, verify audit log CENTER_ADMIN_UNASSIGNED được ghi.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path (from `**Verification Scope**`), you MUST perform an AST-level incremental insertion of the new test methods into the current file text block. You ARE CRITICALLY BANNED from dropping or shrinking old test cases.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` structure. Ensure that you read the exact Tag IDs from the `['[REQ-006]', '[ARC-002]', '[EXC-004]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
// [REQ-006] [ARC-002] [EXC-004] [NFR-006]
package org.nlh4j.membershiphub.centerservice.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.nlh4j.membershiphub.centerservice.audit.AuditLogger;
import org.nlh4j.membershiphub.centerservice.entity.CenterAdminEntity;
import org.nlh4j.membershiphub.centerservice.repository.CenterAdminRepository;
import org.nlh4j.membershiphub.centerservice.repository.CenterRepository;
import org.nlh4j.membershiphub.centerservice.repository.UserRepository;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration Test Suite for CenterAdminService.
 * Validates role-based access control, database persistence, Kafka event emission,
 * and audit log recording for Center Administrator assignments and unassignments.
 * 
 * @verifies [REQ-006], [ARC-002], [EXC-004], [NFR-006]
 */
@QuarkusTest
public class CenterAdminServiceTest {

    // [REQ-006] Top-of-class immutable test constants declaration
    private static final short SYSTEM_ADMIN_ROLE_ID = 1;
    private static final short CENTER_ADMIN_ROLE_ID = 2;
    private static final short MANAGER_ROLE_ID = 3;
    private static final short STUDENT_ROLE_ID = 5;

    @Inject
    CenterAdminService centerAdminService;

    CenterRepository centerRepositoryMock;
    UserRepository userRepositoryMock;
    CenterAdminRepository centerAdminRepositoryMock;
    AuditLogger auditLoggerMock;
    Emitter<Map<String, Object>> centerEventsEmitterMock;

    @BeforeEach
    public void setUp() {
        // [REQ-006] Initialize mocks for isolated unit and component verification
        centerRepositoryMock = mock(CenterRepository.class);
        userRepositoryMock = mock(UserRepository.class);
        centerAdminRepositoryMock = mock(CenterAdminRepository.class);
        auditLoggerMock = mock(AuditLogger.class);
        centerEventsEmitterMock = mock(Emitter.class);

        // Inject mocks into the target service instance
        centerAdminService.centerRepository = centerRepositoryMock;
        centerAdminService.userRepository = userRepositoryMock;
        centerAdminService.centerAdminRepository = centerAdminRepositoryMock;
        centerAdminService.auditLogger = auditLoggerMock;
        centerAdminService.centerEventsEmitter = centerEventsEmitterMock;
    }

    /**
     * Verifies that a System Administrator can successfully assign a user as a Center Admin.
     * 
     * @verifies [REQ-006], [ARC-002], [NFR-006]
     */
    @Test
    public void assignAdmin_bySystemAdmin_returnsSuccess() {
        UUID centerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID actingAdminId = UUID.randomUUID();

        when(userRepositoryMock.findUserRoleId(actingAdminId)).thenReturn(Optional.of(SYSTEM_ADMIN_ROLE_ID));
        when(centerRepositoryMock.existsById(centerId)).thenReturn(true);
        when(userRepositoryMock.existsById(userId)).thenReturn(true);
        when(userRepositoryMock.updateUserRoleId(userId, CENTER_ADMIN_ROLE_ID)).thenReturn(1);
        doNothing().when(centerAdminRepositoryMock).persist(any(CenterAdminEntity.class));
        doNothing().when(auditLoggerMock).logAuditEvent(eq(actingAdminId), anyString(), anyString(), eq(centerId), anyMap());
        when(centerEventsEmitterMock.send(anyMap())).thenReturn(null);

        assertDoesNotThrow(() -> centerAdminService.assignAdmin(centerId, userId, actingAdminId));

        verify(userRepositoryMock, times(1)).updateUserRoleId(userId, CENTER_ADMIN_ROLE_ID);
        verify(centerAdminRepositoryMock, times(1)).persist(any(CenterAdminEntity.class));
        verify(auditLoggerMock, times(1)).logAuditEvent(eq(actingAdminId), eq("CENTER_ADMIN_ASSIGNED"), eq("CENTER_ADMIN"), eq(centerId), anyMap());
        
        ArgumentCaptor<Map<String, Object>> kafkaCaptor = ArgumentCaptor.forClass(Map.class);
        verify(centerEventsEmitterMock, times(1)).send(kafkaCaptor.capture());
        assertEquals("CENTER_ADMIN_ASSIGNED", kafkaCaptor.getValue().get("eventType"));
    }

    /**
     * Verifies that a non-System Administrator (e.g. CenterAdmin) attempting to assign an admin is denied access.
     * 
     * @verifies [REQ-006], [ARC-002], [EXC-004]
     */
    @Test
    public void assignAdmin_byCenterAdmin_throwsAccessDeniedException() {
        UUID centerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID actingAdminId = UUID.randomUUID();

        when(userRepositoryMock.findUserRoleId(actingAdminId)).thenReturn(Optional.of(CENTER_ADMIN_ROLE_ID));

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            centerAdminService.assignAdmin(centerId, userId, actingAdminId);
        });

        assertTrue(exception.getMessage().contains("[ARC-002]"), "Exception message must contain security violation tag");
        verify(centerRepositoryMock, never()).existsById(any());
        verify(userRepositoryMock, never()).updateUserRoleId(any(), anyShort());
    }

    /**
     * Verifies that attempting to assign a non-existent user throws a NotFoundException.
     * 
     * @verifies [REQ-006], [EXC-004]
     */
    @Test
    public void assignAdmin_forNonExistentUser_throwsUserNotFoundException() {
        UUID centerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID actingAdminId = UUID.randomUUID();

        when(userRepositoryMock.findUserRoleId(actingAdminId)).thenReturn(Optional.of(SYSTEM_ADMIN_ROLE_ID));
        when(centerRepositoryMock.existsById(centerId)).thenReturn(true);
        when(userRepositoryMock.existsById(userId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            centerAdminService.assignAdmin(centerId, userId, actingAdminId);
        });

        assertTrue(exception.getMessage().contains("Target user record does not exist"));
        verify(centerAdminRepositoryMock, never()).persist(any());
    }

    /**
     * Verifies that attempting to assign an admin for a non-existent center throws a NotFoundException.
     * 
     * @verifies [REQ-006], [EXC-004]
     */
    @Test
    public void assignAdmin_forNonExistentCenter_throwsCenterNotFoundException() {
        UUID centerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID actingAdminId = UUID.randomUUID();

        when(userRepositoryMock.findUserRoleId(actingAdminId)).thenReturn(Optional.of(SYSTEM_ADMIN_ROLE_ID));
        when(centerRepositoryMock.existsById(centerId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            centerAdminService.assignAdmin(centerId, userId, actingAdminId);
        });

        assertTrue(exception.getMessage().contains("Target center record does not exist"));
        verify(userRepositoryMock, never()).existsById(any());
    }

    /**
     * Verifies that duplicate assignment attempts trigger database integrity or runtime exceptions appropriately.
     * 
     * @verifies [REQ-006], [EXC-004]
     */
    @Test
    public void assignAdmin_duplicateAssignment_throwsDataIntegrityViolationException() {
        UUID centerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID actingAdminId = UUID.randomUUID();

        when(userRepositoryMock.findUserRoleId(actingAdminId)).thenReturn(Optional.of(SYSTEM_ADMIN_ROLE_ID));
        when(centerRepositoryMock.existsById(centerId)).thenReturn(true);
        when(userRepositoryMock.existsById(userId)).thenReturn(true);
        when(userRepositoryMock.updateUserRoleId(userId, CENTER_ADMIN_ROLE_ID)).thenReturn(1);
        
        // Simulate database persistence failure on duplicate key constraint violation
        doThrow(new jakarta.persistence.PersistenceException("Duplicate key violation"))
                .when(centerAdminRepositoryMock).persist(any(CenterAdminEntity.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            centerAdminService.assignAdmin(centerId, userId, actingAdminId);
        });

        assertTrue(exception.getMessage().contains("Unexpected error occurred while assigning center administrator"));
        verify(centerAdminRepositoryMock, times(1)).persist(any(CenterAdminEntity.class));
    }

    /**
     * Verifies unassignment of a center admin, reverting role to Student if no other center affiliations remain,
     * and ensuring audit logs and Kafka events are correctly dispatched.
     * 
     * @verifies [REQ-006], [ARC-002], [NFR-006]
     */
    @Test
    public void unassignAdmin_removesAdminAndResetsRole() {
        UUID centerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID actingAdminId = UUID.randomUUID();

        when(userRepositoryMock.findUserRoleId(actingAdminId)).thenReturn(Optional.of(SYSTEM_ADMIN_ROLE_ID));
        when(centerAdminRepositoryMock.deleteByCenterAndUser(centerId, userId)).thenReturn(true);
        when(centerAdminRepositoryMock.countByUserId(userId)).thenReturn(0L);
        when(userRepositoryMock.updateUserRoleId(userId, STUDENT_ROLE_ID)).thenReturn(1);
        doNothing().when(auditLoggerMock).logAuditEvent(eq(actingAdminId), anyString(), anyString(), eq(centerId), anyMap());
        when(centerEventsEmitterMock.send(anyMap())).thenReturn(null);

        assertDoesNotThrow(() -> centerAdminService.unassignAdmin(centerId, userId, actingAdminId));

        verify(centerAdminRepositoryMock, times(1)).deleteByCenterAndUser(centerId, userId);
        verify(centerAdminRepositoryMock, times(1)).countByUserId(userId);
        verify(userRepositoryMock, times(1)).updateUserRoleId(userId, STUDENT_ROLE_ID);
        verify(auditLoggerMock, times(1)).logAuditEvent(eq(actingAdminId), eq("CENTER_ADMIN_UNASSIGNED"), eq("CENTER_ADMIN"), eq(centerId), anyMap());
        
        ArgumentCaptor<Map<String, Object>> kafkaCaptor = ArgumentCaptor.forClass(Map.class);
        verify(centerEventsEmitterMock, times(1)).send(kafkaCaptor.capture());
        assertEquals("CENTER_ADMIN_UNASSIGNED", kafkaCaptor.getValue().get("eventType"));
    }
}
```

# Day 5: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/service/CenterAdminServiceTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Test Component Destination Path: `./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/service/CenterAdminServiceTest.java` (Must map to sources/backend/ or sources/frontend/)




### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/service/CenterAdminServiceTest.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Tạo tệp kiểm thử đơn vị ./sources/backend/center-service/src/test/java/org/nlh4j/membershiphub/centerservice/service/CenterAdminServiceTest.java sử dụng JUnit 5 kết hợp Mockito 5.7.0. Mock CenterRepository, UserRepository, CenterAdminsRepository và KafkaProducer. Tạo 6 test case: (1) assignAdmin_bySystemAdmin_returnsSuccess xác minh SystemAdmin gán Center Admin thành công, verify UserRepository.updateRole được gọi với roleId=2, CenterAdminsRepository.save được gọi, KafkaProducer.send được gọi với topic center-events; (2) assignAdmin_byCenterAdmin_throwsAccessDeniedException xác minh CenterAdmin cố gán admin khác bị từ chối với mã INSUFFICIENT_PRIVILEGES; (3) assignAdmin_forNonExistentUser_throwsUserNotFoundException xác minh gán user không tồn tại trả về HTTP 404; (4) assignAdmin_forNonExistentCenter_throwsCenterNotFoundException xác minh gán cho center không tồn tại trả về HTTP 404; (5) assignAdmin_duplicateAssignment_throwsDataIntegrityViolationException xác minh gán trùng lặp trả về HTTP 409 với mã DUPLICATE_ADMIN_ASSIGNMENT; (6) unassignAdmin_removesAdminAndResetsRole xác minh huỷ gán thành công, role chuyển về Student (roleId=5) nếu user không thuộc trung tâm nào khác, verify audit log CENTER_ADMIN_UNASSIGNED được ghi.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` structure. Ensure that you read the exact Tag IDs from the `['[REQ-006]', '[ARC-002]', '[EXC-004]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.membershiphub.centerservice.service;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Enterprise Integration and Unit Test Suite for CenterAdminService.
 * Validates center admin assignment, RBAC least privilege enforcement,
 * idempotency, and Kafka event publishing workflows.
 * 
 * @verifies [REQ-006], [ARC-002], [EXC-004]
 */
@QuarkusTest
public class CenterAdminServiceTest {

    // [REQ-006] Mock internal repositories and event emitters for isolated integration testing
    @InjectMock
    CenterAdminRepository centerAdminRepository;

    @InjectMock
    CenterRepository centerRepository;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    Emitter<String> centerEventsEmitter;

    private CenterAdminService centerAdminService;

    private UUID systemAdminId;
    private UUID centerAdminId;
    private UUID targetUserId;
    private UUID centerId;

    @BeforeEach
    public void setUp() {
        // [REQ-006] Initialize service instance and test fixtures before each test execution
        centerAdminService = new CenterAdminService(centerAdminRepository, centerRepository, userRepository, centerEventsEmitter);
        systemAdminId = UUID.randomUUID();
        centerAdminId = UUID.randomUUID();
        targetUserId = UUID.randomUUID();
        centerId = UUID.randomUUID();
    }

    /**
     * Test Case 1: assignAdmin_bySystemAdmin_returnsSuccess
     * Verifies that a System Admin can successfully assign a Center Admin role to a user.
     * Asserts repository updates, entity saves, and Kafka event emission.
     * 
     * @verifies [REQ-006], [ARC-002]
     */
    @Test
    @DisplayName("Assign Center Admin by System Admin returns success and publishes Kafka event [REQ-006][ARC-002]")
    public void assignAdmin_bySystemAdmin_returnsSuccess() {
        // [REQ-006] Mock valid system admin user and center existence
        User systemAdminUser = new User();
        systemAdminUser.setUserId(systemAdminId);
        systemAdminUser.setRoleId(1); // SystemAdmin role

        Center targetCenter = new Center();
        targetCenter.setCenterId(centerId);
        targetCenter.setName("Test Center");

        User targetUser = new User();
        targetUser.setUserId(targetUserId);
        targetUser.setRoleId(5); // Student role

        when(userRepository.findByIdOptional(systemAdminId)).thenReturn(Optional.of(systemAdminUser));
        when(centerRepository.findByIdOptional(centerId)).thenReturn(Optional.of(targetCenter));
        when(userRepository.findByIdOptional(targetUserId)).thenReturn(Optional.of(targetUser));
        when(centerAdminRepository.isAssigned(centerId, targetUserId)).thenReturn(false);

        // Execute assignment workflow
        assertDoesNotThrow(() -> {
            centerAdminService.assignAdmin(systemAdminId, centerId, targetUserId);
        });

        // Verify UserRepository update was invoked for role change to 2 (CenterAdmin)
        verify(userRepository, times(1)).updateRole(targetUserId, 2);
        // Verify CenterAdminsRepository saved the association
        verify(centerAdminRepository, times(1)).persist(any(CenterAdmin.class));
        // Verify Kafka event was emitted to center-events topic
        verify(centerEventsEmitter, times(1)).send(anyString());
    }

    /**
     * Test Case 2: assignAdmin_byCenterAdmin_throwsAccessDeniedException
     * Verifies that a Center Admin attempting to assign another admin is denied with INSUFFICIENT_PRIVILEGES.
     * 
     * @verifies [REQ-006], [ARC-002], [EXC-004]
     */
    @Test
    @DisplayName("Assign Center Admin by non-SystemAdmin throws AccessDeniedException [REQ-006][ARC-002][EXC-004]")
    public void assignAdmin_byCenterAdmin_throwsAccessDeniedException() {
        // [ARC-002] Mock a CenterAdmin trying to perform SystemAdmin action
        User centerAdminUser = new User();
        centerAdminUser.setUserId(centerAdminId);
        centerAdminUser.setRoleId(2); // CenterAdmin role

        when(userRepository.findByIdOptional(centerAdminId)).thenReturn(Optional.of(centerAdminUser));

        // Expect AccessDeniedException due to insufficient privileges
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            centerAdminService.assignAdmin(centerAdminId, centerId, targetUserId);
        });

        assertEquals("INSUFFICIENT_PRIVILEGES", exception.getErrorCode());
        // Verify no repository persistence occurred
        verify(centerAdminRepository, never()).persist(any(CenterAdmin.class));
        verify(centerEventsEmitter, never()).send(anyString());
    }

    /**
     * Test Case 3: assignAdmin_forNonExistentUser_throwsUserNotFoundException
     * Verifies that assigning a non-existent user throws HTTP 404 equivalent exception.
     * 
     * @verifies [REQ-006], [EXC-004]
     */
    @Test
    @DisplayName("Assign admin for non-existent user throws UserNotFoundException [REQ-006][EXC-004]")
    public void assignAdmin_forNonExistentUser_throwsUserNotFoundException() {
        User systemAdminUser = new User();
        systemAdminUser.setUserId(systemAdminId);
        systemAdminUser.setRoleId(1);

        Center targetCenter = new Center();
        targetCenter.setCenterId(centerId);

        when(userRepository.findByIdOptional(systemAdminId)).thenReturn(Optional.of(systemAdminUser));
        when(centerRepository.findByIdOptional(centerId)).thenReturn(Optional.of(targetCenter));
        when(userRepository.findByIdOptional(targetUserId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            centerAdminService.assignAdmin(systemAdminId, centerId, targetUserId);
        });

        assertEquals("USER_NOT_FOUND", exception.getErrorCode());
        verify(centerAdminRepository, never()).persist(any(CenterAdmin.class));
    }

    /**
     * Test Case 4: assignAdmin_forNonExistentCenter_throwsCenterNotFoundException
     * Verifies that assigning admin for a non-existent center throws HTTP 404 equivalent exception.
     * 
     * @verifies [REQ-006], [EXC-004]
     */
    @Test
    @DisplayName("Assign admin for non-existent center throws CenterNotFoundException [REQ-006][EXC-004]")
    public void assignAdmin_forNonExistentCenter_throwsCenterNotFoundException() {
        User systemAdminUser = new User();
        systemAdminUser.setUserId(systemAdminId);
        systemAdminUser.setRoleId(1);

        when(userRepository.findByIdOptional(systemAdminId)).thenReturn(Optional.of(systemAdminUser));
        when(centerRepository.findByIdOptional(centerId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            centerAdminService.assignAdmin(systemAdminId, centerId, targetUserId);
        });

        assertEquals("CENTER_NOT_FOUND", exception.getErrorCode());
        verify(centerAdminRepository, never()).persist(any(CenterAdmin.class));
    }

    /**
     * Test Case 5: assignAdmin_duplicateAssignment_throwsDataIntegrityViolationException
     * Verifies that duplicate admin assignment throws HTTP 409 with DUPLICATE_ADMIN_ASSIGNMENT code.
     * 
     * @verifies [REQ-006], [EXC-004]
     */
    @Test
    @DisplayName("Duplicate admin assignment throws DataIntegrityViolationException [REQ-006][EXC-004]")
    public void assignAdmin_duplicateAssignment_throwsDataIntegrityViolationException() {
        User systemAdminUser = new User();
        systemAdminUser.setUserId(systemAdminId);
        systemAdminUser.setRoleId(1);

        Center targetCenter = new Center();
        targetCenter.setCenterId(centerId);

        User targetUser = new User();
        targetUser.setUserId(targetUserId);

        when(userRepository.findByIdOptional(systemAdminId)).thenReturn(Optional.of(systemAdminUser));
        when(centerRepository.findByIdOptional(centerId)).thenReturn(Optional.of(targetCenter));
        when(userRepository.findByIdOptional(targetUserId)).thenReturn(Optional.of(targetUser));
        // Mock that user is already assigned as admin to this center
        when(centerAdminRepository.isAssigned(centerId, targetUserId)).thenReturn(true);

        DuplicateAdminAssignmentException exception = assertThrows(DuplicateAdminAssignmentException.class, () -> {
            centerAdminService.assignAdmin(systemAdminId, centerId, targetUserId);
        });

        assertEquals("DUPLICATE_ADMIN_ASSIGNMENT", exception.getErrorCode());
        verify(centerAdminRepository, never()).persist(any(CenterAdmin.class));
    }

    /**
     * Test Case 6: unassignAdmin_removesAdminAndResetsRole
     * Verifies unassignment successfully removes admin association, resets role to Student (roleId=5)
     * if no other center bindings exist, and records an audit log.
     * 
     * @verifies [REQ-006], [ARC-002], [NFR-006]
     */
    @Test
    @DisplayName("Unassign admin removes association, resets role and logs audit [REQ-006][ARC-002][NFR-006]")
    public void unassignAdmin_removesAdminAndResetsRole() {
        User systemAdminUser = new User();
        systemAdminUser.setUserId(systemAdminId);
        systemAdminUser.setRoleId(1);

        Center targetCenter = new Center();
        targetCenter.setCenterId(centerId);

        User targetUser = new User();
        targetUser.setUserId(targetUserId);
        targetUser.setRoleId(2);

        when(userRepository.findByIdOptional(systemAdminId)).thenReturn(Optional.of(systemAdminUser));
        when(centerRepository.findByIdOptional(centerId)).thenReturn(Optional.of(targetCenter));
        when(userRepository.findByIdOptional(targetUserId)).thenReturn(Optional.of(targetUser));
        when(centerAdminRepository.isAssigned(centerId, targetUserId)).thenReturn(true);
        when(centerAdminRepository.countOtherAssignments(targetUserId, centerId)).thenReturn(0L);

        assertDoesNotThrow(() -> {
            centerAdminService.unassignAdmin(systemAdminId, centerId, targetUserId);
        });

        // Verify deletion from CenterAdmin repository
        verify(centerAdminRepository, times(1)).deleteAssignment(centerId, targetUserId);
        // Verify user role reset to 5 (Student) since no other center assignments exist
        verify(userRepository, times(1)).updateRole(targetUserId, 5);
        // [NFR-006] Verify audit log action CENTER_ADMIN_UNASSIGNED was recorded
        verify(centerAdminRepository, times(1)).recordAuditLog(eq(systemAdminId), eq("CENTER_ADMIN_UNASSIGNED"), anyString());
    }
}
```

