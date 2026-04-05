package com.union.union.domain.analytics.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 미니앱 애널리틱스 요약 응답 DTO.
 *
 * <p>퍼블리셔 대시보드의 Analytics 탭에서 소비하는 집계 데이터.
 *
 * <ul>
 *   <li>{@code overview}  — 기간 내 총 이벤트, 고유 세션, 고유 사용자</li>
 *   <li>{@code eventsByType} — 이벤트 타입별 카운트 맵</li>
 *   <li>{@code content}  — 상위 화면, 상위 커스텀 이벤트</li>
 *   <li>{@code errors}   — 에러 합계, 에러율, 상위 에러</li>
 *   <li>{@code conversions} — 전환 합계, 상위 전환 타입</li>
 *   <li>{@code performance} — FCP, LCP, page_load, bridge_latency 통계</li>
 *   <li>{@code dailyTrend}  — 날짜별 이벤트 추이</li>
 * </ul>
 */
public record AppAnalyticsSummaryDto(
    String appId,
    LocalDate from,
    LocalDate to,
    OverviewDto overview,
    Map<String, Long> eventsByType,
    ContentDto content,
    ErrorsDto errors,
    ConversionsDto conversions,
    Map<String, PerformanceStatDto> performance,
    List<DailyEventCountDto> dailyTrend
) {

    /** 기간 내 전체 규모 지표 */
    public record OverviewDto(
        long totalEvents,
        long uniqueSessions,
        long uniqueUsers
    ) {}

    /** 화면/커스텀 이벤트 상위 목록 */
    public record ContentDto(
        List<EventCountDto> topScreenViews,
        List<EventCountDto> topCustomEvents
    ) {}

    /** 에러 집계 */
    public record ErrorsDto(
        long total,
        /** errors / totalEvents (0.0 ~ 1.0) */
        double errorRate,
        List<EventCountDto> top
    ) {}

    /** 전환 집계 */
    public record ConversionsDto(
        long total,
        List<EventCountDto> top
    ) {}
}
