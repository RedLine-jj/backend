package com.jj.redline.crawling.imweb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jj.redline.domain.dto.crawl.CategoryDto;
import com.jj.redline.domain.enums.ParseStatus;
import com.jj.redline.domain.dto.crawl.ProductBrief;
import com.jj.redline.domain.dto.crawl.ProductOption;
import com.jj.redline.domain.dto.crawl.ProductSnapshot;
import com.jj.redline.domain.enums.Site;
import com.jj.redline.domain.enums.StockStatus;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

@Component
public class ImwebDetailParser {

    private final ObjectMapper objectMapper;

    public ImwebDetailParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ProductSnapshot parse(
            String omsJson,
            Site site,
            CategoryDto category,
            OffsetDateTime capturedAt,
            ProductBrief brief
    ) {
        if (omsJson == null || omsJson.isBlank()) {
            return failSnapshot(site, category, capturedAt, brief, "EMPTY_OMS_JSON");
        }

        final JsonNode productNode;
        try {
            JsonNode root = objectMapper.readTree(omsJson);
            JsonNode data = root.get("data");
            if (data == null || !data.isArray() || data.isEmpty()) {
                return failSnapshot(site, category, capturedAt, brief, "EMPTY_DATA");
            }
            productNode = data.get(0);
            if (productNode == null || !productNode.isObject()) {
                return failSnapshot(site, category, capturedAt, brief, "INVALID_PRODUCT_DATA");
            }
        } catch (Exception e) {
            return failSnapshot(site, category, capturedAt, brief, "OMS_JSON_PARSE_ERROR");
        }

        String name = textOrNull(productNode.get("name"));
        String brand = toUpperOrNull(textOrNull(productNode.get("brand")));
        Long price = longOrNull(productNode.get("price"));

        if (name == null && brief != null) {
            name = brief.getName();
        }
        if (price == null && brief != null) {
            price = brief.getPrice();
        }

        List<String> optionLabels = extractOptionLabels(productNode.path("options"));
        List<ProductOption> options = extractOptions(productNode.path("options_detail"), optionLabels);

        ParseStatus status = ParseStatus.OK;
        List<String> partialReasons = new ArrayList<>();

        if (name == null || name.isBlank()) {
            partialReasons.add("MISSING_NAME");
        }
        if (brand == null || brand.isBlank()) {
            partialReasons.add("MISSING_BRAND");
        }
        if (price == null) {
            partialReasons.add("MISSING_PRICE");
        }
        if (options.isEmpty()) {
            partialReasons.add("NO_OPTIONS");
        }

        if (!partialReasons.isEmpty()) {
            status = ParseStatus.PARTIAL;
        }

        return ProductSnapshot.builder()
                .site(site)
                .category(category)
                .brand(brand)
                .name(name)
                .url(brief != null ? brief.getUrl() : null)
                .imageUrl(brief != null ? brief.getImageUrl() : null)
                .price(price)
                .capturedAt(capturedAt)
                .options(options)
                .parseStatus(status)
                .parseMessage(partialReasons.isEmpty() ? null : String.join(",", partialReasons))
                .build();
    }

    private List<String> extractOptionLabels(JsonNode optionsNode) {
        if (optionsNode == null || !optionsNode.isArray() || optionsNode.isEmpty()) {
            return List.of();
        }

        JsonNode firstOption = optionsNode.get(0);
        if (firstOption == null || !firstOption.isObject()) {
            return List.of();
        }

        JsonNode valueListNode = firstOption.get("value_list");
        if (valueListNode == null || !valueListNode.isObject()) {
            return List.of();
        }

        List<String> labels = new ArrayList<>();
        Iterator<JsonNode> values = valueListNode.elements();
        while (values.hasNext()) {
            JsonNode valueNode = values.next();
            labels.add(textOrNull(valueNode));
        }
        return labels;
    }

    private List<ProductOption> extractOptions(JsonNode optionsDetailNode, List<String> optionLabels) {
        if (optionsDetailNode == null || !optionsDetailNode.isArray() || optionsDetailNode.isEmpty()) {
            return List.of();
        }

        List<ProductOption> options = new ArrayList<>();
        for (int i = 0; i < optionsDetailNode.size(); i++) {
            JsonNode detail = optionsDetailNode.get(i);
            if (detail == null || !detail.isObject()) {
                continue;
            }

            String optionId = textOrNull(detail.get("code"));
            String optionLabel = com.jj.redline.common.util.OptionLabelNormalizer.normalize(
                    i < optionLabels.size() ? optionLabels.get(i) : null);
            String statusRaw = textOrNull(detail.get("status"));
            int stock = intOrDefault(detail.get("stock"), 0);

            StockStatus stockStatus = ("SALE".equalsIgnoreCase(statusRaw) && stock > 0)
                    ? StockStatus.AVAILABLE
                    : StockStatus.SOLD_OUT;

            if (optionLabel == null || optionLabel.isBlank()) {
                optionLabel = optionId;
            }

            if (optionLabel == null || optionLabel.isBlank()) {
                continue;
            }

            options.add(ProductOption.builder()
                    .optionId(optionId)
                    .optionLabel(optionLabel)
                    .status(stockStatus)
                    .build());
        }

        return options;
    }

    private ProductSnapshot failSnapshot(Site site, CategoryDto category, OffsetDateTime capturedAt, ProductBrief brief, String reason) {
        return ProductSnapshot.builder()
                .site(site)
                .category(category)
                .brand(brief != null ? brief.getBrand() : null)
                .name(brief != null ? brief.getName() : null)
                .url(brief != null ? brief.getUrl() : null)
                .imageUrl(brief != null ? brief.getImageUrl() : null)
                .price(brief != null ? brief.getPrice() : null)
                .capturedAt(capturedAt)
                .options(List.of())
                .parseStatus(ParseStatus.FAIL)
                .parseMessage(reason)
                .build();
    }

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

    private int intOrDefault(JsonNode node, int defaultValue) {
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        if (node.isInt() || node.isLong()) {
            return node.asInt();
        }
        if (node.isTextual()) {
            try {
                return Integer.parseInt(node.asText().trim());
            } catch (Exception ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private String toUpperOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.toUpperCase(Locale.ROOT);
    }
}
