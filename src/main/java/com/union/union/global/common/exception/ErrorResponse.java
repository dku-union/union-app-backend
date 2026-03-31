package com.union.union.global.common.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        String error,
        String code,
        String message,
        String timestamp
) {
    public static ErrorResponse from(BusinessException exception) {
        return new ErrorResponse(
                exception.getStatus().value(),
                exception.getStatus().getReasonPhrase(),
                exception.getCode(),
                exception.getMessage(),
                LocalDateTime.now().toString()
        );
    }

    public static ErrorResponse of(int status, String error, String code, String message) {
        return new ErrorResponse(status, error, code, message, LocalDateTime.now().toString());
    }
}
