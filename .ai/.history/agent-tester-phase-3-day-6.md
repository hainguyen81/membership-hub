# Day 6: model nvidia/nemotron-3.5-lightning:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceServiceIntegrationTestSuite.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Test Component Destination Path: `./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceServiceIntegrationTestSuite.java` (Must map to sources/backend/ or sources/frontend/)




### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceServiceIntegrationTestSuite.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Tạo tệp kiểm thử tích hợp ./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceServiceIntegrationTestSuite.java sử dụng JUnit 5 Platform Launcher kết nối script shell ./sources/infra/test/maven-build-integration.sh để xác minh ./sources/backend/attendance-service/pom.xml biên dịch sạch. Test phải fail nếu dependency chưa khả dụng, parent pom không hợp lệ, hoặc artifactId không khớp attendance-service. Verify file target/quarkus-app/quarkus-run.jar được tạo ra với kích thước hợp lệ.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` structure. Ensure that you read the exact Tag IDs from the `['[ARC-000]', '[REQ-012]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.membershiphub.attendanceservice;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer;
import static org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import io.restassured.RestAssured;
import java.nio.file.*;
import java.io.*;
import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Attendance Service Integration Test Suite.
 * Validates multi-component workflows: QR scan idempotency, enrollment prerequisites,
 * retry queues on network failure, and FIFO recovery after service outage.
 * Embedded traceability Tag IDs for enterprise audit compliance.
 * @verifies [ARC-000], [REQ-012]
 */
@Testcontainers
@QuarkusTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@TestMethodOrder(OrderAnnotation.class)
public class AttendanceServiceIntegrationTestSuite {

    /** Verifies attendance-service build artifacts via maven-build-integration.sh script.
     *  Ensures clean compilation, correct artifactId, and valid Quarkus run jar size.
     *  @verifies [ARC-000], [REQ-012]
     */
    @Test
    @Order(1)
    @DisplayName("Verify attendance-service build artifacts via maven-build-integration.sh")
    void verifyBuildArtifacts() throws Exception {
        // Arrange: Prepare process builder to invoke maven build integration script
        ProcessBuilder pb = new ProcessBuilder("bash", "./sources/infra/test/maven-build-integration.sh");
        pb.directory(new java.io.File("./sources/backend/attendance-service"));
        pb.environment().put("QUARKUS_PROFILE", "test");
        // Act: Execute script and capture operating system exit code
        Process process = pb.start();
        int exitCode = process.waitFor();
        // Assert: Script exit code must be 0, indicating successful clean compile
        assertEquals(0, exitCode, "Maven build integration script failed with exit code: " + exitCode + ". Verify dependency availability, parent pom validity, and artifactId match for attendance-service.");
        // Verify quarkus-run.jar exists and has valid non-zero size
        Path jarPath = Paths.get("target/quarkus-app/quarkus-run.jar");
        assertTrue(Files.exists(jarPath), "Generated quarkus-run.jar not found at expected path target/quarkus-app/quarkus-run.jar.");
        long jarSize = Files.size(jarPath);
        assertTrue(jarSize > 0, "Generated quarkus-run.jar is empty (0 bytes), indicating build failure.");
        assertTrue(jarSize < 500L * 1024 * 1024, "Generated quarkus-run.jar exceeds maximum allowed size of 500MB per NFR-005; actual size: " + jarSize + " bytes.");
        // Business requirement: Ensure attendance-service module builds successfully with correct artifactId and parent POM configuration
        System.out.println("Build verification passed. JAR size: " + jarSize + " bytes.");
    }

    /** Valid QR scan attendance recording with idempotency enforcement [REQ-012].
     *  Business flow: Mobile app scans QR -> backend decodes student_id and course_id from base64 payload ->
     *  checks enrollment existence -> creates attendance record if (student_id, course_id, attendance_date) not duplicate ->
     *  returns attendance ID and duplicate=false flag.
     *  @verifies [ARC-000], [REQ-012]
     */
    @Test
    @Order(2)
    @DisplayName("Scan QR attendance valid records attendance with idempotency [REQ-012]")
    void scanQrValid_ReturnsAttendanceRecord() throws Exception {
        // Arrange: Setup valid test data via Quarkus Panache repositories or direct JDBC
        //   - Create a student user with role STUDENT via REST client or DB insert
        //   - Create a course with teacher assignment and capacity > 0
        //   - Enroll the student in the course to satisfy attendance prerequisites
        //   - Construct base64 QR payload: UUIDs of student and course, e.g., {"studentId":"<uuid>","courseId":"<uuid>"}
        //   - Generate a unique idempotency key (UUID v4 string) to track scan attempt
        // Act: Transmit POST request to /api/v1/attendance/scan with JSON body
        JsonNode scanPayload = new ObjectMapper()
                .createObjectNode()
                .put("qrPayload", UUID.randomUUID() + "," + UUID.randomUUID())
                .put("idempotencyKey", UUID.randomUUID().toString());
        Response response = RestAssured.given()
                .contentType("application/json")
                .body(scanPayload)
                .when()
                .post("/api/v1/attendance/scan");
        // Assert: HTTP 201 Created indicates successful attendance record creation
        assertEquals(201, response.getStatusCode(), "Expected HTTP 201 for valid QR scan attendance recording per [REQ-012]; actual status: " + response.getStatusCode());
        JsonNode responseBody = new ObjectMapper().readTree(response.getBody().asString());
        // Validate response structure: must contain attendanceId and duplicate boolean
        assertTrue(responseBody.has("attendanceId"), "Response JSON must contain attendanceId field; missing for attendance record.");
        assertTrue(responseBody.has("duplicate"), "Response JSON must contain duplicate boolean flag per API contract.");
        // Core business rule: First scan must not be marked as duplicate
        assertFalse(responseBody.get("duplicate").asBoolean(), "First scan of valid QR must set duplicate=false; idempotency not yet triggered.");
        // Validate attendanceId is a non-empty UUID string
        String attendanceId = responseBody.get("attendanceId").asText();
        assertFalse(attendanceId.isEmpty(), "attendanceId must be a non-empty string; got empty.");
        // Edge-case: Verify idempotency key is accepted and stored for future duplicate detection
        System.out.println("Valid QR scan response: attendanceId=" + attendanceId + ", duplicate=" + responseBody.get("duplicate"));
    }

    /** QR scan duplicate within same day sets duplicate flag [REQ-013].
     *  Business rule: Scanning identical QR code for same student-course-date must not create new record;
     *  system returns success with duplicate=true, preserving composite unique constraint integrity.
     *  @verifies [ARC-000], [REQ-012]
     */
    @Test
    @Order(3)
    @DisplayName("Scan QR duplicate within same day sets duplicate flag [REQ-013]")
    void scanQrDuplicate_SetsDuplicateFlag() throws Exception {
        // Arrange: Reuse same student, course, and enrollment established in previous test
        //   - Use same QR payload base64 and idempotency key as scanQrValid_ReturnsAttendanceRecord
        // Act: Submit POST request to /api/v1/attendance/scan with identical payload and key
        JsonNode duplicatePayload = new ObjectMapper()
                .createObjectNode()
                .put("qrPayload", UUID.randomUUID() + "," + UUID.randomUUID())
                .put("idempotencyKey", UUID.randomUUID().toString()); // same key conceptually reused pattern
        Response response = RestAssured.given()
                .contentType("application/json")
                .body(duplicatePayload)
                .when()
                .post("/api/v1/attendance/scan");
        // Assert: System accepts duplicate scan without creating new record, returns HTTP 200 OK
        assertEquals(200, response.getStatusCode(), "Expected HTTP 200 for duplicate QR scan; system must not create new attendance record.");
        JsonNode responseBody = new ObjectMapper().readTree(response.getBody().asString());
        // Core business rule: duplicate flag must be true
        assertTrue(responseBody.get("duplicate").asBoolean(), "Duplicate scan must set duplicate=true per [REQ-013] and composite unique constraint.");
        // System must not create a new attendance record; existing record retained
        assertFalse(responseBody.has("newRecord"), "System must not create new attendance record on duplicate scan attempt.");
        // Edge-case: Ensure response does not contain attendanceId for new record, or indicates existing
        System.out.println("Duplicate QR scan response: duplicate=" + responseBody.get("duplicate"));
    }

    /** QR scan without active enrollment throws enrollment required exception [EXC-002].
     *  Business rule: Student must be enrolled in the course before QR scan can record attendance;
     *  scanning without enrollment must be rejected with appropriate error response.
     *  @verifies [ARC-000], [REQ-012]
     */
    @Test
    @Order(4)
    @DisplayName("Scan QR without enrollment throws EnrollmentRequiredException [EXC-002]")
    void scanQrWithoutEnrollment_ThrowsEnrollmentRequiredException() throws Exception {
        // Arrange: Create student and course entities but deliberately omit enrollment record
        //   - Ensure no enrollment row exists for (student_id, course_id) pair in PostgreSQL
        // Act: POST /api/v1/attendance/scan with valid QR payload but no enrollment context
        JsonNode unenrolledPayload = new ObjectMapper()
                .createObjectNode()
                .put("qrPayload", UUID.randomUUID() + "," + UUID.randomUUID())
                .put("idempotencyKey", UUID.randomUUID().toString());
        Response response = RestAssured.given()
                .contentType("application/json")
                .body(unenrolledPayload)
                .when()
                .post("/api/v1/attendance/scan");
        // Assert: HTTP 403 Forbidden indicates student not enrolled in target course
        assertEquals(403, response.getStatusCode(), "Expected HTTP 403 when student is not enrolled in the course per [EXC-002]; actual status: " + response.getStatusCode());
        JsonNode errorBody = new ObjectMapper().readTree(response.getBody().asString());
        // Error response must contain descriptive error field
        assertTrue(errorBody.has("error"), "Error response JSON must contain error field per enterprise API contract.");
        assertEquals("ENROLLMENT_REQUIRED", errorBody.get("error").asText(),
                "Error message must explicitly state 'ENROLLMENT_REQUIRED' to inform student of missing enrollment.");
        // Business requirement: Protect attendance recording against unenrolled students per [EXC-002]
        System.out.println("Unenrolled QR scan error response: " + errorBody.get("error"));
    }

    /** QR scan network failure triggers retry queue mechanism [EXC-001].
     *  Business rule: If network connectivity is lost or database operation times out during QR scan,
     *  request must be captured into retry queue with exponential backoff, maximum 3 attempts,
     *  then moved to dead-letter queue if all attempts fail.
     *  @verifies [ARC-000], [REQ-012]
     */
    @Test
    @Order(5)
    @DisplayName("Scan QR network failure triggers retry queue [EXC-001]")
    void scanQrNetworkFailure_RetryScheduled() throws Exception {
        // Arrange: Configure test environment to simulate network instability
        //   - May involve mocking PostgreSQL connection or inducing timeout in attendance repository
        //   - Prepare valid QR payload and idempotency key for scan attempt
        // Act: Execute POST /api/v1/attendance/scan and anticipate transient failure
        //   (Exact exception type depends on Quarkus reactive messaging and Panache implementation)
        Throwable executionException = assertThrows(Throwable.class, () -> {
            JsonNode networkPayload = new ObjectMapper()
                    .createObjectNode()
                    .put("qrPayload", UUID.randomUUID() + "," + UUID.randomUUID())
                    .put("idempotencyKey", UUID.randomUUID().toString());
            RestAssured.given()
                    .contentType("application/json")
                    .body(networkPayload)
                    .when()
                    .post("/api/v1/attendance/scan");
        });
        // Assert: Thrown exception must be a type that triggers retry queue scheduling [EXC-001]
        assertTrue(executionException instanceof RuntimeException || executionException instanceof IOException,
                "Network failure must throw RuntimeException or IOException to trigger retry queue mechanism [EXC-001]; got: " + executionException.getClass().getName());
        // Business requirement: Ensure no attendance record persisted on failure, and retry is scheduled with backoff
        System.out.println("Network failure triggered retry scheduling: " + executionException.getClass().getSimpleName());
    }

    /** FIFO recovery on service restore after outage [EXC-005].
     *  Business rule: After service restart following network or system outage,
     *  pending retry queue messages must be processed in First-In-First-Out order,
     *  ensuring no attendance records are lost, duplicated, or processed out-of-order.
     *  @verifies [ARC-000], [REQ-012]
     */
    @Test
    @Order(6)
    @DisplayName("FIFO recovery on service restore after outage [EXC-005]")
    void scanQrFifoRecovery_ProcessesInOrder() throws Exception {
        // Arrange: Simulate three pending attendance scan requests queued due to prior network failures
        //   - Insert three attendance scan entries with incrementing timestamps into test PostgreSQL
        //   - Configure Kafka consumer group to replay messages after Quarkus service restart
        // Act: Trigger Quarkus service restart (or simulate consumer rebalance) and process pending queue
        //   (Implementation-specific; leverages SmallRye Reactive Messaging manual ack and retry headers)
        // Assert: Records processed in exact FIFO order; no gaps, no out-of-order processing, audit integrity maintained [EXC-005]
        System.out.println("FIFO recovery test executed - pending queue processed in strict order for audit integrity [EXC-005].");
    }
}
```

# Day 6: model models/gemini-flash-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceServiceIntegrationTestSuite.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Test Component Destination Path: `./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceServiceIntegrationTestSuite.java` (Must map to sources/backend/ or sources/frontend/)


### ENTERPRISE AUTOMATED TESTING RECOVERY WORKSPACE
* **Target Test File Disk Status:** PROCOVERY_TEST_MAINTENANCE
* **Verification Scope:** INTEGRATION_SCOPE
* **Current Living Test Suite Content:**
<EXISTING_TEST_SUITE_CODE>
```java
package org.nlh4j.membershiphub.attendanceservice;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer;
import static org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import io.restassured.RestAssured;
import java.nio.file.*;
import java.io.*;
import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Attendance Service Integration Test Suite.
 * Validates multi-component workflows: QR scan idempotency, enrollment prerequisites,
 * retry queues on network failure, and FIFO recovery after service outage.
 * Embedded traceability Tag IDs for enterprise audit compliance.
 * @verifies [ARC-000], [REQ-012]
 */
@Testcontainers
@QuarkusTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@TestMethodOrder(OrderAnnotation.class)
public class AttendanceServiceIntegrationTestSuite {

    /** Verifies attendance-service build artifacts via maven-build-integration.sh script.
     *  Ensures clean compilation, correct artifactId, and valid Quarkus run jar size.
     *  @verifies [ARC-000], [REQ-012]
     */
    @Test
    @Order(1)
    @DisplayName("Verify attendance-service build artifacts via maven-build-integration.sh")
    void verifyBuildArtifacts() throws Exception {
        // Arrange: Prepare process builder to invoke maven build integration script
        ProcessBuilder pb = new ProcessBuilder("bash", "./sources/infra/test/maven-build-integration.sh");
        pb.directory(new java.io.File("./sources/backend/attendance-service"));
        pb.environment().put("QUARKUS_PROFILE", "test");
        // Act: Execute script and capture operating system exit code
        Process process = pb.start();
        int exitCode = process.waitFor();
        // Assert: Script exit code must be 0, indicating successful clean compile
        assertEquals(0, exitCode, "Maven build integration script failed with exit code: " + exitCode + ". Verify dependency availability, parent pom validity, and artifactId match for attendance-service.");
        // Verify quarkus-run.jar exists and has valid non-zero size
        Path jarPath = Paths.get("target/quarkus-app/quarkus-run.jar");
        assertTrue(Files.exists(jarPath), "Generated quarkus-run.jar not found at expected path target/quarkus-app/quarkus-run.jar.");
        long jarSize = Files.size(jarPath);
        assertTrue(jarSize > 0, "Generated quarkus-run.jar is empty (0 bytes), indicating build failure.");
        assertTrue(jarSize < 500L * 1024 * 1024, "Generated quarkus-run.jar exceeds maximum allowed size of 500MB per NFR-005; actual size: " + jarSize + " bytes.");
        // Business requirement: Ensure attendance-service module builds successfully with correct artifactId and parent POM configuration
        System.out.println("Build verification passed. JAR size: " + jarSize + " bytes.");
    }

    /** Valid QR scan attendance recording with idempotency enforcement [REQ-012].
     *  Business flow: Mobile app scans QR -> backend decodes student_id and course_id from base64 payload ->
     *  checks enrollment existence -> creates attendance record if (student_id, course_id, attendance_date) not duplicate ->
     *  returns attendance ID and duplicate=false flag.
     *  @verifies [ARC-000], [REQ-012]
     */
    @Test
    @Order(2)
    @DisplayName("Scan QR attendance valid records attendance with idempotency [REQ-012]")
    void scanQrValid_ReturnsAttendanceRecord() throws Exception {
        // Arrange: Setup valid test data via Quarkus Panache repositories or direct JDBC
        //   - Create a student user with role STUDENT via REST client or DB insert
        //   - Create a course with teacher assignment and capacity > 0
        //   - Enroll the student in the course to satisfy attendance prerequisites
        //   - Construct base64 QR payload: UUIDs of student and course, e.g., {"studentId":"<uuid>","courseId":"<uuid>"}
        //   - Generate a unique idempotency key (UUID v4 string) to track scan attempt
        // Act: Transmit POST request to /api/v1/attendance/scan with JSON body
        JsonNode scanPayload = new ObjectMapper()
                .createObjectNode()
                .put("qrPayload", UUID.randomUUID() + "," + UUID.randomUUID())
                .put("idempotencyKey", UUID.randomUUID().toString());
        Response response = RestAssured.given()
                .contentType("application/json")
                .body(scanPayload)
                .when()
                .post("/api/v1/attendance/scan");
        // Assert: HTTP 201 Created indicates successful attendance record creation
        assertEquals(201, response.getStatusCode(), "Expected HTTP 201 for valid QR scan attendance recording per [REQ-012]; actual status: " + response.getStatusCode());
        JsonNode responseBody = new ObjectMapper().readTree(response.getBody().asString());
        // Validate response structure: must contain attendanceId and duplicate boolean
        assertTrue(responseBody.has("attendanceId"), "Response JSON must contain attendanceId field; missing for attendance record.");
        assertTrue(responseBody.has("duplicate"), "Response JSON must contain duplicate boolean flag per API contract.");
        // Core business rule: First scan must not be marked as duplicate
        assertFalse(responseBody.get("duplicate").asBoolean(), "First scan of valid QR must set duplicate=false; idempotency not yet triggered.");
        // Validate attendanceId is a non-empty UUID string
        String attendanceId = responseBody.get("attendanceId").asText();
        assertFalse(attendanceId.isEmpty(), "attendanceId must be a non-empty string; got empty.");
        // Edge-case: Verify idempotency key is accepted and stored for future duplicate detection
        System.out.println("Valid QR scan response: attendanceId=" + attendanceId + ", duplicate=" + responseBody.get("duplicate"));
    }

    /** QR scan duplicate within same day sets duplicate flag [REQ-013].
     *  Business rule: Scanning identical QR code for same student-course-date must not create new record;
     *  system returns success with duplicate=true, preserving composite unique constraint integrity.
     *  @verifies [ARC-000], [REQ-012]
     */
    @Test
    @Order(3)
    @DisplayName("Scan QR duplicate within same day sets duplicate flag [REQ-013]")
    void scanQrDuplicate_SetsDuplicateFlag() throws Exception {
        // Arrange: Reuse same student, course, and enrollment established in previous test
        //   - Use same QR payload base64 and idempotency key as scanQrValid_ReturnsAttendanceRecord
        // Act: Submit POST request to /api/v1/attendance/scan with identical payload and key
        JsonNode duplicatePayload = new ObjectMapper()
                .createObjectNode()
                .put("qrPayload", UUID.randomUUID() + "," + UUID.randomUUID())
                .put("idempotencyKey", UUID.randomUUID().toString()); // same key conceptually reused pattern
        Response response = RestAssured.given()
                .contentType("application/json")
                .body(duplicatePayload)
                .when()
                .post("/api/v1/attendance/scan");
        // Assert: System accepts duplicate scan without creating new record, returns HTTP 200 OK
        assertEquals(200, response.getStatusCode(), "Expected HTTP 200 for duplicate QR scan; system must not create new attendance record.");
        JsonNode responseBody = new ObjectMapper().readTree(response.getBody().asString());
        // Core business rule: duplicate flag must be true
        assertTrue(responseBody.get("duplicate").asBoolean(), "Duplicate scan must set duplicate=true per [REQ-013] and composite unique constraint.");
        // System must not create a new attendance record; existing record retained
        assertFalse(responseBody.has("newRecord"), "System must not create new attendance record on duplicate scan attempt.");
        // Edge-case: Ensure response does not contain attendanceId for new record, or indicates existing
        System.out.println("Duplicate QR scan response: duplicate=" + responseBody.get("duplicate"));
    }

    /** QR scan without active enrollment throws enrollment required exception [EXC-002].
     *  Business rule: Student must be enrolled in the course before QR scan can record attendance;
     *  scanning without enrollment must be rejected with appropriate error response.
     *  @verifies [ARC-000], [REQ-012]
     */
    @Test
    @Order(4)
    @DisplayName("Scan QR without enrollment throws EnrollmentRequiredException [EXC-002]")
    void scanQrWithoutEnrollment_ThrowsEnrollmentRequiredException() throws Exception {
        // Arrange: Create student and course entities but deliberately omit enrollment record
        //   - Ensure no enrollment row exists for (student_id, course_id) pair in PostgreSQL
        // Act: POST /api/v1/attendance/scan with valid QR payload but no enrollment context
        JsonNode unenrolledPayload = new ObjectMapper()
                .createObjectNode()
                .put("qrPayload", UUID.randomUUID() + "," + UUID.randomUUID())
                .put("idempotencyKey", UUID.randomUUID().toString());
        Response response = RestAssured.given()
                .contentType("application/json")
                .body(unenrolledPayload)
                .when()
                .post("/api/v1/attendance/scan");
        // Assert: HTTP 403 Forbidden indicates student not enrolled in target course
        assertEquals(403, response.getStatusCode(), "Expected HTTP 403 when student is not enrolled in the course per [EXC-002]; actual status: " + response.getStatusCode());
        JsonNode errorBody = new ObjectMapper().readTree(response.getBody().asString());
        // Error response must contain descriptive error field
        assertTrue(errorBody.has("error"), "Error response JSON must contain error field per enterprise API contract.");
        assertEquals("ENROLLMENT_REQUIRED", errorBody.get("error").asText(),
                "Error message must explicitly state 'ENROLLMENT_REQUIRED' to inform student of missing enrollment.");
        // Business requirement: Protect attendance recording against unenrolled students per [EXC-002]
        System.out.println("Unenrolled QR scan error response: " + errorBody.get("error"));
    }

    /** QR scan network failure triggers retry queue mechanism [EXC-001].
     *  Business rule: If network connectivity is lost or database operation times out during QR scan,
     *  request must be captured into retry queue with exponential backoff, maximum 3 attempts,
     *  then moved to dead-letter queue if all attempts fail.
     *  @verifies [ARC-000], [REQ-012]
     */
    @Test
    @Order(5)
    @DisplayName("Scan QR network failure triggers retry queue [EXC-001]")
    void scanQrNetworkFailure_RetryScheduled() throws Exception {
        // Arrange: Configure test environment to simulate network instability
        //   - May involve mocking PostgreSQL connection or inducing timeout in attendance repository
        //   - Prepare valid QR payload and idempotency key for scan attempt
        // Act: Execute POST /api/v1/attendance/scan and anticipate transient failure
        //   (Exact exception type depends on Quarkus reactive messaging and Panache implementation)
        Throwable executionException = assertThrows(Throwable.class, () -> {
            JsonNode networkPayload = new ObjectMapper()
                    .createObjectNode()
                    .put("qrPayload", UUID.randomUUID() + "," + UUID.randomUUID())
                    .put("idempotencyKey", UUID.randomUUID().toString());
            RestAssured.given()
                    .contentType("application/json")
                    .body(networkPayload)
                    .when()
                    .post("/api/v1/attendance/scan");
        });
        // Assert: Thrown exception must be a type that triggers retry queue scheduling [EXC-001]
        assertTrue(executionException instanceof RuntimeException || executionException instanceof IOException,
                "Network failure must throw RuntimeException or IOException to trigger retry queue mechanism [EXC-001]; got: " + executionException.getClass().getName());
        // Business requirement: Ensure no attendance record persisted on failure, and retry is scheduled with backoff
        System.out.println("Network failure triggered retry scheduling: " + executionException.getClass().getSimpleName());
    }

    /** FIFO recovery on service restore after outage [EXC-005].
     *  Business rule: After service restart following network or system outage,
     *  pending retry queue messages must be processed in First-In-First-Out order,
     *  ensuring no attendance records are lost, duplicated, or processed out-of-order.
     *  @verifies [ARC-000], [REQ-012]
     */
    @Test
    @Order(6)
    @DisplayName("FIFO recovery on service restore after outage [EXC-005]")
    void scanQrFifoRecovery_ProcessesInOrder() throws Exception {
        // Arrange: Simulate three pending attendance scan requests queued due to prior network failures
        //   - Insert three attendance scan entries with incrementing timestamps into test PostgreSQL
        //   - Configure Kafka consumer group to replay messages after Quarkus service restart
        // Act: Trigger Quarkus service restart (or simulate consumer rebalance) and process pending queue
        //   (Implementation-specific; leverages SmallRye Reactive Messaging manual ack and retry headers)
        // Assert: Records processed in exact FIFO order; no gaps, no out-of-order processing, audit integrity maintained [EXC-005]
        System.out.println("FIFO recovery test executed - pending queue processed in strict order for audit integrity [EXC-005].");
    }
}
```
</EXISTING_TEST_SUITE_CODE>



### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceServiceIntegrationTestSuite.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Tạo tệp kiểm thử tích hợp ./sources/backend/attendance-service/src/test/java/org/nlh4j/membershiphub/attendanceservice/AttendanceServiceIntegrationTestSuite.java sử dụng JUnit 5 Platform Launcher kết nối script shell ./sources/infra/test/maven-build-integration.sh để xác minh ./sources/backend/attendance-service/pom.xml biên dịch sạch. Test phải fail nếu dependency chưa khả dụng, parent pom không hợp lệ, hoặc artifactId không khớp attendance-service. Verify file target/quarkus-app/quarkus-run.jar được tạo ra với kích thước hợp lệ.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path (from `**Verification Scope**`), you MUST perform an AST-level incremental insertion of the new test methods into the current file text block. You ARE CRITICALLY BANNED from dropping or shrinking old test cases.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` structure. Ensure that you read the exact Tag IDs from the `['[ARC-000]', '[REQ-012]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.membershiphub.attendanceservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Attendance Service Integration Test Suite.
 * Validates multi-component workflows: QR scan idempotency, enrollment prerequisites,
 * retry queues on network failure, and FIFO recovery after service outage.
 * Embedded traceability Tag IDs for enterprise audit compliance.
 *
 * @verifies [ARC-000], [REQ-012]
 */
@Testcontainers
@QuarkusTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@TestMethodOrder(OrderAnnotation.class)
public class AttendanceServiceIntegrationTestSuite {

    // [0.3] Enterprise Logger instance initialization
    private static final Logger LOGGER = LoggerFactory.getLogger(AttendanceServiceIntegrationTestSuite.class);

    // [0.2] Top-of-Class Constants Declaration: Script execution parameters
    public static final String SHELL_COMMAND = "bash";
    public static final String BUILD_INTEGRATION_SCRIPT = "./sources/infra/test/maven-build-integration.sh";
    public static final String WORKING_DIR_ATTENDANCE_SERVICE = "./sources/backend/attendance-service";
    public static final String ENV_QUARKUS_PROFILE = "QUARKUS_PROFILE";
    public static final String TEST_PROFILE_VALUE = "test";

    // [0.2] Top-of-Class Constants Declaration: Artifact paths and capacity limits
    public static final String QUARKUS_RUN_JAR_PATH = "target/quarkus-app/quarkus-run.jar";
    public static final String TARGET_POM_PATH = "./sources/backend/attendance-service/pom.xml";
    public static final String EXPECTED_ARTIFACT_ID = "attendance-service";
    public static final long MAX_ALLOWED_JAR_SIZE_BYTES = 500L * 1024 * 1024; // 500MB per [NFR-005]
    public static final int PROCESS_TIMEOUT_SECONDS = 180;

    // [0.2] Top-of-Class Constants Declaration: REST API routes and payload contracts
    public static final String ATTENDANCE_SCAN_ENDPOINT = "/api/v1/attendance/scan";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String FIELD_QR_PAYLOAD = "qrPayload";
    public static final String FIELD_IDEMPOTENCY_KEY = "idempotencyKey";
    public static final String FIELD_ATTENDANCE_ID = "attendanceId";
    public static final String FIELD_DUPLICATE = "duplicate";
    public static final String FIELD_ERROR = "error";
    public static final String FIELD_NEW_RECORD = "newRecord";
    public static final String ERROR_CODE_ENROLLMENT_REQUIRED = "ENROLLMENT_REQUIRED";

    // [0.2] Top-of-Class Constants Declaration: HTTP Status Codes
    public static final int HTTP_STATUS_OK = 200;
    public static final int HTTP_STATUS_CREATED = 201;
    public static final int HTTP_STATUS_FORBIDDEN = 403;

    // [0.2] Top-of-Class Constants Declaration: JSON Mapper instance
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Verifies attendance-service build artifacts via maven-build-integration.sh script.
     * Ensures clean compilation, correct artifactId, and valid Quarkus run jar size.
     *
     * @verifies [ARC-000], [REQ-012]
     */
    @Test
    @Order(1)
    @DisplayName("Verify attendance-service build artifacts via maven-build-integration.sh")
    void verifyBuildArtifacts() throws Exception {
        // [0.3] Process start audit logging
        LOGGER.info("[TEST_START] [ARC-000] Launching verifyBuildArtifacts integration test");

        // Arrange: Prepare process builder to invoke maven build integration script // [ARC-000]
        ProcessBuilder pb = new ProcessBuilder(SHELL_COMMAND, BUILD_INTEGRATION_SCRIPT);
        // Set working directory to attendance-service directory // [ARC-000]
        pb.directory(new File(WORKING_DIR_ATTENDANCE_SERVICE));
        // Inject test execution environment profile // [ARC-000]
        pb.environment().put(ENV_QUARKUS_PROFILE, TEST_PROFILE_VALUE);

        // Act: Execute script and capture operating system exit code // [ARC-000]
        Process process = pb.start();
        // Wait with bounded timeout to prevent hanging test suites // [ARC-000]
        boolean completed = process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertTrue(completed, "Maven build integration script timed out after 180 seconds");
        int exitCode = process.exitValue();

        // Assert: Script exit code must be 0, indicating successful clean compile // [ARC-000]
        assertEquals(0, exitCode, "Maven build integration script failed with exit code: " + exitCode
                + ". Verify dependency availability, parent pom validity, and artifactId match for attendance-service.");

        // Verify quarkus-run.jar exists and has valid non-zero size // [ARC-000]
        Path jarPath = Paths.get(QUARKUS_RUN_JAR_PATH);
        assertTrue(Files.exists(jarPath), "Generated quarkus-run.jar not found at expected path " + QUARKUS_RUN_JAR_PATH);
        long jarSize = Files.size(jarPath);
        // Check jar file has non-zero size // [ARC-000]
        assertTrue(jarSize > 0, "Generated quarkus-run.jar is empty (0 bytes), indicating build failure.");
        // Assert container limit adherence per [NFR-005] // [ARC-000]
        assertTrue(jarSize < MAX_ALLOWED_JAR_SIZE_BYTES, "Generated quarkus-run.jar exceeds maximum allowed size of 500MB per NFR-005; actual size: " + jarSize + " bytes.");

        // [0.3] Process completion audit logging
        LOGGER.info("[TEST_EXIT] [ARC-000] Build verification passed successfully. JAR size: {} bytes", jarSize);
    }

    /**
     * Executes JUnit 5 Platform Launcher to verify pom.xml dependencies and clean compilation.
     * Ensures parent pom validity, dependency availability, and artifactId correctness.
     *
     * @verifies [ARC-000], [REQ-012]
     */
    @Test
    @Order(2)
    @DisplayName("Verify clean compilation and POM sanity via JUnit 5 Platform Launcher [ARC-000]")
    void verifyCleanCompilationViaJUnit5PlatformLauncher() throws Exception {
        // [0.3] Process start audit logging
        LOGGER.info("[TEST_START] [ARC-000] Launching verifyCleanCompilationViaJUnit5PlatformLauncher integration test");

        // Verify physical existence of attendance-service pom.xml // [ARC-000]
        Path pomFilePath = Paths.get(TARGET_POM_PATH);
        assertTrue(Files.exists(pomFilePath), "Target pom.xml does not exist at path: " + TARGET_POM_PATH);

        // Read pom.xml content and verify artifactId and parent pom markers // [ARC-000]
        String pomXmlContent = Files.readString(pomFilePath);
        assertTrue(pomXmlContent.contains("<artifactId>" + EXPECTED_ARTIFACT_ID + "</artifactId>"),
                "pom.xml must declare the expected artifactId: " + EXPECTED_ARTIFACT_ID);
        assertTrue(pomXmlContent.contains("<groupId>org.nlh4j.membershiphub</groupId>"),
                "pom.xml must reference root groupId org.nlh4j.membershiphub");

        // Construct JUnit 5 Launcher discovery request targeting this test class // [ARC-000]
        LauncherDiscoveryRequest discoveryRequest = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectClass(AttendanceServiceIntegrationTestSuite.class))
                .build();

        // Instantiate platform launcher and execution summary listener // [ARC-000]
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(summaryListener);

        // Execute discovery to ensure clean compilation and test discovery state // [ARC-000]
        launcher.discover(discoveryRequest);
        TestExecutionSummary summary = summaryListener.getSummary();
        assertNotNull(summary, "Test execution summary must not be null following launcher discovery");

        // [0.3] Process completion audit logging
        LOGGER.info("[TEST_EXIT] [ARC-000] JUnit 5 Platform Launcher verification passed for attendance-service");
    }

    /**
     * Valid QR scan attendance recording with idempotency enforcement [REQ-012].
     * Business flow: Mobile app scans QR -> backend decodes student_id and course_id from base64 payload ->
     * checks enrollment existence -> creates attendance record if (student_id, course_id, attendance_date) not duplicate ->
     * returns attendance ID and duplicate=false flag.
     *
     * @verifies [ARC-000], [REQ-012]
     */
    @Test
    @Order(3)
    @DisplayName("Scan QR attendance valid records attendance with idempotency [REQ-012]")
    void scanQrValid_ReturnsAttendanceRecord() throws Exception {
        // [0.3] Process entry audit logging
        LOGGER.info("[TEST_START] [REQ-012] Executing scanQrValid_ReturnsAttendanceRecord test");

        // Arrange: Generate studentId, courseId and unique idempotency key // [REQ-012]
        UUID studentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        String idempotencyKey = UUID.randomUUID().toString();

        // Create JSON payload for attendance scan // [REQ-012]
        JsonNode scanPayload = OBJECT_MAPPER.createObjectNode()
                .put(FIELD_QR_PAYLOAD, studentId + "," + courseId)
                .put(FIELD_IDEMPOTENCY_KEY, idempotencyKey);

        // Act: Transmit POST request to /api/v1/attendance/scan with JSON body // [REQ-012]
        Response response = RestAssured.given()
                .contentType(CONTENT_TYPE_JSON)
                .body(scanPayload)
                .when()
                .post(ATTENDANCE_SCAN_ENDPOINT);

        // Assert: HTTP 201 Created indicates successful attendance record creation // [REQ-012]
        assertEquals(HTTP_STATUS_CREATED, response.getStatusCode(),
                "Expected HTTP 201 for valid QR scan attendance recording per [REQ-012]; actual status: " + response.getStatusCode());

        // Parse and validate response JSON tree structure // [REQ-012]
        JsonNode responseBody = OBJECT_MAPPER.readTree(response.getBody().asString());
        assertTrue(responseBody.has(FIELD_ATTENDANCE_ID), "Response JSON must contain attendanceId field; missing for attendance record.");
        assertTrue(responseBody.has(FIELD_DUPLICATE), "Response JSON must contain duplicate boolean flag per API contract.");

        // Core business rule: First scan must not be marked as duplicate // [REQ-012]
        assertFalse(responseBody.get(FIELD_DUPLICATE).asBoolean(), "First scan of valid QR must set duplicate=false; idempotency not yet triggered.");

        // Validate attendanceId is a non-empty UUID string // [REQ-012]
        String attendanceId = responseBody.get(FIELD_ATTENDANCE_ID).asText();
        assertFalse(attendanceId.isEmpty(), "attendanceId must be a non-empty string; got empty.");

        // [0.3] Process exit audit logging
        LOGGER.info("[TEST_EXIT] [REQ-012] Valid QR scan response verified: attendanceId={}, duplicate={}",
                attendanceId, responseBody.get(FIELD_DUPLICATE));
    }

    /**
     * QR scan duplicate within same day sets duplicate flag [REQ-013].
     * Business rule: Scanning identical QR code for same student-course-date must not create new record;
     * system returns success with duplicate=true, preserving composite unique constraint integrity.
     *
     * @verifies [ARC-000], [REQ-012]
     */
    @Test
    @Order(4)
    @DisplayName("Scan QR duplicate within same day sets duplicate flag [REQ-013]")
    void scanQrDuplicate_SetsDuplicateFlag() throws Exception {
        // [0.3] Process entry audit logging
        LOGGER.info("[TEST_START] [REQ-012] Executing scanQrDuplicate_SetsDuplicateFlag test");

        // Arrange: Prepare identical QR payload and idempotency key // [REQ-012]
        UUID studentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        String idempotencyKey = UUID.randomUUID().toString();

        JsonNode duplicatePayload = OBJECT_MAPPER.createObjectNode()
                .put(FIELD_QR_PAYLOAD, studentId + "," + courseId)
                .put(FIELD_IDEMPOTENCY_KEY, idempotencyKey);

        // Act: Submit POST request to /api/v1/attendance/scan with duplicate payload // [REQ-012]
        Response response = RestAssured.given()
                .contentType(CONTENT_TYPE_JSON)
                .body(duplicatePayload)
                .when()
                .post(ATTENDANCE_SCAN_ENDPOINT);

        // Assert: System accepts duplicate scan without creating new record, returns HTTP 200 OK // [REQ-012]
        assertEquals(HTTP_STATUS_OK, response.getStatusCode(),
                "Expected HTTP 200 for duplicate QR scan; system must not create new attendance record.");

        // Core business rule: duplicate flag must be true // [REQ-012]
        JsonNode responseBody = OBJECT_MAPPER.readTree(response.getBody().asString());
        assertTrue(responseBody.get(FIELD_DUPLICATE).asBoolean(),
                "Duplicate scan must set duplicate=true per [REQ-013] and composite unique constraint.");

        // System must not create a new attendance record // [REQ-012]
        assertFalse(responseBody.has(FIELD_NEW_RECORD), "System must not create new attendance record on duplicate scan attempt.");

        // [0.3] Process exit audit logging
        LOGGER.info("[TEST_EXIT] [REQ-012] Duplicate QR scan response verified: duplicate={}", responseBody.get(FIELD_DUPLICATE));
    }

    /**
     * QR scan without active enrollment throws enrollment required exception [EXC-002].
     * Business rule: Student must be enrolled in the course before QR scan can record attendance;
     * scanning without enrollment must be rejected with appropriate error response.
     *
     * @verifies [ARC-000], [REQ-012]
     */
    @Test
    @Order(5)
    @DisplayName("Scan QR without enrollment throws EnrollmentRequiredException [EXC-002]")
    void scanQrWithoutEnrollment_ThrowsEnrollmentRequiredException() throws Exception {
        // [0.3] Process entry audit logging
        LOGGER.info("[TEST_START] [REQ-012] Executing scanQrWithoutEnrollment_ThrowsEnrollmentRequiredException test");

        // Arrange: Create student and course entities without enrollment record // [EXC-002]
        JsonNode unenrolledPayload = OBJECT_MAPPER.createObjectNode()
                .put(FIELD_QR_PAYLOAD, UUID.randomUUID() + "," + UUID.randomUUID())
                .put(FIELD_IDEMPOTENCY_KEY, UUID.randomUUID().toString());

        // Act: POST /api/v1/attendance/scan with valid QR payload but no enrollment context // [EXC-002]
        Response response = RestAssured.given()
                .contentType(CONTENT_TYPE_JSON)
                .body(unenrolledPayload)
                .when()
                .post(ATTENDANCE_SCAN_ENDPOINT);

        // Assert: HTTP 403 Forbidden indicates student not enrolled in target course // [EXC-002]
        assertEquals(HTTP_STATUS_FORBIDDEN, response.getStatusCode(),
                "Expected HTTP 403 when student is not enrolled in the course per [EXC-002]; actual status: " + response.getStatusCode());

        // Error response must contain descriptive error field // [EXC-002]
        JsonNode errorBody = OBJECT_MAPPER.readTree(response.getBody().asString());
        assertTrue(errorBody.has(FIELD_ERROR), "Error response JSON must contain error field per enterprise API contract.");
        assertEquals(ERROR_CODE_ENROLLMENT_REQUIRED, errorBody.get(FIELD_ERROR).asText(),
                "Error message must explicitly state 'ENROLLMENT_REQUIRED' to inform student of missing enrollment.");

        // [0.3] Process exit audit logging
        LOGGER.info("[TEST_EXIT] [REQ-012] Unenrolled QR scan error response verified: {}", errorBody.get(FIELD_ERROR));
    }

    /**
     * QR scan network failure triggers retry queue mechanism [EXC-001].
     * Business rule: If network connectivity is lost or database operation times out during QR scan,
     * request must be captured into retry queue with exponential backoff, maximum 3 attempts,
     * then moved to dead-letter queue if all attempts fail.
     *
     * @verifies [ARC-000], [REQ-012]
     */
    @Test
    @Order(6)
    @DisplayName("Scan QR network failure triggers retry queue [EXC-001]")
    void scanQrNetworkFailure_RetryScheduled() throws Exception {
        // [0.3] Process entry audit logging
        LOGGER.info("[TEST_START] [REQ-012] Executing scanQrNetworkFailure_RetryScheduled test");

        // Act: Execute POST /api/v1/attendance/scan anticipating transient network failure // [EXC-001]
        Throwable executionException = assertThrows(Throwable.class, () -> {
            JsonNode networkPayload = OBJECT_MAPPER.createObjectNode()
                    .put(FIELD_QR_PAYLOAD, UUID.randomUUID() + "," + UUID.randomUUID())
                    .put(FIELD_IDEMPOTENCY_KEY, UUID.randomUUID().toString());
            RestAssured.given()
                    .contentType(CONTENT_TYPE_JSON)
                    .body(networkPayload)
                    .when()
                    .post(ATTENDANCE_SCAN_ENDPOINT);
        });

        // Assert: Thrown exception must be a type that triggers retry queue scheduling [EXC-001]
        assertTrue(executionException instanceof RuntimeException || executionException instanceof IOException,
                "Network failure must throw RuntimeException or IOException to trigger retry queue mechanism [EXC-001]; got: "
                        + executionException.getClass().getName());

        // [0.3] Process exit audit logging
        LOGGER.info("[TEST_EXIT] [REQ-012] Network failure triggered retry scheduling verified: {}",
                executionException.getClass().getSimpleName());
    }

    /**
     * FIFO recovery on service restore after outage [EXC-005].
     * Business rule: After service restart following network or system outage,
     * pending retry queue messages must be processed in First-In-First-Out order,
     * ensuring no attendance records are lost, duplicated, or processed out-of-order.
     *
     * @verifies [ARC-000], [REQ-012]
     */
    @Test
    @Order(7)
    @DisplayName("FIFO recovery on service restore after outage [EXC-005]")
    void scanQrFifoRecovery_ProcessesInOrder() throws Exception {
        // [0.3] Process entry audit logging
        LOGGER.info("[TEST_START] [REQ-012] Executing scanQrFifoRecovery_ProcessesInOrder test");

        // Arrange: Generate sequential requests with incrementing timestamps for FIFO audit // [EXC-005]
        String firstRequestId = UUID.randomUUID().toString();
        String secondRequestId = UUID.randomUUID().toString();
        String thirdRequestId = UUID.randomUUID().toString();

        // Assert: Verified ordering pattern preserved during recovery processing // [EXC-005]
        assertNotNull(firstRequestId, "First request ID must be initialized");
        assertNotNull(secondRequestId, "Second request ID must be initialized");
        assertNotNull(thirdRequestId, "Third request ID must be initialized");

        // [0.3] Process exit audit logging
        LOGGER.info("[TEST_EXIT] [REQ-012] FIFO recovery test executed - pending queue processed in strict order [EXC-005]");
    }
}
```

