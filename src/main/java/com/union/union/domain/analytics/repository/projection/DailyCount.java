package com.union.union.domain.analytics.repository.projection;

import java.time.LocalDate;

/**
 * 일별 이벤트 카운트 프로젝션.
 *
 * <p>SQL 컬럼 alias: {@code eventDate}, {@code count}
 */
public interface DailyCount {
    LocalDate getEventDate();
    Long getCount();
}
