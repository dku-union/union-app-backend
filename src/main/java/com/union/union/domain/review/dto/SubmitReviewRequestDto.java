package com.union.union.domain.review.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SubmitReviewRequestDto(
    @NotNull(message = "versionId는 필수입니다")
    UUID versionId
) {
}
