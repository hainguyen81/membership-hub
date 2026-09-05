package org.nlh4j.membershiphub.courseservice.controller;

import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.nlh4j.membershiphub.courseservice.dto.CourseResponse;
import org.nlh4j.membershiphub.courseservice.dto.CourseCreateRequest;
import org.nlh4j.membershiphub.courseservice.service.CourseService;
import org.nlh4j.membershiphub.courseservice.exception.CourseNotFoundException;
import org.nlh4j.membershiphub.courseservice.exception.CourseValidationException;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Course management REST controller.
 *
 * Traceability: [REQ-007], [REQ-008]
 */
@Path("/api/v1/courses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Course Management")
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    // Constants for pagination defaults
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final String DEFAULT_SORT = "startDate,asc";

    private final CourseService courseService;

    // Constructor injection
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * Retrieve a paginated list of courses.
     *
     * Traceability: [REQ-007]
     */
    @GET
    @Operation(summary = "List courses with pagination")
    @APIResponse(responseCode = "200", description = "Paginated list of courses")
    public Response listCourses(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue(String.valueOf(DEFAULT_PAGE_SIZE)) int size,
            @QueryParam("sort") @DefaultValue(DEFAULT_SORT) String sort) {
        logger.info("[INFO] [REQ-007] Listing courses page={} size={} sort={}", page, size, sort);
        try {
            // Use JPQL parameter binding inside service layer to prevent SQL injection (OWASP A03)
            var pageResult = courseService.findAll(page, size, sort);
            return Response.ok(pageResult).build();
        } catch (Exception e) {
            logger.error("[ERROR] [REQ-007] Failed to list courses. Raw error: {}", e.getMessage(), e);
            throw new RuntimeException("Internal server error while listing courses", e);
        }
    }

    /**
     * Create a new course.
     *
     * Traceability: [REQ-008]
     */
    @POST
    @Operation(summary = "Create a new course")
    @APIResponse(responseCode = "201", description = "Course created successfully")
    @APIResponse(responseCode = "400", description = "Invalid request payload")
    @RolesAllowed({"SystemAdmin", "CenterAdmin"})
    public Response createCourse(@Valid CourseCreateRequest request) {
        logger.info("[INFO] [REQ-008] Creating course with title='{}'", request.getTitle());
        try {
            CourseResponse created = courseService.create(request);
            logger.debug("[DEBUG] [REQ-008] Course created with ID: {}", created.getCourseId());
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (IllegalArgumentException e) {
            logger.warn("[WARN] [REQ-008] Validation failed for course creation: {}", e.getMessage());
            throw new CourseValidationException(e.getMessage());
        } catch (Exception e) {
            logger.error("[ERROR] [REQ-008] Unexpected error during course creation. Raw error: {}", e.getMessage(), e);
            throw new RuntimeException("Internal server error while creating course", e);
        }
    }

    /**
     * Update an existing course.
     *
     * Traceability: [REQ-008]
     */
    @PUT
    @Path("/{id}")
    @Operation(summary = "Update an existing course")
    @APIResponse(responseCode = "200", description = "Course updated successfully")
    @APIResponse(responseCode = "404", description = "Course not found")
    @RolesAllowed({"SystemAdmin", "CenterAdmin"})
    public Response updateCourse(@PathParam("id") String id, @Valid CourseCreateRequest request) {
        logger.info("[INFO] [REQ-008] Updating course ID: {}", id);
        try {
            CourseResponse updated = courseService.update(id, request);
            logger.debug("[DEBUG] [REQ-008] Course updated ID: {}", id);
            return Response.ok(updated).build();
        } catch (CourseNotFoundException e) {
            logger.warn("[WARN] [REQ-008] Course not found for update: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("[ERROR] [REQ-008] Unexpected error during course update. Raw error: {}", e.getMessage(), e);
            throw new RuntimeException("Internal server error while updating course", e);
        }
    }

    /**
     * Soft delete a course.
     *
     * Traceability: [REQ-008]
     */
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Soft delete a course")
    @APIResponse(responseCode = "204", description = "Course soft deleted")
    @APIResponse(responseCode = "404", description = "Course not found")
    @RolesAllowed({"SystemAdmin", "CenterAdmin"})
    public Response deleteCourse(@PathParam("id") String id) {
        logger.info("[INFO] [REQ-008] Soft deleting course ID: {}", id);
        try {
            courseService.softDelete(id);
            logger.debug("[DEBUG] [REQ-008] Course soft deleted ID: {}", id);
            return Response.noContent().build();
        } catch (CourseNotFoundException e) {
            logger.warn("[WARN] [REQ-008] Course not found for deletion: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("[ERROR] [REQ-008] Unexpected error during course deletion. Raw error: {}", e.getMessage(), e);
            throw new RuntimeException("Internal server error while deleting course", e);
        }
    }
}