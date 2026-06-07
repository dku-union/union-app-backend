package com.union.union.global.security.jwt;

import com.union.union.domain.miniapp.entity.PermissionScope;
import com.union.union.domain.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppIdTokenProviderTest {

    private static final String ISSUER = "https://union-api.test";
    private static final String KID = "test-kid-1";
    private static final String APP_ID = "com.union.taxi-pot";

    private AppIdTokenProvider provider;
    private User user;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair keyPair = gen.generateKeyPair();
        String pem = "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getMimeEncoder().encodeToString(keyPair.getPrivate().getEncoded())
                + "\n-----END PRIVATE KEY-----";

        provider = new AppIdTokenProvider(new IdTokenProperties(ISSUER, 3_600_000L, KID, pem));

        user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getNickname()).thenReturn("준서");
        when(user.getEmail()).thenReturn("test@dankook.ac.kr");
        when(user.getProfileImage()).thenReturn(null);
        when(user.getUniversityName()).thenReturn("단국대학교");
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(provider.getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Test
    void 토큰은_공개키로_검증되고_핵심_claim을_담는다() {
        String token = provider.createIdToken(user, APP_ID, Set.of());

        Claims claims = parse(token);
        assertThat(claims.getIssuer()).isEqualTo(ISSUER);
        assertThat(claims.getAudience()).contains(APP_ID);
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("token_use", String.class)).isEqualTo("id");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void user_profile_동의시_nickname을_포함한다() {
        String token = provider.createIdToken(user, APP_ID, Set.of(PermissionScope.USER_PROFILE));
        assertThat(parse(token).get("nickname", String.class)).isEqualTo("준서");
    }

    @Test
    void 동의하지_않은_scope의_claim은_제외된다() {
        // user.profile / user.email 미동의 → nickname/email 없음, university 만 동의
        String token = provider.createIdToken(user, APP_ID, Set.of(PermissionScope.USER_UNIVERSITY));

        Claims claims = parse(token);
        assertThat(claims.get("nickname")).isNull();
        assertThat(claims.get("email")).isNull();
        assertThat(claims.get("university", String.class)).isEqualTo("단국대학교");
    }

    @Test
    void 헤더에_kid가_있어_JWKS와_매칭된다() {
        String token = provider.createIdToken(user, APP_ID, Set.of());
        String headerJson = new String(Base64.getUrlDecoder().decode(token.split("\\.")[0]));
        assertThat(headerJson).contains("\"kid\":\"" + KID + "\"");
        assertThat(headerJson).contains("\"alg\":\"RS256\"");
    }
}
