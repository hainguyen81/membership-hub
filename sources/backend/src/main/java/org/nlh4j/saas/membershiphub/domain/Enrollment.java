package org.nlh4j.saas.membershiphub.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Enrollment entity linking a student to a course within a tenant.
 * <p>
 * All CRUD operations should be performed via parameterized queries in the repository layer.
 * </p>
 */
@Entity
@Table(name = "enrollments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"}),
       indexes = @javax.persistence.Index(name = "idx_tenant_id", columnList = "tenant_id"))
@org.nlh4j.saas.membershiphub.annotation.TenantFilter
public class Enrollment {

    public enum Status {
        PENDING,
        ACTIVE,
        COMPLETED,
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @NotNull
    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @NotNull
    @Column(name = "enrollment_date", nullable = false)
    private LocalDateTime enrollmentDate;

    @NotNull
    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /* ---------- Constructors ---------- */

    public Enrollment() {
        // Default constructor for JPA
    }

    public Enrollment(Long studentId, Long courseId, Status status, LocalDateTime enrollmentDate, String tenantId) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.status = status;
        this.enrollmentDate = enrollmentDate;
        this.tenantId = tenantId;
    }

    /* ---------- Getters & Setters ---------- */

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDateTime enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /* ---------- Utility Methods ---------- */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Enrollment)) return false;
        Enrollment that = (Enrollment) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(studentId, that.studentId) &&
               Objects.equals(courseId, that.courseId) &&
               status == that.status &&
               Objects.equals(enrollmentDate, that.enrollmentDate) &&
               Objects.equals(tenantId, that.tenantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, studentId, courseId, status, enrollmentDate, tenantId);
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", courseId=" + courseId +
                ", status=" + status +
                ", enrollmentDate=" + enrollmentDate +
                ", tenantId='" + tenantId + '\'' +
                '}';
    }
}