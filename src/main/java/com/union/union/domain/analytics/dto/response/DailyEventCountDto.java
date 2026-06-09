package com.union.union.domain.analytics.dto.response;

import com.union.union.domain.analytics.repository.projection.DailyCount;

import java.time.LocalDate;

/** 날짜별 이벤트 카운트. 대시보드 추이 차트용. */
public record DailyEventCountDto(
    LocalDate date,
    long count
) {
    public static DailyEventCountDto from(DailyCount projection) {
        return new DailyEventCountDto(projection.getEventDate(), projection.getCount());
    }
}
