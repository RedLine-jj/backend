package com.jj.redline.crawling.modeman;

import com.jj.redline.exception.CrawlingException;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class ModeManHttpClient {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";

    private final HttpClient client;

    public ModeManHttpClient() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public String get(String url) {
        // Use UriComponentsBuilder to properly encode the URL
        // fromUriString is more robust for potentially malformed input URLs
        String encodedUrl = UriComponentsBuilder.fromUriString(url)
                .encode() // Explicitly encode the entire URI
                .build()
                .toUriString();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(encodedUrl))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    // ❌ Connection 헤더는 java.net.http에서 restricted라서 넣으면 예외 남
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            int code = response.statusCode();
            if (code < 200 || code >= 300) {
                throw new CrawlingException("HTTP error: " + code + " url=" + encodedUrl);
            }

            return response.body();

        } catch (IOException | InterruptedException e) {
            throw new CrawlingException("HTTP request failed: " + encodedUrl, e);
        } catch (IllegalArgumentException e) {
            // URI.create(url) 문제거나 restricted header 같은 케이스
            throw new CrawlingException("Invalid request: " + url + " msg=" + e.getMessage(), e);
        }
    }
}
