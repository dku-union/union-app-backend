package com.union.union.domain.miniapp.utils;

public class KoreanUtils {

    private static final char HANGUL_BEGIN_UNICODE = 0xAC00; // 가
    private static final char HANGUL_END_UNICODE = 0xD7A3; // 힣
    private static final char HANGUL_BASE_UNIT = 588; // 21 * 28

    private static final char[] INITIAL_SOUND = {
            'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };
    private static final char[] MEDIAL_SOUND = {
            'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
    };
    private static final char[] FINAL_SOUND = {
            ' ', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    /**
     * 한글 문자열을 자모 단위로 분해합니다.
     * 예: "학생회" -> "ㅎㅏㄱㅅㅐㅇㅎㅚ"
     * 
     * @param text 분해할 문자열
     * @return 분해된 자모 문자열
     */
    public static String decompose(String text) {
        if (text == null) return null;

        StringBuilder result = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (c >= HANGUL_BEGIN_UNICODE && c <= HANGUL_END_UNICODE) {
                int index = c - HANGUL_BEGIN_UNICODE;
                int initial = index / HANGUL_BASE_UNIT;
                int medial = (index % HANGUL_BASE_UNIT) / 28;
                int finalSound = index % 28;

                result.append(INITIAL_SOUND[initial]);
                result.append(MEDIAL_SOUND[medial]);
                if (finalSound != 0) {
                    result.append(FINAL_SOUND[finalSound]);
                }
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
}
