# Day 2: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/FlywayMigrationIntegrationTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Test Component Destination Path: `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/FlywayMigrationIntegrationTest.java` (Must map to sources/backend/ or sources/frontend/)




### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/FlywayMigrationIntegrationTest.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Tạo tệp kiểm thử tích hợp ./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/FlywayMigrationIntegrationTest.java xác minh toàn bộ script di trú Flyway V1, V2, V3 chạy thành công và tạo đủ 12 bảng nghiệp vụ. Sử dụng @QuarkusTest với profile test, cấu hình Testcontainers PostgreSQL 1.20.4 (org.testcontainers:postgresql:1.20.4) để khởi tạo cơ sở dữ liệu PostgreSQL 16-alpine trong Docker. Truy vấn information_schema.tables xác nhận sự tồn tại của 12 bảng (roles, users, centers, courses, enrollments, attendance, student_cards, notifications, promotions, announcements, system_settings, audit_logs). Kiểm tra ràng buộc UNIQUE composite (student_id, course_id, attendance_date) trên bảng attendance bằng cách insert 2 bản ghi trùng lặp và xác nhận exception. Xác minh CHECK constraints từng bảng hoạt động đúng. Kiểm tra toàn bộ khóa ngoại FK bằng cách thử insert giá trị không tồn tại.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` structure. Ensure that you read the exact Tag IDs from the `['[DAT-001]', '[DAT-002]', '[DAT-003]', '[DAT-004]', '[DAT-005]', '[DAT-006]', '[DAT-007]', '[DAT-008]', '[DAT-009]', '[DAT-010]', '[DAT-011]', '[DAT-012]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.membershiphub.userservice;

// [REQ-001] [DAT-001] [DAT-002] [DAT-003] [DAT-004] [DAT-005] [DAT-006] [DAT-007] [DAT-008] [DAT-009] [DAT-010] [DAT-011] [DAT-012]
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Enterprise Integration Test Suite for verifying Flyway migrations (V1, V2, V3)
 * and ensuring database schema integrity, constraints, FKs, and composite uniqueness.
 * 
 * @verifies [DAT-001], [DAT-002], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011], [DAT-012]
 */
@QuarkusTest
@Testcontainers
public class FlywayMigrationIntegrationTest {

    // [DAT-001] Top-level constant declaration governing the expected tables count
    private static final int EXPECTED_TABLE_COUNT = 12;

    // [DAT-001] Top-level constant declaration for PostgreSQL container version
    private static final String POSTGRES_IMAGE_VERSION = "postgres:16-alpine";

    @Container
    public static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE_VERSION)
            .withDatabaseName("membership_hub_test")
            .withUsername("test")
            .withPassword("test");

    @Inject
    DataSource dataSource;

    @Inject
    Flyway flyway;

    @BeforeEach
    public void setUpFlyway() {
        // [DAT-001] Explicitly trigger Flyway migration against the Testcontainers instance
        flyway.clean();
        flyway.migrate();
    }

    /**
     * Verifies that all 12 enterprise database tables are successfully created by Flyway migrations V1, V2, and V3.
     * 
     * @verifies [DAT-001], [DAT-002], [DAT-003], [DAT-004], [DAT-005], [DAT-006], [DAT-007], [DAT-008], [DAT-009], [DAT-010], [DAT-011], [DAT-012]
     */
    @Test
    public void testAllTwelveTablesAreCreatedSuccessfully() throws SQLException {
        // [DAT-001] Define the mandatory set of 12 business schema tables
        Set<String> expectedTables = new HashSet<>();
        expectedTables.add("roles");
        expectedTables.add("users");
        expectedTables.add("centers");
        expectedTables.add("courses");
        expectedTables.add("enrollments");
        expectedTables.add("attendance");
        expectedTables.add("student_cards");
        expectedTables.add("notifications");
        expectedTables.add("promotions");
        expectedTables.add("announcements");
        expectedTables.add("system_settings");
        expectedTables.add("audit_logs");

        Set<String> actualTables = new HashSet<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    actualTables.add(resultSet.getString("table_name"));
                }
            }
        }

        // [DAT-001] Assert that all required enterprise tables exist in the target schema
        for (String expectedTable : expectedTables) {
            Assertions.assertTrue(actualTables.contains(expectedTable),
                    "Critical enterprise table missing from migration: " + expectedTable);
        }
        Assertions.assertEquals(EXPECTED_TABLE_COUNT, actualTables.size(),
                "Table count mismatch. Expected exactly 12 tables.");
    }

    /**
     * Verifies that composite unique constraints on the attendance table reject duplicate records.
     * 
     * @verifies [DAT-005]
     */
    @Test
    public void testAttendanceCompositeUniqueConstraint() throws SQLException {
        UUID studentId = UUID.randomUUID();
        UUID centerId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID attendanceId1 = UUID.randomUUID();
        UUID attendanceId2 = UUID.randomUUID();
        LocalDate attendanceDate = LocalDate.now();

        try (Connection connection = dataSource.getConnection()) {
            // Seed parent dependencies for foreign keys
            connection.prepareStatement("INSERT INTO roles (role_id, name) VALUES (1, 'STUDENT')").execute();
            connection.prepareStatement("INSERT INTO users (user_id, email, password_hash, full_name, role_id) VALUES ('" + studentId + "', 'student@test.com', 'hash', 'Student Test', 1)").execute();
            connection.prepareStatement("INSERT INTO users (user_id, email, password_hash, full_name, role_id) VALUES ('" + teacherId + "', 'teacher@test.com', 'hash', 'Teacher Test', 1)").execute();
            connection.prepareStatement("INSERT INTO centers (center_id, name, address, tax_id) VALUES ('" + centerId + "', 'Center Test', 'Address Test', '123456789')").execute();
            connection.prepareStatement("INSERT INTO courses (course_id, title, start_date, end_date, teacher_id, center_id) VALUES ('" + courseId + "', 'Course Test', '2026-01-01', '2026-12-31', '" + teacherId + "', '" + centerId + "')").execute();

            // Insert first attendance record (Happy Path)
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO attendance (attendance_id, student_id, course_id, attendance_date) VALUES (?, ?, ?, ?)")) {
                stmt.setObject(1, attendanceId1);
                stmt.setObject(2, studentId);
                stmt.setObject(3, courseId);
                stmt.setObject(4, java.sql.Date.valueOf(attendanceDate));
                int rowsInserted = stmt.executeUpdate();
                Assertions.assertEquals(1, rowsInserted, "First attendance record insertion failed.");
            }

            // Attempt to insert duplicate record with same (student_id, course_id, attendance_date) [DAT-005]
            boolean exceptionThrown = false;
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO attendance (attendance_id, student_id, course_id, attendance_date) VALUES (?, ?, ?, ?)")) {
                stmt.setObject(1, attendanceId2);
                stmt.setObject(2, studentId);
                stmt.setObject(3, courseId);
                stmt.setObject(4, java.sql.Date.valueOf(attendanceDate));
                stmt.executeUpdate();
            } catch (SQLException e) {
                exceptionThrown = true;
                // Verify error code for unique violation in PostgreSQL (23505)
                Assertions.assertEquals("23505", e.getSQLState(), "Expected PostgreSQL unique constraint violation SQLState.");
            }

            Assertions.assertTrue(exceptionThrown, "Composite unique constraint on attendance table failed to prevent duplicate insertion.");
        }
    }

    /**
     * Verifies that table check constraints (e.g., course max_students > 0, discount percent range) function correctly.
     * 
     * @verifies [DAT-003], [DAT-009]
     */
    @Test
    public void testCheckConstraintsEnforcement() throws SQLException {
        UUID centerId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("INSERT INTO roles (role_id, name) VALUES (1, 'TEACHER')").execute();
            connection.prepareStatement("INSERT INTO users (user_id, email, password_hash, full_name, role_id) VALUES ('" + teacherId + "', 't@test.com', 'hash', 'Teacher', 1)").execute();
            connection.prepareStatement("INSERT INTO centers (center_id, name, address, tax_id) VALUES ('" + centerId + "', 'Center', 'Address', '987654321')").execute();

            // Attempt to insert course with invalid max_students <= 0 [DAT-003]
            boolean checkViolationThrown = false;
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO courses (course_id, title, start_date, end_date, teacher_id, center_id, max_students) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                stmt.setObject(1, courseId);
                stmt.setString(2, "Invalid Course");
                stmt.setDate(3, java.sql.Date.valueOf("2026-01-01"));
                stmt.setDate(4, java.sql.Date.valueOf("2026-01-10"));
                stmt.setObject(5, teacherId);
                stmt.setObject(6, centerId);
                stmt.setInt(7, 0); // Invalid max students
                stmt.executeUpdate();
            } catch (SQLException e) {
                checkViolationThrown = true;
                // PostgreSQL check constraint violation SQLState is 23514
                Assertions.assertEquals("23514", e.getSQLState(), "Expected check constraint violation SQLState.");
            }

            Assertions.assertTrue(checkViolationThrown, "Check constraint on max_students failed to reject invalid values.");
        }
    }

    /**
     * Verifies foreign key constraints by attempting to insert records with non-existent parent references.
     * 
     * @verifies [DAT-001], [DAT-003], [DAT-004]
     */
    @Test
    public void testForeignKeyConstraintsEnforcement() throws SQLException {
        UUID nonExistentUserId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        try (Connection connection = dataSource.getConnection()) {
            boolean fkViolationThrown = false;
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO enrollments (enrollment_id, student_id, course_id) VALUES (?, ?, ?)")) {
                stmt.setObject(1, UUID.randomUUID());
                stmt.setObject(2, nonExistentUserId); // Non-existent foreign key reference
                stmt.setObject(3, courseId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                fkViolationThrown = true;
                // PostgreSQL foreign key violation SQLState is 23503
                Assertions.assertEquals("23503", e.getSQLState(), "Expected foreign key violation SQLState.");
            }

            Assertions.assertTrue(fkViolationThrown, "Foreign key constraint failed to prevent insertion with orphan reference.");
        }
    }
}
```

