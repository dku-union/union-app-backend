package com.union.union.domain.miniapp.service;

import com.union.union.domain.miniapp.dto.MiniAppLiteDto;
import com.union.union.domain.miniapp.entity.MiniAppStatus;
import com.union.union.domain.miniapp.repository.MiniAppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MiniAppCacheService {

    private final MiniAppRepository miniAppRepository;

    @Cacheable(value = "discovery_popular", sync = true)
    public List<MiniAppLiteDto> getPopularAppsCache() {
        return miniAppRepository.findPopularMiniApps(MiniAppStatus.DEPLOYED, PageRequest.of(0, 10))
                .stream()
                .map(MiniAppLiteDto::from)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "discovery_new", sync = true)
    public List<MiniAppLiteDto> getNewAppsCache() {
        return miniAppRepository.findNewMiniApps(MiniAppStatus.DEPLOYED, PageRequest.of(0, 10))
                .stream()
                .map(MiniAppLiteDto::from)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "discovery_recommended", sync = true)
    public List<MiniAppLiteDto> getRecommendedAppsCache() {
        return miniAppRepository.findRandomMiniApps(PageRequest.of(0, 5))
                .stream()
                .map(MiniAppLiteDto::from)
                .collect(Collectors.toList());
    }
}
