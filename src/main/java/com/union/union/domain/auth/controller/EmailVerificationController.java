package com.union.union.domain.auth.controller;

import com.union.union.domain.auth.dto.EmailSendRequestDto;
import com.union.union.domain.auth.dto.EmailVerifyRequestDto;
import com.union.union.domain.auth.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth/email")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService verificationService;

    /**
     * 인증 코드 발송 — 이메일 도메인으로 대학교 자동 판별
     * 응답: { "universityName": "단국대학교" }
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendCode(@Valid @RequestBody EmailSendRequestDto request) {
        String universityName = verificationService.sendCode(request.email());
        return ResponseEntity.ok(Map.of("universityName", universityName));
    }

    /**
     * 인증 코드 검증
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyCode(@Valid @RequestBody EmailVerifyRequestDto request) {
        verificationService.verifyCode(request.email(), request.code());
        return ResponseEntity.ok(Map.of("message", "이메일 인증이 완료되었습니다."));
    }
}
