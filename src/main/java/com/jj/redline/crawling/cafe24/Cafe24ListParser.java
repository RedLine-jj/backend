package com.jj.redline.crawling.cafe24;

import com.jj.redline.common.util.MoneyParser;
import com.jj.redline.common.util.UrlNormalizer;
import com.jj.redline.domain.dto.crawl.CrawlSiteConfig;
import com.jj.redline.domain.dto.crawl.ListParseResult;
import com.jj.redline.domain.dto.crawl.ProductBrief;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Cafe24ListParser {

    private final String baseUrl;

    public Cafe24ListParser(CrawlSiteConfig siteConfig) {
        this.baseUrl = "https://" + siteConfig.getDomain();
    }

    public ListParseResult parse(String listHtml) {
        if (listHtml == null || listHtml.isBlank()) {
            return ListParseResult.builder().productBriefs(List.of()).build();
        }

        Document doc = Jsoup.parse(listHtml);
        Elements cards = doc.select(Cafe24ListSelectors.PRODUCT_CARD);

        List<ProductBrief> results = new ArrayList<>();

        for (Element card : cards) {
            ProductBrief brief = extractFromCard(card);
            if (brief != null) {
                results.add(brief);
            }
        }

        return ListParseResult.builder()
                .productBriefs(dedupeByUrl(results))
                .build();
    }

    private ProductBrief extractFromCard(Element card) {
        String href = firstAttr(card, Cafe24ListSelectors.PRODUCT_LINK, "href");
        if (isBlank(href)) {
            return null;
        }

        String url = UrlNormalizer.toAbsoluteUrl(href, baseUrl);
        if (isBlank(url)) {
            return null;
        }

        if (!isProductDetailUrl(url)) {
            return null;
        }

        String imgSrc = firstNonBlank(
                firstAttr(card, Cafe24ListSelectors.PRODUCT_IMAGE, "src"),
                firstAttr(card, Cafe24ListSelectors.PRODUCT_IMAGE, "data-src"),
                firstAttr(card, Cafe24ListSelectors.PRODUCT_IMAGE, "data-original")
        );
        String imageUrl = UrlNormalizer.toAbsoluteUrl(imgSrc, baseUrl);

        String name = firstNonBlank(
                firstAttr(card, Cafe24ListSelectors.PRODUCT_IMAGE, "alt"),
                firstText(card, Cafe24ListSelectors.PRODUCT_NAME)
        );

        String brand = firstText(card, Cafe24ListSelectors.PRODUCT_BRAND);
        Long price = extractPrice(card);

        return ProductBrief.builder()
                .productKey(extractProductId(url))
                .url(url)
                .imageUrl(imageUrl)
                .name(blankToNull(Cafe24JsonLdParser.cleanProductName(name)))
                .brand(blankToNull(brand))
                .price(price)
                .build();
    }

    private static final java.util.regex.Pattern PRODUCT_ID_PATTERN =
            java.util.regex.Pattern.compile("/product/[^/]+/(\\d+)/");

    private String extractProductId(String url) {
        if (isBlank(url)) return null;
        java.util.regex.Matcher m = PRODUCT_ID_PATTERN.matcher(url);
        return m.find() ? m.group(1) : null;
    }

    private boolean isProductDetailUrl(String url) {
        if (isBlank(url)) {
            return false;
        }
        if (url.contains("/product/list.html")) {
            return false;
        }
        if (!url.contains("/product/")) {
            return false;
        }
        if (!url.contains("/category/")) {
            return false;
        }
        return url.contains("/display/");
    }

    private Long extractPrice(Element card) {
        Element priceHolder = card.selectFirst(Cafe24ListSelectors.PRICE_TEXT);

        if (priceHolder != null) {
            String priceAttr = priceHolder.attr("ec-data-price");
            if (!isBlank(priceAttr)) {
                Long price = parseLongSafe(priceAttr);
                if (price != null) {
                    return price;
                }
            }

            String priceText = priceHolder.text();
            if (!isBlank(priceText)) {
                return MoneyParser.parseToLong(priceText);
            }
        }

        Element priceEl = card.selectFirst(Cafe24ListSelectors.PRICE_WITH_ATTR);
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
            if (b == null || isBlank(b.getUrl())) {
                continue;
            }
            map.putIfAbsent(b.getUrl(), b);
        }
        return new ArrayList<>(map.values());
    }

    private String firstAttr(Element root, String cssQuery, String attr) {
        Element el = root.selectFirst(cssQuery);
        if (el == null) {
            return null;
        }
        return blankToNull(el.attr(attr));
    }

    private String firstText(Element root, String cssQuery) {
        Element el = root.selectFirst(cssQuery);
        if (el == null) {
            return null;
        }
        return blankToNull(el.text());
    }

    private String firstNonBlank(String... candidates) {
        if (candidates == null) {
            return null;
        }
        for (String c : candidates) {
            if (!isBlank(c)) {
                return c.trim();
            }
        }
        return null;
    }

    private Long parseLongSafe(String raw) {
        if (isBlank(raw)) {
            return null;
        }
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
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
