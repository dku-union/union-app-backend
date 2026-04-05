package com.union.union.domain.analytics.controller;

import com.union.union.domain.analytics.dto.request.EventBatchRequestDto;
import com.union.union.domain.analytics.dto.response.AnalyticsEventPageDto;
import com.union.union.domain.analytics.dto.response.AppAnalyticsSummaryDto;
import com.union.union.domain.analytics.dto.response.BatchIngestResponseDto;
import com.union.union.domain.analytics.service.AnalyticsIngestService;
import com.union.union.domain.analytics.service.AnalyticsQueryService;
import com.union.union.global.security.jwt.JwtUserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Analytics REST API 컨트롤러.
 *
 * <h3>엔드포인트 분류</h3>
 * <table>
 *   <tr><th>메서드</th><th>경로</th><th>호출자</th><th>인증</th></tr>
 *   <tr><td>POST</td><td>/api/v1/analytics/events</td><td>iOS/Android 네이티브</td><td>User JWT</td></tr>
 *   <tr><td>GET</td><td>/api/v1/analytics/apps/{appId}/summary</td><td>퍼블리셔 대시보드</td><td>Internal JWT (PUBLISHER|ADMIN)</td></tr>
 *   <tr><td>GET</td><td>/api/v1/analytics/apps/{appId}/events</td><td>퍼블리셔 대시보드</td><td>Internal JWT (PUBLISHER|ADMIN)</td></tr>
 * </table>
 *
 * <h3>보안</h3>
 * <ul>
 *   <li>{@code POST /events}: 인증된 사용자라면 누구나 호출 가능 (앱 사용 중인 유저 = ROLE_USER)</li>
 *   <li>Query 엔드포인트: ROLE_PUBLISHER 또는 ROLE_ADMIN 만 접근 가능</li>
 *   <li>PUBLISHER 는 자신의 워크스페이스 미니앱만 조회 가능 (서비스 레이어에서 강제)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Validated
public class AnalyticsController {

    private final AnalyticsIngestService ingestService;
    private final AnalyticsQueryService  queryService;

    // ──────────────────────────────────────────────────────────────────────────
    // Ingest — 네이티브 앱에서 호출
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * 이벤트 배치 수신.
     *
     * <p>네이티브 iOS/Android 앱이 이벤트를 최대 100개씩 배치로 전송한다.
     *
     * <h4>헤더</h4>
     * <ul>
     *   <li>{@code Authorization: Bearer <user_access_token>} — 필수</li>
     *   <li>{@code X-Request-ID: <UUID>} — 멱등성 보장용 (필수)</li>
     *   <li>{@code X-Union-AppId: com.union.soccer} — 미니앱 식별 (필수)</li>
     *   <li>{@code X-Union-SDKVersion: 1.0.0} — 버전 추적용 (선택)</li>
     * </ul>
     *
     * <h4>응답 코드</h4>
     * <ul>
     *   <li>200: 전체 처리 성공</li>
     *   <li>207: 일부 이벤트 거절 ({@code rejected > 0})</li>
     *   <li>400: 요청 형식 오류</li>
     *   <li>401: 인증 실패 (토큰 만료 → 네이티브가 갱신 후 재시도)</li>
     *   <li>404: 미등록 또는 미승인 appId</li>
     * </ul>
     */
    @PostMapping("/events")
    public ResponseEntity<BatchIngestResponseDto> ingestEvents(
        @RequestHeader("X-Request-ID")
        @NotBlank(message = "X-Request-ID header is required")
        String requestId,

        @RequestHeader("X-Union-AppId")
        @NotBlank(message = "X-Union-AppId header is required")
        String appId,

        @RequestHeader(value = "X-Union-SDKVersion", required = false, defaultValue = "unknown")
        String sdkVersion,

        @Valid @RequestBody EventBatchRequestDto batch,

        @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        BatchIngestResponseDto result = ingestService.ingest(requestId, appId, batch, principal);

        // 일부 이벤트 거절 시 207 Multi-Status
        HttpStatus status = result.rejected() > 0 ? HttpStatus.MULTI_STATUS : HttpStatus.OK;
        return ResponseEntity.status(status).body(result);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Query — 퍼블리셔 대시보드에서 호출
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * 미니앱 Analytics 요약 조회.
     *
     * <p>기간 집계 데이터를 반환한다. 결과는 5분간 Redis 캐싱됨.
     *
     * @param appId 미니앱 식별자 (e.g. "com.union.soccer")
     * @param from  조회 시작일 (ISO 8601, 기본값: 오늘 기준 30일 전)
     * @param to    조회 종료일 (ISO 8601, 기본값: 오늘)
     */
    @GetMapping("/apps/{appId}/summary")
    @PreAuthorize("hasRole('PUBLISHER') or hasRole('ADMIN')")
    public ResponseEntity<AppAnalyticsSummaryDto> getSummary(
        @PathVariable String appId,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to,

        @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        LocalDate resolvedFrom = from != null ? from : LocalDate.now().minusDays(30);
        LocalDate resolvedTo   = to   != null ? to   : LocalDate.now();

        validateDateRange(resolvedFrom, resolvedTo);

        return ResponseEntity.ok(queryService.getSummary(appId, resolvedFrom, resolvedTo, principal));
    }

    /**
     * 미니앱 이벤트 목록 페이징 조회.
     *
     * @param appId     미니앱 식별자
     * @param eventType 필터 (선택). lifecycle | screen | performance | error | custom | conversion
     * @param from      조회 시작일 (기본값: 오늘 기준 7일 전)
     * @param to        조회 종료일 (기본값: 오늘)
     * @param pageable  page, size(기본 20, 최대 100), sort
     */
    @GetMapping("/apps/{appId}/events")
    @PreAuthorize("hasRole('PUBLISHER') or hasRole('ADMIN')")
    public ResponseEntity<Page<AnalyticsEventPageDto>> getEvents(
        @PathVariable String appId,

        @RequestParam(required = false) String eventType,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to,

        @PageableDefault(size = 20, sort = "clientTimestamp", direction = Sort.Direction.DESC)
        Pageable pageable,

        @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        LocalDate resolvedFrom = from != null ? from : LocalDate.now().minusDays(7);
        LocalDate resolvedTo   = to   != null ? to   : LocalDate.now();

        validateDateRange(resolvedFrom, resolvedTo);
        validatePageSize(pageable);

        return ResponseEntity.ok(
            queryService.getEvents(appId, eventType, resolvedFrom, resolvedTo, pageable, principal)
        );
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Private — Validation Helpers
    // ──────────────────────────────────────────────────────────────────────────

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new com.union.union.global.common.exception.BadRequestException(
                "from date must be before or equal to to date"
            );
        }
        if (java.time.temporal.ChronoUnit.DAYS.between(from, to) > 365) {
            throw new com.union.union.global.common.exception.BadRequestException(
                "Date range cannot exceed 365 days"
            );
        }
    }

    private void validatePageSize(Pageable pageable) {
        if (pageable.getPageSize() > 100) {
            throw new com.union.union.global.common.exception.BadRequestException(
                "Maximum page size is 100"
            );
        }
    }
}
