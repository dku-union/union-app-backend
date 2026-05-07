package com.union.union.domain.review.controller;

import com.union.union.domain.appversion.dto.TestLinkResponseDto;
import com.union.union.domain.review.dto.ReviewDecisionRequestDto;
import com.union.union.domain.review.dto.ReviewResponseDto;
import com.union.union.domain.review.dto.SubmitReviewRequestDto;
import com.union.union.domain.review.service.ReviewService;
import com.union.union.global.security.jwt.JwtUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('PUBLISHER')")
    public ResponseEntity<ReviewResponseDto> submitForReview(
            @Valid @RequestBody SubmitReviewRequestDto request,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        ReviewResponseDto response = reviewService.submitForReview(request.versionId(), principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReviewResponseDto>> getPendingReviews() {
        List<ReviewResponseDto> response = reviewService.getPendingReviews();
        return ResponseEntity.ok(response);
    }

    /**
     * 현재 로그인된 publisher 가 멤버인 모든 워크스페이스의 심사 이력.
     * 반드시 {@code /{id}} 매핑보다 먼저 선언되어 path variable 충돌(`mine` → UUID 변환 실패)을 막는다.
     */
    @GetMapping("/mine")
    @PreAuthorize("hasRole('PUBLISHER') or hasRole('ADMIN')")
    public ResponseEntity<List<ReviewResponseDto>> getMyReviews(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        List<ReviewResponseDto> response = reviewService.getMyReviews(principal.userId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewResponseDto> getReview(@PathVariable UUID id) {
        ReviewResponseDto response = reviewService.getReview(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/test-link")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TestLinkResponseDto> getTestLink(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        TestLinkResponseDto response = reviewService.getTestLinkForReview(id, principal.userId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/decision")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewResponseDto> decide(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewDecisionRequestDto request,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        ReviewResponseDto response = reviewService.decide(id, request, principal.userId());
        return ResponseEntity.ok(response);
    }
}
