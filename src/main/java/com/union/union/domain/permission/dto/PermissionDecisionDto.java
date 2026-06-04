package com.union.union.domain.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 권한 스코프 1개에 대한 사용자의 허용/거부 결정.
 *
 * @param scope   닷-표기 스코프 (예: "user.profile")
 * @param granted true = 허용, false = 거부
 */
public record PermissionDecisionDto(
        @NotBlank(message = "scope는 필수입니다")
        String scope,

        @NotNull(message = "granted는 필수입니다")
        Boolean granted
) {
}
