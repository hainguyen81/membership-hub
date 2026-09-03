package org.nlh4j.membershiphub.courseservice.exception;

/**
 * Exception thrown when an enrollment record is not found for the specified enrollment ID or student-course combination.
 *
 * Traceability Tags: [REQ-011], [ARC-007]
 */
public class EnrollmentNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new enrollment not found exception with the specified detail message.
     *
     * @param message the detail message
     */
    public EnrollmentNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new enrollment not found exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public EnrollmentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}