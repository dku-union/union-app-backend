package com.union.union.domain.notification.dto;

import com.union.union.domain.notification.entity.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterTokenRequestDto(
        @NotBlank String deviceId,
        @NotNull Platform platform,
        @NotBlank String token,
        String appVersion,
        String osVersion
) {
}
