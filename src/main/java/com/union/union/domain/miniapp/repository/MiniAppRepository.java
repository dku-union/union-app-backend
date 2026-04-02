package com.union.union.domain.miniapp.repository;

import com.union.union.domain.miniapp.entity.MiniApp;
import com.union.union.domain.miniapp.entity.MiniAppCategory;
import com.union.union.domain.miniapp.entity.MiniAppStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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
    @EntityGraph(attributePaths = {"workspace"})
    Optional<MiniApp> findById(Long id);

    @EntityGraph(attributePaths = {"workspace"})
    List<MiniApp> findByStatus(MiniAppStatus status);

    // TODO: 대학교별 필터링 (university 확정 후 구현)
    // List<MiniApp> findByStatusAndUniversityId(MiniAppStatus status, Long universityId);

    @EntityGraph(attributePaths = {"workspace"})
    @Query("SELECT m FROM MiniApp m " +
           "LEFT JOIN MiniAppUsage u ON m.id = u.miniAppId " +
           "WHERE m.status = :status " +
           "GROUP BY m.id " +
           "ORDER BY COUNT(u) DESC")
    List<MiniApp> findPopularMiniApps(@Param("status") MiniAppStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"workspace"})
    List<MiniApp> findByWorkspace_WorkspaceId(UUID workspaceId);

    @EntityGraph(attributePaths = {"workspace"})
    @Query("SELECT m FROM MiniApp m " +
           "JOIN MiniAppUsage u ON m.id = u.miniAppId " +
           "JOIN User user ON u.userId = user.id " +
           "WHERE user.universityName = :universityName " +
           "AND m.status = :status " +
           "GROUP BY m.id " +
           "ORDER BY COUNT(u) DESC")
    List<MiniApp> findRecommendedByUniversity(@Param("universityName") String universityName, @Param("status") MiniAppStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"workspace"})
    @Query("SELECT m FROM MiniApp m " +
           "JOIN MiniAppUsage u ON m.id = u.miniAppId " +
           "WHERE u.userId = :userId " +
           "AND m.status = :status " +
           "GROUP BY m.id " +
           "ORDER BY MAX(u.timestamp) DESC")
    List<MiniApp> findRecentByUser(@Param("userId") UUID userId, @Param("status") MiniAppStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"workspace"})
    @Query("SELECT m FROM MiniApp m " +
           "WHERE m.status = :status " +
           "ORDER BY m.id DESC")
    List<MiniApp> findNewMiniApps(@Param("status") MiniAppStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"workspace"})
    @Query("SELECT m FROM MiniApp m " +
           "WHERE m.status = :status AND (" +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.tags) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<MiniApp> searchByKeyword(@Param("keyword") String keyword, @Param("status") MiniAppStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"workspace"})
    List<MiniApp> findByCategoryAndStatus(MiniAppCategory category, MiniAppStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"workspace"})
    @Query(value = "SELECT * FROM mini_apps WHERE status = 'APPROVED' ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<MiniApp> findRandomMiniApps(@Param("limit") int limit);

}
