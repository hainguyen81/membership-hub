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