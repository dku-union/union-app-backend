package com.union.union.domain.miniapp.dto;

import java.util.List;

public record DiscoveryResponseDto(
        List<MiniAppLiteDto> recentApps,
        List<MiniAppLiteDto> popularApps,
        List<MiniAppLiteDto> newApps,
        List<MiniAppLiteDto> recommendedApps,
        List<String> trendingKeywords,
        List<MiniAppCategoryResponseDto> categories
) {
}
