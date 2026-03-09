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

    public ListParseResult parse(String listHtml) {
        if (listHtml == null || listHtml.isBlank()) {
            return ListParseResult.builder().productBriefs(List.of()).build();
        }

        Document doc = Jsoup.parse(listHtml);
        Elements cards = doc.select(ModeManListSelectors.PRODUCT_CARD);

        List<ProductBrief> results = new ArrayList<>();

        for (Element card : cards) {
            ProductBrief brief = extractFromCard(card);
            if (brief != null) results.add(brief);
        }

        return ListParseResult.builder()
                .productBriefs(dedupeByUrl(results))
                .build();
    }

    private ProductBrief extractFromCard(Element card) {

        String href = firstAttr(card, ModeManListSelectors.PRODUCT_LINK, "href");
        if (isBlank(href)) return null;

        String url = UrlNormalizer.toAbsoluteUrl(href, BASE_URL);
        if (isBlank(url)) return null;

        if (!isProductDetailUrl(url)) return null;

        String imgSrc = firstNonBlank(
                firstAttr(card, ModeManListSelectors.PRODUCT_IMAGE, "src"),
                firstAttr(card, ModeManListSelectors.PRODUCT_IMAGE, "data-src"),
                firstAttr(card, ModeManListSelectors.PRODUCT_IMAGE, "data-original")
        );
        String imageUrl = UrlNormalizer.toAbsoluteUrl(imgSrc, BASE_URL);

        String name = firstNonBlank(
                firstAttr(card, ModeManListSelectors.PRODUCT_IMAGE, "alt"),
                firstText(card, ModeManListSelectors.PRODUCT_NAME)
        );

        String brand = firstText(card, ModeManListSelectors.PRODUCT_BRAND);
        
        Long price = extractPrice(card);

        return ProductBrief.builder()
                .url(url)
                .imageUrl(imageUrl)
                .name(blankToNull(name))
                .brand(blankToNull(brand))
                .price(price)
                .build();
    }

    private boolean isProductDetailUrl(String url) {
        if (isBlank(url)) return false;
        if (url.contains("/product/list.html")) return false;
        if (!url.contains("/product/")) return false;
        if (!url.contains("/category/")) return false;
        if (!url.contains("/display/")) return false;
        return true;
    }

    // [수정] 가격 추출 로직 안정성 강화
    private Long extractPrice(Element card) {
        // 1. 가격 정보를 담고 있을 가능성이 높은 요소를 먼저 찾음
        Element priceHolder = card.selectFirst(ModeManListSelectors.PRICE_TEXT);

        if (priceHolder != null) {
            // 1a. 해당 요소에 ec-data-price 속성이 있는지 확인
            String priceAttr = priceHolder.attr("ec-data-price");
            if (!isBlank(priceAttr)) {
                Long price = parseLongSafe(priceAttr);
                if (price != null) return price;
            }

            // 1b. 속성이 없다면, 요소의 텍스트에서 가격을 파싱
            String priceText = priceHolder.text();
            if (!isBlank(priceText)) {
                return MoneyParser.parseToLong(priceText);
            }
        }
        
        // 2. 최후의 수단으로 카드 전체에서 ec-data-price 속성을 다시 검색
        Element priceEl = card.selectFirst(ModeManListSelectors.PRICE_WITH_ATTR);
        if (priceEl != null) {
            String priceAttr = priceEl.attr("ec-data-price");
            if (!isBlank(priceAttr)) {
                return parseLongSafe(priceAttr);
            }
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
    
    private Long parseLongSafe(String raw) {
        if (isBlank(raw)) return null;
        try {
            return Long.parseLong(raw.trim());
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