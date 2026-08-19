package org.nlh4j.saas.membership_hub.auth;

// Import statements for JAX-RS, MicroProfile JWT, logging and utility classes [ARC-001]
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import org.jboss.logging.Logger;
import org.eclipse.microprofile.jwt.JsonWebToken;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * RBAC (Role-Based Access Control) Global Filter for Membership Hub System
 * <p>
 * This filter enforces role-based access control policies for all API endpoints, integrating
 * with JWT authentication to validate user permissions and center-level access for Center Admin roles.
 * It complies with OWASP Top 10 security standards and enterprise audit logging requirements.
 * </p>
 *
 * @traceability [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
 * @author Enterprise Core Engineering Team
 * @version 1.0
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
public class RbacFilter implements ContainerRequestFilter {
    // -------------------------------------------------------------------------
    // TOP-OF-CLASS CONSTANTS DECLARATION (COMPLIES WITH ENTERPRISE CLEAN CODE STANDARDS [0.2])
    // -------------------------------------------------------------------------
    // Logger initialization for comprehensive audit logging [0.3]
    private static final Logger logger = LoggerFactory.getLogger(RbacFilter.class);
    // RBAC Role Constants [ARC-001]
    public static final String ROLE_SYSTEM_ADMIN = "System Admin";
    public static final String ROLE_CENTER_ADMIN = "Center Admin";
    public static final String ROLE_MANAGER = "Manager";
    public static final String ROLE_TEACHER = "Teacher";
    public static final String ROLE_STUDENT = "Student";
    // JWT Claim Constants [ARC-006]
    public static final String JWT_CLAIM_ROLES = "roles";
    public static final String JWT_CLAIM_USER_ID = "userId";
    public static final String JWT_CLAIM_CENTER_IDS = "centerIds";
    // Public Endpoint Paths (no authentication/authorization required) [ARC-001]
    public static final Set<String> PUBLIC_ENDPOINT_PATHS = Set.of(
        "/api/v1/auth/register",
        "/api/v1/auth/login",
        "/api/v1/auth/oauth2",
        "/q/health",
        "/q/metrics",
        "/q/openapi"
    );
    // Request Parameter/Path Constants [ARC-002]
    public static final String PATH_PARAM_CENTER_ID = "centerId";
    public static final String QUERY_PARAM_CENTER_ID = "centerId";
    // HTTP Method Constants [ARC-001]
    public static final String HTTP_GET = "GET";
    public static final String HTTP_POST = "POST";
    public static final String HTTP_PUT = "PUT";
    public static final String HTTP_DELETE = "DELETE";
    // RBAC Permission Matrix: Endpoint path prefix -> HTTP Method -> Set of allowed roles [ARC-001, ARC-002, ARC-003, ARC-004, ARC-005]
    // Defines granular access control per endpoint category and HTTP method
    private static final Map<String, Map<String, Set<String>>> ENDPOINT_PERMISSION_MATRIX = Map.ofEntries(
        // Admin center management endpoints: System Admin has full access, Center Admin can manage their own centers
        Map.entry("/api/v1/admin/centers", Map.of(
            HTTP_GET, Set.of(ROLE_SYSTEM_ADMIN, ROLE_CENTER_ADMIN),
            HTTP_POST, Set.of(ROLE_SYSTEM_ADMIN),
            HTTP_PUT, Set.of(ROLE_SYSTEM_ADMIN, ROLE_CENTER_ADMIN),
            HTTP_DELETE, Set.of(ROLE_SYSTEM_ADMIN)
        )),
        // Course management endpoints: GET allowed for all authenticated roles, modification only for Admin/Center Admin
        Map.entry("/api/v1/courses", Map.of(
            HTTP_GET, Set.of(ROLE_SYSTEM_ADMIN, ROLE_CENTER_ADMIN, ROLE_TEACHER, ROLE_STUDENT, ROLE_MANAGER),
            HTTP_POST, Set.of(ROLE_SYSTEM_ADMIN, ROLE_CENTER_ADMIN),
            HTTP_PUT, Set.of(ROLE_SYSTEM_ADMIN, ROLE_CENTER_ADMIN),
            HTTP_DELETE, Set.of(ROLE_SYSTEM_ADMIN, ROLE_CENTER_ADMIN)
        )),
        // Enrollment endpoints: Students can enroll, Admin/Manager/Teacher can view enrollment records
        Map.entry("/api/v1/enrollments", Map.of(
            HTTP_GET, Set.of(ROLE_SYSTEM_ADMIN, ROLE_CENTER_ADMIN, ROLE_MANAGER, ROLE_TEACHER),
            HTTP_POST, Set.of(ROLE_STUDENT)
        )),
        // Available courses endpoint: only accessible to Students for course browsing
        Map.entry("/api/v1/courses/available", Map.of(
            HTTP_GET, Set.of(ROLE_STUDENT)
        )),
        // Attendance endpoints: Students can scan QR for attendance, Admin/Teacher can view attendance records
        Map.entry("/api/v1/attendance", Map.of(
            HTTP_GET, Set.of(ROLE_SYSTEM_ADMIN, ROLE_CENTER_ADMIN, ROLE_TEACHER),
            HTTP_POST, Set.of(ROLE_STUDENT)
        )),
        // Membership card endpoints: only accessible to Students for card viewing and renewal
        Map.entry("/api/membership", Map.of(
            HTTP_GET, Set.of(ROLE_STUDENT),
            HTTP_POST, Set.of(ROLE_STUDENT)
        )),
        // Notification endpoints: all roles can view their own notifications, Admin/Manager can send notifications
        Map.entry("/api/notifications", Map.of(
            HTTP_GET, Set.of(ROLE_SYSTEM_ADMIN, ROLE_CENTER_ADMIN, ROLE_MANAGER, ROLE_TEACHER, ROLE_STUDENT),
            HTTP_POST, Set.of(ROLE_SYSTEM_ADMIN, ROLE_CENTER_ADMIN, ROLE_MANAGER)
        )),
        // Promotion endpoints: all roles can view promotions, only Admin/Manager can manage promotions
        Map.entry("/api/promotions", Map.of(
            HTTP_GET, Set.of(ROLE_SYSTEM_ADMIN, ROLE_CENTER_ADMIN, ROLE_MANAGER, ROLE_TEACHER, ROLE_STUDENT),
            HTTP_POST, Set.of(ROLE_SYSTEM_ADMIN, ROLE_CENTER_ADMIN, ROLE_MANAGER),
            HTTP_PUT, Set.of(ROLE_SYSTEM_ADMIN, ROLE_CENTER_ADMIN, ROLE_MANAGER),
            HTTP_DELETE, Set.of(ROLE_SYSTEM_ADMIN, ROLE_CENTER_ADMIN, ROLE_MANAGER)
        )),
        // Announcement endpoints: all roles can view announcements, only Admin/Manager can manage announcements
        Map.entry("/api/announcements", Map.of(
            HTTP_GET, Set.of(ROLE_SYSTEM_ADMIN, ROLE_CENTER_ADMIN, ROLE_MANAGER, ROLE_TEACHER, ROLE_STUDENT),
            HTTP_POST, Set.of(ROLE_SYSTEM_ADMIN, ROLE_CENTER_ADMIN, ROLE_MANAGER),
            HTTP_PUT, Set.of(ROLE_SYSTEM_ADMIN, ROLE_CENTER_ADMIN, ROLE_MANAGER),
            HTTP_DELETE, Set.of(ROLE_SYSTEM_ADMIN, ROLE_CENTER_ADMIN, ROLE_MANAGER)
        )),
        // Report endpoints: only accessible to Admin/Manager for report generation
        Map.entry("/api/v1/reports", Map.of(
            HTTP_GET, Set.of(ROLE_SYSTEM_ADMIN, ROLE_CENTER_ADMIN, ROLE_MANAGER)
        )),
        // Dashboard endpoints: only accessible to Admin/Manager for dashboard viewing
        Map.entry("/api/v1/dashboard", Map.of(
            HTTP_GET, Set.of(ROLE_SYSTEM_ADMIN, ROLE_CENTER_ADMIN, ROLE_MANAGER)
        )),
        // User management endpoints: only accessible to System Admin for user role management
        Map.entry("/api/v1/admin/users", Map.of(
            HTTP_GET, Set.of(ROLE_SYSTEM_ADMIN),
            HTTP_PUT, Set.of(ROLE_SYSTEM_ADMIN)
        ))
    );
    // Error Message Constants [NFR-003]
    public static final String ERROR_MSG_UNAUTHORIZED = "Missing or invalid authentication token";
    public static final String ERROR_MSG_NO_ROLES = "User has no assigned roles";
    public static final String ERROR_MSG_INSUFFICIENT_PERMISSIONS = "Insufficient permissions to access this endpoint";
    public static final String ERROR_MSG_CENTER_ACCESS_DENIED = "You do not have access to the requested center";
    public static final String ERROR_MSG_ACCESS_CONTROL_FAILED = "Access control validation failed";
    // Inject JWT token to extract user identity and role claims [ARC-006]
    @Context
    private JsonWebToken jwtToken;
    /**
     * Global RBAC filter entry point that processes all incoming API requests
     * Enforces role-based access control and center-level permission validation
     * [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
     *
     * @param requestContext the JAX-RS request context
     */
    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Entry point audit log for request tracing [0.3]
        String requestPath = requestContext.getUriInfo().getPath();
        String httpMethod = requestContext.getMethod();
        logger.debug("[RBAC_PROCESS_START] Processing access request | Path: {} | Method: {}", requestPath, httpMethod);
        try {
            // Step 1: Skip RBAC validation for public endpoints (auth, health checks) [ARC-001]
            if (isPublicEndpoint(requestPath)) {
                logger.debug("[RBAC_PUBLIC_ENDPOINT] Access granted to public endpoint: {}", requestPath);
                return;
            }
            // Step 2: Validate that user is authenticated via JWT [ARC-006]
            if (jwtToken == null || jwtToken.getClaim(JWT_CLAIM_USER_ID) == null) {
                logger.warn("[RBAC_UNAUTHORIZED] Unauthenticated access attempt | Path: {} | Method: {}", requestPath, httpMethod);
                abortWithUnauthorized(requestContext, ERROR_MSG_UNAUTHORIZED);
                return;
            }
            // Step 3: Extract user identity, roles and assigned centers from JWT claims [ARC-001]
            String userId = jwtToken.getClaim(JWT_CLAIM_USER_ID).toString();
            List<String> userRoles = jwtToken.getClaim(JWT_CLAIM_ROLES).asList(String.class);
            List<UUID> userAssignedCenterIds = jwtToken.getClaim(JWT_CLAIM_CENTER_IDS).asList(UUID.class);
            // Validate user has at least one assigned role
            if (userRoles == null || userRoles.isEmpty()) {
                logger.warn("[RBAC_FORBIDDEN] User {} has no assigned roles | Path: {} | Method: {}", userId, requestPath, httpMethod);
                abortWithForbidden(requestContext, ERROR_MSG_NO_ROLES);
                return;
            }
            // Step 4: Grant full unrestricted access to System Admin role [ARC-001]
            if (userRoles.contains(ROLE_SYSTEM_ADMIN)) {
                logger.debug("[RBAC_ACCESS_GRANTED] System Admin {} granted full access | Path: {} | Method: {}", userId, requestPath, httpMethod);
                return;
            }
            // Step 5: Validate endpoint access permission for user's assigned roles [ARC-001, ARC-002]
            boolean hasEndpointPermission = userRoles.stream()
                .anyMatch(role -> isEndpointAllowedForRole(requestPath, httpMethod, role));
            if (!hasEndpointPermission) {
                logger.warn("[RBAC_FORBIDDEN] User {} with roles {} denied access to endpoint | Path: {} | Method: {}", 
                    userId, userRoles, requestPath, httpMethod);
                abortWithForbidden(requestContext, ERROR_MSG_INSUFFICIENT_PERMISSIONS);
                return;
            }
            // Step 6: Validate center-level access for Center Admin role [ARC-002]
            if (userRoles.contains(ROLE_CENTER_ADMIN)) {
                UUID requestedCenterId = extractCenterIdFromRequest(requestContext);
                // Only validate if request targets a specific center
                if (requestedCenterId != null) {
                    boolean hasCenterAccess = userAssignedCenterIds != null && userAssignedCenterIds.contains(requestedCenterId);
                    if (!hasCenterAccess) {
                        logger.warn("[RBAC_FORBIDDEN] Center Admin {} attempted to access unassigned center {} | Path: {} | Method: {}", 
                            userId, requestedCenterId, requestPath, httpMethod);
                        abortWithForbidden(requestContext, ERROR_MSG_CENTER_ACCESS_DENIED);
                        return;
                    }
                }
            }
            // Step 7: Access granted, log successful validation [0.3]
            logger.debug("[RBAC_ACCESS_GRANTED] User {} granted access | Path: {} | Method: {}", userId, requestPath, httpMethod);
        } catch (Exception e) {
            // Comprehensive exception logging with traceability tags and raw error details [0.3]
            logger.error("[CRITICAL FAIL] [ARC-001] RBAC filter processing failed | Path: {} | Method: {} | Raw error: {}", 
                requestPath, httpMethod, e.getMessage(), e);
            // Abort with generic error to avoid leaking sensitive system information [NFR-003]
            abortWithForbidden(requestContext, ERROR_MSG_ACCESS_CONTROL_FAILED);
        }
    }
    /**
     * Checks if the request path is a public endpoint that does not require authentication
     * [ARC-001]
     *
     * @param requestPath the incoming request path
     * @return true if the endpoint is public, false otherwise
     */
    private boolean isPublicEndpoint(String requestPath) {
        // Check for exact path match first for performance
        if (PUBLIC_ENDPOINT_PATHS.contains(requestPath)) {
            return true;
        }
        // Check for prefix match to cover dynamic public endpoints (e.g. OAuth2 with provider parameter)
        return PUBLIC_ENDPOINT_PATHS.stream()
            .anyMatch(requestPath::startsWith);
    }
    /**
     * Extracts centerId from request path parameters or query parameters
     * [ARC-002]
     *
     * @param requestContext the JAX-RS request context
     * @return UUID of the requested center, or null if not present in request
     */
    private UUID extractCenterIdFromRequest(ContainerRequestContext requestContext) {
        // First check path parameters (e.g. /api/v1/centers/{centerId}/courses)
        List<String> pathCenterIds = requestContext.getUriInfo().getPathParameters().get(PATH_PARAM_CENTER_ID);
        if (pathCenterIds != null && !pathCenterIds.isEmpty()) {
            return UUID.fromString(pathCenterIds.get(0));
        }
        // Then check query parameters (e.g. /api/v1/courses?centerId=xxx)
        String queryCenterId = requestContext.getUriInfo().getQueryParameters().getFirst(QUERY_PARAM_CENTER_ID);
        if (queryCenterId != null && !queryCenterId.isBlank()) {
            return UUID.fromString(queryCenterId);
        }
        // No centerId found in request
        return null;
    }
    /**
     * Validates if the user's role has permission to access the requested endpoint with the specified HTTP method
     * [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
     *
     * @param requestPath the incoming request path
     * @param httpMethod the HTTP method of the request (GET/POST/PUT/DELETE)
     * @param role the user's role to validate
     * @return true if the role is allowed to access the endpoint, false otherwise
     */
    private boolean isEndpointAllowedForRole(String requestPath, String httpMethod, String role) {
        // First check for exact path match in permission matrix for performance
        if (ENDPOINT_PERMISSION_MATRIX.containsKey(requestPath)) {
            Map<String, Set<String>> methodPermissions = ENDPOINT_PERMISSION_MATRIX.get(requestPath);
            if (methodPermissions.containsKey(httpMethod)) {
                return methodPermissions.get(httpMethod).contains(role);
            }
            // Deny access if HTTP method is not defined for the exact path
            return false;
        }
        // Then check for prefix match to cover dynamic subpaths (e.g. /api/v1/courses/{courseId}/assign-teacher)
        for (Map.Entry<String, Map<String, Set<String>>> matrixEntry : ENDPOINT_PERMISSION_MATRIX.entrySet()) {
            String pathPrefix = matrixEntry.getKey();
            if (requestPath.startsWith(pathPrefix)) {
                Map<String, Set<String>> methodPermissions = matrixEntry.getValue();
                if (methodPermissions.containsKey(httpMethod)) {
                    return methodPermissions.get(httpMethod).contains(role);
                }
                // Deny access if HTTP method is not defined for the path prefix
                return false;
            }
        }
        // Default deny access for undefined endpoints (security best practice to prevent unauthorized access to new endpoints) [NFR-003]
        logger.warn("[RBAC_UNDEFINED_ENDPOINT] Access attempt to undefined endpoint | Path: {} | Method: {} | Role: {}", 
            requestPath, httpMethod, role);
        return false;
    }
    /**
     * Aborts the request with 401 Unauthorized response
     * [ARC-006]
     *
     * @param requestContext the JAX-RS request context
     * @param message the error message to return to client
     */
    private void abortWithUnauthorized(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
            .entity("{\"error\": \"UNAUTHORIZED\", \"message\": \"" + message + "\"}")
            .type("application/json")
            .build());
    }
    /**
     * Aborts the request with 403 Forbidden response
     * [ARC-001]
     *
     * @param requestContext the JAX-RS request context
     * @param message the error message to return to client
     */
    private void abortWithForbidden(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
            .entity("{\"error\": \"ACCESS_DENIED\", \"message\": \"" + message + "\"}")
            .type("application/json")
            .build());
    }
}