package com.union.union.domain.subscription.entity;

import com.union.union.domain.miniapp.entity.MiniApp;
import com.union.union.domain.user.entity.User;
import com.union.union.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "mini_app_subscriptions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_mini_app_subscriptions_user_app",
                columnNames = {"user_id", "mini_app_id"}
        ),
        indexes = {
                @Index(name = "idx_mini_app_subscriptions_app_push",
                        columnList = "mini_app_id, push_enabled, unsubscribed_at"),
                @Index(name = "idx_mini_app_subscriptions_user",
                        columnList = "user_id")
        }
)
public class MiniAppSubscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mini_app_id", nullable = false)
    private MiniApp miniApp;

    @Column(name = "subscribed_at", nullable = false)
    private LocalDateTime subscribedAt;

    @Column(name = "push_enabled", nullable = false)
    private boolean pushEnabled;

    @Column(name = "unsubscribed_at")
    private LocalDateTime unsubscribedAt;

    @Builder
    public MiniAppSubscription(User user, MiniApp miniApp) {
        this.user = user;
        this.miniApp = miniApp;
        this.subscribedAt = LocalDateTime.now();
        this.pushEnabled = true;
    }

    public boolean isActive() {
        return unsubscribedAt == null;
    }

    public void reactivate() {
        this.unsubscribedAt = null;
        this.pushEnabled = true;
        this.subscribedAt = LocalDateTime.now();
    }

    public void setPushEnabled(boolean enabled) {
        this.pushEnabled = enabled;
    }

    public void unsubscribe() {
        this.unsubscribedAt = LocalDateTime.now();
        this.pushEnabled = false;
    }
}
