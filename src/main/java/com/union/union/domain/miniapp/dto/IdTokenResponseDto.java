package com.union.union.domain.miniapp.dto;

/**
 * 미니앱 ID 토큰 발급 응답.
 *
 * @param idToken   RS256 서명 ID 토큰 (publisher 백엔드가 JWKS 로 검증)
 * @param tokenType "Bearer"
 * @param expiresIn 만료까지 남은 초
 */
public record IdTokenResponseDto(
    String idToken,
    String tokenType,
    long expiresIn
) {
}
