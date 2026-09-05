# Day 2: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/CourseController.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/CourseController.java`
*   Traceability Audit Tags For This Task: ['[REQ-007]', '[REQ-008]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo tệp mã nguồn ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/CourseController.java hiện thực hóa lớp CourseController với annotation @Path("/api/v1/courses"). Triển khai các endpoint: (1) GET /api/v1/courses với @QueryParam cho page, size, sort mặc định size=20, sort=startDate,asc, trả về danh sách phân trang gồm courseId, title, startDate, endDate, teacherName thông qua Page<CourseResponse>; (2) POST /api/v1/courses nhận CourseCreateRequest với @Valid và Bean Validation (@NotNull title, @Size(max=150) title, @NotNull startDate, @NotNull endDate, @NotNull teacherId, @NotNull centerId), gọi CourseService.create(); (3) PUT /api/v1/courses/{id} cập nhật; (4) DELETE /api/v1/courses/{id} xoá mềm. Áp dụng @RolesAllowed({"SystemAdmin","CenterAdmin"}) cho POST/PUT/DELETE. Sử dụng JPQL parameter binding trong CourseService để tránh SQL injection theo OWASP A03. Hợp đồng API: GET /api/v1/courses?page=0&size=20&sort=startDate,asc với response 200 chứa content, totalElements, totalPages, page, size.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-007]', '[REQ-008]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
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

# Day 2: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/CourseService.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/CourseService.java`
*   Traceability Audit Tags For This Task: ['[REQ-007]', '[REQ-008]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo tệp mã nguồn ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/CourseController.java hiện thực hóa lớp CourseController với annotation @Path("/api/v1/courses"). Triển khai các endpoint: (1) GET /api/v1/courses với @QueryParam cho page, size, sort mặc định size=20, sort=startDate,asc, trả về danh sách phân trang gồm courseId, title, startDate, endDate, teacherName thông qua Page<CourseResponse>; (2) POST /api/v1/courses nhận CourseCreateRequest với @Valid và Bean Validation (@NotNull title, @Size(max=150) title, @NotNull startDate, @NotNull endDate, @NotNull teacherId, @NotNull centerId), gọi CourseService.create(); (3) PUT /api/v1/courses/{id} cập nhật; (4) DELETE /api/v1/courses/{id} xoá mềm. Áp dụng @RolesAllowed({"SystemAdmin","CenterAdmin"}) cho POST/PUT/DELETE. Sử dụng JPQL parameter binding trong CourseService để tránh SQL injection theo OWASP A03. Hợp đồng API: GET /api/v1/courses?page=0&size=20&sort=startDate,asc với response 200 chứa content, totalElements, totalPages, page, size.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-007]', '[REQ-008]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
/**
 * Service layer for managing courses.
 * Implements core business logic for course CRUD operations, pagination, sorting,
 * and schedule conflict validation to ensure data integrity and security.
 *
 * @traceability [REQ-007], [REQ-008]
 */
package org.nlh4j.membershiphub.courseservice.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.exception.ConstraintViolationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import org.nlh4j.membershiphub.courseservice.repository.CourseRepository;
import org.nlh4j.membershiphub.courseservice.dto.CourseResponse;
import org.nlh4j.membershiphub.courseservice.dto.CourseCreateRequest;
import org.nlh4j.membershiphub.courseservice.dto.CourseUpdateRequest;
import org.nlh4j.membershiphub.courseservice.exception.CourseNotFoundException;
import org.nlh4j.membershiphub.courseservice.exception.ScheduleConflictException;
import org.nlh4j.membershiphub.courseservice.exception.CenterNotFoundException;
import org.nlh4j.membershiphub.courseservice.exception.TeacherNotFoundException;
import org.nlh4j.membershiphub.courseservice.model.Course;
import org.nlh4j.membershiphub.courseservice.model.Center;
import org.nlh4j.membershiphub.courseservice.model.User;
import org.nlh4j.membershiphub.courseservice.model.CourseTeacherMapping;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * CourseService provides transactional, secure, and auditable operations for course management.
 * All public methods are wrapped in @Transactional to ensure atomicity.
 * Logging and traceability tags are injected for enterprise audit compliance.
 *
 * @traceability [REQ-007], [REQ-008]
 */
@ApplicationScoped
@Transactional
public class CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);

    /* Enterprise Constants – Anti‑Magic‑Numbers Guardrail */
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final String DEFAULT_SORT_FIELD = "startDate";
    public static final String DEFAULT_SORT_DIRECTION = "asc";

    private final CourseRepository courseRepository;
    private final EntityManager entityManager;

    /**
     * Constructor‑based dependency injection for CourseRepository and EntityManager.
     *
     * @param courseRepository Repository for Course entity operations.
     * @param entityManager   JPA EntityManager for native queries and criteria API.
     *
     * @traceability [REQ-007], [REQ-008]
     */
    @Inject
    public CourseService(CourseRepository courseRepository, EntityManager entityManager) {
        this.courseRepository = courseRepository;
        this.entityManager = entityManager;
        logger.info("[COURSE-SERVICE] CourseService initialized – traceability tags: [REQ-007], [REQ-008]");
    }

    /**
     * Retrieves a paginated and sorted list of courses.
     *
     * @param page       Zero‑based page number.
     * @param size       Number of records per page (defaults to {@link #DEFAULT_PAGE_SIZE}).
     * @param sortBy     Field to sort by (defaults to {@link #DEFAULT_SORT_FIELD}).
     * @param sortDir    Sort direction – "asc" or "desc" (defaults to {@link #DEFAULT_SORT_DIRECTION}).
     * @return Page of CourseResponse objects.
     *
     * @traceability [REQ-007]
     */
    public Page<CourseResponse> listCourses(int page, int size, String sortBy, String sortDir) {
        logger.info("[COURSE-SERVICE] [REQ-007] Entry listCourses – page={}, size={}, sortBy={}, sortDir={}", page, size, sortBy, sortDir);
        // Apply defaults and sanitize inputs to prevent injection
        int pageNum = Math.max(0, page);
        int pageSize = (size > 0) ? size : DEFAULT_PAGE_SIZE;
        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : DEFAULT_SORT_FIELD;
        String sortDirection = (sortDir != null && !sortDir.isBlank()) ? sortDir.toLowerCase() : DEFAULT_SORT_DIRECTION;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Course> cq = cb.createQuery(Course.class);
        Root<Course> root = cq.from(Course.class);

        // Apply sorting only on whitelisted fields to mitigate injection risks
        List<String> allowedSortFields = List.of("id", "title", "startDate", "endDate", "teacherId", "centerId");
        if (allowedSortFields.contains(sortField)) {
            if ("desc".equals(sortDirection)) {
                cq.orderBy(cb.desc(root.get(sortField)));
            } else {
                cq.orderBy(cb.asc(root.get(sortField)));
            }
        } else {
            cq.orderBy(cb.asc(root.get(DEFAULT_SORT_FIELD)));
        }

        TypedQuery<Course> query = entityManager.createQuery(cq);
        query.setFirstResult(pageNum * pageSize);
        query.setMaxResults(pageSize);

        List<Course> courses = query.getResultList();

        // Convert to DTOs
        List<CourseResponse> content = courses.stream()
                .map(this::mapToResponse)
                .toList();

        // Count total records using a native count query for performance
        Long total = courseRepository.countTotalCourses();
        int totalPages = (int) Math.ceil((double) total / pageSize);

        logger.debug("[COURSE-SERVICE] [REQ-007] Retrieved {} courses (page {} of {})", content.size(), pageNum, totalPages);
        return new Page<>(content, pageNum, pageSize, totalPages, total);
    }

    /**
     * Creates a new course after validating business rules:
     *   - Center and Teacher must exist.
     *   - Teacher must not have overlapping schedule for the requested dates.
     *
     * @param request DTO containing course creation details.
     * @return CourseResponse of the newly created course.
     * @throws CenterNotFoundException If the referenced center does not exist.
     * @throws TeacherNotFoundException If the referenced teacher does not exist.
     * @throws ScheduleConflictException If the teacher already has a course overlapping the requested dates.
     *
     * @traceability [REQ-008]
     */
    public CourseResponse createCourse(@Valid CourseCreateRequest request) {
        logger.info("[COURSE-SERVICE] [REQ-008] Entry createCourse – title={}", request.getTitle());
        // Validate Center existence
        Center center = courseRepository.findCenterById(request.getCenterId())
                .orElseThrow(() -> {
                    logger.error("[COURSE-SERVICE] [REQ-008] Center not found – centerId={}", request.getCenterId());
                    return new CenterNotFoundException("Center not found with ID: " + request.getCenterId());
                });

        // Validate Teacher existence
        User teacher = courseRepository.findUserById(request.getTeacherId())
                .orElseThrow(() -> {
                    logger.error("[COURSE-SERVICE] [REQ-008] Teacher not found – teacherId={}", request.getTeacherId());
                    return new TeacherNotFoundException("Teacher not found with ID: " + request.getTeacherId());
                });

        // Check for schedule overlap using native query to avoid N+1 selects
        boolean hasOverlap = courseRepository.hasTeacherScheduleOverlap(
                request.getTeacherId(),
                request.getStartDate(),
                request.getEndDate(),
                null // exclude newly created course (id null)
        );

        if (hasOverlap) {
            logger.warn("[COURSE-SERVICE] [REQ-008] Schedule conflict detected for teacherId={} dates={}–{}", request.getTeacherId(), request.getStartDate(), request.getEndDate());
            throw new ScheduleConflictException("Teacher already has a course scheduled during the requested period.");
        }

        // Build Course entity
        Course course = new Course();
        course.setId(UUID.randomUUID());
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setStartDate(request.getStartDate());
        course.setEndDate(request.getEndDate());
        course.setTeacherId(request.getTeacherId());
        course.setCenterId(request.getCenterId());
        course.setMaxStudents(request.getMaxStudents() != null ? request.getMaxStudents() : 30);
        course.setCreatedAt(java.time.LocalDateTime.now());
        course.setUpdatedAt(java.time.LocalDateTime.now());

        // Persist course and create mapping in a single transaction
        Course saved = courseRepository.save(course);

        // Create CourseTeacherMapping entry
        CourseTeacherMapping mapping = new CourseTeacherMapping();
        mapping.setMappingId(UUID.randomUUID());
        mapping.setCourseId(saved.getId());
        mapping.setTeacherId(request.getTeacherId());
        mapping.setAssignedAt(java.time.LocalDateTime.now());
        courseRepository.saveCourseTeacherMapping(mapping);

        logger.info("[COURSE-SERVICE] [REQ-008] Course created successfully – courseId={}", saved.getId());
        return mapToResponse(saved);
    }

    /**
     * Updates an existing course.
     *
     * @param id     Course UUID to update.
     * @param request DTO with updated fields.
     * @return Updated CourseResponse.
     * @throws CourseNotFoundException If the course does not exist.
     * @throws ScheduleConflictException If teacher schedule conflict occurs.
     *
     * @traceability [REQ-008]
     */
    public CourseResponse updateCourse(UUID id, @Valid CourseUpdateRequest request) {
        logger.info("[COURSE-SERVICE] [REQ-008] Entry updateCourse – courseId={}", id);
        Course existing = courseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("[COURSE-SERVICE] [REQ-008] Course not found – courseId={}", id);
                    return new CourseNotFoundException("Course not found with ID: " + id);
                });

        // If teacher or dates changed, re-validate schedule overlap
        boolean teacherChanged = !existing.getTeacherId().equals(request.getTeacherId());
        boolean datesChanged = !existing.getStartDate().equals(request.getStartDate()) ||
                !existing.getEndDate().equals(request.getEndDate());

        if (teacherChanged || datesChanged) {
            boolean hasOverlap = courseRepository.hasTeacherScheduleOverlap(
                    request.getTeacherId(),
                    request.getStartDate(),
                    request.getEndDate(),
                    id // exclude current course from overlap check
            );
            if (hasOverlap) {
                logger.warn("[COURSE-SERVICE] [REQ-008] Schedule conflict on update – teacherId={}, dates={}–{}", request.getTeacherId(), request.getStartDate(), request.getEndDate());
                throw new ScheduleConflictException("Teacher schedule conflict detected for updated course.");
            }
        }

        // Apply updates
        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());
        existing.setTeacherId(request.getTeacherId());
        existing.setCenterId(request.getCenterId());
        existing.setMaxStudents(request.getMaxStudents());
        existing.setUpdatedAt(java.time.LocalDateTime.now());

        Course updated = courseRepository.save(existing);

        // Update mapping if teacher changed
        if (teacherChanged) {
            courseRepository.deleteCourseTeacherMappingByCourseId(id);
            CourseTeacherMapping mapping = new CourseTeacherMapping();
            mapping.setMappingId(UUID.randomUUID());
            mapping.setCourseId(id);
            mapping.setTeacherId(request.getTeacherId());
            mapping.setAssignedAt(java.time.LocalDateTime.now());
            courseRepository.saveCourseTeacherMapping(mapping);
        }

        logger.info("[COURSE-SERVICE] [REQ-008] Course updated – courseId={}", id);
        return mapToResponse(updated);
    }

    /**
     * Soft deletes a course by marking it as inactive.
     *
     * @param id Course UUID to delete.
     * @throws CourseNotFoundException If the course does not exist.
     *
     * @traceability [REQ-008]
     */
    public void deleteCourse(UUID id) {
        logger.info("[COURSE-SERVICE] [REQ-008] Entry deleteCourse – courseId={}", id);
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("[COURSE-SERVICE] [REQ-008] Course not found – courseId={}", id);
                    return new CourseNotFoundException("Course not found with ID: " + id);
                });

        // Soft delete flag (assuming Course entity has an 'active' field)
        course.setActive(false);
        course.setUpdatedAt(java.time.LocalDateTime.now());
        courseRepository.save(course);

        // Remove associated teacher mapping
        courseRepository.deleteCourseTeacherMappingByCourseId(id);

        logger.info("[COURSE-SERVICE] [REQ-008] Course soft-deleted – courseId={}", id);
    }

    /**
     * Helper method to map Course entity to CourseResponse DTO.
     *
     * @param course Course entity.
     * @return Populated CourseResponse.
     */
    private CourseResponse mapToResponse(Course course) {
        CourseResponse dto = new CourseResponse();
        dto.setCourseId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setStartDate(course.getStartDate());
        dto.setEndDate(course.getEndDate());
        // Fetch teacher name via repository (could be optimized with join)
        String teacherName = courseRepository.findUserNameById(course.getTeacherId()).orElse("Unknown");
        dto.setTeacherName(teacherName);
        dto.setMaxStudents(course.getMaxStudents());
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());
        return dto;
    }

    /**
     * Simple Page wrapper for pagination results.
     *
     * @param <T> Type of content.
     */
    public static class Page<T> {
        private final List<T> content;
        private final int number;
        private final int size;
        private final int totalPages;
        private final long totalElements;

        public Page(List<T> content, int number, int size, int totalPages, long totalElements) {
            this.content = content;
            this.number = number;
            this.size = size;
            this.totalPages = totalPages;
            this.totalElements = totalElements;
        }

        public List<T> getContent() { return content; }
        public int getNumber() { return number; }
        public int getSize() { return size; }
        public int getTotalPages() { return totalPages; }
        public long getTotalElements() { return totalElements; }
    }
}
```

# Day 2: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/dto/CourseResponse.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/dto/CourseResponse.java`
*   Traceability Audit Tags For This Task: ['[REQ-007]', '[REQ-008]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo tệp mã nguồn ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/CourseController.java hiện thực hóa lớp CourseController với annotation @Path("/api/v1/courses"). Triển khai các endpoint: (1) GET /api/v1/courses với @QueryParam cho page, size, sort mặc định size=20, sort=startDate,asc, trả về danh sách phân trang gồm courseId, title, startDate, endDate, teacherName thông qua Page<CourseResponse>; (2) POST /api/v1/courses nhận CourseCreateRequest với @Valid và Bean Validation (@NotNull title, @Size(max=150) title, @NotNull startDate, @NotNull endDate, @NotNull teacherId, @NotNull centerId), gọi CourseService.create(); (3) PUT /api/v1/courses/{id} cập nhật; (4) DELETE /api/v1/courses/{id} xoá mềm. Áp dụng @RolesAllowed({"SystemAdmin","CenterAdmin"}) cho POST/PUT/DELETE. Sử dụng JPQL parameter binding trong CourseService để tránh SQL injection theo OWASP A03. Hợp đồng API: GET /api/v1/courses?page=0&size=20&sort=startDate,asc với response 200 chứa content, totalElements, totalPages, page, size.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-007]', '[REQ-008]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.membershiphub.courseservice.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * CourseResponse DTO representing a course summary returned by the Course API.
 * Traceability: [REQ-007], [REQ-008]
 */
public class CourseResponse {
    /**
     * Unique identifier of the course.
     * Traceability: [REQ-007], [REQ-008]
     */
    private UUID courseId;

    /**
     * Title of the course (max 150 characters).
     * Traceability: [REQ-007], [REQ-008]
     */
    private String title;

    /**
     * Start date of the course.
     * Traceability: [REQ-007], [REQ-008]
     */
    private LocalDate startDate;

    /**
     * End date of the course.
     * Traceability: [REQ-007], [REQ-008]
     */
    private LocalDate endDate;

    /**
     * Name of the teacher assigned to the course.
     * Traceability: [REQ-007], [REQ-008]
     */
    private String teacherName;

    // Default constructor for frameworks
    public CourseResponse() {
    }

    /**
     * Full constructor for creating immutable response.
     * Traceability: [REQ-007], [REQ-008]
     */
    public CourseResponse(UUID courseId, String title, LocalDate startDate, LocalDate endDate, String teacherName) {
        this.courseId = courseId;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.teacherName = teacherName;
    }

    // Getters and setters
    public UUID getCourseId() {
        return courseId;
    }

    public void setCourseId(UUID courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
}
```

# Day 2: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/dto/CourseCreateRequest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/dto/CourseCreateRequest.java`
*   Traceability Audit Tags For This Task: ['[REQ-007]', '[REQ-008]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo tệp mã nguồn ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/CourseController.java hiện thực hóa lớp CourseController với annotation @Path("/api/v1/courses"). Triển khai các endpoint: (1) GET /api/v1/courses với @QueryParam cho page, size, sort mặc định size=20, sort=startDate,asc, trả về danh sách phân trang gồm courseId, title, startDate, endDate, teacherName thông qua Page<CourseResponse>; (2) POST /api/v1/courses nhận CourseCreateRequest với @Valid và Bean Validation (@NotNull title, @Size(max=150) title, @NotNull startDate, @NotNull endDate, @NotNull teacherId, @NotNull centerId), gọi CourseService.create(); (3) PUT /api/v1/courses/{id} cập nhật; (4) DELETE /api/v1/courses/{id} xoá mềm. Áp dụng @RolesAllowed({"SystemAdmin","CenterAdmin"}) cho POST/PUT/DELETE. Sử dụng JPQL parameter binding trong CourseService để tránh SQL injection theo OWASP A03. Hợp đồng API: GET /api/v1/courses?page=0&size=20&sort=startDate,asc với response 200 chứa content, totalElements, totalPages, page, size.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-007]', '[REQ-008]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
/**
 * 📄 CourseCreateRequest.java
 * 📂 Đường dẫn: ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/dto/CourseCreateRequest.java
 * 🏷️ Thẻ Truy Vết: [REQ-007], [REQ-008]
 * 📝 Mô Tả: DTO yêu cầu tạo mới khóa học, chứa các trường bắt buộc cho việc tạo khóa học mới.
 * 🔒 Bảo Mật: Tất cả các trường đều được xác thực nghiêm ngặt theo Jakarta Bean Validation để ngăn chặn SQL Injection và đảm bảo tính toàn vẹn dữ liệu.
 * 📊 Quy Tắc Kiến Trúc: Tuân thủ quy tắc đặt tên gói org.nlh4j.membershiphub.courseservice.dto, tuân thủ SOLID và OWASP A03.
 */
package org.nlh4j.membershiphub.courseservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 📦 Lớp DTO này được sử dụng trong {@link org.nlh4j.membershiphub.courseservice.controller.CourseController}
 * để nhận payload yêu cầu tạo khóa học từ client.
 *
 * @author Enterprise Backend Team
 * @version 1.0
 * @since 2024-08-29
 */
public class CourseCreateRequest {

    /**
     * 🏷️ [REQ-007] 📝 Tiêu đề khóa học.
     * Bắt buộc, không được null, độ dài tối đa 150 ký tự.
     * Được xác thực bằng @Size để ngăn chặn các cuộc tấn công tràn bộ đệm chuỗi.
     */
    @NotNull(message = "Tiêu đề khóa học không được để trống")
    @Size(max = 150, message = "Tiêu đề khóa học không được vượt quá 150 ký tự")
    private String title;

    /**
     * 🏷️ [REQ-008] 📅 Ngày bắt đầu khóa học.
     * Bắt buộc, không được null, tuân thủ định dạng ISO LocalDate.
     * Được xác thực bằng @NotNull để đảm bảo tính hợp lệ của ngày bắt đầu.
     */
    @NotNull(message = "Ngày bắt đầu khóa học không được để trống")
    private LocalDate startDate;

    /**
     * 🏷️ [REQ-008] 📅 Ngày kết thúc khóa học.
     * Bắt buộc, không được null, tuân thủ định dạng ISO LocalDate.
     * Được xác thực bằng @NotNull để đảm bảo tính hợp lệ của ngày kết thúc.
     */
    @NotNull(message = "Ngày kết thúc khóa học không được để trống")
    private LocalDate endDate;

    /**
     * 🏷️ [REQ-008] 👩‍🏫 ID của giáo viên phụ trách khóa học.
     * Bắt buộc, không được null, phải là UUID hợp lệ.
     * Được xác thực bằng @NotNull để đảm bảo giáo viên được chỉ định.
     */
    @NotNull(message = "ID giáo viên không được để trống")
    private UUID teacherId;

    /**
     * 🏷️ [REQ-008] 🏢 ID của trung tâm tổ chức khóa học.
     * Bắt buộc, không được null, phải là UUID hợp lệ.
     * Được xác thực bằng @NotNull để đảm bảo trung tâm được chỉ định.
     */
    @NotNull(message = "ID trung tâm không được để trống")
    private UUID centerId;

    // -------------------------------------------------------------------------
    // 🛠️ Constructors
    // -------------------------------------------------------------------------

    /**
     * 🏗️ Constructor mặc định (bắt buộc cho reflection và frameworks).
     */
    public CourseCreateRequest() {
    }

    /**
     * 🏗️ Constructor đầy đủ tham số để khởi tạo đối tượng nhanh chóng.
     *
     * @param title      Tiêu đề khóa học
     * @param startDate  Ngày bắt đầu khóa học
     * @param endDate    Ngày kết thúc khóa học
     * @param teacherId  ID của giáo viên
     * @param centerId   ID của trung tâm
     */
    public CourseCreateRequest(String title, LocalDate startDate, LocalDate endDate, UUID teacherId, UUID centerId) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.teacherId = teacherId;
        this.centerId = centerId;
    }

    // -------------------------------------------------------------------------
    // 📌 Getter & Setter Methods
    // -------------------------------------------------------------------------

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public UUID getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(UUID teacherId) {
        this.teacherId = teacherId;
    }

    public UUID getCenterId() {
        return centerId;
    }

    public void setCenterId(UUID centerId) {
        this.centerId = centerId;
    }

    // -------------------------------------------------------------------------
    // 🔄 toString, equals, hashCode (optional, for debugging)
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "CourseCreateRequest{" +
                "title='" + title + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", teacherId=" + teacherId +
                ", centerId=" + centerId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseCreateRequest that = (CourseCreateRequest) o;
        return title.equals(that.title) &&
                startDate.equals(that.startDate) &&
                endDate.equals(that.endDate) &&
                teacherId.equals(that.teacherId) &&
                centerId.equals(that.centerId);
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + startDate.hashCode();
        result = 31 * result + endDate.hashCode();
        result = 31 * result + teacherId.hashCode();
        result = 31 * result + centerId.hashCode();
        return result;
    }
}
```

# Day 2: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/repository/CourseRepository.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/repository/CourseRepository.java`
*   Traceability Audit Tags For This Task: ['[REQ-007]', '[REQ-008]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo tệp mã nguồn ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/CourseController.java hiện thực hóa lớp CourseController với annotation @Path("/api/v1/courses"). Triển khai các endpoint: (1) GET /api/v1/courses với @QueryParam cho page, size, sort mặc định size=20, sort=startDate,asc, trả về danh sách phân trang gồm courseId, title, startDate, endDate, teacherName thông qua Page<CourseResponse>; (2) POST /api/v1/courses nhận CourseCreateRequest với @Valid và Bean Validation (@NotNull title, @Size(max=150) title, @NotNull startDate, @NotNull endDate, @NotNull teacherId, @NotNull centerId), gọi CourseService.create(); (3) PUT /api/v1/courses/{id} cập nhật; (4) DELETE /api/v1/courses/{id} xoá mềm. Áp dụng @RolesAllowed({"SystemAdmin","CenterAdmin"}) cho POST/PUT/DELETE. Sử dụng JPQL parameter binding trong CourseService để tránh SQL injection theo OWASP A03. Hợp đồng API: GET /api/v1/courses?page=0&size=20&sort=startDate,asc với response 200 chứa content, totalElements, totalPages, page, size.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-007]', '[REQ-008]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
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
```

# Day 2: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/exception/ScheduleConflictException.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Component Destination Path: `./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/exception/ScheduleConflictException.java`
*   Traceability Audit Tags For This Task: ['[REQ-007]', '[REQ-008]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo tệp mã nguồn ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/CourseController.java hiện thực hóa lớp CourseController với annotation @Path("/api/v1/courses"). Triển khai các endpoint: (1) GET /api/v1/courses với @QueryParam cho page, size, sort mặc định size=20, sort=startDate,asc, trả về danh sách phân trang gồm courseId, title, startDate, endDate, teacherName thông qua Page<CourseResponse>; (2) POST /api/v1/courses nhận CourseCreateRequest với @Valid và Bean Validation (@NotNull title, @Size(max=150) title, @NotNull startDate, @NotNull endDate, @NotNull teacherId, @NotNull centerId), gọi CourseService.create(); (3) PUT /api/v1/courses/{id} cập nhật; (4) DELETE /api/v1/courses/{id} xoá mềm. Áp dụng @RolesAllowed({"SystemAdmin","CenterAdmin"}) cho POST/PUT/DELETE. Sử dụng JPQL parameter binding trong CourseService để tránh SQL injection theo OWASP A03. Hợp đồng API: GET /api/v1/courses?page=0&size=20&sort=startDate,asc với response 200 chứa content, totalElements, totalPages, page, size.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-007]', '[REQ-008]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
// [REQ-007], [REQ-008]
package org.nlh4j.membershiphub.courseservice.exception;

import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

/**
 * 🏢 ENTERPRISE GOVERNANCE & ARCHITECTURE COMPLIANCE
 * 
 * Target Component: ScheduleConflictException.java
 * Subsystem: course-service
 * Package: org.nlh4j.membershiphub.courseservice.exception
 * Traceability Audit Tags: [REQ-007], [REQ-008]
 * 
 * Business Purpose & Logic Context:
 * This enterprise business exception is thrown when a course schedule scheduling operation
 * (creation or update) violates teacher timetable constraints (time overlap conflict).
 * It enforces the exclusion constraint logic and prevents duplicate bookings for teachers
 * within overlapping date ranges as specified by enterprise specifications [REQ-008].
 * 
 * Security Gating & Exception Auditing Enforcement:
 * - Natively inherits enterprise logging frameworks (Slf4j/Logback via JBoss Logger).
 * - Logs process state transitions and captures exception contexts securely.
 * - Prevents cleartext credential exposure and adheres to the Anti-Magic-Numbers law.
 * - Preserves ancestral exception root causes via the cause-chain constructor law.
 * 
 * @author Enterprise Architecture Core Agent
 * @version 1.0.0
 */
public class ScheduleConflictException extends RuntimeException {

    // =========================================================================
    // 🏛️ CONSTANTS DECLARATION LAW (Top-of-Class Constants Isolation)
    // =========================================================================
    
    /** Serial version UID for serialization compatibility compliance. */
    private static final long serialVersionUID = 1L;

    /** System subsystem classification label for centralized aggregation log tracing. */
    private static final String SUBSYSTEM_NAME = "[COURSE_SERVICE_SUBSYSTEM]";

    /** Enterprise standard tracking tag identifier mapping to architectural requirements. */
    private static final String TRACEABILITY_TAGS = "[REQ-007], [REQ-008]";

    /** Default enterprise error classification code for schedule conflict exceptions. */
    public static final String ERROR_CODE = "SCHEDULE_CONFLICT_ERROR";

    /** Standardized enterprise logging handler instance mapped to this subsystem. */
    private static final Logger LOGGER = Logger.getLogger(ScheduleConflictException.class);

    // =========================================================================
    // 📊 INSTANCE FIELD ATTRIBUTES
    // =========================================================================

    /** Machine-readable enterprise error code for downstream API contract parsing. */
    private final String errorCode;

    // =========================================================================
    // 🛠️ CONSTRUCTORS WITH AUDIT LOGGING & CAUSE PRESERVATION
    // =========================================================================

    /**
     * Constructs a new ScheduleConflictException with a designated detail message.
     * Automatically triggers audit-level enterprise logging complying with [0.3].
     *
     * @param message Detailed descriptive explanation of the scheduling conflict.
     */
    public ScheduleConflictException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
        
        // [0.3] Enterprise Logging & Exception Auditing Law: Log exact subsystem, message, and tag ID
        LOGGER.error(String.format("%s [ERROR_CODE: %s] %s Schedule conflict detected: %s",
                SUBSYSTEM_NAME, this.errorCode, TRACEABILITY_TAGS, message));
    }

    /**
     * Constructs a new ScheduleConflictException with a detailed message and preserves
     * the ancestral root-cause exception object to satisfy the Exception Cause Chain Preservation Law.
     *
     * @param message Descriptive context text explaining the failure boundary.
     * @param cause   The original physical caught exception object (root cause).
     */
    public ScheduleConflictException(String message, Throwable cause) {
        // [0.3] Forwarding original physical caught exception object inside wrapper constructor
        super(message, cause);
        this.errorCode = ERROR_CODE;
        
        // [0.3] Enterprise Logging & Exception Auditing Law: Centralized cloud aggregation trace logging
        LOGGER.error(String.format("%s [ERROR_CODE: %s] %s Schedule conflict execution failed with raw error: %s",
                SUBSYSTEM_NAME, this.errorCode, TRACEABILITY_TAGS, 
                cause != null ? cause.getMessage() : "Unknown Root Cause"), cause);
    }

    /**
     * Constructs a new ScheduleConflictException with a specific enterprise error code,
     * detail message, and root cause exception preservation.
     *
     * @param errorCode Custom enterprise error classification code.
     * @param message   Descriptive context text explaining the failure boundary.
     * @param cause     The original physical caught exception object.
     */
    public ScheduleConflictException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode != null ? errorCode : ERROR_CODE;
        
        // [0.3] Enterprise Logging & Exception Auditing Law: Comprehensive exception logging
        LOGGER.error(String.format("%s [ERROR_CODE: %s] %s Custom schedule conflict exception triggered. Raw error: %s",
                SUBSYSTEM_NAME, this.errorCode, TRACEABILITY_TAGS,
                cause != null ? cause.getMessage() : message), cause);
    }

    // =========================================================================
    // 📦 GETTERS & UTILITY MAPPING METHODS
    // =========================================================================

    /**
     * Retrieves the specific machine-readable enterprise error code associated with this exception.
     *
     * @return The immutable error code string.
     */
    public String getErrorCode() {
        return this.errorCode;
    }
}
```

