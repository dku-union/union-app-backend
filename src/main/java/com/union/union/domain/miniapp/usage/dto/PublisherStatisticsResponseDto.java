package com.union.union.domain.miniapp.usage.dto;

import java.util.List;

public record PublisherStatisticsResponseDto(
    long totalLaunchCount,
    List<MiniAppUsageStatsDto> appStats
) {
}
