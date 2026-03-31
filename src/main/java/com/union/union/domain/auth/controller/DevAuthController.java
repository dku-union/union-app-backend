package com.union.union.domain.auth.controller;

import com.union.union.domain.auth.dto.TokenResponseDto;
import com.union.union.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 개발 환경 전용 — local 프로필에서만 활성화
 */
@Profile("local")
@RestController
@RequestMapping("/api/dev/auth")
@RequiredArgsConstructor
public class DevAuthController {

    private final JwtProvider jwtProvider;

    /**
     * 테스트용 토큰 즉시 발급 (인증/회원가입 불필요)
     *
     * GET /api/dev/auth/token?role=ROLE_USER
     * GET /api/dev/auth/token?role=ROLE_PUBLISHER
     * GET /api/dev/auth/token?role=ROLE_ADMIN
     */
    @GetMapping("/token")
    public ResponseEntity<TokenResponseDto> issueTestToken(
            @RequestParam(defaultValue = "ROLE_USER") String role
    ) {
        UUID fakeUserId = UUID.fromString("66d1bf78-29b5-45d8-bba7-f08f88bffa23");
        String accessToken = jwtProvider.createAccessToken(fakeUserId, "test@dankook.ac.kr", role);
        String refreshToken = jwtProvider.createRefreshToken(fakeUserId);

        return ResponseEntity.ok(new TokenResponseDto(accessToken, refreshToken));
    }
}
