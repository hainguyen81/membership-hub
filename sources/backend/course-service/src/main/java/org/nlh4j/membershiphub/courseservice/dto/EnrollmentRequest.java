package org.nlh4j.membershiphub.courseservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.UUID;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DTO representing the request payload for enrolling a student into a course.
 *
 * <p>Business Logic:
 *   <ul>
 *     <li>Contains the unique identifier of the course to be enrolled.</li>
 *     <li>Validated strictly to ensure non‑null and well‑formed UUID format.</li>
 *     <li>Used by {@link org.nlh4j.membershiphub.courseservice.controller.EnrollmentController}
 *         to drive the enrollment transaction.</li>
 *   </ul>
 *
 * @traceability [REQ-011], [ARC-007]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnrollmentRequest {

    /** Logger for audit and traceability – injected by Quarkus / CDI. */
    private static final Logger logger = LoggerFactory.getLogger(EnrollmentRequest.class);

    /**
     * Unique identifier of the course to be enrolled.
     * <p>Business Rules:
     *   <ul>
     *     <li>Must not be {@code null} – enforced by {@link NotNull}.</li>
     *     <li>Must conform to RFC‑4122 UUID format – enforced by {@link UUID}.</li>
     *     <li>Corresponds to the {@code course_id} column in the {@code courses} table.</li>
     *   </ul>
     * @traceability [REQ-011], [ARC-007]
     */
    @NotNull(message = "{enrollment.courseId.notNull}")
    @UUID(message = "{enrollment.courseId.uuid}")
    @JsonProperty("courseId")
    private String courseId;

    /**
     * Optional human‑readable comment for the enrollment – reserved for future use.
     * <p>Business Rules:
     *   <ul>
     *     <li>Maximum length 500 characters to avoid abuse.</li>
     *     <li>May be {@code null} – validation permits absence.</li>
     *   </ul>
     * @traceability [REQ-011], [ARC-007]
     */
    @Size(max = 500, message = "{enrollment.comment.size}")
    @JsonProperty("comment")
    private String comment;

    // -------------------------------------------------------------------------
    // Constants – all literal strings, error codes and validation messages are
    // hoisted to the class crown to satisfy the Anti‑Magic‑Numbers policy.
    // -------------------------------------------------------------------------
    /** Validation message keys – kept immutable at the top of the class. */
    public static final String MSG_COURSE_ID_NOT_NULL = "Course ID must be provided";
    public static final String MSG_COURSE_ID_UUID      = "Course ID must be a valid UUID";
    public static final String MSG_COMMENT_SIZE        = "Comment may not exceed 500 characters";

    // -------------------------------------------------------------------------
    // Helper factory method – demonstrates defensive copying and immutability.
    // -------------------------------------------------------------------------
    /**
     * Factory method to create an {@code EnrollmentRequest} from a raw UUID string.
     * <p>Ensures that the internal representation is always a trimmed, non‑null value.
     *
     * @param courseId the raw course identifier
     * @return a new {@code EnrollmentRequest} instance
     * @throws IllegalArgumentException if {@code courseId} is {@code null} or blank
     * @traceability [REQ-011], [ARC-007]
     */
    public static EnrollmentRequest of(String courseId) {
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new IllegalArgumentException(MSG_COURSE_ID_NOT_NULL);
        }
        EnrollmentRequest req = new EnrollmentRequest();
        req.courseId = courseId.trim();
        return req;
    }
}