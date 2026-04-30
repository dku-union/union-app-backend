package com.union.union.domain.publisher.auth.dto;

/**
 * 이메일 OTP 발송 응답.
 *
 * @param expiresInSeconds OTP 만료까지 남은 초 (클라이언트 카운트다운 표시용)
 * @param maskedEmail      UI 노출용 마스킹된 이메일 (예: "ju****@dankook.ac.kr")
 */
public record PublisherEmailSendResponseDto(
        int expiresInSeconds,
        String maskedEmail
) {}
