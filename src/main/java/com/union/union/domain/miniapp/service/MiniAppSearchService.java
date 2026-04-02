package com.union.union.domain.miniapp.service;

import com.union.union.domain.miniapp.dto.MiniAppLiteDto;
import com.union.union.domain.miniapp.entity.MiniAppCategory;
import com.union.union.domain.miniapp.entity.MiniAppStatus;
import com.union.union.domain.miniapp.repository.MiniAppCategoryRepository;
import com.union.union.domain.miniapp.repository.MiniAppRepository;
import com.union.union.global.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MiniAppSearchService {

    private final MiniAppRepository miniAppRepository;
    private final MiniAppCategoryRepository miniAppCategoryRepository;
    private final RedisService redisService;
    
    private static final String SEARCH_TRENDING_KEY = "search:trending";

    public List<MiniAppLiteDto> searchByKeyword(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            // Redis ZSet에 검색어 랭크 반영 (실시간 트렌딩 누적)
            redisService.incrementScore(SEARCH_TRENDING_KEY, keyword.trim(), 1.0);
        }

        return miniAppRepository.searchByKeyword(keyword, MiniAppStatus.APPROVED, pageable)
                .stream()
                .map(MiniAppLiteDto::from)
                .collect(Collectors.toList());
    }

    public List<MiniAppLiteDto> searchByCategory(Long categoryId, Pageable pageable) {
        MiniAppCategory category = miniAppCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new com.union.union.global.common.exception.EntityNotFoundException("카테고리를 찾을 수 없습니다"));

        return miniAppRepository.findByCategoryAndStatus(category, MiniAppStatus.APPROVED, pageable)
                .stream()
                .map(MiniAppLiteDto::from)
                .collect(Collectors.toList());
    }

    public List<String> getPopularKeywords(int limit) {
        Set<Object> tops = redisService.getTopRanks(SEARCH_TRENDING_KEY, limit);
        if (tops == null || tops.isEmpty()) {
            return List.of("#축제", "#소통", "#학식", "#스터디"); // 가짜 폴백 데이터 (검색 기록 없을 때 UI 깨짐 방지)
        }
        return tops.stream()
                .map(Object::toString)
                .map(word -> "#" + word) // 해시태그 형식으로 변환 (UI 요구사항 맞춤)
                .collect(Collectors.toList());
    }
}
