package com.union.union.domain.analytics.dto.request;

import jakarta.validation.constraints.*;

import java.util.Map;

/**
 * Native → Server 로 전달되는 개별 트래킹 이벤트 DTO.
 *
 * <p>네이티브 앱이 이미 Enrichment(userId 해시, deviceInfo 등)를 완료한 상태로 전달한다.
 * 서버는 {@code hashedUserId} 를 JWT 기반으로 재검증하여 위변조를 차단한다.
 */
public record RawEventDto(

    // ── 이벤트 식별 ──────────────────────────────────────────

    /** lifecycle | screen | performance | error | custom | conversion */
    @NotBlank
    @Size(max = 20)
    String eventType,

    /** /^[a-z][a-z0-9_]{0,99}$/ 형식 강제 (SDK에서 1차 검증 완료) */
    @NotBlank
    @Size(max = 100)
    @Pattern(regexp = "^[a-z][a-z0-9_]{0,99}$",
             message = "eventName must match /^[a-z][a-z0-9_]{0,99}$/")
    String eventName,

    // ── 타이밍 ───────────────────────────────────────────────

    /** 클라이언트(네이티브 앱) 이벤트 발생 시각 (epoch ms) */
    @NotNull
    @Min(value = 0, message = "clientTimestamp must be a positive epoch ms")
    Long clientTimestamp,

    // ── 세션 ─────────────────────────────────────────────────

    @NotBlank
    @Size(max = 36)
    String sessionId,

    @NotBlank
    @Size(max = 36)
    String superappSessionId,

    // ── 사용자 (프라이버시 보존) ──────────────────────────────

    /** SHA-256(userId:appId). 미로그인 시 null 허용. */
    @Size(max = 64)
    String hashedUserId,

    // ── 앱 컨텍스트 ──────────────────────────────────────────

    /** union.config.json 의 appId (reverse-domain) */
    @NotBlank
    @Size(max = 100)
    String appId,

    @NotBlank
    @Size(max = 30)
    String appVersion,

    @NotBlank
    @Size(max = 20)
    String sdkVersion,

    // ── 플랫폼 ───────────────────────────────────────────────

    /** ios | android */
    @NotBlank
    @Size(max = 10)
    String platform,

    @NotBlank
    @Size(max = 30)
    String osVersion,

    @NotBlank
    @Size(max = 50)
    String deviceModel,

    // ── 순서 ─────────────────────────────────────────────────

    @Min(0)
    int sequenceNumber,

    // ── 페이로드 ─────────────────────────────────────────────

    /** 이벤트 파라미터. string | number | boolean 값만 허용 (SDK에서 1차 검증). */
    Map<String, Object> params,

    /** setUserProperty() 로 설정된 사용자 속성 스냅샷. */
    Map<String, Object> userProperties

) {}
