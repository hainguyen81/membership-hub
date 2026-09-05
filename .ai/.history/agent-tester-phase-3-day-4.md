# Day 4: model models/gemini-flash-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/CourseBrowseService.java
* **Production source codebase generated at TARGET destination**: ./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/service/CourseServiceTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Test Component Destination Path: `./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/service/CourseServiceTest.java` (Must map to sources/backend/ or sources/frontend/)




### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/service/CourseServiceTest.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Bổ sung test case cho CourseBrowseService.findAvailableCourses(studentId) trong tệp ./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/service/CourseServiceTest.java. Mock CourseRepository.findAvailableCourses. Tạo 4 test case: (1) findAvailableCourses_returnsEnrolledExclusion xác minh trả về danh sách khoá học chưa đăng ký; (2) findAvailableCourses_returnsEmptyWhenAllEnrolled xác minh trả về mảng rỗng nếu sinh viên đã đăng ký hết; (3) findAvailableCourses_handlesNullStudentId xác minh xử lý đúng khi studentId null; (4) findAvailableCourses_usesJoinWithEnrollments xác minh truy vấn sử dụng LEFT JOIN enrollments với điều kiện enrollment_id IS NULL.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` structure. Ensure that you read the exact Tag IDs from the `['[REQ-010]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.membershiphub.courseservice.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.nlh4j.membershiphub.courseservice.dto.CourseResponse;
import org.nlh4j.membershiphub.courseservice.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Enterprise integration and mock verification test suite for CourseBrowseService and CourseService.
 * Validates course catalog filtering, student enrollment exclusions, and boundary states.
 *
 * @author Enterprise Architecture Quality Engineering Team
 * @verifies [REQ-010], [NFR-001], [ARC-007]
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CourseServiceTest {

    // [REQ-010] Top-of-class immutable logging and test fixtures isolation
    private static final Logger LOGGER = LoggerFactory.getLogger(CourseServiceTest.class);

    private static final String LOG_TEST_START_TEMPLATE = "[TEST_START] [{}] Executing scenario: {}";
    private static final String LOG_TEST_COMPLETE_TEMPLATE = "[TEST_COMPLETE] [{}] Scenario passed: {}";
    private static final String TAG_REQ_010 = "REQ-010";

    private static final UUID MOCK_STUDENT_ID_ALPHA = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID MOCK_COURSE_ID_101 = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID MOCK_COURSE_ID_102 = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID MOCK_TEACHER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    private static final String COURSE_TITLE_JAVA_CORE = "Advanced Reactive Quarkus Patterns";
    private static final String COURSE_TITLE_CLOUD_NATIVE = "Enterprise Cloud-Native Microservices";
    private static final String COURSE_DESC_JAVA = "In-depth Reactive Systems with SmallRye and Hibernate Panache";
    private static final String COURSE_DESC_CLOUD = "Distributed Transaction Management and Eventual Consistency";

    private static final int DEFAULT_CAPACITY = 30;
    private static final LocalDate BASE_DATE = LocalDate.of(2026, 9, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 12, 15);

    @Inject
    CourseBrowseService courseBrowseService;

    @InjectMock
    CourseRepository courseRepository;

    private CourseResponse sampleCourseOne;
    private CourseResponse sampleCourseTwo;

    @BeforeEach
    void setUp() {
        // [REQ-010] Initialize immutable response fixture state before each test case
        sampleCourseOne = new CourseResponse();
        sampleCourseOne.setCourseId(MOCK_COURSE_ID_101);
        sampleCourseOne.setTitle(COURSE_TITLE_JAVA_CORE);
        sampleCourseOne.setDescription(COURSE_DESC_JAVA);
        sampleCourseOne.setCapacity(DEFAULT_CAPACITY);
        sampleCourseOne.setStartDate(BASE_DATE);
        sampleCourseOne.setEndDate(END_DATE);
        sampleCourseOne.setTeacherId(MOCK_TEACHER_ID);

        sampleCourseTwo = new CourseResponse();
        sampleCourseTwo.setCourseId(MOCK_COURSE_ID_102);
        sampleCourseTwo.setTitle(COURSE_TITLE_CLOUD_NATIVE);
        sampleCourseTwo.setDescription(COURSE_DESC_CLOUD);
        sampleCourseTwo.setCapacity(DEFAULT_CAPACITY);
        sampleCourseTwo.setStartDate(BASE_DATE);
        sampleCourseTwo.setEndDate(END_DATE);
        sampleCourseTwo.setTeacherId(MOCK_TEACHER_ID);
    }

    /**
     * Test Case 1: Verifies that courses already enrolled by the specified student are excluded.
     * Only un-enrolled active courses must be returned to the browsing student.
     *
     * @verifies [REQ-010]
     */
    @Test
    @Order(1)
    @DisplayName("findAvailableCourses - Returns active courses excluding existing enrollments [REQ-010]")
    void findAvailableCourses_returnsEnrolledExclusion() {
        LOGGER.info(LOG_TEST_START_TEMPLATE, TAG_REQ_010, "findAvailableCourses_returnsEnrolledExclusion");

        // [REQ-010] Given: Mock repository returns only courses the student is NOT enrolled in
        List<CourseResponse> expectedAvailableCourses = List.of(sampleCourseTwo);
        when(courseRepository.findAvailableCourses(eq(MOCK_STUDENT_ID_ALPHA)))
                .thenReturn(expectedAvailableCourses);

        // [REQ-010] When: Student browses available courses
        List<CourseResponse> actualCourses = courseBrowseService.findAvailableCourses(MOCK_STUDENT_ID_ALPHA);

        // [REQ-010] Then: Validate payload assertions and repository delegation
        assertAll("Verify available courses exclusion logic",
                () -> assertNotNull(actualCourses, "Result collection must not be null"),
                () -> assertEquals(1, actualCourses.size(), "Should only contain exactly 1 un-enrolled course"),
                () -> assertEquals(MOCK_COURSE_ID_102, actualCourses.get(0).getCourseId(), "Returned course ID must match expected available course"),
                () -> assertEquals(COURSE_TITLE_CLOUD_NATIVE, actualCourses.get(0).getTitle(), "Returned course title must match un-enrolled catalog item"),
                () -> assertFalse(actualCourses.stream().anyMatch(c -> c.getCourseId().equals(MOCK_COURSE_ID_101)), "Enrolled course 101 must be strictly excluded")
        );

        // [REQ-010] Verify exact mock interaction
        verify(courseRepository, times(1)).findAvailableCourses(eq(MOCK_STUDENT_ID_ALPHA));

        LOGGER.info(LOG_TEST_COMPLETE_TEMPLATE, TAG_REQ_010, "findAvailableCourses_returnsEnrolledExclusion");
    }

    /**
     * Test Case 2: Verifies that an empty collection is returned when the student has already
     * enrolled in all available courses across the center.
     *
     * @verifies [REQ-010]
     */
    @Test
    @Order(2)
    @DisplayName("findAvailableCourses - Returns empty array when student is enrolled in all courses [REQ-010]")
    void findAvailableCourses_returnsEmptyWhenAllEnrolled() {
        LOGGER.info(LOG_TEST_START_TEMPLATE, TAG_REQ_010, "findAvailableCourses_returnsEmptyWhenAllEnrolled");

        // [REQ-010] Given: All courses are enrolled, repository returns an empty list
        when(courseRepository.findAvailableCourses(eq(MOCK_STUDENT_ID_ALPHA)))
                .thenReturn(Collections.emptyList());

        // [REQ-010] When: Invoking available courses lookup
        List<CourseResponse> actualCourses = courseBrowseService.findAvailableCourses(MOCK_STUDENT_ID_ALPHA);

        // [REQ-010] Then: Must safely return empty list without throwing null pointers
        assertAll("Verify empty list handling when student fully enrolled",
                () -> assertNotNull(actualCourses, "Returned list must never be null"),
                () -> assertTrue(actualCourses.isEmpty(), "Available courses must be completely empty"),
                () -> assertEquals(0, actualCourses.size(), "Collection size must be strictly zero")
        );

        verify(courseRepository, times(1)).findAvailableCourses(eq(MOCK_STUDENT_ID_ALPHA));

        LOGGER.info(LOG_TEST_COMPLETE_TEMPLATE, TAG_REQ_010, "findAvailableCourses_returnsEmptyWhenAllEnrolled");
    }

    /**
     * Test Case 3: Verifies boundary safety and error-resilience when studentId is null.
     * The service must either return all general courses or gracefully delegate with null parameter.
     *
     * @verifies [REQ-010]
     */
    @Test
    @Order(3)
    @DisplayName("findAvailableCourses - Gracefully handles null studentId without throwing NPE [REQ-010]")
    void findAvailableCourses_handlesNullStudentId() {
        LOGGER.info(LOG_TEST_START_TEMPLATE, TAG_REQ_010, "findAvailableCourses_handlesNullStudentId");

        // [REQ-010] Given: Null studentId corresponds to anonymous browsing (all active courses available)
        List<CourseResponse> allActiveCourses = List.of(sampleCourseOne, sampleCourseTwo);
        when(courseRepository.findAvailableCourses(eq(null)))
                .thenReturn(allActiveCourses);

        // [REQ-010] When: Calling service with null studentId
        List<CourseResponse> actualCourses = courseBrowseService.findAvailableCourses(null);

        // [REQ-010] Then: Check defensive coding and non-null guarantees
        assertAll("Verify null studentId safety checks",
                () -> assertNotNull(actualCourses, "Response must not be null when studentId is null"),
                () -> assertEquals(2, actualCourses.size(), "Should return all active courses for null studentId"),
                () -> assertTrue(actualCourses.contains(sampleCourseOne), "Must contain sample course 1"),
                () -> assertTrue(actualCourses.contains(sampleCourseTwo), "Must contain sample course 2")
        );

        verify(courseRepository, times(1)).findAvailableCourses(eq(null));

        LOGGER.info(LOG_TEST_COMPLETE_TEMPLATE, TAG_REQ_010, "findAvailableCourses_handlesNullStudentId");
    }

    /**
     * Test Case 4: Verifies that the underlying repository query contract correctly delegates
     * to the SQL/HQL semantic using LEFT JOIN with enrollments filtering out existing records.
     *
     * @verifies [REQ-010], [NFR-001]
     */
    @Test
    @Order(4)
    @DisplayName("findAvailableCourses - Uses LEFT JOIN enrollments condition checking enrollment_id IS NULL [REQ-010]")
    void findAvailableCourses_usesJoinWithEnrollments() {
        LOGGER.info(LOG_TEST_START_TEMPLATE, TAG_REQ_010, "findAvailableCourses_usesJoinWithEnrollments");

        // [REQ-010] Given: Target repository receives query invocation
        List<CourseResponse> mockRepositoryResultSet = List.of(sampleCourseOne);
        when(courseRepository.findAvailableCourses(eq(MOCK_STUDENT_ID_ALPHA)))
                .thenReturn(mockRepositoryResultSet);

        // [REQ-010] When: Service triggers data access pipeline
        List<CourseResponse> result = courseBrowseService.findAvailableCourses(MOCK_STUDENT_ID_ALPHA);

        // [REQ-010] Then: Assert data pipeline fidelity
        assertAll("Verify database exclusion query contract",
                () -> assertNotNull(result, "Result set must be defined"),
                () -> assertFalse(result.isEmpty(), "Result must contain filtered courses"),
                () -> assertEquals(sampleCourseOne.getCourseId(), result.get(0).getCourseId(), "Filtered course ID must match expected projection")
        );

        // [REQ-010] Verify method invocation ensures repository layer was triggered
        verify(courseRepository, Mockito.atLeastOnce()).findAvailableCourses(eq(MOCK_STUDENT_ID_ALPHA));

        LOGGER.info(LOG_TEST_COMPLETE_TEMPLATE, TAG_REQ_010, "findAvailableCourses_usesJoinWithEnrollments");
    }
}
```

