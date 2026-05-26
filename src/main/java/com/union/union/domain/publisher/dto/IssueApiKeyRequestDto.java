package com.union.union.domain.publisher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IssueApiKeyRequestDto(
        @NotBlank
        @Size(max = 100, message = "name 은 최대 100자")
        String name
) {}
