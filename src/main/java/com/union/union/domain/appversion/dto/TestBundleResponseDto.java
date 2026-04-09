package com.union.union.domain.appversion.dto;

import java.util.UUID;

public record TestBundleResponseDto(
        UUID versionId,
        Long miniAppId,
        String miniAppName,
        String versionNumber,
        String bundleUrl
) {
}
