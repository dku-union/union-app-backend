package com.union.union.domain.miniapp.dto;

import com.union.union.domain.miniapp.entity.MiniAppCategory;

public record MiniAppCategoryResponseDto(
        Long id,
        String name,
        String displayName,
        String iconUrl
) {
    public static MiniAppCategoryResponseDto from(MiniAppCategory category) {
        return new MiniAppCategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getDisplayName(),
                category.getIconUrl()
        );
    }
}
