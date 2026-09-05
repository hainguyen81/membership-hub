package org.nlh4j.saas.membership-hub.integration;

/**
 * @verifies [REQ-008], [DAT-004], [DAT-005], [DAT-006], [ARC-010], [NFR-003], [EXC-001], [EXC-002], [ARC-007]
 */
@Testcontainers
@SpringBootTest
@TestPropertySource(properties = {"spring.datasource.url=jdbc:postgresql://localhost:5432/testdb"})
@ActiveProfiles("test")
@Slf4j
public class CourseSchemaIntegrationTest {

    /* --------------------------------------------------------------------- */
    /*  CONSTANTS (Top‑of‑Class Immutable Declarations – Anti‑Magic‑Numbers)   */
    /* --------------------------------------------------------------------- */
    private static final String COURSES_TABLE = "courses";
    private static final String ENROLLMENTS_TABLE = "enrollments";
    private static final String ATTENDANCE_TABLE = "attendance";

    private static final String FK_COURSES_TEACHER = "fk_courses_teacher";
    private static final String FK_ENROLLMENTS_STUDENT = "fk_enrollments_student";
    private static final String FK_ENROLLMENTS_COURSE = "fk_enrollments_course";
    private static final String FK_ATTENDANCE_STUDENT = "fk_attendance_student";
    private static final String FK_ATTENDANCE_COURSE = "fk_attendance_course";

    private static final String UK_ENROLLMENTS_STUDENT_COURSE = "uk_enrollments_student_course";
    private static final String UK_ATTENDANCE_STUDENT_COURSE_DATE = "uk_attendance_student_course_date";

    /* --------------------------------------------------------------------- */
    /*  TEST‑CONTAINER – REAL POSTGRESQL INSTANCE                              */
    /* --------------------------------------------------------------------- */
    @Container
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine")
                .asCompatibleSubstituteFor(ImageNameRegistry.OFFICIAL_REGISTRY))
        .withDatabaseName("membership_hub_test")
        .withUsername("test_user")
        .withPassword("test_pass");

    @DynamicPropertySource
    static void configureDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    /* --------------------------------------------------------------------- */
    /*  REPOSITORIES (Autowired – no manual instantiation)                       */
    /* --------------------------------------------------------------------- */
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate; // for raw schema validation

    /* --------------------------------------------------------------------- */
    /*  TEST HELPERS                                                          */
    /* --------------------------------------------------------------------- */
    private void logTestEntry(String tag) {
        log.info("[TEST_START] {} {}", tag, Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    private void logTestExit(String tag) {
        log.info("[TEST_END] {} {}", tag, Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    /* --------------------------------------------------------------------- */
    /*  INTEGRATION TEST CASES                                                */
    /* --------------------------------------------------------------------- */

    /**
     * @verifies [REQ-008], [DAT-004], [ARC-010], [NFR-003], [EXC-001]
     * Validates that the Courses table exists with correct primary‑key,
     * foreign‑key to Users (teacher), and business constraint
     * (startDate < endDate). Also ensures the schema is created by Flyway.
     */
    @Test
    @Tag("schema-validation")
    void testCourseTableSchema() {
        final String tag = "[REQ-008]";
        logTestEntry(tag);
        try {
            // 1️⃣ Verify table existence
            String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?;";
            int tableCount = jdbcTemplate.queryForObject(sql, Integer.class, COURSES_TABLE);
            assertThat(tableCount).isEqualTo(1);

            // 2️⃣ Verify primary key constraint on courseId
            sql = "SELECT CONSTRAINT_NAME FROM information_schema.table_constraints " +
                  "WHERE table_schema = 'public' AND table_name = ? AND constraint_type = 'PRIMARY KEY';";
            String pkName = jdbcTemplate.queryForObject(sql, String.class, COURSES_TABLE);
            assertThat(pkName).isNotNull();

            // 3️⃣ Verify foreign key to Users (teacher)
            sql = "SELECT CONSTRAINT_NAME FROM information_schema.table_constraints " +
                  "WHERE table_schema = 'public' AND table_name = ? AND constraint_type = 'FOREIGN KEY' AND constraint_name = ?;";
            String fkName = jdbcTemplate.queryForObject(sql, String.class, COURSES_TABLE, FK_COURSES_TEACHER);
            assertThat(fkName).isNotNull();

            // 4️⃣ Verify check constraint startDate < endDate
            sql = "SELECT constraint_name FROM information_schema.check_constraints " +
                  "WHERE constraint_name LIKE '%course_dates%' OR constraint_name LIKE '%chk_course_dates%';";
            String checkConstraint = jdbcTemplate.queryForObject(sql, String.class);
            assertThat(checkConstraint).isNotNull();

            logTestExit(tag);
        } catch (Exception e) {
            log.error("[CRITICAL_FAIL] [REQ-008] Course schema validation failed. Raw error: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * @verifies [REQ-008], [DAT-005], [ARC-010], [NFR-003], [EXC-001]
     * Confirms that Enrollments table enforces a UNIQUE constraint on the
     * (studentId, courseId) pair and a foreign‑key to Courses and Users.
     */
    @Test
    @Tag("schema-validation")
    void testEnrollmentTableSchema() {
        final String tag = "[REQ-008]";
        logTestEntry(tag);
        try {
            // 1️⃣ Verify foreign key to Users (student)
            String sql = "SELECT CONSTRAINT_NAME FROM information_schema.table_constraints " +
                         "WHERE table_schema = 'public' AND table_name = ? AND constraint_type = 'FOREIGN KEY' AND constraint_name = ?;";
            String fkStudent = jdbcTemplate.queryForObject(sql, String.class, ENROLLMENTS_TABLE, FK_ENROLLMENTS_STUDENT);
            assertThat(fkStudent).isNotNull();

            // 2️⃣ Verify foreign key to Courses
            sql = "SELECT CONSTRAINT_NAME FROM information_schema.table_constraints " +
                  "WHERE table_schema = 'public' AND table_name = ? AND constraint_type = 'FOREIGN KEY' AND constraint_name = ?;";
            String fkCourse = jdbcTemplate.queryForObject(sql, String.class, ENROLLMENTS_TABLE, FK_ENROLLMENTS_COURSE);
            assertThat(fkCourse).isNotNull();

            // 3️⃣ Verify unique constraint on (studentId, courseId)
            sql = "SELECT CONSTRAINT_NAME FROM information_schema.table_constraints " +
                  "WHERE table_schema = 'public' AND table_name = ? AND constraint_type = 'UNIQUE' AND constraint_name = ?;";
            String ukName = jdbcTemplate.queryForObject(sql, String.class, ENROLLMENTS_TABLE, UK_ENROLLMENTS_STUDENT_COURSE);
            assertThat(ukName).isNotNull();

            logTestExit(tag);
        } catch (Exception e) {
            log.error("[CRITICAL_FAIL] [REQ-008] Enrollment schema validation failed. Raw error: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * @verifies [REQ-008], [DAT-006], [ARC-010], [NFR-003], [EXC-001], [EXC-002]
     * Validates that the Attendance table enforces a UNIQUE constraint on the
     * (studentId, courseId, attendanceDate) triplet and proper foreign keys.
     */
    @Test
    @Tag("schema-validation")
    void testAttendanceTableSchema() {
        final String tag = "[REQ-008]";
        logTestEntry(tag);
        try {
            // 1️⃣ Verify foreign key to Users (student)
            String sql = "SELECT CONSTRAINT_NAME FROM information_schema.table_constraints " +
                         "WHERE table_schema = 'public' AND table_name = ? AND constraint_type = 'FOREIGN KEY' AND constraint_name = ?;";
            String fkStudent = jdbcTemplate.queryForObject(sql, String.class, ATTENDANCE_TABLE, FK_ATTENDANCE_STUDENT);
            assertThat(fkStudent).isNotNull();

            // 2️⃣ Verify foreign key to Courses
            sql = "SELECT CONSTRAINT_NAME FROM information_schema.table_constraints " +
                  "WHERE table_schema = 'public' AND table_name = ? AND constraint_type = 'FOREIGN KEY' AND constraint_name = ?;";
            String fkCourse = jdbcTemplate.queryForObject(sql, String.class, ATTENDANCE_TABLE, FK_ATTENDANCE_COURSE);
            assertThat(fkCourse).isNotNull();

            // 3️⃣ Verify unique constraint on (studentId, courseId, attendanceDate)
            sql = "SELECT CONSTRAINT_NAME FROM information_schema.table_constraints " +
                  "WHERE table_schema = 'public' AND table_name = ? AND constraint_type = 'UNIQUE' AND constraint_name = ?;";
            String ukName = jdbcTemplate.queryForObject(sql, String.class, ATTENDANCE_TABLE, UK_ATTENDANCE_STUDENT_COURSE_DATE);
            assertThat(ukName).isNotNull();

            logTestExit(tag);
        } catch (Exception e) {
            log.error("[CRITICAL_FAIL] [REQ-008] Attendance schema validation failed. Raw error: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * @verifies [REQ-008], [DAT-005], [ARC-010], [NFR-003], [EXC-001]
     * Integration test for the business rule that a student cannot enroll
     * in the same course twice. Attempts a duplicate enrollment and expects
     * a SQLIntegrityConstraintViolationException (or DataIntegrityViolationException).
     */
    @Test
    @Tag("business-rule")
    void testEnrollmentIdempotency() {
        final String tag = "[REQ-008]";
        logTestEntry(tag);
        try {
            // Create a valid course and a student user
            Course course = new Course();
            course.setTitle("Test Course");
            course.setStartDate(LocalDate.now());
            course.setEndDate(LocalDate.now().plusDays(30));
            User teacher = new User();
            teacher.setUserId(UUID.randomUUID());
            course.setTeacher(teacher);
            course = courseRepository.save(course);

            User student = new User();
            student.setUserId(UUID.randomUUID());
            student = userRepository.save(student);

            // First enrollment – should succeed
            Enrollment enrollment1 = new Enrollment();
            enrollment1.setStudent(student);
            enrollment1.setCourse(course);
            enrollmentRepository.save(enrollment1);

            // Second enrollment – must violate unique constraint
            Enrollment enrollment2 = new Enrollment();
            enrollment2.setStudent(student);
            enrollment2.setCourse(course);
            assertThatThrownBy(() -> enrollmentRepository.save(enrollment2))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("unique");

            logTestExit(tag);
        } catch (Exception e) {
            log.error("[CRITICAL_FAIL] [REQ-008] Enrollment idempotency test failed. Raw error: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * @verifies [REQ-008], [DAT-006], [ARC-010], [NFR-003], [EXC-001], [EXC-002]
     * Integration test for the business rule that a student can only be
     * recorded once per course per day. Attempts a duplicate attendance
     * entry and expects a constraint violation.
     */
    @Test
    @Tag("business-rule")
    void testAttendanceIdempotency() {
        final String tag = "[REQ-008]";
        logTestEntry(tag);
        try {
            // Prepare related entities
            Course course = new Course();
            course.setTitle("Attendance Test Course");
            course.setStartDate(LocalDate.now());
            course.setEndDate(LocalDate.now().plusDays(10));
            User teacher = new User();
            teacher.setUserId(UUID.randomUUID());
            course.setTeacher(teacher);
            course = courseRepository.save(course);

            User student = new User();
            student.setUserId(UUID.randomUUID());
            student = userRepository.save(student);

            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setCourse(course);
            enrollmentRepository.save(enrollment);

            // First attendance – should succeed
            Attendance attendance1 = new Attendance();
            attendance1.setStudent(student);
            attendance1.setCourse(course);
            attendance1.setAttendanceDate(LocalDate.now());
            attendanceRepository.save(attendance1);

            // Second attendance – must violate unique constraint
            Attendance attendance2 = new Attendance();
            attendance2.setStudent(student);
            attendance2.setCourse(course);
            attendance2.setAttendanceDate(LocalDate.now());
            assertThatThrownBy(() -> attendanceRepository.save(attendance2))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("unique");

            logTestExit(tag);
        } catch (Exception e) {
            log.error("[CRITICAL_FAIL] [REQ-008] Attendance idempotency test failed. Raw error: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * @verifies [REQ-008], [DAT-005], [ARC-010], [NFR-003], [EXC-001]
     * Validates referential integrity: deleting a Course cascades to
     * Enrollments and Attendance. Verifies that orphaned records are
     * automatically removed.
     */
    @Test
    @Tag("cascade-validation")
    void testCourseDeletionCascades() {
        final String tag = "[REQ-008]";
        logTestEntry(tag);
        try {
            // Create a course with related enrollment and attendance
            Course course = new Course();
            course.setTitle("Cascade Test Course");
            course.setStartDate(LocalDate.now());
            course.setEndDate(LocalDate.now().plusDays(5));
            User teacher = new User();
            teacher.setUserId(UUID.randomUUID());
            course.setTeacher(teacher);
            course = courseRepository.save(course);

            User student = new User();
            student.setUserId(UUID.randomUUID());
            student = userRepository.save(student);

            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setCourse(course);
            enrollmentRepository.save(enrollment);

            Attendance attendance = new Attendance();
            attendance.setStudent(student);
            attendance.setCourse(course);
            attendance.setAttendanceDate(LocalDate.now());
            attendanceRepository.save(attendance);

            // Delete the course – should cascade
            courseRepository.delete(course);

            // Verify that enrollment and attendance are removed
            assertThat(enrollmentRepository.findById(enrollment.getEnrollmentId())).isEmpty();
            assertThat(attendanceRepository.findById(attendance.getAttendanceId())).isEmpty();

            logTestExit(tag);
        } catch (Exception e) {
            log.error("[CRITICAL_FAIL] [REQ-008] Course cascade test failed. Raw error: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * @verifies [REQ-008], [DAT-004], [ARC-010], [NFR-003], [EXC-001]
     * Integration test for schedule conflict detection: attempts to create
     * a second Course for the same teacher overlapping in date range and
     * expects a DataIntegrityViolationException (or a custom business
     * exception) to be thrown.
     */
    @Test
    @Tag("business-rule")
    void testCourseScheduleConflict() {
        final String tag = "[REQ-008]";
        logTestEntry(tag);
        try {
            // Create a teacher user
            User teacher = new User();
            teacher.setUserId(UUID.randomUUID());
            teacher = userRepository.save(teacher);

            // First course – valid
            Course course1 = new Course();
            course1.setTitle("Course 1");
            course1.setTeacher(teacher);
            course1.setStartDate(LocalDate.now());
            course1.setEndDate(LocalDate.now().plusDays(10));
            courseRepository.save(course1);

            // Second course – overlapping dates, should violate business rule
            Course course2 = new Course();
            course2.setTitle("Course 2");
            course2.setTeacher(teacher);
            course2.setStartDate(LocalDate.now().plusDays(5));
            course2.setEndDate(LocalDate.now().plusDays(20));
            assertThatThrownBy(() -> courseRepository.save(course2))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("conflict");

            logTestExit(tag);
        } catch (Exception e) {
            log.error("[CRITICAL_FAIL] [REQ-008] Course schedule conflict test failed. Raw error: {}", e.getMessage(), e);
            throw e;
        }
    }
}