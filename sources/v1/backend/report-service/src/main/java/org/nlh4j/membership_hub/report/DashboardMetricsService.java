package org.nlh4j.saas.membership_hub.report;

// ==========================================
// ENTERPRISE IMPORTS & DEPENDENCY INJECTIONS
// ==========================================
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.jboss.logging.Logger;
import org.nlh4j.saas.membership_hub.auth.service.RbacService;
import org.nlh4j.saas.membership_hub.exception.AccessDeniedException;
import org.nlh4j.saas.membership_hub.websocket.WebSocketSessionManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.mutiny.redis.client.RedisAPI;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service class for generating real-time enrollment dashboard metrics and broadcasting updates.
 * <p>
 * Implements business requirements for [REQ-025]: Real-time enrollment summary dashboard for Center Admin,
 * including total enrolled students, active courses, and upcoming sessions within 7 days.
 * Integrates RBAC access control [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005] to enforce
 * role-based data isolation: Center Admins can only access their assigned center's data, System Admins
 * have full system-wide access. Uses Redis caching with 5-minute TTL to reduce database load, and
 * WebSocket integration to push real-time updates when enrollment, course, or cancellation events occur.
 * </p>
 *
 * @author Principal Software Engineer
 * @version 1.0
 * @since 2024-06-01
 * @traceability [REQ-025], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
 */
@ApplicationScoped
public class DashboardMetricsService {

    // ==========================================
    // ENTERPRISE LOGGER INITIALIZATION [NFR-006]
    // ==========================================
    private static final Logger logger = Logger.getLogger(DashboardMetricsService.class);

    // ==========================================
    // TOP-LEVEL CONSTANTS (No hardcoding allowed per enterprise clean code rules) [0.2]
    // ==========================================
    /** Redis cache key prefix for enrollment dashboard metrics */
    public static final String CACHE_KEY_PREFIX = "dashboard:enrollment:metrics:";
    /** Cache TTL in seconds (5 minutes per performance requirements) */
    public static final long CACHE_TTL_SECONDS = 300L;
    /** Number of days to look ahead for upcoming course sessions */
    public static final int UPCOMING_SESSIONS_DAYS = 7;
    /** System Admin role identifier for RBAC validation */
    public static final String ROLE_SYSTEM_ADMIN = "System Admin";
    /** Center Admin role identifier for RBAC validation */
    public static final String ROLE_CENTER_ADMIN = "Center Admin";
    /** Placeholder UUID for system-wide queries (no center filter) */
    public static final UUID SYSTEM_WIDE_CENTER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    // ==========================================
    // CDI DEPENDENCY INJECTIONS
    // ==========================================
    @Inject
    EntityManager entityManager;

    @Inject
    RedisAPI redisApi;

    @Inject
    RbacService rbacService;

    @Inject
    WebSocketSessionManager webSocketSessionManager;

    @Inject
    ObjectMapper objectMapper;

    // ==========================================
    // PUBLIC CORE SERVICE METHODS
    // ==========================================

    /**
     * Retrieves enrollment dashboard metrics for a specified center or system-wide.
     * <p>
     * Business logic flow:
     * 1. Validate user RBAC permissions to access the requested center
     * 2. Check Redis cache for existing valid metrics (5-minute TTL)
     * 3. If cache miss, query database using optimized native SQL joins
     * 4. Cache fresh metrics and return to caller
     * </p>
     *
     * @param centerId UUID of the target center, pass null for system-wide metrics (System Admin only)
     * @param authenticatedUserId UUID of the currently authenticated user making the request
     * @return Map containing 3 metrics: totalStudents (Long), activeCourses (Long), upcomingSessions (Long)
     * @throws AccessDeniedException if user lacks permission to access the requested center
     * @traceability [REQ-025], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
     */
    public Map<String, Object> getEnrollmentDashboardMetrics(UUID centerId, UUID authenticatedUserId) {
        // [LOG_ENTRY] Audit log for process start with context payload [NFR-006]
        logger.infof("[PROCESS] [REQ-025] Starting dashboard metrics retrieval for user: %s, center: %s", authenticatedUserId, centerId);

        try {
            // Step 1: Enforce RBAC access control before any data access (security first)
            validateCenterAccess(authenticatedUserId, centerId);

            // Step 2: Generate deterministic cache key based on center ID
            String cacheKey = generateCacheKey(centerId);

            // Step 3: Check Redis cache for existing valid metrics to reduce DB load
            Map<String, Object> cachedMetrics = getCachedMetrics(cacheKey);
            if (cachedMetrics != null) {
                logger.infof("[PROCESS] [REQ-025] Cache hit for center %s, returning cached metrics", centerId);
                return cachedMetrics;
            }

            // Step 4: Cache miss - query database for fresh metrics using optimized native SQL
            logger.infof("[PROCESS] [REQ-025] Cache miss for center %s, querying database", centerId);
            Map<String, Object> freshMetrics = queryMetricsFromDatabase(centerId);

            // Step 5: Persist fresh metrics to Redis with configured TTL
            cacheMetrics(cacheKey, freshMetrics);

            // [LOG_EXIT] Audit log for successful process completion
            logger.infof("[PROCESS] [REQ-025] Successfully retrieved dashboard metrics for center %s", centerId);
            return freshMetrics;

        } catch (Exception e) {
            // [EXCEPTION_LOG] Mandatory error logging with tag ID and raw error message [NFR-006]
            logger.errorf("[CRITICAL FAIL] [REQ-025] Dashboard metrics retrieval failed. Raw error: %s", e.getMessage());
            // Preserve original stack trace by passing caught exception to wrapper [0.3]
            throw new RuntimeException("Failed to retrieve enrollment dashboard metrics", e);
        }
    }

    /**
     * Broadcasts updated dashboard metrics to all connected WebSocket clients for a specific center.
     * <p>
     * This method is triggered by domain events (new enrollment, course creation, enrollment cancellation)
     * to push real-time updates to frontend dashboards without requiring client polling.
     * </p>
     *
     * @param centerId UUID of the center to broadcast updates for
     * @traceability [REQ-025], [ARC-009]
     */
    public void broadcastUpdatedMetrics(UUID centerId) {
        try {
            // Invalidate stale cache for the center to ensure next request gets fresh data
            String cacheKey = generateCacheKey(centerId);
            redisApi.del(cacheKey);

            // Fetch latest metrics from database
            Map<String, Object> updatedMetrics = queryMetricsFromDatabase(centerId);

            // Push update to all WebSocket subscribers for this center
            webSocketSessionManager.broadcastToCenter(centerId, "dashboard_metrics_update", updatedMetrics);

            logger.infof("[PROCESS] [REQ-025] Broadcasted updated metrics to WebSocket clients for center %s", centerId);
        } catch (Exception e) {
            // Log error but do not rethrow to avoid breaking event processing pipeline
            logger.errorf("[WARN] [REQ-025] Failed to broadcast dashboard metrics for center %s. Raw error: %s", centerId, e.getMessage());
        }
    }

    // ==========================================
    // PRIVATE HELPER METHODS
    // ==========================================

    /**
     * Validates user RBAC permissions to access the requested center.
     * <p>
     * RBAC rules enforced:
     * - System Admin: Full access to all centers, including system-wide queries
     * - Center Admin: Only access to centers they are explicitly assigned to
     * - All other roles (Manager, Teacher, Student): Denied access to this endpoint
     * </p>
     *
     * @param userId UUID of the authenticated user
     * @param centerId UUID of the requested center (null for system-wide)
     * @throws AccessDeniedException if user lacks required permissions
     */
    private void validateCenterAccess(UUID userId, UUID centerId) {
        // Retrieve user's assigned role from RBAC service
        String userRole = rbacService.getUserRole(userId);

        // System Admin has unrestricted access to all centers and system-wide data
        if (ROLE_SYSTEM_ADMIN.equals(userRole)) {
            return;
        }

        // Center Admin can only access their assigned centers
        if (ROLE_CENTER_ADMIN.equals(userRole)) {
            if (centerId == null) {
                logger.warnf("[SECURITY] [REQ-025] Center Admin %s attempted to access system-wide dashboard", userId);
                throw new AccessDeniedException("Center Admin role cannot access system-wide dashboard metrics");
            }
            boolean isAssigned = rbacService.isCenterAssignedToAdmin(userId, centerId);
            if (!isAssigned) {
                logger.warnf("[SECURITY] [REQ-025] Unauthorized access attempt: User %s (Center Admin) accessed center %s", userId, centerId);
                throw new AccessDeniedException("You are not authorized to access this center's dashboard");
            }
            return;
        }

        // All other roles are explicitly denied access to this endpoint
        logger.warnf("[SECURITY] [REQ-025] Unauthorized access attempt: User %s with role %s accessed dashboard endpoint", userId, userRole);
        throw new AccessDeniedException("Your role does not have permission to access the enrollment dashboard");
    }

    /**
     * Generates a deterministic Redis cache key for the given center ID.
     *
     * @param centerId UUID of the center, null for system-wide queries
     * @return Formatted cache key string
     */
    private String generateCacheKey(UUID centerId) {
        return centerId == null ? CACHE_KEY_PREFIX + "system_wide" : CACHE_KEY_PREFIX + centerId.toString();
    }

    /**
     * Retrieves cached metrics from Redis with safe deserialization.
     *
     * @param cacheKey Redis key to look up
     * @return Deserialized metrics map, or null if cache miss or deserialization fails
     */
    private Map<String, Object> getCachedMetrics(String cacheKey) {
        try {
            String cachedJson = redisApi.get(cacheKey);
            if (cachedJson != null && !cachedJson.isEmpty()) {
                // Safe deserialization using Jackson TypeReference to avoid type casting errors
                return objectMapper.readValue(cachedJson, new TypeReference<Map<String, Object>>() {});
            }
            return null;
        } catch (Exception e) {
            logger.warnf("[WARN] [REQ-025] Cache retrieval failed for key %s. Raw error: %s", cacheKey, e.getMessage());
            return null; // Fallback to database query on cache failure
        }
    }

    /**
     * Persists metrics to Redis with configured TTL.
     *
     * @param cacheKey Redis key to store metrics under
     * @param metrics Metrics map to cache
     */
    private void cacheMetrics(String cacheKey, Map<String, Object> metrics) {
        try {
            String serializedMetrics = objectMapper.writeValueAsString(metrics);
            redisApi.setex(cacheKey, CACHE_TTL_SECONDS, serializedMetrics);
            logger.debugf("[DEBUG] [REQ-025] Metrics cached successfully for key %s with TTL %d seconds", cacheKey, CACHE_TTL_SECONDS);
        } catch (Exception e) {
            logger.warnf("[WARN] [REQ-025] Cache storage failed for key %s. Raw error: %s", cacheKey, e.getMessage());
            // Cache failure is non-critical, proceed without caching
        }
    }

    /**
     * Queries the database for fresh dashboard metrics using optimized native SQL with prepared statements.
     * <p>
     * Compliance notes:
     * - Uses positional query parameters to prevent SQL injection (OWASP Top 10 compliance)
     * - Leverages database indexes on frequently queried columns (center_id, start_date, end_date, student_id)
     * - No in-memory iteration over large collections, all aggregation performed at database layer
     * </p>
     *
     * @param centerId UUID of the center to query for, null for system-wide metrics
     * @return Map containing totalStudents, activeCourses, upcomingSessions metrics
     */
    private Map<String, Object> queryMetricsFromDatabase(UUID centerId) {
        Map<String, Object> metrics = new HashMap<>();

        // Query 1: Total distinct students enrolled in at least one course for the target center
        // Uses JOIN between enrollments and courses to filter by center, COUNT(DISTINCT) to avoid duplicate student counts
        String totalStudentsSql = """
            SELECT COUNT(DISTINCT e.student_id)
            FROM enrollments e
            INNER JOIN courses c ON e.course_id = c.course_id
            WHERE (:centerId IS NULL OR c.center_id = :centerId)
            """;
        Query totalStudentsQuery = entityManager.createNativeQuery(totalStudentsSql);
        totalStudentsQuery.setParameter(1, centerId); // Positional parameter to prevent SQL injection
        BigInteger totalStudentsResult = (BigInteger) totalStudentsQuery.getSingleResult();
        metrics.put("totalStudents", totalStudentsResult != null ? totalStudentsResult.longValue() : 0L);

        // Query 2: Count of active courses (currently running) for the target center
        // Active courses are those where current date is between start_date and end_date
        String activeCoursesSql = """
            SELECT COUNT(*)
            FROM courses c
            WHERE (:centerId IS NULL OR c.center_id = :centerId)
            AND c.start_date <= CURRENT_DATE
            AND c.end_date >= CURRENT_DATE
            """;
        Query activeCoursesQuery = entityManager.createNativeQuery(activeCoursesSql);
        activeCoursesQuery.setParameter(1, centerId);
        BigInteger activeCoursesResult = (BigInteger) activeCoursesQuery.getSingleResult();
        metrics.put("activeCourses", activeCoursesResult != null ? activeCoursesResult.longValue() : 0L);

        // Query 3: Count of upcoming course sessions within the next 7 days
        // NOTE: If a dedicated course_sessions table exists with individual session dates, replace this query with:
        // SELECT COUNT(*) FROM course_sessions s WHERE s.course_id IN (SELECT course_id FROM courses WHERE center_id = :centerId)
        // AND s.session_date BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '7 days'
        // Current implementation uses course start date as a proxy for first session, adjust per actual schema
        String upcomingSessionsSql = """
            SELECT COUNT(*)
            FROM courses c
            WHERE (:centerId IS NULL OR c.center_id = :centerId)
            AND c.start_date BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '? days'
            """;
        Query upcomingSessionsQuery = entityManager.createNativeQuery(upcomingSessionsSql);
        upcomingSessionsQuery.setParameter(1, UPCOMING_SESSIONS_DAYS);
        upcomingSessionsQuery.setParameter(2, centerId);
        BigInteger upcomingSessionsResult = (BigInteger) upcomingSessionsQuery.getSingleResult();
        metrics.put("upcomingSessions", upcomingSessionsResult != null ? upcomingSessionsResult.longValue() : 0L);

        logger.debugf("[DEBUG] [REQ-025] Database query completed for center %s: totalStudents=%d, activeCourses=%d, upcomingSessions=%d",
                centerId, metrics.get("totalStudents"), metrics.get("activeCourses"), metrics.get("upcomingSessions"));
        return metrics;
    }
}