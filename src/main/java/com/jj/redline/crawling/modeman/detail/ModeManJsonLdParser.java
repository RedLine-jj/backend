package com.jj.redline.crawling.modeman.detail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jj.redline.common.util.QueryParamExtractor;
import com.jj.redline.domain.dto.CategoryDto;
import com.jj.redline.domain.dto.ParseStatus;
import com.jj.redline.domain.dto.ProductBrief;
import com.jj.redline.domain.dto.ProductOption;
import com.jj.redline.domain.dto.ProductSnapshot;
import com.jj.redline.domain.dto.Site;
import com.jj.redline.domain.dto.StockStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class ModeManJsonLdParser {

    private final ObjectMapper objectMapper;

    public ModeManJsonLdParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ProductSnapshot parse(
            String detailHtml,
            Site site,
            CategoryDto category,
            OffsetDateTime capturedAt,
            ProductBrief brief
    ) {
        if (detailHtml == null || detailHtml.isBlank()) {
            return failSnapshot(site, category, capturedAt, brief, "EMPTY_HTML");
        }

        Document doc = Jsoup.parse(detailHtml);

        List<Element> scripts = doc.select("script[type=application/ld+json]");
        if (scripts.isEmpty()) {
            return failSnapshot(site, category, capturedAt, brief, "NO_JSONLD_SCRIPT");
        }

        JsonNode productNode = null;

        for (Element script : scripts) {
            String jsonText = script.data();
            if (jsonText == null || jsonText.isBlank()) jsonText = script.html();
            if (jsonText == null || jsonText.isBlank()) continue;

            try {
                JsonNode root = objectMapper.readTree(jsonText);
                JsonNode candidate = findProductNode(root);
                if (candidate != null) {
                    productNode = candidate;
                    break;
                }
            } catch (Exception ignore) {
                // JSON 파싱 실패는 다음 script로 넘어감
            }
        }

        if (productNode == null) {
            return failSnapshot(site, category, capturedAt, brief, "NO_PRODUCT_NODE");
        }

        // [수정] HTML 엔티티를 디코딩(unescape)하는 로직 추가
        String name = unescape(textOrNull(productNode.get("name")));
        String brand = unescape(textOrNull(productNode.path("brand").path("name")));
        String imageUrl = extractFirstImage(productNode.get("image"));
        JsonNode offersNode = productNode.get("offers");

        Long jsonLdPrice = extractPriceFromOffers(offersNode);

        if (isBlank(name) && brief != null) name = brief.getName();
        if (isBlank(brand) && brief != null) brand = brief.getBrand();
        if (isBlank(imageUrl) && brief != null) imageUrl = brief.getImageUrl();
        Long finalPrice = jsonLdPrice != null ? jsonLdPrice : (brief != null ? brief.getPrice() : null);

        List<ProductOption> options = new ArrayList<>();
        if (offersNode == null || offersNode.isNull()) {
            return ProductSnapshot.builder()
                    .site(site).category(category).brand(brand).name(name)
                    .url(brief != null ? brief.getUrl() : null)
                    .imageUrl(imageUrl).price(finalPrice)
                    .capturedAt(capturedAt).options(options)
                    .parseStatus(ParseStatus.PARTIAL).parseMessage("NO_OFFERS")
                    .build();
        }

        if (offersNode.isArray()) {
            for (JsonNode offer : offersNode) {
                parseOfferIntoOptions(offer, name, options);
            }
        } else if (offersNode.isObject()) {
            parseOfferIntoOptions(offersNode, name, options);
        }

        ParseStatus status = options.isEmpty() ? ParseStatus.PARTIAL : ParseStatus.OK;
        String msg = options.isEmpty() ? "OFFERS_EMPTY" : null;

        return ProductSnapshot.builder()
                .site(site).category(category).brand(brand).name(name)
                .url(brief != null ? brief.getUrl() : null)
                .imageUrl(imageUrl).price(finalPrice)
                .capturedAt(capturedAt).options(options)
                .parseStatus(status).parseMessage(msg)
                .build();
    }

    private void parseOfferIntoOptions(JsonNode offer, String productName, List<ProductOption> options) {
        if (offer == null || offer.isNull()) return;

        // [수정] HTML 엔티티 디코딩 추가
        String offerName = unescape(textOrNull(offer.get("name")));
        String availability = textOrNull(offer.get("availability"));
        String offerUrl = textOrNull(offer.get("url"));

        String optionId = QueryParamExtractor.extract(offerUrl, "item_code");
        StockStatus stockStatus = mapAvailabilityToStatus(availability);
        String optionLabel = extractOptionLabel(offerName, productName);

        ProductOption option = ProductOption.builder()
                .optionId(optionId)
                .optionLabel(optionLabel)
                .status(stockStatus)
                .build();

        if (!isBlank(option.getOptionLabel())) {
            options.add(option);
        }
    }

    private Long extractPriceFromOffers(JsonNode offersNode) {
        if (offersNode == null || offersNode.isNull()) return null;

        JsonNode offerToParse;
        if (offersNode.isArray() && offersNode.size() > 0) {
            offerToParse = offersNode.get(0);
        } else if (offersNode.isObject()) {
            offerToParse = offersNode;
        } else {
            return null;
        }

        JsonNode priceNode = offerToParse.get("price");
        if (priceNode != null && priceNode.isNumber()) {
            return priceNode.asLong();
        }
        return null;
    }

    private JsonNode findProductNode(JsonNode node) {
        if (node == null || node.isNull()) return null;

        if (isProductType(node)) return node;

        JsonNode graph = node.get("@graph");
        if (graph != null && graph.isArray()) {
            for (JsonNode n : graph) {
                if (isProductType(n)) return n;
            }
        }

        if (node.isArray()) {
            for (JsonNode n : node) {
                JsonNode found = findProductNode(n);
                if (found != null) return found;
            }
        }

        if (node.isObject()) {
            Iterator<String> it = node.fieldNames();
            while (it.hasNext()) {
                String field = it.next();
                JsonNode child = node.get(field);
                JsonNode found = findProductNode(child);
                if (found != null) return found;
            }
        }

        return null;
    }

    private boolean isProductType(JsonNode node) {
        JsonNode type = node.get("@type");
        if (type == null || type.isNull()) return false;

        if (type.isTextual()) {
            return "Product".equalsIgnoreCase(type.asText());
        }
        if (type.isArray()) {
            for (JsonNode t : type) {
                if (t.isTextual() && "Product".equalsIgnoreCase(t.asText())) return true;
            }
        }
        return false;
    }

    private StockStatus mapAvailabilityToStatus(String availability) {
        if (availability == null) return StockStatus.SOLD_OUT;

        String a = availability.trim().toLowerCase();
        if (a.endsWith("instock")) return StockStatus.AVAILABLE;
        if (a.endsWith("outofstock")) return StockStatus.SOLD_OUT;

        return StockStatus.SOLD_OUT;
    }

    private String extractFirstImage(JsonNode imageNode) {
        if (imageNode == null || imageNode.isNull()) return null;

        if (imageNode.isTextual()) return imageNode.asText();

        if (imageNode.isArray() && imageNode.size() > 0) {
            JsonNode first = imageNode.get(0);
            if (first != null && first.isTextual()) return first.asText();
        }
        return null;
    }

    private String extractOptionLabel(String offerName, String productName) {
        if (isBlank(offerName)) return null;

        String s = offerName.trim();

        if (!isBlank(productName)) {
            String pn = productName.trim();
            if (s.startsWith(pn)) {
                s = s.substring(pn.length()).trim();
            }
        }

        String[] tokens = s.split("\\s+");
        if (tokens.length >= 1) {
            String last = tokens[tokens.length - 1].trim();
            if (!isBlank(last)) return last;
        }
        return offerName.trim();
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
        if (node == null || node.isNull() || !node.isTextual()) return null;
        return node.asText();
    }

    private String unescape(String text) {
        if (isBlank(text)) return text;
        return Parser.unescapeEntities(text, false);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
