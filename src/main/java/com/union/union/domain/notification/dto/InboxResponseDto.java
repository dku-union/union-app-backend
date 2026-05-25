package com.union.union.domain.notification.dto;

import com.union.union.domain.notification.entity.DeeplinkType;
import com.union.union.domain.notification.entity.NotificationCategory;
import com.union.union.domain.notification.entity.NotificationInbox;
import com.union.union.domain.notification.entity.SenderType;

import java.time.LocalDateTime;

public record InboxResponseDto(
        Long id,
        Long campaignId,
        SenderType senderType,
        NotificationCategory category,
        String title,
        String body,
        String imageUrl,
        DeeplinkType deeplinkType,
        String targetAppId,
        String targetPath,
        String targetWebUrl,
        String targetInternalRoute,
        boolean read,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
    public static InboxResponseDto from(NotificationInbox inbox) {
        var c = inbox.getCampaign();
        return new InboxResponseDto(
                inbox.getId(),
                c.getId(),
                c.getSenderType(),
                c.getCategory(),
                c.getTitle(),
                c.getBody(),
                c.getImageUrl(),
                c.getDeeplinkType(),
                c.getTargetAppId(),
                c.getTargetPath(),
                c.getTargetWebUrl(),
                c.getTargetInternalRoute(),
                inbox.isRead(),
                inbox.getReadAt(),
                inbox.getCreatedAt()
        );
    }
}
