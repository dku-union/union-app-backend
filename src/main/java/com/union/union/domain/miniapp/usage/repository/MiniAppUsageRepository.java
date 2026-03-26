package com.union.union.domain.miniapp.usage.repository;

import com.union.union.domain.miniapp.usage.dto.MiniAppUsageStatsDto;
import com.union.union.domain.miniapp.usage.entity.MiniAppUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MiniAppUsageRepository extends JpaRepository<MiniAppUsage, Long> {

    @Query("SELECT new com.union.union.domain.miniapp.usage.dto.MiniAppUsageStatsDto(m.id, m.name, COUNT(u)) " +
           "FROM MiniApp m " +
           "LEFT JOIN MiniAppUsage u ON m.id = u.miniAppId " +
           "WHERE m.publisher.id = :publisherId " +
           "AND (u.timestamp IS NULL OR u.timestamp >= :startDate) " +
           "GROUP BY m.id, m.name")
    List<MiniAppUsageStatsDto> findUsageStatsByPublisher(
            @Param("publisherId") UUID publisherId, 
            @Param("startDate") LocalDateTime startDate
    );

    @Query("SELECT COUNT(u) " +
           "FROM MiniApp m " +
           "JOIN MiniAppUsage u ON m.id = u.miniAppId " +
           "WHERE m.publisher.id = :publisherId " +
           "AND u.timestamp >= :startDate")
    long countUsageByPublisher(
            @Param("publisherId") UUID publisherId, 
            @Param("startDate") LocalDateTime startDate
    );
}
