package org.nlh4j.saas.membershiphub.repository;

import org.nlh4j.saas.membershiphub.domain.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for {@link Enrollment} entity.
 * <p>
 * Provides CRUD operations via {@link JpaRepository} and a custom query
 * to retrieve an enrollment by tenant, student and course identifiers.
 * </p>
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    /**
     * Find an enrollment by tenant ID, student ID and course ID.
     *
     * @param tenantId the tenant identifier
     * @param studentId the student identifier
     * @param courseId the course identifier
     * @return the matching {@link Enrollment} or {@code null} if none found
     */
    @Query("SELECT e FROM Enrollment e " +
           "WHERE e.tenantId = :tenantId " +
           "AND e.studentId = :studentId " +
           "AND e.courseId = :courseId")
    Enrollment findByStudentIdAndCourseId(
            @Param("tenantId") String tenantId,
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId);
}