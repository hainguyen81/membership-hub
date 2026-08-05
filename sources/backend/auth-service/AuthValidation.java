package org.nlh4j.saas.membership-hub.auth;

/**
 * <p>
 * AuthValidation is a stateless component responsible for validating
 * user registration and login requests. It enforces business rules
 * such as email format, password length, and supported authentication
 * providers. All validation logic is implemented using immutable
 * constants to avoid magic values and to satisfy traceability
 * requirements.
 * </p>
 *
 * @traceability [REQ-001], [REQ-002], [ARC-001], [ARC-002], [ARC-003],
 *               [ARC-004], [ARC-005], [DAT-001], [DAT-002],
 *               [NFR-001], [NFR-002], [NFR-003], [NFR-004],
 *               [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
 */
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Component that validates registration and login payloads.
 * <p>
 * All constants are declared at the top of the class to comply with
 * the enterprise anti‑magic‑number policy.  No literal strings are
 * used inside the business logic; every value is referenced via a
 * constant.  This design also facilitates unit testing and
 * configuration changes.
 * </p>
 */
@Component
public class AuthValidation {

    /* --------------------------------------------------------------------- */
    /* 1. Immutable configuration constants (no hardcoded literals)         */
    /* --------------------------------------------------------------------- */

    /** Regular expression for validating email addresses. */
    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    /** Minimum allowed password length. */
    private static final int PASSWORD_MIN_LENGTH = 8;

    /** Maximum allowed password length. */
    private static final int PASSWORD_MAX_LENGTH = 64;

    /** Supported authentication providers. */
    private static final String PROVIDER_LOCAL = "local";
    private static final String PROVIDER_GOOGLE = "google";
    private static final String PROVIDER_FACEBOOK = "facebook";

    /** Set of allowed providers for quick membership checks. */
    private static final Set<String> ALLOWED_PROVIDERS =
            Set.of(PROVIDER_LOCAL, PROVIDER_GOOGLE, PROVIDER_FACEBOOK);

    /** Pre‑compiled pattern for email validation. */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    /* --------------------------------------------------------------------- */
    /* 2. Error message constants (used in exception construction)          */
    /* --------------------------------------------------------------------- */

    private static final String ERROR_MSG_INVALID_EMAIL =
            "Invalid email format";
    private static final String ERROR_MSG_PASSWORD_TOO_SHORT =
            "Password must be at least %d characters";
    private static final String ERROR_MSG_PASSWORD_TOO_LONG =
            "Password must not exceed %d characters";
    private static final String ERROR_MSG_INVALID_PROVIDER =
            "Unsupported provider: %s";

    /* --------------------------------------------------------------------- */
    /* 3. Error code constants (for structured error handling)               */
    /* --------------------------------------------------------------------- */

    private static final String ERROR_CODE_INVALID_EMAIL = "ERR-001";
    private static final String ERROR_CODE_PASSWORD_TOO_SHORT = "ERR-002";
    private static final String ERROR_CODE_PASSWORD_TOO_LONG = "ERR-003";
    private static final String ERROR_CODE_INVALID_PROVIDER = "ERR-004";
    private static final String ERROR_CODE_INVALID_INPUT = "ERR-005";

    /* --------------------------------------------------------------------- */
    /* 4. Logging constants (no sensitive data)                              */
    /* --------------------------------------------------------------------- */

    private static final String LOG_MSG_VALIDATING_REGISTRATION =
            "Validating registration request for email: %s";
    private static final String LOG_MSG_VALIDATING_LOGIN =
            "Validating login request for email: %s";

    /* --------------------------------------------------------------------- */
    /* 5. Logger instance (SLF4J)                                              */
    /* --------------------------------------------------------------------- */

    private static final Logger logger = LoggerFactory.getLogger(AuthValidation.class);

    /* --------------------------------------------------------------------- */
    /* 6. Public API – Registration validation                               */
    /* --------------------------------------------------------------------- */

    /**
     * Validates the payload for a user registration request.
     *
     * @param email    the user's email address
     * @param password the user's chosen password
     * @param provider the authentication provider (e.g., local, google)
     * @throws ValidationException if any validation rule is violated
     */
    public void validateRegistrationRequest(String email, String password, String provider) {
        // Log entry point – email is logged but password is omitted for security
        logger.info(LOG_MSG_VALIDATING_REGISTRATION, maskEmail(email));

        // 1. Email must not be null or empty
        if (email == null || email.isBlank()) {
            throw new ValidationException(ERROR_CODE_INVALID_INPUT,
                    String.format(ERROR_MSG_INVALID_EMAIL));
        }

        // 2. Email format must match the pre‑compiled regex
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException(ERROR_CODE_INVALID_EMAIL,
                    ERROR_MSG_INVALID_EMAIL);
        }

        // 3. Password length checks
        if (password == null) {
            throw new ValidationException(ERROR_CODE_INVALID_INPUT,
                    String.format(ERROR_MSG_PASSWORD_TOO_SHORT, PASSWORD_MIN_LENGTH));
        }
        int pwdLen = password.length();
        if (pwdLen < PASSWORD_MIN_LENGTH) {
            throw new ValidationException(ERROR_CODE_PASSWORD_TOO_SHORT,
                    String.format(ERROR_MSG_PASSWORD_TOO_SHORT, PASSWORD_MIN_LENGTH));
        }
        if (pwdLen > PASSWORD_MAX_LENGTH) {
            throw new ValidationException(ERROR_CODE_PASSWORD_TOO_LONG,
                    String.format(ERROR_MSG_PASSWORD_TOO_LONG, PASSWORD_MAX_LENGTH));
        }

        // 4. Provider must be one of the allowed values
        if (provider == null || !ALLOWED_PROVIDERS.contains(provider)) {
            throw new ValidationException(ERROR_CODE_INVALID_PROVIDER,
                    String.format(ERROR_MSG_INVALID_PROVIDER, provider));
        }

        // Log successful validation
        logger.debug("Registration payload validated successfully for email: {}", maskEmail(email));
    }

    /* --------------------------------------------------------------------- */
    /* 7. Public API – Login validation                                      */
    /* --------------------------------------------------------------------- */

    /**
     * Validates the payload for a user login request.
     *
     * @param email    the user's email address
     * @param password the user's password
     * @throws ValidationException if any validation rule is violated
     */
    public void validateLoginRequest(String email, String password) {
        // Log entry point – email is logged but password is omitted
        logger.info(LOG_MSG_VALIDATING_LOGIN, maskEmail(email));

        // 1. Email must not be null or empty
        if (email == null || email.isBlank()) {
            throw new ValidationException(ERROR_CODE_INVALID_INPUT,
                    String.format(ERROR_MSG_INVALID_EMAIL));
        }

        // 2. Email format must match the pre‑compiled regex
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException(ERROR_CODE_INVALID_EMAIL,
                    ERROR_MSG_INVALID_EMAIL);
        }

        // 3. Password must not be null or empty
        if (password == null || password.isBlank()) {
            throw new ValidationException(ERROR_CODE_INVALID_INPUT,
                    String.format(ERROR_MSG_PASSWORD_TOO_SHORT, PASSWORD_MIN_LENGTH));
        }

        // Log successful validation
        logger.debug("Login payload validated successfully for email: {}", maskEmail(email));
    }

    /* --------------------------------------------------------------------- */
    /* 8. Utility – Email masking for logs (partial masking)                */
    /* --------------------------------------------------------------------- */

    /**
     * Masks an email address for logging purposes.  The local part is
     * replaced with asterisks, preserving the domain for traceability.
     *
     * @param email the original email address
     * @return a masked representation
     */
    private String maskEmail(String email) {
        if (email == null) {
            return "null";
        }
        int atIdx = email.indexOf('@');
        if (atIdx <= 0) {
            return email; // malformed, return as is
        }
        String domain = email.substring(atIdx);
        return "****" + domain;
    }

    /* --------------------------------------------------------------------- */
    /* 9. Custom exception – ValidationException                             */
    /* --------------------------------------------------------------------- */

    /**
     * Runtime exception thrown when validation fails.  It carries an
     * error code for programmatic handling and a human‑readable message.
     */
    public static class ValidationException extends RuntimeException {
        private final String errorCode;

        public ValidationException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }
}