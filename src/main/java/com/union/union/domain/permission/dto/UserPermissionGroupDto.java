package com.union.union.domain.permission.dto;

import java.util.List;

/**
 * 권한 관리 화면용 — 미니앱 단위로 묶은 사용자의 권한 결정 + 미니앱 메타(name/icon).
 */
public record UserPermissionGroupDto(
        Long miniAppId,
        String appId,
        String miniAppName,
        String iconUrl,
        List<PermissionItemDto> permissions
) {
}
