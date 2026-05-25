package com.union.union.domain.notification.entity;

import com.union.union.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification_campaigns")
public class NotificationCampaign extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false, length = 20)
    private SenderType senderType;

    @Column(name = "sender_miniapp_id")
    private Long senderMiniappId;

    @Column(name = "sender_publisher_id", columnDefinition = "uuid")
    private java.util.UUID senderPublisherId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationCategory category;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 500)
    private String body;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "deeplink_type", nullable = false, length = 10)
    private DeeplinkType deeplinkType;

    @Column(name = "target_app_id", length = 100)
    private String targetAppId;

    @Column(name = "target_path", length = 500)
    private String targetPath;

    @Column(name = "target_web_url", length = 500)
    private String targetWebUrl;

    @Column(name = "target_internal_route", length = 200)
    private String targetInternalRoute;

    @Builder
    public NotificationCampaign(SenderType senderType, Long senderMiniappId,
                                java.util.UUID senderPublisherId, NotificationCategory category,
                                String title, String body, String imageUrl,
                                DeeplinkType deeplinkType, String targetAppId,
                                String targetPath, String targetWebUrl, String targetInternalRoute) {
        this.senderType = senderType;
        this.senderMiniappId = senderMiniappId;
        this.senderPublisherId = senderPublisherId;
        this.category = category;
        this.title = title;
        this.body = body;
        this.imageUrl = imageUrl;
        this.deeplinkType = deeplinkType != null ? deeplinkType : DeeplinkType.NONE;
        this.targetAppId = targetAppId;
        this.targetPath = targetPath;
        this.targetWebUrl = targetWebUrl;
        this.targetInternalRoute = targetInternalRoute;
    }
}
