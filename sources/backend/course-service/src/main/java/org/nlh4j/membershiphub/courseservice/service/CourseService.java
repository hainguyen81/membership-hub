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