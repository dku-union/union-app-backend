package com.union.union.domain.miniapp.service;

import com.union.union.domain.appversion.entity.AppVersion;
import com.union.union.domain.appversion.entity.VersionStatus;
import com.union.union.domain.appversion.repository.AppVersionRepository;
import com.union.union.domain.miniapp.dto.*;
import com.union.union.domain.miniapp.entity.MiniApp;
import com.union.union.global.infra.gcs.dto.GcsSignedUrlResponseDto;
import com.union.union.domain.miniapp.entity.MiniAppCategory;
import com.union.union.domain.miniapp.entity.MiniAppStatus;
import com.union.union.domain.miniapp.repository.MiniAppCategoryRepository;
import com.union.union.domain.miniapp.repository.MiniAppRepository;
import com.union.union.domain.miniapp.usage.dto.MiniAppUsageStatsDto;
import com.union.union.domain.miniapp.usage.dto.PublisherStatisticsResponseDto;
import com.union.union.domain.miniapp.usage.entity.MiniAppUsage;
import com.union.union.domain.miniapp.usage.repository.MiniAppUsageRepository;
import com.union.union.domain.user.entity.User;
import com.union.union.domain.user.repository.UserRepository;
import com.union.union.domain.workspace.entity.Workspace;
import com.union.union.domain.workspace.repository.WorkspaceRepository;
import com.union.union.domain.workspace.service.WorkspaceAuthorizationService;
import com.union.union.global.common.exception.EntityNotFoundException;
import com.union.union.global.common.exception.UnauthorizedAccessException;
import com.union.union.global.infra.gcs.GcsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MiniAppService {

    private final MiniAppRepository miniAppRepository;
    private final MiniAppCategoryRepository miniAppCategoryRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceAuthorizationService workspaceAuthorizationService;
    private final MiniAppUsageRepository miniAppUsageRepository;
    private final AppVersionRepository appVersionRepository;
    private final GcsService gcsService;
    private final MiniAppSearchService miniAppSearchService;
    private final MiniAppCacheService miniAppCacheService;

    @Transactional
    @CacheEvict(value = "miniApps", allEntries = true)
    public MiniAppResponseDto register(MiniAppRegisterRequestDto request, UUID publisherId) {
        Workspace workspace = workspaceRepository.findById(request.workspaceId())
                .orElseThrow(() -> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다"));

        workspaceAuthorizationService.validateMembership(workspace.getWorkspaceId(), publisherId);

        MiniAppCategory category = miniAppCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다"));

        MiniApp miniApp = MiniApp.builder()
                .name(request.name())
                .description(request.description())
                .iconUrl(request.iconUrl())
                .category(category)
                .workspace(workspace)
                .status(MiniAppStatus.PENDING)
                .build();

        miniAppRepository.save(miniApp);
        log.info("MiniApp 등록 완료. id={}, name={}, workspaceId={}", miniApp.getId(), miniApp.getName(), workspace.getWorkspaceId());

        return MiniAppResponseDto.from(miniApp);
    }

    // TODO: 대학교별 필터링 (university 확정 후 구현)
    // TODO: 커서 페이징 구현 (기획 확정 후)
    @Cacheable(value = "miniApps", key = "'approved'")
    public List<MiniAppResponseDto> getApprovedMiniApps() {
        return miniAppRepository.findByStatus(MiniAppStatus.APPROVED)
                .stream()
                .map(MiniAppResponseDto::from)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "popularMiniApps", key = "#limit")
    public List<MiniAppResponseDto> getPopularMiniApps(int limit) {
        return miniAppRepository.findPopularMiniApps(MiniAppStatus.APPROVED, PageRequest.of(0, limit))
                .stream()
                .map(MiniAppResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<MiniAppResponseDto> getRecommendedMiniApps(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다"));

        // 1. 같은 대학 인기 앱 (Top 5)
        List<MiniApp> universityPopular = miniAppRepository.findRecommendedByUniversity(
                user.getUniversityName(), MiniAppStatus.APPROVED, PageRequest.of(0, 5));

        // 2. 내가 최근에 사용한 앱 (Top 5)
        List<MiniApp> myRecent = miniAppRepository.findRecentByUser(userId, MiniAppStatus.APPROVED, PageRequest.of(0, 5));

        // 3. ID 기반 중복 제거 및 결합
        java.util.LinkedHashMap<Long, MiniApp> combined = new java.util.LinkedHashMap<>();
        universityPopular.forEach(m -> combined.put(m.getId(), m));
        myRecent.forEach(m -> combined.putIfAbsent(m.getId(), m));

        // 4. 결과가 부족하면 글로벌 인기 앱 추가
        if (combined.size() < 5) {
            List<MiniApp> globalPopular = miniAppRepository.findPopularMiniApps(MiniAppStatus.APPROVED, PageRequest.of(0, 10));
            for (MiniApp popular : globalPopular) {
                combined.putIfAbsent(popular.getId(), popular);
                if (combined.size() >= 10) break;
            }
        }

        return combined.values().stream()
                .map(MiniAppResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 모든 미니앱의 searchableName을 동기화합니다. (데이터 마이그레이션용)
     */
    @Transactional
    public void syncAllSearchableNames() {
        List<MiniApp> allApps = miniAppRepository.findAll();
        for (MiniApp app : allApps) {
            app.syncSearchableName();
        }
        miniAppRepository.saveAll(allApps);
        log.info("모든 미니앱의 searchableName 동기화 완료. 건수={}", allApps.size());
    }

    public DiscoveryResponseDto getDiscoveryData(UUID userId) {
        List<MiniAppLiteDto> recentApps = getRecentApps(userId);
        List<MiniAppLiteDto> popularApps = miniAppCacheService.getPopularAppsCache();
        List<MiniAppLiteDto> newApps = miniAppCacheService.getNewAppsCache();
        List<MiniAppLiteDto> recommendedApps = miniAppCacheService.getRecommendedAppsCache();
        
        // Fetch real-time trending keywords from Redis (top 4)
        List<String> trendingKeywords = miniAppSearchService.getPopularKeywords(4);
        
        List<MiniAppCategoryResponseDto> categories = miniAppCategoryRepository.findByIsActiveTrue()
                .stream()
                .map(MiniAppCategoryResponseDto::from)
                .collect(Collectors.toList());

        return new DiscoveryResponseDto(recentApps, popularApps, newApps, recommendedApps, trendingKeywords, categories);
    }

    private List<MiniAppLiteDto> getRecentApps(UUID userId) {
        if (userId == null) {
            return java.util.Collections.emptyList();
        }
        return miniAppRepository.findRecentByUser(userId, MiniAppStatus.APPROVED, PageRequest.of(0, 5))
                .stream()
                .map(MiniAppLiteDto::from)
                .collect(Collectors.toList());
    }


    public PublisherStatisticsResponseDto getWorkspaceStatistics(UUID workspaceId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        long totalLaunchCount = miniAppUsageRepository.countUsageByWorkspace(workspaceId, sevenDaysAgo);
        List<MiniAppUsageStatsDto> appStats = miniAppUsageRepository.findUsageStatsByWorkspace(workspaceId, sevenDaysAgo);

        return new PublisherStatisticsResponseDto(totalLaunchCount, appStats);
    }

    public List<MiniAppResponseDto> getMiniAppsByWorkspace(UUID workspaceId) {
        return miniAppRepository.findByWorkspace_WorkspaceId(workspaceId)
                .stream()
                .map(MiniAppResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * publisher가 속한 모든 워크스페이스의 미니앱을 합쳐 반환 (iOS publisher 전용 페이지).
     */
    public List<MiniAppResponseDto> getMiniAppsByPublisher(UUID publisherId) {
        return miniAppRepository.findByPublisherMembership(publisherId)
                .stream()
                .map(MiniAppResponseDto::from)
                .collect(Collectors.toList());
    }

    public GcsSignedUrlResponseDto getIconUploadUrl(Long miniAppId, UUID publisherId, String filename) {
        MiniApp miniApp = miniAppRepository.findById(miniAppId)
                .orElseThrow(() -> new EntityNotFoundException("MiniApp을 찾을 수 없습니다"));
        workspaceAuthorizationService.validateMembership(miniApp.getWorkspace().getWorkspaceId(), publisherId);
        return gcsService.getIconUploadUrl(miniAppId, filename);
    }

    @Transactional
    @CacheEvict(value = "miniApps", allEntries = true)
    public MiniAppResponseDto updateIconUrl(Long miniAppId, UUID publisherId, String iconUrl) {
        MiniApp miniApp = miniAppRepository.findById(miniAppId)
                .orElseThrow(() -> new EntityNotFoundException("MiniApp을 찾을 수 없습니다"));
        workspaceAuthorizationService.validateMembership(miniApp.getWorkspace().getWorkspaceId(), publisherId);
        miniApp.updateIconUrl(iconUrl);
        return MiniAppResponseDto.from(miniApp);
    }

    @Transactional
    public String getLaunchUrl(Long id, UUID userId) {
        MiniApp miniApp = miniAppRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MiniApp을 찾을 수 없습니다"));

        if (miniApp.getStatus() != MiniAppStatus.APPROVED) {
            throw new UnauthorizedAccessException("배포되지 않은 MiniApp입니다");
        }

        // TODO: 대학교 제한 체크 (university 필터링 확정 후 구현)

        AppVersion deployedVersion = appVersionRepository
                .findFirstByMiniAppIdAndStatusOrderByCreatedAtDesc(id, VersionStatus.DEPLOYED)
                .orElseThrow(() -> new EntityNotFoundException("배포된 버전이 없습니다"));

        miniAppUsageRepository.save(MiniAppUsage.create(userId, id));

        return gcsService.getCdnDownloadUrl(deployedVersion.getBuildFileUrl());
    }
}
