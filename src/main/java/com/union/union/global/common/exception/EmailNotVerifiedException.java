package com.union.union.global.common.exception;

import org.springframework.http.HttpStatus;

public class EmailNotVerifiedException extends BusinessException {

    public EmailNotVerifiedException(String message) {
        super(HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED", message);
    }
}
