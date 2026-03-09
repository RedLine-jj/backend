package com.jj.redline.common.util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * URL 정규화 유틸
 * - "//mode-man.com/..."  -> "https://mode-man.com/..."
 * - "/product/..."        -> "https://mode-man.com/product/..."
 */
public final class UrlNormalizer {

    private UrlNormalizer() {}

    /**
     * @param rawUrl     HTML에서 추출한 URL(상대경로, // 시작, 절대경로 모두 가능)
     * @param baseOrigin 예: "https://mode-man.com"
     */
    public static String toAbsoluteUrl(String rawUrl, String baseOrigin) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return null;
        }

        String u = rawUrl.trim().replace(" ", "%20");

        // protocol-relative
        if (u.startsWith("//")) {
            return "https:" + u;
        }

        // already absolute
        if (u.startsWith("http://") || u.startsWith("https://")) {
            return sanitizeDoubleHttps(u);
        }

        // relative path
        if (u.startsWith("/")) {
            String origin = normalizeOrigin(baseOrigin);
            return origin + u;
        }

        // 기타 케이스(예: "product/..")는 baseOrigin 기준으로 resolve
        try {
            URI base = new URI(normalizeOrigin(baseOrigin) + "/");
            URI resolved = base.resolve(u);
            return sanitizeDoubleHttps(resolved.toString());
        } catch (URISyntaxException e) {
            // 실패 시 원본 반환(최소한 깨지지 않게)
            return sanitizeDoubleHttps(u);
        }
    }

    private static String normalizeOrigin(String baseOrigin) {
        if (baseOrigin == null || baseOrigin.isBlank()) {
            throw new IllegalArgumentException("baseOrigin is required");
        }
        String origin = baseOrigin.trim();
        // 끝에 / 제거
        while (origin.endsWith("/")) {
            origin = origin.substring(0, origin.length() - 1);
        }
        return origin;
    }

    /**
     * 일부 JSON-LD에서 "https:https://..." 같이 중복 prefix가 나오는 케이스 방어.
     */
    private static String sanitizeDoubleHttps(String url) {
        if (url == null) return null;
        String u = url.trim();
        if (u.startsWith("https:https://")) {
            return u.replaceFirst("^https:https://", "https://");
        }
        if (u.startsWith("http:http://")) {
            return u.replaceFirst("^http:http://", "http://");
        }
        if (u.startsWith("http:https://")) {
            return u.replaceFirst("^http:https://", "https://");
        }
        return u;
    }
}