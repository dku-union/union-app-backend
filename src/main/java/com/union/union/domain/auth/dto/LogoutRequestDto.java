package com.union.union.domain.auth.dto;

/**
 * 로그아웃 요청. refreshToken 이 주어지면 그 토큰(= 해당 기기 세션)만 무효화하고,
 * 없으면(구버전 클라이언트) 사용자의 모든 refresh 토큰을 무효화한다.
 */
public record LogoutRequestDto(
        String refreshToken
) {
}
