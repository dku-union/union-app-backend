package com.union.union.domain.report.entity;

import com.union.union.domain.user.entity.User;
import com.union.union.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reports", indexes = {
        @Index(name = "idx_reports_reporter", columnList = "reporter_user_id"),
        @Index(name = "idx_reports_status", columnList = "status")
})
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_user_id", nullable = false)
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ReportTargetType targetType;

    @Column(name = "target_mini_app_id")
    private Long targetMiniAppId;

    @Column(name = "target_review_id", columnDefinition = "uuid")
    private UUID targetReviewId;

    @Column(name = "reported_user_id", columnDefinition = "uuid")
    private UUID reportedUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportReason reason;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    @Column(name = "action_taken", length = 100)
    private String actionTaken;

    @Column(name = "reviewed_by", columnDefinition = "uuid")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Builder
    public Report(User reporter, ReportTargetType targetType, Long targetMiniAppId,
                  UUID targetReviewId, UUID reportedUserId, ReportReason reason, String detail) {
        this.reporter = reporter;
        this.targetType = targetType;
        this.targetMiniAppId = targetMiniAppId;
        this.targetReviewId = targetReviewId;
        this.reportedUserId = reportedUserId;
        this.reason = reason;
        this.detail = detail;
        this.status = ReportStatus.PENDING;
        this.actionTaken = "NONE";
    }

    public void process(UUID adminId, ReportStatus newStatus, String actionTaken) {
        this.status = newStatus;
        this.reviewedBy = adminId;
        this.reviewedAt = java.time.LocalDateTime.now();
        if (actionTaken != null) this.actionTaken = actionTaken;
    }
}
