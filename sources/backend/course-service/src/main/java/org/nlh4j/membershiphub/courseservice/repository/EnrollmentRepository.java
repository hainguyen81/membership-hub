/**
 * EnrollmentRepository provides data access operations for the Enrollment entity.
 * This repository supports core enrollment management functionalities including
 * duplicate enrollment detection, capacity checking, and persistence.
 *
 * Traceability Tags: [REQ-011], [ARC-007]
 */
package org.nlh4j.membershiphub.courseservice.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.nlh4j.membershiphub.courseservice.model.Enrollment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * Repository interface for Enrollment entity.
 * All methods are transactional and follow enterprise logging and security standards.
 * Traceability Tags: [REQ-011], [ARC-007]
 */
@ApplicationScoped
@Transactional
public interface EnrollmentRepository {

    /**
     * Retrieves an existing enrollment record based on the unique combination of
     * student and course identifiers. This method enforces the business rule that
     * a student cannot be enrolled in the same course more than once.
     *
     * @param studentId UUID of the student.
     * @param courseId  UUID of the course.
     * @return Optional containing the Enrollment if found; empty otherwise.
     * Traceability Tags: [REQ-011], [ARC-007]
     */
    Optional<Enrollment> findByStudentIdAndCourseId(UUID studentId, UUID courseId);

    /**
     * Counts the total number of enrollments associated with a specific course.
     * This method is primarily used to enforce course capacity constraints
     * before allowing a new enrollment.
     *
     * @param courseId UUID of the course.
     * @return Number of current enrollments for the course.
     * Traceability Tags: [REQ-011], [ARC-007]
     */
    long countByCourseId(UUID courseId);

    /**
     * Retrieves all enrollment records for a given course. Useful for reporting
     * and administrative queries such as generating attendance lists or course
     * analytics.
     *
     * @param courseId UUID of the course.
     * @return List of Enrollment entities linked to the course.
     * Traceability Tags: [REQ-011], [ARC-007]
     */
    List<Enrollment> findByCourseId(UUID courseId);

    /**
     * Persists a new Enrollment entity to the underlying data store.
     * This method is wrapped in a transaction and will cascade appropriate
     * lifecycle events (e.g., audit logging) as defined by the entity model.
     *
     * @param enrollment The Enrollment instance to be stored.
     * Traceability Tags: [REQ-011], [ARC-007]
     */
    void persist(Enrollment enrollment);

    /**
     * Deletes an enrollment record by its unique identifier.
     * This operation is irreversible and may trigger related cleanup actions
     * (e.g., releasing seat capacity, revoking associated notifications).
     *
     * @param enrollmentId UUID of the enrollment to delete.
     * Traceability Tags: [REQ-011], [ARC-007]
     */
    void deleteById(UUID enrollmentId);
}