package com.union.union.domain.auth.store;

import java.util.Optional;

public interface EmailVerificationStore {
    /**
     * 인증 코드 저장
     * @param email 이메일
     * @param code 인증 코드
     * @param expireTimeMillis 만료 시간 (밀리초)
     */
    void save(String email, String code, long expireTimeMillis);

    /**
     * 인증 코드 조회
     * @param email 이메일
     * @return 인증 코드 (있을 경우)
     */
    Optional<String> get(String email);

    /**
     * 인증 코드 삭제
     * @param email 이메일
     */
    void delete(String email);

    /**
     * 이메일을 인증 완료 상태로 표시
     * @param email 이메일
     */
    void markAsVerified(String email);

    /**
     * 이메일이 인증 완료된 상태인지 확인
     * @param email 이메일
     * @return 인증 완료 여부
     */
    boolean isVerified(String email);
}
