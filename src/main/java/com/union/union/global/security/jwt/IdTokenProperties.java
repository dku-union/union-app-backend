package com.union.union.global.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 미니앱 ID 토큰(RS256) 발급 설정.
 *
 * <p>세션 access token(HS256, {@link JwtProperties})과 별개다. ID 토큰은 publisher 자체
 * 백엔드가 사용자 신원을 검증하도록 발급하는 OIDC 스타일 토큰으로, 비대칭 서명이라
 * publisher 는 시크릿 공유 없이 JWKS 공개키로만 검증한다.
 *
 * @param issuer     iss claim 이자 JWKS base URL (publisher 는 {issuer}/.well-known/jwks.json 으로 키 조회)
 * @param expiration 만료(ms)
 * @param kid        JWK key id (토큰 헤더 + JWKS 매칭용)
 * @param privateKey RSA private key (PKCS#8 PEM). prod 는 IDTOKEN_PRIVATE_KEY 로 주입, local 은 dev 키.
 */
@ConfigurationProperties(prefix = "idtoken")
public record IdTokenProperties(
    String issuer,
    long expiration,
    String kid,
    String privateKey
) {
}
