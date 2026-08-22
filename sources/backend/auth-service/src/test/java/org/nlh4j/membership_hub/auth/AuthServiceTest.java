package org.nlh4j.saas.membership_hub.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

/**
 * Unit tests for {@link AuthService}.
 *
 * @verifies [REQ-001], [EXC-004]
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtEncoder jwtEncoder;
    @Mock
    private FirebaseAuthService firebaseAuthService;
    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @Mock
    private IdempotencyKeyService idempotencyKeyService;

    @InjectMocks
    private AuthService authService;

    private final UUID userId = UUID.randomUUID();
    private final String email = "user@example.com";
    private final String password = "P@ssw0rd!";
    private final String encodedPassword = "$2a$10$encoded";
    private final String accessToken = "access-token";
    private final String refreshToken = "refresh-token";

    @BeforeEach
    void setUp() {
        // Stub JWT encoder to return predictable tokens
        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(new org.springframework.security.oauth2.jwt.Jwt("token", Instant.now(), Instant.now().plusSeconds(60),
                        Collections.emptyMap(), Collections.emptyMap()));
    }

    @Nested
    @DisplayName("authenticate")
    class AuthenticateTests {

        @Test
        @DisplayName("should authenticate successfully and return tokens")
        @verifies [REQ-001], [EXC-004]
        void testAuthenticateSuccess() {
            // Arrange: mock authentication manager to succeed
            Authentication auth = new UsernamePasswordAuthenticationToken(email, password);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(auth);

            // Act
            AuthenticationResponse response = authService.authenticate(
                    new AuthenticationRequest(email, password, null));

            // Assert: tokens are present and userId matches
            assertNotNull(response.getAccessToken(), "Access token should not be null");
            assertNotNull(response.getRefreshToken(), "Refresh token should not be null");
            assertEquals(JwtTokenConstants.ACCESS_TOKEN_TYPE, response.getTokenType(),
                    "Token type should be Bearer");
            assertEquals(JwtTokenConstants.ACCESS_TOKEN_EXPIRATION_MS / 1000,
                    response.getExpiresIn(), "ExpiresIn should match constant");
            assertNull(response.getUserId(), "UserId is not set for authentication");
        }

        @Test
        @DisplayName("should throw AuthenticationException on bad credentials")
        @verifies [REQ-001], [EXC-004]
        void testAuthenticateBadCredentials() {
            // Arrange: authentication manager throws BadCredentialsException
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // Act & Assert
            AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
                    authService.authenticate(new AuthenticationRequest(email, "wrong", null)),
                    "Expected AuthenticationException for bad credentials");
            assertEquals(JwtTokenConstants.ERROR_CODE_INVALID_CREDENTIALS,
                    ex.getMessage(), "Error code should indicate invalid credentials");
        }
    }

    @Nested
    @DisplayName("register")
    class RegisterTests {

        @Test
        @DisplayName("should register new user and return tokens")
        @verifies [REQ-001], [EXC-004]
        void testRegisterSuccess() {
            // Arrange: email not existing, password encoder, Firebase registration
            when(userRepository.existsByEmail(email)).thenReturn(false);
            when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
            when(userRepository.save(any(Users.class))).thenAnswer(invocation -> {
                Users u = invocation.getArgument(0);
                u.setUserId(userId);
                return u;
            });

            // Act
            AuthenticationResponse response = authService.register(
                    new AuthenticationRequest(email, password, "John Doe"));

            // Assert: user saved, Firebase called, tokens returned
            verify(userRepository).save(any(Users.class));
            verify(firebaseAuthService).registerUser(any(Users.class));
            assertEquals(userId, response.getUserId(), "Returned userId should match saved user");
            assertNotNull(response.getAccessToken(), "Access token should be generated");
            assertNotNull(response.getRefreshToken(), "Refresh token should be generated");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when email already exists")
        @verifies [REQ-001], [EXC-004]
        void testRegisterDuplicateEmail() {
            // Arrange: email already exists
            when(userRepository.existsByEmail(email)).thenReturn(true);

            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    authService.register(new AuthenticationRequest(email, password, "John Doe")),
                    "Expected exception for duplicate email");
            assertEquals("Email already registered", ex.getMessage(),
                    "Exception message should indicate duplicate email");
        }

        @Test
        @DisplayName("should validate password strength and throw IllegalArgumentException")
        @verifies [REQ-001], [EXC-004]
        void testRegisterInvalidPassword() {
            // Arrange: weak password
            String weakPassword = "weak";

            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    authService.register(new AuthenticationRequest(email, weakPassword, "John Doe")),
                    "Expected exception for weak password");
            assertTrue(ex.getMessage().contains("Password must contain"),
                    "Exception message should mention password requirements");
        }
    }

    @Nested
    @DisplayName("loadUserByUsername")
    class LoadUserTests {

        @Test
        @DisplayName("should load user details successfully")
        @verifies [REQ-001], [EXC-004]
        void testLoadUserByUsernameSuccess() {
            // Arrange: user exists
            Users user = new Users();
            user.setUserId(userId);
            user.setEmail(email);
            user.setPasswordHash(encodedPassword);
            user.setEnabled(true);
            user.setRoles(Collections.singleton(new Roles(5, "STUDENT", null)));
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

            // Act
            UserDetails details = authService.loadUserByUsername(email);

            // Assert: details contain correct username and authorities
            assertEquals(email, details.getUsername(), "Username should match email");
            assertTrue(details.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")),
                    "User should have STUDENT role authority");
        }

        @Test
        @DisplayName("should throw UsernameNotFoundException when user not found")
        @verifies [REQ-001], [EXC-004]
        void testLoadUserByUsernameNotFound() {
            // Arrange: user not found
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            // Act & Assert
            UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class, () ->
                    authService.loadUserByUsername(email),
                    "Expected UsernameNotFoundException for missing user");
            assertTrue(ex.getMessage().contains(email),
                    "Exception message should reference missing email");
        }
    }

    @Nested
    @DisplayName("idempotency")
    class IdempotencyTests {

        @Test
        @DisplayName("should validate idempotency key")
        @verifies [REQ-001], [EXC-004]
        void testIsIdempotent() {
            // Arrange: key is valid
            String key = "unique-key";
            when(idempotencyKeyService.isValid(key)).thenReturn(true);

            // Act
            boolean result = authService.isIdempotent(key);

            // Assert
            assertTrue(result, "Idempotency key should be considered valid");
            verify(idempotencyKeyService).isValid(key);
        }

        @Test
        @DisplayName("should mark key as processed")
        @verifies [REQ-001], [EXC-004]
        void testMarkAsProcessed() {
            // Arrange
            String key = "unique-key";

            // Act
            authService.markAsProcessed(key);

            // Assert
            verify(idempotencyKeyService).markAsProcessed(key);
        }
    }
}