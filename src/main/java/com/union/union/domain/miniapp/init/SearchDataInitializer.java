package com.union.union.domain.miniapp.init;

import com.union.union.domain.miniapp.service.MiniAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 프로젝트 시작 시 미니앱 검색 전용 이름을 동기화하는 초기화 클래스.
 * 기존 데이터에 searchableName이 없는 경우를 대비해 1회 실행 유도.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchDataInitializer implements CommandLineRunner {

    private final MiniAppService miniAppService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting MiniApp SearchableName synchronization...");
        try {
            miniAppService.syncAllSearchableNames();
            log.info("MiniApp SearchableName synchronization completed successfully.");
        } catch (Exception e) {
            log.error("Failed to synchronize MiniApp SearchableName", e);
        }
    }
}
