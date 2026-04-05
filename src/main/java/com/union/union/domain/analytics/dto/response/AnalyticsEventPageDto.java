package com.union.union.domain.analytics.dto.response;

import com.union.union.domain.analytics.entity.AnalyticsEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * 이벤트 목록 조회용 경량 응답 DTO.
 * params/userProperties 는 원본 JSON 문자열 그대로 반환.
 */
public record AnalyticsEventPageDto(
    UUID id,
    String eventType,
    String eventName,
    String sessionId,
    String hashedUserId,
    String platform,
    String osVersion,
    String deviceModel,
    int sequenceNumber,
    Instant clientTimestamp,
    Instant serverTimestamp,
    String params,
    String userProperties
) {
    public static AnalyticsEventPageDto from(AnalyticsEvent e) {
        return new AnalyticsEventPageDto(
            e.getId(),
            e.getEventType(),
            e.getEventName(),
            e.getSessionId(),
            e.getHashedUserId(),
            e.getPlatform(),
            e.getOsVersion(),
            e.getDeviceModel(),
            e.getSequenceNumber(),
            e.getClientTimestamp(),
            e.getServerTimestamp(),
            e.getParams(),
            e.getUserProperties()
        );
    }
}
