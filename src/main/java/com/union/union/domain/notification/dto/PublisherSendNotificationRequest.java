package com.union.union.domain.notification.dto;

import com.union.union.domain.notification.entity.DeeplinkType;
import com.union.union.domain.notification.entity.NotificationCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PublisherSendNotificationRequest(
        @NotBlank
        @Size(max = 100, message = "targetAppId 는 최대 100자")
        String targetAppId,

        @NotBlank
        @Size(max = 120, message = "title 은 최대 120자")
        String title,

        @NotBlank
        @Size(max = 500, message = "body 는 최대 500자")
        String body,

        @Size(max = 500)
        String imageUrl,

        @NotNull
        NotificationCategory category,

        DeeplinkType deeplinkType,

        @Size(max = 500)
        String targetPath,

        @Size(max = 500)
        String targetWebUrl,

        @Size(max = 200)
        String targetInternalRoute
) {}
