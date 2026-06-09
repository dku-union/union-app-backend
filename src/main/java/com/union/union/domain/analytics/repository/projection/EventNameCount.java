package com.union.union.domain.analytics.repository.projection;

/**
 * 이벤트 이름별 카운트 프로젝션.
 * Native Query 결과를 Spring Data JPA 인터페이스 프로젝션으로 매핑.
 *
 * <p>SQL 컬럼 alias: {@code name}, {@code count}
 */
public interface EventNameCount {
    String getName();
    Long getCount();
}
