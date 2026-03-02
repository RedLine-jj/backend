package com.jj.redline.common.util;

/**
 * 금액 문자열 -> 정수 변환
 * 예)
 * - "w378,000" -> 378000
 * - "378,000원" -> 378000
 * - "378000" -> 378000
 */
public final class MoneyParser {

    private MoneyParser() {}

    public static Integer parseToInt(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        // 숫자만 남기기 (콤마/원/통화기호/공백 등 제거)
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.isBlank()) {
            return null;
        }

        try {
            // 가격이 int 범위를 넘을 일은 거의 없지만 방어적으로 처리
            long v = Long.parseLong(digits);
            if (v > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            return (int) v;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}