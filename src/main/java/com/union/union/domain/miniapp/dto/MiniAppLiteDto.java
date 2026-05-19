package com.union.union.domain.miniapp.dto;

import java.time.LocalDateTime;

public record MiniAppLiteDto(
        Long id,
        String name,
        String appId,
        String iconUrl,
        String publisherName,
        MiniAppCategoryResponseDto category,
        Double rating,
        String description,
        LocalDateTime createdAt
) {
    public static MiniAppLiteDto from(com.union.union.domain.miniapp.entity.MiniApp miniApp) {
        return new MiniAppLiteDto(
                miniApp.getId(),
                miniApp.getName(),
                miniApp.getAppId(),
                miniApp.getIconUrl(),
                miniApp.getWorkspace() != null ? miniApp.getWorkspace().getName() : "Union Dev",
                miniApp.getCategory() != null ? MiniAppCategoryResponseDto.from(miniApp.getCategory()) : null,
                null, // rating: 별점 도메인 미구현 (추후 user_app_ratings 테이블 도입 시 채움)
                miniApp.getDescription(),
                miniApp.getCreatedAt()
        );
    }
}
