package org.nlh4j.saas.membershiphub.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Attendance entity representing a QR check‑in record.
 * <p>
 * The QR token is stored as a SHA‑256 hash to prevent replay attacks.
 * Multi‑tenant isolation is enforced via {@link org.nlh4j.saas.membershiphub.annotation.TenantFilter}.
 * </p>
 */
@Entity
@Table(name = "attendances",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id", "attendance_date"}))
@org.nlh4j.saas.membershiphub.annotation.TenantFilter
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @NotNull
    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @NotNull
    @Column(name = "attendance_date", nullable = false)
    private LocalDateTime attendanceDate;

    @NotNull
    @Column(name = "qr_token_hash", nullable = false, length = 64)
    private String qrTokenHash;

    @NotNull
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /* --------------------------------------------------------------------- */
    /*  Constructors                                                          */
    /* --------------------------------------------------------------------- */

    public Attendance() {
        // Default constructor for JPA
    }

    public Attendance(Long studentId, Long courseId, LocalDateTime attendanceDate,
                      String qrToken, String tenantId) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.attendanceDate = attendanceDate;
        setQrToken(qrToken);
        this.tenantId = tenantId;
    }

    /* --------------------------------------------------------------------- */
    /*  Getters & Setters                                                    */
    /* --------------------------------------------------------------------- */

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

    public LocalDateTime getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDateTime attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public String getQrTokenHash() {
        return qrTokenHash;
    }

    /**
     * Sets the QR token by hashing it with SHA‑256.
     *
     * @param token the raw QR token
     */
    public void setQrToken(String token) {
        this.qrTokenHash = hashToken(token);
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

    /* --------------------------------------------------------------------- */
    /*  Utility Methods                                                      */
    /* --------------------------------------------------------------------- */

    /**
     * Computes the SHA‑256 hash of the provided token and returns it as a
     * 64‑character hexadecimal string.
     *
     * @param token the raw token
     * @return the SHA‑256 hash in hex
     */
    private static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be available; rethrow as unchecked
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}