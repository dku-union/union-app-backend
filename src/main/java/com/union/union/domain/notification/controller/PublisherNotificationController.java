package com.union.union.domain.notification.controller;

import com.union.union.domain.notification.dto.PublisherSendNotificationRequest;
import com.union.union.domain.notification.dto.PublisherSendNotificationResponse;
import com.union.union.domain.notification.service.NotificationService;
import com.union.union.global.security.apikey.PublisherApiKeyPrincipal;
import com.union.union.global.security.jwt.JwtUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PublisherNotificationController {

    private final NotificationService notificationService;

    /**
     * Publisher 백엔드 → Spring (X-Union-Api-Key 헤더로 인증).
     * 미니앱 구독자에게 푸시 알림을 발송.
     */
    @PostMapping("/api/v1/publishers/notifications")
    @PreAuthorize("hasAuthority('SCOPE_notifications:send')")
    public ResponseEntity<PublisherSendNotificationResponse> sendWithApiKey(
            @Valid @RequestBody PublisherSendNotificationRequest request,
            @AuthenticationPrincipal PublisherApiKeyPrincipal principal
    ) {
        PublisherSendNotificationResponse result =
                notificationService.sendByPublisher(principal.publisherId(), request);
        return ResponseEntity.ok(result);
    }

    /**
     * 대시보드 → Spring (JWT 인증). API Key 발급 전 테스트용.
     */
    @PostMapping("/api/v1/publishers/me/notifications")
    @PreAuthorize("hasRole('PUBLISHER')")
    public ResponseEntity<PublisherSendNotificationResponse> sendAsAuthenticatedPublisher(
            @Valid @RequestBody PublisherSendNotificationRequest request,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        PublisherSendNotificationResponse result =
                notificationService.sendByPublisher(principal.userId(), request);
        return ResponseEntity.ok(result);
    }
}
