package com.jj.redline.crawling.semibasement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jj.redline.exception.BadRequestException;
import com.jj.redline.exception.CrawlingException;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class SemiBasementHttpClient {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";
    private static final String BASE_URL = "https://semibasement.com";
    private static final String OMS_PRODUCTS_PATH = "/ajax/oms/OMS_get_products.cm";
    private static final int MAX_BATCH_SIZE = 20;

    private final HttpClient client;
    private final ObjectMapper objectMapper;

    public SemiBasementHttpClient() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String getHtml(String url) {
        String encodedUrl = UriComponentsBuilder.fromUriString(url)
                .encode()
                .build()
                .toUriString();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(encodedUrl))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            if (code < 200 || code >= 300) {
                throw new CrawlingException("HTTP error: " + code + " url=" + encodedUrl);
            }

            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new CrawlingException("HTTP request failed: " + encodedUrl, e);
        } catch (IllegalArgumentException e) {
            throw new CrawlingException("Invalid request: " + url + " msg=" + e.getMessage(), e);
        }
    }

    public String getProductDetails(List<Integer> idxList, String refererUrl) {
        if (idxList == null || idxList.isEmpty()) {
            throw new BadRequestException("idxList must not be empty");
        }

        List<JsonNode> mergedData = new ArrayList<>();
        String msg = "SUCCESS";

        for (int i = 0; i < idxList.size(); i += MAX_BATCH_SIZE) {
            int end = Math.min(i + MAX_BATCH_SIZE, idxList.size());
            List<Integer> batch = idxList.subList(i, end);
            String responseBody = requestOmsProducts(batch, refererUrl);

            try {
                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode currentMsg = root.get("msg");
                if (currentMsg != null && currentMsg.isTextual() && !currentMsg.asText().isBlank()) {
                    msg = currentMsg.asText();
                }

                JsonNode data = root.get("data");
                if (data != null && data.isArray()) {
                    for (JsonNode node : data) {
                        mergedData.add(node);
                    }
                }
            } catch (Exception e) {
                throw new CrawlingException("Failed to parse OMS response: " + responseBody, e);
            }
        }

        try {
            JsonNode mergedRoot = objectMapper.createObjectNode()
                    .put("msg", msg)
                    .set("data", objectMapper.valueToTree(mergedData));
            return objectMapper.writeValueAsString(mergedRoot);
        } catch (Exception e) {
            throw new CrawlingException("Failed to build merged OMS response", e);
        }
    }

    private String requestOmsProducts(List<Integer> batch, String refererUrl) {
        String url = UriComponentsBuilder.fromUriString(BASE_URL + OMS_PRODUCTS_PATH)
                .queryParam("prod_nos[]", batch.toArray())
                .encode()
                .build()
                .toUriString();

        String safeReferer = (refererUrl == null || refererUrl.isBlank()) ? BASE_URL : refererUrl;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", USER_AGENT)
                    .header("Referer", safeReferer)
                    .header("Origin", BASE_URL)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            if (code < 200 || code >= 300) {
                throw new CrawlingException("HTTP error: " + code + " url=" + url);
            }

            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new CrawlingException("HTTP request failed: " + url, e);
        } catch (IllegalArgumentException e) {
            throw new CrawlingException("Invalid request: " + url + " msg=" + e.getMessage(), e);
        }
    }
}
