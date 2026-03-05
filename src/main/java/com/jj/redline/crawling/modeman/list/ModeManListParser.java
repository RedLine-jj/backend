package com.jj.redline.crawling.modeman.list;

import com.jj.redline.common.util.MoneyParser;
import com.jj.redline.common.util.UrlNormalizer;
import com.jj.redline.domain.dto.ProductBrief;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

public class ModeManListParser {

    private static final String BASE_URL = "https://mode-man.com";

    public List<ProductBrief> parse(String listHtml) {
        if (listHtml == null || listHtml.isBlank()) return List.of();

        Document doc = Jsoup.parse(listHtml);
        Elements cards = doc.select(ModeManListSelectors.PRODUCT_CARD);

        List<ProductBrief> results = new ArrayList<>();

        for (Element card : cards) {
            ProductBrief brief = extractFromCard(card);
            if (brief != null) results.add(brief);
        }

        return dedupeByUrl(results);
    }

    private ProductBrief extractFromCard(Element card) {

        // URL
        String href = firstAttr(card, ModeManListSelectors.PRODUCT_LINK, "href");
        if (isBlank(href)) return null;

        String url = UrlNormalizer.toAbsoluteUrl(href, BASE_URL);
        if (isBlank(url)) return null;

        // ✅ 여기서 “상품 상세 URL”만 통과
        if (!isProductDetailUrl(url)) return null;

        // 이미지
        String imgSrc = firstNonBlank(
                firstAttr(card, ModeManListSelectors.PRODUCT_IMAGE, "src"),
                firstAttr(card, ModeManListSelectors.PRODUCT_IMAGE, "data-src"),
                firstAttr(card, ModeManListSelectors.PRODUCT_IMAGE, "data-original")
        );
        String imageUrl = UrlNormalizer.toAbsoluteUrl(imgSrc, BASE_URL);

        // 이름
        String name = firstNonBlank(
                firstAttr(card, ModeManListSelectors.PRODUCT_IMAGE, "alt"),
                firstText(card, ModeManListSelectors.PRODUCT_NAME)
        );

        // 브랜드
        String brand = firstText(card, ModeManListSelectors.PRODUCT_BRAND);

        // 가격
        Integer price = extractPrice(card);

        return ProductBrief.builder()
                .url(url)
                .imageUrl(imageUrl)
                .name(blankToNull(name))
                .brand(blankToNull(brand))
                .price(price)
                .build();
    }

    /**
     * 모드맨 “상품 상세”만 true
     * 예: /product/lvc-1953-type-ii-jacket-flippen/14897/category/263/display/1/
     */
    private boolean isProductDetailUrl(String url) {
        if (isBlank(url)) return false;

        // list 페이지, 기타 페이지 제외
        if (url.contains("/product/list.html")) return false;

        // 상세는 보통 category/display를 포함 (실제 샘플/너가 붙인 HTML 기준)
        if (!url.contains("/product/")) return false;
        if (!url.contains("/category/")) return false;
        if (!url.contains("/display/")) return false;

        return true;
    }

    private Integer extractPrice(Element card) {
        // ec-data-price 우선
        Element priceEl = card.selectFirst("[ec-data-price]");
        if (priceEl != null) {
            Integer p = parseIntSafe(priceEl.attr("ec-data-price"));
            if (p != null) return p;
        }

        // 텍스트 가격 fallback
        String priceText = firstText(card, ModeManListSelectors.PRICE_TEXT);
        if (!isBlank(priceText)) {
            return MoneyParser.parseToInt(priceText);
        }

        return null;
    }

    private List<ProductBrief> dedupeByUrl(List<ProductBrief> items) {
        Map<String, ProductBrief> map = new LinkedHashMap<>();
        for (ProductBrief b : items) {
            if (b == null || isBlank(b.getUrl())) continue;
            map.putIfAbsent(b.getUrl(), b);
        }
        return new ArrayList<>(map.values());
    }

    private String firstAttr(Element root, String cssQuery, String attr) {
        Element el = root.selectFirst(cssQuery);
        if (el == null) return null;
        return blankToNull(el.attr(attr));
    }

    private String firstText(Element root, String cssQuery) {
        Element el = root.selectFirst(cssQuery);
        if (el == null) return null;
        return blankToNull(el.text());
    }

    private String firstNonBlank(String... candidates) {
        if (candidates == null) return null;
        for (String c : candidates) {
            if (!isBlank(c)) return c.trim();
        }
        return null;
    }

    private Integer parseIntSafe(String raw) {
        if (isBlank(raw)) return null;
        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}