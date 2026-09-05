package org.nlh4j.saas.membership_hub.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit test suite for RbacMiddleware
 * Verifies compliance with RBAC requirements and enterprise security constraints
 * @verifies [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
 */
@ExtendWith(MockitoExtension.class)
public class RbacMiddlewareTest {
    // Enterprise logger instance per governance mandate [0.3]
    private static final Logger logger = LoggerFactory.getLogger(RbacMiddlewareTest.class);
    
    // Top-level constants per anti-magic-number policy [0.2]
    private static final String TEST_USER_EMAIL = "test@membershiphub.com";
    private static final String TEST_CENTER_ID = "123e4567-e89b-12d3-a456-426614174000";
    private static final String OTHER_CENTER_ID = "123e4567-e89b-12d3-a456-426614174001";
    private static final String TEST_JWT_TOKEN = "valid.jwt.token";
    private static final String INVALID_JWT_TOKEN = "invalid.jwt.token";
    private static final String EXPIRED_JWT_TOKEN = "expired.jwt.token";
    private static final int HTTP_OK = 200;
    private static final int HTTP_UNAUTHORIZED = 401;
    private static final int HTTP_FORBIDDEN = 403;
    private static final long LATENCY_THRESHOLD_MS = 10; // NFR-001 requirement
    
    // Mock external dependencies for isolated unit testing
    @Mock
    private ContainerRequestContext requestContext;
    
    @Mock
    private ResourceInfo resourceInfo;
    
    @Mock
    private org.nlh4j.saas.membership_hub.security.service.JwtTokenService jwtTokenService;
    
    @Mock
    private org.nlh4j.saas.membership_hub.security.service.RbacPermissionService rbacPermissionService;
    
    @Mock
    private org.nlh4j.saas.membership_hub.security.model.UserContext userContext;
    
    // Instance under test
    private RbacMiddleware rbacMiddleware;
    
    @BeforeEach
    void setUp() {
        // Initialize RbacMiddleware with mocked dependencies for isolation
        rbacMiddleware = new RbacMiddleware(jwtTokenService, rbacPermissionService);
        reset(requestContext, resourceInfo, jwtTokenService, rbacPermissionService, userContext);
        logger.info("[TEST_SETUP] RbacMiddleware test environment initialized with mocked dependencies");
    }
    
    /**
     * Test case 1: System Admin successfully accesses center management endpoint
     * Validates that System Admin role has full access to admin endpoints per RBAC matrix
     * @verifies [REQ-003], [ARC-001], [ARC-002]
     */
    @Test
    void testSystemAdminAccessCenterManagement_Success() {
        logger.info("[TEST_START] [REQ-003] [ARC-001] [ARC-002] Testing System Admin access to center management endpoint");
        
        // Arrange: Mock System Admin user with valid JWT token
        mockValidJwtToken(TEST_JWT_TOKEN, "SYSTEM_ADMIN", null);
        mockRequestEndpoint("/api/v1/admin/centers", "GET");
        mockResourcePermissions(Set.of("SYSTEM_ADMIN"));
        
        // Act: Execute the RBAC filter and measure latency
        long startTime = System.nanoTime();
        rbacMiddleware.filter(requestContext);
        long endTime = System.nanoTime();
        long latencyMs = (endTime - startTime) / 1_000_000;
        
        // Assert: Verify request is not aborted, latency meets NFR-001 requirement
        verify(requestContext, never()).abortWith(any(Response.class));
        assertTrue(latencyMs < LATENCY_THRESHOLD_MS, 
            "RBAC permission check latency must be under 10ms per NFR-001, actual: " + latencyMs + "ms");
        
        logger.info("[TEST_PASS] [REQ-003] System Admin access to center management endpoint allowed successfully, latency: {}ms", latencyMs);
    }
    
    /**
     * Test case 2: Student accessing course management endpoint is denied with 403 Forbidden
     * Validates that Student role has no access to admin course management endpoints
     * @verifies [REQ-003], [ARC-001], [ARC-003]
     */
    @Test
    void testStudentAccessCourseManagement_Denied403() {
        logger.info("[TEST_START] [REQ-003] [ARC-001] [ARC-003] Testing Student access to course management endpoint");
        
        // Arrange: Mock Student user with valid JWT token
        mockValidJwtToken(TEST_JWT_TOKEN, "STUDENT", TEST_CENTER_ID);
        mockRequestEndpoint("/api/v1/admin/courses", "POST");
        mockResourcePermissions(Set.of("SYSTEM_ADMIN", "CENTER_ADMIN"));
        
        // Act: Execute the RBAC filter
        rbacMiddleware.filter(requestContext);
        
        // Assert: Verify request is aborted with 403 Forbidden
        verify(requestContext).abortWith(Response.status(HTTP_FORBIDDEN).build());
        
        logger.info("[TEST_PASS] [REQ-003] Student access to course management endpoint correctly denied with 403 Forbidden");
    }
    
    /**
     * Test case 3: Center Admin accessing another center's resource is denied with 403 Forbidden
     * Validates that Center Admin role is scoped only to their assigned center per RBAC matrix
     * @verifies [REQ-003], [ARC-002], [ARC-004]
     */
    @Test
    void testCenterAdminAccessOtherCenterResource_Denied403() {
        logger.info("[TEST_START] [REQ-003] [ARC-002] [ARC-004] Testing Center Admin access to other center's resource");
        
        // Arrange: Mock Center Admin user assigned to TEST_CENTER_ID, requesting OTHER_CENTER_ID resource
        mockValidJwtToken(TEST_JWT_TOKEN, "CENTER_ADMIN", TEST_CENTER_ID);
        mockRequestEndpoint("/api/v1/centers/" + OTHER_CENTER_ID + "/students", "GET");
        mockResourcePermissions(Set.of("CENTER_ADMIN"));
        // Mock center ID extraction from request path to return OTHER_CENTER_ID
        when(requestContext.getUriInfo()).thenReturn(mock(javax.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/v1/centers/" + OTHER_CENTER_ID + "/students");
        
        // Act: Execute the RBAC filter
        rbacMiddleware.filter(requestContext);
        
        // Assert: Verify request is aborted with 403 Forbidden
        verify(requestContext).abortWith(Response.status(HTTP_FORBIDDEN).build());
        
        logger.info("[TEST_PASS] [REQ-003] Center Admin access to other center's resource correctly denied with 403 Forbidden");
    }
    
    /**
     * Test case 4: Manager accessing course edit endpoint is denied with 403 Forbidden
     * Validates that Manager role has no permission to modify course resources per RBAC matrix
     * @verifies [REQ-003], [ARC-003], [ARC-005]
     */
    @Test
    void testManagerAccessCourseEditEndpoint_Denied403() {
        logger.info("[TEST_START] [REQ-003] [ARC-003] [ARC-005] Testing Manager access to course edit endpoint");
        
        // Arrange: Mock Manager user with valid JWT token
        mockValidJwtToken(TEST_JWT_TOKEN, "MANAGER", TEST_CENTER_ID);
        mockRequestEndpoint("/api/v1/courses/123e4567-e89b-12d3-a456-426614174002", "PUT");
        mockResourcePermissions(Set.of("SYSTEM_ADMIN", "CENTER_ADMIN"));
        
        // Act: Execute the RBAC filter
        rbacMiddleware.filter(requestContext);
        
        // Assert: Verify request is aborted with 403 Forbidden
        verify(requestContext).abortWith(Response.status(HTTP_FORBIDDEN).build());
        
        logger.info("[TEST_PASS] [REQ-003] Manager access to course edit endpoint correctly denied with 403 Forbidden");
    }
    
    /**
     * Test case 5: Teacher accessing course enrollment endpoint is denied with 403 Forbidden
     * Validates that Teacher role has no permission to manage student enrollments per RBAC matrix
     * @verifies [REQ-003], [ARC-004], [ARC-005]
     */
    @Test
    void testTeacherAccessEnrollmentEndpoint_Denied403() {
        logger.info("[TEST_START] [REQ-003] [ARC-004] [ARC-005] Testing Teacher access to enrollment endpoint");
        
        // Arrange: Mock Teacher user with valid JWT token
        mockValidJwtToken(TEST_JWT_TOKEN, "TEACHER", TEST_CENTER_ID);
        mockRequestEndpoint("/api/v1/enrollments", "POST");
        mockResourcePermissions(Set.of("STUDENT"));
        
        // Act: Execute the RBAC filter
        rbacMiddleware.filter(requestContext);
        
        // Assert: Verify request is aborted with 403 Forbidden
        verify(requestContext).abortWith(Response.status(HTTP_FORBIDDEN).build());
        
        logger.info("[TEST_PASS] [REQ-003] Teacher access to enrollment endpoint correctly denied with 403 Forbidden");
    }
    
    /**
     * Test case 6: Expired/invalid JWT token is denied with 401 Unauthorized
     * Validates that invalid or expired authentication tokens are rejected at the entry point
     * @verifies [REQ-003], [ARC-001], [ARC-006]
     */
    @Test
    void testInvalidJwtToken_Denied401() {
        logger.info("[TEST_START] [REQ-003] [ARC-001] [ARC-006] Testing invalid/expired JWT token access");
        
        // Arrange: Mock invalid JWT token in request header
        when(requestContext.getHeaderString("Authorization")).thenReturn("Bearer " + INVALID_JWT_TOKEN);
        when(jwtTokenService.validateToken(INVALID_JWT_TOKEN)).thenThrow(new org.nlh4j.saas.membership_hub.security.exception.JwtValidationException("Token expired or invalid"));
        mockRequestEndpoint("/api/v1/admin/centers", "GET");
        
        // Act: Execute the RBAC filter
        rbacMiddleware.filter(requestContext);
        
        // Assert: Verify request is aborted with 401 Unauthorized
        verify(requestContext).abortWith(Response.status(HTTP_UNAUTHORIZED).build());
        
        logger.info("[TEST_PASS] [REQ-003] Invalid JWT token correctly denied with 401 Unauthorized");
    }
    
    /**
     * Test case 7: RBAC permission check latency is under 10ms threshold per NFR-001
     * Validates that permission checks do not introduce unacceptable latency for API requests
     * @verifies [REQ-003], [ARC-001], [NFR-001]
     */
    @Test
    void testRbacPermissionCheckLatency_Under10ms() {
        logger.info("[TEST_START] [REQ-003] [ARC-001] [NFR-001] Testing RBAC permission check latency compliance");
        
        // Arrange: Prepare valid request with System Admin role
        mockValidJwtToken(TEST_JWT_TOKEN, "SYSTEM_ADMIN", null);
        mockRequestEndpoint("/api/v1/admin/centers", "GET");
        mockResourcePermissions(Set.of("SYSTEM_ADMIN"));
        
        // Act: Run 100 iterations to measure average and peak latency
        long totalLatency = 0;
        long maxLatency = 0;
        int iterations = 100;
        
        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime();
            rbacMiddleware.filter(requestContext);
            long endTime = System.nanoTime();
            long latency = (endTime - startTime) / 1_000_000;
            totalLatency += latency;
            maxLatency = Math.max(maxLatency, latency);
        }
        
        long avgLatency = totalLatency / iterations;
        
        // Assert: Verify both average and peak latency meet NFR-001 requirements
        assertTrue(avgLatency < LATENCY_THRESHOLD_MS, 
            "Average RBAC latency must be under 10ms per NFR-001, actual: " + avgLatency + "ms");
        assertTrue(maxLatency < LATENCY_THRESHOLD_MS, 
            "Peak RBAC latency must be under 10ms per NFR-001, actual: " + maxLatency + "ms");
        
        logger.info("[TEST_PASS] [REQ-003] RBAC latency meets NFR-001 requirement: avg={}ms, peak={}ms", avgLatency, maxLatency);
    }
    
    // Helper method to mock valid JWT token and user context
    private void mockValidJwtToken(String token, String role, String centerId) {
        when(userContext.getEmail()).thenReturn(TEST_USER_EMAIL);
        when(userContext.getRole()).thenReturn(role);
        when(userContext.getCenterId()).thenReturn(centerId);
        when(jwtTokenService.validateToken(token)).thenReturn(userContext);
        when(requestContext.getHeaderString("Authorization")).thenReturn("Bearer " + token);
    }
    
    // Helper method to mock request endpoint and HTTP method
    private void mockRequestEndpoint(String path, String method) {
        javax.ws.rs.core.UriInfo uriInfo = mock(javax.ws.rs.core.UriInfo.class);
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(requestContext.getMethod()).thenReturn(method);
        when(resourceInfo.getResourceMethod()).thenReturn(mock(Method.class));
        when(uriInfo.getPath()).thenReturn(path);
    }
    
    // Helper method to mock required permissions for requested resource
    private void mockResourcePermissions(Set<String> allowedRoles) {
        Method resourceMethod = resourceInfo.getResourceMethod();
        when(rbacPermissionService.getRequiredRoles(resourceMethod)).thenReturn(allowedRoles);
    }
}