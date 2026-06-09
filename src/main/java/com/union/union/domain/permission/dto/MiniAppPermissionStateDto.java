package com.union.union.domain.permission.dto;

import java.util.List;

/**
 * 특정 미니앱에 대한 현재 사용자의 권한 상태 (선언 스코프 + 결정).
 * 미니앱 최초 접속 게이트와 권한 관리 화면이 소비한다.
 */
public record MiniAppPermissionStateDto(
        Long miniAppId,
        String appId,
        List<PermissionItemDto> permissions
) {
}
