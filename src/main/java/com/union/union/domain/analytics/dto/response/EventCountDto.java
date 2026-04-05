package com.union.union.domain.analytics.dto.response;

import com.union.union.domain.analytics.repository.projection.EventNameCount;

/**
 * 이름-카운트 쌍 범용 DTO.
 * 상위 화면, 상위 커스텀 이벤트, 상위 에러, 상위 전환 등에 공통 사용.
 */
public record EventCountDto(
    String name,
    long count
) {
    public static EventCountDto from(EventNameCount projection) {
        return new EventCountDto(projection.getName(), projection.getCount());
    }
}
