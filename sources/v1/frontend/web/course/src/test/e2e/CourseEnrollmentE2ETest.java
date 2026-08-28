package org.nlh4j.saas.membership-hub.course.e2e;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.UUID;

/**
 * End-to-end integration test suite for course enrollment and QR attendance workflows.
 * Validates compliance with requirements [REQ-007], [REQ-010], [REQ-011], [REQ-012], [REQ-013]
 * and exception handling [EXC-001], [EXC-002] as per enterprise architecture specifications.
 * 
 * Test Coverage Verification Tags:
 * - @verifies [REQ-007] Course list API functionality
 * - @verifies [REQ-010] Available courses filtering for students
 * - @verifies [REQ-011] Student enrollment flow with auto-account creation
 * - @verifies [REQ-012] QR code attendance scanning
 * - @verifies [REQ-013] Idempotent attendance record creation
 */
@Testcontainers
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CourseEnrollmentE2ETest {
    // [LOGGER-001] Enterprise logger for test process tracing per audit requirements
    private static final Logger logger = LoggerFactory.getLogger(CourseEnrollmentE2ETest.class);

    // [CONTAINER-001] Testcontainers PostgreSQL instance for isolated integration testing
    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("membership_hub_test")
            .withUsername("test_user")
            .withPassword("test_pass");

    // [CONST-001] API endpoint constants (hoisted per anti-magic-number policy)
    public static final String COURSE_LIST_ENDPOINT = "/api/v1/courses";
    public static final String AVAILABLE_COURSES_ENDPOINT = "/api/v1/courses/available";
    public static final String ENROLLMENT_ENDPOINT = "/api/v1/enrollments";
    public static final String ATTENDANCE_SCAN_ENDPOINT = "/api/v1/attendance/scan";
    public static final String AUTH_REGISTER_ENDPOINT = "/api/v1/auth/register";
    public static final String AUTH_LOGIN_ENDPOINT = "/api/v1/auth/login";
    public static final String ADMIN_CENTER_ENDPOINT = "/api/v1/admin/centers";
    public static final String ADMIN_USER_ENDPOINT = "/api/v1/admin/users";

    // [CONST-002] Test user data constants
    public static final String TEST_STUDENT_EMAIL = "test.student@membershiphub.com";
    public static final String TEST_STUDENT_PASSWORD = "TestPass123!";
    public static final String TEST_STUDENT_FULL_NAME = "Test Student";
    public static final String TEST_TEACHER_EMAIL = "test.teacher@membershiphub.com";
    public static final String TEST_TEACHER_PASSWORD = "TestPass123!";
    public static final String TEST_TEACHER_FULL_NAME = "Test Teacher";
    public static final String TEST_ADMIN_EMAIL = "test.admin@membershiphub.com";
    public static final String TEST_ADMIN_PASSWORD = "TestPass123!";
    public static final String TEST_ADMIN_FULL_NAME = "Test Admin";
    public static final String TEST_CENTER_NAME = "Test Center E2E";
    public static final String TEST_CENTER_ADDRESS = "123 Test Street, Test City";
    public static final String TEST_CENTER_TAX_ID = "1234567890";

    // [CONST-003] Test course data constants
    public static final String TEST_COURSE_TITLE = "Test Course E2E";
    public static final String TEST_COURSE_DESCRIPTION = "E2E test course for enrollment and attendance validation";
    public static final String TEST_COURSE_START_DATE = "2024-12-01";
    public static final String TEST_COURSE_END_DATE = "2024-12-31";
    public static final int TEST_COURSE_MAX_STUDENTS = 2;
    public static final String TEST_QR_CODE_PAYLOAD = "courseId=test-course-id&sessionId=test-session-001";

    // [CONST-004] Test response/error code constants
    public static final String STATUS_RECORDED = "RECORDED";
    public static final String STATUS_DUPLICATE = "DUPLICATE";
    public static final String ERROR_TAX_ID_CONFLICT = "TAX_ID_CONFLICT";
    public static final String ERROR_SCHEDULE_CONFLICT = "CONFLICT";
    public static final String ERROR_ENROLLMENT_FULL = "ENROLLMENT_FULL";
    public static final String ERROR_ALREADY_ENROLLED = "ALREADY_ENROLLED";
    public static final String ERROR_INVALID_QR = "INVALID_QR";
    public static final String ERROR_NOT_ENROLLED = "NOT_ENROLLED";

    // Test data state holders
    private String studentAccessToken;
    private String teacherAccessToken;
    private String adminAccessToken;
    private UUID testCourseId;
    private UUID testStudentId;
    private UUID testTeacherId;
    private UUID testCenterId;

    // [SETUP-001] Initialize test environment and seed required test data before each test
    @BeforeEach
    public void setup() {
        logger.info("[TEST_SETUP] [REQ-007][REQ-010][REQ-011][REQ-012][REQ-013] Initializing E2E test environment for course enrollment and attendance flow");
        RestAssured.baseURI = "http://localhost:8081"; // Quarkus test default runtime port
        RestAssured.defaultParser = io.restassured.parsing.Parser.JSON;

        // Step 1: Register and authenticate system admin for center/course creation
        Response adminRegisterResponse = RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"" + TEST_ADMIN_EMAIL + "\",\"password\":\"" + TEST_ADMIN_PASSWORD + "\",\"fullName\":\"" + TEST_ADMIN_FULL_NAME + "\",\"provider\":\"local\"}")
                .post(AUTH_REGISTER_ENDPOINT);
        Assertions.assertEquals(201, adminRegisterResponse.getStatusCode(), "Admin registration should return 201 Created");
        adminAccessToken = adminRegisterResponse.jsonPath().getString("accessToken");

        // Step 2: Create test center for course association
        Response centerResponse = RestAssured.given()
                .header("Authorization", "Bearer " + adminAccessToken)
                .contentType(ContentType.JSON)
                .body("{\"name\":\"" + TEST_CENTER_NAME + "\",\"address\":\"" + TEST_CENTER_ADDRESS + "\",\"taxId\":\"" + TEST_CENTER_TAX_ID + "\",\"contactPhone\":\"0123456789\",\"contactEmail\":\"center@test.com\"}")
                .post(ADMIN_CENTER_ENDPOINT);
        Assertions.assertEquals(201, centerResponse.getStatusCode(), "Center creation should return 201 Created");
        testCenterId = centerResponse.jsonPath().getUUID("centerId");

        // Step 3: Register and authenticate test teacher for course assignment
        Response teacherRegisterResponse = RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"" + TEST_TEACHER_EMAIL + "\",\"password\":\"" + TEST_TEACHER_PASSWORD + "\",\"fullName\":\"" + TEST_TEACHER_FULL_NAME + "\",\"provider\":\"local\"}")
                .post(AUTH_REGISTER_ENDPOINT);
        Assertions.assertEquals(201, teacherRegisterResponse.getStatusCode(), "Teacher registration should return 201 Created");
        teacherAccessToken = teacherRegisterResponse.jsonPath().getString("accessToken");
        testTeacherId = teacherRegisterResponse.jsonPath().getUUID("userId");

        // Step 4: Create test course assigned to test teacher
        Response courseResponse = RestAssured.given()
                .header("Authorization", "Bearer " + adminAccessToken)
                .contentType(ContentType.JSON)
                .body("{\"title\":\"" + TEST_COURSE_TITLE + "\",\"description\":\"" + TEST_COURSE_DESCRIPTION + "\",\"startDate\":\"" + TEST_COURSE_START_DATE + "\",\"endDate\":\"" + TEST_COURSE_END_DATE + "\",\"teacherId\":\"" + testTeacherId + "\",\"maxStudents\":" + TEST_COURSE_MAX_STUDENTS + ",\"centerId\":\"" + testCenterId + "\"}")
                .post(COURSE_LIST_ENDPOINT);
        Assertions.assertEquals(201, courseResponse.getStatusCode(), "Course creation should return 201 Created");
        testCourseId = courseResponse.jsonPath().getUUID("courseId");

        // Step 5: Register and authenticate test student for enrollment and attendance testing
        Response studentRegisterResponse = RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"" + TEST_STUDENT_EMAIL + "\",\"password\":\"" + TEST_STUDENT_PASSWORD + "\",\"fullName\":\"" + TEST_STUDENT_FULL_NAME + "\",\"provider\":\"local\"}")
                .post(AUTH_REGISTER_ENDPOINT);
        Assertions.assertEquals(201, studentRegisterResponse.getStatusCode(), "Student registration should return 201 Created");
        studentAccessToken = studentRegisterResponse.jsonPath().getString("accessToken");
        testStudentId = studentRegisterResponse.jsonPath().getUUID("userId");

        logger.info("[TEST_SETUP] [REQ-007][REQ-010][REQ-011][REQ-012][REQ-013] Test environment initialized successfully with seeded test data");
    }

    // [CLEANUP-001] Clean up test data after each test execution to maintain test isolation
    @AfterEach
    public void cleanup() {
        logger.info("[TEST_CLEANUP] [REQ-007][REQ-010][REQ-011][REQ-012][REQ-013] Starting test data cleanup");
        try {
            // Delete test course if exists
            if (testCourseId != null) {
                RestAssured.given()
                        .header("Authorization", "Bearer " + adminAccessToken)
                        .delete(COURSE_LIST_ENDPOINT + "/" + testCourseId);
            }
            // Delete test student if exists
            if (testStudentId != null) {
                RestAssured.given()
                        .header("Authorization", "Bearer " + adminAccessToken)
                        .delete(ADMIN_USER_ENDPOINT + "/" + testStudentId);
            }
            // Delete test teacher if exists
            if (testTeacherId != null) {
                RestAssured.given()
                        .header("Authorization", "Bearer " + adminAccessToken)
                        .delete(ADMIN_USER_ENDPOINT + "/" + testTeacherId);
            }
            // Delete test center if exists
            if (testCenterId != null) {
                RestAssured.given()
                        .header("Authorization", "Bearer " + adminAccessToken)
                        .delete(ADMIN_CENTER_ENDPOINT + "/" + testCenterId);
            }
            logger.info("[TEST_CLEANUP] [REQ-007][REQ-010][REQ-011][REQ-012][REQ-013] Test data cleanup completed successfully");
        } catch (Exception e) {
            logger.error("[TEST_CLEANUP_FAIL] [REQ-007][REQ-010][REQ-011][REQ-012][REQ-013] Test data cleanup failed with error: {}", e.getMessage());
        }
    }

    /**
     * Validates full happy path: student views available courses, enrolls in a course,
     * scans QR for attendance, and verifies idempotent duplicate scan handling.
     * 
     * Verification Tags:
     * - [REQ-007] Course list retrieval
     * - [REQ-010] Available courses endpoint excludes already enrolled courses
     * - [REQ-011] Successful enrollment with record creation
     * - [REQ-012] QR scan creates valid attendance record
     * - [REQ-013] Idempotent attendance record creation for duplicate scans
     */
    @Test
    @Order(1)
    @DisplayName("Validate full course enrollment and QR attendance happy path")
    public void testHappyPathEnrollmentAndAttendance() {
        logger.info("[TEST_START] [REQ-007][REQ-010][REQ-011][REQ-012][REQ-013] Executing happy path test for course enrollment and QR attendance");
        try {
            // Step 1: Authenticate student to get access token
            Response loginResponse = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body("{\"email\":\"" + TEST_STUDENT_EMAIL + "\",\"password\":\"" + TEST_STUDENT_PASSWORD + "\"}")
                    .post(AUTH_LOGIN_ENDPOINT);
            Assertions.assertEquals(200, loginResponse.getStatusCode(), "Student login should return 200 OK");
            studentAccessToken = loginResponse.jsonPath().getString("accessToken");
            Assertions.assertNotNull(studentAccessToken, "Access token should not be null for authenticated student");

            // Step 2: Retrieve list of all courses (REQ-007 validation)
            Response allCoursesResponse = RestAssured.given()
                    .header("Authorization", "Bearer " + studentAccessToken)
                    .get(COURSE_LIST_ENDPOINT);
            Assertions.assertEquals(200, allCoursesResponse.getStatusCode(), "Get all courses should return 200 OK");
            int totalCourseCount = allCoursesResponse.jsonPath().getList("$").size();
            Assertions.assertTrue(totalCourseCount > 0, "System should have at least one active course");

            // Step 3: Retrieve available courses for student (excludes already enrolled) (REQ-010 validation)
            Response availableCoursesResponse = RestAssured.given()
                    .header("Authorization", "Bearer " + studentAccessToken)
                    .get(AVAILABLE_COURSES_ENDPOINT);
            Assertions.assertEquals(200, availableCoursesResponse.getStatusCode(), "Get available courses should return 200 OK");
            int initialAvailableCount = availableCoursesResponse.jsonPath().getList("$").size();
            Assertions.assertTrue(initialAvailableCount > 0, "Student should have at least one available course to enroll");
            Assertions.assertTrue(availableCoursesResponse.jsonPath().getList("courseId").contains(testCourseId.toString()), 
                    "Test course should be present in available courses list");

            // Step 4: Enroll student in test course (REQ-011 validation)
            Response enrollResponse = RestAssured.given()
                    .header("Authorization", "Bearer " + studentAccessToken)
                    .contentType(ContentType.JSON)
                    .body("{\"courseId\":\"" + testCourseId + "\"}")
                    .post(ENROLLMENT_ENDPOINT);
            Assertions.assertEquals(201, enrollResponse.getStatusCode(), "Enrollment should return 201 Created");
            UUID enrollmentId = enrollResponse.jsonPath().getUUID("enrollmentId");
            Assertions.assertNotNull(enrollmentId, "Enrollment record ID should not be null");

            // Step 5: Verify course is removed from available list after enrollment (REQ-010 validation)
            Response availableCoursesAfterEnroll = RestAssured.given()
                    .header("Authorization", "Bearer " + studentAccessToken)
                    .get(AVAILABLE_COURSES_ENDPOINT);
            Assertions.assertEquals(200, availableCoursesAfterEnroll.getStatusCode(), "Get available courses after enrollment should return 200 OK");
            int availableCountAfterEnroll = availableCoursesAfterEnroll.jsonPath().getList("$").size();
            Assertions.assertEquals(initialAvailableCount - 1, availableCountAfterEnroll, 
                    "Available course count should decrease by 1 after successful enrollment");
            Assertions.assertFalse(availableCoursesAfterEnroll.jsonPath().getList("courseId").contains(testCourseId.toString()), 
                    "Enrolled course should not appear in available courses list");

            // Step 6: Scan valid QR code for attendance (REQ-012 validation)
            long scanTimestamp = System.currentTimeMillis();
            Response attendanceResponse = RestAssured.given()
                    .header("Authorization", "Bearer " + studentAccessToken)
                    .contentType(ContentType.JSON)
                    .body("{\"qrCode\":\"" + TEST_QR_CODE_PAYLOAD + "\",\"timestamp\":" + scanTimestamp + "}")
                    .post(ATTENDANCE_SCAN_ENDPOINT);
            Assertions.assertEquals(200, attendanceResponse.getStatusCode(), "Valid QR scan should return 200 OK");
            String attendanceStatus = attendanceResponse.jsonPath().getString("status");
            Assertions.assertEquals(STATUS_RECORDED, attendanceStatus, "First QR scan should return RECORDED status");
            UUID attendanceId = attendanceResponse.jsonPath().getUUID("attendanceId");
            Assertions.assertNotNull(attendanceId, "Attendance record ID should not be null");

            // Step 7: Scan same QR code again to verify idempotency (REQ-013 validation)
            Response duplicateAttendanceResponse = RestAssured.given()
                    .header("Authorization", "Bearer " + studentAccessToken)
                    .contentType(ContentType.JSON)
                    .body("{\"qrCode\":\"" + TEST_QR_CODE_PAYLOAD + "\",\"timestamp\":" + (scanTimestamp + 1000) + "}")
                    .post(ATTENDANCE_SCAN_ENDPOINT);
            Assertions.assertEquals(200, duplicateAttendanceResponse.getStatusCode(), "Duplicate QR scan should return 200 OK");
            String duplicateStatus = duplicateAttendanceResponse.jsonPath().getString("status");
            Assertions.assertEquals(STATUS_DUPLICATE, duplicateStatus, "Duplicate QR scan should return DUPLICATE status");
            UUID duplicateAttendanceId = duplicateAttendanceResponse.jsonPath().getUUID("attendanceId");
            Assertions.assertEquals(attendanceId, duplicateAttendanceId, "Duplicate scan should return existing attendance ID, not create new record");

            logger.info("[TEST_PASS] [REQ-007][REQ-010][REQ-011][REQ-012][REQ-013] Happy path test completed successfully");
        } catch (Exception e) {
            logger.error("[TEST_FAIL] [REQ-007][REQ-010][REQ-011][REQ-012][REQ-013] Happy path test failed with error: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Validates that a student cannot enroll in a course they are already enrolled in.
     * Edge case validation for enrollment uniqueness constraint.
     * 
     * Verification Tags:
     * - [REQ-010] Available courses filtering logic
     * - [REQ-011] Enrollment uniqueness constraint enforcement
     */
    @Test
    @Order(2)
    @DisplayName("Validate duplicate enrollment rejection")
    public void testDuplicateEnrollmentRejection() {
        logger.info("[TEST_START] [REQ-010][REQ-011] Executing duplicate enrollment rejection test");
        try {
            // First enrollment attempt (should succeed)
            Response firstEnrollResponse = RestAssured.given()
                    .header("Authorization", "Bearer " + studentAccessToken)
                    .contentType(ContentType.JSON)
                    .body("{\"courseId\":\"" + testCourseId + "\"}")
                    .post(ENROLLMENT_ENDPOINT);
            Assertions.assertEquals(201, firstEnrollResponse.getStatusCode(), "First enrollment should return 201 Created");

            // Second enrollment attempt (should fail with conflict)
            Response duplicateEnrollResponse = RestAssured.given()
                    .header("Authorization", "Bearer " + studentAccessToken)
                    .contentType(ContentType.JSON)
                    .body("{\"courseId\":\"" + testCourseId + "\"}")
                    .post(ENROLLMENT_ENDPOINT);
            Assertions.assertEquals(409, duplicateEnrollResponse.getStatusCode(), "Duplicate enrollment should return 409 Conflict");
            String errorCode = duplicateEnrollResponse.jsonPath().getString("error");
            Assertions.assertEquals(ERROR_ALREADY_ENROLLED, errorCode, "Error code should indicate student is already enrolled in the course");

            logger.info("[TEST_PASS] [REQ-010][REQ-011] Duplicate enrollment rejection test completed successfully");
        } catch (Exception e) {
            logger.error("[TEST_FAIL] [REQ-010][REQ-011] Duplicate enrollment test failed with error: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Validates that a student cannot scan QR for a course they are not enrolled in.
     * Edge case validation for attendance access control.
     * 
     * Verification Tags:
     * - [REQ-012] QR scan access control
     * - [REQ-013] Attendance enrollment validation
     */
    @Test
    @Order(3)
    @DisplayName("Validate QR scan rejection for non-enrolled student")
    public void testQRScanForNonEnrolledStudent() {
        logger.info("[TEST_START] [REQ-012][REQ-013] Executing QR scan rejection test for non-enrolled student");
        try {
            // Create a new student not enrolled in the test course
            String newStudentEmail = "new.student." + UUID.randomUUID() + "@test.com";
            Response newStudentRegisterResponse = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body("{\"email\":\"" + newStudentEmail + "\",\"password\":\"" + TEST_STUDENT_PASSWORD + "\",\"fullName\":\"New Test Student\",\"provider\":\"local\"}")
                    .post(AUTH_REGISTER_ENDPOINT);
            Assertions.assertEquals(201, newStudentRegisterResponse.getStatusCode(), "New student registration should succeed");
            String newStudentToken = newStudentRegisterResponse.jsonPath().getString("accessToken");

            // Attempt QR scan for course student is not enrolled in
            Response attendanceResponse = RestAssured.given()
                    .header("Authorization", "Bearer " + newStudentToken)
                    .contentType(ContentType.JSON)
                    .body("{\"qrCode\":\"" + TEST_QR_CODE_PAYLOAD + "\",\"timestamp\":" + System.currentTimeMillis() + "}")
                    .post(ATTENDANCE_SCAN_ENDPOINT);
            Assertions.assertEquals(403, attendanceResponse.getStatusCode(), "QR scan for non-enrolled student should return 403 Forbidden");
            String errorCode = attendanceResponse.jsonPath().getString("error");
            Assertions.assertEquals(ERROR_NOT_ENROLLED, errorCode, "Error code should indicate student is not enrolled in the course");

            logger.info("[TEST_PASS] [REQ-012][REQ-013] QR scan rejection test for non-enrolled student completed successfully");
        } catch (Exception e) {
            logger.error("[TEST_FAIL] [REQ-012][REQ-013] QR scan rejection test failed with error: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Validates that an invalid QR code is rejected by the attendance system.
     * Edge case validation for QR code format validation.
     * 
     * Verification Tags:
     * - [REQ-012] QR code format validation
     */
    @Test
    @Order(4)
    @DisplayName("Validate QR scan rejection for invalid QR code")
    public void testInvalidQRCodeScan() {
        logger.info("[TEST_START] [REQ-012] Executing invalid QR code scan test");
        try {
            Response attendanceResponse = RestAssured.given()
                    .header("Authorization", "Bearer " + studentAccessToken)
                    .contentType(ContentType.JSON)
                    .body("{\"qrCode\":\"invalid-qr-code-payload-format\",\"timestamp\":" + System.currentTimeMillis() + "}")
                    .post(ATTENDANCE_SCAN_ENDPOINT);
            Assertions.assertEquals(400, attendanceResponse.getStatusCode(), "Invalid QR scan should return 400 Bad Request");
            String errorCode = attendanceResponse.jsonPath().getString("error");
            Assertions.assertEquals(ERROR_INVALID_QR, errorCode, "Error code should indicate invalid QR code format");

            logger.info("[TEST_PASS] [REQ-012] Invalid QR code scan test completed successfully");
        } catch (Exception e) {
            logger.error("[TEST_FAIL] [REQ-012] Invalid QR code scan test failed with error: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Validates that course creation is rejected when teacher has a schedule conflict.
     * Exception case validation for schedule conflict handling [EXC-001].
     * 
     * Verification Tags:
     * - [REQ-008] Course creation with schedule conflict check
     * - [EXC-001] Schedule conflict exception handling
     */
    @Test
    @Order(5)
    @DisplayName("Validate course creation rejection for teacher schedule conflict")
    public void testCourseCreationWithScheduleConflict() {
        logger.info("[TEST_START] [REQ-008][EXC-001] Executing course creation with schedule conflict test");
        try {
            // Create first course for teacher with valid date range
            Response firstCourseResponse = RestAssured.given()
                    .header("Authorization", "Bearer " + adminAccessToken)
                    .contentType(ContentType.JSON)
                    .body("{\"title\":\"First Conflict Test Course\",\"startDate\":\"" + TEST_COURSE_START_DATE + "\",\"endDate\":\"" + TEST_COURSE_END_DATE + "\",\"teacherId\":\"" + testTeacherId + "\",\"maxStudents\":" + TEST_COURSE_MAX_STUDENTS + ",\"centerId\":\"" + testCenterId + "\"}")
                    .post(COURSE_LIST_ENDPOINT);
            Assertions.assertEquals(201, firstCourseResponse.getStatusCode(), "First course creation should succeed");

            // Attempt to create second course for same teacher with overlapping dates
            Response conflictCourseResponse = RestAssured.given()
                    .header("Authorization", "Bearer " + adminAccessToken)
                    .contentType(ContentType.JSON)
                    .body("{\"title\":\"Conflict Test Course\",\"startDate\":\"" + TEST_COURSE_START_DATE + "\",\"endDate\":\"" + TEST_COURSE_END_DATE + "\",\"teacherId\":\"" + testTeacherId + "\",\"maxStudents\":" + TEST_COURSE_MAX_STUDENTS + ",\"centerId\":\"" + testCenterId + "\"}")
                    .post(COURSE_LIST_ENDPOINT);
            Assertions.assertEquals(409, conflictCourseResponse.getStatusCode(), "Conflicting course creation should return 409 Conflict");
            String errorCode = conflictCourseResponse.jsonPath().getString("error");
            Assertions.assertEquals(ERROR_SCHEDULE_CONFLICT, errorCode, "Error code should indicate teacher schedule conflict");

            logger.info("[TEST_PASS] [REQ-008][EXC-001] Course creation with schedule conflict test completed successfully");
        } catch (Exception e) {
            logger.error("[TEST_FAIL] [REQ-008][EXC-001] Course creation conflict test failed with error: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Validates that enrollment is rejected when a course has reached maximum capacity.
     * Edge case validation for course capacity constraints.
     * 
     * Verification Tags:
     * - [REQ-011] Enrollment capacity validation
     */
    @Test
    @Order(6)
    @DisplayName("Validate enrollment rejection for full course")
    public void testEnrollmentInFullCourse() {
        logger.info("[TEST_START] [REQ-011] Executing enrollment in full course test");
        try {
            // Create a course with maximum capacity of 1 student
            Response smallCourseResponse = RestAssured.given()
                    .header("Authorization", "Bearer " + adminAccessToken)
                    .contentType(ContentType.JSON)
                    .body("{\"title\":\"Small Capacity Course\",\"startDate\":\"" + TEST_COURSE_START_DATE + "\",\"endDate\":\"" + TEST_COURSE_END_DATE + "\",\"teacherId\":\"" + testTeacherId + "\",\"maxStudents\":1,\"centerId\":\"" + testCenterId + "\"}")
                    .post(COURSE_LIST_ENDPOINT);
            Assertions.assertEquals(201, smallCourseResponse.getStatusCode(), "Small capacity course creation should succeed");
            UUID smallCourseId = smallCourseResponse.jsonPath().getUUID("courseId");

            // Enroll first student (should succeed)
            Response firstEnrollResponse = RestAssured.given()
                    .header("Authorization", "Bearer " + studentAccessToken)
                    .contentType(ContentType.JSON)
                    .body("{\"courseId\":\"" + smallCourseId + "\"}")
                    .post(ENROLLMENT_ENDPOINT);
            Assertions.assertEquals(201, firstEnrollResponse.getStatusCode(), "First enrollment in small course should succeed");

            // Create second student and attempt enrollment (should fail)
            String secondStudentEmail = "second.student." + UUID.randomUUID() + "@test.com";
            Response secondStudentRegisterResponse = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body("{\"email\":\"" + secondStudentEmail + "\",\"password\":\"" + TEST_STUDENT_PASSWORD + "\",\"fullName\":\"Second Test Student\",\"provider\":\"local\"}")
                    .post(AUTH_REGISTER_ENDPOINT);
            Assertions.assertEquals(201, secondStudentRegisterResponse.getStatusCode(), "Second student registration should succeed");
            String secondStudentToken = secondStudentRegisterResponse.jsonPath().getString("accessToken");

            Response secondEnrollResponse = RestAssured.given()
                    .header("Authorization", "Bearer " + secondStudentToken)
                    .contentType(ContentType.JSON)
                    .body("{\"courseId\":\"" + smallCourseId + "\"}")
                    .post(ENROLLMENT_ENDPOINT);
            Assertions.assertEquals(409, secondEnrollResponse.getStatusCode(), "Enrollment in full course should return 409 Conflict");
            String errorCode = secondEnrollResponse.jsonPath().getString("error");
            Assertions.assertEquals(ERROR_ENROLLMENT_FULL, errorCode, "Error code should indicate course has reached maximum capacity");

            logger.info("[TEST_PASS] [REQ-011] Enrollment in full course test completed successfully");
        } catch (Exception e) {
            logger.error("[TEST_FAIL] [REQ-011] Enrollment in full course test failed with error: {}", e.getMessage());
            throw e;
        }
    }
}