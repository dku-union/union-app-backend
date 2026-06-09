package com.union.union.domain.publisher.controller;

import com.union.union.domain.publisher.dto.ApiKeyResponseDto;
import com.union.union.domain.publisher.dto.IssueApiKeyRequestDto;
import com.union.union.domain.publisher.dto.IssueApiKeyResponseDto;
import com.union.union.domain.publisher.service.ApiKeyService;
import com.union.union.global.security.jwt.JwtUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/publishers/me/api-keys")
@RequiredArgsConstructor
public class PublisherApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping
    @PreAuthorize("hasRole('PUBLISHER')")
    public ResponseEntity<IssueApiKeyResponseDto> issue(
            @Valid @RequestBody IssueApiKeyRequestDto request,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        ApiKeyService.IssueResult result = apiKeyService.issue(principal.userId(), request.name());
        return ResponseEntity.ok(new IssueApiKeyResponseDto(
                result.id(),
                result.rawKey(),
                result.keyPrefix(),
                result.name(),
                result.scopes(),
                result.createdAt()
        ));
    }

    @GetMapping
    @PreAuthorize("hasRole('PUBLISHER')")
    public ResponseEntity<List<ApiKeyResponseDto>> list(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        List<ApiKeyResponseDto> keys = apiKeyService.list(principal.userId()).stream()
                .map(ApiKeyResponseDto::from)
                .toList();
        return ResponseEntity.ok(keys);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PUBLISHER')")
    public ResponseEntity<Void> revoke(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        apiKeyService.revoke(principal.userId(), id);
        return ResponseEntity.noContent().build();
    }
}
