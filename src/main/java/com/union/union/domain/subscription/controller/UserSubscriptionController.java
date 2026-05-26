package com.union.union.domain.subscription.controller;

import com.union.union.domain.subscription.dto.SubscriptionResponseDto;
import com.union.union.domain.subscription.dto.UpdatePushEnabledRequestDto;
import com.union.union.domain.subscription.service.SubscriptionService;
import com.union.union.global.security.jwt.JwtUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserSubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/api/v1/users/me/miniapps/{appId}/subscription")
    public ResponseEntity<Void> subscribe(
            @PathVariable String appId,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        subscriptionService.subscribeByAppId(principal.userId(), appId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/api/v1/users/me/miniapps/{appId}/subscription")
    public ResponseEntity<Void> updatePushEnabled(
            @PathVariable String appId,
            @Valid @RequestBody UpdatePushEnabledRequestDto request,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        subscriptionService.setPushEnabled(principal.userId(), appId, request.pushEnabled());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/v1/users/me/miniapps/{appId}/subscription")
    public ResponseEntity<Void> unsubscribe(
            @PathVariable String appId,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        subscriptionService.unsubscribe(principal.userId(), appId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/users/me/subscriptions")
    public ResponseEntity<List<SubscriptionResponseDto>> list(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        List<SubscriptionResponseDto> subs = subscriptionService.listActiveByUser(principal.userId()).stream()
                .map(SubscriptionResponseDto::from)
                .toList();
        return ResponseEntity.ok(subs);
    }
}
