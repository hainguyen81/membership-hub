/**
 * Test suite for the authentication service covering registration, login, and user retrieval.
 *
 * @traceability [REQ-001], [REQ-002], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005],
 *               [DAT-001], [DAT-002], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005],
 *               [NFR-006], [NFR-007], [NFR-008], [NFR-009]
 */
package org.nlh4j.saas.membership-hub.auth;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import org.nlh4j.saas.membership-hub.auth.repository.UserRepository;
import org.nlh4j.saas.membership-hub.auth.repository.RoleRepository;
import org.nlh4j.saas.membership-hub.auth.model.User;
import org.nlh4j.saas.membership-hub.auth.model.Role;

/**
 * Integration tests for AuthService endpoints.
 *
 * @traceability [REQ-001], [REQ-002], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005],
 *               [DAT-001], [DAT-002], [NFR-001], [NFR-002], [NFR-003], [NFR-004], [NFR-005],
 *               [NFR-006], [NFR-007], [NFR-008], [NFR-009]
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@Rollback
public class AuthServiceTest {

    /* --------------------------------------------------------------------- */
    /*  Constants – configuration and test data                               */
    /* --------------------------------------------------------------------- */

    /** Base URL for authentication endpoints. */
    private static final String AUTH_BASE_URL = "/api/auth";

    /** Endpoint for user registration. */
    private static final String REGISTER_URL = AUTH_BASE_URL + "/register";

    /** Endpoint for user login. */
    private static final String LOGIN_URL = AUTH_BASE_URL + "/login";

    /** Endpoint for retrieving current user details. */
    private static final String ME_URL = AUTH_BASE_URL + "/me";

    /** Test email address. */
    private static final String TEST_EMAIL = "test.user@example.com";

    /** Test password (plain text). */
    private static final String TEST_PASSWORD = "SecureP@ssw0rd!";

    /** Default authentication provider for local accounts. */
    private static final String TEST_PROVIDER = "local";

    /** Default role name assigned to new users. */
    private static final String DEFAULT_ROLE_NAME = "USER";

    /** HTTP header name for bearer token. */
    private static final String AUTH_HEADER = "Authorization";

    /** Bearer token prefix. */
    private static final String BEARER_PREFIX = "Bearer ";

    /* --------------------------------------------------------------------- */
    /*  Autowired components – Spring context injection                       */
    /* --------------------------------------------------------------------- */

    @Autowired
    private MockMvc mockMvc; // MockMvc for performing HTTP requests

    @Autowired
    private ObjectMapper objectMapper; // Jackson ObjectMapper for JSON serialization

    @Autowired
    private UserRepository userRepository; // JPA repository for User entity

    @Autowired
    private RoleRepository roleRepository; // JPA repository for Role entity

    /* --------------------------------------------------------------------- */
    /*  Test lifecycle hooks – setup and teardown                            */
    /* --------------------------------------------------------------------- */

    /**
     * Create the default role before any tests run.
     *
     * @traceability [ARC-001], [ARC-002]
     */
    @BeforeAll
    void setUpDefaultRole() {
        // Ensure the default role exists; create if absent
        roleRepository.findByName(DEFAULT_ROLE_NAME)
                .orElseGet(() -> roleRepository.save(new Role(DEFAULT_ROLE_NAME, "Default user role")));
    }

    /**
     * Clean up all users after all tests have completed.
     *
     * @traceability [ARC-003], [ARC-004]
     */
    @AfterAll
    void tearDown() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    /**
     * Ensure a clean state before each test by deleting all users.
     *
     * @traceability [ARC-005]
     */
    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    /* --------------------------------------------------------------------- */
    /*  Test cases – happy path and negative scenarios                       */
    /* --------------------------------------------------------------------- */

    /**
     * Test successful user registration.
     *
     * @traceability [DAT-001], [DAT-002]
     */
    @Test
    @Order(1)
    @DisplayName("Register a new user successfully")
    void testRegisterSuccess() throws Exception {
        // Prepare registration payload
        var payload = new RegistrationRequest(TEST_EMAIL, TEST_PASSWORD, TEST_PROVIDER);

        // Perform POST /register
        var result = mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andExpect(jsonPath("$.refreshToken", not(emptyString())))
                .andReturn();

        // Extract token from response for further validation
        var responseBody = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = responseBody.get("token").asText();

        // Verify that the user is persisted with hashed password
        var userOpt = userRepository.findByEmail(TEST_EMAIL);
        Assertions.assertTrue(userOpt.isPresent(), "User should exist in repository");
        User user = userOpt.get();
        Assertions.assertNotEquals(TEST_PASSWORD, user.getPasswordHash(),
                "Stored password should be hashed, not plain text");

        // Verify that the default role is assigned
        Assertions.assertEquals(DEFAULT_ROLE_NAME, user.getRole().getName(),
                "User should have default role assigned");
    }

    /**
     * Test registration fails when email is already taken.
     *
     * @traceability [EXC-001]
     */
    @Test
    @Order(2)
    @DisplayName("Register with duplicate email should fail")
    void testRegisterDuplicateEmail() throws Exception {
        // First registration
        var payload = new RegistrationRequest(TEST_EMAIL, TEST_PASSWORD, TEST_PROVIDER);
        mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        // Second registration with same email
        mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("email already exists")));
    }

    /**
     * Test registration fails with invalid email format.
     *
     * @traceability [EXC-002]
     */
    @Test
    @Order(3)
    @DisplayName("Register with invalid email should fail")
    void testRegisterInvalidEmail() throws Exception {
        var payload = new RegistrationRequest("invalid-email", TEST_PASSWORD, TEST_PROVIDER);
        mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Invalid email format")));
    }

    /**
     * Test login succeeds with correct credentials.
     *
     * @traceability [DAT-001], [DAT-002]
     */
    @Test
    @Order(4)
    @DisplayName("Login with correct credentials succeeds")
    void testLoginSuccess() throws Exception {
        // Register user first
        var regPayload = new RegistrationRequest(TEST_EMAIL, TEST_PASSWORD, TEST_PROVIDER);
        mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regPayload)))
                .andExpect(status().isOk());

        // Prepare login payload
        var loginPayload = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

        // Perform POST /login
        var result = mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andExpect(jsonPath("$.refreshToken", not(emptyString())))
                .andReturn();

        // Extract token for subsequent authenticated request
        var responseBody = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = responseBody.get("token").asText();

        // Use token to access protected endpoint /me
        mockMvc.perform(get(ME_URL)
                .header(AUTH_HEADER, BEARER_PREFIX + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(TEST_EMAIL)))
                .andExpect(jsonPath("$.role", is(DEFAULT_ROLE_NAME)));
    }

    /**
     * Test login fails with incorrect password.
     *
     * @traceability [EXC-003]
     */
    @Test
    @Order(5)
    @DisplayName("Login with incorrect password fails")
    void testLoginWrongPassword() throws Exception {
        // Register user first
        var regPayload = new RegistrationRequest(TEST_EMAIL, TEST_PASSWORD, TEST_PROVIDER);
        mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regPayload)))
                .andExpect(status().isOk());

        // Attempt login with wrong password
        var loginPayload = new LoginRequest(TEST_EMAIL, "WrongPassword123");
        mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", containsString("Invalid credentials")));
    }

    /**
     * Test login fails with non-existent email.
     *
     * @traceability [EXC-004]
     */
    @Test
    @Order(6)
    @DisplayName("Login with non-existent email fails")
    void testLoginNonExistentEmail() throws Exception {
        var loginPayload = new LoginRequest("unknown@example.com", TEST_PASSWORD);
        mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("User not found")));
    }

    /* --------------------------------------------------------------------- */
    /*  DTO classes for request payloads – kept internal to test class      */
    /* --------------------------------------------------------------------- */

    /**
     * DTO for registration request.
     *
     * @traceability [DAT-001], [DAT-002]
     */
    private static class RegistrationRequest {
        public String email;
        public String password;
        public String provider;

        public RegistrationRequest(String email, String password, String provider) {
            this.email = email;
            this.password = password;
            this.provider = provider;
        }
    }

    /**
     * DTO for login request.
     *
     * @traceability [DAT-001], [DAT-002]
     */
    private static class LoginRequest {
        public String email;
        public String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
}