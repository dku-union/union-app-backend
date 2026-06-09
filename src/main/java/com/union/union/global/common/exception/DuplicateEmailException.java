package com.union.union.global.common.exception;

import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends BusinessException {

    public DuplicateEmailException(String message) {
        super(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", message);
    }
}
