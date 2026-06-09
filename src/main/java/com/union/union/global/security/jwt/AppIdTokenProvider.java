package com.union.union.global.security.jwt;

import com.union.union.domain.miniapp.entity.PermissionScope;
import com.union.union.domain.user.entity.User;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Set;

/**
 * 미니앱 ID 토큰(RS256) 발급기. {@link JwtProvider}(세션 토큰, HS256)와 평행 구조다.
 *
 * <p>발급한 토큰은 publisher 자체 백엔드가 {@code /.well-known/jwks.json} 공개키로 검증한다.
 * {@code aud}=appId 로 스코프되어 Union 1st-party API 에는 통하지 않는다(세션 토큰과 분리).
 */
@Slf4j
@Component
public class AppIdTokenProvider {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final String issuer;
    private final String kid;
    private final long expiration;
    private final boolean enabled;

    public AppIdTokenProvider(IdTokenProperties properties) {
        this.issuer = properties.issuer();
        this.kid = properties.kid();
        this.expiration = properties.expiration();

        // 키 미설정이면 부팅을 막지 않고 기능만 비활성화한다(기존 prod 무중단 배포 보호).
        // IDTOKEN_PRIVATE_KEY 가 주입되면 ID 토큰 발급/JWKS 가 활성화된다.
        if (properties.privateKey() == null || properties.privateKey().isBlank()) {
            log.warn("idtoken.private-key 미설정 — 미니앱 ID 토큰 발급/JWKS 비활성화 상태로 기동");
            this.privateKey = null;
            this.publicKey = null;
            this.enabled = false;
            return;
        }
        // 키가 있으면 활성화 — issuer/kid/expiration 도 유효해야 검증 가능한 토큰이 나온다.
        if (issuer == null || issuer.isBlank() || kid == null || kid.isBlank() || expiration <= 0) {
            throw new IllegalStateException(
                    "idtoken issuer/kid/expiration 설정이 유효하지 않습니다 (issuer/kid 비어있거나 expiration<=0)");
        }
        RSAPrivateCrtKey crtKey = parsePrivateKey(properties.privateKey());
        this.privateKey = crtKey;
        this.publicKey = derivePublicKey(crtKey);
        this.enabled = true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 사용자 신원을 담은 ID 토큰을 미니앱(appId) 스코프로 발급한다.
     * {@code sub}(userId)는 항상 포함하고, 프로필 claim 은 사용자가 동의한 scope 에 한해 추가한다.
     */
    public String createIdToken(User user, String appId, Set<PermissionScope> grantedScopes) {
        if (!enabled) {
            throw new IllegalStateException("미니앱 ID 토큰 발급이 비활성화되어 있습니다 (idtoken.private-key 미설정)");
        }
        Date now = new Date();
        var builder = Jwts.builder()
                .header().keyId(kid).and()
                .issuer(issuer)
                .audience().add(appId).and()
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .claim("token_use", "id");

        if (grantedScopes.contains(PermissionScope.USER_PROFILE)) {
            builder.claim("nickname", user.getNickname());
            if (user.getProfileImage() != null) {
                builder.claim("profileImage", user.getProfileImage());
            }
        }
        if (grantedScopes.contains(PermissionScope.USER_EMAIL)) {
            builder.claim("email", user.getEmail());
        }
        if (grantedScopes.contains(PermissionScope.USER_UNIVERSITY) && user.getUniversityName() != null) {
            builder.claim("university", user.getUniversityName());
        }

        return builder.signWith(privateKey, Jwts.SIG.RS256).compact();
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public String getKid() {
        return kid;
    }

    private static RSAPrivateCrtKey parsePrivateKey(String pem) {
        if (pem == null || pem.isBlank()) {
            throw new IllegalStateException("idtoken.private-key 가 설정되지 않았습니다 (IDTOKEN_PRIVATE_KEY)");
        }
        String base64 = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        try {
            byte[] der = Base64.getDecoder().decode(base64);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return (RSAPrivateCrtKey) factory.generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (Exception e) {
            throw new IllegalStateException("idtoken RSA private key 파싱 실패 (PKCS#8 PEM 이어야 함)", e);
        }
    }

    private static RSAPublicKey derivePublicKey(RSAPrivateCrtKey crtKey) {
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) factory.generatePublic(
                    new RSAPublicKeySpec(crtKey.getModulus(), crtKey.getPublicExponent()));
        } catch (Exception e) {
            throw new IllegalStateException("idtoken RSA public key 유도 실패", e);
        }
    }
}
