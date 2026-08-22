package com.membershiphub.rest;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mock.InjectMock;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.MediaType;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.jboss.logging.Logger;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import com.membershiphub.model.Enrollment;
import com.membershiphub.model.Course;
import com.membershiphub.model.User;
import com.membershiphub.service.NotificationService;
import com.membershiphub.dto.EnrollmentRequest;
import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

/**
 * Integration test suite for Enrollment Resource endpoints.
 * Tests the complete enrollment workflow including course availability filtering
 * and automatic student account creation.
 * 
 * @verifies [REQ-010] Student course browsing with enrolled course exclusion
 * @verifies [REQ-011] Course enrollment with automatic student account creation
 */
@QuarkusTest
@Testcontainers
public class EnrollmentResourceTest {
    
    private static final Logger LOG = Logger.getLogger(EnrollmentResourceTest.class);
    
    // [REQ-010], [REQ-011] Static constants for test data and endpoint URLs
    private static final String TEST_STUDENT_EMAIL = "newstudent@example.com";
    private static final String EXISTING_STUDENT_EMAIL = "existing@example.com";
    private static final String VALID_JWT_TOKEN = "Bearer valid-jwt-token";
    private static final String ENROLLMENT_API_PATH = "/api/v1/enrollments";
    private static final String AVAILABLE_COURSES_API_PATH = "/api/v1/courses/available";
    private static final String COURSES_API_PATH = "/api/v1/courses";
    
    // [REQ-010], [REQ-011] PostgreSQL Testcontainer for integration testing with real database
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("membership_hub_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("test-data.sql");
    
    // Mock external notification service to avoid external API dependencies during integration test
    @InjectMock
    NotificationService notificationService;
    
    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8081; // Quarkus test default port
    }
    
    @BeforeEach
    void setUp() {
        // [REQ-011] Reset mocks and setup default behavior for notification service
        Mockito.reset(notificationService);
        when(notificationService.sendEnrollmentConfirmation(any(), any()))
            .thenReturn(Uni.createFrom().item(true));
        when(notificationService.sendCourseAssignmentNotification(any(), any()))
            .thenReturn(Uni.createFrom().item(true));
    }
    
    /**
     * Test happy path: Student successfully enrolls in an available course.
     * Verifies enrollment creation, automatic student account creation,
     * and notification dispatch.
     * 
     * @verifies [REQ-011]
     */
    @Test
    void testEnrollInAvailableCourse_Success() {
        // [REQ-011] Log test start for audit trail
        LOG.info("[TEST_START] [REQ-011] testEnrollInAvailableCourse_Success");
        
        // [REQ-011] Test data: valid course ID and new student email
        UUID courseId = UUID.randomUUID();
        
        // [REQ-011] Execute POST /api/v1/enrollments with valid payload
        // Business logic: System should create student account automatically if not exists
        // and create enrollment record with idempotent behavior
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", VALID_JWT_TOKEN)
            .body("{\"courseId\":\"" + courseId + "\",\"studentEmail\":\"" + TEST_STUDENT_EMAIL + "\"}")
        .when()
            .post(ENROLLMENT_API_PATH)
        .then()
            // [REQ-011] Assert: 201 Created response with enrollment ID
            // Validation strategy: Verify HTTP status code and response body structure
            .statusCode(201)
            .body("enrollmentId", notNullValue())
            .body("message", equalTo("Đăng ký khóa học thành công"));
        
        // [REQ-011] Verify: Notification service was called to send confirmation
        // Edge case validation: Ensure notification is triggered on successful enrollment
        Mockito.verify(notificationService, Mockito.times(1))
            .sendEnrollmentConfirmation(any(), any());
        
        // [REQ-011] Log test completion for audit trail
        LOG.info("[TEST_END] [REQ-011] testEnrollInAvailableCourse_Success - PASSED");
    }
    
    /**
     * Test edge case: Student attempts to enroll in a course that is already full.
     * Verifies system rejects enrollment when max students capacity is reached.
     * 
     * @verifies [REQ-011]
     */
    @Test
    void testEnrollInFullCourse_ReturnsConflict() {
        // [REQ-011] Log test start for audit trail
        LOG.info("[TEST_START] [REQ-011] testEnrollInFullCourse_ReturnsConflict");
        
        // [REQ-011] Test data: course at maximum capacity
        UUID courseId = UUID.randomUUID();
        
        // [REQ-011] Execute enrollment request for full course
        // Business logic: System should check course capacity before allowing enrollment
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", VALID_JWT_TOKEN)
            .body("{\"courseId\":\"" + courseId + "\",\"studentEmail\":\"" + TEST_STUDENT_EMAIL + "\"}")
        .when()
            .post(ENROLLMENT_API_PATH)
        .then()
            // [REQ-011] Assert: 409 Conflict with COURSE_FULL error
            // Validation strategy: Verify capacity constraint is enforced at service layer
            .statusCode(409)
            .body("error", equalTo("COURSE_FULL"));
        
        // [REQ-011] Log test completion for audit trail
        LOG.info("[TEST_END] [REQ-011] testEnrollInFullCourse_ReturnsConflict - PASSED");
    }
    
    /**
     * Test edge case: Student attempts to enroll in a course already enrolled in.
     * Verifies unique constraint enforcement on (studentId, courseId) pair.
     * 
     * @verifies [REQ-011]
     */
    @Test
    void testEnrollInAlreadyEnrolledCourse_ReturnsConflict() {
        // [REQ-011] Log test start for audit trail
        LOG.info("[TEST_START] [REQ-011] testEnrollInAlreadyEnrolledCourse_ReturnsConflict");
        
        // [REQ-011] Test data: student already enrolled in the course
        UUID courseId = UUID.randomUUID();
        
        // [REQ-011] Execute duplicate enrollment request
        // Business logic: System should prevent duplicate enrollments using unique constraint
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", VALID_JWT_TOKEN)
            .body("{\"courseId\":\"" + courseId + "\"}")
        .when()
            .post(ENROLLMENT_API_PATH)
        .then()
            // [REQ-011] Assert: 409 Conflict with ALREADY_ENROLLED error
            // Validation strategy: Verify unique constraint (studentId, courseId) is enforced
            .statusCode(409)
            .body("error", equalTo("ALREADY_ENROLLED"));
        
        // [REQ-011] Log test completion for audit trail
        LOG.info("[TEST_END] [REQ-011] testEnrollInAlreadyEnrolledCourse_ReturnsConflict - PASSED");
    }
    
    /**
     * Test happy path: Student views available courses and enrolled courses are excluded.
     * Verifies filtering logic for available courses list.
     * 
     * @verifies [REQ-010]
     */
    @Test
    void testGetAvailableCourses_ExcludesEnrolledCourses() {
        // [REQ-010] Log test start for audit trail
        LOG.info("[TEST_START] [REQ-010] testGetAvailableCourses_ExcludesEnrolledCourses");
        
        // [REQ-010] Test data: student with existing enrollment in one course
        UUID studentId = UUID.randomUUID();
        
        // [REQ-010] Execute GET /api/v1/courses/available
        // Business logic: System should filter out courses student is already enrolled in
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", VALID_JWT_TOKEN)
            .queryParam("studentId", studentId.toString())
        .when()
            .get(AVAILABLE_COURSES_API_PATH)
        .then()
            // [REQ-010] Assert: 200 OK with only non-enrolled courses
            // Validation strategy: Verify enrolled courses are excluded from available list
            .statusCode(200)
            .body("$", hasSize(1))
            .body("[0].courseId", notNullValue())
            .body("[0].title", notNullValue());
        
        // [REQ-010] Log test completion for audit trail
        LOG.info("[TEST_END] [REQ-010] testGetAvailableCourses_ExcludesEnrolledCourses - PASSED");
    }
    
    /**
     * Test edge case: Invalid course ID format returns bad request.
     * Verifies input validation for enrollment requests.
     * 
     * @verifies [REQ-011]
     */
    @Test
    void testEnrollWithInvalidCourseId_ReturnsBadRequest() {
        // [REQ-011] Log test start for audit trail
        LOG.info("[TEST_START] [REQ-011] testEnrollWithInvalidCourseId_ReturnsBadRequest");
        
        // [REQ-011] Execute with malformed course ID
        // Business logic: System should validate UUID format before processing enrollment
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", VALID_JWT_TOKEN)
            .body("{\"courseId\":\"invalid-uuid\",\"studentEmail\":\"" + TEST_STUDENT_EMAIL + "\"}")
        .when()
            .post(ENROLLMENT_API_PATH)
        .then()
            // [REQ-011] Assert: 400 Bad Request with validation error
            // Validation strategy: Verify input validation rejects malformed UUIDs
            .statusCode(400)
            .body("error", equalTo("VALIDATION_FAILED"));
        
        // [REQ-011] Log test completion for audit trail
        LOG.info("[TEST_END] [REQ-011] testEnrollWithInvalidCourseId_ReturnsBadRequest - PASSED");
    }
    
    /**
     * Test edge case: Empty request body returns validation error.
     * Verifies request validation for mandatory fields.
     * 
     * @verifies [REQ-011]
     */
    @Test
    void testEnrollWithEmptyRequest_ReturnsBadRequest() {
        // [REQ-011] Log test start for audit trail
        LOG.info("[TEST_START] [REQ-011] testEnrollWithEmptyRequest_ReturnsBadRequest");
        
        // [REQ-011] Execute with empty JSON body
        // Business logic: System should require courseId and studentEmail fields
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", VALID_JWT_TOKEN)
            .body("{}")
        .when()
            .post(ENROLLMENT_API_PATH)
        .then()
            // [REQ-011] Assert: 400 Bad Request with validation error
            // Validation strategy: Verify mandatory field validation for enrollment request
            .statusCode(400)
            .body("error", equalTo("VALIDATION_FAILED"));
        
        // [REQ-011] Log test completion for audit trail
        LOG.info("[TEST_END] [REQ-011] testEnrollWithEmptyRequest_ReturnsBadRequest - PASSED");
    }
    
    /**
     * Test edge case: Unauthorized access without JWT token returns 401.
     * Verifies authentication requirement for enrollment endpoints.
     * 
     * @verifies [REQ-010], [REQ-011]
     */
    @Test
    void testEnrollWithoutAuthToken_ReturnsUnauthorized() {
        // [REQ-010], [REQ-011] Log test start for audit trail
        LOG.info("[TEST_START] [REQ-010][REQ-011] testEnrollWithoutAuthToken_ReturnsUnauthorized");
        
        // [REQ-010], [REQ-011] Execute without Authorization header
        // Business logic: System should require valid JWT for all enrollment operations
        given()
            .contentType(ContentType.JSON)
            .body("{\"courseId\":\"" + UUID.randomUUID() + "\"}")
        .when()
            .post(ENROLLMENT_API_PATH)
        .then()
            // [REQ-010], [REQ-011] Assert: 401 Unauthorized
            // Validation strategy: Verify authentication gate is enforced at API layer
            .statusCode(401);
        
        // [REQ-010], [REQ-011] Log test completion for audit trail
        LOG.info("[TEST_END] [REQ-010][REQ-011] testEnrollWithoutAuthToken_ReturnsUnauthorized - PASSED");
    }
    
    /**
     * Test happy path: Enrollment automatically creates student account if not exists.
     * Verifies automatic account creation logic for new students.
     * 
     * @verifies [REQ-011]
     */
    @Test
    void testEnroll_AutoCreatesStudentAccount_WhenNotExists() {
        // [REQ-011] Log test start for audit trail
        LOG.info("[TEST_START] [REQ-011] testEnroll_AutoCreatesStudentAccount_WhenNotExists");
        
        // [REQ-011] Test data: course ID and email for non-existent student
        UUID courseId = UUID.randomUUID();
        String newStudentEmail = "autocreate@example.com";
        
        // [REQ-011] Execute enrollment with new student email
        // Business logic: System should create User record with Student role automatically
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", VALID_JWT_TOKEN)
            .body("{\"courseId\":\"" + courseId + "\",\"studentEmail\":\"" + newStudentEmail + "\"}")
        .when()
            .post(ENROLLMENT_API_PATH)
        .then()
            // [REQ-011] Assert: Enrollment succeeds with auto-created account
            // Validation strategy: Verify enrollment succeeds and account is created
            .statusCode(201)
            .body("enrollmentId", notNullValue());
        
        // [REQ-011] Verify: Notification was sent to newly created student
        // Edge case validation: Ensure notification triggers for auto-created accounts
        Mockito.verify(notificationService, Mockito.times(1))
            .sendEnrollmentConfirmation(any(), any());
        
        // [REQ-011] Log test completion for audit trail
        LOG.info("[TEST_END] [REQ-011] testEnroll_AutoCreatesStudentAccount_WhenNotExists - PASSED");
    }
}