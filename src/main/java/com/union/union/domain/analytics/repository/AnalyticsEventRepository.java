package com.union.union.domain.analytics.repository;

import com.union.union.domain.analytics.entity.AnalyticsEvent;
import com.union.union.domain.analytics.repository.projection.DailyCount;
import com.union.union.domain.analytics.repository.projection.EventNameCount;
import com.union.union.domain.analytics.repository.projection.PerformanceMetricStat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, UUID> {

    // ──────────────────────────────────────────────────────────────
    // 기본 카운트 (JPQL — 인덱스 활용)
    // ──────────────────────────────────────────────────────────────

    long countByAppIdAndClientTimestampBetween(String appId, Instant from, Instant to);

    @Query("SELECT COUNT(DISTINCT e.sessionId) FROM AnalyticsEvent e " +
           "WHERE e.appId = :appId AND e.clientTimestamp BETWEEN :from AND :to")
    long countDistinctSessions(
        @Param("appId") String appId,
        @Param("from") Instant from,
        @Param("to") Instant to
    );

    @Query("SELECT COUNT(DISTINCT e.hashedUserId) FROM AnalyticsEvent e " +
           "WHERE e.appId = :appId AND e.clientTimestamp BETWEEN :from AND :to " +
           "AND e.hashedUserId IS NOT NULL")
    long countDistinctUsers(
        @Param("appId") String appId,
        @Param("from") Instant from,
        @Param("to") Instant to
    );

    // ──────────────────────────────────────────────────────────────
    // 이벤트 타입별 카운트
    // ──────────────────────────────────────────────────────────────

    /**
     * 이벤트 타입별 카운트 집계.
     * alias: {@code name} = eventType, {@code count} = COUNT(*)
     */
    @Query(nativeQuery = true, value = """
        SELECT event_type  AS name,
               COUNT(*)    AS count
        FROM   analytics_events
        WHERE  app_id           = :appId
          AND  client_timestamp BETWEEN :from AND :to
        GROUP  BY event_type
        ORDER  BY count DESC
        """)
    List<EventNameCount> countByEventType(
        @Param("appId") String appId,
        @Param("from") Instant from,
        @Param("to") Instant to
    );

    // ──────────────────────────────────────────────────────────────
    // 콘텐츠 분석
    // ──────────────────────────────────────────────────────────────

    /**
     * 상위 화면 뷰 목록.
     * {@code params} 컬럼을 jsonb 로 캐스팅하여 pageName 추출.
     */
    @Query(nativeQuery = true, value = """
        SELECT params::jsonb->>'pageName' AS name,
               COUNT(*)                  AS count
        FROM   analytics_events
        WHERE  app_id           = :appId
          AND  event_type       = 'screen'
          AND  event_name       = 'screen_view'
          AND  client_timestamp BETWEEN :from AND :to
          AND  params           IS NOT NULL
        GROUP  BY params::jsonb->>'pageName'
        ORDER  BY count DESC
        LIMIT  :limit
        """)
    List<EventNameCount> findTopScreenViews(
        @Param("appId") String appId,
        @Param("from") Instant from,
        @Param("to") Instant to,
        @Param("limit") int limit
    );

    /**
     * 상위 커스텀 이벤트 목록.
     */
    @Query(nativeQuery = true, value = """
        SELECT event_name AS name,
               COUNT(*)   AS count
        FROM   analytics_events
        WHERE  app_id           = :appId
          AND  event_type       = 'custom'
          AND  client_timestamp BETWEEN :from AND :to
        GROUP  BY event_name
        ORDER  BY count DESC
        LIMIT  :limit
        """)
    List<EventNameCount> findTopCustomEvents(
        @Param("appId") String appId,
        @Param("from") Instant from,
        @Param("to") Instant to,
        @Param("limit") int limit
    );

    // ──────────────────────────────────────────────────────────────
    // 에러 분석
    // ──────────────────────────────────────────────────────────────

    long countByAppIdAndEventTypeAndClientTimestampBetween(
        String appId, String eventType, Instant from, Instant to
    );

    /**
     * 상위 에러 이벤트 목록.
     */
    @Query(nativeQuery = true, value = """
        SELECT event_name AS name,
               COUNT(*)   AS count
        FROM   analytics_events
        WHERE  app_id           = :appId
          AND  event_type       = 'error'
          AND  client_timestamp BETWEEN :from AND :to
        GROUP  BY event_name
        ORDER  BY count DESC
        LIMIT  :limit
        """)
    List<EventNameCount> findTopErrors(
        @Param("appId") String appId,
        @Param("from") Instant from,
        @Param("to") Instant to,
        @Param("limit") int limit
    );

    // ──────────────────────────────────────────────────────────────
    // 전환 분석
    // ──────────────────────────────────────────────────────────────

    /**
     * 상위 전환 타입 목록.
     * conversionType 은 params::jsonb->>'conversionType' 에서 추출.
     */
    @Query(nativeQuery = true, value = """
        SELECT params::jsonb->>'conversionType' AS name,
               COUNT(*)                         AS count
        FROM   analytics_events
        WHERE  app_id           = :appId
          AND  event_type       = 'conversion'
          AND  client_timestamp BETWEEN :from AND :to
          AND  params           IS NOT NULL
        GROUP  BY params::jsonb->>'conversionType'
        ORDER  BY count DESC
        LIMIT  :limit
        """)
    List<EventNameCount> findTopConversions(
        @Param("appId") String appId,
        @Param("from") Instant from,
        @Param("to") Instant to,
        @Param("limit") int limit
    );

    // ──────────────────────────────────────────────────────────────
    // 성능 지표 (percentile_cont)
    // ──────────────────────────────────────────────────────────────

    /**
     * 성능 지표별 p50, p95, 평균, 샘플 수 집계.
     *
     * <p>대상 지표: first_contentful_paint, largest_contentful_paint,
     * page_load, dom_content_loaded, bridge_latency
     *
     * <p>alias: metricName, p50, p95, avgValue, sampleCount
     */
    @Query(nativeQuery = true, value = """
        SELECT
            params::jsonb->>'metricName'                                                                   AS "metricName",
            PERCENTILE_CONT(0.5)  WITHIN GROUP (ORDER BY (params::jsonb->>'value')::float8)                AS p50,
            PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY (params::jsonb->>'value')::float8)                AS p95,
            AVG((params::jsonb->>'value')::float8)                                                         AS "avgValue",
            COUNT(*)                                                                                        AS "sampleCount"
        FROM  analytics_events
        WHERE app_id           = :appId
          AND event_type       = 'performance'
          AND event_name       = 'performance_metric'
          AND client_timestamp BETWEEN :from AND :to
          AND params           IS NOT NULL
          AND params::jsonb->>'metricName' IN (
                'first_contentful_paint',
                'largest_contentful_paint',
                'page_load',
                'dom_content_loaded',
                'bridge_latency'
              )
        GROUP BY params::jsonb->>'metricName'
        """)
    List<PerformanceMetricStat> findPerformanceStats(
        @Param("appId") String appId,
        @Param("from") Instant from,
        @Param("to") Instant to
    );

    // ──────────────────────────────────────────────────────────────
    // 일별 추이
    // ──────────────────────────────────────────────────────────────

    /**
     * 날짜별 이벤트 카운트 (KST 기준).
     * alias: eventDate (LocalDate), count (Long)
     */
    @Query(nativeQuery = true, value = """
        SELECT
            DATE(client_timestamp AT TIME ZONE 'Asia/Seoul')  AS "eventDate",
            COUNT(*)                                          AS count
        FROM  analytics_events
        WHERE app_id           = :appId
          AND client_timestamp BETWEEN :from AND :to
        GROUP BY DATE(client_timestamp AT TIME ZONE 'Asia/Seoul')
        ORDER BY "eventDate"
        """)
    List<DailyCount> findDailyTrend(
        @Param("appId") String appId,
        @Param("from") Instant from,
        @Param("to") Instant to
    );

    // ──────────────────────────────────────────────────────────────
    // 이벤트 목록 (페이징)
    // ──────────────────────────────────────────────────────────────

    Page<AnalyticsEvent> findByAppIdAndClientTimestampBetween(
        String appId, Instant from, Instant to, Pageable pageable
    );

    Page<AnalyticsEvent> findByAppIdAndEventTypeAndClientTimestampBetween(
        String appId, String eventType, Instant from, Instant to, Pageable pageable
    );
}
