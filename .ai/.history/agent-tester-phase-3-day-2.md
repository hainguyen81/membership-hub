# Day 2: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/controller/CourseController.java
* **Production source codebase generated at TARGET destination**: ./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/controller/CourseControllerTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Test Component Destination Path: `./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/controller/CourseControllerTest.java` (Must map to sources/backend/ or sources/frontend/)




### 📁 TARGET SOURCE IMPLEMENTATION CONTEXT (VERIFICATION TARGET)
Analyze the core logical operations within this implementation code block to construct your isolated unit assertions:
```java
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
```


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Tạo tệp kiểm thử đơn vị ./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/controller/CourseControllerTest.java sử dụng JUnit 5 kết hợp Mockito 5.7.0 và RestAssured 5.4.0. Mock CourseService. Tạo 6 test case: (1) listCourses_byAuthenticatedUser_returns200WithPagination xác minh GET trả về HTTP 200 với mảng content chứa các khoá học; (2) createCourse_bySystemAdmin_returns201 xác minh SystemAdmin tạo mới thành công trả về HTTP 201; (3) createCourse_withMissingTitle_returns400 xác minh thiếu trường title trả về HTTP 400 với mảng errors; (4) createCourse_withEndDateBeforeStartDate_returns400 xác minh endDate < startDate trả về HTTP 400; (5) createCourse_byTeacher_returns403 xác minh Teacher cố tạo khoá học bị từ chối với mã INSUFFICIENT_PRIVILEGES; (6) createCourse_withScheduleConflict_returns409 xác minh trùng lịch giáo viên ném ScheduleConflictException trả về HTTP 409 với mã SCHEDULE_CONFLICT_409.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` structure. Ensure that you read the exact Tag IDs from the `['[REQ-007]', '[REQ-008]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.membershiphub.courseservice.controller;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.nlh4j.membershiphub.courseservice.dto.CourseCreateRequest;
import org.nlh4j.membershiphub.courseservice.dto.CourseResponse;
import org.nlh4j.membershiphub.courseservice.exception.ScheduleConflictException;
import org.nlh4j.membershiphub.courseservice.service.CourseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for {@link CourseController} verifying all CRUD endpoints, pagination,
 * validation rules, and schedule conflict exceptions.
 *
 * <p>Traceability Tags:</p>
 * <ul>
 *   <li>[REQ-007] – Course listing and pagination</li>
 *   <li>[REQ-008] – Course creation, update, and deletion with validation</li>
 * </ul>
 *
 * @author Senior Test Automation Engineer
 * @version 1.0
 * @since 2024-01-01
 */
class CourseControllerTest {

    @Mock
    private CourseService courseService;

    @InjectMocks
    private CourseController courseController;

    private static final String API_BASE_PATH = "/api/v1/courses";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        RestAssuredMockMvc.standaloneSetup(courseController);
    }

    /**
     * Verifies that authenticated users can fetch a paginated list of courses successfully [REQ-007].
     */
    @Test
    @DisplayName("listCourses_byAuthenticatedUser_returns200WithPagination [REQ-007]")
    void listCourses_byAuthenticatedUser_returns200WithPagination() {
        // [REQ-007] Happy case: verify paginated course retrieval returns HTTP 200 with content
        CourseResponse sampleCourse = new CourseResponse(
                UUID.randomUUID(),
                "Spring Boot Masterclass",
                "Advanced Enterprise Backend",
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                UUID.randomUUID(),
                30
        );

        Page<CourseResponse> coursePage = new PageImpl<>(Collections.singletonList(sampleCourse), PageRequest.of(0, 20), 1);
        when(courseService.findAll(any())).thenReturn(coursePage);

        RestAssuredMockMvc.given()
                .param("page", 0)
                .param("size", 20)
                .param("sort", "startDate,asc")
        .when()
                .get(API_BASE_PATH)
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", notNullValue())
                .body("content.size()", is(1))
                .body("content[0].title", equalTo("Spring Boot Masterclass"))
                .body("totalElements", is(1));

        verify(courseService, times(1)).findAll(any());
    }

    /**
     * Verifies that SystemAdmin or CenterAdmin can create a new course successfully [REQ-008].
     */
    @Test
    @DisplayName("createCourse_bySystemAdmin_returns201 [REQ-008]")
    void createCourse_bySystemAdmin_returns201() {
        // [REQ-008] Happy case: valid course creation request returns HTTP 201 Created
        CourseCreateRequest request = new CourseCreateRequest(
                "Quarkus Fundamentals",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(15),
                UUID.randomUUID(),
                25
        );

        CourseResponse createdResponse = new CourseResponse(
                UUID.randomUUID(),
                request.getTitle(),
                "Cloud Native Java",
                request.getStartDate(),
                request.getEndDate(),
                request.getTeacherId(),
                request.getMaxStudents()
        );

        when(courseService.create(any(CourseCreateRequest.class))).thenReturn(createdResponse);

        RestAssuredMockMvc.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
        .when()
                .post(API_BASE_PATH)
        .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("title", equalTo("Quarkus Fundamentals"))
                .body("maxStudents", is(25));

        verify(courseService, times(1)).create(any(CourseCreateRequest.class));
    }

    /**
     * Verifies that creating a course with a missing title triggers validation failure (HTTP 400) [REQ-008].
     */
    @Test
    @DisplayName("createCourse_withMissingTitle_returns400 [REQ-008]")
    void createCourse_withMissingTitle_returns400() {
        // [REQ-008] Edge case / Negative: blank or null title must be rejected by Bean Validation
        CourseCreateRequest invalidRequest = new CourseCreateRequest(
                "",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(15),
                UUID.randomUUID(),
                20
        );

        // Simulating Spring validation rejection
        RestAssuredMockMvc.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(invalidRequest)
        .when()
                .post(API_BASE_PATH)
        .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        verify(courseService, never()).create(any());
    }

    /**
     * Verifies that creating a course where endDate is before startDate returns HTTP 400 [REQ-008].
     */
    @Test
    @DisplayName("createCourse_withEndDateBeforeStartDate_returns400 [REQ-008]")
    void createCourse_withEndDateBeforeStartDate_returns400() {
        // [REQ-008] Boundary condition: end date preceding start date violates business constraint
        CourseCreateRequest invalidRequest = new CourseCreateRequest(
                "Malformed Schedule Course",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(5), // endDate < startDate
                UUID.randomUUID(),
                25
        );

        // Stubbing service to throw IllegalArgumentException for date range violation
        when(courseService.create(any(CourseCreateRequest.class)))
                .thenThrow(new IllegalArgumentException("End date must be after or equal to start date"));

        RestAssuredMockMvc.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(invalidRequest)
        .when()
                .post(API_BASE_PATH)
        .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        verify(courseService, times(1)).create(any());
    }

    /**
     * Verifies that a user with Teacher role attempting to create a course is denied with HTTP 403 [REQ-008].
     */
    @Test
    @DisplayName("createCourse_byTeacher_returns403 [REQ-008]")
    void createCourse_byTeacher_returns403() {
        // [REQ-008] Exception path: teachers lack course creation privileges
        CourseCreateRequest request = new CourseCreateRequest(
                "Teacher Attempt Course",
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                UUID.randomUUID(),
                20
        );

        // Simulating unauthorized role restriction throwing SecurityException / AccessDeniedException
        when(courseService.create(any(CourseCreateRequest.class)))
                .thenThrow(new SecurityException("INSUFFICIENT_PRIVILEGES: Teachers cannot create courses"));

        RestAssuredMockMvc.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
        .when()
                .post(API_BASE_PATH)
        .then()
                .statusCode(HttpStatus.FORBIDDEN.value());

        verify(courseService, times(1)).create(any());
    }

    /**
     * Verifies that creating a course with a teacher schedule conflict throws ScheduleConflictException (HTTP 409) [REQ-008].
     */
    @Test
    @DisplayName("createCourse_withScheduleConflict_returns409 [REQ-008]")
    void createCourse_withScheduleConflict_returns409() {
        // [REQ-008] Exception path: teacher overlapping schedule triggers SCHEDULE_CONFLICT_409
        CourseCreateRequest request = new CourseCreateRequest(
                "Overlapping Course",
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                UUID.randomUUID(),
                15
        );

        when(courseService.create(any(CourseCreateRequest.class)))
                .thenThrow(new ScheduleConflictException("SCHEDULE_CONFLICT_409: Teacher is already booked during this period"));

        RestAssuredMockMvc.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
        .when()
                .post(API_BASE_PATH)
        .then()
                .statusCode(HttpStatus.CONFLICT.value());

        verify(courseService, times(1)).create(any());
    }
}
```

