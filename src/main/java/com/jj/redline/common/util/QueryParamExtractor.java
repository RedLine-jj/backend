package com.jj.redline.common.util;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * URL에서 쿼리 파라미터 추출
 * 예) https://mode-man.com/product/detail.html?product_no=8128&item_code=P0000MAQ000A
 *  - item_code -> P0000MAQ000A
 */
public final class QueryParamExtractor {

    private QueryParamExtractor() {}

    /**
     * 쿼리 파라미터 값을 추출하여 반환 (없으면 null)
     * @param url URL
     * @param key 파라미터 키
     * @return 파라미터 값
     */
    public static String extract(String url, String key) {
        return getQueryParam(url, key).orElse(null);
    }

    public static Optional<String> getQueryParam(String url, String key) {
        if (url == null || url.isBlank() || key == null || key.isBlank()) {
            return Optional.empty();
        }

        try {
            URI uri = URI.create(url.trim());
            String query = uri.getRawQuery();
            if (query == null || query.isBlank()) {
                return Optional.empty();
            }

            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf('=');
                if (idx < 0) continue;

                String k = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                if (!key.equals(k)) continue;

                String v = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                return Optional.ofNullable(v).filter(s -> !s.isBlank());
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
