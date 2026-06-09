package com.union.union.domain.notification.dto;

public record PublisherSendNotificationResponse(
        Long campaignId,
        long sentTokenCount,
        long subscriberCount
) {}
