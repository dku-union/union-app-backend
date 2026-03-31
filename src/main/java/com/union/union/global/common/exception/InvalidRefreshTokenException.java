package com.union.union.global.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends BusinessException {

    public InvalidRefreshTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", message);
    }
}
