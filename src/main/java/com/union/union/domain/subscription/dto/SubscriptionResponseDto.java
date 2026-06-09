package com.union.union.domain.subscription.dto;

import com.union.union.domain.subscription.entity.MiniAppSubscription;

import java.time.LocalDateTime;

public record SubscriptionResponseDto(
        Long id,
        Long miniAppId,
        String appId,
        String miniAppName,
        String iconUrl,
        boolean pushEnabled,
        LocalDateTime subscribedAt
) {
    public static SubscriptionResponseDto from(MiniAppSubscription s) {
        return new SubscriptionResponseDto(
                s.getId(),
                s.getMiniApp().getId(),
                s.getMiniApp().getAppId(),
                s.getMiniApp().getName(),
                s.getMiniApp().getIconUrl(),
                s.isPushEnabled(),
                s.getSubscribedAt()
        );
    }
}
