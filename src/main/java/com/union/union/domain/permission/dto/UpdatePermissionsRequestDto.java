package com.union.union.domain.permission.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 특정 미니앱에 대한 사용자 권한 결정 배치 업서트 요청.
 */
public record UpdatePermissionsRequestDto(
        @NotEmpty(message = "decisions는 비어 있을 수 없습니다")
        @Valid
        List<PermissionDecisionDto> decisions
) {
}
