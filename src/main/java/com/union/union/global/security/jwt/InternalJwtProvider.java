package com.union.union.global.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * BFF(Next.js) → Spring 내부 통신용 JWT Provider.
 * NextAuth 세션의 publisherId, role로 생성된 30초짜리 Internal JWT를 검증한다.
 */
@Slf4j
@Component
public class InternalJwtProvider {

    private final SecretKey key;

    public InternalJwtProvider(InternalJwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        String role = claims.get("role", String.class);
        var authorities = List.of(new SimpleGrantedAuthority(role));
        var principal = new JwtUserPrincipal(
                UUID.fromString(claims.getSubject()),
                null,
                role
        );
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("Expired internal JWT token");
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid internal JWT token: {}", e.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
