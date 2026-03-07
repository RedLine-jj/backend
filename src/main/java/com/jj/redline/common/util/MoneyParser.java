package com.jj.redline.common.util;

/**
 * 금액 문자열 -> 숫자 변환
 */
public final class MoneyParser {

    private MoneyParser() {}

    /**
     * 금액 문자열을 Long으로 변환
     */
    public static Long parseToLong(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 금액 문자열을 Integer로 변환
     */
    public static Integer parseToInt(String raw) {
        Long v = parseToLong(raw);
        if (v == null) return null;

        if (v > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return v.intValue();
    }
}
