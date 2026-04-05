package com.union.union.domain.miniapp.repository;

import com.union.union.domain.miniapp.entity.MiniApp;
import com.union.union.domain.miniapp.entity.MiniAppCategory;
import com.union.union.domain.miniapp.entity.MiniAppStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MiniAppRepository extends JpaRepository<MiniApp, Long> {

    @Override
    @Query("SELECT m FROM MiniApp m JOIN FETCH m.workspace WHERE m.id = :id")
    Optional<MiniApp> findById(@Param("id") Long id);

    @Query("SELECT m FROM MiniApp m JOIN FETCH m.workspace WHERE m.status = :status")
    List<MiniApp> findByStatus(@Param("status") MiniAppStatus status);

    @Query("SELECT m FROM MiniApp m JOIN FETCH m.workspace " +
           "WHERE m.status = :status AND m.id IN (" +
           "  SELECT u.miniAppId FROM MiniAppUsage u GROUP BY u.miniAppId ORDER BY COUNT(u) DESC" +
           ") ORDER BY (" +
           "  SELECT COUNT(u2) FROM MiniAppUsage u2 WHERE u2.miniAppId = m.id" +
           ") DESC")
    List<MiniApp> findPopularMiniApps(@Param("status") MiniAppStatus status, Pageable pageable);

    @Query("SELECT m FROM MiniApp m JOIN FETCH m.workspace WHERE m.workspace.workspaceId = :workspaceId")
    List<MiniApp> findByWorkspace_WorkspaceId(@Param("workspaceId") UUID workspaceId);

    @Query("SELECT m FROM MiniApp m JOIN FETCH m.workspace " +
           "WHERE m.status = :status AND m.id IN (" +
           "  SELECT u.miniAppId FROM MiniAppUsage u " +
           "  JOIN User user ON u.userId = user.id " +
           "  WHERE user.universityName = :universityName " +
           "  GROUP BY u.miniAppId ORDER BY COUNT(u) DESC" +
           ")")
    List<MiniApp> findRecommendedByUniversity(@Param("universityName") String universityName, @Param("status") MiniAppStatus status, Pageable pageable);

    @Query("SELECT m FROM MiniApp m JOIN FETCH m.workspace " +
           "WHERE m.status = :status AND m.id IN (" +
           "  SELECT u.miniAppId FROM MiniAppUsage u " +
           "  WHERE u.userId = :userId " +
           "  GROUP BY u.miniAppId" +
           ") ORDER BY (" +
           "  SELECT MAX(u2.timestamp) FROM MiniAppUsage u2 WHERE u2.miniAppId = m.id AND u2.userId = :userId" +
           ") DESC")
    List<MiniApp> findRecentByUser(@Param("userId") UUID userId, @Param("status") MiniAppStatus status, Pageable pageable);

    @Query("SELECT m FROM MiniApp m JOIN FETCH m.workspace " +
           "WHERE m.status = :status " +
           "ORDER BY m.id DESC")
    List<MiniApp> findNewMiniApps(@Param("status") MiniAppStatus status, Pageable pageable);

    @Query("SELECT m FROM MiniApp m JOIN FETCH m.workspace " +
           "WHERE m.status = :status AND (" +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.tags) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<MiniApp> searchByKeyword(@Param("keyword") String keyword, @Param("status") MiniAppStatus status, Pageable pageable);

    @Query("SELECT m FROM MiniApp m JOIN FETCH m.workspace WHERE m.category = :category AND m.status = :status")
    List<MiniApp> findByCategoryAndStatus(@Param("category") MiniAppCategory category, @Param("status") MiniAppStatus status, Pageable pageable);

    @Query("SELECT m FROM MiniApp m JOIN FETCH m.workspace WHERE m.status = 'APPROVED' ORDER BY FUNCTION('RANDOM')")
    List<MiniApp> findRandomMiniApps(Pageable pageable);
}
