package com.union.union.domain.analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * 미니앱에서 수집된 트래킹 이벤트 원본 레코드.
 *
 * <p>JS SDK → Native Bridge → POST /api/v1/analytics/events 경로로 유입된 이벤트를
 * 네이티브에서 보강(Enrichment)한 최종 페이로드를 그대로 저장한다.
 *
 * <p>params / userProperties 는 JSON 문자열로 저장되며,
 * PostgreSQL 의 {@code ::jsonb} 캐스팅을 통해 네이티브 쿼리에서 JSON 필드 추출이 가능하다.
 */
@Entity
@Table(
    name = "analytics_events",
    indexes = {
        @Index(name = "idx_ae_app_id",          columnList = "app_id"),
        @Index(name = "idx_ae_app_id_ts",        columnList = "app_id, client_timestamp"),
        @Index(name = "idx_ae_session_id",       columnList = "session_id"),
        @Index(name = "idx_ae_event_type_name",  columnList = "event_type, event_name"),
        @Index(name = "idx_ae_hashed_user_id",   columnList = "hashed_user_id"),
        @Index(name = "idx_ae_client_timestamp", columnList = "client_timestamp"),
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AnalyticsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    // ──────────────────────────────────────────────────────
    // 이벤트 식별
    // ──────────────────────────────────────────────────────

    /**
     * 이벤트 분류. lifecycle | screen | performance | error | custom | conversion
     */
    @Column(name = "event_type", nullable = false, length = 20)
    private String eventType;

    /**
     * 이벤트 이름. SDK 레벨에서 /^[a-z][a-z0-9_]{0,99}$/ 형식이 강제됨.
     */
    @Column(name = "event_name", nullable = false, length = 100)
    private String eventName;

    // ──────────────────────────────────────────────────────
    // 앱 컨텍스트
    // ──────────────────────────────────────────────────────

    /** 미니앱 식별자 (reverse-domain, e.g. "com.union.soccer") */
    @Column(name = "app_id", nullable = false, length = 100)
    private String appId;

    @Column(name = "app_version", nullable = false, length = 30)
    private String appVersion;

    @Column(name = "sdk_version", nullable = false, length = 20)
    private String sdkVersion;

    // ──────────────────────────────────────────────────────
    // 세션
    // ──────────────────────────────────────────────────────

    /** 미니앱 실행 단위 세션 UUID */
    @Column(name = "session_id", nullable = false, length = 36)
    private String sessionId;

    /** 슈퍼앱 전체 세션 UUID (크로스 미니앱 세션 분석용) */
    @Column(name = "superapp_session_id", nullable = false, length = 36)
    private String superappSessionId;

    // ──────────────────────────────────────────────────────
    // 사용자 (프라이버시 보존)
    // ──────────────────────────────────────────────────────

    /**
     * SHA-256(userId:appId). 미로그인/익명 세션 시 null.
     * 원본 userId 는 서버에 저장하지 않는다.
     */
    @Column(name = "hashed_user_id", length = 64)
    private String hashedUserId;

    // ──────────────────────────────────────────────────────
    // 플랫폼
    // ──────────────────────────────────────────────────────

    @Column(nullable = false, length = 10)
    private String platform;

    @Column(name = "os_version", nullable = false, length = 30)
    private String osVersion;

    @Column(name = "device_model", nullable = false, length = 50)
    private String deviceModel;

    // ──────────────────────────────────────────────────────
    // 순서 및 타이밍
    // ──────────────────────────────────────────────────────

    /** 세션 내 이벤트 순서 (0부터 증가). 네이티브가 설정. 이벤트 유실 감지용. */
    @Column(name = "sequence_number", nullable = false)
    private int sequenceNumber;

    /** 클라이언트(네이티브 앱) 측 이벤트 발생 시각 */
    @Column(name = "client_timestamp", nullable = false)
    private Instant clientTimestamp;

    /** 서버 수신 시각 (신뢰 기준 타임스탬프). 자동 설정. */
    @CreatedDate
    @Column(name = "server_timestamp", nullable = false, updatable = false)
    private Instant serverTimestamp;

    // ──────────────────────────────────────────────────────
    // 페이로드 (JSON)
    // ──────────────────────────────────────────────────────

    /**
     * 이벤트 파라미터 (JSON 문자열).
     * PII 는 SDK 및 네이티브에서 2중 마스킹됨.
     * Native SQL 에서 {@code params::jsonb->>'key'} 로 접근 가능.
     */
    @Column(name = "params", columnDefinition = "TEXT")
    private String params;

    /**
     * 이벤트 발생 시점의 사용자 속성 스냅샷 (JSON 문자열).
     * Native SQL 에서 {@code user_properties::jsonb->>'key'} 로 접근 가능.
     */
    @Column(name = "user_properties", columnDefinition = "TEXT")
    private String userProperties;
}
