package com.union.union.domain.notification.controller;

import com.union.union.domain.notification.dto.SendNotificationRequestDto;
import com.union.union.domain.notification.service.NotificationService;
import com.union.union.global.security.jwt.JwtUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> sendSystemNotification(
            @Valid @RequestBody SendNotificationRequestDto request,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        notificationService.sendSystemNotification(request, principal.userId());
        return ResponseEntity.ok().build();
    }
}
