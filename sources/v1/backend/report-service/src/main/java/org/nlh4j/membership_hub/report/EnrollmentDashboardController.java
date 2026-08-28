package org.nlh4j.saas.membership_hub.report;

// ==============================================
// ENTERPRISE IMPORT MANDATE (STRICT LAYER ISOLATION)
// ==============================================
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.nlh4j.saas.membership_hub.auth.dto.EnrollmentDashboardDTO;
import org.nlh4j.saas.membership_hub.report.service.EnrollmentDashboardService;
import org.nlh4j.saas.membership_hub.infrastructure.redis.RedisService;
import org.nlh4j.saas.membership_hub.infrastructure.websocket.WebSocketService;
import org.nlh4j.saas.membership_hub.security.RbacUtil;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.CloseReason;
import javax.websocket.server.ServerEndpoint;

/**
 * REST Controller for real-time enrollment dashboard aggregated statistics.
 * Implements cached data retrieval, RBAC access control, and WebSocket real-time update broadcasting
 * for Center Admin and System Admin roles as per enterprise requirements.
 * 
 * @traceability [REQ-025]
 * @author Enterprise Core Engineering Team
 * @version 1.0.0
 */
public class EnrollmentDashboardController {

    // ==============================================
    // ENTERPRISE CONSTANTS DECLARATION (TOP LAYER MANDATE)
    // ==============================================
    /** REST API endpoint path for enrollment dashboard statistics */
    public static final String DASHBOARD_ENDPOINT_PATH = "/api/v1/dashboard/enrollment";
    /** Redis cache key prefix for enrollment dashboard data */
    public static final String REDIS_CACHE_KEY_PREFIX = "dashboard:enrollment:stats:";
    /** Cache TTL in seconds (5 minutes per NFR-004 performance requirements) */
    public static final long CACHE_TTL_SECONDS = 300L;
    /** WebSocket endpoint path for real-time dashboard updates */
    public static final String WS_DASHBOARD_UPDATE_ENDPOINT = "/ws/dashboard/enrollment-updates";
    /** Error message for invalid center ID format */
    public static final String ERR_INVALID_CENTER_ID = "Invalid center ID format. Must be a valid UUID.";
    /** Error message for insufficient permissions to access dashboard */
    public static final String ERR_INSUFFICIENT_PERMISSIONS = "You do not have permission to access this dashboard resource.";
    /** Error message template for Redis connection failures */
    public static final String ERR_REDIS_CONNECTION_FAILURE = "Failed to connect to Redis cache service. Raw error: {}";

    // ==============================================
    // ENTERPRISE LOGGER INITIALIZATION (MANDATORY)
    // ==============================================
    private static final Logger logger = LoggerFactory.getLogger(EnrollmentDashboardController.class);

    // ==============================================
    // DEPENDENCY INJECTION (CDI MANDATE)
    // ==============================================
    /** Service layer for dashboard business logic and high-performance database aggregation */
    @Inject
    private EnrollmentDashboardService enrollmentDashboardService;
    /** Redis service for caching dashboard data to reduce database load */
    @Inject
    private RedisService redisService;
    /** WebSocket service for managing client sessions and broadcasting real-time updates */
    @Inject
    private WebSocketService webSocketService;
    /** RBAC utility for role validation and permission enforcement */
    @Inject
    private RbacUtil rbacUtil;

    // ==============================================
    // REST API ENDPOINT IMPLEMENTATION
    // ==============================================
    /**
     * GET endpoint to retrieve aggregated enrollment dashboard statistics.
     * Returns total enrolled students, active courses, and upcoming sessions for the specified center
     * (or system-wide statistics for System Admin users).
     * 
     * @param centerId Optional query parameter for center ID (only applicable for System Admin role)
     * @param securityContext Injected security context containing authenticated user identity and role
     * @return JSON response with aggregated dashboard statistics
     * @traceability [REQ-025]
     */
    @GET
    @Path(DASHBOARD_ENDPOINT_PATH)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEnrollmentDashboard(@QueryParam("centerId") String centerId, @Context SecurityContext securityContext) {
        // [LOG_ENTRY] Log request initiation with traceability tag and user context
        logger.info("[PROCESS_START] [REQ-025] Fetching enrollment dashboard statistics for authenticated user: {}", 
            securityContext.getUserPrincipal().getName());

        try {
            // ==============================================
            // STEP 1: RBAC ACCESS CONTROL VALIDATION (OWASP MANDATE)
            // ==============================================
            // Extract current user's role from JWT token via security context
            String userRole = rbacUtil.getCurrentUserRole(securityContext);
            UUID targetCenterId = null;

            // Validate user has permission to access dashboard resources
            if (!rbacUtil.hasDashboardAccess(userRole)) {
                // [LOG_SECURITY] Log unauthorized access attempt for audit trail
                logger.warn("[SECURITY_ALERT] [REQ-025] Unauthorized dashboard access attempt by user: {} with assigned role: {}", 
                    securityContext.getUserPrincipal().getName(), userRole);
                // Return 403 Forbidden for roles without dashboard access (e.g. Student, Teacher)
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\": \"FORBIDDEN\", \"message\": \"" + ERR_INSUFFICIENT_PERMISSIONS + "\"}")
                        .build();
            }

            // Handle center ID scoping based on user role
            if ("SYSTEM_ADMIN".equalsIgnoreCase(userRole)) {
                // System Admin can access system-wide stats or specific center stats via query param
                if (centerId != null && !centerId.isBlank()) {
                    try {
                        // Validate center ID is a valid UUID to prevent injection attacks
                        targetCenterId = UUID.fromString(centerId);
                    } catch (IllegalArgumentException e) {
                        // [LOG_VALIDATION] Log invalid center ID format input
                        logger.error("[VALIDATION_FAIL] [REQ-025] Invalid center ID format provided by user: {}. Input value: {}. Raw error: {}", 
                            securityContext.getUserPrincipal().getName(), centerId, e.getMessage());
                        // Return 400 Bad Request for malformed UUID
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("{\"error\": \"INVALID_INPUT\", \"message\": \"" + ERR_INVALID_CENTER_ID + "\"}")
                                .build();
                }
                // If no centerId provided, targetCenterId remains null for system-wide aggregation
            } else {
                // Center Admin and Manager roles can only access their own assigned center's dashboard
                targetCenterId = rbacUtil.getCurrentUserManagedCenterId(securityContext);
                if (targetCenterId == null) {
                    // [LOG_AUTH] Log user with no center assignment attempting dashboard access
                    logger.error("[AUTH_FAIL] [REQ-025] User {} has no assigned center, cannot access dashboard data", 
                        securityContext.getUserPrincipal().getName());
                    return Response.status(Response.Status.FORBIDDEN)
                            .entity("{\"error\": \"FORBIDDEN\", \"message\": \"No center assigned to your account.\"}")
                            .build();
                }
            }

            // ==============================================
            // STEP 2: REDIS CACHE LOOKUP (PERFORMANCE OPTIMIZATION)
            // ==============================================
            // Build unique cache key based on target center (or system-wide scope)
            String cacheKey = targetCenterId != null 
                    ? REDIS_CACHE_KEY_PREFIX + targetCenterId.toString() 
                    : REDIS_CACHE_KEY_PREFIX + "system";
            EnrollmentDashboardDTO cachedStats = null;

            try {
                // Attempt to retrieve pre-aggregated stats from Redis cache
                cachedStats = redisService.get(cacheKey, EnrollmentDashboardDTO.class);
                if (cachedStats != null) {
                    // [LOG_DEBUG] Log cache hit for performance monitoring
                    logger.debug("[CACHE_HIT] [REQ-025] Retrieved dashboard statistics from cache for key: {}", cacheKey);
                    // [LOG_EXIT] Log successful response from cache
                    logger.info("[PROCESS_SUCCESS] [REQ-025] Returned cached dashboard stats for user: {}, centerId: {}", 
                        securityContext.getUserPrincipal().getName(), targetCenterId);
                    return Response.ok(cachedStats).build();
                }
            } catch (Exception e) {
                // [LOG_ERROR] Log Redis connection failure, fallback to database query without failing request
                logger.error("[CACHE_FAIL] [REQ-025] " + ERR_REDIS_CONNECTION_FAILURE, e.getMessage(), e);
                // Proceed to database aggregation if cache is unavailable
            }

            // ==============================================
            // STEP 3: DATABASE AGGREGATION (HIGH PERFORMANCE NATIVE SQL)
            // ==============================================
            // Fetch fresh aggregated stats from service layer (uses native SQL JOINs, no in-memory loops per enterprise rules)
            EnrollmentDashboardDTO dashboardStats;
            try {
                dashboardStats = enrollmentDashboardService.getAggregatedDashboardStats(targetCenterId);
                // Return empty DTO if no data exists for the requested scope
                if (dashboardStats == null) {
                    dashboardStats = new EnrollmentDashboardDTO();
                }
            } catch (Exception e) {
                // [LOG_CRITICAL] Log database aggregation failure with full context for troubleshooting
                logger.error("[DB_FAIL] [REQ-025] Failed to fetch aggregated dashboard statistics from database for centerId: {}. Raw error: {}", 
                    targetCenterId, e.getMessage(), e);
                // Return 500 Internal Server Error for database failures
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\": \"INTERNAL_ERROR\", \"message\": \"Failed to retrieve dashboard statistics. Please try again later.\"}")
                        .build();
            }

            // ==============================================
            // STEP 4: CACHE POPULATION (REDIS PERFORMANCE OPTIMIZATION)
            // ==============================================
            try {
                // Store fresh stats in Redis with 5 minute TTL to reduce database load
                redisService.set(cacheKey, dashboardStats, CACHE_TTL_SECONDS);
                // [LOG_DEBUG] Log successful cache population
                logger.debug("[CACHE_POPULATE] [REQ-025] Cached dashboard statistics for key: {} with TTL: {} seconds", 
                    cacheKey, CACHE_TTL_SECONDS);
            } catch (Exception e) {
                // [LOG_WARN] Log cache population failure, do not fail the request as data is already fetched
                logger.warn("[CACHE_POPULATE_FAIL] [REQ-025] Failed to cache dashboard statistics for key: {}. Raw error: {}", 
                    cacheKey, e.getMessage(), e);
            }

            // [LOG_EXIT] Log successful response from database
            logger.info("[PROCESS_SUCCESS] [REQ-025] Returned fresh dashboard statistics for user: {}, centerId: {}", 
                securityContext.getUserPrincipal().getName(), targetCenterId);
            // Return 200 OK with aggregated dashboard stats
            return Response.ok(dashboardStats).build();

        } catch (Exception e) {
            // [LOG_CRITICAL] Log unhandled exceptions with full context for incident response
            logger.error("[CRITICAL_FAIL] [REQ-025] Unhandled exception while processing enrollment dashboard request for user: {}. Raw error: {}", 
                securityContext.getUserPrincipal().getName(), e.getMessage(), e);
            // Return 500 Internal Server Error for unexpected failures
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"INTERNAL_ERROR\", \"message\": \"An unexpected error occurred. Please contact support.\"}")
                    .build();
        }
    }

    // ==============================================
    // WEBSOCKET REAL-TIME UPDATE IMPLEMENTATION
    // ==============================================
    /**
     * WebSocket endpoint for pushing real-time enrollment dashboard updates to connected frontend clients.
     * Automatically pushes updated statistics when enrollment, cancellation, or course creation events occur.
     * 
     * @traceability [REQ-025]
     */
    @ServerEndpoint(WS_DASHBOARD_UPDATE_ENDPOINT)
    public static class EnrollmentDashboardWebSocket {
        // Inject dependencies for WebSocket handling
        @Inject
        private WebSocketService webSocketService;
        @Inject
        private RbacUtil rbacUtil;
        @Inject
        private EnrollmentDashboardService enrollmentDashboardService;
        @Inject
        private RedisService redisService;

        private static final Logger logger = LoggerFactory.getLogger(EnrollmentDashboardWebSocket.class);
        private static final String ERR_WS_UNAUTHORIZED = "Unauthorized WebSocket access";

        /**
         * Handle new WebSocket client connection.
         * Registers client session to receive updates scoped to their assigned center or system-wide.
         * 
         * @param session Incoming WebSocket session
         * @param securityContext Injected security context for user authentication
         */
        @OnOpen
        public void onOpen(Session session, @Context SecurityContext securityContext) {
            // [LOG_ENTRY] Log new WebSocket connection attempt
            logger.info("[WS_CONNECT] [REQ-025] New WebSocket connection attempt from user: {}", 
                securityContext.getUserPrincipal().getName());

            try {
                // Validate user authentication
                if (securityContext.getUserPrincipal() == null) {
                    // Close connection for unauthenticated users per security policy
                    session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, ERR_WS_UNAUTHORIZED));
                    return;
                }

                // Validate user role has permission to receive dashboard updates
                String userRole = rbacUtil.getCurrentUserRole(securityContext);
                if (!rbacUtil.hasDashboardAccess(userRole)) {
                    session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, ERR_INSUFFICIENT_PERMISSIONS));
                    return;
                }

                UUID userCenterId = rbacUtil.getCurrentUserManagedCenterId(securityContext);
                if ("SYSTEM_ADMIN".equalsIgnoreCase(userRole)) {
                    // Register System Admin for system-wide updates
                    webSocketService.registerSession(session, null, securityContext.getUserPrincipal().getName());
                } else if (userCenterId != null) {
                    // Register Center Admin/Manager for center-specific updates only
                    webSocketService.registerSession(session, userCenterId, securityContext.getUserPrincipal().getName());
                } else {
                    session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "No center assigned to account"));
                }
            } catch (Exception e) {
                // [LOG_ERROR] Log WebSocket connection handling failure
                logger.error("[WS_CONNECT_FAIL] [REQ-025] Failed to establish WebSocket connection for user: {}. Raw error: {}", 
                    securityContext.getUserPrincipal().getName(), e.getMessage(), e);
                try {
                    session.close(new CloseReason(CloseReason.CloseCodes.INTERNAL_ERROR, "Connection establishment failed"));
                } catch (IOException ioException) {
                    logger.error("[WS_CLOSE_FAIL] [REQ-025] Failed to close failed WebSocket session for user: {}. Raw error: {}", 
                        securityContext.getUserPrincipal().getName(), ioException.getMessage(), ioException);
                }
            }
        }

        /**
         * Handle incoming WebSocket messages (used for keep-alive ping/pong).
         * 
         * @param message Incoming message from client
         * @param session Active WebSocket session
         */
        @OnMessage
        public void onMessage(String message, Session session) {
            // [LOG_DEBUG] Log incoming WebSocket message for debugging
            logger.debug("[WS_MESSAGE] [REQ-025] Received WebSocket message from client: {}", message);
            // Handle keep-alive ping messages
            if ("ping".equalsIgnoreCase(message.trim())) {
                try {
                    session.getAsyncRemote().sendText("pong");
                } catch (Exception e) {
                    logger.error("[WS_SEND_FAIL] [REQ-025] Failed to send pong response to client. Raw error: {}", e.getMessage(), e);
                }
            }
        }

        /**
         * Handle WebSocket connection close.
         * Cleans up session resources from WebSocket service.
         * 
         * @param session Closing WebSocket session
         * @param reason Close reason code and message
         */
        @OnClose
        public void onClose(Session session, CloseReason reason) {
            // [LOG_INFO] Log WebSocket disconnection for audit
            logger.info("[WS_DISCONNECT] [REQ-025] WebSocket connection closed. Reason: {}", reason.getReasonPhrase());
            // Unregister session to free up resources
            webSocketService.unregisterSession(session);
        }

        /**
         * Handle WebSocket connection errors.
         * Logs error details and cleans up session resources.
         * 
         * @param session Errored WebSocket session
         * @param throwable Root cause of the error
         */
        @OnError
        public void onError(Session session, Throwable throwable) {
            // [LOG_ERROR] Log WebSocket runtime error
            logger.error("[WS_ERROR] [REQ-025] WebSocket runtime error occurred. Raw error: {}", throwable.getMessage(), throwable);
            // Clean up session resources
            webSocketService.unregisterSession(session);
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.INTERNAL_ERROR, "Runtime connection error"));
            } catch (IOException e) {
                logger.error("[WS_CLOSE_FAIL] [REQ-025] Failed to close errored WebSocket session. Raw error: {}", e.getMessage(), e);
            }
        }

        // ==============================================
        // REAL-TIME UPDATE BROADCAST API
        // ==============================================
        /**
         * Broadcast updated dashboard statistics to all relevant connected frontend clients.
         * Triggered by enrollment creation, cancellation, or new course creation events.
         * Automatically evicts stale cache before fetching and broadcasting fresh data.
         * 
         * @param centerId Center ID to broadcast updates for (null for system-wide updates)
         * @traceability [REQ-025]
         */
        public void broadcastDashboardUpdate(UUID centerId) {
            try {
                // [LOG_DEBUG] Log broadcast initiation
                logger.debug("[WS_BROADCAST_START] [REQ-025] Initiating dashboard update broadcast for centerId: {}", centerId);
                
                // Evict stale cache to ensure fresh data is fetched
                evictDashboardCache(centerId);
                
                // Fetch fresh aggregated stats
                EnrollmentDashboardDTO updatedStats = enrollmentDashboardService.getAggregatedDashboardStats(centerId);
                if (updatedStats == null) {
                    updatedStats = new EnrollmentDashboardDTO();
                }

                // Broadcast update to all relevant connected clients
                webSocketService.broadcastToCenter(centerId, updatedStats);
                
                // [LOG_INFO] Log successful broadcast
                logger.info("[WS_BROADCAST_SUCCESS] [REQ-025] Successfully broadcasted dashboard update for centerId: {}", centerId);
            } catch (Exception e) {
                // [LOG_ERROR] Log broadcast failure for troubleshooting
                logger.error("[WS_BROADCAST_FAIL] [REQ-025] Failed to broadcast dashboard update for centerId: {}. Raw error: {}", 
                    centerId, e.getMessage(), e);
            }
        }

        /**
         * Evict cached dashboard statistics for a specific center or system-wide scope.
         * Called automatically before broadcasting updates to ensure clients receive fresh data.
         * 
         * @param centerId Center ID to evict cache for (null for system-wide cache)
         * @traceability [REQ-025]
         */
        private void evictDashboardCache(UUID centerId) {
            try {
                String cacheKey = centerId != null 
                        ? REDIS_CACHE_KEY_PREFIX + centerId.toString() 
                        : REDIS_CACHE_KEY_PREFIX + "system";
                redisService.delete(cacheKey);
                logger.debug("[CACHE_EVICT] [REQ-025] Evicted stale dashboard cache for key: {}", cacheKey);
            } catch (Exception e) {
                logger.warn("[CACHE_EVICT_FAIL] [REQ-025] Failed to evict dashboard cache for centerId: {}. Raw error: {}", 
                    centerId, e.getMessage(), e);
            }
        }
    }
}