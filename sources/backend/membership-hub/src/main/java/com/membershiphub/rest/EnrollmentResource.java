package org.nlh4j.saas.membership-hub.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * EnrollmentResource handles student course enrollment operations:
 * - Browsing available courses (excluding those already enrolled).
 * - Enrolling a student in a course, with automatic Student account creation if missing.
 * <p>
 * Traceability Tags: [REQ-010], [REQ-011]
 */
@RestController
@RequestMapping("/api/v1/enrollments")
@Validated
public class EnrollmentResource {

    // Enterprise‑level constants – hoisted to the crown for anti‑magic‑numbers compliance
    public static final String LOG_PREFIX = "[ENROLLMENT_RESOURCE]";
    public static final String MSG_AVAILABLE_COURSES_FETCHED = "Available courses fetched successfully for student {}";
    public static final String MSG_ENROLLMENT_SUCCESS = "Enrollment completed successfully for student {} in course {}";
    public static final String MSG_STUDENT_ACCOUNT_CREATED = "Student account auto‑created for email {}";
    public static final String ERR_CODE_COURSE_NOT_FOUND = "COURSE_NOT_FOUND";
    public static final String ERR_CODE_STUDENT_NOT_FOUND = "STUDENT_NOT_FOUND";
    public static final String ERR_CODE_ALREADY_ENROLLED = "ALREADY_ENROLLED";
    public static final String ERR_CODE_IDEMPOTENCY_VIOLATION = "IDEMPOTENCY_VIOLATION";
    public static final String ERR_CODE_INVALID_INPUT = "INVALID_INPUT";
    // In‑memory store for idempotency keys – in production replace with Redis or similar
    public static final Set<String> IDEMPOTENCY_KEYS = ConcurrentHashMap.newKeySet();

    private final Logger logger = LoggerFactory.getLogger(EnrollmentResource.class);

    private final EnrollmentService enrollmentService;

    /**
     * Constructor‑based dependency injection – preserves SOLID Single Responsibility Principle.
     */
    public EnrollmentResource(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    /**
     * GET endpoint to browse courses that a student can still enroll in.
     * <p>
     * Traceability Tags: [REQ-010]
     */
    @GetMapping("/available")
    public ResponseEntity<List<CourseDTO>> getAvailableCourses(
            @RequestParam @NotNull UUID studentId) {

        logger.info("{} [ENTRY] Fetching available courses for studentId: {}", LOG_PREFIX, studentId);

        try {
            List<CourseDTO> available = enrollmentService.getAvailableCourses(studentId);
            logger.info(MSG_AVAILABLE_COURSES_FETCHED, studentId);
            logger.info("{} [EXIT] Returning {} available courses for studentId: {}", LOG_PREFIX, available.size(), studentId);
            return ResponseEntity.ok(available);
        } catch (Exception e) {
            // Comprehensive exception logging per enterprise audit law
            logger.error("[CRITICAL FAIL] [REQ-010] Failed to fetch available courses for studentId: {}. Raw error: {}", studentId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to retrieve available courses", e);
        }
    }

    /**
     * POST endpoint to enroll a student in a course.
     * <p>
     * Traceability Tags: [REQ-011]
     */
    @PostMapping
    @Transactional
    public ResponseEntity<EnrollmentResponse> enrollStudent(
            @RequestHeader(value = "Idempotency-Key", required = true) String idempotencyKey,
            @Valid @RequestBody EnrollmentRequest request) {

        logger.info("{} [ENTRY] Processing enrollment request – studentId: {}, courseId: {}", LOG_PREFIX, request.getStudentId(), request.getCourseId());

        // Idempotency validation – prevents duplicate execution of the same request
        if (!IDEMPOTENCY_KEYS.add(idempotencyKey)) {
            logger.warn("{} [WARN] Idempotency violation detected for key: {}", LOG_PREFIX, idempotencyKey);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format("Request with Idempotency-Key %s has already been processed.", idempotencyKey));
        }

        try {
            // Delegate business logic to the service layer
            EnrollmentResponse response = enrollmentService.enrollStudent(request.getStudentId(), request.getCourseId());

            logger.info(MSG_ENROLLMENT_SUCCESS, request.getStudentId(), request.getCourseId());
            logger.info("{} [EXIT] Enrollment completed – enrollmentId: {}", LOG_PREFIX, response.getEnrollmentId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException iae) {
            // Business‑logic violations – map to appropriate HTTP status
            logger.error("[CRITICAL FAIL] [REQ-011] Business rule violation during enrollment – studentId: {}, courseId: {}. Raw error: {}", request.getStudentId(), request.getCourseId(), iae.getMessage(), iae);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, iae.getMessage(), iae);
        } catch (Exception e) {
            // Catch‑all for unexpected failures – full audit trail
            logger.error("[CRITICAL FAIL] [REQ-011] Unexpected error during enrollment – studentId: {}, courseId: {}. Raw error: {}", request.getStudentId(), request.getCourseId(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Enrollment processing failed", e);
        }
    }

    /**
     * Simple data transfer object for enrollment request payload.
     */
    public static class EnrollmentRequest {
        @NotNull(message = "Student ID is required")
        private UUID studentId;
        @NotNull(message = "Course ID is required")
        private UUID courseId;

        // Getters & Setters (omitted for brevity – standard auto‑generation)
        public UUID getStudentId() { return studentId; }
        public void setStudentId(UUID studentId) { this.studentId = studentId; }
        public UUID getCourseId() { return courseId; }
        public void setCourseId(UUID courseId) { this.courseId = courseId; }
    }

    /**
     * Data transfer object for enrollment response.
     */
    public static class EnrollmentResponse {
        private UUID enrollmentId;
        private UUID studentId;
        private UUID courseId;
        private Instant enrolledAt;

        // Getters & Setters
        public UUID getEnrollmentId() { return enrollmentId; }
        public void setEnrollmentId(UUID enrollmentId) { this.enrollmentId = enrollmentId; }
        public UUID getStudentId() { return studentId; }
        public void setStudentId(UUID studentId) { this.studentId = studentId; }
        public UUID getCourseId() { return courseId; }
        public void setCourseId(UUID courseId) { this.courseId = courseId; }
        public Instant getEnrolledAt() { return enrolledAt; }
        public void setEnrolledAt(Instant enrolledAt) { this.enrolledAt = enrolledAt; }
    }

    /**
     * Data transfer object for course information exposed to students.
     */
    public static class CourseDTO {
        private UUID courseId;
        private String title;
        private String description;
        private Integer maxStudents;
        private Integer enrolledCount;

        // Getters & Setters
        public UUID getCourseId() { return courseId; }
        public void setCourseId(UUID courseId) { this.courseId = courseId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Integer getMaxStudents() { return maxStudents; }
        public void setMaxStudents(Integer maxStudents) { this.maxStudents = maxStudents; }
        public Integer getEnrolledCount() { return enrolledCount; }
        public void setEnrolledCount(Integer enrolledCount) { this.enrolledCount = enrolledCount; }
    }

    /**
     * Service interface defining the core enrollment business operations.
     */
    interface EnrollmentService {
        List<CourseDTO> getAvailableCourses(UUID studentId);
        EnrollmentResponse enrollStudent(UUID studentId, UUID courseId);
    }

    /**
     * Concrete implementation of {@link EnrollmentService}.
     * <p>
     * This class encapsulates all data‑access and business‑rule logic, keeping the REST layer thin.
     */
    @Service
    static class EnrollmentServiceImpl implements EnrollmentService {

        private final Logger logger = LoggerFactory.getLogger(EnrollmentServiceImpl.class);

        // In‑memory “databases” for demonstration – replace with real JPA repositories in production
        private final java.util.Map<UUID, Student> students = new java.util.concurrent.ConcurrentHashMap<>();
        private final java.util.Map<UUID, Course> courses = new java.util.concurrent.ConcurrentHashMap<>();
        private final java.util.Map<UUID, Enrollment> enrollments = new java.util.concurrent.ConcurrentHashMap<>();

        public EnrollmentServiceImpl() {
            // Seed sample data – not part of production code
            Student s = new Student();
            s.setUserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
            s.setEmail("student@example.com");
            s.setRole("Student");
            students.put(s.getUserId(), s);

            Course c = new Course();
            c.setCourseId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
            c.setTitle("Introduction to Spring Boot");
            c.setDescription("A comprehensive beginner course.");
            c.setMaxStudents(30);
            c.setEnrolledCount(0);
            courses.put(c.getCourseId(), c);
        }

        @Override
        public List<CourseDTO> getAvailableCourses(UUID studentId) {
            logger.info("[ENTRY] EnrollmentService – fetching available courses for studentId: {}", studentId);
            if (!students.containsKey(studentId)) {
                throw new IllegalArgumentException("Student not found");
            }

            List<CourseDTO> available = courses.values().stream()
                    .filter(course -> !enrollments.containsKey(new EnrollmentKey(studentId, course.getCourseId())))
                    .map(this::toCourseDTO)
                    .collect(Collectors.toList());

            logger.info("[EXIT] EnrollmentService – {} available courses for studentId: {}", available.size(), studentId);
            return available;
        }

        @Override
        public EnrollmentResponse enrollStudent(UUID studentId, UUID courseId) {
            logger.info("[ENTRY] EnrollmentService – enrolling studentId: {} into courseId: {}", studentId, courseId);

            // Auto‑create Student if missing – business rule from REQ‑011
            Student student = students.computeIfAbsent(studentId, k -> {
                Student newStudent = new Student();
                newStudent.setUserId(k);
                newStudent.setEmail("autocreated_" + k + "@example.com");
                newStudent.setRole("Student");
                logger.info("[INFO] Student account auto‑created for email: {}", newStudent.getEmail());
                return newStudent;
            });

            Course course = courses.get(courseId);
            if (course == null) {
                throw new IllegalArgumentException("Course not found");
            }

            EnrollmentKey key = new EnrollmentKey(studentId, courseId);
            if (enrollments.containsKey(key)) {
                throw new IllegalArgumentException("Student already enrolled in this course");
            }

            // Create enrollment record
            Enrollment enrollment = new Enrollment();
            enrollment.setEnrollmentId(UUID.randomUUID());
            enrollment.setStudentId(studentId);
            enrollment.setCourseId(courseId);
            enrollment.setEnrolledAt(Instant.now());
            enrollments.put(key, enrollment);

            // Update course enrollment count
            course.setEnrolledCount(course.getEnrolledCount() + 1);

            EnrollmentResponse response = new EnrollmentResponse();
            response.setEnrollmentId(enrollment.getEnrollmentId());
            response.setStudentId(studentId);
            response.setCourseId(courseId);
            response.setEnrolledAt(enrollment.getEnrolledAt());

            logger.info("[EXIT] EnrollmentService – enrollment completed for enrollmentId: {}", enrollment.getEnrollmentId());
            return response;
        }

        private CourseDTO toCourseDTO(Course course) {
            CourseDTO dto = new CourseDTO();
            dto.setCourseId(course.getCourseId());
            dto.setTitle(course.getTitle());
            dto.setDescription(course.getDescription());
            dto.setMaxStudents(course.getMaxStudents());
            dto.setEnrolledCount(course.getEnrolledCount());
            return dto;
        }

        /**
         * Simple POJO representing a student – replace with JPA entity in real implementation.
         */
        static class Student {
            private UUID userId;
            private String email;
            private String role;

            public UUID getUserId() { return userId; }
            public void setUserId(UUID userId) { this.userId = userId; }
            public String getEmail() { return email; }
            public void setEmail(String email) { this.email = email; }
            public String getRole() { return role; }
            public void setRole(String role) { this.role = role; }
        }

        /**
         * Simple POJO representing a course – replace with JPA entity in real implementation.
         */
        static class Course {
            private UUID courseId;
            private String title;
            private String description;
            private Integer maxStudents;
            private Integer enrolledCount;

            public UUID getCourseId() { return courseId; }
            public void setCourseId(UUID courseId) { this.courseId = courseId; }
            public String getTitle() { return title; }
            public void setTitle(String title) { this.title = title; }
            public String getDescription() { return description; }
            public void setDescription(String description) { this.description = description; }
            public Integer getMaxStudents() { return maxStudents; }
            public void setMaxStudents(Integer maxStudents) { this.maxStudents = maxStudents; }
            public Integer getEnrolledCount() { return enrolledCount; }
            public void setEnrolledCount(Integer enrolledCount) { this.enrolledCount = enrolledCount; }
        }

        /**
         * Composite key for enrollment – used for in‑memory storage demonstration.
         */
        static class EnrollmentKey {
            private final UUID studentId;
            private final UUID courseId;

            public EnrollmentKey(UUID studentId, UUID courseId) {
                this.studentId = studentId;
                this.courseId = courseId;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                EnrollmentKey that = (EnrollmentKey) o;
                return studentId.equals(that.studentId) && courseId.equals(that.courseId);
            }

            @Override
            public int hashCode() {
                return 31 * studentId.hashCode() + courseId.hashCode();
            }
        }

        /**
         * Simple POJO representing an enrollment record – replace with JPA entity in real implementation.
         */
        static class Enrollment {
            private UUID enrollmentId;
            private UUID studentId;
            private UUID courseId;
            private Instant enrolledAt;

            public UUID getEnrollmentId() { return enrollmentId; }
            public void setEnrollmentId(UUID enrollmentId) { this.enrollmentId = enrollmentId; }
            public UUID getStudentId() { return studentId; }
            public void setStudentId(UUID studentId) { this.studentId = studentId; }
            public UUID getCourseId() { return courseId; }
            public void setCourseId(UUID courseId) { this.courseId = courseId; }
            public Instant getEnrolledAt() { return enrolledAt; }
            public void setEnrolledAt(Instant enrolledAt) { this.enrolledAt = enrolledAt; }
        }
    }
}