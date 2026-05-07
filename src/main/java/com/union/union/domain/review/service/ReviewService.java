package com.union.union.domain.review.service;

import com.union.union.domain.appversion.dto.TestLinkResponseDto;
import com.union.union.domain.appversion.entity.AppVersion;
import com.union.union.domain.appversion.entity.VersionStatus;
import com.union.union.domain.appversion.repository.AppVersionRepository;
import com.union.union.domain.appversion.service.AppVersionService;
import com.union.union.domain.review.dto.ReviewDecisionRequestDto;
import com.union.union.domain.review.dto.ReviewResponseDto;
import com.union.union.domain.user.entity.User;
import com.union.union.domain.user.repository.UserRepository;
import com.union.union.domain.review.entity.Review;
import com.union.union.domain.review.entity.Verdict;
import com.union.union.domain.review.repository.ReviewRepository;
import com.union.union.domain.workspace.service.WorkspaceAuthorizationService;
import com.union.union.global.common.exception.BadRequestException;
import com.union.union.global.common.exception.ConflictException;
import com.union.union.global.common.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppVersionRepository appVersionRepository;
    private final UserRepository userRepository;
    private final WorkspaceAuthorizationService workspaceAuthorizationService;
    private final AppVersionService appVersionService;

    @Transactional
    public ReviewResponseDto submitForReview(UUID versionId, UUID publisherId) {
        AppVersion version = appVersionRepository.findDetailedById(versionId)
                .orElseThrow(() -> new EntityNotFoundException("AppVersion을 찾을 수 없습니다"));

        workspaceAuthorizationService.validateMembership(
                version.getMiniApp().getWorkspace().getWorkspaceId(), publisherId);

        if (reviewRepository.existsByVersionIdAndVerdict(versionId, Verdict.PENDING)) {
            throw new ConflictException("이미 심사가 진행 중인 버전입니다");
        }

        version.submitForReview();

        Review review = Review.builder()
                .version(version)
                .build();

        reviewRepository.save(review);

        log.info("심사 요청 완료. reviewId={}, versionId={}", review.getId(), versionId);

        return ReviewResponseDto.from(review);
    }

    public List<ReviewResponseDto> getPendingReviews() {
        return reviewRepository.findByVerdictOrderByCreatedAtAsc(Verdict.PENDING)
                .stream()
                .map(ReviewResponseDto::from)
                .collect(Collectors.toList());
    }

    public ReviewResponseDto getReview(UUID reviewId) {
        Review review = reviewRepository.findDetailedById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review를 찾을 수 없습니다"));
        return ReviewResponseDto.from(review);
    }

    public TestLinkResponseDto getTestLinkForReview(UUID reviewId, UUID adminId) {
        Review review = reviewRepository.findDetailedById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review를 찾을 수 없습니다"));
        return appVersionService.generateTestLink(review.getVersion().getId(), adminId);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "miniApps", allEntries = true),
            @CacheEvict(value = "popularMiniApps", allEntries = true)
    })
    public ReviewResponseDto decide(UUID reviewId, ReviewDecisionRequestDto request, UUID adminId) {
        Review review = reviewRepository.findDetailedById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review를 찾을 수 없습니다"));

        User admin = userRepository.findById(adminId).orElse(null);

        review.decide(admin, request.verdict(), request.reason());

        AppVersion version = review.getVersion();
        if (request.verdict() == Verdict.ACCEPTED) {
            version.accept();
            log.info("심사 승인. reviewId={}, versionId={}", reviewId, version.getId());
        } else {
            version.reject();
            log.info("심사 거절. reviewId={}, versionId={}, reason={}", reviewId, version.getId(), request.reason());
        }

        return ReviewResponseDto.from(review);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "miniApps", allEntries = true),
            @CacheEvict(value = "popularMiniApps", allEntries = true)
    })
    public ReviewResponseDto decideByVersionId(UUID versionId, ReviewDecisionRequestDto request, UUID adminId) {
        AppVersion version = appVersionRepository.findDetailedById(versionId)
                .orElseThrow(() -> new EntityNotFoundException("AppVersion을 찾을 수 없습니다"));

        if (version.getStatus() != VersionStatus.IN_REVIEW) {
            throw new BadRequestException("IN_REVIEW 상태가 아닙니다. 현재 상태: " + version.getStatus());
        }

        User admin = userRepository.findById(adminId).orElse(null);

        Review review = reviewRepository.findDetailedByVersionIdAndVerdict(versionId, Verdict.PENDING)
                .orElseGet(() -> {
                    Review newReview = Review.builder().version(version).build();
                    return reviewRepository.save(newReview);
                });

        review.decide(admin, request.verdict(), request.reason());

        if (request.verdict() == Verdict.ACCEPTED) {
            version.accept();
            log.info("심사 승인 (versionId 기준). versionId={}", versionId);
        } else {
            version.reject();
            log.info("심사 거절 (versionId 기준). versionId={}, reason={}", versionId, request.reason());
        }

        return ReviewResponseDto.from(review);
    }
}
