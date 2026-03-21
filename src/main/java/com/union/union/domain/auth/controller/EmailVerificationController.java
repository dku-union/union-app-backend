package com.union.union.domain.auth.controller;

import com.union.union.domain.auth.dto.EmailSendRequestDto;
import com.union.union.domain.auth.dto.EmailVerifyRequestDto;
import com.union.union.domain.auth.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/email")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService verificationService;

    /**
     * 인증 코드 발송
     */
    @PostMapping("/send")
    public ResponseEntity<String> sendCode(@RequestBody EmailSendRequestDto request) {
        verificationService.sendCode(request.email());
        return ResponseEntity.ok("인증 코드가 발송되었습니다.");
    }

    /**
     * 인증 코드 검증
     */
    @PostMapping("/verify")
    public ResponseEntity<String> verifyCode(@RequestBody EmailVerifyRequestDto request) {
        verificationService.verifyCode(request.email(), request.code());
        return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
    }
}
