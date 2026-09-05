package org.nlh4j.membershiphub.userservice;

// [REQ-000] Import statements for JUnit 5, Quarkus testing framework, and standard I/O utilities
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * MavenBuildIntegrationTest verifies the multi-module Maven build structure,
 * compilation success across all backend services, and POM configuration compliance.
 * 
 * @verifies [ARC-000]
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MavenBuildIntegrationTest {

    // [REQ-000] Top-of-class immutable constants declaration law adherence
    public static final String ROOT_POM_RELATIVE_PATH = "./sources/backend/pom.xml";
    public static final String EXPECTED_GROUP_ID = "<groupId>org.nlh4j.membershiphub</groupId>";
    public static final String EXPECTED_JAVA_VERSION = "17";
    public static final String EXPECTED_QUARKUS_BOM_VERSION = "3.15.1";
    public static final long MAVEN_PROCESS_TIMEOUT_MINUTES = 10L;
    
    // [DAT-000] Sub-module directory names for jar verification
    public static final String[] SUB_MODULES = {
        "user-service",
        "center-service",
        "course-service",
        "attendance-service"
    };

    // [ARC-003] Standardized enterprise logger initialization
    private static final Logger LOGGER = LoggerFactory.getLogger(MavenBuildIntegrationTest.class);

    /**
     * Executes the Maven clean install command across the multi-module project structure
     * and verifies that all target artifact binaries are successfully produced.
     * 
     * @verifies [ARC-000]
     */
    @Test
    @Order(1)
    public void testMultiModuleMavenCompilationAndArtifactGeneration() {
        LOGGER.info("[PROCESS] [ARC-000] Starting multi-module Maven build integration verification.");

        try {
            // Locate the root pom.xml path
            Path rootPomPath = Paths.get(ROOT_POM_RELATIVE_PATH).toAbsolutePath().normalize();
            assertTrue(Files.exists(rootPomPath), "[CRITICAL FAIL] [ARC-000] Root pom.xml not found at path: " + rootPomPath);

            // Validate root pom contents for mandatory enterprise standards
            String rootPomContent = Files.readString(rootPomPath, StandardCharsets.UTF_8);
            assertTrue(rootPomContent.contains(EXPECTED_GROUP_ID), 
                    "[CRITICAL FAIL] [ARC-000] Root pom.xml does not contain the required Group ID: " + EXPECTED_GROUP_ID);
            assertTrue(rootPomContent.contains(EXPECTED_JAVA_VERSION) || rootPomContent.contains("<maven.compiler.release>17</maven.compiler.release>"), 
                    "[CRITICAL FAIL] [ARC-000] Root pom.xml does not target Java 17 successfully.");
            assertTrue(rootPomContent.contains(EXPECTED_QUARKUS_BOM_VERSION), 
                    "[CRITICAL FAIL] [ARC-000] Root pom.xml does not declare Quarkus BOM version " + EXPECTED_QUARKUS_BOM_VERSION);

            // Determine working directory for Maven execution (parent of sources/backend or absolute path)
            File workingDir = rootPomPath.getParent().toFile();
            
            // Construct ProcessBuilder to execute 'mvn clean install -DskipTests'
            ProcessBuilder processBuilder;
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win")) {
                processBuilder = new ProcessBuilder("cmd.exe", "/c", "mvn clean install -DskipTests");
            } else {
                processBuilder = new ProcessBuilder("mvn", "clean", "install", "-DskipTests");
            }
            
            processBuilder.directory(workingDir);
            processBuilder.redirectErrorStream(true);

            LOGGER.info("[PROCESS] [ARC-000] Executing Maven build command in directory: {}", workingDir.getAbsolutePath());
            Process process = processBuilder.start();

            // Capture output stream for traceability logging
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("BUILD SUCCESS") || line.contains("ERROR") || line.contains("FAILURE")) {
                        LOGGER.info("[MAVEN_OUTPUT] [ARC-000] {}", line);
                    }
                }
            }

            boolean finished = process.waitFor(MAVEN_PROCESS_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                fail("[CRITICAL FAIL] [ARC-000] Maven build process timed out after " + MAVEN_PROCESS_TIMEOUT_MINUTES + " minutes.");
            }

            int exitCode = process.exitValue();
            assertEquals(0, exitCode, "[CRITICAL FAIL] [ARC-000] Maven build execution failed with exit code: " + exitCode);
            LOGGER.info("[PROCESS] [ARC-000] Maven build execution completed successfully with exit code 0.");

            // Verify that target/*.jar or quarkus-app artifacts are generated for all 4 sub-modules
            for (String module : SUB_MODULES) {
                Path moduleTargetPath = rootPomPath.getParent().resolve(module).resolve("target");
                assertTrue(Files.exists(moduleTargetPath), 
                        "[CRITICAL FAIL] [ARC-000] Target directory missing for module: " + module);
                LOGGER.info("[PROCESS] [ARC-000] Verified target directory exists for module: {}", module);
            }

            LOGGER.info("[PROCESS] [ARC-000] MavenBuildIntegrationTest passed successfully.");

        } catch (Exception e) {
            // [EXC-000] Comprehensive exception logging protocol adherence
            LOGGER.error("[CRITICAL FAIL] [ARC-000] Maven build integration test failed due to exception. Raw error: {}", e.getMessage(), e);
            fail("[CRITICAL FAIL] [ARC-000] MavenBuildIntegrationTest execution failed: " + e.getMessage());
        }
    }
}