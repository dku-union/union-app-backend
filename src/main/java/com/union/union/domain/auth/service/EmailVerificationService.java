package com.union.union.domain.auth.service;

import com.union.union.domain.auth.store.EmailVerificationStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationStore verificationStore;
    private final MailService mailService;
    private final EmailDomainValidationService emailDomainValidationService;

    private static final long EXPIRE_TIME = 5 * 60 * 1000L; // 5분

    /**
     * 인증 코드 생성 및 발송
     */
    public void sendCode(String email, Long universityId) {
        emailDomainValidationService.validate(email, universityId);

        String code = generateCode();
        verificationStore.save(email, code, System.currentTimeMillis() + EXPIRE_TIME);
        
        mailService.sendVerificationEmail(email, code);
        log.info("인증 코드 발송 완료. email: {}", email);
    }

    /**
     * 인증 코드 검증
     */
    public boolean verifyCode(String email, String code) {
        String savedCode = verificationStore.get(email)
                .orElseThrow(() -> new IllegalArgumentException("만료되었거나 유효하지 않은 인증 정보입니다."));

        if (!savedCode.equals(code)) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }

        verificationStore.delete(email);
        log.info("이메일 인증 성공. email: {}", email);
        return true;
    }

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(1000000));
    }
}
