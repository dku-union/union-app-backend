package com.union.union.domain.miniapp.repository;

import com.union.union.domain.miniapp.entity.MiniApp;
import com.union.union.domain.miniapp.entity.MiniAppStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MiniAppRepository extends JpaRepository<MiniApp, Long> {
    
    @Query("SELECT m FROM MiniApp m WHERE m.status = :status AND (:universityId IS NULL OR m.university.id = :universityId)")
    List<MiniApp> findByStatusAndUniversityId(@Param("status") MiniAppStatus status, @Param("universityId") Long universityId);

    @Query("SELECT m FROM MiniApp m " +
           "LEFT JOIN MiniAppUsage u ON m.id = u.miniAppId " +
           "WHERE m.status = com.union.union.domain.miniapp.entity.MiniAppStatus.APPROVED " +
           "GROUP BY m.id " +
           "ORDER BY COUNT(u) DESC")
    List<MiniApp> findPopularMiniApps(Pageable pageable);

    List<MiniApp> findByPublisherId(UUID publisherId);

    @Query("SELECT m FROM MiniApp m " +
           "JOIN MiniAppUsage u ON m.id = u.miniAppId " +
           "JOIN User user ON u.userId = user.id " +
           "WHERE user.universityName = :universityName " +
           "AND m.status = 'APPROVED' " +
           "GROUP BY m.id " +
           "ORDER BY COUNT(u) DESC")
    List<MiniApp> findRecommendedByUniversity(@Param("universityName") String universityName, Pageable pageable);

    @Query("SELECT m FROM MiniApp m " +
           "JOIN MiniAppUsage u ON m.id = u.miniAppId " +
           "WHERE u.userId = :userId " +
           "AND m.status = 'APPROVED' " +
           "GROUP BY m.id " +
           "ORDER BY MAX(u.timestamp) DESC")
    List<MiniApp> findRecentByUser(@Param("userId") UUID userId, Pageable pageable);
}
