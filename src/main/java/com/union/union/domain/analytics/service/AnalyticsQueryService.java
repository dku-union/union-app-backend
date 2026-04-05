package com.union.union.domain.analytics.service;

import com.union.union.domain.analytics.dto.response.*;
import com.union.union.domain.analytics.repository.AnalyticsEventRepository;
import com.union.union.domain.analytics.repository.projection.EventNameCount;
import com.union.union.domain.analytics.repository.projection.PerformanceMetricStat;
import com.union.union.domain.miniapp.entity.MiniApp;
import com.union.union.domain.miniapp.entity.MiniAppStatus;
import com.union.union.domain.miniapp.repository.MiniAppRepository;
import com.union.union.domain.workspace.service.WorkspaceAuthorizationService;
import com.union.union.global.common.exception.EntityNotFoundException;
import com.union.union.global.security.jwt.JwtUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Analytics 조회 서비스 (퍼블리셔 대시보드용).
 *
 * <h3>캐싱 전략</h3>
 * <ul>
 *   <li>{@code analyticsSummary} — 5분 TTL. 집계 쿼리 부하 방지</li>
 * </ul>
 *
 * <h3>인가 (Authorization)</h3>
 * <ul>
 *   <li>ROLE_ADMIN: 모든 앱 조회 가능</li>
 *   <li>ROLE_PUBLISHER: 자신의 워크스페이스 미니앱만 조회 가능</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsQueryService {

    private static final ZoneId KST          = ZoneId.of("Asia/Seoul");
    private static final int    TOP_LIMIT     = 10;

    private final AnalyticsEventRepository      eventRepository;
    private final MiniAppRepository             miniAppRepository;
    private final WorkspaceAuthorizationService workspaceAuthorizationService;

    // ──────────────────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * 미니앱 Analytics 요약 조회.
     * 캐시 키: {@code analyticsSummary::{appId}_{from}_{to}}
     *
     * @throws EntityNotFoundException appId 에 해당하는 앱이 없을 때
     * @throws com.union.union.global.common.exception.UnauthorizedAccessException
     *         퍼블리셔가 해당 앱 워크스페이스 멤버가 아닐 때
     */
    @Cacheable(
        value    = "analyticsSummary",
        key      = "#appId + '_' + #from + '_' + #to",
        condition = "#principal.role() != 'ROLE_ADMIN'"   // Admin 은 캐시 바이패스 (실시간 필요)
    )
    public AppAnalyticsSummaryDto getSummary(
        String appId,
        LocalDate from,
        LocalDate to,
        JwtUserPrincipal principal
    ) {
        authorizeAccess(appId, principal);

        Instant fromInstant = from.atStartOfDay(KST).toInstant();
        Instant toInstant   = to.plusDays(1).atStartOfDay(KST).toInstant();

        // 1. 기본 지표
        long totalEvents    = eventRepository.countByAppIdAndClientTimestampBetween(appId, fromInstant, toInstant);
        long uniqueSessions = eventRepository.countDistinctSessions(appId, fromInstant, toInstant);
        long uniqueUsers    = eventRepository.countDistinctUsers(appId, fromInstant, toInstant);

        // 2. 이벤트 타입별 카운트
        Map<String, Long> eventsByType = eventRepository.countByEventType(appId, fromInstant, toInstant)
            .stream()
            .collect(Collectors.toMap(
                EventNameCount::getName,
                EventNameCount::getCount,
                (a, b) -> a,
                LinkedHashMap::new
            ));

        // 3. 콘텐츠 분석
        List<EventCountDto> topScreenViews   = toEventCountDtos(eventRepository.findTopScreenViews(appId, fromInstant, toInstant, TOP_LIMIT));
        List<EventCountDto> topCustomEvents  = toEventCountDtos(eventRepository.findTopCustomEvents(appId, fromInstant, toInstant, TOP_LIMIT));

        // 4. 에러 분석
        long totalErrors = eventsByType.getOrDefault("error", 0L);
        double errorRate = totalEvents > 0 ? (double) totalErrors / totalEvents : 0.0;
        List<EventCountDto> topErrors = toEventCountDtos(eventRepository.findTopErrors(appId, fromInstant, toInstant, TOP_LIMIT));

        // 5. 전환 분석
        long totalConversions = eventsByType.getOrDefault("conversion", 0L);
        List<EventCountDto> topConversions = toEventCountDtos(eventRepository.findTopConversions(appId, fromInstant, toInstant, TOP_LIMIT));

        // 6. 성능 지표
        Map<String, PerformanceStatDto> performance = eventRepository
            .findPerformanceStats(appId, fromInstant, toInstant)
            .stream()
            .collect(Collectors.toMap(
                PerformanceMetricStat::getMetricName,
                PerformanceStatDto::from,
                (a, b) -> a,
                LinkedHashMap::new
            ));

        // 7. 일별 추이
        List<DailyEventCountDto> dailyTrend = eventRepository
            .findDailyTrend(appId, fromInstant, toInstant)
            .stream()
            .map(DailyEventCountDto::from)
            .toList();

        return new AppAnalyticsSummaryDto(
            appId, from, to,
            new AppAnalyticsSummaryDto.OverviewDto(totalEvents, uniqueSessions, uniqueUsers),
            eventsByType,
            new AppAnalyticsSummaryDto.ContentDto(topScreenViews, topCustomEvents),
            new AppAnalyticsSummaryDto.ErrorsDto(totalErrors, Math.round(errorRate * 10000.0) / 10000.0, topErrors),
            new AppAnalyticsSummaryDto.ConversionsDto(totalConversions, topConversions),
            performance,
            dailyTrend
        );
    }

    /**
     * 이벤트 목록 페이징 조회.
     *
     * @param eventType null 이면 전체 타입 조회
     */
    public Page<AnalyticsEventPageDto> getEvents(
        String appId,
        String eventType,
        LocalDate from,
        LocalDate to,
        Pageable pageable,
        JwtUserPrincipal principal
    ) {
        authorizeAccess(appId, principal);

        Instant fromInstant = from.atStartOfDay(KST).toInstant();
        Instant toInstant   = to.plusDays(1).atStartOfDay(KST).toInstant();

        if (eventType != null && !eventType.isBlank()) {
            return eventRepository
                .findByAppIdAndEventTypeAndClientTimestampBetween(appId, eventType, fromInstant, toInstant, pageable)
                .map(AnalyticsEventPageDto::from);
        }

        return eventRepository
            .findByAppIdAndClientTimestampBetween(appId, fromInstant, toInstant, pageable)
            .map(AnalyticsEventPageDto::from);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Private — Authorization
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * PUBLISHER 는 자신의 워크스페이스 앱만 조회 가능.
     * ADMIN 은 모든 앱 조회 가능.
     */
    private void authorizeAccess(String appId, JwtUserPrincipal principal) {
        if ("ROLE_ADMIN".equals(principal.role())) return;

        MiniApp miniApp = miniAppRepository.findByAppId(appId)
            .orElseThrow(() -> new EntityNotFoundException("Mini-app not found: " + appId));

        workspaceAuthorizationService.validateMembership(
            miniApp.getWorkspace().getWorkspaceId(),
            principal.userId()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Private — Mapping
    // ──────────────────────────────────────────────────────────────────────────

    private List<EventCountDto> toEventCountDtos(List<EventNameCount> projections) {
        return projections.stream()
            .map(EventCountDto::from)
            .toList();
    }
}
