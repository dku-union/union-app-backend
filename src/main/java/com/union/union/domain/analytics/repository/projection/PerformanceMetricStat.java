package com.union.union.domain.analytics.repository.projection;

/**
 * 성능 지표 통계 프로젝션.
 * {@code percentile_cont} 를 사용한 Native Query 결과 매핑.
 *
 * <p>SQL 컬럼 alias: {@code metricName}, {@code p50}, {@code p95},
 * {@code avgValue}, {@code sampleCount}
 */
public interface PerformanceMetricStat {
    String getMetricName();
    Double getP50();
    Double getP95();
    Double getAvgValue();
    Long getSampleCount();
}
