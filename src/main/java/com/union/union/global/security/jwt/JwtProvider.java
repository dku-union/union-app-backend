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
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class JwtProvider {

    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtProvider(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = properties.accessTokenExpiration();
        this.refreshTokenExpiration = properties.refreshTokenExpiration();
    }

    public String createAccessToken(UUID userId, String email, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpiration))
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(UUID userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExpiration))
                .signWith(key)
                .compact();
    }

    public long getRefreshTokenExpirationMillis() {
        return refreshTokenExpiration;
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        String role = claims.get("role", String.class);
        var authorities = List.of(new SimpleGrantedAuthority(role));
        var principal = new JwtUserPrincipal(
                UUID.fromString(claims.getSubject()),
                claims.get("email", String.class),
                role
        );
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("Expired JWT token");
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }

    public UUID getUserIdFromToken(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
