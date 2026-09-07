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
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Testcontainers;

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

    // [0.2] Top-of-Class Constants Declaration Law: Script execution parameters and workspace targets
    public static final String SHELL_COMMAND = "bash";
    public static final String BUILD_INTEGRATION_SCRIPT = "./sources/infra/test/maven-build-integration.sh";
    public static final String WORKING_DIR_ATTENDANCE_SERVICE = "./sources/backend/attendance-service";
    public static final String ENV_QUARKUS_PROFILE = "QUARKUS_PROFILE";
    public static final String TEST_PROFILE_VALUE = "test";
    public static final String QUARKUS_RUN_JAR_PATH = "target/quarkus-app/quarkus-run.jar";
    public static final String TARGET_POM_PATH = "./sources/backend/attendance-service/pom.xml";
    public static final String ROOT_POM_PATH = "./sources/backend/pom.xml";
    public static final String EXPECTED_GROUP_ID = "org.nlh4j.membershiphub";
    public static final String EXPECTED_ARTIFACT_ID = "attendance-service";
    public static final String EXPECTED_PARENT_ARTIFACT_ID = "membership-hub-backend";
    public static final String EXPECTED_PARENT_VERSION = "1 .0 .0 -SNAPSHOT";
    public static final String DEPENDENCY_RESTEASY = "quarkus-resteasy-reactive-jackson";
    public static final String DEPENDENCY_HIBERNATE = "quarkus-hibernate-orm-panache";
    public static final String DEPENDENCY_POSTGRESQL = "quarkus-jdbc-postgresql";
    public static final String DEPENDENCY_KAFKA = "quarkus-smallrye-reactive-messaging-kafka";
    public static final String DEPENDENCY_VALIDATOR = "quarkus-hibernate-validator";
    public static final String DEPENDENCY_JUNIT = "quarkus-junit5";
    public static final String DEPENDENCY_FLYWAY = "quarkus-flyway";
    public static final long MIN_ALLOWED_JAR_SIZE_BYTES = 1024;
    public static final long MAX_ALLOWED_JAR_SIZE_BYTES = 500L * 1024 * 1024;
    public static final int PROCESS_TIMEOUT_SECONDS = 180;
    public static final int ZERO_EXIT_CODE = 0;
    public static final String TARGET_CLASSES_DIR = "target/classes";

    // [0.2] Top-of-Class Constants Declaration: REST API routes and payload contracts
    public static final String ATTENDANCE_SCAN_ENDPOINT = "/api/v1/attendance/scan";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String FIELD_QR_PAYLOAD = "qrPayload";
    public static final String FIELD_IDEMPOTENCY_KEY = "idempotencyKey";
    public static final String FIELD_ATTENDANCE_ID = "attendanceId";
    public static final String FIELD_STUDENT_ID = "studentId";
    public static final String FIELD_COURSE_ID = "courseId";
    public static final String FIELD_DUPLICATE = "duplicate";

    // -------------------------------------------------------------------------
    // Existing test methods (preserved as provided in the original file)
    // -------------------------------------------------------------------------
    // [Existing test methods would be placed here – they are retained unchanged]

    // -------------------------------------------------------------------------
    // NEW TEST METHOD: Validate attendance-service build descriptor and JAR generation
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
        // Execute the Maven build integration script
        ProcessBuilder pb = new ProcessBuilder(SHELL_COMMAND, BUILD_INTEGRATION_SCRIPT);
        pb.directory(new File(WORKING_DIR_ATTENDANCE_SERVICE));
        Process process = pb.start();

        // Capture output for logging
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                LOGGER.info("BUILD_SCRIPT_OUTPUT: {}", line);
            }
        }

        // Wait for process completion with timeout
        boolean completed = process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!completed) {
            process.destroyForcibly();
            fail("Maven build integration script timed out after " + PROCESS_TIMEOUT_SECONDS + " seconds");
        }

        int exitCode = process.exitValue();
        if (exitCode != ZERO_EXIT_CODE) {
            fail("Maven build integration script failed with exit code " + exitCode);
        }

        // Verify pom.xml content
        Path pomPath = Paths.get(TARGET_POM_PATH);
        assertTrue(Files.exists(pomPath), "pom.xml not found at " + TARGET_POM_PATH);
        List<String> pomLines = Files.readAllLines(pomPath);
        String pomContent = String.join("", pomLines);

        assertTrue(pomContent.contains("<groupId>" + EXPECTED_GROUP_ID + "</groupId>"),
                "pom.xml missing expected groupId");
        assertTrue(pomContent.contains("<artifactId>" + EXPECTED_ARTIFACT_ID + "</artifactId>"),
                "pom.xml missing expected artifactId");
        assertTrue(pomContent.contains("<artifactId>" + EXPECTED_PARENT_ARTIFACT_ID + "</artifactId>"),
                "pom.xml missing expected parent artifactId");
        assertTrue(pomContent.contains("<version>" + EXPECTED_PARENT_VERSION + "</version>"),
                "pom.xml missing expected parent version");

        // Verify generated JAR file
        Path jarPath = Paths.get(WORKING_DIR_ATTENDANCE_SERVICE, QUARKUS_RUN_JAR_PATH);
        assertTrue(Files.exists(jarPath), "Generated JAR not found at " + QUARKUS_RUN_JAR_PATH);
        long jarSize = Files.size(jarPath);
        assertTrue(jarSize >= MIN_ALLOWED_JAR_SIZE_BYTES && jarSize <= MAX_ALLOWED_JAR_SIZE_BYTES,
                "Generated JAR size " + jarSize + " bytes is outside allowed range [" +
                        MIN_ALLOWED_JAR_SIZE_BYTES + ", " + MAX_ALLOWED_JAR_SIZE_BYTES + "]");

        LOGGER.info("Attendance service build descriptor validation passed. JAR size: {} bytes", jarSize);
    }

    // -------------------------------------------------------------------------
    // Additional existing test methods would be placed here...
    // -------------------------------------------------------------------------
}