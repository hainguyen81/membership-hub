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