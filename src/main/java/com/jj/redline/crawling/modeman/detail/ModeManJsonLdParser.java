package com.jj.redline.crawling.modeman.detail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper; 
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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List; 

/**
 * [역할] 모드맨 상품 상세 HTML에서 JSON-LD(Product) 파싱하여 ProductSnapshot 생성
 *
 * 파싱 전략:
 * 1) script[type="application/ld+json"] 모두 수집
 * 2) JSON 파싱 후, @type == "Product" 인 노드를 찾음
 * 3) Product 레벨: name, brand.name, image[0], offers[]
 * 4) offers[] -> options[]:
 *    - optionLabel: offers.name에서 "상품명" 제거 후 마지막 토큰을 추출(가능하면)
 *                 실패하면 offers.name 그대로 사용
 *    - status: availability InStock/OutOfStock 매핑 
 */
public class ModeManJsonLdParser {

    private final ObjectMapper objectMapper;

    public ModeManJsonLdParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * @param detailHtml  상품 상세 HTML
     * @param site        MODEMAN
     * @param category    CategoryDto(code/name)
     * @param capturedAt  UTC OffsetDateTime
     * @param brief       (선택) List 단계에서 가져온 값(brand/name/imageUrl/price/url) fallback
     */
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

        // 여러 script 중에서 Product를 찾아야 함
        for (Element script : scripts) {
            String jsonText = script.data();

            if (jsonText == null || jsonText.isBlank()) jsonText = script.html();
            if (jsonText == null || jsonText.isBlank()) continue;

            try {
                JsonNode root = objectMapper.readTree(jsonText);

                // JSON-LD는 객체/배열/그래프(@graph) 등 다양한 형태 가능
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

        // Product 레벨 필드 추출 (없으면 brief fallback)
        String name = textOrNull(productNode.get("name"));
        String brand = textOrNull(productNode.path("brand").path("name"));
        String imageUrl = extractFirstImage(productNode.get("image"));

        // fallback 적용
        if (isBlank(name) && brief != null) name = brief.getName();
        if (isBlank(brand) && brief != null) brand = brief.getBrand();
        if (isBlank(imageUrl) && brief != null) imageUrl = brief.getImageUrl();

        // options 파싱
        List<ProductOption> options = new ArrayList<>();
        JsonNode offersNode = productNode.get("offers");

        if (offersNode == null || offersNode.isNull()) {
            // offers가 없으면 PARTIAL
            return ProductSnapshot.builder()
                    .site(site)
                    .category(category)
                    .brand(brand)
                    .name(name)
                    .url(brief != null ? brief.getUrl() : null)
                    .imageUrl(imageUrl)
                    .price(brief != null ? brief.getPrice() : null)
                    .capturedAt(capturedAt)
                    .options(options)
                    .parseStatus(ParseStatus.PARTIAL)
                    .parseMessage("NO_OFFERS")
                    .build();
        }

        // offers가 배열 or 단일 객체일 수 있음
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
                .site(site)
                .category(category)
                .brand(brand)
                .name(name)
                .url(brief != null ? brief.getUrl() : null)
                .imageUrl(imageUrl)
                .price(brief != null ? brief.getPrice() : null)
                .capturedAt(capturedAt)
                .options(options)
                .parseStatus(status)
                .parseMessage(msg)
                .build();
    }

    private void parseOfferIntoOptions(JsonNode offer, String productName, List<ProductOption> options) {
        if (offer == null || offer.isNull()) return;

        String offerName = textOrNull(offer.get("name"));
        String availability = textOrNull(offer.get("availability")); 

        StockStatus stockStatus = mapAvailabilityToStatus(availability);

        // optionLabel 추출 규칙:
        // 1) offer.name가 "상품명 1(30)" 형태면 상품명 제거 후 남은 마지막 토큰을 사용
        // 2) 실패하면 offer.name 그대로
        String optionLabel = extractOptionLabel(offerName, productName);
  
        ProductOption option = ProductOption.builder() 
                .optionLabel(optionLabel)
                .status(stockStatus)
                .build();

        // optionLabel이 비어있으면 스킵(데이터 오염 방지)
        if (!isBlank(option.getOptionLabel())) {
            options.add(option);
        }
    }

    /**
     * JSON-LD 구조에서 @type == Product인 노드를 찾는다.
     */
    private JsonNode findProductNode(JsonNode node) {
        if (node == null || node.isNull()) return null;

        // 1) 현재 노드가 Product인지
        if (isProductType(node)) return node;

        // 2) @graph 탐색
        JsonNode graph = node.get("@graph");
        if (graph != null && graph.isArray()) {
            for (JsonNode n : graph) {
                if (isProductType(n)) return n;
            }
        }

        // 3) 배열 전체 탐색
        if (node.isArray()) {
            for (JsonNode n : node) {
                JsonNode found = findProductNode(n);
                if (found != null) return found;
            }
        }

        // 4) 객체의 모든 필드 탐색(느리지만 안전)
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

        // schema.org에서 "https://schema.org/InStock" 형태로 올 수도 있음
        String a = availability.trim().toLowerCase();
        if (a.endsWith("instock")) return StockStatus.AVAILABLE;
        if (a.endsWith("outofstock")) return StockStatus.SOLD_OUT;

        // 알 수 없으면 보수적으로 SOLD_OUT 처리(또는 UNKNOWN 추가 가능)
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

        // productName이 앞부분에 들어 있으면 제거 시도
        if (!isBlank(productName)) {
            String pn = productName.trim();
            if (s.startsWith(pn)) {
                s = s.substring(pn.length()).trim();
            }
        }

        // 남은 문자열이 "1(30)" 혹은 "30" 처럼 끝 토큰이면 그걸 사용
        // 공백 기준 마지막 토큰
        String[] tokens = s.split("\\s+");
        if (tokens.length >= 1) {
            String last = tokens[tokens.length - 1].trim();
            // last가 너무 짧거나 이상하면 offerName 전체 사용
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

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}