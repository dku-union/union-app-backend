package com.union.union.domain.publisher.auth.service;

/**
 * 이메일 로컬파트를 마스킹한다.
 *
 * <pre>
 *   junseo@dankook.ac.kr  → ju****@dankook.ac.kr
 *   ab@x.com              → a*@x.com
 *   a@x.com               → *@x.com
 * </pre>
 *
 * UI에서 인증번호 발송 안내 문구에 노출되며, 사용자가 다른 계정의 이메일이 아닌
 * 자기 계정에 발송되었음을 즉시 식별할 수 있도록 돕는 목적.
 */
public final class EmailMasker {

    private EmailMasker() {}

    public static String mask(String email) {
        if (email == null) return "";
        int at = email.indexOf('@');
        if (at <= 0) return email;

        String local = email.substring(0, at);
        String domain = email.substring(at);

        if (local.length() == 1) {
            return "*" + domain;
        }
        if (local.length() == 2) {
            return local.charAt(0) + "*" + domain;
        }

        int visible = Math.min(2, local.length() - 1);
        return local.substring(0, visible) + "****" + domain;
    }
}
