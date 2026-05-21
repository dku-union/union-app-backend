package com.union.union.domain.miniapp.dto;

import com.union.union.domain.miniapp.entity.PermissionScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record MiniAppRegisterRequestDto(
    @NotBlank(message = "앱 이름은 필수입니다")
    @Size(max = 100, message = "앱 이름은 100자 이내여야 합니다")
    String name,

    @Size(max = 2000)
    String description,

    @Size(max = 500, message = "아이콘 URL은 500자 이내여야 합니다")
    String iconUrl,

    @NotNull(message = "워크스페이스 ID는 필수입니다")
    UUID workspaceId,

    @NotNull(message = "카테고리 ID는 필수입니다")
    Long categoryId,

    String appId,

    @Size(max = 10, message = "키워드는 최대 10개까지 입력 가능합니다")
    List<String> keywords,

    @Size(max = 8, message = "권한 스코프는 최대 8개까지 입력 가능합니다")
    List<PermissionScope> permissions
) {
}
