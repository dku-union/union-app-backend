package com.union.union.domain.miniapp.dto;

import com.union.union.domain.miniapp.entity.MiniApp;
import com.union.union.domain.miniapp.entity.MiniAppStatus;
import com.union.union.domain.miniapp.entity.PermissionScope;

import java.time.LocalDateTime;
import java.util.List;

public record MiniAppResponseDto(
    Long id,
    String name,
    String description,
    String iconUrl,
    String workspaceName,
    MiniAppStatus status,
    String tags,
    List<PermissionScope> permissions,
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
            miniApp.getTags(),
            miniApp.getPermissions(),
            miniApp.getCreatedAt()
        );
    }
}
