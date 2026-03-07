package com.jj.redline.common.util;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class TimeUtil {

    private TimeUtil() {}

    /**
     * KST 기준 현재 시각 반환
     * 예: 2026-03-01T21:10:00+09:00
     */
    public static OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.of("+9"));
    }

    /**
     * UTC 기준 현재 시각 반환
     * 예: 2026-03-01T12:10:00Z
     */
    public static OffsetDateTime nowUtc() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
