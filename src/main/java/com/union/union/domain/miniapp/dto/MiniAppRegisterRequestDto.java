package com.union.union.domain.miniapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record MiniAppRegisterRequestDto(
    @NotBlank(message = "앱 이름은 필수입니다")
    @Size(max = 100, message = "앱 이름은 100자 이내여야 합니다")
    String name,

    String description,

    @Size(max = 500, message = "아이콘 URL은 500자 이내여야 합니다")
    String iconUrl,

    @NotNull(message = "워크스페이스 ID는 필수입니다")
    UUID workspaceId,

    @NotNull(message = "카테고리 ID는 필수입니다")
    Long categoryId
) {
}
