package com.union.union.global.security.jwt;

import java.util.UUID;

public record JwtUserPrincipal(
    UUID userId,
    String email,
    String role
) {
}
