package com.union.union.domain.appversion.dto;

import com.union.union.domain.appversion.entity.AppVersion;
import com.union.union.domain.appversion.entity.VersionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppVersionResponseDto(
    UUID id,
    Long miniAppId,
    String miniAppName,
    String publisherNickname,
    String versionNumber,
    String releaseNotes,
    VersionStatus status,
    String buildFileUrl,
    Long bundleSize,
    LocalDateTime testedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static AppVersionResponseDto from(AppVersion version) {
        return new AppVersionResponseDto(
            version.getId(),
            version.getMiniApp().getId(),
            version.getMiniApp().getName(),
            version.getPublisher().getNickname(),
            version.getVersionNumber(),
            version.getReleaseNotes(),
            version.getStatus(),
            version.getBuildFileUrl(),
            version.getBundleSize(),
            version.getTestedAt(),
            version.getCreatedAt(),
            version.getUpdatedAt()
        );
    }
}
