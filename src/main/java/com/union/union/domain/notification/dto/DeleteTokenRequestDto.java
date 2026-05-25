package com.union.union.domain.notification.dto;

public record DeleteTokenRequestDto(
        String token,
        String deviceId
) {
}
