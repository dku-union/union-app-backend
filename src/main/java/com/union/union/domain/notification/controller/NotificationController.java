package com.union.union.domain.notification.controller;

import com.union.union.domain.notification.dto.*;
import com.union.union.domain.notification.service.NotificationService;
import com.union.union.global.security.jwt.JwtUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PutMapping("/token")
    public ResponseEntity<Void> registerToken(
            @Valid @RequestBody RegisterTokenRequestDto request,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        String role = principal.role();
        if ("ROLE_PUBLISHER".equals(role) || "ROLE_ADMIN".equals(role)) {
            notificationService.upsertPublisherToken(principal.userId(), request);
        } else {
            notificationService.upsertUserToken(principal.userId(), request);
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/token")
    public ResponseEntity<Void> deleteToken(
            @RequestBody DeleteTokenRequestDto request,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        notificationService.deleteToken(principal.userId(), principal.role(), request.token(), request.deviceId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/inbox")
    public ResponseEntity<List<InboxResponseDto>> getInbox(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        List<InboxResponseDto> response = notificationService.getInbox(principal.userId(), cursor, limit);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/inbox/{id}/read")
    public ResponseEntity<Void> markRead(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        notificationService.markRead(id, principal.userId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/inbox/read-all")
    public ResponseEntity<Void> markAllRead(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        notificationService.markAllRead(principal.userId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        long count = notificationService.getUnreadCount(principal.userId());
        return ResponseEntity.ok(Map.of("count", count));
    }
}
