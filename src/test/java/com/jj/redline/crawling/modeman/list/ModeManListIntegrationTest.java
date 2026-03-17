package com.jj.redline.crawling.modeman.list;

import com.jj.redline.crawling.modeman.ModeManHttpClient;
import com.jj.redline.domain.dto.ProductBrief;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ModeManListIntegrationTest {

    @Test
    void list_real_call_parse_not_empty() {

        // ✅ 통합 테스트 실행 조건 (로컬에서만)
        boolean enabled =
                "true".equalsIgnoreCase(System.getProperty("it"))
                        || "true".equalsIgnoreCase(System.getenv("IT"));

        assumeTrue(enabled, "integration test skipped. add -Dit=true or set IT=true");

        ModeManHttpClient http = new ModeManHttpClient();
        ModeManListParser parser = new ModeManListParser();

        String url = "https://mode-man.com/product/list.html?cate_no=263";

        System.out.println("\n===============================");
        System.out.println("HTTP CALL: " + url);
        System.out.println("===============================\n");

        String html = http.get(url);
        assertThat(html).isNotBlank();

        System.out.println("[HTML length]=" + html.length());
        System.out.println("[HTML head]\n" + html.substring(0, Math.min(500, html.length())));

        // ✅ “상품이 들어있는 리스트 HTML”인지 먼저 확인
        // (이 마커들이 없으면: 리다이렉트/JS렌더링/차단 등으로 파서가 뽑을 수 없음 → 스킵)
        boolean hasProductMarkers =
                html.contains("anchorBoxId_")
                        || html.contains("ec-data-price")
                        || html.contains("/product/")
                        && !html.contains("/product/list.html");

        assumeTrue(hasProductMarkers,
                "integration test skipped: response HTML does not look like product list (maybe redirected / JS-rendered / blocked)");

        List<ProductBrief> briefs = parser.parse(html).getProductBriefs();

        // ✅ 여기서부터는 진짜 “리스트가 파싱 가능한 HTML”이라는 전제
        assertThat(briefs).isNotEmpty();

        System.out.println("\n========= PARSE RESULT =========");
        System.out.println("Total count = " + briefs.size());
        briefs.stream().limit(3).forEach(b -> {
            System.out.println("--------------------------------");
            System.out.println("URL   : " + b.getUrl());
            System.out.println("NAME  : " + b.getName());
            System.out.println("BRAND : " + b.getBrand());
            System.out.println("PRICE : " + b.getPrice());
        });
        System.out.println("--------------------------------\n");

        System.out.println("[first url] " + briefs.get(0).getUrl());
        
        // ✅ 첫 상품 URL은 “상세 페이지”여야 함
        assertThat(briefs.get(0).getUrl())
                .contains("/product/")
                .doesNotContain("list.html");
    }
}