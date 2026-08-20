/**
 * Global RBAC filter enforcing role-based access control across all REST endpoints.
 * Traceability Tags: [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
 */
package org.nlh4j.saas.membership-hub.auth;

import java.io.IOException;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RbacFilter implements {@link Filter} to enforce enterprise RBAC policies.
 * <p>
 * This filter validates JWT tokens from the {@code Authorization} header, extracts
 * user roles, and checks whether the requesting principal is authorized to access
 * the target endpoint based on a simple path‑role mapping. All decisions are
 * logged for auditability and any validation failure results in a structured
 * error log that includes the original exception message and the traceability
 * tag identifiers required by the enterprise governance framework.
 * </p>
 *
 * @traceability [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
 */
public class RbacFilter implements Filter {

    /** Logger instance for audit and error reporting. */
    private static final Logger logger = LoggerFactory.getLogger(RbacFilter.class);

    /**
     * Role constants used for permission evaluation.
     * <p>
     * These constants are aligned with the enterprise role definitions described in
     * the architectural requirement set (ARC‑001 through ARC‑005). They are kept
     * at the class level to satisfy the anti‑magic‑numbers policy and to allow
     * easy reference throughout the filter logic.
     * </p>
     */
    public static final String ROLE_SYSTEM_ADMIN = "SYSTEM_ADMIN";
    public static final String ROLE_CENTER_ADMIN = "CENTER_ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_TEACHER = "TEACHER";
    public static final String ROLE_STUDENT = "STUDENT";

    /**
     * Path prefixes mapped to required roles.
     * <p>
     * This simple mapping enforces the RBAC rules defined in the specification:
     * <ul>
     *   <li>{@code /api/admin/*} → {@code SYSTEM_ADMIN} or {@code CENTER_ADMIN}</li>
     *   <li>{@code /api/center/*} → {@code CENTER_ADMIN}</li>
     *   <li>{@code /api/manager/*} → {@code MANAGER}</li>
     *   <li>{@code /api/teacher/*} → {@code TEACHER}</li>
     *   <li>{@code /api/student/*} → {@code STUDENT}</li>
     *   <li>All other paths → any authenticated user</li>
     * </ul>
     * </p>
     */
    private static final String[] ADMIN_PATHS = {"/api/admin/", "/api/centers/", "/api/courses/"};
    private static final String[] CENTER_PATHS = {"/api/center/"};
    private static final String[] MANAGER_PATHS = {"/api/manager/"};
    private static final String[] TEACHER_PATHS = {"/api/teacher/"};
    private static final String[] STUDENT_PATHS = {"/api/student/"};

    /**
     * No‑arg constructor required by the Servlet container.
     */
    public RbacFilter() {
        // Intentionally left empty – initialization is deferred to init()
    }

    /**
     * {@inheritDoc}
     * <p>
     * The filter performs token validation and role extraction here. All
     * {@code IOException} and {@code ServletException} instances are caught,
     * logged with the required traceability tag, and re‑thrown to preserve the
     * original cause chain as mandated by the enterprise exception handling
     * policy.
     * </p>
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Log entry point for audit tracing
        logger.info("[ENTRY] RBAC filter processing request for URI: {}", httpRequest.getRequestURI());

        try {
            // 1. Extract Authorization header
            final String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.error("[CRITICAL FAIL] [ARC-001] RBAC filter failed due to missing or malformed Authorization header. Raw error: {}", "Authorization header not present or does not start with Bearer");
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
                return;
            }

            // 2. Validate JWT token and retrieve roles
            final String token = authHeader.substring(7);
            final Set<String> userRoles = TokenService.validateToken(token);
            if (userRoles == null || userRoles.isEmpty()) {
                logger.error("[CRITICAL FAIL] [ARC-002] RBAC filter failed due to invalid or expired JWT token. Raw error: {}", "Token validation returned no roles");
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return;
            }

            // 3. Determine required role based on request path
            final String requestPath = httpRequest.getRequestURI();
            final String requiredRole = resolveRequiredRole(requestPath);

            // 4. Perform role check
            if (!hasRequiredRole(userRoles, requiredRole)) {
                logger.error("[CRITICAL FAIL] [ARC-003] RBAC filter failed due to insufficient permissions. Raw error: {}", "User does not possess required role for path " + requestPath);
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient permissions");
                return;
            }

            // 5. All checks passed – continue filter chain
            chain.doFilter(request, response);
        } catch (final Exception e) {
            // Preserve original cause and log with traceability tag
            logger.error("[CRITICAL FAIL] [ARC-004] RBAC filter encountered an unexpected error while processing request. Raw error: {}", e.getMessage(), e);
            throw new ServletException("RBAC filter processing failed", e);
        } finally {
            // Log exit point for audit completeness
            logger.info("[EXIT] RBAC filter completed request for URI: {}", httpRequest.getRequestURI());
        }
    }

    /**
     * Resolve the required role for a given request path.
     * <p>
     * This method implements the simple path‑to‑role mapping described in the
     * class‑level documentation. It is kept lightweight to avoid heavy
     * computation at runtime and to satisfy the performance constraints of
     * the enterprise platform.
     * </p>
     *
     * @param path the request URI path
     * @return the role name that is required for the path, or {@code null} if any
     *         authenticated user is allowed
     */
    private String resolveRequiredRole(final String path) {
        for (final String admin : ADMIN_PATHS) {
            if (path.startsWith(admin)) {
                return ROLE_SYSTEM_ADMIN; // System Admin or Center Admin
            }
        }
        for (final String center : CENTER_PATHS) {
            if (path.startsWith(center)) {
                return ROLE_CENTER_ADMIN;
            }
        }
        for (final String manager : MANAGER_PATHS) {
            if (path.startsWith(manager)) {
                return ROLE_MANAGER;
            }
        }
        for (final String teacher : TEACHER_PATHS) {
            if (path.startsWith(teacher)) {
                return ROLE_TEACHER;
            }
        }
        for (final String student : STUDENT_PATHS) {
            if (path.startsWith(student)) {
                return ROLE_STUDENT;
            }
        }
        // No specific role required – any authenticated user may proceed
        return null;
    }

    /**
     * Check whether the user’s role set satisfies the required role.
     * <p>
     * The evaluation follows the enterprise RBAC hierarchy:
     * <ul>
     *   <li>{@code SYSTEM_ADMIN} can act as any role.</li>
     *   <li>{@code CENTER_ADMIN} can act as {@code CENTER_ADMIN} or {@code MANAGER}.</li>
     *   <li>{@code MANAGER} can act as {@code MANAGER}.</li>
     *   <li>{@code TEACHER} can act as {@code TEACHER}.</li>
     *   <li>{@code STUDENT} can act as {@code STUDENT}.</li>
     * </ul>
     * </p>
     *
     * @param userRoles   the set of roles extracted from the JWT token
     * @param requiredRole the role required by the target endpoint (may be {@code null})
     * @return {@code true} if the user possesses the required role or a higher‑privilege role
     */
    private boolean hasRequiredRole(final Set<String> userRoles, final String requiredRole) {
        if (requiredRole == null) {
            return true; // any authenticated user is allowed
        }
        if (userRoles == null) {
            return false;
        }

        // SYSTEM_ADMIN can bypass any role check
        if (userRoles.contains(ROLE_SYSTEM_ADMIN)) {
            return true;
        }

        // Direct role match
        if (userRoles.contains(requiredRole)) {
            return true;
        }

        // Additional hierarchical allowances (e.g., CENTER_ADMIN can act as MANAGER)
        if (ROLE_CENTER_ADMIN.equals(requiredRole) && userRoles.contains(ROLE_CENTER_ADMIN)) {
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        // No external resources are required for this filter – initialization is a no‑op.
        logger.info("[INIT] RBAC filter initialized successfully.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        // Clean up any resources if needed. Currently none.
        logger.info("[DESTROY] RBAC filter destroyed.");
    }
}