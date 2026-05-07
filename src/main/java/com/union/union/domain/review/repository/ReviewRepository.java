package com.union.union.domain.review.repository;

import com.union.union.domain.review.entity.Review;
import com.union.union.domain.review.entity.Verdict;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    @EntityGraph(attributePaths = {"version", "version.miniApp", "version.miniApp.workspace", "reviewer"})
    List<Review> findByVerdictOrderByCreatedAtAsc(Verdict verdict);

    Optional<Review> findByVersionId(UUID versionId);

    @EntityGraph(attributePaths = {"version", "version.miniApp", "version.miniApp.workspace", "reviewer"})
    Optional<Review> findDetailedByVersionIdAndVerdict(UUID versionId, Verdict verdict);

    boolean existsByVersionIdAndVerdict(UUID versionId, Verdict verdict);

    @EntityGraph(attributePaths = {"version", "version.miniApp", "version.miniApp.workspace", "reviewer"})
    Optional<Review> findDetailedById(UUID id);

    /**
     * publisher 가 멤버인 모든 워크스페이스의 모든 심사 이력. dashboard "심사 현황" 페이지용.
     * BaseEntity 의 createdAt 은 reviews 테이블에서 submitted_at 컬럼으로 매핑됨.
     */
    @Query("SELECT DISTINCT r FROM Review r " +
           "JOIN FETCH r.version v " +
           "JOIN FETCH v.miniApp ma " +
           "JOIN FETCH ma.workspace w " +
           "LEFT JOIN FETCH w.owner " +
           "LEFT JOIN FETCH r.reviewer " +
           "JOIN WorkspaceMember wm ON wm.workspace.workspaceId = w.workspaceId " +
           "WHERE wm.publisher.publisherId = :publisherId " +
           "ORDER BY r.createdAt DESC")
    List<Review> findAllByPublisherMembership(@Param("publisherId") UUID publisherId);
}
