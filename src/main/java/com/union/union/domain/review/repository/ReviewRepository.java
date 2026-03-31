package com.union.union.domain.review.repository;

import com.union.union.domain.review.entity.Review;
import com.union.union.domain.review.entity.Verdict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByVerdictOrderByCreatedAtAsc(Verdict verdict);

    Optional<Review> findByVersionId(UUID versionId);
}
