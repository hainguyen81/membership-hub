package org.nlh4j.saas.membership_hub.auth;

// [IMPORT SECTION: Core JAX-RS and Quarkus dependencies]
import java.util.Arrays;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceMethodInvoker;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.Priorities;
import javax.inject.Inject;
import org.jboss.logging.Logger;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.nlh4j.saas.membership_hub.entity.User;
import org.nlh4j.saas.membership_hub.enums.UserRole;
import org.nlh4j.saas.membership_hub.service.UserService;

/**
 * Global RBAC (Role-Based Access Control) filter for all API endpoints in the membership-hub system.
 * <p>
 * This filter intercepts all incoming HTTP requests, validates JWT authentication tokens,
 * enforces role-based access permissions, and ensures center-level access isolation for Center Admin roles.
 * It complies with OWASP Top 10 security standards, enforces least privilege access principles,
 * and integrates with the enterprise audit logging framework for full traceability of all access attempts.
 * </p>
 *
 * @traceability [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
 * @author Enterprise Core Security Team
 * @version 1.0
 */
@Provider // Register as JAX-RS provider for automatic request interception
@Priority(Priorities.AUTHORIZATION) // Ensure execution after authentication filters
public class RbacFilter implements ContainerRequestFilter {
    // -------------------------------------------------------------------------
    // [ENTERPRISE LOGGING COMPLIANCE: Mandatory logger initialization per governance matrix]
    // -------------------------------------------------------------------------
    private static final Logger logger = Logger.getLogger(RbacFilter.class);

    // -------------------------------------------------------------------------
    // [CONSTANTS DECLARATION: All hardcoded values isolated at class crown per anti-magic-numbers policy]
    // -------------------------------------------------------------------------
    public static final class Constants {
        // JWT Token Configuration
        public static final String JWT_AUTH_HEADER = "Authorization";
        public static final String JWT_BEARER_PREFIX = "Bearer ";
        public static final String JWT_ROLE_CLAIM = "role";
        public static final String JWT_USER_ID_CLAIM = "user_id";
        public static final String JWT_CENTER_IDS_CLAIM = "assigned_center_ids";

        // HTTP Response Status Codes
        public static final Response.Status STATUS_UNAUTHORIZED = Response.Status.UNAUTHORIZED;
        public static final Response.Status STATUS_FORBIDDEN = Response.Status.FORBIDDEN;

        // Error Message Templates (no hardcoded strings in business logic)
        public static final String ERR_MISSING_TOKEN = "Missing or invalid Authorization token";
        public static final String ERR_INVALID_TOKEN = "Invalid or expired JWT token";
        public static final String ERR_INSUFFICIENT_PERMISSIONS = "User does not have sufficient permissions to access this resource";
        public static final String ERR_CENTER_ACCESS_DENIED = "User is not authorized to access resources for the specified center";
        public static final String ERR_USER_ACCESS_DENIED = "User is not authorized to access this user's resources";

        // Audit Log Message Templates
        public static final String LOG_ACCESS_DENIED = "[RBAC] Access denied for user ID: {} to resource: {} {}. Reason: {}";
        public static final String LOG_ACCESS_GRANTED = "[RBAC] Access granted for user ID: {} with role {} to resource: {} {}";
        public static final String LOG_PROCESSING_ERROR = "[RBAC] Error processing RBAC check for request to {}. Raw error: {}";

        // Path Parameter Keys
        public static final String PATH_PARAM_CENTER_ID = "centerId";
        public static final String PATH_PARAM_USER_ID = "userId";
    }

    // -------------------------------------------------------------------------
    // [DEPENDENCY INJECTION: Quarkus native injection for JWT and user service]
    // -------------------------------------------------------------------------
    @Inject
    JsonWebToken jwt; // SmallRye JWT for token claim extraction

    @Inject
    UserService userService; // Service for user/center association checks

    // -------------------------------------------------------------------------
    // [CORE RBAC FILTER LOGIC: Intercepts all requests for access control]
    // -------------------------------------------------------------------------
    @Override
    public void filter(ContainerRequestContext requestContext) {
        // [ARC-001] Log entry point of RBAC check for audit trail compliance
        logger.debug("[RBAC] Starting RBAC check for request: {} {}", requestContext.getMethod(), requestContext.getUri().getPath());

        try {
            // --------------------------
            // Step 1: Validate JWT Token
            // --------------------------
            String authHeader = requestContext.getHeaderString(Constants.JWT_AUTH_HEADER);
            // Check for missing or malformed Authorization header
            if (authHeader == null || !authHeader.startsWith(Constants.JWT_BEARER_PREFIX)) {
                logger.warn(Constants.LOG_ACCESS_DENIED, "anonymous", requestContext.getUri().getPath(), "Missing or invalid token");
                requestContext.abortWith(Response.status(Constants.STATUS_UNAUTHORIZED.getStatusCode())
                        .entity("{\"error\": \"UNAUTHORIZED\", \"message\": \"" + Constants.ERR_MISSING_TOKEN + "\"}")
                        .build());
                return;
            }

            // Extract raw JWT token without Bearer prefix
            String token = authHeader.substring(Constants.JWT_BEARER_PREFIX.length());
            // Validate token has required claims (SmallRye JWT automatically validates signature and expiration)
            if (!jwt.getClaimNames().contains(Constants.JWT_USER_ID_CLAIM)) {
                logger.warn(Constants.LOG_ACCESS_DENIED, "anonymous", requestContext.getUri().getPath(), "Invalid token claims");
                requestContext.abortWith(Response.status(Constants.STATUS_UNAUTHORIZED.getStatusCode())
                        .entity("{\"error\": \"UNAUTHORIZED\", \"message\": \"" + Constants.ERR_INVALID_TOKEN + "\"}")
                        .build());
                return;
            }

            // --------------------------
            // Step 2: Extract User Identity and Role
            // --------------------------
            String userId = jwt.getClaim(Constants.JWT_USER_ID_CLAIM);
            String roleName = jwt.getClaim(Constants.JWT_ROLE_CLAIM);
            // Convert role string to enum for type-safe comparison
            UserRole userRole;
            try {
                userRole = UserRole.valueOf(roleName.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.error(Constants.LOG_PROCESSING_ERROR, requestContext.getUri().getPath(), "Invalid role in JWT token: " + roleName);
                requestContext.abortWith(Response.status(Constants.STATUS_UNAUTHORIZED.getStatusCode())
                        .entity("{\"error\": \"UNAUTHORIZED\", \"message\": \"" + Constants.ERR_INVALID_TOKEN + "\"}")
                        .build());
                return;
            }

            logger.debug("[RBAC] Processing request for user ID: {}, role: {}", userId, userRole);

            // --------------------------
            // Step 3: System Admin Full Access Bypass
            // --------------------------
            // [ARC-001] System Admin has unrestricted access to all system resources
            if (userRole == UserRole.SYSTEM_ADMIN) {
                logger.info(Constants.LOG_ACCESS_GRANTED, userId, userRole, requestContext.getMethod(), requestContext.getUri().getPath());
                return; // No further checks required for System Admin
            }

            // --------------------------
            // Step 4: Center-Level Access Isolation Check
            // --------------------------
            // [ARC-002] Enforce center access isolation for non-System Admin roles
            String centerIdPathParam = requestContext.getUriInfo().getPathParameters().getFirst(Constants.PATH_PARAM_CENTER_ID);
            if (centerIdPathParam != null && !centerIdPathParam.isEmpty()) {
                if (!hasCenterAccess(userId, userRole, centerIdPathParam)) {
                    logger.warn(Constants.LOG_ACCESS_DENIED, userId, requestContext.getUri().getPath(), "Unauthorized center access");
                    requestContext.abortWith(Response.status(Constants.STATUS_FORBIDDEN.getStatusCode())
                            .entity("{\"error\": \"FORBIDDEN\", \"message\": \"" + Constants.ERR_CENTER_ACCESS_DENIED + "\"}")
                            .build());
                    return;
                }
            }

            // --------------------------
            // Step 5: Role-Based Permission Check
            // --------------------------
            // [ARC-003], [ARC-004], [ARC-005] Validate user role against @RolesAllowed annotation on resource
            if (!hasRequiredRolePermission(requestContext, userRole)) {
                logger.warn(Constants.LOG_ACCESS_DENIED, userId, requestContext.getUri().getPath(), "Insufficient role permissions");
                requestContext.abortWith(Response.status(Constants.STATUS_FORBIDDEN.getStatusCode())
                        .entity("{\"error\": \"FORBIDDEN\", \"message\": \"" + Constants.ERR_INSUFFICIENT_PERMISSIONS + "\"}")
                        .build());
                return;
            }

            // --------------------------
            // Step 6: User-Level Resource Access Check
            // --------------------------
            // [ARC-004] Ensure users can only access their own resources unless they are privileged roles
            String userIdPathParam = requestContext.getUriInfo().getPathParameters().getFirst(Constants.PATH_PARAM_USER_ID);
            if (userIdPathParam != null && !userIdPathParam.isEmpty() && !userId.equals(userIdPathParam)) {
                // Allow only System Admin, Center Admin, Manager, and Teacher to access other users' resources
                if (userRole != UserRole.CENTER_ADMIN && userRole != UserRole.MANAGER && userRole != UserRole.TEACHER) {
                    logger.warn(Constants.LOG_ACCESS_DENIED, userId, requestContext.getUri().getPath(), "Unauthorized access to user resource");
                    requestContext.abortWith(Response.status(Constants.STATUS_FORBIDDEN.getStatusCode())
                            .entity("{\"error\": \"FORBIDDEN\", \"message\": \"" + Constants.ERR_USER_ACCESS_DENIED + "\"}")
                            .build());
                    return;
                }
            }

            // All checks passed: log access granted
            logger.info(Constants.LOG_ACCESS_GRANTED, userId, userRole, requestContext.getMethod(), requestContext.getUri().getPath());

        } catch (Exception e) {
            // [ARC-001] [ARC-002] [ARC-003] [ARC-004] [ARC-005] Mandatory error logging with full context per enterprise logging law
            logger.error("[CRITICAL FAIL] [ARC-001] [ARC-002] [ARC-003] [ARC-004] [ARC-005] RBAC processing failed for request to {}. Raw error: {}", requestContext.getUri().getPath(), e.getMessage(), e);
            // Abort request with 500 to prevent unauthorized access due to processing failure
            requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                    .entity("{\"error\": \"INTERNAL_SERVER_ERROR\", \"message\": \"Failed to process access control check\"}")
                    .build());
        }
    }

    // -------------------------------------------------------------------------
    // [HELPER METHODS: Encapsulated business logic for RBAC checks]
    // -------------------------------------------------------------------------
    /**
     * Checks if the user has access to the specified center.
     * <p>
     * Business Rules:
     * 1. System Admin has full access to all centers
     * 2. Center Admin has access only to centers explicitly assigned to them
     * 3. Manager, Teacher, and Student have access only if they are associated with the center (enrolled in course, assigned to class, etc.)
     * </p>
     * @param userId the ID of the requesting user
     * @param userRole the role of the user
     * @param centerId the ID of the center to access
     * @return true if user has access to the center, false otherwise
     * @traceability [ARC-002]
     */
    private boolean hasCenterAccess(String userId, UserRole userRole, String centerId) {
        // System Admin has unrestricted center access
        if (userRole == UserRole.SYSTEM_ADMIN) {
            return true;
        }

        // Center Admin: check against pre-assigned centers in JWT (optimized to avoid DB call)
        if (userRole == UserRole.CENTER_ADMIN) {
            List<String> assignedCenterIds = jwt.getClaim(Constants.JWT_CENTER_IDS_CLAIM);
            // Fallback to DB query if JWT claim is missing (e.g., token issued before claim was added)
            if (assignedCenterIds == null || assignedCenterIds.isEmpty()) {
                User user = userService.findById(userId);
                assignedCenterIds = user.getAssignedCenters().stream()
                        .map(center -> center.getCenterId().toString())
                        .collect(Collectors.toList());
            }
            return assignedCenterIds.contains(centerId);
        }

        // Other roles: delegate to UserService to check association with center
        // (e.g., Teacher is assigned to active course in center, Student is enrolled in active course in center)
        return userService.isUserAssociatedWithCenter(userId, centerId);
    }

    /**
     * Checks if the user's role has permission to access the requested endpoint.
     * <p>
     * Business Rules:
     * 1. Checks for @RolesAllowed annotation on the resource method first, then on the resource class
     * 2. Denies access by default if no @RolesAllowed annotation is present (secure by default principle)
     * 3. Role comparison is case-insensitive
     * </p>
     * @param requestContext the JAX-RS request context
     * @param userRole the role of the requesting user
     * @return true if the user has the required role, false otherwise
     * @traceability [ARC-001], [ARC-003], [ARC-004], [ARC-005]
     */
    private boolean hasRequiredRolePermission(ContainerRequestContext requestContext, UserRole userRole) {
        // Retrieve the resource method being invoked from request context
        ResourceMethodInvoker methodInvoker = (ResourceMethodInvoker) requestContext.getProperty("org.jboss.resteasy.core.ResourceMethodInvoker");
        if (methodInvoker == null) {
            logger.warn("[RBAC] Could not retrieve resource method invoker for request: {} {}", requestContext.getMethod(), requestContext.getUri().getPath());
            return false; // Deny access if resource method cannot be determined
        }

        // Check for @RolesAllowed annotation on method first, then fallback to class-level annotation
        RolesAllowed rolesAllowed = methodInvoker.getMethod().getAnnotation(RolesAllowed.class);
        if (rolesAllowed == null) {
            rolesAllowed = methodInvoker.getResourceClass().getAnnotation(RolesAllowed.class);
        }

        // Secure by default: deny access if no @RolesAllowed annotation is defined
        if (rolesAllowed == null) {
            logger.debug("[RBAC] No @RolesAllowed annotation found for resource: {} {}", requestContext.getMethod(), requestContext.getUri().getPath());
            return false;
        }

        // Check if user's role is in the allowed roles list (case-insensitive comparison)
        List<String> allowedRoles = Arrays.asList(rolesAllowed.value());
        boolean hasPermission = allowedRoles.stream()
                .anyMatch(allowedRole -> allowedRole.equalsIgnoreCase(userRole.name()));

        if (!hasPermission) {
            logger.debug("[RBAC] User role {} not in allowed roles {} for resource: {}", userRole, Arrays.toString(rolesAllowed.value()), requestContext.getUri().getPath());
        }

        return hasPermission;
    }
}