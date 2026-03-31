package com.union.union.global.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedAccessException extends BusinessException {

    public UnauthorizedAccessException(String message) {
        super(HttpStatus.FORBIDDEN, "UNAUTHORIZED_ACCESS", message);
    }
}
