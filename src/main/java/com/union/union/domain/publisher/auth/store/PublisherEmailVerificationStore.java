package com.union.union.domain.publisher.auth.store;

import java.util.Optional;

/**
 * 퍼블리셔 로그인 OTP 저장소 추상화.
 *
 * <p>기존 {@link com.union.union.domain.auth.store.EmailVerificationStore}와 다른 용도:
 * <ul>
 *     <li>signup 인증은 "이메일 소유 증명 → 회원가입" 단계에 사용</li>
 *     <li>본 store는 "이미 가입된 퍼블리셔의 패스워드리스 로그인 OTP"</li>
 * </ul>
 * 키 네임스페이스를 분리해 두 플로우가 서로의 코드를 덮어쓰지 않도록 한다.
 */
public interface PublisherEmailVerificationStore {

    void save(String email, String code, long expireMillisFromEpoch);

    Optional<String> get(String email);

    void delete(String email);
}
