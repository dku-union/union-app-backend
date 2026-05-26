package com.union.union.domain.report.dto;

import com.union.union.domain.report.entity.ReportReason;
import com.union.union.domain.report.entity.ReportTargetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateReportRequestDto(
        @NotNull ReportTargetType targetType,
        Long targetMiniAppId,
        UUID targetReviewId,
        UUID reportedUserId,
        @NotNull ReportReason reason,
        @Size(max = 1000) String detail
) {
}
