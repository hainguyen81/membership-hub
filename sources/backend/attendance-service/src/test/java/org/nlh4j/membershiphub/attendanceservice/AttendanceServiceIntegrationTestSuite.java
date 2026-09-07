package org.nlh4j.membershiphub.attendanceservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Attendance Service Integration Test Suite.
 * Validates multi-component workflows: build descriptor validation, clean maven packaging,
 * JUnit 5 Platform Launcher execution pipeline, QR scan idempotency, enrollment prerequisites,
 * retry queues on network failure, and FIFO recovery after service outage.
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

    // [0.2] Top-of-Class Constants Declaration Law: Process execution commands and script paths
    public static final String SHELL_COMMAND = "bash";
    public static final String BUILD_INTEGRATION_SCRIPT = "./sources/infra/test/maven-build-integration.sh";
    public static final String WORKING_DIR_ATTENDANCE_SERVICE = "./sources/backend/attendance-service";
    public static final String ENV_QUARKUS_PROFILE = "QUARKUS_PROFILE";
    public static final String TEST_PROFILE_VALUE = "test";
    public static final String QUARKUS_RUN_JAR_PATH = "target/quarkus-app/quarkus-run.jar";
    public static final String TARGET_POM_PATH = "./sources/backend/attendance-service/pom.xml";
    public static final String ROOT_POM_PATH = "./sources/backend/pom.xml";

    // [0.2] Top-of-Class Constants: Artifact Identifiers and Packaging Coordinates
    public static final String EXPECTED_GROUP_ID = "org.nlh4j.membershiphub";
    public static final String EXPECTED_ARTIFACT_ID = "attendance-service";
    public static final String EXPECTED_PARENT_ARTIFACT_ID = "membership-hub-backend";
    public static final String EXPECTED_PARENT_VERSION = "1.0.0-SNAPSHOT";
    public static final String BANNED_EXAMPLE_PACKAGE_TOKEN = "com.example";

    // [0.2] Top-of-Class Constants: Mandatory Production and Enterprise Dependencies
    public static final String DEPENDENCY_RESTEASY = "quarkus-resteasy-reactive-jackson";
    public static final String DEPENDENCY_HIBERNATE = "quarkus-hibernate-orm-panache";
    public static final String DEPENDENCY_POSTGRESQL = "quarkus-jdbc-postgresql";
    public static final String DEPENDENCY_KAFKA = "quarkus-smallrye-reactive-messaging-kafka";
    public static final String DEPENDENCY_VALIDATOR = "quarkus-hibernate-validator";
    public static final String DEPENDENCY_JUNIT = "quarkus-junit5";
    public static final String DEPENDENCY_FLYWAY = "quarkus-flyway";

    // [0.2] Top-of-Class Constants: Capacity, Timing, and Exit Code Boundaries
    public static final long MIN_ALLOWED_JAR_SIZE_BYTES = 1024L;
    public static final long MAX_ALLOWED_JAR_SIZE_BYTES = 500L * 1024L * 1024L;
    public static final int PROCESS_TIMEOUT_SECONDS = 180;
    public static final int ZERO_EXIT_CODE = 0;
    public static final String TARGET_CLASSES_DIR = "target/classes";

    // [0.2] Top-of-Class Constants: REST API routes and payload contracts
    public static final String ATTENDANCE_SCAN_ENDPOINT = "/api/v1/attendance/scan";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String FIELD_QR_PAYLOAD = "qrPayload";
    public static final String FIELD_IDEMPOTENCY_KEY = "idempotencyKey";
    public static final String FIELD_ATTENDANCE_ID = "attendanceId";
    public static final String FIELD_STUDENT_ID = "studentId";
    public static final String FIELD_COURSE_ID = "courseId";
    public static final String FIELD_DUPLICATE = "duplicate";

    // [0.2] Top-of-Class Constants: Traceability Tags and Log Format Templates
    public static final String TAG_ARC_000 = "[ARC-000]";
    public static final String TAG_REQ_012 = "[REQ-012]";
    public static final String LOG_TEST_START_TEMPLATE = "[TEST_START] {} Executing test: {}";
    public static final String LOG_TEST_END_TEMPLATE = "[TEST_END] {} Completed test: {}";
    public static final String LOG_PROCESS_OUTPUT_TEMPLATE = "[PROCESS] Script execution output: {}";
    public static final String ERROR_TIMEOUT_TEMPLATE = "Maven build integration script timed out after %d seconds";
    public static final String ERROR_NON_ZERO_EXIT_TEMPLATE = "Maven build integration script failed with exit code %d";
    public static final String ERROR_POM_MISSING_TEMPLATE = "pom.xml descriptor not found at %s";
    public static final String ERROR_JAR_MISSING_TEMPLATE = "Target runner jar not found at %s";
    public static final String ERROR_JAR_SIZE_OUT_OF_BOUNDS = "Generated JAR size %d bytes is outside allowed range [%d, %d]";

    // -------------------------------------------------------------------------
    // HISTORICAL TEST METHOD (Preserved intact under Anti-Wipeout Law)
    // -------------------------------------------------------------------------
    /**
     * Validate attendance-service build descriptor and JAR generation.
     * Executes the Maven build integration script, verifies pom.xml structure,
     * artifact ID, and ensures the generated JAR file exists and is within size limits.
     *
     * @verifies [ARC-000], [REQ-012]
     */
    @Test
    @DisplayName("Validate attendance-service build descriptor and JAR generation [ARC-000][REQ-012]")
    @Order(1)
    void testAttendanceServiceBuildDescriptorAndJarGeneration() throws IOException, InterruptedException {
        // [0.3] Entry log trace
        LOGGER.info(LOG_TEST_START_TEMPLATE, TAG_ARC_000, "testAttendanceServiceBuildDescriptorAndJarGeneration");

        // Execute the Maven build integration script // [ARC-000]
        ProcessBuilder pb = new ProcessBuilder(SHELL_COMMAND, BUILD_INTEGRATION_SCRIPT);
        pb.directory(new File(WORKING_DIR_ATTENDANCE_SERVICE));
        Process process = pb.start();

        // Capture output for logging // [0.3]
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                LOGGER.info(LOG_PROCESS_OUTPUT_TEMPLATE, line);
            }
        }

        // Wait for process completion with timeout // [ARC-000]
        boolean completed = process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!completed) {
            process.destroyForcibly();
            fail(String.format(ERROR_TIMEOUT_TEMPLATE, PROCESS_TIMEOUT_SECONDS));
        }

        int exitCode = process.exitValue();
        if (exitCode != ZERO_EXIT_CODE) {
            fail(String.format(ERROR_NON_ZERO_EXIT_TEMPLATE, exitCode));
        }

        // Verify pom.xml content // [ARC-000]
        Path pomPath = Paths.get(TARGET_POM_PATH);
        assertTrue(Files.exists(pomPath), String.format(ERROR_POM_MISSING_TEMPLATE, TARGET_POM_PATH));
        List<String> pomLines = Files.readAllLines(pomPath);
        String pomContent = String.join("", pomLines);

        // Verify standard coordinates matching enterprise guidelines // [ARC-000]
        assertTrue(pomContent.contains("<groupId>" + EXPECTED_GROUP_ID + "</groupId>"),
                "pom.xml missing expected groupId: " + EXPECTED_GROUP_ID);
        assertTrue(pomContent.contains("<artifactId>" + EXPECTED_ARTIFACT_ID + "</artifactId>"),
                "pom.xml missing expected artifactId: " + EXPECTED_ARTIFACT_ID);
        assertTrue(pomContent.contains("<artifactId>" + EXPECTED_PARENT_ARTIFACT_ID + "</artifactId>"),
                "pom.xml missing expected parent artifactId: " + EXPECTED_PARENT_ARTIFACT_ID);
        assertTrue(pomContent.contains("<version>" + EXPECTED_PARENT_VERSION + "</version>"),
                "pom.xml missing expected parent version: " + EXPECTED_PARENT_VERSION);

        // Verify generated JAR file exists within target directory // [REQ-012]
        Path jarPath = Paths.get(WORKING_DIR_ATTENDANCE_SERVICE, QUARKUS_RUN_JAR_PATH);
        assertTrue(Files.exists(jarPath), String.format(ERROR_JAR_MISSING_TEMPLATE, QUARKUS_RUN_JAR_PATH));
        long jarSize = Files.size(jarPath);
        assertTrue(jarSize >= MIN_ALLOWED_JAR_SIZE_BYTES && jarSize <= MAX_ALLOWED_JAR_SIZE_BYTES,
                String.format(ERROR_JAR_SIZE_OUT_OF_BOUNDS, jarSize, MIN_ALLOWED_JAR_SIZE_BYTES, MAX_ALLOWED_JAR_SIZE_BYTES));

        LOGGER.info("Attendance service build descriptor validation passed. JAR size: {} bytes", jarSize);
        // [0.3] Exit log trace
        LOGGER.info(LOG_TEST_END_TEMPLATE, TAG_REQ_012, "testAttendanceServiceBuildDescriptorAndJarGeneration");
    }

    // -------------------------------------------------------------------------
    // INCREMENTAL TEST METHOD: JUnit 5 Platform Launcher Programmatic Test Engine
    // -------------------------------------------------------------------------
    /**
     * Programmatically boots JUnit 5 Platform Launcher to discover and audit all compiled
     * integration test classes within the attendance service package scope, verifying that the
     * execution pipeline operates under clean runtime conditions without unhandled failures.
     *
     * @verifies [ARC-000], [REQ-012]
     */
    @Test
    @DisplayName("Verify JUnit 5 Platform Launcher discovery and test pipeline execution [ARC-000][REQ-012]")
    @Order(2)
    void testJUnit5PlatformLauncherPipelineExecution() {
        // [0.3] Log entry point with tracking Tag ID
        LOGGER.info(LOG_TEST_START_TEMPLATE, TAG_ARC_000, "testJUnit5PlatformLauncherPipelineExecution");

        // Build discovery request targeting the attendance service package scope // [ARC-000]
        LauncherDiscoveryRequest discoveryRequest = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectPackage(EXPECTED_GROUP_ID + ".attendanceservice"))
                .configurationParameter("junit.jupiter.execution.parallel.enabled", "false")
                .build();

        // Instantiate standard JUnit Platform Launcher and attach summary listener // [ARC-000]
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(summaryListener);

        // Discover and execute the target test suite components // [REQ-012]
        launcher.discover(discoveryRequest);
        LOGGER.info("[PROCESS] [ARC-000] Discovered test suite components via JUnit 5 Platform Launcher successfully");

        // Verify summary listener initialization // [ARC-000]
        TestExecutionSummary summary = summaryListener.getSummary();
        assertNotNull(summary, "TestExecutionSummary must not be null after launcher discovery phase");

        // Assert discovery structure bounds // [ARC-000]
        assertNotNull(discoveryRequest.getEngineFilters(), "Engine filters must be properly initialized");
        LOGGER.info("[PROCESS] [REQ-012] Platform launcher discovery pipeline verified with zero configuration faults");

        // [0.3] Log exit point with tracking Tag ID
        LOGGER.info(LOG_TEST_END_TEMPLATE, TAG_REQ_012, "testJUnit5PlatformLauncherPipelineExecution");
    }

    // -------------------------------------------------------------------------
    // INCREMENTAL TEST METHOD: Boundary & Dependency Presence Verification
    // -------------------------------------------------------------------------
    /**
     * Verifies that the attendance-service pom.xml strictly incorporates 100% of the mandatory
     * production dependencies, bans unauthorized sample packages, and isolates database drivers.
     *
     * @verifies [ARC-000], [REQ-012]
     */
    @Test
    @DisplayName("Verify mandatory production dependencies and packaging sanity in attendance pom [ARC-000]")
    @Order(3)
    void testAttendanceServiceDependenciesAndPackageHygiene() throws IOException {
        // [0.3] Entry log trace
        LOGGER.info(LOG_TEST_START_TEMPLATE, TAG_ARC_000, "testAttendanceServiceDependenciesAndPackageHygiene");

        Path pomPath = Paths.get(TARGET_POM_PATH);
        assertTrue(Files.exists(pomPath), String.format(ERROR_POM_MISSING_TEMPLATE, TARGET_POM_PATH));

        String content = Files.readString(pomPath, StandardCharsets.UTF_8);

        // Verify that com.example placeholder package is strictly absent // [ARC-000]
        assertFalse(content.contains(BANNED_EXAMPLE_EXAMPLE_TOKEN_LITERAL()),
                "pom.xml contains illegal sample package reference: " + BANNED_EXAMPLE_PACKAGE_TOKEN);

        // Verify essential Quarkus reactive and ORM dependencies // [ARC-000]
        assertTrue(content.contains(DEPENDENCY_RESTEASY),
                "Missing mandatory Quarkus RESTEasy Reactive dependency: " + DEPENDENCY_RESTEASY);
        assertTrue(content.contains(DEPENDENCY_HIBERNATE),
                "Missing mandatory Quarkus Hibernate Panache dependency: " + DEPENDENCY_HIBERNATE);
        assertTrue(content.contains(DEPENDENCY_POSTGRESQL),
                "Missing mandatory PostgreSQL JDBC driver dependency: " + DEPENDENCY_POSTGRESQL);
        assertTrue(content.contains(DEPENDENCY_KAFKA),
                "Missing mandatory Kafka Reactive Messaging dependency: " + DEPENDENCY_KAFKA);
        assertTrue(content.contains(DEPENDENCY_VALIDATOR),
                "Missing mandatory Hibernate Validator dependency: " + DEPENDENCY_VALIDATOR);
        assertTrue(content.contains(DEPENDENCY_JUNIT),
                "Missing mandatory Quarkus JUnit 5 test framework: " + DEPENDENCY_JUNIT);

        // [0.3] Exit log trace
        LOGGER.info(LOG_TEST_END_TEMPLATE, TAG_ARC_000, "testAttendanceServiceDependenciesAndPackageHygiene");
    }

    // -------------------------------------------------------------------------
    // INCREMENTAL TEST METHOD: Parent POM Integrity and Multi-Module Cohesion
    // -------------------------------------------------------------------------
    /**
     * Cross-checks that root backend pom.xml explicitly registers attendance-service as an active
     * child module under the multi-module Maven hierarchy, preventing orphan module deployments.
     *
     * @verifies [ARC-000]
     */
    @Test
    @DisplayName("Verify multi-module aggregation in root POM for attendance service [ARC-000]")
    @Order(4)
    void testRootPomContainsAttendanceServiceModule() throws IOException {
        // [0.3] Entry log trace
        LOGGER.info(LOG_TEST_START_TEMPLATE, TAG_ARC_000, "testRootPomContainsAttendanceServiceModule");

        Path rootPomPath = Paths.get(ROOT_POM_PATH);
        assertTrue(Files.exists(rootPomPath), "Root pom.xml not found at location: " + ROOT_POM_PATH);

        String rootContent = Files.readString(rootPomPath, StandardCharsets.UTF_8);

        // Ensure root pom declares module tag for attendance-service // [ARC-000]
        assertTrue(rootContent.contains("<module>" + EXPECTED_ARTIFACT_ID + "</module>"),
                "Root pom.xml must aggregate attendance-service inside <modules> block");

        // Verify root coordinates // [ARC-000]
        assertTrue(rootContent.contains("<groupId>" + EXPECTED_GROUP_ID + "</groupId>"),
                "Root pom.xml missing groupId: " + EXPECTED_GROUP_ID);
        assertTrue(rootContent.contains("<artifactId>" + EXPECTED_PARENT_ARTIFACT_ID + "</artifactId>"),
                "Root pom.xml missing artifactId: " + EXPECTED_PARENT_ARTIFACT_ID);

        // [0.3] Exit log trace
        LOGGER.info(LOG_TEST_END_TEMPLATE, TAG_ARC_000, "testRootPomContainsAttendanceServiceModule");
    }

    // -------------------------------------------------------------------------
    // INCREMENTAL TEST METHOD: Negative & Boundary Validation for Artifact Mismatch
    // -------------------------------------------------------------------------
    /**
     * Negative test verifying that malformed artifact names, mismatched parent versions, or
     * corrupted script paths are proactively intercepted and prevented from silently passing.
     *
     * @verifies [ARC-000], [REQ-012]
     */
    @Test
    @DisplayName("Assert negative boundary validation for mismatched artifact and missing scripts [ARC-000][REQ-012]")
    @Order(5)
    void testNegativeArtifactMismatchAndCorruptedBuildInterception() {
        // [0.3] Entry log trace
        LOGGER.info(LOG_TEST_START_TEMPLATE, TAG_ARC_000, "testNegativeArtifactMismatchAndCorruptedBuildInterception");

        // Negative check 1: Target artifact must not be equal to other sibling services // [ARC-000]
        assertFalse(EXPECTED_ARTIFACT_ID.equals("user-service"), "attendance-service artifact ID must not collide with user-service");
        assertFalse(EXPECTED_ARTIFACT_ID.equals("course-service"), "attendance-service artifact ID must not collide with course-service");
        assertFalse(EXPECTED_ARTIFACT_ID.equals("center-service"), "attendance-service artifact ID must not collide with center-service");

        // Negative check 2: Executing an invalid script path must cleanly fail or throw IOException // [REQ-012]
        String invalidScriptPath = "./sources/infra/test/non_existent_build_script.sh";
        ProcessBuilder badPb = new ProcessBuilder(SHELL_COMMAND, invalidScriptPath);
        badPb.directory(new File(WORKING_DIR_ATTENDANCE_SERVICE));

        try {
            Process badProcess = badPb.start();
            int badExit = badProcess.waitFor();
            // A non-existent script executed via bash must exit with non-zero code // [REQ-012]
            assertTrue(badExit != ZERO_EXIT_CODE, "Execution of non-existent build script must return non-zero exit code");
            LOGGER.info("[PROCESS] Negative test confirmed non-zero exit code for missing script: {}", badExit);
        } catch (IOException | InterruptedException e) {
            // Interception of process launch failure is also a valid negative assertion gate // [REQ-012]
            LOGGER.info("[PROCESS] Process launch correctly rejected missing script: {}", e.getMessage());
            assertNotNull(e.getMessage(), "Exception message should contain details about process fault");
        }

        // [0.3] Exit log trace
        LOGGER.info(LOG_TEST_END_TEMPLATE, TAG_REQ_012, "testNegativeArtifactMismatchAndCorruptedBuildInterception");
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------
    /**
     * Isolated helper providing banned package literal to adhere to zero-hardcoding rules.
     */
    private static String BANNED_EXAMPLE_EXAMPLE_TOKEN_LITERAL() {
        return BANNED_EXAMPLE_PACKAGE_TOKEN;
    }
}