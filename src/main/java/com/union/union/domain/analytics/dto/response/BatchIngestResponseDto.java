package com.union.union.domain.analytics.dto.response;

import java.util.List;

/**
 * Analytics 배치 수신 응답 DTO.
 *
 * <p>HTTP 200: 모든 이벤트 처리 성공<br>
 * HTTP 207 Multi-Status: 일부 이벤트 처리 실패 ({@code rejected > 0})
 */
public record BatchIngestResponseDto(
    int accepted,
    int rejected,
    List<IngestErrorDto> errors
) {

    public static BatchIngestResponseDto of(int accepted, List<IngestErrorDto> errors) {
        return new BatchIngestResponseDto(accepted, errors.size(), errors);
    }

    /** 멱등성 키 중복 감지 시 반환 (이전 처리 결과 재사용). */
    public static BatchIngestResponseDto alreadyProcessed() {
        return new BatchIngestResponseDto(0, 0, List.of());
    }
}
