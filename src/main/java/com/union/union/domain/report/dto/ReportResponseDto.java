package com.union.union.domain.report.dto;

import com.union.union.domain.report.entity.Report;
import com.union.union.domain.report.entity.ReportStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReportResponseDto(
        UUID id,
        ReportStatus status,
        LocalDateTime createdAt
) {
    public static ReportResponseDto from(Report report) {
        return new ReportResponseDto(report.getId(), report.getStatus(), report.getCreatedAt());
    }
}
