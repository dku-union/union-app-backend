package com.union.union.domain.analytics.dto.response;

/**
 * 배치 내 개별 이벤트 처리 실패 정보.
 *
 * @param index   배치 배열 내 이벤트 인덱스 (0-based)
 * @param code    에러 코드 (e.g. INVALID_EVENT_TYPE, TIMESTAMP_OUT_OF_RANGE)
 * @param message 에러 상세 메시지
 */
public record IngestErrorDto(
    int index,
    String code,
    String message
) {}
