package com.union.union.domain.publisher.auth.controller;

import com.union.union.domain.auth.dto.RefreshRequestDto;
import com.union.union.domain.publisher.auth.dto.PublisherEmailSendRequestDto;
import com.union.union.domain.publisher.auth.dto.PublisherEmailSendResponseDto;
import com.union.union.domain.publisher.auth.dto.PublisherEmailVerifyRequestDto;
import com.union.union.domain.publisher.auth.dto.PublisherTokenResponseDto;
import com.union.union.domain.publisher.auth.service.PublisherAuthService;
import com.union.union.global.security.jwt.JwtUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * iOS 퍼블리셔 패스워드리스 로그인 API.
 *
 * <ul>
 *     <li>POST {@code /api/v1/auth/publisher/email/send}</li>
 *     <li>POST {@code /api/v1/auth/publisher/email/verify}</li>
 *     <li>POST {@code /api/v1/auth/publisher/refresh}</li>
 *     <li>POST {@code /api/v1/auth/publisher/logout}</li>
 * </ul>
 *
 * 모든 send/verify/refresh는 {@code SecurityConfig}에서 permitAll로 노출되며,
 * rate-limit는 {@code RateLimitFilter}가 적용한다.
 */
@RestController
@RequestMapping("/api/v1/auth/publisher")
@RequiredArgsConstructor
public class PublisherAuthController {

    private final PublisherAuthService publisherAuthService;

    @PostMapping("/email/send")
    public ResponseEntity<PublisherEmailSendResponseDto> sendCode(
            @Valid @RequestBody PublisherEmailSendRequestDto request
    ) {
        return ResponseEntity.ok(publisherAuthService.sendCode(request.email()));
    }

    @PostMapping("/email/verify")
    public ResponseEntity<PublisherTokenResponseDto> verifyCode(
            @Valid @RequestBody PublisherEmailVerifyRequestDto request
    ) {
        return ResponseEntity.ok(
                publisherAuthService.verifyAndIssueTokens(request.email(), request.code())
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<PublisherTokenResponseDto> refresh(
            @Valid @RequestBody RefreshRequestDto request
    ) {
        return ResponseEntity.ok(publisherAuthService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    @PreAuthorize("hasRole('PUBLISHER')")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal JwtUserPrincipal principal) {
        publisherAuthService.logout(principal.userId());
        return ResponseEntity.noContent().build();
    }
}
