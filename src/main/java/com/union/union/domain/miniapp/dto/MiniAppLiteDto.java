package com.union.union.domain.miniapp.dto;

import com.union.union.domain.miniapp.entity.MiniAppCategory;

public record MiniAppLiteDto(
        Long id,
        String name,
        String iconUrl,
        String publisherName,
        MiniAppCategory category,
        Double rating
) {
    public static MiniAppLiteDto from(com.union.union.domain.miniapp.entity.MiniApp miniApp) {
        return new MiniAppLiteDto(
                miniApp.getId(),
                miniApp.getName(),
                miniApp.getIconUrl(),
                miniApp.getWorkspace() != null ? miniApp.getWorkspace().getName() : "Union Dev",
                miniApp.getCategory(),
                4.5 // Mock rating for now
        );
    }
}
