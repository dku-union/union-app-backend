package com.union.union.domain.notification.entity;

import com.union.union.domain.publisher.entity.Publisher;
import com.union.union.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "publisher_fcm_tokens",
        uniqueConstraints = @UniqueConstraint(columnNames = {"publisher_id", "device_id"}))
public class PublisherFcmToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", referencedColumnName = "publisher_id", nullable = false)
    private Publisher publisher;

    @Column(name = "device_id", nullable = false, length = 64)
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Platform platform;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "app_version", length = 20)
    private String appVersion;

    @Column(name = "os_version", length = 20)
    private String osVersion;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Builder
    public PublisherFcmToken(Publisher publisher, String deviceId, Platform platform, String token,
                             String appVersion, String osVersion) {
        this.publisher = publisher;
        this.deviceId = deviceId;
        this.platform = platform;
        this.token = token;
        this.appVersion = appVersion;
        this.osVersion = osVersion;
        this.lastSeenAt = LocalDateTime.now();
    }

    public void upsert(String token, String appVersion, String osVersion) {
        this.token = token;
        this.appVersion = appVersion;
        this.osVersion = osVersion;
        this.lastSeenAt = LocalDateTime.now();
    }
}
