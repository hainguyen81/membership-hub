package org.nlh4j.saas.membershiphub.domain;

import java.util.Date;
import java.util.Objects;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * User entity for authentication and role-based access control.
 * <p>
 * This entity is part of the multi‑tenant architecture. All queries are automatically
 * filtered by {@link TenantFilter} to ensure tenant isolation. Soft delete is
 * implemented via {@code isDeleted} flag and {@link @Where} clause.
 * </p>
 */
@Entity
@Table(name = "users",
       uniqueConstraints = @UniqueConstraint(columnNames = {"email", "tenant_id"}))
@Where(clause = "is_deleted = false")
@TenantFilter
@EntityListeners({AuditingEntityListener.class})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

    /**
     * Primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = Access.READ_ONLY)
    private Long id;

    /**
     * User's email address. Must be unique per tenant.
     */
    @Column(name = "email", nullable = false, length = 255, unique = true)
    @NotBlank
    @Email
    @Size(max = 255)
    @JsonProperty(access = Access.READ_ONLY)
    private String email;

    /**
     * BCrypt hashed password. Stored as a 60‑character string.
     */
    @Column(name = "password_hash", nullable = false, length = 60)
    @NotBlank
    @Size(min = 60, max = 60)
    @JsonProperty(access = Access.WRITE_ONLY)
    private String passwordHash;

    /**
     * Role of the user. Stored as a string.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @NotNull
    @JsonProperty(access = Access.READ_ONLY)
    private Role role;

    /**
     * Center the user belongs to. Nullable for system‑wide users.
     */
    @Column(name = "center_id")
    private Long centerId;

    /**
     * Tenant identifier for multi‑tenant isolation.
     */
    @Column(name = "tenant_id", nullable = false, length = 36)
    @NotBlank
    @Size(max = 36)
    @JsonProperty(access = Access.READ_ONLY)
    private String tenantId;

    /**
     * Soft delete flag.
     */
    @Column(name = "is_deleted", nullable = false)
    @JsonProperty(access = Access.READ_ONLY)
    private Boolean isDeleted = Boolean.FALSE;

    /**
     * Creation timestamp. Managed by Spring Data JPA.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @CreatedDate
    @JsonProperty(access = Access.READ_ONLY)
    private Date createdAt;

    /**
     * Last update timestamp. Managed by Spring Data JPA.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    @LastModifiedDate
    @JsonProperty(access = Access.READ_ONLY)
    private Date updatedAt;

    /* --------------------------------------------------------------------- */
    /* Constructors                                                          */
    /* --------------------------------------------------------------------- */

    public User() {
        // Default constructor for JPA
    }

    public User(String email, String rawPassword, Role role, Long centerId, String tenantId) {
        this.email = email;
        setPassword(rawPassword);
        this.role = role;
        this.centerId = centerId;
        this.tenantId = tenantId;
    }

    /* --------------------------------------------------------------------- */
    /* Getters & Setters                                                    */
    /* --------------------------------------------------------------------- */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @JsonIgnore
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
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

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /* --------------------------------------------------------------------- */
    /* Business Logic                                                       */
    /* --------------------------------------------------------------------- */

    /**
     * Sets the user's password by hashing the raw password with BCrypt.
     *
     * @param rawPassword the plain text password
     */
    public void setPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        this.passwordHash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * Verifies a raw password against the stored BCrypt hash.
     *
     * @param rawPassword the plain text password to verify
     * @return true if the password matches, false otherwise
     */
    public boolean checkPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank() || this.passwordHash == null) {
            return false;
        }
        return BCrypt.checkpw(rawPassword, this.passwordHash);
    }

    /* --------------------------------------------------------------------- */
    /* Lifecycle Callbacks                                                  */
    /* --------------------------------------------------------------------- */

    @PrePersist
    protected void onCreate() {
        this.isDeleted = Boolean.FALSE;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }

    /* --------------------------------------------------------------------- */
    /* Object Overrides                                                     */
    /* --------------------------------------------------------------------- */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", email='" + email + '\'' +
               ", role=" + role +
               ", centerId=" + centerId +
               ", tenantId='" + tenantId + '\'' +
               ", isDeleted=" + isDeleted +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }

    /* --------------------------------------------------------------------- */
    /* Role Enumeration                                                    */
    /* --------------------------------------------------------------------- */

    public enum Role {
        SYSTEM_ADMIN,
        ADMIN,
        MANAGER,
        TEACHER,
        STUDENT
    }
}