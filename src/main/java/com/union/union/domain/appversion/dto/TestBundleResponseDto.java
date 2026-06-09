package com.union.union.domain.appversion.dto;

import java.util.UUID;

public record TestBundleResponseDto(
        UUID versionId,
        Long miniAppId,
        String miniAppName,
        String versionNumber,
        String bundleUrl,
        /** reverse-domain appId (예: com.union.soccer). Bridge `notification` 모듈이 자기 미니앱 식별에 사용. */
        String appId
) {
}
