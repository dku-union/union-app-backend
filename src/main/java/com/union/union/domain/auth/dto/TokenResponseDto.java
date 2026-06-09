package com.union.union.domain.auth.dto;

public record TokenResponseDto(
    String accessToken,
    String refreshToken
) {
}
