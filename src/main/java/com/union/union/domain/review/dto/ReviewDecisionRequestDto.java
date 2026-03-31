package com.union.union.domain.review.dto;

import com.union.union.domain.review.entity.Verdict;
import jakarta.validation.constraints.NotNull;

public record ReviewDecisionRequestDto(
    @NotNull(message = "심사 결과는 필수입니다")
    Verdict verdict,

    String reason
) {
}
