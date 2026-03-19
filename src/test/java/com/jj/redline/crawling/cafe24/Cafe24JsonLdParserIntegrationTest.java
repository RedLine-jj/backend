package com.jj.redline.crawling.cafe24;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jj.redline.domain.dto.crawl.CategoryDto;
import com.jj.redline.domain.dto.crawl.ProductBrief;
import com.jj.redline.domain.dto.crawl.ProductSnapshot;
import com.jj.redline.domain.enums.Site;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class Cafe24JsonLdParserIntegrationTest {

    private final boolean enabled =
            "true".equalsIgnoreCase(System.getProperty("it"))
                    || "true".equalsIgnoreCase(System.getenv("IT"));

    private final Cafe24HttpClient http = new Cafe24HttpClient();
    private final Cafe24JsonLdParser parser = new Cafe24JsonLdParser(new ObjectMapper());

    @Test
    void neststore_detail_should_strip_html_tags() {
        assumeTrue(enabled, "skipped: add -Dit=true");

        String url = "https://neststore.co.kr/product/union-special-overalls-work-trousers-chopper-builder-yarn-dyed-rude-bl/6249/category/34/display/1/";
        String html = http.get(url);

        ProductBrief brief = ProductBrief.builder()
                .url(url)
                .build();

        ProductSnapshot snapshot = parser.parse(html, Site.NEST_STORE,
                new CategoryDto(34, "Denim Pants"), OffsetDateTime.now(), brief);

        System.out.println("\n========= NESTSTORE DETAIL PARSE =========");
        System.out.println("NAME   : " + snapshot.getName());
        System.out.println("BRAND  : " + snapshot.getBrand());
        System.out.println("PRICE  : " + snapshot.getPrice());
        System.out.println("STATUS : " + snapshot.getParseStatus());
        System.out.println("OPTIONS: " + snapshot.getOptions().size());
        snapshot.getOptions().forEach(o ->
                System.out.println("  " + o.getOptionLabel() + " [" + o.getStatus() + "]"));
        System.out.println("==========================================\n");

        assertThat(snapshot.getName()).doesNotContain("<b>", "<br>", "<font>", "</b>", "</font>");
        assertThat(snapshot.getBrand()).isNotBlank();
        assertThat(snapshot.getOptions()).isNotEmpty();
    }

    @Test
    void modeman_detail_should_parse_normally() {
        assumeTrue(enabled, "skipped: add -Dit=true");

        String url = "https://mode-man.com/product/sc41947-14-25oz-denim-jacket-1953-model-one-wash/1073/category/263/display/1/";
        String html = http.get(url);

        ProductBrief brief = ProductBrief.builder()
                .url(url)
                .build();

        ProductSnapshot snapshot = parser.parse(html, Site.MODEMAN,
                new CategoryDto(263, "Denim Jackets"), OffsetDateTime.now(), brief);

        System.out.println("\n========= MODEMAN DETAIL PARSE =========");
        System.out.println("NAME   : " + snapshot.getName());
        System.out.println("BRAND  : " + snapshot.getBrand());
        System.out.println("PRICE  : " + snapshot.getPrice());
        System.out.println("STATUS : " + snapshot.getParseStatus());
        System.out.println("OPTIONS: " + snapshot.getOptions().size());
        snapshot.getOptions().forEach(o ->
                System.out.println("  " + o.getOptionLabel() + " [" + o.getStatus() + "]"));
        System.out.println("========================================\n");

        assertThat(snapshot.getName()).isNotBlank();
        assertThat(snapshot.getBrand()).isNotBlank();
        assertThat(snapshot.getOptions()).isNotEmpty();
    }
}
