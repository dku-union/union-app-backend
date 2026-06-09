package com.union.union.global.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "internal-jwt")
public record InternalJwtProperties(
    String secret
) {
}
