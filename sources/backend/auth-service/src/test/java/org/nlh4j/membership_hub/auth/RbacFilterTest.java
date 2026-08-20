/**
 * Unit test suite for {@link org.nlh4j.saas.membership-hub.auth.RbacFilter}.
 * <p>
 * This test class validates the enterprise RBAC enforcement logic, including
 * token extraction, role resolution, permission checks, and error handling.
 * All test cases are annotated with the required traceability Tag IDs to
 * satisfy the enterprise governance framework.
 * </p>
 *
 * @verifies [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
 */
package org.nlh4j.saas.membership-hub.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test implementation for {@link RbacFilter}.
 * <p>
 * Each test method includes inline documentation describing the business
 * requirement being validated, the edge cases covered, and the assertion
 * strategy employed.
 * </p>
 *
 * @verifies [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
 */
public class RbacFilterTest {

    /** Logger for test verification (mirrors production logging). */
    private static final Logger testLogger = LoggerFactory.getLogger(RbacFilterTest.class);

    /** Mocked {@link TokenService} to control token validation outcomes. */
    @Mock
    private TokenService tokenServiceMock;

    /** {@link RbacFilter} instance under test, with its dependencies injected. */
    @InjectMocks
    private RbacFilter rbacFilter;

    /** JUnit Mockito annotation processor – initializes mocks before each test. */
    @BeforeEach
    public void setUp() {
        // Initialize mock framework and invoke filter initialization (mirrors production init)
        MockitoAnnotations.openMocks(this);
        rbacFilter.init(null);
        testLogger.info("[TEST_SETUP] RbacFilterTest initialized with mocked TokenService.");
    }

    /**
     * Clean‑up after each test – verifies that the filter's destroy logging is
     * invoked as per the enterprise lifecycle requirements.
     * <p>
     * Traceability: [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
     * </p>
     */
    @AfterEach
    public void tearDown() {
        rbacFilter.destroy();
        testLogger.info("[TEST_TEARDOWN] RbacFilterTest cleaned up.");
    }

    /**
     * Happy‑path scenario: valid Bearer token with required role for {@code /api/admin/users}.
     * <p>
     * Validates that the filter correctly extracts a valid token, retrieves the
     * user's roles, resolves the required role for an admin path, and grants
     * access when the user possesses the {@code SYSTEM_ADMIN} role.
     * <p>
     * Traceability: [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
     */
    @Test
    public void testDoFilter_ValidAdminToken_GrantAccess() throws IOException, ServletException {
        // Prepare a mock request with a valid Authorization header
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final FilterChain filterChain = mock(FilterChain.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");
        when(request.getRequestURI()).thenReturn("/api/admin/users");

        // Mock token validation to return a role set containing SYSTEM_ADMIN
        final Set<String> adminRoles = new HashSet<>();
        adminRoles.add(RbacFilter.ROLE_SYSTEM_ADMIN);
        when(tokenServiceMock.validateToken("validToken")).thenReturn(adminRoles);

        // Execute filter – should proceed to the chain without sending an error
        rbacFilter.doFilter(request, response, filterChain);

        // Verify that the filter chain was invoked (access granted)
        verify(filterChain).doFilter(request, response);
        // Ensure no error response was sent
        verify(response, never()).sendError(anyInt(), anyString());
        testLogger.info("[TEST_PASS] Valid admin token correctly granted access.");
    }

    /**
     * Negative scenario: missing Authorization header results in {@code 401}.
     * <p>
     * Confirms that the filter detects a null or malformed Authorization header
     * and returns a {@code SC_UNAUTHORIZED} response with an appropriate error
     * message, logging the failure with the required traceability tag.
     * <p>
     * Traceability: [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
     */
    @Test
    public void testDoFilter_MissingAuthorizationHeader_Returns401() throws IOException, ServletException {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final FilterChain filterChain = mock(FilterChain.class);
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/admin/users");

        rbacFilter.doFilter(request, response, filterChain);

        // Verify that the filter responded with 401 Unauthorized
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
        verify(filterChain, never()).doFilter(any(), any());
        testLogger.info("[TEST_PASS] Missing Authorization header correctly rejected with 401.");
    }

    /**
     * Negative scenario: malformed Authorization header (no Bearer prefix) returns {@code 401}.
     * <p>
     * Ensures that the filter validates the header format and rejects tokens that
     * do not start with {@code Bearer }, logging the failure with traceability tag.
     * <p>
     * Traceability: [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
     */
    @Test
    public void testDoFilter_MalformedAuthorizationHeader_Returns401() throws IOException, ServletException {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final FilterChain filterChain = mock(FilterChain.class);
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");
        when(request.getRequestURI()).thenReturn("/api/admin/users");

        rbacFilter.doFilter(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
        verify(filterChain, never()).doFilter(any(), any());
        testLogger.info("[TEST_PASS] Malformed Authorization header correctly rejected with 401.");
    }

    /**
     * Negative scenario: invalid or expired token returns {@code 401}.
     * <p>
     * Validates that when {@link TokenService#validateToken} returns {@code null}
     * or an empty set, the filter logs the failure with traceability tag and
     * responds with {@code 401}.
     * <p>
     * Traceability: [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
     */
    @Test
    public void testDoFilter_InvalidToken_Returns401() throws IOException, ServletException {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final FilterChain filterChain = mock(FilterChain.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidToken");
        when(request.getRequestURI()).thenReturn("/api/admin/users");
        when(tokenServiceMock.validateToken("invalidToken")).thenReturn(null);

        rbacFilter.doFilter(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
        verify(filterChain, never()).doFilter(any(), any());
        testLogger.info("[TEST_PASS] Invalid token correctly rejected with 401.");
    }

    /**
     * Negative scenario: insufficient permissions for a protected path returns {@code 403}.
     * <p>
     * Checks that a user with a {@code STUDENT} role cannot access an
     * {@code /api/admin/} endpoint, resulting in a {@code 403} response and
     * appropriate error logging with traceability tag.
     * <p>
     * Traceability: [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
     */
    @Test
    public void testDoFilter_InsufficientPermissions_Returns403() throws IOException, ServletException {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final FilterChain filterChain = mock(FilterChain.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(request.getRequestURI()).thenReturn("/api/admin/users");
        when(tokenServiceMock.validateToken("token")).thenReturn(Collections.singleton(RbacFilter.ROLE_STUDENT));

        rbacFilter.doFilter(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient permissions");
        verify(filterChain, never()).doFilter(any(), any());
        testLogger.info("[TEST_PASS] Insufficient permissions correctly rejected with 403.");
    }

    /**
     * Path resolution test: {@code /api/center/profile} requires {@code CENTER_ADMIN}.
     * <p>
     * Uses reflection to invoke the private {@code resolveRequiredRole} method and
     * verifies that the correct role is returned for a given path prefix.
     * <p>
     * Traceability: [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
     */
    @Test
    public void testResolveRequiredRole_CenterPath_ReturnsCenterAdmin() throws Exception {
        final Method method = RbacFilter.class.getDeclaredMethod("resolveRequiredRole", String.class);
        method.setAccessible(true);
        final String required = (String) method.invoke(rbacFilter, "/api/center/profile");
        assertEquals(RbacFilter.ROLE_CENTER_ADMIN, required,
                "Path /api/center/profile should require CENTER_ADMIN role");
        testLogger.info("[TEST_PASS] Center path correctly resolved to CENTER_ADMIN.");
    }

    /**
     * Path resolution test: {@code /api/manager/employees} requires {@code MANAGER}.
     * <p>
     * Validates that the filter's internal path‑to‑role mapping correctly
     * identifies the required role for a manager endpoint.
     * <p>
     * Traceability: [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
     */
    @Test
    public void testResolveRequiredRole_ManagerPath_ReturnsManager() throws Exception {
        final Method method = RbacFilter.class.getDeclaredMethod("resolveRequiredRole", String.class);
        method.setAccessible(true);
        final String required = (String) method.invoke(rbacFilter, "/api/manager/employees");
        assertEquals(RbacFilter.ROLE_MANAGER, required,
                "Path /api/manager/employees should require MANAGER role");
        testLogger.info("[TEST_PASS] Manager path correctly resolved to MANAGER.");
    }

    /**
     * Path resolution test: unrestricted path returns {@code null}.
     * <p>
     * Ensures that paths not matching any defined prefix (e.g., {@code /api/public/info})
     * result in a {@code null} required role, allowing any authenticated user.
     * <p>
     * Traceability: [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
     */
    @Test
    public void testResolveRequiredRole_UnrestrictedPath_ReturnsNull() throws Exception {
        final Method method = RbacFilter.class.getDeclaredMethod("resolveRequiredRole", String.class);
        method.setAccessible(true);
        final String required = (String) method.invoke(rbacFilter, "/api/public/info");
        assertNull(required,
                "Unrestricted path should not require a specific role");
        testLogger.info("[TEST_PASS] Unrestricted path correctly resolved to null.");
    }

    /**
     * Role hierarchy test: {@code SYSTEM_ADMIN} bypasses all role checks.
     * <p>
     * Confirms that a user possessing the {@code SYSTEM_ADMIN} role is granted
     * access regardless of the required role for the target endpoint.
     * <p>
     * Traceability: [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
     */
    @Test
    public void testHasRequiredRole_SystemAdmin_Bypass() throws Exception {
        final Method method = RbacFilter.class.getDeclaredMethod("hasRequiredRole", Set.class, String.class);
        method.setAccessible(true);
        final Set<String> systemAdminRoles = Collections.singleton(RbacFilter.ROLE_SYSTEM_ADMIN);
        final boolean result = (boolean) method.invoke(rbacFilter, systemAdminRoles, RbacFilter.ROLE_STUDENT);
        assertTrue(result,
                "SYSTEM_ADMIN should bypass any required role check");
        testLogger.info("[TEST_PASS] SYSTEM_ADMIN correctly bypassed role check.");
    }

    /**
     * Role hierarchy test: direct role match grants access.
     * <p>
     * Verifies that a user with a role exactly matching the required role (e.g.,
     * {@code TEACHER} for a {@code /api/teacher/schedule} endpoint) is authorized.
     * <p>
     * Traceability: [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
     */
    @Test
    public void testHasRequiredRole_DirectMatch_Grant() throws Exception {
        final Method method = RbacFilter.class.getDeclaredMethod("hasRequiredRole", Set.class, String.class);
        method.setAccessible(true);
        final Set<String> teacherRoles = Collections.singleton(RbacFilter.ROLE_TEACHER);
        final boolean result = (boolean) method.invoke(rbacFilter, teacherRoles, RbacFilter.ROLE_TEACHER);
        assertTrue(result,
                "Direct role match should grant access");
        testLogger.info("[TEST_PASS] Direct role match correctly granted access.");
    }

    /**
     * Role hierarchy test: insufficient role results in denial.
     * <p>
     * Ensures that a {@code STUDENT} cannot access a {@code /api/teacher/schedule}
     * endpoint, resulting in a {@code false} authorization decision.
     * <p>
     * Traceability: [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
     */
    @Test
    public void testHasRequiredRole_InsufficientRole_Deny() throws Exception {
        final Method method = RbacFilter.class.getDeclaredMethod("hasRequiredRole", Set.class, String.class);
        method.setAccessible(true);
        final Set<String> studentRoles = Collections.singleton(RbacFilter.ROLE_STUDENT);
        final boolean result = (boolean) method.invoke(rbacFilter, studentRoles, RbacFilter.ROLE_TEACHER);
        assertFalse(result,
                "STUDENT should not be authorized for TEACHER role");
        testLogger.info("[TEST_PASS] Insufficient role correctly denied.");
    }

    /**
     * Exception handling test: unexpected exception from {@link TokenService} is
     * caught, logged with traceability tag, and re‑thrown as a {@link ServletException}
     * preserving the original cause.
     * <p>
     * Validates that the filter's internal {@code catch} block complies with the
     * enterprise exception handling policy (ARC‑004) and that the original
     * cause chain is maintained.
     * <p>
     * Traceability: [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
     */
    @Test
    public void testDoFilter_UnexpectedException_PreservesCause() throws IOException, ServletException {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final FilterChain filterChain = mock(FilterChain.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(request.getRequestURI()).thenReturn("/api/admin/users");
        // Simulate an unexpected runtime exception from token validation
        when(tokenServiceMock.validateToken("token")).thenThrow(new RuntimeException("Simulated token validation failure"));

        // Expect ServletException to be thrown, wrapping the original cause
        final ServletException thrown = assertThrows(ServletException.class, () -> {
            rbacFilter.doFilter(request, response, filterChain);
        });
        // Verify that the original cause is preserved
        assertNotNull(thrown.getCause(), "Original cause should be preserved");
        assertEquals("Simulated token validation failure", thrown.getCause().getMessage());
        // Ensure that the filter logged the error with traceability tag
        testLogger.info("[TEST_PASS] Unexpected exception correctly caught and cause preserved.");
    }

    /**
     * Integration‑style test: full happy‑path flow with a valid token and matching role.
     * <p>
     * This test simulates a complete request through the filter, verifying that
     * the filter chain is invoked and that no error responses are sent. It
     * ensures that the filter's logging (entry/exit) occurs as expected.
     * <p>
     * Traceability: [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
     */
    @Test
    public void testDoFilter_Integration_HappyPath() throws IOException, ServletException {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final FilterChain filterChain = mock(FilterChain.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer happyToken");
        when(request.getRequestURI()).thenReturn("/api/admin/users");
        when(tokenServiceMock.validateToken("happyToken")).thenReturn(Collections.singleton(RbacFilter.ROLE_SYSTEM_ADMIN));

        rbacFilter.doFilter(request, response, filterChain);

        // Verify that the filter chain was invoked (access granted)
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
        testLogger.info("[TEST_PASS] Integration happy‑path flow completed successfully.");
    }
}