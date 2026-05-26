package com.union.union.domain.publisher.dto;

import java.time.LocalDateTime;

public record IssueApiKeyResponseDto(
        Long id,
        String rawKey,
        String keyPrefix,
        String name,
        String scopes,
        LocalDateTime createdAt
) {}
