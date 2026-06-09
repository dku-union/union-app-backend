package com.union.union.domain.notification.dto;

import com.union.union.domain.notification.entity.DeeplinkType;
import com.union.union.domain.notification.entity.NotificationCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendNotificationRequestDto(
        @NotBlank String title,
        @NotBlank String body,
        String imageUrl,
        @NotNull NotificationCategory category,
        DeeplinkType deeplinkType,
        String targetAppId,
        String targetPath,
        String targetWebUrl,
        String targetInternalRoute
) {
}
