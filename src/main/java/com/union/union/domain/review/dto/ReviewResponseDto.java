package com.union.union.domain.review.dto;

import com.union.union.domain.review.entity.Review;
import com.union.union.domain.review.entity.Verdict;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponseDto(
    UUID id,
    UUID versionId,
    String versionNumber,
    String miniAppName,
    String publisherNickname,
    String reviewerNickname,
    Verdict verdict,
    String reason,
    LocalDateTime submittedAt,
    LocalDateTime decidedAt
) {
    public static ReviewResponseDto from(Review review) {
        return new ReviewResponseDto(
            review.getId(),
            review.getVersion().getId(),
            review.getVersion().getVersionNumber(),
            review.getVersion().getMiniApp().getName(),
            review.getVersion().getPublisher().getNickname(),
            review.getReviewer() != null ? review.getReviewer().getNickname() : null,
            review.getVerdict(),
            review.getReason(),
            review.getCreatedAt(),
            review.getDecidedAt()
        );
    }
}
