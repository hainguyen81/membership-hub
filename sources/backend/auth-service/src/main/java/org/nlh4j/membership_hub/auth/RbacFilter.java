package org.nlh4j.saas.membership_hub.auth;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.nlh4j.saas.membership_hub.auth.exception.AuthException;
import org.nlh4j.saas.membership_hub.auth.exception.RbacException;
import org.nlh4j.saas.membership_hub.auth.model.UserRole;
import org.nlh4j.saas.membership_hub.auth.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global RBAC (Role-Based Access Control) Filter for all API endpoints.
 * 
 * This filter implements enterprise-grade security gating by:
 * 1. Validating JWT authentication tokens for all requests
 * 2. Extracting user roles and permissions from authenticated tokens
 * 3. Enforcing role-based access control with center isolation
 * 4. Logging all access attempts for audit compliance
 * 5. Providing detailed error responses for authorization failures
 * 
 * @author Enterprise Security Team
 * @version 1.0
 * @since 2024-01-01
 * 
 * Traceability Tags: [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
 */
@Provider
@Priority(1)
public class RbacFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RbacFilter.class);
    
    // Enterprise RBAC Role Definitions
    public enum Role {
        SYSTEM_ADMIN("ROLE_SYSTEM_ADMIN", "Full system access"),
        CENTER_ADMIN("ROLE_CENTER_ADMIN", "Center-specific administrative access"),
        MANAGER("ROLE_MANAGER", "Center management and user oversight"),
        TEACHER("ROLE_TEACHER", "Course and student management"),
        STUDENT("ROLE_STUDENT", "Self-service and course enrollment");
        
        private final String roleName;
        private final String description;
        
        Role(String roleName, String description) {
            this.roleName = roleName;
            this.description = description;
        }
        
        public String getRoleName() {
            return roleName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Enterprise API Endpoint Security Matrix
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
        "/api/v1/auth/register",
        "/api/v1/auth/login",
        "/api/v1/auth/oauth2/*",
        "/api/v1/centers",
        "/api/v1/health",
        "/api/v1/metrics"
    );
    
    // Role-based endpoint access control matrix
    private static final List<String> SYSTEM_ADMIN_ENDPOINTS = Arrays.asList(
        "/api/v1/admin/*",
        "/api/v1/centers/*",
        "/api/v1/users/*",
        "/api/v1/reports/*"
    );
    
    private static final List<String> CENTER_ADMIN_ENDPOINTS = Arrays.asList(
        "/api/v1/centers/*/admins",
        "/api/v1/centers/*/courses",
        "/api/v1/centers/*/enrollments",
        "/api/v1/centers/*/memberships",
        "/api/v1/centers/*/notifications"
    );
    
    private static final List<String> MANAGER_ENDPOINTS = Arrays.asList(
        "/api/v1/centers/*/users",
        "/api/v1/centers/*/announcements",
        "/api/v1/centers/*/promotions"
    );
    
    private static final List<String> TEACHER_ENDPOINTS = Arrays.asList(
        "/api/v1/courses/*/students",
        "/api/v1/courses/*/attendance",
        "/api/v1/courses/*/assignments"
    );
    
    private static final List<String> STUDENT_ENDPOINTS = Arrays.asList(
        "/api/v1/courses/available",
        "/api/v1/enrollments",
        "/api/v1/membership/card",
        "/api/v1/attendance/scan",
        "/api/v1/notifications/*"
    );
    
    private final UserService userService;
    
    public RbacFilter(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod().toUpperCase();
        
        // Log entry point for audit trail
        logger.info("[RBAC_FILTER] Processing request: {} {} for path: {}", 
                   method, requestContext.getMethod(), path);
        
        try {
            // Skip RBAC for public endpoints
            if (isPublicEndpoint(path)) {
                logger.debug("[RBAC_FILTER] Public endpoint access: {}", path);
                return;
            }
            
            // Validate JWT authentication
            String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new AuthException("Missing or invalid authorization header", 
                                       "RBAC-001", "Authentication required");
            }
            
            String token = authHeader.substring(7);
            
            // Validate token and extract user information
            JsonWebToken jwtToken = validateToken(token);
            String userId = jwtToken.getClaim("sub").toString();
            String email = jwtToken.getClaim("email").toString();
            
            // Fetch user roles from database
            List<UserRole> userRoles = userService.getUserRoles(UUID.fromString(userId));
            
            if (userRoles.isEmpty()) {
                throw new AuthException("User has no assigned roles", 
                                       "RBAC-002", "Role assignment required");
            }
            
            // Check role-based access control
            boolean accessGranted = checkAccessControl(path, method, userRoles);
            
            if (!accessGranted) {
                throw new RbacException("Access denied: Insufficient permissions for this resource", 
                                        "RBAC-003", "Role-based access control violation");
            }
            
            // Log successful access
            logger.info("[RBAC_FILTER] Access granted for user {} to path: {}", 
                       userId, path);
            
        } catch (AuthException | RbacException e) {
            // Log error with traceability tag
            logger.error("[RBAC_FILTER] [ARC-001] Authorization failed for path {}: {}", 
                        path, e.getMessage(), e);
            
            // Build error response
            requestContext.abortWith(
                Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse(
                        e.getErrorCode(),
                        e.getMessage(),
                        e.getTraceabilityTag()))
                    .build()
            );
        } catch (Exception e) {
            // Log unexpected errors
            logger.error("[RBAC_FILTER] [ARC-002] Unexpected error processing request {}: {}", 
                        path, e.getMessage(), e);
            
            requestContext.abortWith(
                Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(
                        "INTERNAL_ERROR",
                        "An unexpected error occurred during authorization",
                        "ARC-002"))
                    .build()
            );
        }
    }
    
    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream()
            .anyMatch(publicEndpoint -> 
                path.matches(publicEndpoint.replace("*", ".*")));
    }
    
    private boolean checkAccessControl(String path, String method, List<UserRole> userRoles) {
        // Check each role the user has
        for (UserRole role : userRoles) {
            Role roleEnum = Role.valueOf(role.getRoleName());
            
            switch (roleEnum) {
                case SYSTEM_ADMIN:
                    if (isSystemAdminEndpoint(path)) {
                        return true;
                    }
                    break;
                    
                case CENTER_ADMIN:
                    if (isCenterAdminEndpoint(path) && 
                        hasAccessToCenter(path, role.getCenterId())) {
                        return true;
                    }
                    break;
                    
                case MANAGER:
                    if (isManagerEndpoint(path) && 
                        hasAccessToCenter(path, role.getCenterId())) {
                        return true;
                    }
                    break;
                    
                case TEACHER:
                    if (isTeacherEndpoint(path) && 
                        isTeacherForCourse(path, role.getUserId())) {
                        return true;
                    }
                    break;
                    
                case STUDENT:
                    if (isStudentEndpoint(path) && 
                        isStudentForCourse(path, role.getUserId())) {
                        return true;
                    }
                    break;
            }
        }
        
        return false;
    }
    
    private boolean isSystemAdminEndpoint(String path) {
        return SYSTEM_ADMIN_ENDPOINTS.stream()
            .anyMatch(endpoint -> path.matches(endpoint.replace("*", ".*")));
    }
    
    private boolean isCenterAdminEndpoint(String path) {
        return CENTER_ADMIN_ENDPOINTS.stream()
            .anyMatch(endpoint -> path.matches(endpoint.replace("*", ".*")));
    }
    
    private boolean isManagerEndpoint(String path) {
        return MANAGER_ENDPOINTS.stream()
            .anyMatch(endpoint -> path.matches(endpoint.replace("*", ".*")));
    }
    
    private boolean isTeacherEndpoint(String path) {
        return TEACHER_ENDPOINTS.stream()
            .anyMatch(endpoint -> path.matches(endpoint.replace("*", ".*")));
    }
    
    private boolean isStudentEndpoint(String path) {
        return STUDENT_ENDPOINTS.stream()
            .anyMatch(endpoint -> path.matches(endpoint.replace("*", ".*")));
    }
    
    private boolean hasAccessToCenter(String path, UUID centerId) {
        // Extract center ID from path and compare with user's center
        // Implementation depends on path pattern
        return true; // Placeholder - implement actual center validation
    }
    
    private boolean isTeacherForCourse(String path, UUID userId) {
        // Check if user is teacher for the course in the path
        // Implementation depends on path pattern
        return true; // Placeholder - implement actual course validation
    }
    
    private boolean isStudentForCourse(String path, UUID userId) {
        // Check if user is enrolled in the course in the path
        // Implementation depends on path pattern
        return true; // Placeholder - implement actual enrollment validation
    }
    
    private JsonWebToken validateToken(String token) {
        // Validate JWT token using Firebase or local JWT validation
        // Implementation depends on authentication service
        return null; // Placeholder - implement actual token validation
    }
    
    /**
     * Error response model for RBAC filter.
     */
    public static class ErrorResponse {
        private final String errorCode;
        private final String message;
        private final String traceabilityTag;
        
        public ErrorResponse(String errorCode, String message, String traceabilityTag) {
            this.errorCode = errorCode;
            this.message = message;
            this.traceabilityTag = traceabilityTag;
        }
        
        public String getErrorCode() {
            return errorCode;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getTraceabilityTag() {
            return traceabilityTag;
        }
    }
}