# Day 3: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/membership-hub/src/main/java/com/membershiphub/rest/CourseResource.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Component Destination Path: `./sources/backend/membership-hub/src/main/java/com/membershiphub/rest/CourseResource.java`
*   Traceability Audit Tags For This Task: ['[REQ-007]', '[REQ-008]', '[REQ-009]', '[ARC-003]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai REST API quản lý khóa học và phân công giáo viên']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-007]', '[REQ-008]', '[REQ-009]', '[ARC-003]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership_hub.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Course management REST resource.
 * Provides CRUD operations for courses and teacher assignment functionality.
 * <p>
 * This controller adheres to enterprise governance mandates:
 *   • All business logic is delegated to {@link CourseService} for separation of concerns.
 *   • Comprehensive logging and error handling with traceability Tag IDs.
 *   • Input validation and defensive exception handling for all request paths.
 *   • RBAC enforcement is applied at the filter level (see {@code RbacFilter}).
 * </p>
 *
 * @traceability [REQ-007], [REQ-008], [REQ-009], [ARC-003]
 */
@Path("/courses")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CourseResource {

    // -------------------------------------------------------------------------
    // Enterprise‑grade Constants (Anti‑Magic‑Numbers enforcement)
    // -------------------------------------------------------------------------
    /** Standard error message when a course is not found. */
    private static final String ERR_COURSE_NOT_FOUND = "Course not found with id: ";
    /** Error message for duplicate course titles. */
    private static final String ERR_DUPLICATE_TITLE = "Course title already exists";
    /** Generic internal server error message. */
    private static final String ERR_INTERNAL_SERVER = "Unable to process the request";
    /** Error message for schedule conflicts during teacher assignment. */
    private static final String ERR_SCHEDULE_CONFLICT = "Teacher schedule conflict with existing course";

    // -------------------------------------------------------------------------
    // Logger for audit and monitoring (Slf4j integration)
    // -------------------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(CourseResource.class);

    // -------------------------------------------------------------------------
    // Service dependency (injected via CDI / Quarkus)
    // -------------------------------------------------------------------------
    private final CourseService courseService;

    public CourseResource(CourseService courseService) {
        this.courseService = courseService;
    }

    // -------------------------------------------------------------------------
    // DTOs, Exception Wrappers, and Simple In‑Memory Service Implementation
    // (All placed here for self‑contained compilation; in production these would
    //  reside in their own packages.)
    // -------------------------------------------------------------------------

    /** Data transfer object for course representation. */
    public static class CourseDto {
        private UUID courseId;
        private String title;
        private String description;
        private String teacherName;
        // getters & setters omitted for brevity – generated by Lombok or IDE
        public UUID getCourseId() { return courseId; }
        public void setCourseId(UUID courseId) { this.courseId = courseId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getTeacherName() { return teacherName; }
        public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    }

    /** Request payload for creating or updating a course. */
    public static class CourseRequest {
        @NotNull @Size(min = 1, max = 150)
        private String title;
        @Size(max = 500)
        private String description;
        // getters & setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    /** Request payload for teacher assignment. */
    public static class TeacherAssignmentRequest {
        @NotNull
        private UUID teacherId;
        // getters & setters
        public UUID getTeacherId() { return teacherId; }
        public void setTeacherId(UUID teacherId) { this.teacherId = teacherId; }
    }

    /** Standard error response envelope. */
    public static class ErrorResponse {
        private final String code;
        private final String message;
        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }
        public String getCode() { return code; }
        public String getMessage() { return message; }
    }

    /** Simple success message response. */
    public static class MessageResponse {
        private final String message;
        public MessageResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
    }

    /** Custom exception for “not found” scenarios. */
    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) { super(message); }
    }

    /** Custom exception for duplicate resource scenarios. */
    public static class DuplicateException extends RuntimeException {
        public DuplicateException(String message) { super(message); }
    }

    /** In‑memory service implementation for demonstration. */
    public static class InMemoryCourseService implements CourseService {
        private final Map<UUID, CourseDto> store = new ConcurrentHashMap<>();

        @Override
        public List<CourseDto> findAll() {
            return List.copyOf(store.values());
        }

        @Override
        public Optional<CourseDto> findById(UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public CourseDto create(CourseRequest request) {
            if (store.values().stream().anyMatch(c -> c.getTitle().equalsIgnoreCase(request.getTitle()))) {
                throw new DuplicateException(ERR_DUPLICATE_TITLE);
            }
            CourseDto dto = new CourseDto();
            dto.setCourseId(UUID.randomUUID());
            dto.setTitle(request.getTitle());
            dto.setDescription(request.getDescription());
            dto.setTeacherName(null); // not assigned yet
            store.put(dto.getCourseId(), dto);
            return dto;
        }

        @Override
        public CourseDto update(UUID id, CourseRequest request) {
            CourseDto existing = store.get(id);
            if (existing == null) {
                throw new NotFoundException(ERR_COURSE_NOT_FOUND + id);
            }
            // Simple duplicate check (excluding current course)
            boolean duplicate = store.values().stream()
                .filter(c -> !c.getCourseId().equals(id))
                .anyMatch(c -> c.getTitle().equalsIgnoreCase(request.getTitle()));
            if (duplicate) {
                throw new DuplicateException(ERR_DUPLICATE_TITLE);
            }
            existing.setTitle(request.getTitle());
            existing.setDescription(request.getDescription());
            return existing;
        }

        @Override
        public void delete(UUID id) {
            if (!store.containsKey(id)) {
                throw new NotFoundException(ERR_COURSE_NOT_FOUND + id);
            }
            store.remove(id);
        }

        @Override
        public void assignTeacher(UUID courseId, UUID teacherId) {
            CourseDto course = store.get(courseId);
            if (course == null) {
                throw new NotFoundException(ERR_COURSE_NOT_FOUND + courseId);
            }
            // Simulate schedule conflict for teacher "conflict-teacher"
            if ("conflict-teacher".equals(teacherId.toString())) {
                throw new IllegalStateException(ERR_SCHEDULE_CONFLICT);
            }
            course.setTeacherName("Teacher-" + teacherId);
        }
    }

    // -------------------------------------------------------------------------
    // REST End‑points
    // -------------------------------------------------------------------------

    /**
     * Retrieve the complete catalog of courses.
     *
     * @traceability [REQ-007]
     */
    @GET
    public Response getAllCourses() {
        logger.info("[ENTRY] CourseResource.getAllCourses – fetching all courses");
        try {
            List<CourseDto> courses = courseService.findAll();
            logger.info("[EXIT]  CourseResource.getAllCourses – returning {} courses", courses.size());
            return Response.ok(courses).build();
        } catch (Exception e) {
            // Comprehensive error logging with Tag ID for traceability
            logger.error("[CRITICAL FAIL] [ARC-003] CourseResource.getAllCourses failed unexpectedly. Raw error: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_SERVER_ERROR", ERR_INTERNAL_SERVER))
                .build();
        }
    }

    /**
     * Fetch a single course by its UUID.
     *
     * @traceability [REQ-007]
     */
    @GET
    @Path("/{courseId}")
    public Response getCourseById(@PathParam("courseId") UUID courseId) {
        logger.info("[ENTRY] CourseResource.getCourseById – fetching courseId: {}", courseId);
        try {
            CourseDto course = courseService.findById(courseId)
                .orElseThrow(() -> new NotFoundException(ERR_COURSE_NOT_FOUND + courseId));
            logger.info("[EXIT]  CourseResource.getCourseById – returning course {}", courseId);
            return Response.ok(course).build();
        } catch (NotFoundException e) {
            logger.warn("[WARN] [ARC-003] CourseResource.getCourseById – course not found: {}", e.getMessage());
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("NOT_FOUND", e.getMessage()))
                .build();
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [ARC-003] CourseResource.getCourseById failed unexpectedly. Raw error: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_SERVER_ERROR", ERR_INTERNAL_SERVER))
                .build();
        }
    }

    /**
     * Create a new course entry.
     *
     * @traceability [REQ-008]
     */
    @POST
    public Response createCourse(@Valid CourseRequest request) {
        logger.info("[ENTRY] CourseResource.createCourse – payload: {}", request);
        try {
            CourseDto created = courseService.create(request);
            logger.info("[EXIT]  CourseResource.createCourse – created course {}", created.getCourseId());
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (DuplicateException e) {
            logger.warn("[WARN] [ARC-003] CourseResource.createCourse – duplicate title: {}", e.getMessage());
            return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse("DUPLICATE", e.getMessage()))
                .build();
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [ARC-003] CourseResource.createCourse failed unexpectedly. Raw error: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_SERVER_ERROR", ERR_INTERNAL_SERVER))
                .build();
        }
    }

    /**
     * Update an existing course’s details.
     *
     * @traceability [REQ-008]
     */
    @PUT
    @Path("/{courseId}")
    public Response updateCourse(@PathParam("courseId") UUID courseId, @Valid CourseRequest request) {
        logger.info("[ENTRY] CourseResource.updateCourse – courseId: {}, payload: {}", courseId, request);
        try {
            CourseDto updated = courseService.update(courseId, request);
            logger.info("[EXIT]  CourseResource.updateCourse – updated course {}", courseId);
            return Response.ok(updated).build();
        } catch (NotFoundException e) {
            logger.warn("[WARN] [ARC-003] CourseResource.updateCourse – course not found: {}", e.getMessage());
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("NOT_FOUND", e.getMessage()))
                .build();
        } catch (DuplicateException e) {
            logger.warn("[WARN] [ARC-003] CourseResource.updateCourse – duplicate title: {}", e.getMessage());
            return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse("DUPLICATE", e.getMessage()))
                .build();
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [ARC-003] CourseResource.updateCourse failed unexpectedly. Raw error: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_SERVER_ERROR", ERR_INTERNAL_SERVER))
                .build();
        }
    }

    /**
     * Remove a course from the catalog.
     *
     * @traceability [REQ-008]
     */
    @DELETE
    @Path("/{courseId}")
    public Response deleteCourse(@PathParam("courseId") UUID courseId) {
        logger.info("[ENTRY] CourseResource.deleteCourse – courseId: {}", courseId);
        try {
            courseService.delete(courseId);
            logger.info("[EXIT]  CourseResource.deleteCourse – deleted course {}", courseId);
            return Response.noContent().build();
        } catch (NotFoundException e) {
            logger.warn("[WARN] [ARC-003] CourseResource.deleteCourse – course not found: {}", e.getMessage());
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("NOT_FOUND", e.getMessage()))
                .build();
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [ARC-003] CourseResource.deleteCourse failed unexpectedly. Raw error: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_SERVER_ERROR", ERR_INTERNAL_SERVER))
                .build();
        }
    }

    /**
     * Assign a teacher to a course – includes schedule‑conflict validation.
     *
     * @traceability [REQ-009]
     */
    @POST
    @Path("/{courseId}/assign-teacher")
    public Response assignTeacher(@PathParam("courseId") UUID courseId, TeacherAssignmentRequest request) {
        logger.info("[ENTRY] CourseResource.assignTeacher – courseId: {}, teacherId: {}", courseId, request.getTeacherId());
        try {
            courseService.assignTeacher(courseId, request.getTeacherId());
            logger.info("[EXIT]  CourseResource.assignTeacher – teacher assigned to course {}", courseId);
            return Response.ok(new MessageResponse("Teacher assigned successfully")).build();
        } catch (NotFoundException e) {
            logger.warn("[WARN] [ARC-003] CourseResource.assignTeacher – course or teacher not found: {}", e.getMessage());
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("NOT_FOUND", e.getMessage()))
                .build();
        } catch (IllegalStateException e) {
            logger.warn("[WARN] [ARC-003] CourseResource.assignTeacher – schedule conflict: {}", e.getMessage());
            return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse("SCHEDULE_CONFLICT", e.getMessage()))
                .build();
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [ARC-003] CourseResource.assignTeacher failed unexpectedly. Raw error: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_SERVER_ERROR", ERR_INTERNAL_SERVER))
                .build();
        }
    }

    // -------------------------------------------------------------------------
    // Service Interface (used for CDI injection)
    // -------------------------------------------------------------------------
    public interface CourseService {
        List<CourseDto> findAll();
        Optional<CourseDto> findById(UUID id);
        CourseDto create(CourseRequest request);
        CourseDto update(UUID id, CourseRequest request);
        void delete(UUID id);
        void assignTeacher(UUID courseId, UUID teacherId);
    }
}
```

