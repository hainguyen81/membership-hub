package org.nlh4j.saas.membership_hub.user;

// ====================== IMPORTS ======================
import java.util.UUID;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.nlh4j.saas.membership_hub.dto.PageResponse;
import org.nlh4j.saas.membership_hub.dto.RoleUpdateRequest;
import org.nlh4j.saas.membership_hub.dto.UserResponse;
import org.nlh4j.saas.membership_hub.entity.Role;
import org.nlh4j.saas.membership_hub.exception.MembershipHubException;
import org.nlh4j.saas.membership_hub.service.RoleService;
import org.nlh4j.saas.membership_hub.service.UserService;
import org.nlh4j.saas.membership_hub.util.DataMaskingUtil;

/**
 * REST Resource for user and role management operations in the membership-hub system.
 * Implements RBAC (Role-Based Access Control) compliant endpoints for user listing and role assignment,
 * enforcing strict permission isolation per enterprise security architecture requirements.
 * 
 * @traceability [REQ-003], [ARC-001]
 */
@Path("/api/v1/admin/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("SYSTEM_ADMIN") // Enforce RBAC: only System Admin can access these endpoints per [ARC-001]
public class UserResource {
    // ====================== TOP-OF-CLASS CONSTANTS (NO HARDCODED LITERALS IN METHOD BODIES) ======================
    /** Default page number for paginated user list (minimum value: 1) */
    public static final String DEFAULT_PAGE = "1";
    /** Default page size for paginated user list */
    public static final String DEFAULT_PAGE_SIZE = "20";
    /** Maximum allowed page size to prevent excessive data exposure and DoS risks */
    public static final int MAX_PAGE_SIZE = 100;
    /** Error message prefix for user not found scenarios */
    public static final String ERR_USER_NOT_FOUND = "User not found with ID: ";
    /** Error message prefix for role not found scenarios */
    public static final String ERR_ROLE_NOT_FOUND = "Role not found with ID: ";
    /** Error message for invalid role ID format/range */
    public static final String ERR_INVALID_ROLE_ID = "Invalid role ID: must be a numeric value between 1 and 5 (matching 5 system-defined roles)";
    /** Error message for unauthorized access attempts */
    public static final String ERR_PERMISSION_DENIED = "Access denied: Only System Admin can perform user/role management operations";
    /** Error message for duplicate email conflicts during user updates */
    public static final String ERR_EMAIL_CONFLICT = "Email is already registered to another user";
    /** Logger instance for audit logging, error tracking, and compliance reporting per [NFR-006] */
    private static final Logger logger = Logger.getLogger(UserResource.class);

    // ====================== DEPENDENCY INJECTION (CDI) ======================
    /** Service layer for user business logic, abstracts data access and business rules */
    @Inject
    UserService userService;
    /** Service layer for role business logic, validates role existence and permissions */
    @Inject
    RoleService roleService;
    /** Utility for masking sensitive PII data (emails, UUIDs) in logs per [NFR-006] */
    @Inject
    DataMaskingUtil dataMaskingUtil;

    // ====================== ENDPOINT: LIST USERS WITH FILTERING AND PAGINATION ======================
    /**
     * Retrieves a paginated list of users with optional filtering by role and search term.
     * Supports filtering by role ID, full name/email search, and pagination to handle large user datasets.
     * Delegates data aggregation to the service layer to avoid in-memory iteration and ensure optimal performance.
     * 
     * @param roleId Optional role ID to filter users (1-5 corresponding to system-defined roles)
     * @param searchTerm Optional search term to filter by user full name or masked email
     * @param page Optional page number (default: 1, minimum: 1)
     * @param size Optional page size (default: 20, maximum: 100)
     * @return Paginated list of user response DTOs with role information, wrapped in standard page response structure
     * @traceability [REQ-003], [ARC-001]
     */
    @GET
    public Response getUsers(
            @QueryParam("roleId") String roleId,
            @QueryParam("searchTerm") String searchTerm,
            @QueryParam("page") String page,
            @QueryParam("size") String size) {
        // [AUDIT LOG] Entry point log with masked sensitive input parameters per [NFR-006]
        logger.info("[PROCESS] [REQ-003] [ARC-001] Entering getUsers endpoint | Filters - roleId: " + dataMaskingUtil.maskSensitiveData(roleId) + ", searchTerm: " + dataMaskingUtil.maskSensitiveData(searchTerm));
        try {
            // Parse and validate pagination parameters with fallback to defaults
            int pageNum = parsePositiveInteger(page, DEFAULT_PAGE);
            int pageSize = parsePositiveInteger(size, DEFAULT_PAGE_SIZE);
            // Enforce maximum page size cap to prevent excessive data retrieval and DoS attacks
            if (pageSize > MAX_PAGE_SIZE) {
                pageSize = MAX_PAGE_SIZE;
                logger.debug("[DEBUG] [REQ-003] Page size capped to maximum allowed value: " + MAX_PAGE_SIZE + " for security");
            }
            // Delegate data aggregation to service layer (uses indexed database queries, no in-memory iteration per enterprise performance rules)
            PageResponse<UserResponse> userPage = userService.getAllUsers(roleId, searchTerm, pageNum, pageSize);
            // [AUDIT LOG] Exit point log with success context and record count
            logger.info("[PROCESS] [REQ-003] [ARC-001] Successfully retrieved " + userPage.getTotalElements() + " total users, returning page " + pageNum + " with " + userPage.getContent().size() + " records");
            return Response.ok(userPage).build();
        } catch (IllegalArgumentException e) {
            // [ERROR LOG] Log validation errors with required 3 context keys: subsystem, raw error, traceability tags
            logger.error("[CRITICAL FAIL] [REQ-003] [ARC-001] UserResource - Invalid input parameter in getUsers. Raw error: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MembershipHubException("VALIDATION_FAILED", "Invalid input parameters: " + e.getMessage()))
                    .build();
        } catch (Exception e) {
            // [ERROR LOG] Log unexpected errors with full stack trace for debugging, preserve root cause
            logger.error("[CRITICAL FAIL] [REQ-003] [ARC-001] UserResource - Unexpected system error in getUsers endpoint. Raw error: {}", e.getMessage(), e);
            // Forward original exception to custom enterprise exception to preserve stack trace per governance rules
            throw new MembershipHubException("INTERNAL_SERVER_ERROR", "Failed to retrieve user list due to system error", e);
        }
    }

    // ====================== ENDPOINT: UPDATE USER ROLE ======================
    /**
     * Updates the role of a specified user, applying RBAC permissions immediately as per [REQ-003].
     * Validates user existence, role validity, and enforces permission checks before updating.
     * 
     * @param userId UUID of the user to update role for (path parameter)
     * @param roleUpdateRequest Request body containing new role ID (1-5)
     * @return Success message confirming role update and permission application
     * @traceability [REQ-003], [ARC-001]
     */
    @PUT
    @Path("/{userId}/role")
    public Response updateUserRole(@PathParam("userId") String userId, RoleUpdateRequest roleUpdateRequest) {
        // [AUDIT LOG] Entry point log with masked sensitive user ID per [NFR-006]
        logger.info("[PROCESS] [REQ-003] [ARC-001] Entering updateUserRole endpoint for user ID: " + dataMaskingUtil.maskSensitiveData(userId));
        try {
            // Validate UUID format of user ID (prevents invalid input attacks)
            UUID userUuid = UUID.fromString(userId);
            // Validate request body and role ID presence
            if (roleUpdateRequest == null || roleUpdateRequest.getRoleId() == null) {
                logger.warn("[WARN] [REQ-003] [ARC-001] Missing role ID in update request for user: " + dataMaskingUtil.maskSensitiveData(userId));
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new MembershipHubException("VALIDATION_FAILED", "Role ID is required in request body"))
                        .build();
            }
            // Validate role ID is within valid range (1-5 for 5 system-defined roles per [ARC-001])
            Short roleId = validateRoleId(roleUpdateRequest.getRoleId());
            // Check if target role exists in system to prevent invalid role assignments
            Role existingRole = roleService.getRoleById(roleId);
            if (existingRole == null) {
                logger.warn("[WARN] [REQ-003] [ARC-001] Attempt to assign non-existent role ID: " + roleId + " to user: " + dataMaskingUtil.maskSensitiveData(userId));
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new MembershipHubException("ROLE_NOT_FOUND", ERR_ROLE_NOT_FOUND + roleId))
                        .build();
            }
            // Check if target user exists to prevent updates to non-existent users
            if (!userService.userExists(userUuid)) {
                logger.warn("[WARN] [REQ-003] [ARC-001] Attempt to update role for non-existent user: " + dataMaskingUtil.maskSensitiveData(userId));
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new MembershipHubException("USER_NOT_FOUND", ERR_USER_NOT_FOUND + userId))
                        .build();
            }
            // Delegate role update to service layer, which applies permissions immediately per [REQ-003] business rule
            userService.updateUserRole(userUuid, roleId);
            // [AUDIT LOG] Exit point log with success context and masked user ID
            logger.info("[PROCESS] [REQ-003] [ARC-001] Successfully updated user role to '" + existingRole.getName() + "' for user: " + dataMaskingUtil.maskSensitiveData(userId));
            return Response.ok(new MembershipHubException("SUCCESS", "User role updated successfully, permissions applied immediately")).build();
        } catch (IllegalArgumentException e) {
            // Handle invalid UUID format or invalid role ID values
            logger.error("[CRITICAL FAIL] [REQ-003] [ARC-001] UserResource - Invalid input in updateUserRole. Raw error: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MembershipHubException("VALIDATION_FAILED", "Invalid input: " + e.getMessage()))
                    .build();
        } catch (SecurityException e) {
            // Handle permission denied errors (e.g., user trying to update higher privilege role)
            logger.error("[CRITICAL FAIL] [REQ-003] [ARC-001] UserResource - Permission denied in updateUserRole. Raw error: {}", e.getMessage());
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new MembershipHubException("PERMISSION_DENIED", ERR_PERMISSION_DENIED))
                    .build();
        } catch (Exception e) {
            // [ERROR LOG] Log unexpected errors with full stack trace, preserve root cause per governance rules
            logger.error("[CRITICAL FAIL] [REQ-003] [ARC-001] UserResource - Unexpected system error in updateUserRole endpoint. Raw error: {}", e.getMessage(), e);
            // Forward original exception to custom enterprise exception to preserve stack trace
            throw new MembershipHubException("INTERNAL_SERVER_ERROR", "Failed to update user role due to system error", e);
        }
    }

    // ====================== PRIVATE HELPER METHODS ======================
    /**
     * Parses a string value to a positive integer (minimum 1), returns default value if parsing fails.
     * Used for validating pagination parameters to prevent invalid input attacks.
     * 
     * @param value String value to parse
     * @param defaultValue Default value to return if input is null/invalid
     * @return Parsed positive integer (minimum 1)
     */
    private int parsePositiveInteger(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return Integer.parseInt(defaultValue);
        }
        try {
            int parsed = Integer.parseInt(value);
            return Math.max(parsed, 1); // Enforce minimum value of 1 for pagination
        } catch (NumberFormatException e) {
            logger.debug("[DEBUG] [REQ-003] Invalid numeric input: " + dataMaskingUtil.maskSensitiveData(value) + ", using default value: " + defaultValue);
            return Integer.parseInt(defaultValue);
        }
    }

    /**
     * Validates that role ID is within the valid range of 1-5 (matching 5 system-defined roles per [ARC-001]).
     * Throws IllegalArgumentException if role ID is out of range.
     * 
     * @param roleId Role ID to validate
     * @return Validated role ID as Short
     */
    private Short validateRoleId(Integer roleId) {
        if (roleId == null || roleId < 1 || roleId > 5) {
            throw new IllegalArgumentException(ERR_INVALID_ROLE_ID + ": received value " + roleId);
        }
        return roleId.shortValue();
    }
}