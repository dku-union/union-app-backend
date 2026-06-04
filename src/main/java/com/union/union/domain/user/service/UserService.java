package com.union.union.domain.user.service;

import com.union.union.domain.auth.repository.RefreshTokenRepository;
import com.union.union.domain.notification.repository.UserFcmTokenRepository;
import com.union.union.domain.subscription.repository.MiniAppSubscriptionRepository;
import com.union.union.domain.user.dto.ProfileImageUploadUrlRequestDto;
import com.union.union.domain.user.dto.UpdateNicknameRequestDto;
import com.union.union.domain.user.dto.UpdateProfileImageRequestDto;
import com.union.union.domain.user.entity.User;
import com.union.union.domain.user.repository.UserRepository;
import com.union.union.global.common.exception.EntityNotFoundException;
import com.union.union.global.infra.gcs.GcsService;
import com.union.union.global.infra.gcs.dto.GcsSignedUrlResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final GcsService gcsService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserFcmTokenRepository userFcmTokenRepository;
    private final MiniAppSubscriptionRepository miniAppSubscriptionRepository;

    public User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. id: " + id));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateNickname(UUID userId, UpdateNicknameRequestDto request) {
        User user = getUser(userId);
        user.updateProfile(request.nickname(), null);
        return user;
    }

    public GcsSignedUrlResponseDto getProfileImageUploadUrl(UUID userId, ProfileImageUploadUrlRequestDto request) {
        getUser(userId); // 존재 여부 검증
        return gcsService.getProfileImageUploadUrl(userId, request.filename());
    }

    @Transactional
    public User updateProfileImage(UUID userId, UpdateProfileImageRequestDto request) {
        User user = getUser(userId);
        user.updateProfile(null, request.imageUrl());
        return user;
    }

    @Transactional
    public void withdrawMe(UUID userId) {
        withdrawAndPurge(getUser(userId));
    }

    @Transactional
    public void deleteUser(UUID id) {
        withdrawAndPurge(getUser(id));
    }

    /**
     * 탈퇴(본인/관리자 공통) 시 계정의 인증·푸시 흔적을 일괄 정리한다.
     * - 상태 WITHDRAWN 전환 + email tombstone(재가입 허용) + 프로필 이미지 제거
     * - refresh 토큰 전체 무효화 → 탈퇴 후 /auth/refresh 로 access 토큰 재발급 차단
     * - FCM 토큰 전체 삭제 → 탈퇴 기기로 푸시 중단
     * - 활성 구독 전체 해지 → 미니앱 푸시 타깃에서 제외
     *
     * 모든 벌크 연산은 User 가 아닌 자식 엔티티만 대상으로 하며, 같은 트랜잭션에서 해당
     * 엔티티들을 다시 로드하지 않으므로 영속성 컨텍스트 staleness 문제가 없다.
     * managed User 는 계속 사용하므로 persistence context 를 clear 하지 않는다.
     */
    private void withdrawAndPurge(User user) {
        UUID userId = user.getId();
        user.withdraw();
        user.anonymizeOnWithdrawal();

        refreshTokenRepository.revokeAllByUserId(userId);
        int fcmDeleted = userFcmTokenRepository.deleteAllByUserId(userId);
        int subsDeactivated = miniAppSubscriptionRepository.deactivateAllByUserId(userId);

        log.info("회원 탈퇴 처리 완료. userId={}, fcmToken 삭제={}, 구독 해지={}",
                userId, fcmDeleted, subsDeactivated);
    }
}
