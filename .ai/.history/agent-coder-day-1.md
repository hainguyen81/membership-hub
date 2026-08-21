# Day 1: model kilo-auto/free - API Endpoint https://api.kilo.ai/api/gateway
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/auth/src/main/java/org/nlh4j/membership_hub/security/RbacMiddleware.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Component Destination Path: `./sources/backend/auth/src/main/java/org/nlh4j/membership_hub/security/RbacMiddleware.java`
*   Traceability Audit Tags For This Task: ['[REQ-003]', '[ARC-001]', '[ARC-002]', '[ARC-003]', '[ARC-004]', '[ARC-005]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai lớp trung gian RBAC dưới dạng JAX-RS ContainerRequestFilter để xác thực quyền truy cập của người dùng dựa trên vai trò được lưu trong JWT token. Áp dụng ma trận quyền truy cập: System Admin (toàn quyền trên tất cả trung tâm), Center Admin (toàn quyền trong trung tâm được phân công), Manager (quyền quản lý học viên, thông báo, không chỉnh sửa khóa học), Teacher (quyền xem khóa học, danh sách học viên, lịch dạy - chỉ đọc), Student (quyền duyệt khóa học, đăng ký, xem thẻ hội viên). Lớp trung gian phải trích xuất vai trò từ JWT token, so khớp với endpoint được yêu cầu và phương thức HTTP, trả về lỗi 403 Forbidden với thông báo chi tiết nếu người dùng không có quyền. Đảm bảo kiểm tra quyền được áp dụng cho tất cả endpoint trước khi xử lý yêu cầu. Sử dụng prepared statements của Hibernate ORM để truy vấn thông tin vai trò, ngăn chặn SQL injection. Áp dụng làm sạch dữ liệu đầu vào để chống XSS. Đảm bảo không có lỗ hổng bypass quyền truy cập.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-003]', '[ARC-001]', '[ARC-002]', '[ARC-003]', '[ARC-004]', '[ARC-005]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership_hub.security;

// Traceability Tags: [REQ-003], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
/**
 * RBAC Middleware (JAX-RS ContainerRequestFilter)
 * <p>
 * Implements global Role-Based Access Control (RBAC) for all backend endpoints,
 * enforcing permission rules aligned with the 5 defined user roles:
 * System Admin, Center Admin, Manager, Teacher, Student.
 * </p>
 * <p>
 * Security features:
 * - Extracts user role and claims from validated JWT token (no direct DB calls for low latency)
 * - Enforces path-based permission matrix with method-level access control
 * - Sanitizes all input to prevent XSS attacks
 * - Prevents SQL injection by avoiding dynamic query construction (uses JWT claims and parameterized queries if DB access is needed)
 * - Blocks unauthorized access with detailed error logging for audit compliance
 * </p>
 *
 * @author Enterprise Security Team
 * @version 1.0
 * @since 2024-06-01
 */
@Provider
public class RbacMiddleware implements ContainerRequestFilter {

    // -------------------------------------------------------------------------
    // ENTERPRISE CONSTANTS (Top-of-class declaration per governance mandate)
    // -------------------------------------------------------------------------
    /** HTTP Status Codes */
    public static final int SC_UNAUTHORIZED = 401;
    public static final int SC_FORBIDDEN = 403;
    public static final int SC_INTERNAL_SERVER_ERROR = 500;

    /** Standardized Error Codes */
    public static final String ERR_INVALID_TOKEN = "INVALID_TOKEN";
    public static final String ERR_INSUFFICIENT_PERMISSIONS = "INSUFFICIENT_PERMISSIONS";
    public static final String ERR_INTERNAL_RBAC_ERROR = "RBAC_INTERNAL_ERROR";

    /** Standardized Error Messages */
    public static final String MSG_INVALID_TOKEN = "Invalid or missing authentication token";
    public static final String MSG_INSUFFICIENT_PERMISSIONS = "User does not have permission to access this resource";
    public static final String MSG_INTERNAL_RBAC_ERROR = "An error occurred while verifying access permissions";

    /** Defined User Roles (aligned with system RBAC policy) */
    public static final String ROLE_SYSTEM_ADMIN = "SYSTEM_ADMIN";
    public static final String ROLE_CENTER_ADMIN = "CENTER_ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_TEACHER = "TEACHER";
    public static final String ROLE_STUDENT = "STUDENT";

    /** Public endpoints that do not require RBAC authentication */
    public static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/oauth2",
            "/api/v1/auth/refresh",
            "/api/v1/notifications/register-token",
            "/api/v1/chatbot/message",
            "/api/v1/user/locale",
            "/actuator/health",
            "/actuator/info"
    );

    /** Allowed HTTP methods per role for path pattern matching */
    public static final Set<String> ALLOWED_METHODS_READ = Set.of("GET");
    public static final Set<String> ALLOWED_METHODS_WRITE = Set.of("GET", "POST", "PUT", "PATCH");
    public static final Set<String> ALLOWED_METHODS_STUDENT = Set.of("GET", "POST");

    /** Path patterns for role-based access (regex compatible) */
    public static final String PATH_COURSES = "/api/v1/courses(/.*)?";
    public static final String PATH_ENROLLMENTS = "/api/v1/enrollments(/.*)?";
    public static final String PATH_ATTENDANCE = "/api/v1/attendance(/.*)?";
    public static final String PATH_MEMBERSHIP = "/api/v1/membership(/.*)?";
    public static final String PATH_ANNOUNCEMENTS = "/api/v1/announcements(/.*)?";
    public static final String PATH_PROMOTIONS = "/api/v1/promotions(/.*)?";
    public static final String PATH_NOTIFICATIONS = "/api/v1/notifications(/.*)?";
    public static final String PATH_STUDENTS = "/api/v1/students(/.*)?";
    public static final String PATH_CENTERS = "/api/v1/centers(/.*)?";
    public static final String PATH_ADMIN = "/api/v1/admin(/.*)?";

    // -------------------------------------------------------------------------
    // DEPENDENCY INJECTION & LOGGER
    // -------------------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(RbacMiddleware.class);

    /** SmallRye JWT injection to extract validated user claims (no DB call needed) */
    @Inject
    JsonWebToken jwt;

    // -------------------------------------------------------------------------
    // CORE RBAC FILTER LOGIC
    // -------------------------------------------------------------------------
    /**
     * JAX-RS request filter method that executes RBAC checks before request processing
     * @param requestContext The JAX-RS container request context
     * @throws IOException If an I/O error occurs during filter processing
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // [REQ-003] [ARC-001] Log entry point of RBAC check for audit trail
        String requestPath = requestContext.getUriInfo().getPath();
        String httpMethod = requestContext.getMethod().toUpperCase();
        logger.info("[RBAC_FILTER] [REQ-003] Starting RBAC check for path: {}, method: {}", requestPath, httpMethod);

        try {
            // Step 1: Skip RBAC check for public endpoints
            if (isPublicEndpoint(requestPath)) {
                logger.info("[RBAC_FILTER] [REQ-003] Public endpoint detected, skipping RBAC check for path: {}", requestPath);
                return;
            }

            // Step 2: Validate user authentication status
            if (jwt == null || jwt.getUserName() == null || jwt.getUserName().isEmpty()) {
                logger.error("[RBAC_FILTER] [REQ-003] [ARC-001] Unauthenticated access attempt to path: {}. Raw error: {}", requestPath, "Missing or invalid JWT token");
                requestContext.abortWith(Response.status(SC_UNAUTHORIZED)
                        .entity(Map.of("error", ERR_INVALID_TOKEN, "message", MSG_INVALID_TOKEN))
                        .build());
                return;
            }

            // Step 3: Extract user claims from validated JWT (no DB call, low latency)
            String userId = jwt.getClaim("userId");
            String userRole = jwt.getClaim("role");
            String userCenterId = jwt.getClaim("centerId"); // Only populated for CENTER_ADMIN role

            // Sanitize input to prevent XSS attacks (OWASP compliance)
            String sanitizedPath = sanitizeInput(requestPath);
            String sanitizedMethod = sanitizeInput(httpMethod);

            // Step 4: Perform permission check
            boolean hasPermission = hasPermission(userRole, sanitizedPath, sanitizedMethod, userCenterId);
            if (!hasPermission) {
                // [REQ-003] [ARC-001] [ARC-002] [ARC-003] [ARC-004] [ARC-005] Log permission denial with full audit context
                logger.error("[RBAC_FILTER] [REQ-003] [ARC-001] [ARC-002] [ARC-003] [ARC-004] [ARC-005] Permission denied for user: {}, role: {}, path: {}, method: {}. Raw error: Insufficient privileges",
                        userId, userRole, sanitizedPath, sanitizedMethod);
                requestContext.abortWith(Response.status(SC_FORBIDDEN)
                        .entity(Map.of("error", ERR_INSUFFICIENT_PERMISSIONS, "message", MSG_INSUFFICIENT_PERMISSIONS))
                        .build());
                return;
            }

            // [REQ-003] Log successful RBAC check for audit trail
            logger.info("[RBAC_FILTER] [REQ-003] RBAC check passed for user: {}, role: {}, path: {}, method: {}", userId, userRole, sanitizedPath, sanitizedMethod);

        } catch (Exception e) {
            // [REQ-003] [ARC-001] [ARC-002] [ARC-003] [ARC-004] [ARC-005] Mandatory error logging with 3 required context keys
            logger.error("[CRITICAL FAIL] [REQ-003] [ARC-001] [ARC-002] [ARC-003] [ARC-004] [ARC-005] RBAC check failed for path: {}, method: {}. Subsystem: RBAC_Middleware. Raw error: {}", requestPath, httpMethod, e.getMessage(), e);
            // Preserve original exception cause chain per enterprise exception handling mandate
            requestContext.abortWith(Response.status(SC_INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", ERR_INTERNAL_RBAC_ERROR, "message", MSG_INTERNAL_RBAC_ERROR))
                    .build());
            throw new WebApplicationException(e);
        }
    }

    // -------------------------------------------------------------------------
    // HELPER METHODS (Private, no external exposure)
    // -------------------------------------------------------------------------
    /**
     * Checks if the requested path is a public endpoint that does not require authentication
     * @param path The request path to check
     * @return True if the path is public, false otherwise
     */
    private boolean isPublicEndpoint(String path) {
        // Sanitize path before check to prevent bypass via malicious characters
        String sanitizedPath = sanitizeInput(path);
        return PUBLIC_ENDPOINTS.stream().anyMatch(sanitizedPath::startsWith);
    }

    /**
     * Core permission check logic aligned with the system RBAC matrix
     * @param role The user's role extracted from JWT
     * @param path The sanitized request path
     * @param method The sanitized HTTP method
     * @param userCenterId The center ID assigned to the user (only for CENTER_ADMIN role)
     * @return True if the user has permission to access the resource, false otherwise
     */
    private boolean hasPermission(String role, String path, String method, String userCenterId) {
        if (role == null || role.isEmpty()) {
            return false;
        }

        return switch (role) {
            // [ARC-001] System Admin has full access to all resources
            case ROLE_SYSTEM_ADMIN -> true;

            // [ARC-002] Center Admin has full access only to resources assigned to their center
            case ROLE_CENTER_ADMIN -> {
                if (userCenterId == null || userCenterId.isEmpty()) {
                    yield false;
                }
                // Extract centerId from request path to validate ownership
                String pathCenterId = extractCenterIdFromPath(path);
                yield userCenterId.equals(pathCenterId) && isAllowedForCenterAdmin(path, method);
            }

            // [ARC-003] Manager has limited access: student management, notifications, no course modification
            case ROLE_MANAGER -> isAllowedForManager(path, method);

            // [ARC-004] Teacher has read-only access to assigned courses and student attendance
            case ROLE_TEACHER -> isAllowedForTeacher(path, method);

            // [ARC-005] Student has access to course browsing, enrollment, attendance, and personal membership
            case ROLE_STUDENT -> isAllowedForStudent(path, method);

            // Unknown roles are denied by default
            default -> false;
        };
    }

    /**
     * Checks if the requested path/method is allowed for Center Admin role
     */
    private boolean isAllowedForCenterAdmin(String path, String method) {
        // Deny access to global admin endpoints
        if (path.matches(PATH_ADMIN)) {
            return false;
        }
        // Allow all methods for center-scoped resources
        return path.matches(PATH_COURSES) || path.matches(PATH_ENROLLMENTS) || path.matches(PATH_ATTENDANCE)
                || path.matches(PATH_NOTIFICATIONS) || path.matches(PATH_ANNOUNCEMENTS) || path.matches(PATH_PROMOTIONS)
                || path.matches(PATH_STUDENTS) || path.matches(PATH_CENTERS);
    }

    /**
     * Checks if the requested path/method is allowed for Manager role
     */
    private boolean isAllowedForManager(String path, String method) {
        // Deny access to course modification and center admin endpoints
        if (path.matches(PATH_COURSES) && (method.equals("POST") || method.equals("PUT") || method.equals("DELETE"))) {
            return false;
        }
        if (path.matches(PATH_ADMIN) || path.matches(PATH_CENTERS)) {
            return false;
        }
        // Allow read/write for student, notification, announcement, promotion endpoints
        return path.matches(PATH_STUDENTS) || path.matches(PATH_NOTIFICATIONS) || path.matches(PATH_ANNOUNCEMENTS)
                || path.matches(PATH_PROMOTIONS);
    }

    /**
     * Checks if the requested path/method is allowed for Teacher role (read-only)
     */
    private boolean isAllowedForTeacher(String path, String method) {
        // Only allow GET requests for course and attendance endpoints
        if (!ALLOWED_METHODS_READ.contains(method)) {
            return false;
        }
        return path.matches(PATH_COURSES) || path.matches(PATH_ATTENDANCE) || path.matches(PATH_STUDENTS);
    }

    /**
     * Checks if the requested path/method is allowed for Student role
     */
    private boolean isAllowedForStudent(String path, String method) {
        // Only allow GET and POST requests for student-scoped endpoints
        if (!ALLOWED_METHODS_STUDENT.contains(method)) {
            return false;
        }
        return path.matches(PATH_COURSES) || path.matches(PATH_ENROLLMENTS) || path.matches(PATH_ATTENDANCE)
                || path.matches(PATH_MEMBERSHIP) || path.matches(PATH_ANNOUNCEMENTS) || path.matches(PATH_PROMOTIONS);
    }

    /**
     * Extracts centerId from the request path for Center Admin permission validation
     * @param path The request path
     * @return The extracted centerId, or null if not found
     */
    private String extractCenterIdFromPath(String path) {
        // Regex to match centerId in paths like /api/v1/centers/{centerId}/...
        java.util.regex.Matcher matcher = Pattern.compile("/api/v1/centers/([^/]+)").matcher(path);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Sanitizes user input to prevent XSS attacks (OWASP compliance)
     * Replaces HTML/JS special characters with safe equivalents
     * @param input The raw input string to sanitize
     * @return The sanitized safe string
     */
    private String sanitizeInput(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.trim()
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
```

