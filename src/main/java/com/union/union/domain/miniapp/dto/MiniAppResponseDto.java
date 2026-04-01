package com.union.union.domain.miniapp.dto;

import com.union.union.domain.miniapp.entity.MiniApp;
import com.union.union.domain.miniapp.entity.MiniAppStatus;

import java.time.LocalDateTime;

public record MiniAppResponseDto(
    Long id,
    String name,
    String description,
    String iconUrl,
    String workspaceName,
    MiniAppStatus status,
    LocalDateTime createdAt
) {
    public static MiniAppResponseDto from(MiniApp miniApp) {
        return new MiniAppResponseDto(
            miniApp.getId(),
            miniApp.getName(),
            miniApp.getDescription(),
            miniApp.getIconUrl(),
            miniApp.getWorkspace().getName(),
            miniApp.getStatus(),
            miniApp.getCreatedAt()
        );
    }
}
