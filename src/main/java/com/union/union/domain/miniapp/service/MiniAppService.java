package com.union.union.domain.miniapp.service;

import com.union.union.domain.miniapp.dto.MiniAppRegisterRequestDto;
import com.union.union.domain.miniapp.dto.MiniAppResponseDto;
import com.union.union.domain.miniapp.entity.MiniApp;
import com.union.union.domain.miniapp.entity.MiniAppStatus;
import com.union.union.domain.miniapp.repository.MiniAppRepository;
import com.union.union.domain.miniapp.usage.dto.MiniAppUsageStatsDto;
import com.union.union.domain.miniapp.usage.dto.PublisherStatisticsResponseDto;
import com.union.union.domain.miniapp.usage.entity.MiniAppUsage;
import com.union.union.domain.miniapp.usage.repository.MiniAppUsageRepository;
import com.union.union.domain.university.entity.UniversityDomain;
import com.union.union.domain.university.repository.UniversityDomainRepository;
import com.union.union.domain.user.entity.User;
import com.union.union.domain.user.repository.UserRepository;
import com.union.union.global.common.exception.EntityNotFoundException;
import com.union.union.global.common.exception.UnauthorizedAccessException;
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
    private final UserRepository userRepository;
    private final UniversityDomainRepository universityDomainRepository;
    private final MiniAppUsageRepository miniAppUsageRepository;

    @Transactional
    @CacheEvict(value = "miniApps", allEntries = true)
    public MiniAppResponseDto register(MiniAppRegisterRequestDto request, UUID userId) {
        User publisher = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다"));

        if (publisher.getRole() != User.Role.ROLE_PUBLISHER) {
            throw new UnauthorizedAccessException("MiniApp을 등록할 권한이 없습니다 (PUBLISHER 권한 필요)");
        }

        UniversityDomain university = null;
        if (request.universityId() != null) {
            university = universityDomainRepository.findById(request.universityId())
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 대학교입니다"));
        }

        MiniApp miniApp = MiniApp.builder()
                .name(request.name())
                .description(request.description())
                .iconUrl(request.iconUrl())
                .launchUrl(request.launchUrl())
                .publisher(publisher)
                .university(university)
                .status(MiniAppStatus.PENDING)
                .build();

        miniAppRepository.save(miniApp);
        log.info("MiniApp 등록 완료. id={}, name={}, publisher={}", miniApp.getId(), miniApp.getName(), publisher.getEmail());

        return MiniAppResponseDto.from(miniApp);
    }

    @Cacheable(value = "miniApps", key = "#universityId ?: 'all'")
    public List<MiniAppResponseDto> getApprovedMiniApps(Long universityId) {
        return miniAppRepository.findByStatusAndUniversityId(MiniAppStatus.APPROVED, universityId)
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

        // 3. 중복 제거 및 결합
        List<MiniApp> combined = new java.util.ArrayList<>(universityPopular);
        for (MiniApp recent : myRecent) {
            if (!combined.contains(recent)) {
                combined.add(recent);
            }
        }

        // 4. 결과가 부족하면 글로벌 인기 앱 추가
        if (combined.size() < 5) {
            List<MiniApp> globalPopular = miniAppRepository.findPopularMiniApps(MiniAppStatus.APPROVED, PageRequest.of(0, 10));
            for (MiniApp popular : globalPopular) {
                if (!combined.contains(popular)) {
                    combined.add(popular);
                }
                if (combined.size() >= 10) break;
            }
        }

        return combined.stream()
                .map(MiniAppResponseDto::from)
                .collect(Collectors.toList());
    }

    public PublisherStatisticsResponseDto getPublisherStatistics(UUID publisherId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        
        long totalLaunchCount = miniAppUsageRepository.countUsageByPublisher(publisherId, sevenDaysAgo);
        List<MiniAppUsageStatsDto> appStats = miniAppUsageRepository.findUsageStatsByPublisher(publisherId, sevenDaysAgo);
        
        return new PublisherStatisticsResponseDto(totalLaunchCount, appStats);
    }

    public List<MiniAppResponseDto> getMiniAppsByPublisher(UUID publisherId) {
        return miniAppRepository.findByPublisherId(publisherId)
                .stream()
                .map(MiniAppResponseDto::from)
                .collect(Collectors.toList());
    }

    public String getLaunchUrl(Long id, UUID userId) {
        MiniApp miniApp = miniAppRepository.findDetailsById(id)
                .orElseThrow(() -> new EntityNotFoundException("MiniApp을 찾을 수 없습니다"));

        if (miniApp.getStatus() != MiniAppStatus.APPROVED) {
            throw new UnauthorizedAccessException("승인되지 않은 MiniApp입니다");
        }

        // 대학교 제한이 있는 경우 권한 체크
        if (miniApp.getUniversity() != null) {
            if (userId == null) {
                throw new UnauthorizedAccessException("해당 대학교 전용 MiniApp입니다. 로그인이 필요합니다.");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다"));

            if (!miniApp.getUniversity().getUniversityName().equals(user.getUniversityName())) {
                throw new UnauthorizedAccessException("해당 대학교 학생만 접근 가능한 MiniApp입니다");
            }
        }

        // 사용 로그 기록 (비로그인 유저는 userId = null로 기록될 수 있으나 요구사항상 userId 필수인 경우 로그인 필수 처리 필요)
        if (userId != null) {
            miniAppUsageRepository.save(MiniAppUsage.create(userId, id));
        }

        return miniApp.getLaunchUrl();
    }

}
