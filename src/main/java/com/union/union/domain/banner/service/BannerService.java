package com.union.union.domain.banner.service;

import com.union.union.domain.banner.dto.BannerResponseDto;
import com.union.union.domain.banner.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerService {

    private final BannerRepository bannerRepository;

    /**
     * 활성 배너 목록 — 캐시 5분.
     * 시점 의존이라 캐시 키에 시간을 포함하지 않고 전체 결과를 단일 키로 보관.
     * (배너 변경 빈도 낮음, 약간의 staleness 허용.)
     */
    @Cacheable(value = "banners", key = "'active'")
    public List<BannerResponseDto> getActiveBanners() {
        return bannerRepository.findActiveBanners(LocalDateTime.now())
                .stream()
                .map(BannerResponseDto::from)
                .toList();
    }

    /**
     * 배너 등록/수정/삭제 시 호출 — 캐시 무효화.
     */
    @CacheEvict(value = "banners", allEntries = true)
    public void invalidateCache() {
        // marker
    }
}
