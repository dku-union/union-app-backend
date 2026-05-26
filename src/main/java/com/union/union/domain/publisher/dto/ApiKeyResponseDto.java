package com.union.union.domain.publisher.dto;

import com.union.union.domain.publisher.entity.ApiKey;

import java.time.LocalDateTime;

public record ApiKeyResponseDto(
        Long id,
        String keyPrefix,
        String name,
        String scopes,
        LocalDateTime createdAt,
        LocalDateTime lastUsedAt,
        LocalDateTime revokedAt
) {
    public static ApiKeyResponseDto from(ApiKey k) {
        return new ApiKeyResponseDto(
                k.getId(),
                k.getKeyPrefix(),
                k.getName(),
                k.getScopes(),
                k.getCreatedAt(),
                k.getLastUsedAt(),
                k.getRevokedAt()
        );
    }
}
