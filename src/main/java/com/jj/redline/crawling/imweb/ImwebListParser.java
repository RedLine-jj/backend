package com.jj.redline.crawling.imweb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

public class ImwebListParser {

    private static final String PRODUCT_SELECTOR = "div[data-product-properties]";

    private final ObjectMapper objectMapper;

    public ImwebListParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ListParseResult parse(String listHtml) {
        if (listHtml == null || listHtml.isBlank()) {
            return ListParseResult.builder().productBriefs(List.of()).build();
        }

        Document doc = Jsoup.parse(listHtml);
        Elements products = doc.select(PRODUCT_SELECTOR);

        Map<String, ProductBrief> dedupeMap = new LinkedHashMap<>();
        for (Element product : products) {
            ProductBriefWithKey brief = extractProductBrief(product);
            if (brief != null && brief.productKey() != null && !brief.productKey().isBlank()) {
                dedupeMap.putIfAbsent(brief.productKey(), brief.value());
            }
        }

        return ListParseResult.builder()
                .productBriefs(new ArrayList<>(dedupeMap.values()))
                .build();
    }

    private ProductBriefWithKey extractProductBrief(Element productElement) {
        String rawProperties = productElement.attr("data-product-properties");
        if (rawProperties == null || rawProperties.isBlank()) {
            return null;
        }

        try {
            JsonNode properties = objectMapper.readTree(rawProperties);

            JsonNode idxNode = properties.get("idx");
            if (idxNode == null || !idxNode.isNumber()) {
                return null;
            }

            int idx = idxNode.asInt();
            String code = textOrNull(properties.get("code"));
            String name = textOrNull(properties.get("name"));
            Long originalPrice = longOrNull(properties.get("original_price"));
            String imageUrl = textOrNull(properties.get("image_url"));
            Long price = longOrNull(properties.get("price"));

            if ((name == null || name.isBlank()) && code != null && !code.isBlank()) {
                name = code;
            }

            if (price == null) {
                price = originalPrice;
            }

            ProductBrief brief = ProductBrief.builder()
                    .productKey(String.valueOf(idx))
                    .name(name)
                    .imageUrl(imageUrl)
                    .price(price)
                    .build();

            return new ProductBriefWithKey(brief.getProductKey(), brief);
        } catch (Exception ignored) {
            return null;
        }
    }

    private record ProductBriefWithKey(String productKey, ProductBrief value) {}

    private String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value == null || value.isBlank() ? null : value;
    }

    private Long longOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.asLong();
        }
        if (node.isTextual()) {
            try {
                return Long.parseLong(node.asText().trim());
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }
}
