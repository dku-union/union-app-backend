package com.union.union.global.common.exception;

import org.springframework.http.HttpStatus;

public class ExternalServiceException extends BusinessException {

    public ExternalServiceException(String message) {
        super(HttpStatus.BAD_GATEWAY, "EXTERNAL_SERVICE_ERROR", message);
    }
}
