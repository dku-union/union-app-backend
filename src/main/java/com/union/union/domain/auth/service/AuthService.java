package com.union.union.domain.auth.service;

import com.union.union.domain.auth.dto.*;
import com.union.union.domain.auth.entity.RefreshToken;
import com.union.union.domain.auth.repository.RefreshTokenRepository;
import com.union.union.domain.auth.store.EmailVerificationStore;
import com.union.union.domain.user.entity.User;
import com.union.union.domain.user.repository.UserRepository;
import com.union.union.global.common.exception.DuplicateEmailException;
import com.union.union.global.common.exception.EmailNotVerifiedException;
import com.union.union.global.common.exception.EntityNotFoundException;
import com.union.union.global.common.exception.InvalidRefreshTokenException;
import com.union.union.global.common.exception.UnauthorizedAccessException;
import com.union.union.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationStore verificationStore;
    private final EmailDomainValidationService emailDomainValidationService;

    @Transactional
    public TokenResponseDto signUp(SignUpRequestDto request) {
        // 1. 이메일 중복 확인
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다");
        }

        // 2. 이메일 인증 여부 확인
        if (!verificationStore.isVerified(request.email())) {
            throw new EmailNotVerifiedException("이메일 인증이 완료되지 않았습니다");
        }

        // 3. 이메일 도메인으로 대학교 자동 판별
        String universityName = emailDomainValidationService.validateAndResolve(request.email())
                .getUniversityName();

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .universityName(universityName)
                .build();

        user.verify();
        userRepository.save(user);

        log.info("회원가입 완료. userId={}", user.getId());
        return issueTokens(user);
    }

    @Transactional
    public TokenResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedAccessException("이메일 또는 비밀번호가 올바르지 않습니다"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedAccessException("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        if (user.getUserStatus() == User.UserStatus.WITHDRAWN) {
            throw new UnauthorizedAccessException("탈퇴한 계정입니다");
        }

        if (user.getUserStatus() == User.UserStatus.SUSPENDED) {
            throw new UnauthorizedAccessException("정지된 계정입니다. 관리자에게 문의하세요");
        }

        log.info("로그인 성공. userId={}", user.getId());
        return issueTokens(user);
    }

    // refresh 거부 시에도 의도된 토큰 무효화(재사용 감지/만료/비활성 계정 revoke)는 보존해야 한다.
    // InvalidRefreshTokenException(RuntimeException) 으로 인한 롤백을 막아 revoke 가 커밋되게 한다.
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public TokenResponseDto refresh(RefreshRequestDto request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new InvalidRefreshTokenException("유효하지 않은 refresh token입니다"));

        // 이미 무효화된 토큰 재사용 → 탈취 감지 → 전체 세션 kill
        if (storedToken.isRevoked()) {
            log.warn("Refresh token 재사용 감지! userId={}. 전체 세션 무효화.", storedToken.getUser().getId());
            refreshTokenRepository.revokeAllByUserId(storedToken.getUser().getId());
            throw new InvalidRefreshTokenException("보안상 모든 세션이 로그아웃되었습니다. 다시 로그인해주세요.");
        }

        if (storedToken.isExpired()) {
            storedToken.revoke();
            throw new InvalidRefreshTokenException("세션이 만료되었습니다. 다시 로그인해주세요.");
        }

        User user = storedToken.getUser();

        // 비활성(탈퇴/정지) 계정은 토큰 재발급 차단.
        // login() 과 달리 refresh 는 그동안 상태 검사가 없어, 정지된 사용자(suspend() 는 토큰을
        // revoke 하지 않음)나 탈퇴 직후 race 로 살아남은 토큰으로 access 토큰을 계속 찍어낼 수 있었다.
        // 이 단일 choke point 에서 막는다(매 요청 DB 조회 없이).
        if (user.getUserStatus() != User.UserStatus.ACTIVE) {
            log.warn("비활성 계정의 토큰 갱신 시도 차단. userId={}, status={}", user.getId(), user.getUserStatus());
            refreshTokenRepository.revokeAllByUserId(user.getId());
            throw new InvalidRefreshTokenException("비활성화된 계정입니다. 다시 로그인해주세요.");
        }

        // Rotation: 기존 토큰 무효화
        storedToken.revoke();

        log.info("토큰 갱신. userId={}", user.getId());
        return issueTokens(user);
    }

    @Transactional
    public void logout(UUID userId, String refreshToken) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다"));

        if (refreshToken != null && !refreshToken.isBlank()) {
            // 해당 기기의 refresh 토큰만 무효화한다.
            // - 다른 기기 세션을 끊지 않는다(기기 단위 로그아웃)
            // - 로그아웃 직후 재로그인이 발생해도 새 세션의 토큰은 건드리지 않는다(전체 무효화 race 방지)
            // 본인 토큰일 때만 처리하며, 일치하지 않으면 무시한다(전체 무효화 폴백으로 빠지지 않음).
            refreshTokenRepository.findByToken(refreshToken)
                    .filter(rt -> rt.getUser().getId().equals(userId))
                    .ifPresent(RefreshToken::revoke);
            log.info("로그아웃(기기 단위). userId={}", userId);
        } else {
            // 토큰 미제공(구버전 클라이언트) → 전체 세션 무효화 폴백
            refreshTokenRepository.revokeAllByUserId(userId);
            log.info("로그아웃(전체). userId={}", userId);
        }
    }

    private TokenResponseDto issueTokens(User user) {
        String accessToken = jwtProvider.createAccessToken(
                user.getId(), user.getEmail(), user.getRole().name()
        );
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        long expirationMillis = jwtProvider.getRefreshTokenExpirationMillis();
        RefreshToken tokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(expirationMillis / 1000))
                .build();
        refreshTokenRepository.save(tokenEntity);

        return new TokenResponseDto(accessToken, refreshToken);
    }
}
