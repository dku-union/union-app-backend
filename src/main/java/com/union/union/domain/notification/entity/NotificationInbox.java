package com.union.union.domain.notification.entity;

import com.union.union.domain.user.entity.User;
import com.union.union.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification_inbox",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "campaign_id"}),
        indexes = {
                @Index(name = "idx_inbox_user_created", columnList = "user_id, created_at DESC"),
                @Index(name = "idx_inbox_user_unread", columnList = "user_id, is_read")
        })
public class NotificationInbox extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private NotificationCampaign campaign;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Builder
    public NotificationInbox(User user, NotificationCampaign campaign) {
        this.user = user;
        this.campaign = campaign;
        this.read = false;
    }

    public void markRead() {
        this.read = true;
        this.readAt = LocalDateTime.now();
    }
}
