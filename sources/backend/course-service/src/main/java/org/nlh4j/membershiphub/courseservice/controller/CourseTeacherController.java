package org.nlh4j.membershiphub.courseservice.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.nlh4j.membershiphub.courseservice.dto.TeacherAssignRequest;
import org.nlh4j.membershiphub.courseservice.messaging.KafkaTeacherProducer;
import org.nlh4j.membershiphub.courseservice.service.CourseTeacherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller responsible for course-to-teacher assignment and unassignment operations.
 * <p>
 * Enforces strict transactional integrity, Role-Based Access Control (RBAC),
 * and transactional outbox/asynchronous event streaming to Apache Kafka for downstream consumers.
 * </p>
 *
 * @author Enterprise Architecture Team
 * @version 1.0.0
 * @traceability [REQ-009], [ARC-007]
 */
@Path(CourseTeacherController.BASE_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class CourseTeacherController {

    // =========================================================================
    // 0. TOP-OF-CLASS IMMUTABLE CONSTANTS DECLARATION
    // =========================================================================

    /** Base endpoint routing URI template for teacher assignment sub-resource */
    public static final String BASE_PATH = "/api/v1/courses/{id}/teachers";

    /** Path parameter variable binding name for Course UUID */
    public static final String PATH_PARAM_ID = "id";

    /** Path parameter variable binding name for Teacher UUID */
    public static final String PATH_PARAM_TEACHER_ID = "teacherId";

    /** HTTP header key used to validate and guarantee operational idempotency */
    public static final String HEADER_IDEMPOTENCY_KEY = "Idempotency-Key";

    /** System Administrator role key granting global administrative authority */
    public static final String ROLE_SYSTEM_ADMIN = "SystemAdmin";

    /** Center Administrator role key granting regional center-level authority */
    public static final String ROLE_CENTER_ADMIN = "CenterAdmin";

    /** Kafka target topic name for teacher assignment and unassignment lifecycle events */
    public static final String TOPIC_TEACHER_EVENTS = "teacher-events";

    /** Event type payload indicator representing a teacher assignment operation */
    public static final String EVENT_TYPE_TEACHER_ASSIGNED = "teacher-assigned";

    /** Event type payload indicator representing a teacher unassignment operation */
    public static final String EVENT_TYPE_TEACHER_UNASSIGNED = "teacher-unassigned";

    /** Subsystem diagnostic module identification label for centralized audit logs */
    public static final String SUBSYSTEM_MODULE = "COURSE-SERVICE-TEACHER-CONTROLLER";

    /** Log format template string for operational function entry tracing */
    private static final String LOG_ENTRY_TEMPLATE = "[ENTRY] [REQ-009] [ARC-007] Subsystem: {} | Operation: {} | CourseID: {} | TeacherID: {} | IdempotencyKey: {}";

    /** Log format template string for operational function exit tracing */
    private static final String LOG_EXIT_TEMPLATE = "[EXIT] [REQ-009] [ARC-007] Subsystem: {} | Operation: {} | CourseID: {} | TeacherID: {} | HTTP Status: {}";

    /** Log format template string for operational error audit reporting */
    private static final String LOG_ERROR_TEMPLATE = "[CRITICAL FAIL] [REQ-009] [ARC-007] Subsystem: {} | Operation: {} | CourseID: {} | TeacherID: {} | Error Message: {}";

    /** Response payload key for the course unique identifier */
    public static final String KEY_COURSE_ID = "courseId";

    /** Response payload key for the teacher unique identifier */
    public static final String KEY_TEACHER_ID = "teacherId";

    /** Response payload key for the operational event action type */
    public static final String KEY_EVENT_TYPE = "eventType";

    /** Response payload key for the ISO-8601 operational timestamp */
    public static final String KEY_ASSIGNED_AT = "assignedAt";

    /** Response payload key for operational human-readable status feedback */
    public static final String KEY_MESSAGE = "message";

    /** Success notification message emitted upon successful teacher binding */
    public static final String MSG_ASSIGN_SUCCESS = "Teacher successfully assigned to course";

    /** Success notification message emitted upon successful teacher detachment */
    public static final String MSG_UNASSIGN_SUCCESS = "Teacher successfully unassigned from course";

    /** Sub-resource path pattern for detaching a specific teacher identifier */
    public static final String SUB_PATH_TEACHER_ID = "/{" + PATH_PARAM_TEACHER_ID + "}";

    // =========================================================================
    // 1. CLASS LOGGER INITIALIZATION
    // =========================================================================

    /** Thread-safe centralized SLF4J logger binding */
    private static final Logger LOGGER = LoggerFactory.getLogger(CourseTeacherController.class);

    // =========================================================================
    // 2. DEPENDENCY INJECTION CONTAINERS
    // =========================================================================

    /** Domain service encapsulating database transaction logic for course-teacher bindings */
    @Inject
    CourseTeacherService courseTeacherService;

    /** Reactive Apache Kafka publisher transmitting teacher management events */
    @Inject
    KafkaTeacherProducer kafkaTeacherProducer;

    // =========================================================================
    // 3. REST CONTROLLER ENDPOINTS
    // =========================================================================

    /**
     * Assigns an instructor to a designated course and asynchronously dispatches a Kafka notification event.
     * <p>
     * Implements transaction atomicity ensuring database persistence and transactional outbox emission
     * align simultaneously. Rejects requests with schedule conflicts or authorization deficits.
     * </p>
     *
     * @param courseIdRaw    the unique identifier of the target course parsed from the URL path.
     * @param request        the validated DTO carrying the target teacher's UUID.
     * @param idempotencyKey optional client-supplied idempotency key preventing duplicate bindings.
     * @return HTTP 201 Created with JSON metadata and resource location URI.
     * @traceability [REQ-009], [ARC-007]
     */
    @POST
    @Transactional
    @RolesAllowed({ROLE_SYSTEM_ADMIN, ROLE_CENTER_ADMIN})
    public Response assignTeacherToCourse(
            @PathParam(PATH_PARAM_ID) @NotNull final String courseIdRaw,
            @Valid @NotNull final TeacherAssignRequest request,
            @HeaderParam(HEADER_IDEMPOTENCY_KEY) final String idempotencyKey) {

        // [REQ-009] Extract and mask sensitive tokens, preparing UUID conversion
        final String maskedKey = maskIdempotencyKey(idempotencyKey);
        final String operationName = "assignTeacherToCourse";

        // [REQ-009] Log execution entry boundary with context tracing
        LOGGER.info(LOG_ENTRY_TEMPLATE, SUBSYSTEM_MODULE, operationName, courseIdRaw, request.getTeacherId(), maskedKey);

        try {
            // [REQ-009] Parse course UUID from path parameter with defensive integrity check
            final UUID courseId = UUID.fromString(courseIdRaw);
            // [REQ-009] Extract teacher UUID from bean-validated request entity
            final UUID teacherId = request.getTeacherId();

            // [REQ-009] Persist teacher-course relationship with DB schedule exclusion check
            courseTeacherService.assignTeacher(courseId, teacherId, idempotencyKey);

            // [ARC-007] Construct immutable timestamp string conforming to ISO-8601 UTC format
            final String assignedAtTimestamp = Instant.now().toString();

            // [ARC-007] Prepare typed message payload map for Kafka event ingestion
            final Map<String, Object> eventPayload = new HashMap<>();
            // [ARC-007] Populate event contract metadata attributes
            eventPayload.put(KEY_EVENT_TYPE, EVENT_TYPE_TEACHER_ASSIGNED);
            // [ARC-007] Set course UUID in Kafka event payload
            eventPayload.put(KEY_COURSE_ID, courseId.toString());
            // [ARC-007] Set teacher UUID in Kafka event payload
            eventPayload.put(KEY_TEACHER_ID, teacherId.toString());
            // [ARC-007] Set execution instant timestamp
            eventPayload.put(KEY_ASSIGNED_AT, assignedAtTimestamp);

            // [ARC-007] Stream event to Apache Kafka topic partitioned deterministically by courseId
            kafkaTeacherProducer.publishTeacherEvent(
                    TOPIC_TEACHER_EVENTS,
                    courseId.toString(),
                    Collections.unmodifiableMap(eventPayload)
            );

            // [REQ-009] Build client success response payload
            final Map<String, Object> responseBody = new HashMap<>();
            // [REQ-009] Populate operational outcome information
            responseBody.put(KEY_MESSAGE, MSG_ASSIGN_SUCCESS);
            // [REQ-009] Populate confirmed course ID
            responseBody.put(KEY_COURSE_ID, courseId);
            // [REQ-009] Populate confirmed teacher ID
            responseBody.put(KEY_TEACHER_ID, teacherId);
            // [REQ-009] Populate operational timestamp
            responseBody.put(KEY_ASSIGNED_AT, assignedAtTimestamp);

            // [REQ-009] Build canonical resource access URI for newly assigned teacher entity
            final URI locationUri = URI.create(BASE_PATH.replace("{" + PATH_PARAM_ID + "}", courseId.toString()) + "/" + teacherId);

            // [REQ-009] Log execution exit gate prior to response dispatch
            LOGGER.info(LOG_EXIT_TEMPLATE, SUBSYSTEM_MODULE, operationName, courseId, teacherId, Response.Status.CREATED.getStatusCode());

            // [REQ-009] Return HTTP 201 Created containing metadata and location header
            return Response.created(locationUri)
                    .entity(Collections.unmodifiableMap(responseBody))
                    .build();

        } catch (IllegalArgumentException ex) {
            // [REQ-009] Audit malformed UUID or parsing errors explicitly
            LOGGER.error(LOG_ERROR_TEMPLATE, SUBSYSTEM_MODULE, operationName, courseIdRaw, request.getTeacherId(), ex.getMessage());
            // [REQ-009] Forward raw cause while throwing domain exception to safeguard API boundary
            throw ex;
        } catch (Exception ex) {
            // [REQ-009] Log critical unexpected execution failure with context tracking tags
            LOGGER.error(LOG_ERROR_TEMPLATE, SUBSYSTEM_MODULE, operationName, courseIdRaw, request.getTeacherId(), ex.getMessage());
            // [REQ-009] Preserve exception cause chain in accordance with Enterprise Exception Auditing Law
            throw new RuntimeException("Operational failure assigning teacher to course [REQ-009]: " + ex.getMessage(), ex);
        }
    }

    /**
     * Unassigns an instructor from a designated course and broadcasts an event to Apache Kafka.
     * <p>
     * Ensures atomic removal of the mapping entry in database tables and guarantees notification
     * to downstream notification and attendance services for timetable synchronizations.
     * </p>
     *
     * @param courseIdRaw     the unique identifier of the target course parsed from the URL path.
     * @param teacherIdRaw    the unique identifier of the target teacher parsed from the sub-path.
     * @param idempotencyKey  optional client-supplied idempotency key preventing duplicate executions.
     * @return HTTP 204 No Content upon verified unassignment.
     * @traceability [REQ-009], [ARC-007]
     */
    @DELETE
    @Path(SUB_PATH_TEACHER_ID)
    @Transactional
    @RolesAllowed({ROLE_SYSTEM_ADMIN, ROLE_CENTER_ADMIN})
    public Response unassignTeacherFromCourse(
            @PathParam(PATH_PARAM_ID) @NotNull final String courseIdRaw,
            @PathParam(PATH_PARAM_TEACHER_ID) @NotNull final String teacherIdRaw,
            @HeaderParam(HEADER_IDEMPOTENCY_KEY) final String idempotencyKey) {

        // [REQ-009] Prepare idempotency masking and operation label
        final String maskedKey = maskIdempotencyKey(idempotencyKey);
        final String operationName = "unassignTeacherFromCourse";

        // [REQ-009] Log execution entry boundary with context tracing
        LOGGER.info(LOG_ENTRY_TEMPLATE, SUBSYSTEM_MODULE, operationName, courseIdRaw, teacherIdRaw, maskedKey);

        try {
            // [REQ-009] Parse course UUID parameter
            final UUID courseId = UUID.fromString(courseIdRaw);
            // [REQ-009] Parse teacher UUID parameter
            final UUID teacherId = UUID.fromString(teacherIdRaw);

            // [REQ-009] Delegate physical or logical detachment to the transactional service layer
            courseTeacherService.unassignTeacher(courseId, teacherId, idempotencyKey);

            // [ARC-007] Construct UTC timestamp
            final String unassignedAtTimestamp = Instant.now().toString();

            // [ARC-007] Assemble detachment payload contract for Kafka distribution
            final Map<String, Object> eventPayload = new HashMap<>();
            // [ARC-007] Assign event classification
            eventPayload.put(KEY_EVENT_TYPE, EVENT_TYPE_TEACHER_UNASSIGNED);
            // [ARC-007] Set course UUID
            eventPayload.put(KEY_COURSE_ID, courseId.toString());
            // [ARC-007] Set teacher UUID
            eventPayload.put(KEY_TEACHER_ID, teacherId.toString());
            // [ARC-007] Set removal instant
            eventPayload.put(KEY_ASSIGNED_AT, unassignedAtTimestamp);

            // [ARC-007] Emit asynchronous unassignment event to Kafka cluster topic
            kafkaTeacherProducer.publishTeacherEvent(
                    TOPIC_TEACHER_EVENTS,
                    courseId.toString(),
                    Collections.unmodifiableMap(eventPayload)
            );

            // [REQ-009] Log execution completion exit boundary
            LOGGER.info(LOG_EXIT_TEMPLATE, SUBSYSTEM_MODULE, operationName, courseId, teacherId, Response.Status.NO_CONTENT.getStatusCode());

            // [REQ-009] Return HTTP 204 No Content per standard REST modification guidelines
            return Response.noContent().build();

        } catch (IllegalArgumentException ex) {
            // [REQ-009] Capture format parsing violations
            LOGGER.error(LOG_ERROR_TEMPLATE, SUBSYSTEM_MODULE, operationName, courseIdRaw, teacherIdRaw, ex.getMessage());
            // [REQ-009] Forward exception maintaining original boundary
            throw ex;
        } catch (Exception ex) {
            // [REQ-009] Log critical operational failure with context keys
            LOGGER.error(LOG_ERROR_TEMPLATE, SUBSYSTEM_MODULE, operationName, courseIdRaw, teacherIdRaw, ex.getMessage());
            // [REQ-009] Preserve exception cause chain in accordance with Enterprise Exception Auditing Law
            throw new RuntimeException("Operational failure unassigning teacher from course [REQ-009]: " + ex.getMessage(), ex);
        }
    }

    // =========================================================================
    // 4. PRIVATE UTILITY AND DATA MASKING ROUTINES
    // =========================================================================

    /**
     * Programmatically sanitizes and masks sensitive idempotency keys to safeguard audit logs.
     * <p>
     * In accordance with Enterprise Log Scrubbing Law, prevents exposure of raw secret headers.
     * </p>
     *
     * @param rawKey the cleartext idempotency key received in the HTTP request.
     * @return a masked string representation preserving only bounding characters, or "NONE" if null.
     */
    private String maskIdempotencyKey(final String rawKey) {
        // [ARC-007] Validate presence of raw key before formatting
        if (rawKey == null || rawKey.trim().isEmpty()) {
            return "NONE";
        }
        final String trimmed = rawKey.trim();
        // [ARC-007] Mask short strings completely
        if (trimmed.length() <= 8) {
            return "***MASKED***";
        }
        // [ARC-007] Retain initial 4 and final 4 characters separated by masking pattern
        return trimmed.substring(0, 4) + "****" + trimmed.substring(trimmed.length() - 4);
    }
}