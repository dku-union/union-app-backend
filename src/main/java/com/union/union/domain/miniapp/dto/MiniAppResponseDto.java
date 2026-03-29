package com.union.union.domain.miniapp.dto;

import com.union.union.domain.miniapp.entity.MiniApp;
import com.union.union.domain.miniapp.entity.MiniAppStatus;

import java.time.LocalDateTime;

public record MiniAppResponseDto(
    Long id,
    String name,
    String description,
    String iconUrl,
    String launchUrl,
    String publisherNickname,
    String universityName,
    MiniAppStatus status,
    LocalDateTime createdAt
) {
    public static MiniAppResponseDto from(MiniApp miniApp) {
        return new MiniAppResponseDto(
            miniApp.getId(),
            miniApp.getName(),
            miniApp.getDescription(),
            miniApp.getIconUrl(),
            miniApp.getLaunchUrl(),
            miniApp.getPublisher().getNickname(),
            miniApp.getUniversity() != null ? miniApp.getUniversity().getUniversityName() : null,
            miniApp.getStatus(),
            miniApp.getCreatedAt()
        );
    }
}
