package com.union.union.domain.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record FcmTokenRequestDto(
        @NotBlank String token
) {}
