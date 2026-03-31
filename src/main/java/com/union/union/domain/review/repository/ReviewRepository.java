package com.union.union.domain.review.repository;

import com.union.union.domain.review.entity.Review;
import com.union.union.domain.review.entity.Verdict;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    @EntityGraph(attributePaths = {"version", "version.miniApp", "version.publisher", "reviewer"})
    List<Review> findByVerdictOrderByCreatedAtAsc(Verdict verdict);

    Optional<Review> findByVersionId(UUID versionId);

    boolean existsByVersionIdAndVerdict(UUID versionId, Verdict verdict);

    @EntityGraph(attributePaths = {"version", "version.miniApp", "version.publisher", "reviewer"})
    Optional<Review> findDetailedById(UUID id);
}
