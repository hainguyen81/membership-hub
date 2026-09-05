```java
package org.nlh4j.membershiphub.courseservice.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * CourseController handles CRUD operations for Course entities.
 * <p>
 * This controller implements the following REST endpoints:
 * <ul>
 *   <li>GET /api/v1/courses – paginated list of courses (supports sorting by startDate)</li>
 *   <li>POST /api/v1/courses – create a new course (requires SystemAdmin or CenterAdmin role)</li>
 *   <li>PUT /api/v1/courses/{id} – update an existing course (requires SystemAdmin or CenterAdmin role)</li>
 *   <li>DELETE /api/v1/courses/{id} – soft delete a course (requires SystemAdmin or CenterAdmin role)</li>
 * </ul>
 *
 * <p>Traceability Tags:</p>
 * <ul>
 *   <li>[REQ-007] – Course listing and pagination</li>
 *   <li>[REQ-008] – Course creation, update, and deletion with validation</li>
 * </ul>
 *
 * <p>Security:</p>
 * <ul>
 *   <li>OWASP A03: Injection – All database queries use Spring Data JPA with parameter binding.</li>
 *   <li>Role-based access control enforced via {@code @RolesAllowed} annotations.</li>
 * </ul>
 *
 * @author System Architect
 * @version 1.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/v1/courses")
@CrossOrigin(origins = "*")
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    private final CourseService courseService;

    /**
     * Constructor-based dependency injection for CourseService.
     *
     * @param courseService the CourseService implementation
     */
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * Retrieve a paginated list of courses.
     * <p>
     * Query parameters:
     * <ul>
     *   <li>page – zero‑based page number (default 0)</li>
     *   <li>size – page size (default 20)</li>
     *   <li>sort – comma‑separated sort fields, e.g. {@code startDate,asc}</li>
     * </ul>
     *
     * @param page the page number
     * @param size the page size
     * @param sort the sort specification
     * @return {@code Page<CourseResponse>} containing the courses for the requested page
     * @throws IllegalArgumentException if sort specification is invalid
     */
    @GetMapping
    public ResponseEntity<Page<CourseResponse>> getCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startDate,asc") String sort) {

        logger.info("[REQ-007] Fetching courses – page={}, size={}, sort={}", page, size, sort);

        try {
            Sort sortSpec = Sort.by(Sort.Direction.fromString(sort.split(",")[1]),
                                   sort.split(",")[0]);
            Pageable pageable = PageRequest.of(page, size, sortSpec);
            Page<CourseResponse> coursesPage = courseService.findAll(pageable);

            logger.debug("[REQ-007] Returning {} courses (total {}), page {} of {}",
                         coursesPage.getNumberOfElements(),
                         coursesPage.getTotalElements(),
                         coursesPage.getNumber());

            return ResponseEntity.ok(coursesPage);
        } catch (IllegalArgumentException ex) {
            logger.warn("[REQ-007] Invalid sort parameter '{}': {}", sort, ex.getMessage());
            throw new IllegalArgumentException("Invalid sort specification: " + sort, ex);
        }
    }

    /**
     * Create a new course.
     * <p>
     * Request body must contain a {@code CourseCreateRequest} with non‑null fields:
     * {@code title} (max 150 chars), {@code startDate}, {@