package org.nlh4j.saas.membership-hub.rest.integration;

// [IMPORT] Core testing and framework dependencies
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.nlh4j.saas.membership-hub.model.Center;
import org.nlh4j.saas.membership-hub.model.Course;
import org.nlh4j.saas.membership-hub.model.User;
import org.nlh4j.saas.membership-hub.repository.CenterRepository;
import org.nlh4j.saas.membership-hub.repository.CourseRepository;
import org.nlh4j.saas.membership-hub.repository.UserRepository;
import org.nlh4j.saas.membership-hub.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.quarkus.test.Inject;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.RestAssured;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

/**
 * Integration test suite for CourseResource REST API endpoints.
 * Validates all course management operations including CRUD, schedule conflict checking, and teacher assignment.
 * @verifies [REQ-007], [REQ-008], [REQ-009]
 */
@Testcontainers
@QuarkusTest
public class CourseResourceTest {

    // [CONST] Class-level logger for test execution tracing
    private static final Logger LOGGER = LoggerFactory.getLogger(CourseResourceTest.class);

    // [CONST] Immutable API endpoint paths (no hardcoded literals in test logic)
    public static final String COURSE_API_PATH = "/api/v1/courses";
    public static final String COURSE_BY_ID_API_PATH = "/api/v1/courses/%s";
    public static final String ASSIGN_TEACHER_API_PATH = "/api/v1/courses/%s/assign-teacher";
    public static final String AUTH_LOGIN_API_PATH = "/api/v1/auth/login";

    // [CONST] Immutable test data constants (no magic values)
    public static final String TEST_ADMIN_EMAIL = "admin.course.test@membershiphub.com";
    public static final String TEST_ADMIN_PASSWORD = "SecureTestPass123!";
    public static final String TEST_TEACHER_EMAIL = "teacher.course.test@membershiphub.com";
    public static final String TEST_TEACHER_PASSWORD = "TeacherTestPass123!";
    public static final String TEST_COURSE_TITLE = "Advanced Java Programming";
    public static final String TEST_COURSE_DESCRIPTION = "Master Java 21 and Quarkus framework";
    public static final LocalDate TEST_COURSE_START_DATE = LocalDate.of(2024, 6, 1);
    public static final LocalDate TEST_COURSE_END_DATE = LocalDate.of(2024, 6, 30);
    public static final int TEST_MAX_STUDENTS = 25;
    public static final String TEST_CENTER_NAME = "Test Training Center";
    public static final String TEST_CENTER_ADDRESS = "456 Tech Street, Hanoi, Vietnam";
    public static final String TEST_CENTER_TAX_ID = "0123456789";
    public static final String TEST_CENTER_CONTACT_PHONE = "0987654321";
    public static final String TEST_CENTER_CONTACT_EMAIL = "center.test@membershiphub.com";
    public static final short ROLE_SYSTEM_ADMIN_ID = 1;
    public static final short ROLE_TEACHER_ID = 3;
    public static final short ROLE_STUDENT_ID = 2;

    // [INFRA] Testcontainers PostgreSQL instance for real database integration testing
    @Container
    public static PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("membership_hub_course_test")
            .withUsername("test_user")
            .withPassword("test_pass");

    // [INFRA] Injected application repositories for database state verification
    @Inject
    CourseRepository courseRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    CenterRepository centerRepository;

    // [MOCK] Mock external notification service to verify trigger without real external calls
    @InjectMock
    NotificationService notificationService;

    // [STATE] Test context variables
    private String adminAccessToken;
    private User testAdminUser;
    private User testTeacherUser;
    private Center testCenter;
    private Course testCourse;

    /**
     * Initialize test context with required test data before each test case.
     * @verifies [REQ-007], [REQ-008], [REQ-009]
     */
    @BeforeEach
    void setUp() {
        LOGGER.info("[TEST_SETUP] [REQ-007][REQ-008][REQ-009] Initializing integration test context for CourseResource");

        // [ARRANGE] Create test center
        testCenter = new Center();
        testCenter.setName(TEST_CENTER_NAME);
        testCenter.setAddress(TEST_CENTER_ADDRESS);
        testCenter.setTaxId(TEST_CENTER_TAX_ID);
        testCenter.setContactPhone(TEST_CENTER_CONTACT_PHONE);
        testCenter.setContactEmail(TEST_CENTER_CONTACT_EMAIL);
        centerRepository.persist(testCenter);

        // [ARRANGE] Create test System Admin user
        testAdminUser = new User();
        testAdminUser.setEmail(TEST_ADMIN_EMAIL);
        testAdminUser.setPasswordHash(BCrypt.hashpw(TEST_ADMIN_PASSWORD, BCrypt.gensalt()));
        testAdminUser.setFullName("Test System Admin");
        testAdminUser.setRoleId(ROLE_SYSTEM_ADMIN_ID);
        testAdminUser.setProvider("local");
        userRepository.persist(testAdminUser);

        // [ARRANGE] Create test Teacher user
        testTeacherUser = new User();
        testTeacherUser.setEmail(TEST_TEACHER_EMAIL);
        testTeacherUser.setPasswordHash(BCrypt.hashpw(TEST_TEACHER_PASSWORD, BCrypt.gensalt()));
        testTeacherUser.setFullName("Test Teacher");
        testTeacherUser.setRoleId(ROLE_TEACHER_ID);
        testTeacherUser.setProvider("local");
        userRepository.persist(testTeacherUser);

        // [ARRANGE] Login as admin to obtain valid JWT access token
        Response loginResponse = RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(String.format("{\"email\":\"%s\",\"password\":\"%s\"}", TEST_ADMIN_EMAIL, TEST_ADMIN_PASSWORD))
                .when()
                .post(AUTH_LOGIN_API_PATH)
                .then()
                .statusCode(200)
                .extract()
                .response();
        adminAccessToken = loginResponse.jsonPath().getString("accessToken");

        // [ARRANGE] Create base test course for reuse in test cases
        testCourse = new Course();
        testCourse.setTitle(TEST_COURSE_TITLE);
        testCourse.setDescription(TEST_COURSE_DESCRIPTION);
        testCourse.setStartDate(TEST_COURSE_START_DATE);
        testCourse.setEndDate(TEST_COURSE_END_DATE);
        testCourse.setTeacherId(testTeacherUser.getUserId());
        testCourse.setMaxStudents(TEST_MAX_STUDENTS);
        courseRepository.persist(testCourse);

        // [ARRANGE] Reset mock notification service state before each test
        org.mockito.Mockito.reset(notificationService);

        LOGGER.info("[TEST_SETUP] [REQ-007][REQ-008][REQ-009] Test context initialized successfully");
    }

    /**
     * Test retrieving paginated list of all courses with optional centerId filter.
     * Validates happy path for course list retrieval endpoint.
     * @verifies [REQ-007]
     */
    @Test
    void testGetCourses_ReturnsPaginatedCourseList() {
        LOGGER.info("[TEST_START] [REQ-007] Testing GET {} endpoint for course list retrieval", COURSE_API_PATH);

        try {
            // [ACT] Call GET /api/v1/courses without filter
            Response unfilteredResponse = RestAssured.given()
                    .header("Authorization", "Bearer " + adminAccessToken)
                    .when()
                    .get(COURSE_API_PATH)
                    .then()
                    .extract()
                    .response();

            // [ASSERT] Verify unfiltered response status and content
            assertEquals(200, unfilteredResponse.getStatus(), "Expected HTTP 200 OK for unfiltered request");
            List<Course> unfilteredCourses = unfilteredResponse.readEntity(new GenericType<List<Course>>() {});
            assertNotNull(unfilteredCourses, "Expected non-null course list");
            assertTrue(unfilteredCourses.size() >= 1, "Expected at least 1 course in list");
            assertTrue(unfilteredCourses.stream().anyMatch(c -> c.getTitle().equals(TEST_COURSE_TITLE)),
                    "Expected test course to be present in unfiltered list");

            // [ACT] Call GET with centerId filter
            Response filteredResponse = RestAssured.given()
                    .header("Authorization", "Bearer " + adminAccessToken)
                    .queryParam("centerId", testCenter.getCenterId())
                    .when()
                    .get(COURSE_API_PATH)
                    .then()
                    .extract()
                    .response();

            // [ASSERT] Verify filtered response returns only courses for specified center
            assertEquals(200, filteredResponse.getStatus(), "Expected HTTP 200 OK for filtered request");
            List<Course> filteredCourses = filteredResponse.readEntity(new GenericType<List<Course>>() {});
            assertEquals(1, filteredCourses.size(), "Expected 1 course for test center, got " + filteredCourses.size());
            assertEquals(TEST_COURSE_TITLE, filteredCourses.get(0).getTitle(),
                    "Expected test course to be present in filtered list");

            LOGGER.info("[TEST_END] [REQ-007] GET courses list test passed successfully");
        } catch (Exception e) {
            LOGGER.error("[TEST_FAILED] [REQ-007] Get courses list test failed. Raw error: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Test creating a new course with valid input.
     * Validates happy path for course creation endpoint.
     * @verifies [REQ-008]
     */
    @Test
    void testCreateCourse_ReturnsCreatedCourse_WhenInputIsValid() {
        LOGGER.info("[TEST_START] [REQ-008] Testing POST {} endpoint for valid course creation", COURSE_API_PATH);

        try {
            // [ARRANGE] Prepare valid course request payload
            String requestBody = String.format("""
                    {
                        "title": "New Test Course",
                        "description": "New course description for testing",
                        "startDate": "%s",
                        "endDate": "%s",
                        "teacherId": "%s",
                        "maxStudents": 20
                    }
                    """, TEST_COURSE_START_DATE.plusDays(15), TEST_COURSE_END_DATE.plusDays(15), testTeacherUser.getUserId());

            // [ACT] Call POST /api/v1/courses
            Response createResponse = RestAssured.given()
                    .header("Authorization", "Bearer " + adminAccessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .when()
                    .post(COURSE_API_PATH)
                    .then()
                    .extract()
                    .response();

            // [ASSERT] Verify response status and payload
            assertEquals(201, createResponse.getStatus(), "Expected HTTP 201 Created for valid request");
            Course createdCourse = createResponse.readEntity(Course.class);
            assertNotNull(createdCourse, "Expected non-null created course response");
            assertNotNull(createdCourse.getCourseId(), "Expected generated course ID in response");
            assertEquals("New Test Course", createdCourse.getTitle(), "Expected course title to match request");
            assertEquals(testTeacherUser.getUserId(), createdCourse.getTeacherId(), "Expected teacher ID to match request");

            // [ASSERT] Verify course is persisted in database
            Course persistedCourse = courseRepository.findById(createdCourse.getCourseId());
            assertNotNull(persistedCourse, "Expected course to be persisted in database");
            assertEquals("New Test Course", persistedCourse.getTitle(), "Expected persisted course title to match");

            LOGGER.info("[TEST_END] [REQ-008] Valid course creation test passed successfully");
        } catch (Exception e) {
            LOGGER.error("[TEST_FAILED] [REQ-008] Valid course creation test failed. Raw error: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Test creating a course returns conflict when teacher has overlapping schedule.
     * Validates schedule conflict business rule enforcement.
     * @verifies [REQ-008]
     */
    @Test
    void testCreateCourse_ReturnsConflict_WhenTeacherHasScheduleOverlap() {
        LOGGER.info("[TEST_START] [REQ-008] Testing schedule conflict validation for course creation");

        try {
            // [ARRANGE] Prepare course request with overlapping dates for existing teacher schedule
            String requestBody = String.format("""
                    {
                        "title": "Conflicting Course",
                        "description": "Course with overlapping teacher schedule",
                        "startDate": "%s",
                        "endDate": "%s",
                        "teacherId": "%s",
                        "maxStudents": 20
                    }
                    """, TEST_COURSE_START_DATE.plusDays(5), TEST_COURSE_END_DATE.minusDays(5), testTeacherUser.getUserId());

            // [ACT] Call POST /api/v1/courses
            Response conflictResponse = RestAssured.given()
                    .header("Authorization", "Bearer " + adminAccessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .when()
                    .post(COURSE_API_PATH)
                    .then()
                    .extract()
                    .response();

            // [ASSERT] Verify conflict response
            assertEquals(409, conflictResponse.getStatus(), "Expected HTTP 409 Conflict for overlapping schedule");
            String responseBody = conflictResponse.readEntity(String.class);
            assertTrue(responseBody.contains("CONFLICT"), "Expected CONFLICT error code in response");
            assertTrue(responseBody.contains("schedule") || responseBody.contains("trùng lịch"),
                    "Expected schedule conflict error message in response");

            // [ASSERT] Verify no new course was persisted
            List<Course> allCourses = courseRepository.listAll();
            assertEquals(1, allCourses.size(), "Expected no new course to be created on conflict");

            LOGGER.info("[TEST_END] [REQ-008] Schedule conflict validation test passed successfully");
        } catch (Exception e) {
            LOGGER.error("[TEST_FAILED] [REQ-008] Schedule conflict test failed. Raw error: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Test creating a course returns validation error when end date is before start date.
     * Validates date range business rule enforcement.
     * @verifies [REQ-008]
     */
    @Test
    void testCreateCourse_ReturnsBadRequest_WhenEndDateBeforeStartDate() {
        LOGGER.info("[TEST_START] [REQ-008] Testing date range validation for course creation");

        try {
            // [ARRANGE] Prepare invalid course request (end date before start date)
            String requestBody = String.format("""
                    {
                        "title": "Invalid Date Course",
                        "description": "Course with invalid date range",
                        "startDate": "%s",
                        "endDate": "%s",
                        "teacherId": "%s",
                        "maxStudents": 20
                    }
                    """, TEST_COURSE_END_DATE, TEST_COURSE_START_DATE, testTeacherUser.getUserId());

            // [ACT] Call POST /api/v1/courses
            Response validationResponse = RestAssured.given()
                    .header("Authorization", "Bearer " + adminAccessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .when()
                    .post(COURSE_API_PATH)
                    .then()
                    .extract()
                    .response();

            // [ASSERT] Verify validation error response
            assertEquals(400, validationResponse.getStatus(), "Expected HTTP 400 Bad Request for invalid dates");
            String responseBody = validationResponse.readEntity(String.class);
            assertTrue(responseBody.contains("VALIDATION_FAILED"), "Expected VALIDATION_FAILED error code in response");

            LOGGER.info("[TEST_END] [REQ-008] Date range validation test passed successfully");
        } catch (Exception e) {
            LOGGER.error("[TEST_FAILED] [REQ-008] Date range validation test failed. Raw error: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Test updating an existing course with valid input.
     * Validates happy path for course update endpoint.
     * @verifies [REQ-008]
     */
    @Test
    void testUpdateCourse_ReturnsUpdatedCourse_WhenInputIsValid() {
        LOGGER.info("[TEST_START] [REQ-008] Testing PUT {} endpoint for course update", COURSE_BY_ID_API_PATH);

        try {
            // [ARRANGE] Prepare update request payload
            String updatedTitle = "Updated Advanced Java Programming";
            String requestBody = String.format("""
                    {
                        "title": "%s",
                        "description": "Updated course description",
                        "startDate": "%s",
                        "endDate": "%s",
                        "teacherId": "%s",
                        "maxStudents": 30
                    }
                    """, updatedTitle, TEST_COURSE_START_DATE, TEST_COURSE_END_DATE, testTeacherUser.getUserId());

            // [ACT] Call PUT /api/v1/courses/{courseId}
            Response updateResponse = RestAssured.given()
                    .header("Authorization", "Bearer " + adminAccessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .when()
                    .put(COURSE_BY_ID_API_PATH, testCourse.getCourseId())
                    .then()
                    .extract()
                    .response();

            // [ASSERT] Verify response status and payload
            assertEquals(200, updateResponse.getStatus(), "Expected HTTP 200 OK for valid update");
            Course updatedCourse = updateResponse.readEntity(Course.class);
            assertEquals(updatedTitle, updatedCourse.getTitle(), "Expected updated course title to match request");

            // [ASSERT] Verify update is persisted in database
            Course persistedCourse = courseRepository.findById(testCourse.getCourseId());
            assertEquals(updatedTitle, persistedCourse.getTitle(), "Expected persisted course title to be updated");

            LOGGER.info("[TEST_END] [REQ-008] Course update test passed successfully");
        } catch (Exception e) {
            LOGGER.error("[TEST_FAILED] [REQ-008] Course update test failed. Raw error: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Test deleting an existing course.
     * Validates happy path for course deletion endpoint.
     * @verifies [REQ-008]
     */
    @Test
    void testDeleteCourse_ReturnsNoContent_WhenCourseExists() {
        LOGGER.info("[TEST_START] [REQ-008] Testing DELETE {} endpoint for course deletion", COURSE_BY_ID_API_PATH);

        try {
            // [ACT] Call DELETE /api/v1/courses/{courseId}
            Response deleteResponse = RestAssured.given()
                    .header("Authorization", "Bearer " + adminAccessToken)
                    .when()
                    .delete(COURSE_BY_ID_API_PATH, testCourse.getCourseId())
                    .then()
                    .extract()
                    .response();

            // [ASSERT] Verify response status
            assertEquals(204, deleteResponse.getStatus(), "Expected HTTP 204 No Content for successful deletion");

            // [ASSERT] Verify course is removed from database
            Course deletedCourse = courseRepository.findById(testCourse.getCourseId());
            assertTrue(deletedCourse == null, "Expected course to be deleted from database");

            LOGGER.info("[TEST_END] [REQ-008] Course deletion test passed successfully");
        } catch (Exception e) {
            LOGGER.error("[TEST_FAILED] [REQ-008] Course deletion test failed. Raw error: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Test assigning a teacher to a course successfully triggers notification.
     * Validates happy path for teacher assignment endpoint and side effect notification.
     * @verifies [REQ-009]
     */
    @Test
    void testAssignTeacherToCourse_ReturnsSuccess_AndTriggersNotification() {
        LOGGER.info("[TEST_START] [REQ-009] Testing POST {} endpoint for teacher assignment", ASSIGN_TEACHER_API_PATH);

        try {
            // [ARRANGE] Create a course without assigned teacher
            Course courseWithoutTeacher = new Course();
            courseWithoutTeacher.setTitle("Course Without Teacher");
            courseWithoutTeacher.setDescription("Test course for teacher assignment flow");
            courseWithoutTeacher.setStartDate(TEST_COURSE_START_DATE.plusDays(20));
            courseWithoutTeacher.setEndDate(TEST_COURSE_END_DATE.plusDays(20));
            courseWithoutTeacher.setMaxStudents(TEST_MAX_STUDENTS);
            courseRepository.persist(courseWithoutTeacher);

            // [ARRANGE] Prepare assign teacher request payload
            String requestBody = String.format("{\"teacherId\":\"%s\"}", testTeacherUser.getUserId());

            // [ACT] Call POST /api/v1/courses/{courseId}/assign-teacher
            Response assignResponse = RestAssured.given()
                    .header("Authorization", "Bearer " + adminAccessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .when()
                    .post(ASSIGN_TEACHER_API_PATH, courseWithoutTeacher.getCourseId())
                    .then()
                    .extract()
                    .response();

            // [ASSERT] Verify response status and message
            assertEquals(200, assignResponse.getStatus(), "Expected HTTP 200 OK for successful teacher assignment");
            String responseBody = assignResponse.readEntity(String.class);
            assertTrue(responseBody.contains("Phân công giáo viên thành công"),
                    "Expected success message in response");

            // [ASSERT] Verify teacher is assigned in database
            Course updatedCourse = courseRepository.findById(courseWithoutTeacher.getCourseId());
            assertEquals(testTeacherUser.getUserId(), updatedCourse.getTeacherId(),
                    "Expected teacher to be assigned to course");

            // [ASSERT] Verify notification service is triggered exactly once
            verify(notificationService, org.mockito.Mockito.times(1))
                    .sendCourseAssignmentNotification(testTeacherUser.getUserId(), courseWithoutTeacher.getCourseId());

            LOGGER.info("[TEST_END] [REQ-009] Teacher assignment test passed successfully");
        } catch (Exception e) {
            LOGGER.error("[TEST_FAILED] [REQ-009] Teacher assignment test failed. Raw error: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Test assigning a teacher to a course returns conflict when teacher has schedule overlap.
     * Validates schedule conflict rule for teacher assignment flow.
     * @verifies [REQ-009]
     */
    @Test
    void testAssignTeacherToCourse_ReturnsConflict_WhenTeacherHasScheduleOverlap() {
        LOGGER.info("[TEST_START] [REQ-009] Testing schedule conflict validation for teacher assignment");

        try {
            // [ARRANGE] Create a course with dates overlapping existing teacher course
            Course conflictingCourse = new Course();
            conflictingCourse.setTitle("Conflicting Course For Teacher");
            conflictingCourse.setDescription("Course with overlapping dates for teacher");
            conflictingCourse.setStartDate(TEST_COURSE_START_DATE.plusDays(5));
            conflictingCourse.setEndDate(TEST_COURSE_END_DATE.minusDays(5));
            conflictingCourse.setMaxStudents(TEST_MAX_STUDENTS);
            courseRepository.persist(conflictingCourse);

            // [ARRANGE] Prepare assign teacher request
            String requestBody = String.format("{\"teacherId\":\"%s\"}", testTeacherUser.getUserId());

            // [ACT] Call assign teacher endpoint
            Response assignResponse = RestAssured.given()
                    .header("Authorization", "Bearer " + adminAccessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .when()
                    .post(ASSIGN_TEACHER_API_PATH, conflictingCourse.getCourseId())
                    .then()
                    .extract()
                    .response();

            // [ASSERT] Verify conflict response
            assertEquals(409, assignResponse.getStatus(), "Expected HTTP 409 Conflict for overlapping schedule");
            String responseBody = assignResponse.readEntity(String.class);
            assertTrue(responseBody.contains("CONFLICT"), "Expected CONFLICT error code in response");
            assertTrue(responseBody.contains("schedule") || responseBody.contains("trùng lịch"),
                    "Expected schedule conflict error message in response");

            // [ASSERT] Verify teacher was not assigned
            Course unassignedCourse = courseRepository.findById(conflictingCourse.getCourseId());
            assertTrue(unassignedCourse.getTeacherId() == null, "Expected teacher to not be assigned on conflict");

            LOGGER.info("[TEST_END] [REQ-009] Teacher assignment conflict test passed successfully");
        } catch (Exception e) {
            LOGGER.error("[TEST_FAILED] [REQ-009] Teacher assignment conflict test failed. Raw error: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Test retrieving a course by valid ID.
     * Validates happy path for single course retrieval endpoint.
     * @verifies [REQ-007]
     */
    @Test
    void testGetCourseById_ReturnsCourse_WhenCourseExists() {
        LOGGER.info("[TEST_START] [REQ-007] Testing GET {} endpoint for single course retrieval", COURSE_BY_ID_API_PATH);

        try {
            // [ACT] Call GET /api/v1/courses/{courseId}
            Response response = RestAssured.given()
                    .header("Authorization", "Bearer " + adminAccessToken)
                    .when()
                    .get(COURSE_BY_ID_API_PATH, testCourse.getCourseId())
                    .then()
                    .extract()
                    .response();

            // [ASSERT] Verify response status and content
            assertEquals(200, response.getStatus(), "Expected HTTP 200 OK for existing course");
            Course retrievedCourse = response.readEntity(Course.class);
            assertNotNull(retrievedCourse, "Expected non-null course response");
            assertEquals(testCourse.getCourseId(), retrievedCourse.getCourseId(), "Expected course ID to match");
            assertEquals(TEST_COURSE_TITLE, retrievedCourse.getTitle(), "Expected course title to match");

            LOGGER.info("[TEST_END] [REQ-007] Get course by ID test passed successfully");
        } catch (Exception e) {
            LOGGER.error("[TEST_FAILED] [REQ-007] Get course by ID test failed. Raw error: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Test retrieving a course by invalid ID returns 404 Not Found.
     * Validates error handling for non-existent resource.
     * @verifies [REQ-007]
     */
    @Test
    void testGetCourseById_ReturnsNotFound_WhenCourseDoesNotExist() {
        LOGGER.info("[TEST_START] [REQ-007] Testing GET {} endpoint for non-existent course", COURSE_BY_ID_API_PATH);

        try {
            // [ACT] Call GET with non-existent UUID
            UUID nonExistentId = UUID.randomUUID();
            Response response = RestAssured.given()
                    .header("Authorization", "Bearer " + adminAccessToken)
                    .when()
                    .get(COURSE_BY_ID_API_PATH, nonExistentId)
                    .then()
                    .extract()
                    .response();

            // [ASSERT] Verify 404 response
            assertEquals(404, response.getStatus(), "Expected HTTP 404 Not Found for non-existent course");

            LOGGER.info("[TEST_END] [REQ-007] Get non-existent course test passed successfully");
        } catch (Exception e) {
            LOGGER.error("[TEST_FAILED] [REQ-007] Get non-existent course test failed. Raw error: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Test creating a course returns 403 Forbidden when user has Student role.
     * Validates RBAC enforcement for course creation endpoint.
     * @verifies [REQ-008]
     */
    @Test
    void testCreateCourse_ReturnsForbidden_WhenUserIsStudent() {
        LOGGER.info("[TEST_START] [REQ-008] Testing RBAC enforcement for course creation endpoint");

        try {
            // [ARRANGE] Create test student user and obtain access token
            User studentUser = new User();
            studentUser.setEmail("student.course.rbac@membershiphub.com");
            studentUser.setPasswordHash(BCrypt.hashpw("StudentTestPass123!", BCrypt.gensalt()));
            studentUser.setFullName("Test RBAC Student");
            studentUser.setRoleId(ROLE_STUDENT_ID);
            studentUser.setProvider("local");
            userRepository.persist(studentUser);

            Response studentLoginResponse = RestAssured.given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(String.format("{\"email\":\"%s\",\"password\":\"%s\"}", studentUser.getEmail(), "StudentTestPass123!"))
                    .when()
                    .post(AUTH_LOGIN_API_PATH)
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();
            String studentAccessToken = studentLoginResponse.jsonPath().getString("accessToken");

            // [ARRANGE] Prepare valid course request
            String requestBody = String.format("""
                    {
                        "title": "Student Unauthorized Course",
                        "description": "This should be rejected",
                        "startDate": "%s",
                        "endDate": "%s",
                        "teacherId": "%s",
                        "maxStudents": 20
                    }
                    """, TEST_COURSE_START_DATE.plusDays(15), TEST_COURSE_END_DATE.plusDays(15), testTeacherUser.getUserId());

            // [ACT] Call POST /api/v1/courses as student user
            Response response = RestAssured.given()
                    .header("Authorization", "Bearer " + studentAccessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .when()
                    .post(COURSE_API_PATH)
                    .then()
                    .extract()
                    .response();

            // [ASSERT] Verify 403 Forbidden response
            assertEquals(403, response.getStatus(), "Expected HTTP 403 Forbidden for student user");
            String responseBody = response.readEntity(String.class);
            assertTrue(responseBody.contains("FORBIDDEN") || responseBody.contains("không có quyền"),
                    "Expected permission denied error message in response");

            LOGGER.info("[TEST_END] [REQ-008] RBAC enforcement test passed successfully");
        } catch (Exception e) {
            LOGGER.error("[TEST_FAILED] [REQ-008] RBAC enforcement test failed. Raw error: {}", e.getMessage());
            throw e;
        }
    }
}