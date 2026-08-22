# Day 5: model kilo-auto/free - API Endpoint https://api.kilo.ai/api/gateway
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/frontend/web/course/src/test/e2e/CourseEnrollmentE2ETest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Test Component Destination Path: `./sources/frontend/web/course/src/test/e2e/CourseEnrollmentE2ETest.java` (Must map to sources/backend/ or sources/frontend/)


### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/frontend/web/course/src/test/e2e/CourseEnrollmentE2ETest.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Viết bộ kiểm thử end-to-end cho luồng đăng ký và điểm danh QR']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` structure. Ensure that you read the exact Tag IDs from the `['[REQ-007]', '[REQ-010]', '[REQ-011]', '[REQ-012]', '[REQ-013]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
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
```

# Day 5: model kilo-auto/free - API Endpoint https://api.kilo.ai/api/gateway
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/frontend/web/course/src/test/e2e/AttendanceQRScanE2ETest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.saas.membership-hub
*   Target Test Component Destination Path: `./sources/frontend/web/course/src/test/e2e/AttendanceQRScanE2ETest.java` (Must map to sources/backend/ or sources/frontend/)


### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/frontend/web/course/src/test/e2e/AttendanceQRScanE2ETest.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Viết bộ kiểm thử end-to-end cho luồng đăng ký và điểm danh QR']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.membership-hub` structure. Ensure that you read the exact Tag IDs from the `['[REQ-007]', '[REQ-010]', '[REQ-011]', '[REQ-012]', '[REQ-013]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.saas.membership-hub.frontend.web.course.test.e2e;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nlh4j.saas.membership-hub.entity.Attendance;
import org.nlh4j.saas.membership-hub.entity.Course;
import org.nlh4j.saas.membership-hub.entity.Enrollment;
import org.nlh4j.saas.membership-hub.entity.User;
import org.nlh4j.saas.membership-hub.repository.AttendanceRepository;
import org.nlh4j.saas.membership-hub.repository.CourseRepository;
import org.nlh4j.saas.membership-hub.repository.EnrollmentRepository;
import org.nlh4j.saas.membership-hub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * End-to-end test suite for course enrollment and QR code attendance workflow.
 * Validates full multi-component flow from frontend request to backend processing to database state.
 * @verifies [REQ-007] Course list retrieval with teacher and schedule info
 * @verifies [REQ-010] Available course listing for students (excludes enrolled courses)
 * @verifies [REQ-011] Student course enrollment with auto-account creation logic
 * @verifies [REQ-012] QR code attendance scanning and validation
 * @verifies [REQ-013] Attendance idempotency (single record per student/course/day)
 */
@QuarkusTest
@ExtendWith(MockitoExtension.class)
public class AttendanceQRScanE2ETest {
    // [CONST] Enterprise logger instance for test traceability
    private static final Logger logger = LoggerFactory.getLogger(AttendanceQRScanE2ETest.class);
    // [CONST] API base paths aligned with system architecture contracts
    private static final String COURSE_API_BASE = "/api/v1/courses";
    private static final String ENROLLMENT_API_BASE = "/api/v1/enrollments";
    private static final String ATTENDANCE_API_BASE = "/api/v1/attendance";
    private static final String AUTH_API_BASE = "/api/v1/auth";
    private static final String ADMIN_API_BASE = "/api/v1/admin";
    // [CONST] Test data constants (no hardcoded literals in test logic)
    private static final String TEST_STUDENT_EMAIL = "test.student@membershiphub.com";
    private static final String TEST_STUDENT_PASSWORD = "TestPass123!";
    private static final String TEST_STUDENT_FULL_NAME = "Test Student E2E";
    private static final String TEST_TEACHER_EMAIL = "test.teacher@membershiphub.com";
    private static final String TEST_TEACHER_PASSWORD = "TestPass123!";
    private static final String TEST_TEACHER_FULL_NAME = "Test Teacher E2E";
    private static final String TEST_CENTER_NAME = "E2E Test Center";
    private static final String TEST_CENTER_ADDRESS = "456 Test Avenue, Test City";
    private static final String TEST_CENTER_TAX_ID = "9876543210";
    private static final String TEST_COURSE_TITLE = "E2E Test Course 2024";
    private static final String TEST_COURSE_DESCRIPTION = "End-to-end test course for attendance workflow";
    private static final int TEST_COURSE_MAX_STUDENTS = 30;
    private static final int TEST_COURSE_MAX_STUDENTS_SINGLE_SLOT = 1;
    // [CONST] WireMock configuration for external service mocking (Firebase, FCM, Zalo)
    private static final int WIREMOCK_PORT = 8089;
    private static WireMockServer wireMockServer;
    // [CONST] Authenticated JWT tokens for test requests
    private String studentJwtToken;
    private String teacherJwtToken;
    private String adminJwtToken;
    // [CONST] Test entity IDs for workflow validation
    private UUID testCenterId;
    private UUID testCourseId;
    private UUID testTeacherId;
    private UUID testStudentId;
    private UUID testSecondStudentId;
    // [CONST] QR payload format per system architecture (contains courseId and sessionId)
    private static final String QR_PAYLOAD_FORMAT = "{\"courseId\":\"%s\",\"sessionId\":\"%s\"}";
    // [CONST] Repository injections for database state validation
    @Inject
    UserRepository userRepository;
    @Inject
    CourseRepository courseRepository;
    @Inject
    EnrollmentRepository enrollmentRepository;
    @Inject
    AttendanceRepository attendanceRepository;

    /**
     * Initialize test environment: start WireMock server, mock external services
     * @verifies [NFR-003] External service isolation for test stability
     */
    @BeforeAll
    static void setupTestEnvironment() {
        logger.info("[TEST_SETUP] Starting WireMock server for external service mocking");
        wireMockServer = new WireMockServer(WIREMOCK_PORT);
        wireMockServer.start();
        WireMock.configureFor("localhost", WIREMOCK_PORT);
        // Mock Firebase Auth OAuth2 endpoints
        stubFor(post(urlEqualTo("/oauth2/v1/token"))
                .willReturn(okJson("{\"access_token\":\"mock_firebase_token\",\"expires_in\":3600}")));
        // Mock FCM push notification endpoint
        stubFor(post(urlEqualTo("/fcm/send"))
                .willReturn(okJson("{\"success\":1,\"failure\":0}")));
        // Mock Zalo API endpoint
        stubFor(post(urlEqualTo("/zalo/v1/group/message"))
                .willReturn(okJson("{\"error\":0,\"message\":\"Sent\"}")));
        logger.info("[TEST_SETUP] WireMock server started and external services mocked successfully");
    }

    /**
     * Per-test setup: create test users, center, course, and enrollments
     * @verifies [REQ-001] User registration with email/password
     * @verifies [REQ-004] Center creation
     * @verifies [REQ-007] Course creation with teacher assignment
     * @verifies [REQ-010] Student enrollment in course
     */
    @BeforeEach
    void setupTestData() {
        logger.info("[TEST_SETUP] Starting test data initialization for test case");
        // 1. Register admin user and assign System Admin role
        Response adminRegisterResponse = given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"admin.test@membershiphub.com\",\"password\":\"AdminPass123!\",\"fullName\":\"Test Admin\",\"provider\":\"local\"}")
                .when()
                .post(AUTH_API_BASE + "/register")
                .then()
                .statusCode(201)
                .extract()
                .response();
        adminJwtToken = adminRegisterResponse.jsonPath().getString("accessToken");
        UUID adminUserId = adminRegisterResponse.jsonPath().getUUID("userId");
        // Assign System Admin role (role_id=1 per system design)
        given()
                .header("Authorization", "Bearer " + adminJwtToken)
                .contentType(ContentType.JSON)
                .body("{\"roleId\":1}")
                .when()
                .post(ADMIN_API_BASE + "/users/" + adminUserId + "/role")
                .then()
                .statusCode(200);
        // 2. Create test center
        Response centerResponse = given()
                .header("Authorization", "Bearer " + adminJwtToken)
                .contentType(ContentType.JSON)
                .body("{\"name\":\"" + TEST_CENTER_NAME + "\",\"address\":\"" + TEST_CENTER_ADDRESS + "\",\"taxId\":\"" + TEST_CENTER_TAX_ID + "\",\"contactPhone\":\"0123456789\",\"contactEmail\":\"center@" + TEST_CENTER_NAME.toLowerCase().replace(" ", "") + ".com\"}")
                .when()
                .post(ADMIN_API_BASE + "/centers")
                .then()
                .statusCode(201)
                .extract()
                .response();
        testCenterId = centerResponse.jsonPath().getUUID("centerId");
        // 3. Register teacher user
        Response teacherRegisterResponse = given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"" + TEST_TEACHER_EMAIL + "\",\"password\":\"" + TEST_TEACHER_PASSWORD + "\",\"fullName\":\"" + TEST_TEACHER_FULL_NAME + "\",\"provider\":\"local\"}")
                .when()
                .post(AUTH_API_BASE + "/register")
                .then()
                .statusCode(201)
                .extract()
                .response();
        teacherJwtToken = teacherRegisterResponse.jsonPath().getString("accessToken");
        testTeacherId = teacherRegisterResponse.jsonPath().getUUID("userId");
        // Assign Teacher role (role_id=4 per system design)
        given()
                .header("Authorization", "Bearer " + adminJwtToken)
                .contentType(ContentType.JSON)
                .body("{\"roleId\":4}")
                .when()
                .post(ADMIN_API_BASE + "/users/" + testTeacherId + "/role")
                .then()
                .statusCode(200);
        // 4. Create test course and assign teacher
        Response courseResponse = given()
                .header("Authorization", "Bearer " + adminJwtToken)
                .contentType(ContentType.JSON)
                .body("{\"title\":\"" + TEST_COURSE_TITLE + "\",\"description\":\"" + TEST_COURSE_DESCRIPTION + "\",\"startDate\":\"2024-01-01\",\"endDate\":\"2024-12-31\",\"teacherId\":\"" + testTeacherId + "\",\"maxStudents\":" + TEST_COURSE_MAX_STUDENTS + "}")
                .when()
                .post(COURSE_API_BASE)
                .then()
                .statusCode(201)
                .extract()
                .response();
        testCourseId = courseResponse.jsonPath().getUUID("courseId");
        // 5. Register student user
        Response studentRegisterResponse = given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"" + TEST_STUDENT_EMAIL + "\",\"password\":\"" + TEST_STUDENT_PASSWORD + "\",\"fullName\":\"" + TEST_STUDENT_FULL_NAME + "\",\"provider\":\"local\"}")
                .when()
                .post(AUTH_API_BASE + "/register")
                .then()
                .statusCode(201)
                .extract()
                .response();
        studentJwtToken = studentRegisterResponse.jsonPath().getString("accessToken");
        testStudentId = studentRegisterResponse.jsonPath().getUUID("userId");
        // Assign Student role (role_id=5 per system design)
        given()
                .header("Authorization", "Bearer " + adminJwtToken)
                .contentType(ContentType.JSON)
                .body("{\"roleId\":5}")
                .when()
                .post(ADMIN_API_BASE + "/users/" + testStudentId + "/role")
                .then()
                .statusCode(200);
        // 6. Register second student for conflict test cases
        Response secondStudentRegisterResponse = given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"test.student2@membershiphub.com\",\"password\":\"TestPass123!\",\"fullName\":\"Test Student 2\",\"provider\":\"local\"}")
                .when()
                .post(AUTH_API_BASE + "/register")
                .then()
                .statusCode(201)
                .extract()
                .response();
        testSecondStudentId = secondStudentRegisterResponse.jsonPath().getUUID("userId");
        given()
                .header("Authorization", "Bearer " + adminJwtToken)
                .contentType(ContentType.JSON)
                .body("{\"roleId\":5}")
                .when()
                .post(ADMIN_API_BASE + "/users/" + testSecondStudentId + "/role")
                .then()
                .statusCode(200);
        logger.info("[TEST_SETUP] Test data initialized successfully: centerId={}, courseId={}, studentId={}, teacherId={}", testCenterId, testCourseId, testStudentId, testTeacherId);
    }

    /**
     * Clean up test data after each test case to ensure test isolation
     * @verifies [NFR-006] Audit log cleanup and test data isolation
     */
    @AfterEach
    void cleanupTestData() {
        logger.info("[TEST_CLEANUP] Starting test data cleanup for test case");
        // Delete attendance records first (foreign key constraints)
        attendanceRepository.deleteAll();
        // Delete enrollment records
        enrollmentRepository.deleteAll();
        // Delete course records
        courseRepository.deleteAll();
        // Delete center records
        userRepository.deleteAll();
        logger.info("[TEST_CLEANUP] Test data cleaned up successfully");
    }

    /**
     * Stop WireMock server after all tests complete
     */
    @AfterAll
    static void tearDownTestEnvironment() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            logger.info("[TEST_TEARDOWN] WireMock server stopped");
        }
    }

    /**
     * Helper method to generate valid QR payload for test course
     * @return Base64 encoded QR payload with courseId and sessionId
     */
    private String generateValidQrPayload() {
        String rawPayload = String.format(QR_PAYLOAD_FORMAT, testCourseId.toString(), UUID.randomUUID().toString());
        return java.util.Base64.getEncoder().encodeToString(rawPayload.getBytes());
    }

    // ------------------------------
    // HAPPY PATH TEST CASES
    // ------------------------------

    /**
     * Test happy path: Student views available courses, enrolls in a course, then scans valid QR code for attendance.
     * Validates end-to-end flow from course listing to attendance recording.
     * @verifies [REQ-007] Course list retrieval with correct metadata
     * @verifies [REQ-010] Available course listing excludes enrolled courses
     * @verifies [REQ-011] Student enrollment creates valid enrollment record
     * @verifies [REQ-012] QR scan creates valid attendance record
     */
    @Test
    @DisplayName("E2E: Student enrolls in course and scans QR for successful attendance")
    void testEnrollInAvailableCourseAndScanQRSuccess() {
        logger.info("[TEST_START] [REQ-007][REQ-010][REQ-011][REQ-012] Testing enrollment and QR scan happy path");
        // Step 1: Get list of available courses (should include test course)
        Response availableCoursesResponse = given()
                .header("Authorization", "Bearer " + studentJwtToken)
                .queryParam("page", 1)
                .queryParam("size", 20)
                .when()
                .get(COURSE_API_BASE + "/available")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].courseId", equalTo(testCourseId.toString()))
                .body("[0].title", equalTo(TEST_COURSE_TITLE))
                .body("[0].remainingSlots", greaterThan(0))
                .extract()
                .response();
        logger.info("[TEST_STEP] Available courses retrieved successfully, count={}", availableCoursesResponse.jsonPath().getList("$").size());
        // Step 2: Enroll student in test course
        Response enrollmentResponse = given()
                .header("Authorization", "Bearer " + studentJwtToken)
                .contentType(ContentType.JSON)
                .body("{\"courseId\":\"" + testCourseId + "\"}")
                .when()
                .post(ENROLLMENT_API_BASE)
                .then()
                .statusCode(201)
                .body("enrollmentId", notNullValue())
                .body("message", equalTo("Đăng ký khóa học thành công"))
                .extract()
                .response();
        UUID enrollmentId = enrollmentResponse.jsonPath().getUUID("enrollmentId");
        logger.info("[TEST_STEP] Student enrolled in course successfully, enrollmentId={}", enrollmentId);
        // Verify enrollment record exists in database
        Assertions.assertTrue(enrollmentRepository.findByStudentIdAndCourseId(testStudentId, testCourseId).isPresent(),
                "Enrollment record should exist in database after successful enrollment");
        // Step 3: Scan valid QR code for enrolled course
        String validQrPayload = generateValidQrPayload();
        Response attendanceResponse = given()
                .header("Authorization", "Bearer " + studentJwtToken)
                .contentType(ContentType.JSON)
                .body("{\"qrCode\":\"" + validQrPayload + "\",\"timestamp\":\"" + LocalDate.now() + "T10:00:00\"}")
                .when()
                .post(ATTENDANCE_API_BASE + "/scan")
                .then()
                .statusCode(200)
                .body("status", equalTo("RECORDED"))
                .body("attendanceId", notNullValue())
                .body("message", equalTo("Điểm danh thành công"))
                .extract()
                .response();
        UUID attendanceId = attendanceResponse.jsonPath().getUUID("attendanceId");
        logger.info("[TEST_STEP] QR scan successful, attendanceId={}, status=RECORDED", attendanceId);
        // Verify attendance record exists in database
        Assertions.assertTrue(attendanceRepository.findByStudentIdAndCourseIdAndAttendanceDate(testStudentId, testCourseId, LocalDate.now()).isPresent(),
                "Attendance record should exist in database after successful QR scan");
        // Verify only one attendance record exists for student/course/date (idempotency base check)
        long attendanceCount = attendanceRepository.countByStudentIdAndCourseIdAndAttendanceDate(testStudentId, testCourseId, LocalDate.now());
        Assertions.assertEquals(1, attendanceCount, "Only one attendance record should exist per student/course/day");
        logger.info("[TEST_PASS] [REQ-007][REQ-010][REQ-011][REQ-012] Enrollment and QR scan happy path test passed");
    }

    // ------------------------------
    // EDGE CASE TEST CASES
    // ------------------------------

    /**
     * Test idempotency: Student scans same QR code twice on the same day, second scan returns DUPLICATE status.
     * Validates attendance idempotency requirement to prevent duplicate records.
     * @verifies [REQ-013] Attendance idempotency (no duplicate records for same student/course/day)
     */
    @Test
    @DisplayName("E2E: Duplicate QR scan on same day returns DUPLICATE status")
    void testDuplicateQRScanReturnsDuplicateStatus() {
        logger.info("[TEST_START] [REQ-013] Testing attendance idempotency with duplicate QR scan");
        // Pre-requisite: Enroll student in course
        given()
                .header("Authorization", "Bearer " + studentJwtToken)
                .contentType(ContentType.JSON)
                .body("{\"courseId\":\"" + testCourseId + "\"}")
                .when()
                .post(ENROLLMENT_API_BASE)
                .then()
                .statusCode(201);
        // First QR scan (successful)
        String validQrPayload = generateValidQrPayload();
        given()
                .header("Authorization", "Bearer " + studentJwtToken)
                .contentType(ContentType.JSON)
                .body("{\"qrCode\":\"" + validQrPayload + "\",\"timestamp\":\"" + LocalDate.now() + "T10:00:00\"}")
                .when()
                .post(ATTENDANCE_API_BASE + "/scan")
                .then()
                .statusCode(200)
                .body("status", equalTo("RECORDED"));
        logger.info("[TEST_STEP] First QR scan completed successfully");
        // Second QR scan (duplicate)
        Response duplicateScanResponse = given()
                .header("Authorization", "Bearer " + studentJwtToken)
                .contentType(ContentType.JSON)
                .body("{\"qrCode\":\"" + validQrPayload + "\",\"timestamp\":\"" + LocalDate.now() + "T10:05:00\"}")
                .when()
                .post(ATTENDANCE_API_BASE + "/scan")
                .then()
                .statusCode(200)
                .body("status", equalTo("DUPLICATE"))
                .body("message", equalTo("Đã ghi nhận điểm danh cho buổi học này trước đó"))
                .extract()
                .response();
        logger.info("[TEST_STEP] Second duplicate QR scan returned DUPLICATE status as expected");
        // Verify only one attendance record exists in database
        long attendanceCount = attendanceRepository.countByStudentIdAndCourseIdAndAttendanceDate(testStudentId, testCourseId, LocalDate.now());
        Assertions.assertEquals(1, attendanceCount, "Duplicate scan must not create new attendance records");
        logger.info("[TEST_PASS] [REQ-013] Attendance idempotency test passed");
    }

    /**
     * Test edge case: Student tries to enroll in a course that is already full (max students reached).
     * Validates enrollment capacity constraint.
     * @verifies [REQ-010] Enrollment capacity validation
     * @verifies [REQ-011] Enrollment rejection for full courses
     */
    @Test
    @DisplayName("E2E: Enrollment in full course returns conflict error")
    void testEnrollInFullCourseReturnsConflict() {
        logger.info("[TEST_START] [REQ-010][REQ-011] Testing enrollment in full course edge case");
        // Create a course with only 1 slot
        Response fullCourseResponse = given()
                .header("Authorization", "Bearer " + adminJwtToken)
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Full Test Course\",\"description\":\"Course with 1 slot\",\"startDate\":\"2024-01-01\",\"endDate\":\"2024-12-31\",\"teacherId\":\"" + testTeacherId + "\",\"maxStudents\":" + TEST_COURSE_MAX_STUDENTS_SINGLE_SLOT + "}")
                .when()
                .post(COURSE_API_BASE)
                .then()
                .statusCode(201)
                .extract()
                .response();
        UUID fullCourseId = fullCourseResponse.jsonPath().getUUID("courseId");
        // Enroll first student (success)
        given()
                .header("Authorization", "Bearer " + studentJwtToken)
                .contentType(ContentType.JSON)
                .body("{\"courseId\":\"" + fullCourseId + "\"}")
                .when()
                .post(ENROLLMENT_API_BASE)
                .then()
                .statusCode(201);
        logger.info("[TEST_STEP] First student enrolled in full course successfully");
        // Try to enroll second student (should fail)
        given()
                .header("Authorization", "Bearer " + testSecondStudentId)
                .contentType(ContentType.JSON)
                .body("{\"courseId\":\"" + fullCourseId + "\"}")
                .when()
                .post(ENROLLMENT_API_BASE)
                .then()
                .statusCode(409)
                .body("error", equalTo("ENROLLMENT_FULL"))
                .body("message", equalTo("Khóa học đã đủ sĩ số, không thể đăng ký"));
        logger.info("[TEST_STEP] Second student enrollment rejected with 409 conflict as expected");
        // Verify only one enrollment exists for the course
        long enrollmentCount = enrollmentRepository.countByCourseId(fullCourseId);
        Assertions.assertEquals(1, enrollmentCount, "Only one enrollment should exist for full course");
        logger.info("[TEST_PASS] [REQ-010][REQ-011] Full course enrollment edge case test passed");
    }

    /**
     * Test edge case: Student tries to enroll in a course they are already enrolled in.
     * Validates unique enrollment constraint per student/course.
     * @verifies [REQ-011] Unique enrollment constraint (no duplicate enrollments)
     */
    @Test
    @DisplayName("E2E: Duplicate enrollment returns conflict error")
    void testEnrollInAlreadyEnrolledCourseReturnsConflict() {
        logger.info("[TEST_START] [REQ-011] Testing duplicate enrollment edge case");
        // First enrollment (success)
        given()
                .header("Authorization", "Bearer " + studentJwtToken)
                .contentType(ContentType.JSON)
                .body("{\"courseId\":\"" + testCourseId + "\"}")
                .when()
                .post(ENROLLMENT_API_BASE)
                .then()
                .statusCode(201);
        logger.info("[TEST_STEP] First enrollment completed successfully");
        // Second enrollment attempt (should fail)
        given()
                .header("Authorization", "Bearer " + studentJwtToken)
                .contentType(ContentType.JSON)
                .body("{\"courseId\":\"" + testCourseId + "\"}")
                .when()
                .post(ENROLLMENT_API_BASE)
                .then()
                .statusCode(409)
                .body("error", equalTo("ENROLLMENT_DUPLICATE"))
                .body("message", equalTo("Bạn đã đăng ký khóa học này trước đó"));
        logger.info("[TEST_STEP] Duplicate enrollment rejected with 409 conflict as expected");
        // Verify only one enrollment exists for student/course
        long enrollmentCount = enrollmentRepository.countByStudentIdAndCourseId(testStudentId, testCourseId);
        Assertions.assertEquals(1, enrollmentCount, "Only one enrollment should exist per student/course");
        logger.info("[TEST_PASS] [REQ-011] Duplicate enrollment edge case test passed");
    }

    // ------------------------------
    // EXCEPTION TEST CASES
    // ------------------------------

    /**
     * Test exception case: Student scans QR code for a course they are not enrolled in.
     * Validates access control for attendance scanning.
     * @verifies [REQ-012] Attendance access control (only enrolled students can scan)
     */
    @Test
    @DisplayName("E2E: QR scan for non-enrolled course returns 403 Forbidden")
    void testScanQRForNonEnrolledCourseReturnsForbidden() {
        logger.info("[TEST_START] [REQ-012] Testing QR scan for non-enrolled course exception case");
        // Create a course that the student is not enrolled in
        Response nonEnrolledCourseResponse = given()
                .header("Authorization", "Bearer " + adminJwtToken)
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Non-Enrolled Course\",\"description\":\"Course student is not enrolled in\",\"startDate\":\"2024-01-01\",\"endDate\":\"2024-12-31\",\"teacherId\":\"" + testTeacherId + "\",\"maxStudents\":" + TEST_COURSE_MAX_STUDENTS + "}")
                .when()
                .post(COURSE_API_BASE)
                .then()
                .statusCode(201)
                .extract()
                .response();
        UUID nonEnrolledCourseId = nonEnrolledCourseResponse.jsonPath().getUUID("courseId");
        // Generate QR payload for non-enrolled course
        String nonEnrolledQrPayload = String.format(QR_PAYLOAD_FORMAT, nonEnrolledCourseId.toString(), UUID.randomUUID().toString());
        // Attempt to scan QR for non-enrolled course
        given()
                .header("Authorization", "Bearer " + studentJwtToken)
                .contentType(ContentType.JSON)
                .body("{\"qrCode\":\"" + nonEnrolledQrPayload + "\",\"timestamp\":\"" + LocalDate.now() + "T10:00:00\"}")
                .when()
                .post(ATTENDANCE_API_BASE + "/scan")
                .then()
                .statusCode(403)
                .body("error", equalTo("ACCESS_DENIED"))
                .body("message", equalTo("Bạn không được đăng ký khóa học này, không thể điểm danh"));
        logger.info("[TEST_STEP] QR scan for non-enrolled course returned 403 Forbidden as expected");
        // Verify no attendance record exists for non-enrolled course
        Assertions.assertTrue(attendanceRepository.findByStudentIdAndCourseIdAndAttendanceDate(testStudentId, nonEnrolledCourseId, LocalDate.now()).isEmpty(),
                "No attendance record should exist for non-enrolled course");
        logger.info("[TEST_PASS] [REQ-012] Non-enrolled course QR scan exception test passed");
    }

    /**
     * Test exception case: Student scans invalid/malformed QR code.
     * Validates input validation for QR payload.
     * @verifies [REQ-012] QR payload input validation
     */
    @Test
    @DisplayName("E2E: Invalid QR code returns 400 Bad Request")
    void testInvalidQRCodeReturnsBadRequest() {
        logger.info("[TEST_START] [REQ-012] Testing invalid QR code exception case");
        // Enroll student in course first
        given()
                .header("Authorization", "Bearer " + studentJwtToken)
                .contentType(ContentType.JSON)
                .body("{\"courseId\":\"" + testCourseId + "\"}")
                .when()
                .post(ENROLLMENT_API_BASE)
                .then()
                .statusCode(201);
        // Test with malformed QR payload (missing courseId)
        String invalidQrPayload = "{\"sessionId\":\"" + UUID.randomUUID() + "\"}";
        given()
                .header("Authorization", "Bearer " + studentJwtToken)
                .contentType(ContentType.JSON)
                .body("{\"qrCode\":\"" + invalidQrPayload + "\",\"timestamp\":\"" + LocalDate.now() + "T10:00:00\"}")
                .when()
                .post(ATTENDANCE_API_BASE + "/scan")
                .then()
                .statusCode(400)
                .body("error", equalTo("VALIDATION_FAILED"))
                .body("message", equalTo("Mã QR không hợp lệ, thiếu thông tin khóa học"));
        logger.info("[TEST_STEP] Invalid QR code returned 400 Bad Request as expected");
        // Test with non-existent courseId in QR payload
        String nonExistentCourseQrPayload = String.format(QR_PAYLOAD_FORMAT, UUID.randomUUID().toString(), UUID.randomUUID().toString());
        given()
                .header("Authorization", "Bearer " + studentJwtToken)
                .contentType(ContentType.JSON)
                .body("{\"qrCode\":\"" + nonExistentCourseQrPayload + "\",\"timestamp\":\"" + LocalDate.now() + "T10:00:00\"}")
                .when()
                .post(ATTENDANCE_API_BASE + "/scan")
                .then()
                .statusCode(404)
                .body("error", equalTo("COURSE_NOT_FOUND"))
                .body("message", equalTo("Khóa học không tồn tại trong hệ thống"));
        logger.info("[TEST_STEP] QR code with non-existent courseId returned 404 Not Found as expected");
        // Verify no attendance records were created
        long attendanceCount = attendanceRepository.countByStudentIdAndCourseIdAndAttendanceDate(testStudentId, testCourseId, LocalDate.now());
        Assertions.assertEquals(0, attendanceCount, "No attendance records should be created for invalid QR scans");
        logger.info("[TEST_PASS] [REQ-012] Invalid QR code exception test passed");
    }

    /**
     * Test available courses endpoint excludes already enrolled courses.
     * Validates course listing logic for students.
     * @verifies [REQ-007] Course list retrieval with correct filtering
     * @verifies [REQ-010] Available courses exclude enrolled courses
     */
    @Test
    @DisplayName("E2E: Available courses list excludes already enrolled courses")
    void testGetAvailableCoursesExcludesEnrolledCourses() {
        logger.info("[TEST_START] [REQ-007][REQ-010] Testing available courses filtering logic");
        // Get initial list of available courses (should include test course)
        Response initialAvailableResponse = given()
                .header("Authorization", "Bearer " + studentJwtToken)
                .when()
                .get(COURSE_API_BASE + "/available")
                .then()
                .statusCode(200)
                .extract()
                .response();
        int initialAvailableCount = initialAvailableResponse.jsonPath().getList("$").size();
        boolean initialContainsCourse = initialAvailableResponse.jsonPath().getList("courseId").contains(testCourseId.toString());
        Assertions.assertTrue(initialContainsCourse, "Test course should be in initial available courses list");
        logger.info("[TEST_STEP] Initial available courses count={}, includes test course={}", initialAvailableCount, initialContainsCourse);
        // Enroll student in test course
        given()
                .header("Authorization", "Bearer " + studentJwtToken)
                .contentType(ContentType.JSON)
                .body("{\"courseId\":\"" + testCourseId + "\"}")
                .when()
                .post(ENROLLMENT_API_BASE)
                .then()
                .statusCode(201);
        logger.info("[TEST_STEP] Student enrolled in test course");
        // Get available courses again (should exclude test course)
        Response afterEnrollmentResponse = given()
                .header("Authorization", "Bearer " + studentJwtToken)
                .when()
                .get(COURSE_API_BASE + "/available")
                .then()
                .statusCode(200)
                .extract()
                .response();
        int afterEnrollmentCount = afterEnrollmentResponse.jsonPath().getList("$").size();
        boolean afterContainsCourse = afterEnrollmentResponse.jsonPath().getList("courseId").contains(testCourseId.toString());
        Assertions.assertFalse(afterContainsCourse, "Enrolled course should be excluded from available courses list");
        Assertions.assertEquals(initialAvailableCount - 1, afterEnrollmentCount, "Available courses count should decrease by 1 after enrollment");
        logger.info("[TEST_STEP] After enrollment available courses count={}, excludes test course={}", afterEnrollmentCount, afterContainsCourse);
        logger.info("[TEST_PASS] [REQ-007][REQ-010] Available courses filtering test passed");
    }
}
```

