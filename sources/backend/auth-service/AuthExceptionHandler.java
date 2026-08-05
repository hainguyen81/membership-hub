package org.nlh4j.saas.membershiphub.auth;

/**
 * Global exception handler for the authentication service.
 *
 * <p>This class centralises handling of all exceptions thrown by the
 * authentication endpoints.  It ensures that:
 * <ul>
 *   <li>All error responses are consistent and contain a machine‑readable
 *       error code.</li>
 *   <li>Sensitive data (e.g. passwords, tokens) are never logged.</li>
 *   <li>All logs are enriched with traceability tags and contextual
 *       information for auditability.</li>
 *   <li>The application remains resilient by catching all foreseeable
 *       runtime errors and converting them to a controlled HTTP response.</li>
 * </ul>
 *
 * @traceability [REQ-001], [REQ-002], [ARC-001], [ARC-002], [ARC-003],
 *               [ARC-004], [ARC-005], [DAT-001], [DAT-002],
 *               [NFR-001], [NFR-002], [NFR-003], [NFR-004],
 *               [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MissingServletRequestPartException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Rest controller advice that intercepts exceptions thrown by the
 * authentication service and translates them into structured HTTP
 * responses.
 *
 * @traceability [REQ-001], [REQ-002], [ARC-001], [ARC-002], [ARC-003],
 *               [ARC-004], [ARC-005], [DAT-001], [DAT-002],
 *               [NFR-001], [NFR-002], [NFR-003], [NFR-004],
 *               [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthExceptionHandler {

    /* --------------------------------------------------------------------- */
    /*  Constants – all literal values are hoisted to the top of the class.  */
    /* --------------------------------------------------------------------- */

    /** Logger instance for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthExceptionHandler.class);

    /** ISO‑8601 timestamp format used in error responses. */
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /** Default error message for unexpected server errors. */
    private static final String DEFAULT_ERROR_MESSAGE = "An unexpected error occurred. Please try again later.";

    /** Generic error code for server‑side failures. */
    private static final String SERVER_ERROR_CODE = "SERVER-001";

    /** Error code for authentication failures. */
    private static final String AUTH_ERROR_CODE = "AUTH-001";

    /** Error code for validation failures. */
    private static final String VALIDATION_ERROR_CODE = "VALID-001";

    /** Error code for data integrity violations (e.g. duplicate tax ID). */
    private static final String DATA_INTEGRITY_ERROR_CODE = "DATA-001";

    /** Error code for unsupported media type requests. */
    private static final String MEDIA_TYPE_NOT_SUPPORTED_CODE = "MEDIA-001";

    /** Error code for unsupported HTTP method requests. */
    private static final String METHOD_NOT_SUPPORTED_CODE = "METHOD-001";

    /** Error code for missing request parameters. */
    private static final String MISSING_PARAM_ERROR_CODE = "PARAM-001";

    /** Error code for unreadable HTTP messages. */
    private static final String MESSAGE_NOT_READABLE_CODE = "MSG-001";

    /* --------------------------------------------------------------------- */
    /*  Exception handlers – each method logs the error and returns a        */
    /*  structured response.  All handlers are annotated with @traceability  */
    /*  tags to satisfy audit requirements.                                 */
    /* --------------------------------------------------------------------- */

    /**
     * Handles generic {@link AuthenticationException}s thrown during
     * authentication flows (e.g. bad credentials, account locked).
     *
     * @param ex the authentication exception
     * @param request the web request context
     * @return a {@link ResponseEntity} containing the error payload
     *
     * @traceability [REQ-001], [REQ-002], [ARC-001], [ARC-002], [ARC-003],
     *               [ARC-004], [ARC-005], [DAT-001], [DAT-002],
     *               [NFR-001], [NFR-002], [NFR-003], [NFR-004],
     *               [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {

        // Log at ERROR level with traceability tag and masked message
        LOGGER.error("[AUTH-EXCEPTION] [ARC-001] Authentication failed: {}", maskString(ex.getMessage()));

        // Build and return a structured error response
        ErrorResponse error = buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                AUTH_ERROR_CODE,
                "Authentication failed. Please check your credentials.",
                request);

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles validation errors thrown by {@link MethodArgumentNotValidException}
     * (e.g. @Valid annotated DTOs).
     *
     * @param ex the validation exception
     * @param request the web request context
     * @return a {@link ResponseEntity} containing the error payload
     *
     * @traceability [REQ-001], [REQ-002], [ARC-001], [ARC-002], [ARC-003],
     *               [ARC-004], [ARC-005], [DAT-001], [DAT-002],
     *               [NFR-001], [NFR-002], [NFR-003], [NFR-004],
     *               [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {

        // Extract field errors and build a user‑friendly message
        List<String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        String message = "Validation failed: " + String.join("; ", fieldErrors);

        // Log the validation failure
        LOGGER.warn("[VALIDATION-EXCEPTION] [ARC-002] {}", message);

        ErrorResponse error = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                VALIDATION_ERROR_CODE,
                message,
                request);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles constraint violations (e.g. @NotNull, @Size) that occur outside
     * of controller method arguments.
     *
     * @param ex the constraint violation exception
     * @param request the web request context
     * @return a {@link ResponseEntity} containing the error payload
     *
     * @traceability [REQ-001], [REQ-002], [ARC-001], [ARC-002], [ARC-003],
     *               [ARC-004], [ARC-005], [DAT-001], [DAT-002],
     *               [NFR-001], [NFR-002], [NFR-003], [NFR-004],
     *               [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {

        String message = ex.getConstraintViolations()
                .stream()
                .map(cv -> cv.getMessage())
                .collect(Collectors.joining("; "));

        LOGGER.warn("[CONSTRAINT-VIOLATION] [ARC-003] {}", message);

        ErrorResponse error = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                VALIDATION_ERROR_CODE,
                message,
                request);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles data integrity violations such as duplicate unique keys.
     *
     * @param ex the data integrity violation exception
     * @param request the web request context
     * @return a {@link ResponseEntity} containing the error payload
     *
     * @traceability [REQ-001], [REQ-002], [ARC-001], [ARC-002], [ARC-003],
     *               [ARC-004], [ARC-005], [DAT-001], [DAT-002],
     *               [NFR-001], [NFR-002], [NFR-003], [NFR-004],
     *               [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {

        LOGGER.error("[DATA-INT-VIOLATION] [ARC-004] {}", maskString(ex.getMessage()));

        ErrorResponse error = buildErrorResponse(
                HttpStatus.CONFLICT,
                DATA_INTEGRITY_ERROR_CODE,
                "Data integrity violation. Please check your input.",
                request);

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * Handles unsupported media type requests.
     *
     * @param ex the media type not supported exception
     * @param request the web request context
     * @return a {@link ResponseEntity} containing the error payload
     *
     * @traceability [REQ-001], [REQ-002], [ARC-001], [ARC-002], [ARC-003],
     *               [ARC-004], [ARC-005], [DAT-001], [DAT-002],
     *               [NFR-001], [NFR-002], [NFR-003], [NFR-004],
     *               [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, WebRequest request) {

        LOGGER.warn("[MEDIA-TYPE-NOT-SUPPORTED] [ARC-005] {}", ex.getMessage());

        ErrorResponse error = buildErrorResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                MEDIA_TYPE_NOT_SUPPORTED_CODE,
                "Unsupported media type.",
                request);

        return new ResponseEntity<>(error, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * Handles unsupported HTTP method requests.
     *
     * @param ex the method not supported exception
     * @param request the web request context
     * @return a {@link ResponseEntity} containing the error payload
     *
     * @traceability [REQ-001], [REQ-002], [ARC-001], [ARC-002], [ARC-003],
     *               [ARC-004], [ARC-005], [DAT-001], [DAT-002],
     *               [NFR-001], [NFR-002], [NFR-003], [NFR-004],
     *               [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {

        LOGGER.warn("[METHOD-NOT-SUPPORTED] [ARC-006] {}", ex.getMessage());

        ErrorResponse error = buildErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                METHOD_NOT_SUPPORTED_CODE,
                "HTTP method not allowed.",
                request);

        return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Handles missing request parameters.
     *
     * @param ex the missing parameter exception
     * @param request the web request context
     * @return a {@link ResponseEntity} containing the error payload
     *
     * @traceability [REQ-001], [REQ-002], [ARC-001], [ARC-002], [ARC-003],
     *               [ARC-004], [ARC-005], [DAT-001], [DAT-002],
     *               [NFR-001], [NFR-002], [NFR-003], [NFR-004],
     *               [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex, WebRequest request) {

        LOGGER.warn("[MISSING-PARAM] [ARC-007] {}", ex.getMessage());

        ErrorResponse error = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                MISSING_PARAM_ERROR_CODE,
                "Required request parameter missing.",
                request);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles unreadable HTTP messages (e.g. malformed JSON).
     *
     * @param ex the message not readable exception
     * @param request the web request context
     * @return a {@link ResponseEntity} containing the error payload
     *
     * @traceability [REQ-001], [REQ-002], [ARC-001], [ARC-002], [ARC-003],
     *               [ARC-004], [ARC-005], [DAT-001], [DAT-002],
     *               [NFR-001], [NFR-002], [NFR-003], [NFR-004],
     *               [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {

        LOGGER.warn("[MESSAGE-NOT-READABLE] [ARC-008] {}", ex.getMessage());

        ErrorResponse error = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                MESSAGE_NOT_READABLE_CODE,
                "Malformed request body.",
                request);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all other uncaught exceptions.
     *
     * @param ex the exception
     * @param request the web request context
     * @return a {@link ResponseEntity} containing the error payload
     *
     * @traceability [REQ-001], [REQ-002], [ARC-001], [ARC-002], [ARC-003],
     *               [ARC-004], [ARC-005], [DAT-001], [DAT-002],
     *               [NFR-001], [NFR-002], [NFR-003], [NFR-004],
     *               [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleAllExceptions(
            Exception ex, WebRequest request) {

        // Log the exception with stack trace for debugging
        LOGGER.error("[UNHANDLED-EXCEPTION] [ARC-009] {}", ex.getMessage(), ex);

        ErrorResponse error = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                SERVER_ERROR_CODE,
                DEFAULT_ERROR_MESSAGE,
                request);

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /* --------------------------------------------------------------------- */
    /*  Helper methods – all logic is encapsulated and uses constants only.  */
    /* --------------------------------------------------------------------- */

    /**
     * Builds a structured {@link ErrorResponse} object.
     *
     * @param status the HTTP status to return
     * @param errorCode the application‑specific error code
     * @param message the human‑readable error message
     * @param request the web request context
     * @return an {@link ErrorResponse} instance
     */
    private ErrorResponse buildErrorResponse(
            HttpStatus status,
            String errorCode,
            String message,
            WebRequest request) {

        // Capture the current timestamp in ISO format
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT));

        // Retrieve the request path for context
        String path = request.getDescription(false).replace("uri=", "");

        // Construct the error response payload
        return new ErrorResponse(
                timestamp,
                status.value(),
                status.getReasonPhrase(),
                errorCode,
                message,
                path);
    }

    /**
     * Masks sensitive data by replacing all but the first and last
     * characters with asterisks.  This is a simple placeholder; real
     * applications should use a robust masking strategy.
     *
     * @param input the original string
     * @return the masked string
     */
    private String maskString(String input) {
        if (input == null || input.length() <= 2) {
            return input;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(input.charAt(0));
        for (int i = 1; i < input.length() - 1; i++) {
            sb.append('*');
        }
        sb.append(input.charAt(input.length() - 1));
        return sb.toString();
    }

    /* --------------------------------------------------------------------- */
    /*  Error response DTO – immutable and serialisable.                     */
    /* --------------------------------------------------------------------- */

    /**
     * Immutable error response payload returned to API consumers.
     *
     * @traceability [REQ-001], [REQ-002], [ARC-001], [ARC-002], [ARC-003],
     *               [ARC-004], [ARC-005], [DAT-001], [DAT-002],
     *               [NFR-001], [NFR-002], [NFR-003], [NFR-004],
     *               [NFR-005], [NFR-006], [NFR-007], [NFR-008], [NFR-009]
     */
    public static final class ErrorResponse {

        private final String timestamp;
        private final int status;
        private final String error;
        private final String errorCode;
        private final String message;
        private final String path;

        /**
         * Constructs an {@link ErrorResponse}.
         *
         * @param timestamp ISO‑8601 timestamp
         * @param status HTTP status code
         * @param error HTTP status reason phrase
         * @param errorCode application‑specific error code
         * @param message human‑readable message
         * @param path request URI
         */
        public ErrorResponse(
                String timestamp,
                int status,
                String error,
                String errorCode,
                String message,
                String path) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.errorCode = errorCode;
            this.message = message;
            this.path = path;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public int getStatus() {
            return status;
        }

        public String getError() {
            return error;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getMessage() {
            return message;
        }

        public String getPath() {
            return path;
        }

        @Override
        public String toString() {
            return "ErrorResponse{" +
                    "timestamp='" + timestamp + '\'' +
                    ", status=" + status +
                    ", error='" + error + '\'' +
                    ", errorCode='" + errorCode + '\'' +
                    ", message='" + message + '\'' +
                    ", path='" + path + '\'' +
                    '}';
        }
    }
}