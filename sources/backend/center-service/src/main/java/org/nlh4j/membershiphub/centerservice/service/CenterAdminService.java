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