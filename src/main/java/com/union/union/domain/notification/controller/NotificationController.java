package com.union.union.domain.notification.controller;

import com.union.union.domain.notification.dto.FcmTokenRequestDto;
import com.union.union.domain.notification.service.NotificationService;
import com.union.union.global.security.jwt.JwtUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/token")
    public ResponseEntity<Void> registerToken(
            @Valid @RequestBody FcmTokenRequestDto request,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        if (principal.role().contains("PUBLISHER") || principal.role().contains("ADMIN")) {
            notificationService.registerPublisherToken(principal.userId(), request.token());
        } else {
            notificationService.registerUserToken(principal.userId(), request.token());
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/token")
    public ResponseEntity<Void> deleteToken(
            @Valid @RequestBody FcmTokenRequestDto request
    ) {
        notificationService.deleteToken(request.token());
        return ResponseEntity.noContent().build();
    }
}
