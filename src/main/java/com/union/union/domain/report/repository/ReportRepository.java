package com.union.union.domain.report.repository;

import com.union.union.domain.report.entity.Report;
import com.union.union.domain.report.entity.ReportStatus;
import com.union.union.domain.report.entity.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    @Query("""
        SELECT COUNT(r) > 0 FROM Report r
        WHERE r.reporter.id = :reporterId
          AND r.targetType = :targetType
          AND (r.targetMiniAppId = :miniAppId OR :miniAppId IS NULL)
          AND (r.targetReviewId = :reviewId OR :reviewId IS NULL)
          AND (r.reportedUserId = :reportedUserId OR :reportedUserId IS NULL)
          AND r.status IN (com.union.union.domain.report.entity.ReportStatus.PENDING,
                           com.union.union.domain.report.entity.ReportStatus.IN_PROGRESS)
        """)
    boolean existsActiveDuplicate(
            @Param("reporterId") UUID reporterId,
            @Param("targetType") ReportTargetType targetType,
            @Param("miniAppId") Long miniAppId,
            @Param("reviewId") UUID reviewId,
            @Param("reportedUserId") UUID reportedUserId
    );
}
