package org.nlh4j.membershiphub.courseservice.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.nlh4j.membershiphub.courseservice.dto.AvailableCourseResponse;
import org.nlh4j.membershiphub.courseservice.service.CourseBrowseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Controller providing REST API endpoints for students to browse eligible courses.
 * Enforces strict role-based access control, requiring the caller to hold the 'Student' role.
 * Queries exclude courses for which the authenticated student has already enrolled.
 *
 * @author Enterprise Architecture Core Engine
 * @version 1.0.0
 * @traceability [REQ-010]
 */
@Path(StudentCourseBrowseController.BASE_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Tag(name = "Student Course Browsing", description = "Endpoints for students to discover available courses")
public class StudentCourseBrowseController {

    // [REQ-010] Top-of-Class Constants Declaration
    public static final String BASE_PATH = "/api/v1/students/courses";
    public static final String AVAILABLE_COURSES_PATH = "/available";
    public static final String ROLE_STUDENT = "Student";
    public static final String LOG_ENTRY_PREFIX = "[REQ-010] [ENTRY] Requesting available courses for student identifier: {}";
    public static final String LOG_SUCCESS_PREFIX = "[REQ-010] [EXIT] Successfully returned {} available courses for student identifier: {}";
    public static final String LOG_ANONYMOUS_PRINCIPAL = "ANONYMOUS";
    public static final String ERR_MISSING_PRINCIPAL = "Authenticated security principal must not be null.";
    public static final String ERR_INVALID_UUID = "Security principal does not match valid UUID format: ";

    // Standardized enterprise logging engine instantiation
    private static final Logger LOGGER = LoggerFactory.getLogger(StudentCourseBrowseController.class);

    // [REQ-010] Service injection to execute high-performance query logic
    @Inject
    CourseBrowseService courseBrowseService;

    /**
     * Retrieves a curated list of courses available for enrollment by the authenticated student.
     * Evaluates the student's identity extracted securely from the JWT SecurityContext,
     * filtering out courses with pre-existing enrollments via optimized database query.
     *
     * @param securityContext The Jakarta SecurityContext housing the authenticated student's JWT claims.
     * @return HTTP 200 OK with a collection of {@link AvailableCourseResponse} elements,
     *         or standard error representations if validation fails.
     * @traceability [REQ-010]
     */
    @GET
    @Path(AVAILABLE_COURSES_PATH)
    @RolesAllowed(ROLE_STUDENT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Browse Available Courses",
            description = "Fetches active courses that the calling student has not enrolled in yet, respecting center affiliation."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "List of available courses retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = AvailableCourseResponse.class, type = org.eclipse.microprofile.openapi.annotations.enums.SchemaType.ARRAY)
                    )
            ),
            @APIResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or expired JWT credentials"
            ),
            @APIResponse(
                    responseCode = "403",
                    description = "Forbidden - Caller does not possess the required 'Student' role"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Internal Server Error - Failure while compiling available courses"
            )
    })
    public Response getAvailableCourses(@Context @NotNull SecurityContext securityContext) {
        // [REQ-010] Extract security principal from injected security context
        final Principal principal = securityContext.getUserPrincipal();
        final String rawPrincipalName = (principal != null) ? principal.getName() : LOG_ANONYMOUS_PRINCIPAL;

        // Structured process logging at boundary entry point
        LOGGER.info(LOG_ENTRY_PREFIX, rawPrincipalName);

        // Fail-fast security validation: principal must be authenticated and valid
        if (principal == null || rawPrincipalName.isBlank() || LOG_ANONYMOUS_PRINCIPAL.equals(rawPrincipalName)) {
            LOGGER.error("[CRITICAL FAIL] [REQ-010] Security context missing valid principal. Raw token identifier absent.");
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Collections.singletonMap("errorMessage", ERR_MISSING_PRINCIPAL))
                    .build();
        }

        final UUID studentId;
        try {
            // [REQ-010] Parse student unique UUID from principal subject
            studentId = UUID.fromString(rawPrincipalName);
        } catch (IllegalArgumentException ex) {
            // Preserve security audit trail without raw stack leaks
            LOGGER.error("[CRITICAL FAIL] [REQ-010] Malformed student UUID token detected: {}. Raw error: {}",
                    rawPrincipalName, ex.getMessage(), ex);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Collections.singletonMap("errorMessage", ERR_INVALID_UUID + rawPrincipalName))
                    .build();
        }

        try {
            // [REQ-010] Delegate retrieval to the underlying high-performance service layer
            final List<AvailableCourseResponse> availableCourses = courseBrowseService.findAvailableCoursesForStudent(studentId);

            // Structured process logging at boundary exit point
            LOGGER.info(LOG_SUCCESS_PREFIX, availableCourses.size(), studentId);

            // Emit HTTP 200 payload containing the curated available course listings
            return Response.ok(availableCourses).build();

        } catch (Exception ex) {
            // [REQ-010] Intercept and record internal business logic or database access failures
            LOGGER.error("[CRITICAL FAIL] [REQ-010] Error occurred while querying available courses for student ID: {}. Raw error: {}",
                    studentId, ex.getMessage(), ex);

            // Re-throw wrapped or bubble up enterprise failure without exposing raw internal internals
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Collections.singletonMap("errorMessage", "An error occurred while compiling available courses."))
                    .build();
        }
    }
}