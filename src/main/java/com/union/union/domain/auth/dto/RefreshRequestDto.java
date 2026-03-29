package com.union.union.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequestDto(
    @NotBlank(message = "Refresh token은 필수입니다")
    String refreshToken
) {
}
