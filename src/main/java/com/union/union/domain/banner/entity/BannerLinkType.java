package com.union.union.domain.banner.entity;

public enum BannerLinkType {
    /** 클릭 액션 없음 — 단순 노출 배너 */
    NONE,
    /** 미니앱 실행 — link_target 은 MiniApp.id (Long) 의 문자열 표현 */
    MINI_APP,
    /** 외부 URL — link_target 은 https URL */
    EXTERNAL_URL
}
