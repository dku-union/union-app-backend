package com.union.union.domain.miniapp.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MiniAppCacheScheduler {

    /**
     * 인기 미니앱: 30분(1,800,000ms)마다 캐시 브라우징 초기화
     */
    @Scheduled(fixedRate = 1800000)
    @CacheEvict(value = "discovery_popular", allEntries = true)
    public void evictPopularAppsCache() {
        log.info("인기 미니앱(Popular) 캐시가 초기화되었습니다. (주기: 30분)");
    }

    /**
     * 추천 미니앱: 30분(1,800,000ms)마다 캐시 브라우징 초기화
     */
    @Scheduled(fixedRate = 1800000)
    @CacheEvict(value = "discovery_recommended", allEntries = true)
    public void evictRecommendedAppsCache() {
        log.info("추천 미니앱(Recommended) 캐시가 초기화되었습니다. (주기: 30분)");
    }

    /**
     * 신규 미니앱: 10분(600,000ms)마다 캐시 브라우징 초기화
     */
    @Scheduled(fixedRate = 600000)
    @CacheEvict(value = "discovery_new", allEntries = true)
    public void evictNewAppsCache() {
        log.info("새로운 미니앱(New) 캐시가 초기화되었습니다. (주기: 10분)");
    }
}
