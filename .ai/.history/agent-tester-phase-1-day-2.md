# Day 2: model models/gemini-flash-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/UserSchemaMigrationIT.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: membership-hub
*   Enforced Java Package Prefix Base: org.nlh4j.membershiphub
*   Target Test Component Destination Path: `./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/UserSchemaMigrationIT.java` (Must map to sources/backend/ or sources/frontend/)




### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/UserSchemaMigrationIT.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Tạo lớp kiểm thử tích hợp Flyway ./sources/backend/user-service/src/test/java/org/nlh4j/membershiphub/userservice/UserSchemaMigrationIT.java sử dụng @QuarkusTest. Cấu hình Testcontainers PostgreSQL 16, thực thi @QuarkusTestResource để khởi tạo container. Inject Flyway bean, gọi flyway.migrate() và xác minh các bảng roles, users tồn tại thông qua truy vấn JDBC metadata. Bổ sung kiểm tra ràng buộc CHECK chk_users_provider bằng cách chèn giá trị không hợp lệ và kỳ vọng ngoại lệ SQLException.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.membershiphub` structure. Ensure that you read the exact Tag IDs from the `['[DAT-001]', '[DAT-002]', '[DAT-003]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.membershiphub.userservice;

// [DAT-001] [DAT-002] [DAT-003]
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Integration test suite validating the database schema migrations and constraints for Users and Roles.
 *
 * @author Enterprise Architecture Team
 * @verifies [DAT-001] Role table structure and default role seeding verification
 * @verifies [DAT-002] User table structure, indexes, and column integrity verification
 * @verifies [DAT-003] Database constraints, foreign keys, and CHECK constraint enforcement
 */
@QuarkusTest
@QuarkusTestResource(UserSchemaMigrationIT.PostgresTestResource.class)
public class UserSchemaMigrationIT {

    // [DAT-001] Logger instance initialization for audit tracking
    private static final Logger LOGGER = LoggerFactory.getLogger(UserSchemaMigrationIT.class);

    // [DAT-001] Top-of-class immutable constant configurations and SQL queries
    private static final String POSTGRES_DOCKER_IMAGE = "postgres:16-alpine";
    private static final String DB_NAME = "membership_hub_test";
    private static final String DB_USERNAME = "test_user";
    private static final String DB_PASSWORD = "test_password";

    private static final String TABLE_ROLES = "roles";
    private static final String TABLE_USERS = "users";
    private static final String SCHEMA_PUBLIC = "public";
    private static final String TABLE_TYPE_TABLE = "TABLE";

    private static final String COLUMN_ROLE_ID = "role_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_PROVIDER = "provider";

    private static final String SQL_SELECT_TABLES_METADATA = "SELECT table_name FROM information_schema.tables WHERE table_schema = ? AND table_name IN (?, ?)";
    private static final String SQL_SELECT_ROLE_COUNT = "SELECT COUNT(*) FROM roles";
    private static final String SQL_SELECT_SPECIFIC_ROLE = "SELECT role_id, name FROM roles WHERE role_id = ?";
    private static final String SQL_INSERT_USER_TEMPLATE = "INSERT INTO users (user_id, email, password_hash, full_name, role_id, provider) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_USER_BY_ID = "SELECT user_id, email, provider FROM users WHERE user_id = ?";

    private static final String SAMPLE_VALID_EMAIL = "developer.test@membershiphub.org";
    private static final String SAMPLE_DUPLICATE_EMAIL = "developer.test@membershiphub.org";
    private static final String SAMPLE_PASSWORD_HASH = "$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF01234567890123456789";
    private static final String SAMPLE_FULL_NAME = "Integration Test Engineer";
    private static final String SAMPLE_INVALID_PROVIDER = "unsupported_oauth_vendor";
    private static final String SAMPLE_VALID_LOCAL_PROVIDER = "local";
    private static final String SAMPLE_VALID_GOOGLE_PROVIDER = "google";
    private static final String SAMPLE_VALID_FIREBASE_PROVIDER = "firebase";
    private static final String SAMPLE_VALID_FACEBOOK_PROVIDER = "facebook";

    private static final int EXPECTED_TOTAL_DEFAULT_ROLES = 5;
    private static final int EXPECTED_TABLE_COUNT = 2;
    private static final short DEFAULT_STUDENT_ROLE_ID = 5;
    private static final String DEFAULT_STUDENT_ROLE_NAME = "Student";
    private static final short INVALID_FOREIGN_KEY_ROLE_ID = 999;
    private static final String PG_CHECK_VIOLATION_SQL_STATE = "23514";
    private static final String PG_FOREIGN_KEY_VIOLATION_SQL_STATE = "23503";
    private static final String PG_UNIQUE_VIOLATION_SQL_STATE = "23505";

    private static final String LOG_START_TEST_SCHEMA_TABLES = "[TEST_START] [DAT-001] [DAT-002] Initiating schema table existence verification...";
    private static final String LOG_END_TEST_SCHEMA_TABLES = "[TEST_COMPLETE] [DAT-001] [DAT-002] Schema tables successfully verified.";
    private static final String LOG_START_TEST_DEFAULT_ROLES = "[TEST_START] [DAT-001] Initiating seed roles verification...";
    private static final String LOG_END_TEST_DEFAULT_ROLES = "[TEST_COMPLETE] [DAT-001] Default seed roles successfully verified.";
    private static final String LOG_START_TEST_VALID_USER = "[TEST_START] [DAT-002] Initiating valid user insertion test across distinct providers...";
    private static final String LOG_END_TEST_VALID_USER = "[TEST_COMPLETE] [DAT-002] Valid user insertion test completed successfully.";
    private static final String LOG_START_TEST_INVALID_PROVIDER = "[TEST_START] [DAT-002] [DAT-003] Initiating invalid provider CHECK constraint rejection test...";
    private static final String LOG_END_TEST_INVALID_PROVIDER = "[TEST_COMPLETE] [DAT-002] [DAT-003] CHECK constraint rejection test successfully passed.";
    private static final String LOG_START_TEST_FOREIGN_KEY = "[TEST_START] [DAT-003] Initiating foreign key integrity constraint test for role_id...";
    private static final String LOG_END_TEST_FOREIGN_KEY = "[TEST_COMPLETE] [DAT-003] Foreign key constraint test successfully passed.";
    private static final String LOG_START_TEST_UNIQUE_EMAIL = "[TEST_START] [DAT-002] [DAT-003] Initiating unique email constraint test...";
    private static final String LOG_END_TEST_UNIQUE_EMAIL = "[TEST_COMPLETE] [DAT-002] [DAT-003] Unique email constraint test successfully passed.";

    // [DAT-001] Injected Database Migration Manager
    @Inject
    Flyway flyway;

    // [DAT-001] Injected Enterprise Database Connection Pool
    @Inject
    DataSource dataSource;

    /**
     * Dedicated PostgreSQL test container resource manager for live integration testing.
     */
    public static class PostgresTestResource implements QuarkusTestResourceLifecycleManager {

        // [DAT-001] Singleton container instance for lifecycle scope
        private static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
                new PostgreSQLContainer<>(POSTGRES_DOCKER_IMAGE)
                        .withDatabaseName(DB_NAME)
                        .withUsername(DB_USERNAME)
                        .withPassword(DB_PASSWORD);

        @Override
        public Map<String, String> start() {
            // [DAT-001] Start physical PostgreSQL testcontainer
            POSTGRES_CONTAINER.start();
            // [DAT-001] Map dynamic port and connection credentials back to Quarkus datasource configuration
            return Map.of(
                    "quarkus.datasource.jdbc.url", POSTGRES_CONTAINER.getJdbcUrl(),
                    "quarkus.datasource.username", POSTGRES_CONTAINER.getUsername(),
                    "quarkus.datasource.password", POSTGRES_CONTAINER.getPassword(),
                    "quarkus.datasource.db-kind", "postgresql"
            );
        }

        @Override
        public void stop() {
            // [DAT-001] Gracefully stop container upon test suite completion
            if (POSTGRES_CONTAINER.isRunning()) {
                POSTGRES_CONTAINER.stop();
            }
        }
    }

    /**
     * Verifies that Flyway applies migrations correctly and creates the core 'roles' and 'users' tables.
     *
     * @verifies [DAT-001] Roles table creation
     * @verifies [DAT-002] Users table creation
     */
    @Test
    @DisplayName("Verify that roles and users tables exist in database metadata after migration")
    void testRolesAndUsersTablesExist() throws SQLException {
        // [DAT-001] Log test execution start point
        LOGGER.info(LOG_START_TEST_SCHEMA_TABLES);

        // [DAT-001] Ensure Flyway instance is available and execute database migration
        Assertions.assertNotNull(flyway, "Flyway migration manager bean must be injected and non-null");
        flyway.migrate();

        // [DAT-001] Establish connection and check information_schema
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_TABLES_METADATA)) {

            // [DAT-001] Parameterize query parameters
            statement.setString(1, SCHEMA_PUBLIC);
            statement.setString(2, TABLE_ROLES);
            statement.setString(3, TABLE_USERS);

            // [DAT-001] Execute metadata search query
            try (ResultSet resultSet = statement.executeQuery()) {
                Set<String> discoveredTables = new HashSet<>();
                while (resultSet.next()) {
                    discoveredTables.add(resultSet.getString(1).toLowerCase());
                }

                // [DAT-001] [DAT-002] Assert both required tables are present
                Assertions.assertEquals(EXPECTED_TABLE_COUNT, discoveredTables.size(), "Database must contain exactly 2 core tables for users and roles");
                Assertions.assertTrue(discoveredTables.contains(TABLE_ROLES), "Roles table must be present in the public schema");
                Assertions.assertTrue(discoveredTables.contains(TABLE_USERS), "Users table must be present in the public schema");
            }
        }

        // [DAT-001] Log test execution completion point
        LOGGER.info(LOG_END_TEST_SCHEMA_TABLES);
    }

    /**
     * Verifies that default seed data for all 5 enterprise RBAC roles are inserted by V1 migration.
     *
     * @verifies [DAT-001] Initial roles seeding verification
     */
    @Test
    @DisplayName("Verify that default 5 RBAC roles are properly seeded during migration")
    void testDefaultRolesSeeded() throws SQLException {
        // [DAT-001] Log test execution start point
        LOGGER.info(LOG_START_TEST_DEFAULT_ROLES);

        // [DAT-001] Query the seeded roles count and specific roles
        try (Connection connection = dataSource.getConnection();
             Statement countStatement = connection.createStatement();
             ResultSet countResultSet = countStatement.executeQuery(SQL_SELECT_ROLE_COUNT)) {

            // [DAT-001] Validate row count
            Assertions.assertTrue(countResultSet.next(), "Count query must return at least one row");
            int totalRoles = countResultSet.getInt(1);
            Assertions.assertTrue(totalRoles >= EXPECTED_TOTAL_DEFAULT_ROLES, "Roles table must contain at least 5 default RBAC roles");
        }

        // [DAT-001] Query specific role to confirm integrity
        try (Connection connection = dataSource.getConnection();
             PreparedStatement roleStatement = connection.prepareStatement(SQL_SELECT_SPECIFIC_ROLE)) {

            roleStatement.setShort(1, DEFAULT_STUDENT_ROLE_ID);
            try (ResultSet roleResultSet = roleStatement.executeQuery()) {
                Assertions.assertTrue(roleResultSet.next(), "Student role record must be present in roles table");
                Assertions.assertEquals(DEFAULT_STUDENT_ROLE_NAME, roleResultSet.getString(COLUMN_NAME), "Role ID 5 must correspond to Student role");
            }
        }

        // [DAT-001] Log test execution completion point
        LOGGER.info(LOG_END_TEST_DEFAULT_ROLES);
    }

    /**
     * Verifies that user records with valid authentication providers can be persisted successfully.
     *
     * @verifies [DAT-002] User creation with standard supported providers (local, google, firebase, facebook)
     */
    @Test
    @DisplayName("Verify happy path user insertion with distinct supported authentication providers")
    void testInsertUserWithValidProviders() throws SQLException {
        // [DAT-002] Log test execution start point
        LOGGER.info(LOG_START_TEST_VALID_USER);

        String[] supportedProviders = new String[]{
                SAMPLE_VALID_LOCAL_PROVIDER,
                SAMPLE_VALID_GOOGLE_PROVIDER,
                SAMPLE_VALID_FIREBASE_PROVIDER,
                SAMPLE_VALID_FACEBOOK_PROVIDER
        };

        try (Connection connection = dataSource.getConnection()) {
            for (String provider : supportedProviders) {
                UUID userId = UUID.randomUUID();
                String userEmail = provider + "_" + SAMPLE_VALID_EMAIL;

                try (PreparedStatement insertStatement = connection.prepareStatement(SQL_INSERT_USER_TEMPLATE)) {
                    // [DAT-002] Set parameterized query fields
                    insertStatement.setObject(1, userId);
                    insertStatement.setString(2, userEmail);
                    insertStatement.setString(3, SAMPLE_PASSWORD_HASH);
                    insertStatement.setString(4, SAMPLE_FULL_NAME);
                    insertStatement.setShort(5, DEFAULT_STUDENT_ROLE_ID);
                    insertStatement.setString(6, provider);

                    // [DAT-002] Execute insert operation
                    int rowsInserted = insertStatement.executeUpdate();
                    Assertions.assertEquals(1, rowsInserted, "User record must be successfully inserted for provider: " + provider);
                }

                // [DAT-002] Verify record existence via SELECT
                try (PreparedStatement queryStatement = connection.prepareStatement(SQL_SELECT_USER_BY_ID)) {
                    queryStatement.setObject(1, userId);
                    try (ResultSet resultSet = queryStatement.executeQuery()) {
                        Assertions.assertTrue(resultSet.next(), "Newly created user record must be retrievable from database");
                        Assertions.assertEquals(userEmail, resultSet.getString(COLUMN_EMAIL), "Retrieved email must match the inserted email");
                        Assertions.assertEquals(provider, resultSet.getString(COLUMN_PROVIDER), "Retrieved provider must match the inserted provider");
                    }
                }
            }
        }

        // [DAT-002] Log test execution completion point
        LOGGER.info(LOG_END_TEST_VALID_USER);
    }

    /**
     * Verifies that the database CHECK constraint 'chk_users_provider' actively rejects unsupported OAuth providers.
     *
     * @verifies [DAT-002] chk_users_provider constraint validation
     * @verifies [DAT-003] SQL Exception propagation upon constraint violation
     */
    @Test
    @DisplayName("Verify that invalid provider triggers SQL Exception due to CHECK constraint chk_users_provider")
    void testInsertUserWithInvalidProviderThrowsSQLException() throws SQLException {
        // [DAT-002] [DAT-003] Log test execution start point
        LOGGER.info(LOG_START_TEST_INVALID_PROVIDER);

        UUID testUserId = UUID.randomUUID();
        String invalidUserEmail = "invalid.provider." + testUserId + "@membershiphub.org";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement insertStatement = connection.prepareStatement(SQL_INSERT_USER_TEMPLATE)) {

            // [DAT-002] Populate statement with an illegal provider string
            insertStatement.setObject(1, testUserId);
            insertStatement.setString(2, invalidUserEmail);
            insertStatement.setString(3, SAMPLE_PASSWORD_HASH);
            insertStatement.setString(4, SAMPLE_FULL_NAME);
            insertStatement.setShort(5, DEFAULT_STUDENT_ROLE_ID);
            insertStatement.setString(6, SAMPLE_INVALID_PROVIDER);

            // [DAT-003] Assert that executing the statement raises SQLException due to constraint failure
            SQLException thrownException = Assertions.assertThrows(
                    SQLException.class,
                    insertStatement::executeUpdate,
                    "Inserting an unsupported provider string must trigger a CHECK constraint SQLException"
            );

            // [DAT-003] Validate SQLState to confirm it is a CHECK constraint violation
            String sqlState = thrownException.getSQLState();
            Assertions.assertNotNull(sqlState, "SQLState code must not be null");
            Assertions.assertTrue(
                    sqlState.startsWith(PG_CHECK_VIOLATION_SQL_STATE) || thrownException.getMessage().toLowerCase().contains("chk_users_provider"),
                    "Exception must be caused by chk_users_provider CHECK constraint violation"
            );
        }

        // [DAT-002] [DAT-003] Log test execution completion point
        LOGGER.info(LOG_END_TEST_INVALID_PROVIDER);
    }

    /**
     * Verifies that assigning a non-existent role_id to a user triggers a Foreign Key constraint violation.
     *
     * @verifies [DAT-003] Foreign key constraint fk_users_role integrity enforcement
     */
    @Test
    @DisplayName("Verify that non-existent role_id triggers Foreign Key SQLException")
    void testInsertUserWithInvalidRoleIdThrowsForeignKeyException() throws SQLException {
        // [DAT-003] Log test execution start point
        LOGGER.info(LOG_START_TEST_FOREIGN_KEY);

        UUID testUserId = UUID.randomUUID();
        String testUserEmail = "fk.violation." + testUserId + "@membershiphub.org";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement insertStatement = connection.prepareStatement(SQL_INSERT_USER_TEMPLATE)) {

            // [DAT-003] Populate statement with non-existent foreign key
            insertStatement.setObject(1, testUserId);
            insertStatement.setString(2, testUserEmail);
            insertStatement.setString(3, SAMPLE_PASSWORD_HASH);
            insertStatement.setString(4, SAMPLE_FULL_NAME);
            insertStatement.setShort(5, INVALID_FOREIGN_KEY_ROLE_ID);
            insertStatement.setString(6, SAMPLE_VALID_LOCAL_PROVIDER);

            // [DAT-003] Assert foreign key rejection
            SQLException thrownException = Assertions.assertThrows(
                    SQLException.class,
                    insertStatement::executeUpdate,
                    "Inserting an invalid role_id must trigger a foreign key constraint violation"
            );

            // [DAT-003] Validate SQLState code
            String sqlState = thrownException.getSQLState();
            Assertions.assertNotNull(sqlState, "SQLState code must not be null");
            Assertions.assertTrue(
                    sqlState.startsWith(PG_FOREIGN_KEY_VIOLATION_SQL_STATE) || thrownException.getMessage().toLowerCase().contains("fk_users_role"),
                    "Exception must indicate a foreign key constraint violation"
            );
        }

        // [DAT-003] Log test execution completion point
        LOGGER.info(LOG_END_TEST_FOREIGN_KEY);
    }

    /**
     * Verifies that duplicate email address registration is actively blocked by the unique constraint.
     *
     * @verifies [DAT-002] Unique constraint on email column
     * @verifies [DAT-003] Database constraint violation detection
     */
    @Test
    @DisplayName("Verify that inserting duplicate email address throws Unique Constraint SQLException")
    void testInsertDuplicateEmailThrowsSQLException() throws SQLException {
        // [DAT-002] [DAT-003] Log test execution start point
        LOGGER.info(LOG_START_TEST_UNIQUE_EMAIL);

        String uniqueEmail = "unique.test." + UUID.randomUUID() + "@membershiphub.org";
        UUID firstUserId = UUID.randomUUID();
        UUID secondUserId = UUID.randomUUID();

        try (Connection connection = dataSource.getConnection()) {
            // [DAT-002] First insert must succeed
            try (PreparedStatement firstStatement = connection.prepareStatement(SQL_INSERT_USER_TEMPLATE)) {
                firstStatement.setObject(1, firstUserId);
                firstStatement.setString(2, uniqueEmail);
                firstStatement.setString(3, SAMPLE_PASSWORD_HASH);
                firstStatement.setString(4, SAMPLE_FULL_NAME);
                firstStatement.setShort(5, DEFAULT_STUDENT_ROLE_ID);
                firstStatement.setString(6, SAMPLE_VALID_LOCAL_PROVIDER);

                int rowsInserted = firstStatement.executeUpdate();
                Assertions.assertEquals(1, rowsInserted, "First user insertion must succeed");
            }

            // [DAT-003] Second insert with the identical email address must fail
            try (PreparedStatement secondStatement = connection.prepareStatement(SQL_INSERT_USER_TEMPLATE)) {
                secondStatement.setObject(1, secondUserId);
                secondStatement.setString(2, uniqueEmail);
                secondStatement.setString(3, SAMPLE_PASSWORD_HASH);
                secondStatement.setString(4, SAMPLE_FULL_NAME);
                secondStatement.setShort(5, DEFAULT_STUDENT_ROLE_ID);
                secondStatement.setString(6, SAMPLE_VALID_LOCAL_PROVIDER);

                SQLException thrownException = Assertions.assertThrows(
                        SQLException.class,
                        secondStatement::executeUpdate,
                        "Inserting duplicate email address must trigger a unique constraint violation"
                );

                String sqlState = thrownException.getSQLState();
                Assertions.assertNotNull(sqlState, "SQLState code must not be null");
                Assertions.assertTrue(
                        sqlState.startsWith(PG_UNIQUE_VIOLATION_SQL_STATE) || thrownException.getMessage().toLowerCase().contains("unique") || thrownException.getMessage().toLowerCase().contains("duplicate"),
                        "Exception must indicate a unique constraint violation"
                );
            }
        }

        // [DAT-002] [DAT-003] Log test execution completion point
        LOGGER.info(LOG_END_TEST_UNIQUE_EMAIL);
    }
}
```

