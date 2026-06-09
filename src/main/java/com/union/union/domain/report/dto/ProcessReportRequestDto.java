package com.union.union.domain.report.dto;

import com.union.union.domain.report.entity.ReportStatus;
import jakarta.validation.constraints.NotNull;

public record ProcessReportRequestDto(
        @NotNull
        ReportStatus status,
        String actionTaken
) {}
