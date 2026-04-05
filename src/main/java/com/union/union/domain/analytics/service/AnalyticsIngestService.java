package com.union.union.domain.analytics.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.union.union.domain.analytics.dto.request.EventBatchRequestDto;
import com.union.union.domain.analytics.dto.request.RawEventDto;
import com.union.union.domain.analytics.dto.response.BatchIngestResponseDto;
import com.union.union.domain.analytics.dto.response.IngestErrorDto;
import com.union.union.domain.analytics.entity.AnalyticsEvent;
import com.union.union.domain.analytics.repository.AnalyticsEventRepository;
import com.union.union.domain.miniapp.entity.MiniAppStatus;
import com.union.union.domain.miniapp.repository.MiniAppRepository;
import com.union.union.global.common.exception.EntityNotFoundException;
import com.union.union.global.infra.redis.RedisService;
import com.union.union.global.security.jwt.JwtUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Analytics 이벤트 배치 수신 서비스.
 *
 * <h3>처리 흐름</h3>
 * <ol>
 *   <li>멱등성 키 중복 체크 (Redis, TTL 24h)</li>
 *   <li>appId 검증 — APPROVED 상태 미니앱만 허용 (Redis 캐시 5분)</li>
 *   <li>배치 내 헤더 appId 와 이벤트 appId 불일치 검사</li>
 *   <li>이벤트별 유효성 검사 (타임스탬프 범위, eventType, platform 등)</li>
 *   <li>hashedUserId JWT 재검증 — 위변조 이벤트 거절</li>
 *   <li>유효 이벤트 벌크 저장 (saveAll)</li>
 *   <li>Redis 실시간 카운터 업데이트 (이벤트 타입별 INCR)</li>
 *   <li>멱등성 키 마킹</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AnalyticsIngestService {

    private static final Set<String> VALID_EVENT_TYPES =
        Set.of("lifecycle", "screen", "performance", "error", "custom", "conversion");

    private static final Set<String> VALID_PLATFORMS = Set.of("ios", "android");

    /** 클라이언트 타임스탬프 허용 범위: 과거 24시간 ~ 미래 5분 */
    private static final Duration MAX_PAST_WINDOW   = Duration.ofHours(24);
    private static final Duration MAX_FUTURE_WINDOW = Duration.ofMinutes(5);

    /** 멱등성 키 TTL */
    private static final Duration IDEMPOTENCY_TTL = Duration.ofDays(1);

    /** Redis 실시간 카운터 TTL (당일 기준, 다음날 자정 이후 자동 만료) */
    private static final Duration COUNTER_TTL = Duration.ofDays(2);

    private final AnalyticsEventRepository eventRepository;
    private final MiniAppRepository        miniAppRepository;
    private final AnalyticsHasher          hasher;
    private final RedisService             redisService;
    private final ObjectMapper             objectMapper;

    // ──────────────────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * 이벤트 배치 수신 및 저장.
     *
     * @param requestId  X-Request-ID 헤더 (멱등성 키)
     * @param headerAppId X-Union-AppId 헤더 (배치 단위 앱 식별)
     * @param batch      요청 본문
     * @param principal  현재 인증된 사용자 (JWT)
     */
    public BatchIngestResponseDto ingest(
        String requestId,
        String headerAppId,
        EventBatchRequestDto batch,
        JwtUserPrincipal principal
    ) {
        // 1. 멱등성 중복 체크
        String idempotencyKey = buildIdempotencyKey(requestId);
        if (redisService.hasKey(idempotencyKey)) {
            log.debug("[Analytics] Duplicate batch ignored: requestId={}", requestId);
            return BatchIngestResponseDto.alreadyProcessed();
        }

        // 2. appId → APPROVED MiniApp 검증 (캐시)
        validateAppId(headerAppId);

        // 3. 이벤트별 검증 및 엔티티 변환
        List<AnalyticsEvent> toSave = new ArrayList<>();
        List<IngestErrorDto> errors  = new ArrayList<>();
        Instant now = Instant.now();

        for (int i = 0; i < batch.events().size(); i++) {
            RawEventDto raw = batch.events().get(i);
            try {
                validateEvent(raw, headerAppId, principal.userId(), now);
                toSave.add(toEntity(raw));
            } catch (AnalyticsValidationException e) {
                log.debug("[Analytics] Event[{}] rejected: {}", i, e.getMessage());
                errors.add(new IngestErrorDto(i, e.getCode(), e.getMessage()));
            }
        }

        // 4. 벌크 저장
        if (!toSave.isEmpty()) {
            eventRepository.saveAll(toSave);
            updateRealtimeCounters(headerAppId, toSave);
        }

        // 5. 멱등성 키 마킹
        redisService.setValuesWithTimeout(idempotencyKey, "1", IDEMPOTENCY_TTL);

        int accepted = toSave.size();
        log.info("[Analytics] Batch processed: appId={}, accepted={}, rejected={}", headerAppId, accepted, errors.size());
        return BatchIngestResponseDto.of(accepted, errors);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Private — Validation
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * appId 에 해당하는 APPROVED 미니앱 존재 여부 검증.
     * Redis 에 직접 캐싱 (5분 TTL).
     *
     * <p>Spring AOP 프록시 self-invocation 문제를 피하기 위해
     * {@code @Cacheable} 대신 Redis 직접 캐싱 방식을 사용한다.
     *
     * @throws EntityNotFoundException 등록되지 않거나 미승인 앱
     */
    private void validateAppId(String appId) {
        String cacheKey = "analyticsAppId:" + appId;

        // 캐시 히트 → 검증 완료된 appId
        if (redisService.hasKey(cacheKey)) return;

        boolean exists = miniAppRepository.existsByAppIdAndStatus(appId, MiniAppStatus.APPROVED);
        if (!exists) {
            throw new EntityNotFoundException(
                "Mini-app not found or not approved: appId=" + appId
            );
        }

        // 검증 결과 5분간 캐싱
        redisService.setValuesWithTimeout(cacheKey, "valid", Duration.ofMinutes(5));
    }

    private void validateEvent(RawEventDto raw, String headerAppId, UUID userId, Instant now) {
        // appId 불일치
        if (!headerAppId.equals(raw.appId())) {
            throw new AnalyticsValidationException(
                "APP_ID_MISMATCH",
                "Event appId does not match X-Union-AppId header"
            );
        }

        // eventType 화이트리스트
        if (!VALID_EVENT_TYPES.contains(raw.eventType())) {
            throw new AnalyticsValidationException(
                "INVALID_EVENT_TYPE",
                "Unknown eventType: " + raw.eventType()
            );
        }

        // platform 화이트리스트
        if (!VALID_PLATFORMS.contains(raw.platform())) {
            throw new AnalyticsValidationException(
                "INVALID_PLATFORM",
                "Unknown platform: " + raw.platform()
            );
        }

        // 타임스탬프 범위
        Instant clientTs = Instant.ofEpochMilli(raw.clientTimestamp());
        if (clientTs.isBefore(now.minus(MAX_PAST_WINDOW))) {
            throw new AnalyticsValidationException(
                "TIMESTAMP_TOO_OLD",
                "clientTimestamp is older than 24 hours"
            );
        }
        if (clientTs.isAfter(now.plus(MAX_FUTURE_WINDOW))) {
            throw new AnalyticsValidationException(
                "TIMESTAMP_IN_FUTURE",
                "clientTimestamp is too far in the future"
            );
        }

        // hashedUserId JWT 재검증 (제공된 경우에만)
        if (raw.hashedUserId() != null && !raw.hashedUserId().isBlank()) {
            if (!hasher.verify(userId, raw.appId(), raw.hashedUserId())) {
                throw new AnalyticsValidationException(
                    "HASHED_USER_ID_MISMATCH",
                    "hashedUserId does not match JWT identity"
                );
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Private — Mapping
    // ──────────────────────────────────────────────────────────────────────────

    private AnalyticsEvent toEntity(RawEventDto raw) {
        return AnalyticsEvent.builder()
            .eventType(raw.eventType())
            .eventName(raw.eventName())
            .appId(raw.appId())
            .appVersion(raw.appVersion())
            .sdkVersion(raw.sdkVersion())
            .sessionId(raw.sessionId())
            .superappSessionId(raw.superappSessionId())
            .hashedUserId(raw.hashedUserId())
            .platform(raw.platform())
            .osVersion(raw.osVersion())
            .deviceModel(raw.deviceModel())
            .sequenceNumber(raw.sequenceNumber())
            .clientTimestamp(Instant.ofEpochMilli(raw.clientTimestamp()))
            .params(toJson(raw.params()))
            .userProperties(toJson(raw.userProperties()))
            .build();
    }

    private String toJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("[Analytics] Failed to serialize params to JSON", e);
            return null;
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Private — Redis Counters
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * 이벤트 타입별 실시간 카운터 업데이트.
     * 대시보드 "오늘의 통계" 에서 DB 조회 없이 빠르게 읽을 수 있도록.
     *
     * <p>key 형식: {@code analytics:daily:{appId}:{YYYY-MM-DD}}
     * <p>value: Sorted Set (eventType → score)
     */
    private void updateRealtimeCounters(String appId, List<AnalyticsEvent> events) {
        String today = java.time.LocalDate.now().toString(); // YYYY-MM-DD
        String counterKey = buildDailyCounterKey(appId, today);

        for (AnalyticsEvent event : events) {
            redisService.incrementScore(counterKey, event.getEventType(), 1.0);
        }
        // 카운터 TTL 갱신 (2일)
        redisService.setExpire(counterKey, COUNTER_TTL);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Private — Key Builders
    // ──────────────────────────────────────────────────────────────────────────

    private static String buildIdempotencyKey(String requestId) {
        return "analytics:batch:" + requestId;
    }

    public static String buildDailyCounterKey(String appId, String date) {
        return "analytics:daily:" + appId + ":" + date;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Inner — Validation Exception
    // ──────────────────────────────────────────────────────────────────────────

    static class AnalyticsValidationException extends RuntimeException {
        private final String code;

        AnalyticsValidationException(String code, String message) {
            super(message);
            this.code = code;
        }

        String getCode() { return code; }
    }
}
