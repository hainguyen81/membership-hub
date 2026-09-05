```java
package org.nlh4j.membershipub.userservice;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.UUID;

/**
 * Enterprise Integration Test Suite for verifying Flyway migrations (V1, V2, V3)
 * and ensuring database schema integrity, constraints, foreign keys, and composite uniqueness.
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
    @Order(1)
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
    @Order(2)
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
            connection.prepareStatement("INSERT INTO