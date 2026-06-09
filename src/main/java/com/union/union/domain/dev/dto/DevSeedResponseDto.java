package com.union.union.domain.dev.dto;

import java.util.UUID;

public record DevSeedResponseDto(
        Long publisherId,
        UUID publisherUuid,
        UUID workspaceId,
        Long miniAppId,
        String appId,
        String miniAppName,
        UUID versionId,
        String versionNumber,
        String versionStatus,
        String buildFileUrl,
        String downloadUrl,
        long bundleSize
) {}
