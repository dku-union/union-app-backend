package com.union.union.domain.publisher.auth.exception;

import com.union.union.global.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * 입력된 전화번호로 등록된 퍼블리셔가 없을 때.
 * iOS 클라이언트가 이 코드를 받으면 "유니온 웹사이트에서 회원가입" 안내 alert을 띄운다.
 */
public class PublisherNotRegisteredException extends BusinessException {

    public PublisherNotRegisteredException() {
        super(HttpStatus.NOT_FOUND, "PUBLISHER_NOT_REGISTERED",
                "등록되지 않은 계정입니다. 유니온 웹사이트에서 먼저 회원가입 해주세요.");
    }
}
