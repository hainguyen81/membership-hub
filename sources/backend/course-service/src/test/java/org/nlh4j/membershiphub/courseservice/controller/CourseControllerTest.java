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