package com.union.union.domain.auth.service;

import com.union.union.domain.auth.dto.*;
import com.union.union.domain.auth.entity.RefreshToken;
import com.union.union.domain.auth.repository.RefreshTokenRepository;
import com.union.union.domain.user.entity.User;
import com.union.union.domain.user.repository.UserRepository;
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

    @Transactional
    public TokenResponseDto signUp(SignUpRequestDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .universityName(request.universityName())
                .build();
        userRepository.save(user);

        log.info("회원가입 완료. userId={}, email={}", user.getId(), user.getEmail());
        return issueTokens(user);
    }

    @Transactional
    public TokenResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        log.info("로그인 성공. userId={}", user.getId());
        return issueTokens(user);
    }

    @Transactional
    public TokenResponseDto refresh(RefreshRequestDto request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 refresh token입니다"));

        // 이미 무효화된 토큰 재사용 → 탈취 감지 → 전체 세션 kill
        if (storedToken.isRevoked()) {
            log.warn("Refresh token 재사용 감지! userId={}. 전체 세션 무효화.", storedToken.getUser().getId());
            refreshTokenRepository.revokeAllByUserId(storedToken.getUser().getId());
            throw new IllegalArgumentException("보안상 모든 세션이 로그아웃되었습니다. 다시 로그인해주세요.");
        }

        if (storedToken.isExpired()) {
            storedToken.revoke();
            throw new IllegalArgumentException("세션이 만료되었습니다. 다시 로그인해주세요.");
        }

        // Rotation: 기존 토큰 무효화
        storedToken.revoke();

        User user = storedToken.getUser();
        log.info("토큰 갱신. userId={}", user.getId());
        return issueTokens(user);
    }

    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("로그아웃. userId={}", userId);
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
