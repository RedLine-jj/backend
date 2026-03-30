package com.jj.redline.crawling.cafe24;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class Cafe24HttpClient {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";

    private final HttpClient client;

    public Cafe24HttpClient() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public String get(String url) {
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

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            int code = response.statusCode();
            if (code < 200 || code >= 300) {
                throw new RuntimeException("HTTP error: " + code + " url=" + encodedUrl);
            }

            return response.body();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("HTTP request interrupted: " + encodedUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("HTTP request failed: " + encodedUrl, e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid request: " + url + " msg=" + e.getMessage(), e);
        }
    }
}
