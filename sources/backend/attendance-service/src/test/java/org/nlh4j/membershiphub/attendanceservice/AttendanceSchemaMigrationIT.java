package org.nlh4j.membershiphub.attendanceservice;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.quarkus.test.junit.QuarkusTest;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Enterprise Integration Test Suite for Database Schema Migrations covering
 * Courses, Enrollments, Attendance, and StudentCards tables.
 *
 * @author Enterprise Quality Assurance Engineering Team
 * @verifies [DAT-004] Courses entity schema mapping and constraints
 * @verifies [DAT-005] Enrollments entity schema mapping and uniqueness
 * @verifies [DAT-006] Attendance composite idempotency and check constraints
 * @verifies [DAT-007] StudentCards validity and life cycle status constraints
 * @verifies [REQ-012] QR attendance scan relational integrity
 * @verifies [REQ-013] Idempotent attendance persistence validation
 * @verifies [EXC-001] Missing and malformed attendance payload isolation
 * @verifies [EXC-002] Duplicate scan handling via composite unique indexes
 */
@QuarkusTest
@QuarkusTestResource(AttendanceSchemaMigrationIT.PostgresTestResource.class)
public class AttendanceSchemaMigrationIT {

    // =========================================================================
    // TOP-OF-CLASS STATIC CONSTANTS DECLARATIONS [0.2]
    // =========================================================================
    private static final Logger LOGGER = LoggerFactory.getLogger(AttendanceSchemaMigrationIT.class);

    private static final String LOG_PREFIX_TEST_START = "[TEST_START] [{}] Initializing integration test case: {}";
    private static final String LOG_PREFIX_TEST_SUCCESS = "[TEST_SUCCESS] [{}] Successfully executed assertion for: {}";
    private static final String LOG_PREFIX_CLEANUP = "[CLEANUP] [DAT-ALL] Purging test transaction artifacts from database";

    private static final String SQL_STATE_UNIQUE_VIOLATION = "23505";
    private static final String SQL_STATE_CHECK_VIOLATION = "23514";

    private static final String TABLE_COURSES = "courses";
    private static final String TABLE_ENROLLMENTS = "enrollments";
    private static final String TABLE_ATTENDANCE = "attendance";
    private static final String TABLE_STUDENT_CARDS = "student_cards";
    private static final String TABLE_USERS = "users";
    private static final String TABLE_ROLES = "roles";
    private static final String TABLE_CENTERS = "centers";

    private static final int EXPECTED_TARGET_TABLE_COUNT = 4;
    private static final int DEFAULT_ROLE_TEACHER_ID = 4;
    private static final int DEFAULT_ROLE_STUDENT_ID = 5;

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_PRESENT = "PRESENT";
    private static final String STATUS_ABSENT = "ABSENT";
    private static final String STATUS_LATE = "LATE";
    private static final String STATUS_INVALID = "INVALID_STATUS";

    private static final String SQL_COUNT_TARGET_TABLES =
        "SELECT count(*) FROM information_schema.tables " +
        "WHERE table_schema = 'public' AND table_name IN ('courses', 'enrollments', 'attendance', 'student_cards')";

    private static final String SQL_INSERT_ROLE =
        "INSERT INTO roles (role_id, name, description) VALUES (?, ?, ?) ON CONFLICT (role_id) DO NOTHING";

    private static final String SQL_INSERT_USER =
        "INSERT INTO users (user_id, email, password_hash, full_name, role_id, provider) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_CENTER =
        "INSERT INTO centers (center_id, name, address, tax_id) VALUES (?, ?, ?, ?)";

    private static final String SQL_INSERT_COURSE =
        "INSERT INTO courses (course_id, title, description, start_date, end_date, teacher_id, max_students, center_id) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_ENROLLMENT =
        "INSERT INTO enrollments (enrollment_id, student_id, course_id, enrollment_date, status) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_ATTENDANCE =
        "INSERT INTO attendance (attendance_id, student_id, course_id, attendance_date, timestamp, status) " +
        "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_STUDENT_CARD =
        "INSERT INTO student_cards (card_id, student_id, issue_date, validity_days, remaining_days, end_date, status) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_PURGE_TEST_DATA =
        "TRUNCATE TABLE attendance, enrollments, student_cards, courses, centers, users, roles CASCADE";

    // Pre-allocated Deterministic Fixtures
    private static final UUID FIXTURE_TEACHER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID FIXTURE_STUDENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID FIXTURE_STUDENT_ID_ALT = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID FIXTURE_CENTER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID FIXTURE_COURSE_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID FIXTURE_ATTENDANCE_ID_1 = UUID.fromString("66666666-6666-6666-6666-666666666661");
    private static final UUID FIXTURE_ATTENDANCE_ID_2 = UUID.fromString("66666666-6666-6666-6666-666666666662");

    @Inject
    Flyway flyway;

    @Inject
    DataSource dataSource;

    /**
     * Managed Testcontainers PostgreSQL 16 Environment Bootstrapper.
     */
    public static class PostgresTestResource implements QuarkusTestResourceLifecycleManager {
        private PostgreSQLContainer<?> postgres;

        @Override
        public Map<String, String> start() {
            postgres = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("membership_hub_test")
                .withUsername("test_user")
                .withPassword("test_password");
            postgres.start();

            Map<String, String> properties = new HashMap<>();
            properties.put("quarkus.datasource.jdbc.url", postgres.getJdbcUrl());
            properties.put("quarkus.datasource.username", postgres.getUsername());
            properties.put("quarkus.datasource.password", postgres.getPassword());
            properties.put("quarkus.flyway.migrate-at-start", "true");
            return properties;
        }

        @Override
        public void stop() {
            if (postgres != null && postgres.isRunning()) {
                postgres.stop();
            }
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        LOGGER.info("[SETUP] Initializing schema migrations and preparing relational seed anchors");
        // Ensure Flyway executes all pending DDL migrations
        Assertions.assertNotNull(flyway, "Flyway bean injection must not be null");
        flyway.migrate();

        // Seed structural prerequisites: Roles, Users, Centers, Courses
        seedPrerequisites();
    }

    @AfterEach
    void tearDown() throws SQLException {
        LOGGER.info(LOG_PREFIX_CLEANUP);
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(SQL_PURGE_TEST_DATA);
        } catch (SQLException e) {
            LOGGER.error("[ERROR] [DAT-ALL] Tear down cleanup failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    // =========================================================================
    // 1. HAPPY CASES: SCHEMA VERIFICATION & RELATIONAL PERSISTENCE
    // =========================================================================

    /**
     * Verifies that all 4 target tables exist in the PostgreSQL information schema.
     *
     * @verifies [DAT-004], [DAT-005], [DAT-006], [DAT-007]
     */
    @Test
    @DisplayName("Verify that courses, enrollments, attendance, and student_cards tables exist")
    void testAllTargetTablesExistInInformationSchema() throws SQLException {
        LOGGER.info(LOG_PREFIX_TEST_START, "DAT-004][DAT-005][DAT-006][DAT-007", "testAllTargetTablesExistInInformationSchema");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_COUNT_TARGET_TABLES);
             ResultSet rs = stmt.executeQuery()) {

            Assertions.assertTrue(rs.next(), "Result set should return at least one aggregate row");
            int tableCount = rs.getInt(1);

            // [DAT-004] to [DAT-007]: Assert that all 4 core business tables are successfully provisioned
            Assertions.assertEquals(EXPECTED_TARGET_TABLE_COUNT, tableCount,
                "Exactly 4 tables (courses, enrollments, attendance, student_cards) must be present in schema");

            LOGGER.info(LOG_PREFIX_TEST_SUCCESS, "DAT-004][DAT-005][DAT-006][DAT-007", "Target tables count matches 4");
        }
    }

    /**
     * Verifies complete relational insertion graph across courses, enrollments, student_cards, and attendance.
     *
     * @verifies [DAT-004], [DAT-005], [DAT-006], [DAT-007], [REQ-012]
     */
    @Test
    @DisplayName("Verify valid full relational lifecycle insertion across target entities")
    void testValidRelationalDataInsertionHappyPath() throws SQLException {
        LOGGER.info(LOG_PREFIX_TEST_START, "DAT-004][DAT-005][DAT-006][DAT-007", "testValidRelationalDataInsertionHappyPath");

        try (Connection conn = dataSource.getConnection()) {
            // [DAT-005] Insert Enrollment
            UUID enrollmentId = UUID.randomUUID();
            try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_ENROLLMENT)) {
                stmt.setObject(1, enrollmentId);
                stmt.setObject(2, FIXTURE_STUDENT_ID);
                stmt.setObject(3, FIXTURE_COURSE_ID);
                stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setString(5, STATUS_ACTIVE);
                int rows = stmt.executeUpdate();
                Assertions.assertEquals(1, rows, "Enrollment record should be persisted successfully");
            }

            // [DAT-007] Insert StudentCard
            UUID cardId = UUID.randomUUID();
            try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_STUDENT_CARD)) {
                stmt.setObject(1, cardId);
                stmt.setObject(2, FIXTURE_STUDENT_ID);
                stmt.setDate(3, Date.valueOf(LocalDate.now()));
                stmt.setInt(4, 365);
                stmt.setInt(5, 365);
                stmt.setDate(6, Date.valueOf(LocalDate.now().plusDays(365)));
                stmt.setString(7, STATUS_ACTIVE);
                int rows = stmt.executeUpdate();
                Assertions.assertEquals(1, rows, "StudentCard record should be persisted successfully");
            }

            // [DAT-006] Insert Attendance
            try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_ATTENDANCE)) {
                stmt.setObject(1, FIXTURE_ATTENDANCE_ID_1);
                stmt.setObject(2, FIXTURE_STUDENT_ID);
                stmt.setObject(3, FIXTURE_COURSE_ID);
                stmt.setDate(4, Date.valueOf(LocalDate.now()));
                stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setString(6, STATUS_PRESENT);
                int rows = stmt.executeUpdate();
                Assertions.assertEquals(1, rows, "Attendance record should be persisted successfully");
            }

            LOGGER.info(LOG_PREFIX_TEST_SUCCESS, "DAT-004][DAT-005][DAT-006][DAT-007", "Full relational happy path persisted");
        }
    }

    // =========================================================================
    // 2. EDGE CASES & BOUNDARY CONDITIONS: IDEMPOTENCY & UNIQUENESS
    // =========================================================================

    /**
     * Validates that the composite unique constraint `uq_attendance_idempotency` prevents duplicate attendance.
     *
     * @verifies [DAT-006], [REQ-013], [EXC-002]
     */
    @Test
    @DisplayName("Verify attendance composite unique constraint enforces idempotency per student/course/date")
    void testAttendanceCompositeIdempotencyConstraintViolation() throws SQLException {
        LOGGER.info(LOG_PREFIX_TEST_START, "DAT-006][REQ-013][EXC-002", "testAttendanceCompositeIdempotencyConstraintViolation");

        try (Connection conn = dataSource.getConnection()) {
            LocalDate today = LocalDate.now();

            // Insert primary attendance record
            try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_ATTENDANCE)) {
                stmt.setObject(1, FIXTURE_ATTENDANCE_ID_1);
                stmt.setObject(2, FIXTURE_STUDENT_ID);
                stmt.setObject(3, FIXTURE_COURSE_ID);
                stmt.setDate(4, Date.valueOf(today));
                stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setString(6, STATUS_PRESENT);
                stmt.executeUpdate();
            }

            // [REQ-013] [EXC-002] Attempting to insert a duplicate scan on the same date must fail at database level
            try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_ATTENDANCE)) {
                stmt.setObject(1, FIXTURE_ATTENDANCE_ID_2);
                stmt.setObject(2, FIXTURE_STUDENT_ID);
                stmt.setObject(3, FIXTURE_COURSE_ID);
                stmt.setDate(4, Date.valueOf(today)); // Exact same date
                stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setString(6, STATUS_PRESENT);

                SQLException exception = Assertions.assertThrows(SQLException.class, stmt::executeUpdate,
                    "Inserting duplicate attendance for the same student, course, and date must trigger SQL exception");

                // Assert SQLState 23505 (Unique Constraint Violation)
                Assertions.assertEquals(SQL_STATE_UNIQUE_VIOLATION, exception.getSQLState(),
                    "Expected SQLState 23505 for duplicate attendance idempotency composite key");

                LOGGER.info(LOG_PREFIX_TEST_SUCCESS, "DAT-006][REQ-013][EXC-002",
                    "Idempotency verified: Duplicate attendance blocked with SQLState 23505");
            }
        }
    }

    /**
     * Validates that enrollment table enforces unique student and course combinations.
     *
     * @verifies [DAT-005]
     */
    @Test
    @DisplayName("Verify enrollments table blocks duplicate student-course pairings")
    void testEnrollmentUniqueStudentCourseConstraint() throws SQLException {
        LOGGER.info(LOG_PREFIX_TEST_START, "DAT-005", "testEnrollmentUniqueStudentCourseConstraint");

        try (Connection conn = dataSource.getConnection()) {
            // First valid enrollment
            try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_ENROLLMENT)) {
                stmt.setObject(1, UUID.randomUUID());
                stmt.setObject(2, FIXTURE_STUDENT_ID);
                stmt.setObject(3, FIXTURE_COURSE_ID);
                stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setString(5, STATUS_ACTIVE);
                stmt.executeUpdate();
            }

            // Attempt duplicate enrollment for the exact same student and course
            try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_ENROLLMENT)) {
                stmt.setObject(1, UUID.randomUUID());
                stmt.setObject(2, FIXTURE_STUDENT_ID);
                stmt.setObject(3, FIXTURE_COURSE_ID);
                stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setString(5, STATUS_ACTIVE);

                SQLException exception = Assertions.assertThrows(SQLException.class, stmt::executeUpdate,
                    "Duplicate student enrollment in the same course must violate uniqueness constraint");

                Assertions.assertEquals(SQL_STATE_UNIQUE_VIOLATION, exception.getSQLState(),
                    "Expected SQLState 23505 for duplicate enrollment");

                LOGGER.info(LOG_PREFIX_TEST_SUCCESS, "DAT-005", "Duplicate enrollment blocked successfully");
            }
        }
    }

    /**
     * Validates that student_cards table enforces a 1-to-1 relationship with user via student_id unique constraint.
     *
     * @verifies [DAT-007]
     */
    @Test
    @DisplayName("Verify student_cards table rejects multiple active cards for the same student")
    void testStudentCardsUniqueStudentIdConstraint() throws SQLException {
        LOGGER.info(LOG_PREFIX_TEST_START, "DAT-007", "testStudentCardsUniqueStudentIdConstraint");

        try (Connection conn = dataSource.getConnection()) {
            // Insert primary student card
            try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_STUDENT_CARD)) {
                stmt.setObject(1, UUID.randomUUID());
                stmt.setObject(2, FIXTURE_STUDENT_ID);
                stmt.setDate(3, Date.valueOf(LocalDate.now()));
                stmt.setInt(4, 30);
                stmt.setInt(5, 30);
                stmt.setDate(6, Date.valueOf(LocalDate.now().plusDays(30)));
                stmt.setString(7, STATUS_ACTIVE);
                stmt.executeUpdate();
            }

            // Attempt to assign a second distinct card to the same student
            try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_STUDENT_CARD)) {
                stmt.setObject(1, UUID.randomUUID());
                stmt.setObject(2, FIXTURE_STUDENT_ID); // Duplicate student ID
                stmt.setDate(3, Date.valueOf(LocalDate.now()));
                stmt.setInt(4, 60);
                stmt.setInt(5, 60);
                stmt.setDate(6, Date.valueOf(LocalDate.now().plusDays(60)));
                stmt.setString(7, STATUS_ACTIVE);

                SQLException exception = Assertions.assertThrows(SQLException.class, stmt::executeUpdate,
                    "Duplicate student card assignment to a single student must trigger unique violation");

                Assertions.assertEquals(SQL_STATE_UNIQUE_VIOLATION, exception.getSQLState(),
                    "Expected SQLState 23505 for duplicate student card assignment");

                LOGGER.info(LOG_PREFIX_TEST_SUCCESS, "DAT-007", "Duplicate student card assignment blocked");
            }
        }
    }

    // =========================================================================
    // 3. EXCEPTION CASES & NEGATIVE PATHS: CHECK CONSTRAINTS & DATA INTEGRITY
    // =========================================================================

    /**
     * Validates that the check constraint `chk_attendance_status` rejects invalid status strings.
     *
     * @verifies [DAT-006], [EXC-001]
     */
    @Test
    @DisplayName("Verify attendance table check constraint chk_attendance_status rejects malformed statuses")
    void testAttendanceStatusCheckConstraintRejectsInvalidValue() throws SQLException {
        LOGGER.info(LOG_PREFIX_TEST_START, "DAT-006][EXC-001", "testAttendanceStatusCheckConstraintRejectsInvalidValue");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_ATTENDANCE)) {

            stmt.setObject(1, UUID.randomUUID());
            stmt.setObject(2, FIXTURE_STUDENT_ID);
            stmt.setObject(3, FIXTURE_COURSE_ID);
            stmt.setDate(4, Date.valueOf(LocalDate.now()));
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(6, STATUS_INVALID); // Violates CHECK (status IN ('PRESENT','ABSENT','LATE'))

            SQLException exception = Assertions.assertThrows(SQLException.class, stmt::executeUpdate,
                "Inserting invalid attendance status must violate check constraint");

            Assertions.assertEquals(SQL_STATE_CHECK_VIOLATION, exception.getSQLState(),
                "Expected SQLState 23514 (Check Constraint Violation) for invalid attendance status");

            LOGGER.info(LOG_PREFIX_TEST_SUCCESS, "DAT-006][EXC-001", "Invalid attendance status successfully rejected");
        }
    }

    /**
     * Validates that the check constraint `chk_student_cards_validity` enforces positive validity and non-negative remaining days.
     *
     * @verifies [DAT-007]
     */
    @Test
    @DisplayName("Verify student_cards check constraint rejects negative validity and remaining days")
    void testStudentCardsValidityCheckConstraintRejectsNegativeValues() throws SQLException {
        LOGGER.info(LOG_PREFIX_TEST_START, "DAT-007", "testStudentCardsValidityCheckConstraintRejectsNegativeValues");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_STUDENT_CARD)) {

            stmt.setObject(1, UUID.randomUUID());
            stmt.setObject(2, FIXTURE_STUDENT_ID_ALT);
            stmt.setDate(3, Date.valueOf(LocalDate.now()));
            stmt.setInt(4, 0); // Violates validity_days > 0
            stmt.setInt(5, -5); // Violates remaining_days >= 0
            stmt.setDate(6, Date.valueOf(LocalDate.now()));
            stmt.setString(7, STATUS_ACTIVE);

            SQLException exception = Assertions.assertThrows(SQLException.class, stmt::executeUpdate,
                "Student card with non-positive validity or negative remaining days must be rejected by check constraint");

            Assertions.assertEquals(SQL_STATE_CHECK_VIOLATION, exception.getSQLState(),
                "Expected SQLState 23514 for student card validity check constraint");

            LOGGER.info(LOG_PREFIX_TEST_SUCCESS, "DAT-007", "Invalid student card validity days successfully rejected");
        }
    }

    /**
     * Validates that courses table check constraint `chk_courses_dates` ensures end_date is greater than or equal to start_date.
     *
     * @verifies [DAT-004]
     */
    @Test
    @DisplayName("Verify courses check constraint chk_courses_dates rejects end_date preceding start_date")
    void testCoursesDateCheckConstraintRejectsInvertedDateRange() throws SQLException {
        LOGGER.info(LOG_PREFIX_TEST_START, "DAT-004", "testCoursesDateCheckConstraintRejectsInvertedDateRange");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_COURSE)) {

            stmt.setObject(1, UUID.randomUUID());
            stmt.setString(2, "Advanced Quantum Computing");
            stmt.setString(3, "Course with intentionally inverted date parameters");
            stmt.setDate(4, Date.valueOf(LocalDate.now().plusDays(30))); // Start Date
            stmt.setDate(5, Date.valueOf(LocalDate.now().minusDays(10))); // End Date before Start Date
            stmt.setObject(6, FIXTURE_TEACHER_ID);
            stmt.setInt(7, 25);
            stmt.setObject(8, FIXTURE_CENTER_ID);

            SQLException exception = Assertions.assertThrows(SQLException.class, stmt::executeUpdate,
                "Creating a course with end_date prior to start_date must violate check constraint");

            Assertions.assertEquals(SQL_STATE_CHECK_VIOLATION, exception.getSQLState(),
                "Expected SQLState 23514 for inverted course date range");

            LOGGER.info(LOG_PREFIX_TEST_SUCCESS, "DAT-004", "Inverted course dates check constraint verified");
        }
    }

    // =========================================================================
    // PRIVATE HELPER ROUTINES & SEEDING UTILITIES
    // =========================================================================

    /**
     * Seeds relational dependency parents (Roles, Users, Centers, and Courses)
     * required for foreign-key compliance during schema tests.
     */
    private void seedPrerequisites() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Seed Roles
            try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_ROLE)) {
                stmt.setInt(1, DEFAULT_ROLE_TEACHER_ID);
                stmt.setString(2, "Teacher");
                stmt.setString(3, "Assigned Instructor Role");
                stmt.executeUpdate();

                stmt.setInt(1, DEFAULT_ROLE_STUDENT_ID);
                stmt.setString(2, "Student");
                stmt.setString(3, "Enrolled Learner Role");
                stmt.executeUpdate();
            }

            // 2. Seed Users (Teacher and Students)
            try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_USER)) {
                // Teacher
                stmt.setObject(1, FIXTURE_TEACHER_ID);
                stmt.setString(2, "teacher.lead@membershiphub.local");
                stmt.setString(3, "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");
                stmt.setString(4, "Lead Enterprise Instructor");
                stmt.setInt(5, DEFAULT_ROLE_TEACHER_ID);
                stmt.setString(6, "local");
                stmt.executeUpdate();

                // Student Primary
                stmt.setObject(1, FIXTURE_STUDENT_ID);
                stmt.setString(2, "student.alpha@membershiphub.local");
                stmt.setString(3, "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");
                stmt.setString(4, "Alpha Enrolled Student");
                stmt.setInt(5, DEFAULT_ROLE_STUDENT_ID);
                stmt.setString(6, "local");
                stmt.executeUpdate();

                // Student Alt
                stmt.setObject(1, FIXTURE_STUDENT_ID_ALT);
                stmt.setString(2, "student.beta@membershiphub.local");
                stmt.setString(3, "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");
                stmt.setString(4, "Beta Enrolled Student");
                stmt.setInt(5, DEFAULT_ROLE_STUDENT_ID);
                stmt.setString(6, "local");
                stmt.executeUpdate();
            }

            // 3. Seed Center
            try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_CENTER)) {
                stmt.setObject(1, FIXTURE_CENTER_ID);
                stmt.setString(2, "Main Enterprise Technology Center");
                stmt.setString(3, "789 Innovation Boulevard, District 1");
                stmt.setString(4, "0123456789");
                stmt.executeUpdate();
            }

            // 4. Seed Course
            try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_COURSE)) {
                stmt.setObject(1, FIXTURE_COURSE_ID);
                stmt.setString(2, "Microservices with Quarkus & PostgreSQL");
                stmt.setString(3, "Enterprise Cloud-Native Microservices Architecture");
                stmt.setDate(4, Date.valueOf(LocalDate.now().minusDays(10)));
                stmt.setDate(5, Date.valueOf(LocalDate.now().plusDays(80)));
                stmt.setObject(6, FIXTURE_TEACHER_ID);
                stmt.setInt(7, 30);
                stmt.setObject(8, FIXTURE_CENTER_ID);
                stmt.executeUpdate();
            }

            conn.commit();
        }
    }
}