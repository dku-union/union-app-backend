package com.union.union.domain.publisher.auth.exception;

import com.union.union.global.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * 퍼블리셔 계정 상태가 ACTIVE가 아닐 때 (PENDING/REJECTED 등).
 */
public class PublisherInactiveException extends BusinessException {

    public PublisherInactiveException(String detail) {
        super(HttpStatus.FORBIDDEN, "PUBLISHER_INACTIVE", detail);
    }
}
