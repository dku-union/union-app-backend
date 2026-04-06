package com.union.union.domain.miniapp.dto;



public record MiniAppLiteDto(
        Long id,
        String name,
        String appId,
        String iconUrl,
        String publisherName,
        MiniAppCategoryResponseDto category,
        Double rating
) {
    public static MiniAppLiteDto from(com.union.union.domain.miniapp.entity.MiniApp miniApp) {
        return new MiniAppLiteDto(
                miniApp.getId(),
                miniApp.getName(),
                miniApp.getAppId(),
                miniApp.getIconUrl(),
                miniApp.getWorkspace() != null ? miniApp.getWorkspace().getName() : "Union Dev",
                miniApp.getCategory() != null ? MiniAppCategoryResponseDto.from(miniApp.getCategory()) : null,
                4.5 // Mock rating for now
        );
    }
}
