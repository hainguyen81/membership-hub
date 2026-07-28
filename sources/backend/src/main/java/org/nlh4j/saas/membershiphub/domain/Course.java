package org.nlh4j.saas.membershiphub.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.AssertTrue;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.nlh4j.saas.membershiphub.annotation.TenantFilter;

/**
 * Represents a course offered by a center within a tenant.
 * <p>
 * The entity enforces:
 * <ul>
 *   <li>Multi‑tenant isolation via {@link TenantFilter}</li>
 *   <li>Unique constraint on {@code (center_id, title)}</li>
 *   <li>Audit timestamps for creation and updates</li>
 *   <li>Validation that {@code startDate} precedes {@code endDate}</li>
 * </ul>
 * </p>
 */
@Entity
@Table(
    name = "courses",
    uniqueConstraints = @UniqueConstraint(columnNames = {"center_id", "title"})
)
@TenantFilter
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull
    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @NotNull
    @Column(name = "center_id", nullable = false)
    private Long centerId;

    @NotNull
    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /* --------------------------------------------------------------------- */
    /*  Validation Methods                                                   */
    /* --------------------------------------------------------------------- */

    /**
     * Ensures that the start date is not after the end date.
     *
     * @return {@code true} if the dates are valid, otherwise {@code false}
     */
    @AssertTrue(message = "Start date must be before or equal to end date")
    private boolean isDateRangeValid() {
        return startDate == null || endDate == null || !startDate.isAfter(endDate);
    }

    /* --------------------------------------------------------------------- */
    /*  Getters & Setters                                                    */
    /* --------------------------------------------------------------------- */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public Long getCenterId() {
        return centerId;
    }

    public void setCenterId(Long centerId) {
        this.centerId = centerId;
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
}