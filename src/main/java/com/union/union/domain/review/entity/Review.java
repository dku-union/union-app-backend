package com.union.union.domain.review.entity;

import com.union.union.domain.appversion.entity.AppVersion;
import com.union.union.domain.user.entity.User;
import com.union.union.global.common.entity.BaseEntity;
import com.union.union.global.common.exception.BadRequestException;
import com.union.union.global.common.exception.ConflictException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reviews")
@AttributeOverride(name = "createdAt", column = @Column(name = "submitted_at"))
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    private AppVersion version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Verdict verdict;

    @Column(columnDefinition = "TEXT")
    private String reason;

    private LocalDateTime decidedAt;

    @Builder
    public Review(AppVersion version) {
        this.version = version;
        this.verdict = Verdict.PENDING;
    }

    public void decide(User reviewer, Verdict verdict, String reason) {
        if (this.verdict != Verdict.PENDING) {
            throw new ConflictException("이미 처리된 심사입니다");
        }
        if (verdict == Verdict.REJECTED && (reason == null || reason.isBlank())) {
            throw new BadRequestException("거절 시 사유는 필수입니다");
        }
        this.reviewer = reviewer;
        this.verdict = verdict;
        this.reason = reason;
        this.decidedAt = LocalDateTime.now();
    }
}
