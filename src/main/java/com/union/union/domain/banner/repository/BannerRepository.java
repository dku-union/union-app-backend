package com.union.union.domain.banner.repository;

import com.union.union.domain.banner.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {

    /**
     * 현재 시각 기준 활성 + 노출 윈도우 내 배너 목록 (sort_order 오름차순, created_at 내림차순).
     * start_at NULL = 즉시 노출 시작, end_at NULL = 무기한.
     */
    @Query("""
        SELECT b FROM Banner b
        WHERE b.isActive = true
          AND (b.startAt IS NULL OR b.startAt <= :now)
          AND (b.endAt   IS NULL OR b.endAt   >= :now)
        ORDER BY b.sortOrder ASC, b.createdAt DESC
    """)
    List<Banner> findActiveBanners(@Param("now") LocalDateTime now);
}
