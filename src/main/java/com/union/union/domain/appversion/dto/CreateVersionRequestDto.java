package com.union.union.domain.appversion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateVersionRequestDto(
    @NotNull(message = "miniAppId는 필수입니다")
    Long miniAppId,

    @NotBlank(message = "버전 번호는 필수입니다")
    @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+$", message = "버전 번호는 semver 형식이어야 합니다 (예: 1.0.0)")
    String versionNumber,

    String releaseNotes
) {
}
