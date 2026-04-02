package com.union.union.domain.miniapp.dto;

import com.union.union.domain.miniapp.entity.MiniAppCategory;

import java.util.List;

public record DiscoveryResponseDto(
        List<MiniAppLiteDto> recentApps,
        List<MiniAppLiteDto> popularApps,
        List<MiniAppLiteDto> newApps,
        List<String> trendingKeywords,
        List<MiniAppCategory> categories
) {
}
