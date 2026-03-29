package com.union.union.domain.miniapp.usage.dto;

public record MiniAppUsageStatsDto(
    Long miniAppId,
    String miniAppName,
    long launchCount
) {
}
