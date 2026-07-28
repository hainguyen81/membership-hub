package org.nlh4j.saas.membershiphub.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Notification entity representing system messages sent to users.
 * <p>
 * GDPR retention policy is enforced by a scheduled job that deletes
 * notifications older than the configured retention period. The entity
 * itself contains only the core fields required for the notification
 * lifecycle.
 * </p>
 */
@Entity
@Table(name = "notifications")
@TenantFilter
public class Notification {

    public enum Channel {
        ZALO,
        MOBILE_PUSH
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_id", nullable = false)
    @NotNull
    private Long recipientId;

    @Column(name = "message", nullable = false, length = 2048)
    @NotNull
    @Size(max = 2048)
    private String message;

    @Column(name = "sent_at", nullable = false)
    @NotNull
    private LocalDateTime sentAt;

    @Column(name = "channel", nullable = false, length = 20)
    @NotNull
    @Enumerated(EnumType.STRING)
    private Channel channel;

    @Column(name = "tenant_id", nullable = false)
    @NotNull
    private String tenantId;

    /* --------------------------------------------------------------------- */
    /* Constructors                                                          */
    /* --------------------------------------------------------------------- */

    public Notification() {
        // Default constructor for JPA
    }

    public Notification(Long recipientId, String message, Channel channel, String tenantId) {
        this.recipientId = recipientId;
        this.message = message;
        this.channel = channel;
        this.tenantId = tenantId;
        this.sentAt = LocalDateTime.now();
    }

    /* --------------------------------------------------------------------- */
    /* Getters & Setters                                                    */
    /* --------------------------------------------------------------------- */

    public Long getId() {
        return id;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /* --------------------------------------------------------------------- */
    /* Utility Methods                                                      */
    /* --------------------------------------------------------------------- */

    /**
     * Determines whether this notification is older than the specified
     * retention period. The retention period is expressed in days.
     *
     * @param retentionDays number of days to retain notifications
     * @return true if the notification should be deleted
     */
    public boolean isExpired(int retentionDays) {
        return sentAt.isBefore(LocalDateTime.now().minusDays(retentionDays));
    }
}