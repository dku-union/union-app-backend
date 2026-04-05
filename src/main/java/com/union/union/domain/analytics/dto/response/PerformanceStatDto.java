package com.union.union.domain.analytics.dto.response;

import com.union.union.domain.analytics.repository.projection.PerformanceMetricStat;

/**
 * 단일 성능 지표 통계.
 * p50(중간값), p95(95th percentile), 평균, 샘플 수 포함.
 */
public record PerformanceStatDto(
    double p50,
    double p95,
    double avg,
    long sampleCount
) {
    public static PerformanceStatDto from(PerformanceMetricStat stat) {
        return new PerformanceStatDto(
            stat.getP50()         != null ? Math.round(stat.getP50()         * 10) / 10.0 : 0.0,
            stat.getP95()         != null ? Math.round(stat.getP95()         * 10) / 10.0 : 0.0,
            stat.getAvgValue()    != null ? Math.round(stat.getAvgValue()    * 10) / 10.0 : 0.0,
            stat.getSampleCount() != null ? stat.getSampleCount() : 0L
        );
    }
}
