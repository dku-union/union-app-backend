package com.union.union.domain.report.dto;

import com.union.union.domain.report.entity.Report;
import com.union.union.domain.report.entity.ReportReason;
import com.union.union.domain.report.entity.ReportStatus;
import com.union.union.domain.report.entity.ReportTargetType;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminReportResponseDto(
        UUID id,
        UUID reporterId,
        String reporterNickname,
        ReportTargetType targetType,
        Long targetMiniAppId,
        UUID targetReviewId,
        UUID reportedUserId,
        ReportReason reason,
        String detail,
        ReportStatus status,
        String actionTaken,
        UUID reviewedBy,
        LocalDateTime reviewedAt,
        LocalDateTime createdAt
) {
    public static AdminReportResponseDto from(Report report) {
        return new AdminReportResponseDto(
                report.getId(),
                report.getReporter().getId(),
                report.getReporter().getNickname(),
                report.getTargetType(),
                report.getTargetMiniAppId(),
                report.getTargetReviewId(),
                report.getReportedUserId(),
                report.getReason(),
                report.getDetail(),
                report.getStatus(),
                report.getActionTaken(),
                report.getReviewedBy(),
                report.getReviewedAt(),
                report.getCreatedAt()
        );
    }
}
