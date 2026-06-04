package com.union.union.domain.permission.dto;

/**
 * 미니앱이 선언한 권한 스코프 1개에 대한 현재 사용자의 상태.
 *
 * @param scope       닷-표기 스코프 (예: "device.location")
 * @param hasDecision 사용자가 이 스코프에 대해 결정을 내린 적이 있는지. false 면 iOS 가 프롬프트해야 함.
 * @param granted     허용 여부. {@code hasDecision == false} 이면 default-deny 로 항상 false.
 */
public record PermissionItemDto(
        String scope,
        boolean hasDecision,
        boolean granted
) {
}
