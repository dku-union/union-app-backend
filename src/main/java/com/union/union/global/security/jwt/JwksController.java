package com.union.union.global.security.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JWKS(JSON Web Key Set) 공개 엔드포인트.
 *
 * <p>publisher 자체 백엔드가 미니앱 ID 토큰을 검증할 때 여기서 RS256 공개키를 조회한다.
 * 공개키만 노출하며, 비활성화 상태(키 미설정)면 빈 키셋을 반환한다.
 */
@RestController
public class JwksController {

    private final String jwksJson;

    public JwksController(AppIdTokenProvider idTokenProvider) {
        if (!idTokenProvider.isEnabled()) {
            this.jwksJson = "{\"keys\":[]}";
            return;
        }
        RSAKey jwk = new RSAKey.Builder(idTokenProvider.getPublicKey())
                .keyID(idTokenProvider.getKid())
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .build()
                .toPublicJWK();
        this.jwksJson = new JWKSet(jwk).toString();
    }

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public String jwks() {
        return jwksJson;
    }
}
