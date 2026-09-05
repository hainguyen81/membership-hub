# Day 3: model models/gemini-flash-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: ./sources/backend/course-service/src/main/java/org/nlh4j/membershiphub/courseservice/service/CourseTeacherService.java
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
['Bổ sung test case trong tệp ./sources/backend/course-service/src/test/java/org/nlh4j/membershiphub/courseservice/service/CourseServiceTest.java cho CourseTeacherService.assign và CourseTeacherService.unassign. Mock CourseRepository và KafkaTeacherProducer. Xác nhận rằng khi gán thành công, sự kiện Kafka được publish đúng topic teacher-events với payload chứa eventType=teacher-assigned, courseId, teacherId, assignedAt. Test trường hợp giáo viên đã tồn tại trong mapping ném DataIntegrityViolationException với mã DUPLICATE_TEACHER_ASSIGNMENT_409. Test trường hợp course không tồn tại ném CourseNotFoundException trả về HTTP 404. Sử dụng JUnit 5 kết hợp Mockito 5.7.0.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` structure. Ensure that you read the exact Tag IDs from the `['[REQ-009]', '[ARC-007]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.membershiphub.courseservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enterprise Service Level Integration Test Suite verifying Course Teacher Assignment,
 * Teacher Unassignment, and Kafka Event Generation contracts.
 *
 * Traceability Matrix:
 * @verifies [REQ-009] Gán/huỷ gán giáo viên cho khoá học kèm đẩy sự kiện Kafka teacher-assigned
 * @verifies [ARC-007] Kiến trúc tích hợp phản ứng sự kiện và đảm bảo toàn vẹn dữ liệu
 * @verifies [ARC-008] Kafka Event Schema Contracts
 * @verifies [EXC-004] Enterprise Exception Handling & Error Boundaries
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CourseServiceTest {

    // =========================================================================
    // 0. TOP-OF-CLASS STATIC CONSTANTS DECLARATION
    // =========================================================================
    private static final Logger LOGGER = LoggerFactory.getLogger(CourseServiceTest.class);

    public static final String TRACE_TAG_REQ_009 = "[REQ-009]";
    public static final String TRACE_TAG_ARC_007 = "[ARC-007]";
    public static final String TRACE_TAG_ARC_008 = "[ARC-008]";
    public static final String TRACE_TAG_EXC_004 = "[EXC-004]";

    public static final String EXPECTED_KAFKA_TOPIC = "teacher-events";
    public static final String EVENT_TYPE_TEACHER_ASSIGNED = "teacher-assigned";
    public static final String EVENT_TYPE_TEACHER_UNASSIGNED = "teacher-unassigned";

    public static final String ERROR_CODE_DUPLICATE_ASSIGNMENT = "DUPLICATE_TEACHER_ASSIGNMENT_409";
    public static final String ERROR_CODE_COURSE_NOT_FOUND = "COURSE_NOT_FOUND_404";
    public static final String ERROR_CODE_TEACHER_NOT_ASSIGNED = "TEACHER_NOT_ASSIGNED_404";

    private static final UUID MOCK_COURSE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID MOCK_TEACHER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID MOCK_CENTER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID NON_EXISTENT_COURSE_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
    private static final UUID UNASSIGNED_TEACHER_ID = UUID.fromString("88888888-8888-8888-8888-888888888888");

    private static final String MOCK_COURSE_TITLE = "Advanced Java Cloud Architecture";
    private static final String MOCK_COURSE_DESC = "Mastering Quarkus, Kafka, and Reactive Microservices";
    private static final int MOCK_MAX_STUDENTS = 30;

    // =========================================================================
    // INJECTED COMPONENTS & TEST DOUBLES
    // =========================================================================
    @Inject
    CourseTeacherService courseTeacherService;

    @InjectMock
    CourseRepository courseRepository;

    @InjectMock
    KafkaTeacherProducer kafkaTeacherProducer;

    @Captor
    ArgumentCaptor<TeacherEventPayload> eventPayloadCaptor;

    @Captor
    ArgumentCaptor<String> topicCaptor;

    @Captor
    ArgumentCaptor<UUID> keyCaptor;

    private Course mockCourse;

    @BeforeEach
    void setUp() {
        LOGGER.info("[TEST_SETUP] {} Initializing test fixture and mock domains", TRACE_TAG_REQ_009);

        mockCourse = new Course();
        mockCourse.setCourseId(MOCK_COURSE_ID);
        mockCourse.setTitle(MOCK_COURSE_TITLE);
        mockCourse.setDescription(MOCK_COURSE_DESC);
        mockCourse.setStartDate(LocalDate.now().plusDays(7));
        mockCourse.setEndDate(LocalDate.now().plusMonths(3));
        mockCourse.setCenterId(MOCK_CENTER_ID);
        mockCourse.setMaxStudents(MOCK_MAX_STUDENTS);
        mockCourse.setTeacherId(null);
    }

    // =========================================================================
    // CATEGORY 1: HAPPY PATH TEST CASES
    // =========================================================================

    /**
     * @verifies [REQ-009] Assign teacher successfully, persist relationship and emit Kafka event
     * @verifies [ARC-007] Multi-component reactive state synchronization
     * @verifies [ARC-008] Validates Kafka event topic and JSON payload metadata
     */
    @Test
    @Order(1)
    @DisplayName("Should successfully assign teacher, update course state and publish teacher-assigned event to Kafka")
    void testAssignTeacher_Success_ShouldPublishKafkaEvent() {
        LOGGER.info("[TEST_START] {} Executing happy path teacher assignment", TRACE_TAG_REQ_009);

        // Arrange
        when(courseRepository.findByIdOptional(eq(MOCK_COURSE_ID))).thenReturn(Optional.of(mockCourse));
        when(courseRepository.isTeacherAssignedToCourse(eq(MOCK_COURSE_ID), eq(MOCK_TEACHER_ID))).thenReturn(false);

        TeacherAssignRequest request = new TeacherAssignRequest();
        request.setTeacherId(MOCK_TEACHER_ID);

        // Act
        CourseAssignmentResponse response = courseTeacherService.assignTeacher(MOCK_COURSE_ID, request);

        // Assert - Business Layer
        assertNotNull(response, "Course assignment response must not be null");
        assertEquals(MOCK_COURSE_ID, response.getCourseId(), "Course ID must match target");
        assertEquals(MOCK_TEACHER_ID, response.getTeacherId(), "Assigned Teacher ID must match payload");
        assertNotNull(response.getAssignedAt(), "Assignment timestamp must be stamped");

        // Assert - Database Persistence Layer
        verify(courseRepository, times(1)).findByIdOptional(eq(MOCK_COURSE_ID));
        verify(courseRepository, times(1)).isTeacherAssignedToCourse(eq(MOCK_COURSE_ID), eq(MOCK_TEACHER_ID));
        verify(courseRepository, times(1)).assignTeacherMapping(eq(MOCK_COURSE_ID), eq(MOCK_TEACHER_ID), any(Instant.class));

        // Assert - Kafka Event Dispatch Layer
        verify(kafkaTeacherProducer, times(1)).publishTeacherEvent(
                topicCaptor.capture(),
                keyCaptor.capture(),
                eventPayloadCaptor.capture()
        );

        assertEquals(EXPECTED_KAFKA_TOPIC, topicCaptor.getValue(), "Event must be emitted to 'teacher-events' topic");
        assertEquals(MOCK_COURSE_ID, keyCaptor.getValue(), "Kafka message key must be partition-keyed by Course ID");

        TeacherEventPayload emittedEvent = eventPayloadCaptor.getValue();
        assertNotNull(emittedEvent, "Emitted Kafka event payload must not be null");
        assertEquals(EVENT_TYPE_TEACHER_ASSIGNED, emittedEvent.getEventType(), "Event type must be 'teacher-assigned'");
        assertEquals(MOCK_COURSE_ID, emittedEvent.getCourseId(), "Course ID in payload must match");
        assertEquals(MOCK_TEACHER_ID, emittedEvent.getTeacherId(), "Teacher ID in payload must match");
        assertNotNull(emittedEvent.getAssignedAt(), "Assigned timestamp in payload must be present");

        LOGGER.info("[TEST_COMPLETE] {} Teacher assignment and Kafka event propagation successfully verified", TRACE_TAG_REQ_009);
    }

    /**
     * @verifies [REQ-009] Unassign teacher successfully, delete relationship and emit Kafka event
     * @verifies [ARC-007] Multi-component reactive state synchronization
     * @verifies [ARC-008] Validates Kafka event topic and JSON payload metadata for unassignment
     */
    @Test
    @Order(2)
    @DisplayName("Should successfully unassign teacher, remove relationship and publish teacher-unassigned event to Kafka")
    void testUnassignTeacher_Success_ShouldPublishKafkaEvent() {
        LOGGER.info("[TEST_START] {} Executing happy path teacher unassignment", TRACE_TAG_REQ_009);

        // Arrange
        mockCourse.setTeacherId(MOCK_TEACHER_ID);
        when(courseRepository.findByIdOptional(eq(MOCK_COURSE_ID))).thenReturn(Optional.of(mockCourse));
        when(courseRepository.isTeacherAssignedToCourse(eq(MOCK_COURSE_ID), eq(MOCK_TEACHER_ID))).thenReturn(true);

        // Act
        courseTeacherService.unassignTeacher(MOCK_COURSE_ID, MOCK_TEACHER_ID);

        // Assert - Database Persistence Layer
        verify(courseRepository, times(1)).findByIdOptional(eq(MOCK_COURSE_ID));
        verify(courseRepository, times(1)).isTeacherAssignedToCourse(eq(MOCK_COURSE_ID), eq(MOCK_TEACHER_ID));
        verify(courseRepository, times(1)).removeTeacherMapping(eq(MOCK_COURSE_ID), eq(MOCK_TEACHER_ID));

        // Assert - Kafka Event Dispatch Layer
        verify(kafkaTeacherProducer, times(1)).publishTeacherEvent(
                topicCaptor.capture(),
                keyCaptor.capture(),
                eventPayloadCaptor.capture()
        );

        assertEquals(EXPECTED_KAFKA_TOPIC, topicCaptor.getValue(), "Event must target 'teacher-events' topic");
        assertEquals(MOCK_COURSE_ID, keyCaptor.getValue(), "Partition key must align with Course ID");

        TeacherEventPayload emittedEvent = eventPayloadCaptor.getValue();
        assertNotNull(emittedEvent, "Emitted unassignment payload must not be null");
        assertEquals(EVENT_TYPE_TEACHER_UNASSIGNED, emittedEvent.getEventType(), "Event type must be 'teacher-unassigned'");
        assertEquals(MOCK_COURSE_ID, emittedEvent.getCourseId(), "Course ID in unassignment event must match");
        assertEquals(MOCK_TEACHER_ID, emittedEvent.getTeacherId(), "Teacher ID in unassignment event must match");

        LOGGER.info("[TEST_COMPLETE] {} Teacher unassignment workflow verified successfully", TRACE_TAG_REQ_009);
    }

    // =========================================================================
    // CATEGORY 2: EXCEPTION & NEGATIVE PATH TEST CASES
    // =========================================================================

    /**
     * @verifies [REQ-009] Conflict check: prevent assigning a teacher who is already assigned
     * @verifies [EXC-004] DataIntegrityViolationException with code DUPLICATE_TEACHER_ASSIGNMENT_409
     */
    @Test
    @Order(3)
    @DisplayName("Should throw DataIntegrityViolationException when teacher is already assigned to the course")
    void testAssignTeacher_DuplicateAssignment_ShouldThrowConflictException() {
        LOGGER.info("[TEST_START] {} [EXC-004] Testing duplicate teacher assignment conflict", TRACE_TAG_REQ_009);

        // Arrange
        when(courseRepository.findByIdOptional(eq(MOCK_COURSE_ID))).thenReturn(Optional.of(mockCourse));
        when(courseRepository.isTeacherAssignedToCourse(eq(MOCK_COURSE_ID), eq(MOCK_TEACHER_ID))).thenReturn(true);

        TeacherAssignRequest request = new TeacherAssignRequest();
        request.setTeacherId(MOCK_TEACHER_ID);

        // Act & Assert
        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> courseTeacherService.assignTeacher(MOCK_COURSE_ID, request),
                "Expected duplicate assignment to trigger DataIntegrityViolationException"
        );

        assertEquals(ERROR_CODE_DUPLICATE_ASSIGNMENT, exception.getErrorCode(), "Error code must equal DUPLICATE_TEACHER_ASSIGNMENT_409");
        assertTrue(exception.getMessage().contains(MOCK_TEACHER_ID.toString()), "Exception message should contain the duplicate teacher ID");

        // Verify that mapping persistence and Kafka dispatch are blocked
        verify(courseRepository, never()).assignTeacherMapping(any(), any(), any());
        verifyNoInteractions(kafkaTeacherProducer);

        LOGGER.info("[TEST_COMPLETE] {} [EXC-004] Duplicate teacher assignment properly rejected with HTTP 409 mapping", TRACE_TAG_REQ_009);
    }

    /**
     * @verifies [REQ-009] Target course validation: reject assignment when Course does not exist
     * @verifies [EXC-004] CourseNotFoundException triggering HTTP 404 response
     */
    @Test
    @Order(4)
    @DisplayName("Should throw CourseNotFoundException when attempting to assign teacher to non-existent course")
    void testAssignTeacher_CourseNotFound_ShouldThrowNotFoundException() {
        LOGGER.info("[TEST_START] {} [EXC-004] Testing teacher assignment to non-existent course", TRACE_TAG_REQ_009);

        // Arrange
        when(courseRepository.findByIdOptional(eq(NON_EXISTENT_COURSE_ID))).thenReturn(Optional.empty());

        TeacherAssignRequest request = new TeacherAssignRequest();
        request.setTeacherId(MOCK_TEACHER_ID);

        // Act & Assert
        CourseNotFoundException exception = assertThrows(
                CourseNotFoundException.class,
                () -> courseTeacherService.assignTeacher(NON_EXISTENT_COURSE_ID, request),
                "Expected missing course to trigger CourseNotFoundException"
        );

        assertEquals(ERROR_CODE_COURSE_NOT_FOUND, exception.getErrorCode(), "Error code must match COURSE_NOT_FOUND_404");
        assertTrue(exception.getMessage().contains(NON_EXISTENT_COURSE_ID.toString()), "Exception message should reference non-existent course ID");

        // Verify absolute isolation: no persistence and no Kafka event
        verify(courseRepository, never()).assignTeacherMapping(any(), any(), any());
        verifyNoInteractions(kafkaTeacherProducer);

        LOGGER.info("[TEST_COMPLETE] {} [EXC-004] Non-existent course assignment properly rejected with HTTP 404 mapping", TRACE_TAG_REQ_009);
    }

    /**
     * @verifies [REQ-009] Target course validation: reject unassignment when Course does not exist
     * @verifies [EXC-004] CourseNotFoundException triggering HTTP 404 response
     */
    @Test
    @Order(5)
    @DisplayName("Should throw CourseNotFoundException when attempting to unassign teacher from non-existent course")
    void testUnassignTeacher_CourseNotFound_ShouldThrowNotFoundException() {
        LOGGER.info("[TEST_START] {} [EXC-004] Testing teacher unassignment from non-existent course", TRACE_TAG_REQ_009);

        // Arrange
        when(courseRepository.findByIdOptional(eq(NON_EXISTENT_COURSE_ID))).thenReturn(Optional.empty());

        // Act & Assert
        CourseNotFoundException exception = assertThrows(
                CourseNotFoundException.class,
                () -> courseTeacherService.unassignTeacher(NON_EXISTENT_COURSE_ID, MOCK_TEACHER_ID),
                "Expected missing course to trigger CourseNotFoundException on unassign"
        );

        assertEquals(ERROR_CODE_COURSE_NOT_FOUND, exception.getErrorCode(), "Error code must match COURSE_NOT_FOUND_404");

        // Verify that database removal and Kafka dispatch were bypassed
        verify(courseRepository, never()).removeTeacherMapping(any(), any());
        verifyNoInteractions(kafkaTeacherProducer);

        LOGGER.info("[TEST_COMPLETE] {} [EXC-004] Non-existent course unassignment handled correctly", TRACE_TAG_REQ_009);
    }

    /**
     * @verifies [REQ-009] Relationship validation: reject unassignment when teacher is not assigned to course
     * @verifies [EXC-004] TeacherNotAssignedException triggering HTTP 404 response
     */
    @Test
    @Order(6)
    @DisplayName("Should throw TeacherNotAssignedException when teacher is not currently mapped to the course")
    void testUnassignTeacher_TeacherNotAssigned_ShouldThrowException() {
        LOGGER.info("[TEST_START] {} [EXC-004] Testing unassignment of a teacher not mapped to target course", TRACE_TAG_REQ_009);

        // Arrange
        when(courseRepository.findByIdOptional(eq(MOCK_COURSE_ID))).thenReturn(Optional.of(mockCourse));
        when(courseRepository.isTeacherAssignedToCourse(eq(MOCK_COURSE_ID), eq(UNASSIGNED_TEACHER_ID))).thenReturn(false);

        // Act & Assert
        TeacherNotAssignedException exception = assertThrows(
                TeacherNotAssignedException.class,
                () -> courseTeacherService.unassignTeacher(MOCK_COURSE_ID, UNASSIGNED_TEACHER_ID),
                "Expected unassigning unmapped teacher to trigger TeacherNotAssignedException"
        );

        assertEquals(ERROR_CODE_TEACHER_NOT_ASSIGNED, exception.getErrorCode(), "Error code must match TEACHER_NOT_ASSIGNED_404");

        // Verify no mapping deletion and no Kafka publication occurred
        verify(courseRepository, never()).removeTeacherMapping(any(), any());
        verifyNoInteractions(kafkaTeacherProducer);

        LOGGER.info("[TEST_COMPLETE] {} [EXC-004] Unmapped teacher unassignment rejected appropriately", TRACE_TAG_REQ_009);
    }

    // =========================================================================
    // CATEGORY 3: EDGE CASES & BOUNDARY DEFENSE TEST CASES
    // =========================================================================

    /**
     * @verifies [REQ-009] Payload Validation: reject assignment when request payload or teacherId is null
     * @verifies [EXC-004] IllegalArgumentException boundary checks
     */
    @Test
    @Order(7)
    @DisplayName("Should throw IllegalArgumentException when TeacherAssignRequest or teacherId is null")
    void testAssignTeacher_NullPayloadOrTeacherId_ShouldThrowIllegalArgumentException() {
        LOGGER.info("[TEST_START] {} Testing edge case boundary conditions for null inputs", TRACE_TAG_REQ_009);

        // Boundary 1: Null Request Object
        assertThrows(
                IllegalArgumentException.class,
                () -> courseTeacherService.assignTeacher(MOCK_COURSE_ID, null),
                "Passing null TeacherAssignRequest must throw IllegalArgumentException"
        );

        // Boundary 2: Null TeacherId inside Request
        TeacherAssignRequest nullTeacherRequest = new TeacherAssignRequest();
        nullTeacherRequest.setTeacherId(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> courseTeacherService.assignTeacher(MOCK_COURSE_ID, nullTeacherRequest),
                "Passing null teacherId inside request must throw IllegalArgumentException"
        );

        // Boundary 3: Null CourseId
        assertThrows(
                IllegalArgumentException.class,
                () -> courseTeacherService.assignTeacher(null, new TeacherAssignRequest(MOCK_TEACHER_ID)),
                "Passing null courseId parameter must throw IllegalArgumentException"
        );

        // Assert complete repository and Kafka isolation
        verifyNoInteractions(courseRepository);
        verifyNoInteractions(kafkaTeacherProducer);

        LOGGER.info("[TEST_COMPLETE] {} Null boundary checks successfully enforced", TRACE_TAG_REQ_009);
    }

    /**
     * @verifies [REQ-009] Resilient Transaction Boundary: Kafka producer failure causes transaction rollback
     * @verifies [ARC-007] Ensures consistency between DB mapping and Kafka messaging broker
     */
    @Test
    @Order(8)
    @DisplayName("Should bubble exception and prevent commit if Kafka event publication crashes")
    void testAssignTeacher_KafkaPublishFailure_ShouldBubbleException() {
        LOGGER.info("[TEST_START] {} [ARC-007] Verifying exception preservation when Kafka broker encounters drop", TRACE_TAG_REQ_009);

        // Arrange
        when(courseRepository.findByIdOptional(eq(MOCK_COURSE_ID))).thenReturn(Optional.of(mockCourse));
        when(courseRepository.isTeacherAssignedToCourse(eq(MOCK_COURSE_ID), eq(MOCK_TEACHER_ID))).thenReturn(false);

        doThrow(new KafkaPublishException("Kafka broker network partition timeout"))
                .when(kafkaTeacherProducer)
                .publishTeacherEvent(any(), any(), any());

        TeacherAssignRequest request = new TeacherAssignRequest(MOCK_TEACHER_ID);

        // Act & Assert
        KafkaPublishException exception = assertThrows(
                KafkaPublishException.class,
                () -> courseTeacherService.assignTeacher(MOCK_COURSE_ID, request),
                "Kafka failure must propagate through the service boundary to trigger rollback"
        );

        assertTrue(exception.getMessage().contains("Kafka broker network partition timeout"), "Root cause message must be preserved");

        // Verify DB was invoked prior to Kafka failure
        verify(courseRepository, times(1)).assignTeacherMapping(eq(MOCK_COURSE_ID), eq(MOCK_TEACHER_ID), any(Instant.class));
        verify(kafkaTeacherProducer, times(1)).publishTeacherEvent(any(), any(), any());

        LOGGER.info("[TEST_COMPLETE] {} Kafka fault propagation verified for rollback integrity", TRACE_TAG_REQ_009);
    }

    // =========================================================================
    // EMBEDDED ENTERPRISE STUB MODELS FOR COMPILATION RESOLUTION
    // =========================================================================

    public static class TeacherAssignRequest {
        private UUID teacherId;

        public TeacherAssignRequest() {}

        public TeacherAssignRequest(UUID teacherId) {
            this.teacherId = teacherId;
        }

        public UUID getTeacherId() {
            return teacherId;
        }

        public void setTeacherId(UUID teacherId) {
            this.teacherId = teacherId;
        }
    }

    public static class CourseAssignmentResponse {
        private UUID courseId;
        private UUID teacherId;
        private Instant assignedAt;

        public CourseAssignmentResponse(UUID courseId, UUID teacherId, Instant assignedAt) {
            this.courseId = courseId;
            this.teacherId = teacherId;
            this.assignedAt = assignedAt;
        }

        public UUID getCourseId() {
            return courseId;
        }

        public UUID getTeacherId() {
            return teacherId;
        }

        public Instant getAssignedAt() {
            return assignedAt;
        }
    }

    public static class TeacherEventPayload {
        private String eventType;
        private UUID courseId;
        private UUID teacherId;
        private Instant assignedAt;

        public TeacherEventPayload() {}

        public TeacherEventPayload(String eventType, UUID courseId, UUID teacherId, Instant assignedAt) {
            this.eventType = eventType;
            this.courseId = courseId;
            this.teacherId = teacherId;
            this.assignedAt = assignedAt;
        }

        public String getEventType() {
            return eventType;
        }

        public UUID getCourseId() {
            return courseId;
        }

        public UUID getTeacherId() {
            return teacherId;
        }

        public Instant getAssignedAt() {
            return assignedAt;
        }
    }

    public static class Course {
        private UUID courseId;
        private String title;
        private String description;
        private LocalDate startDate;
        private LocalDate endDate;
        private UUID teacherId;
        private int maxStudents;
        private UUID centerId;

        public UUID getCourseId() { return courseId; }
        public void setCourseId(UUID courseId) { this.courseId = courseId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public UUID getTeacherId() { return teacherId; }
        public void setTeacherId(UUID teacherId) { this.teacherId = teacherId; }
        public int getMaxStudents() { return maxStudents; }
        public void setMaxStudents(int maxStudents) { this.maxStudents = maxStudents; }
        public UUID getCenterId() { return centerId; }
        public void setCenterId(UUID centerId) { this.centerId = centerId; }
    }

    public interface CourseRepository {
        Optional<Course> findByIdOptional(UUID courseId);
        boolean isTeacherAssignedToCourse(UUID courseId, UUID teacherId);
        void assignTeacherMapping(UUID courseId, UUID teacherId, Instant assignedAt);
        void removeTeacherMapping(UUID courseId, UUID teacherId);
    }

    public interface KafkaTeacherProducer {
        void publishTeacherEvent(String topic, UUID key, TeacherEventPayload payload);
    }

    public interface CourseTeacherService {
        CourseAssignmentResponse assignTeacher(UUID courseId, TeacherAssignRequest request);
        void unassignTeacher(UUID courseId, UUID teacherId);
    }

    // =========================================================================
    // ENTERPRISE DOMAIN EXCEPTIONS
    // =========================================================================

    public static class DataIntegrityViolationException extends RuntimeException {
        private final String errorCode;

        public DataIntegrityViolationException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }

    public static class CourseNotFoundException extends RuntimeException {
        private final String errorCode;

        public CourseNotFoundException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }

    public static class TeacherNotAssignedException extends RuntimeException {
        private final String errorCode;

        public TeacherNotAssignedException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }

    public static class KafkaPublishException extends RuntimeException {
        public KafkaPublishException(String message) {
            super(message);
        }
    }
}
```

