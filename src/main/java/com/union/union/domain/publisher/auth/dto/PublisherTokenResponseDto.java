package com.union.union.domain.publisher.auth.dto;

import java.util.UUID;

public record PublisherTokenResponseDto(
        String accessToken,
        String refreshToken,
        UUID publisherId,
        String name,
        String role
) {}
