package com.jj.redline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jj.redline.crawling.modeman.detail.ModeManJsonLdParser;
import com.jj.redline.domain.dto.*;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ModeManJsonLdParserTest {

    @Test
    void parse_success_offersArray() {
        ObjectMapper om = new ObjectMapper();
        ModeManJsonLdParser parser = new ModeManJsonLdParser(om);

        String html = """
            <html><head></head><body>
              <script type="application/ld+json">
              {
                "@context":"https://schema.org",
                "@type":"Product",
                "name":"PM-301 Indigo Classic Wide Jeans",
                "image":["https://mode-man.com/web/product/big/202208/xxx.jpg"],
                "brand":{"@type":"Brand","name":"PHIGVEL MAKERS CO."},
                "offers":[
                  {"name":"PM-301 Indigo Classic Wide Jeans 1(30)","availability":"OutOfStock"},
                  {"name":"PM-301 Indigo Classic Wide Jeans 2(32)","availability":"InStock"},
                  {"name":"PM-301 Indigo Classic Wide Jeans 3(34)","availability":"https://schema.org/InStock"},
                  {"name":"PM-301 Indigo Classic Wide Jeans 4(36)","availability":"https://schema.org/OutOfStock"}
                ]
              }
              </script>
            </body></html>
        """;

        ProductBrief brief = ProductBrief.builder()
                .url("https://mode-man.com/product/pm-301-indigo-classic-wide-jeans/8128/category/212/display/1/")
                .imageUrl("https://mode-man.com/web/product/big/202208/fallback.jpg")
                .price(378000L)
                .brand("BRIEF_BRAND_SHOULD_NOT_WIN")
                .name("BRIEF_NAME_SHOULD_NOT_WIN")
                .build();

        ProductSnapshot snap = parser.parse(
                html,
                Site.MODEMAN,
                new CategoryDto(263, "Denim Jackets"),
                OffsetDateTime.of(2026,3,1,12,0,0,0, ZoneOffset.UTC),
                brief
        );

        assertThat(snap.getParseStatus()).isEqualTo(ParseStatus.OK);
        assertThat(snap.getName()).isEqualTo("PM-301 Indigo Classic Wide Jeans");
        assertThat(snap.getBrand()).isEqualTo("PHIGVEL MAKERS CO.");
        assertThat(snap.getImageUrl()).isEqualTo("https://mode-man.com/web/product/big/202208/xxx.jpg");
        assertThat(snap.getUrl()).isEqualTo(brief.getUrl());
        assertThat(snap.getPrice()).isEqualTo(378000L);

        List<ProductOption> opts = snap.getOptions();
        assertThat(opts).hasSize(4);

        assertThat(opts.get(0).getOptionLabel()).isEqualTo("1(30)");
        assertThat(opts.get(0).getStatus()).isEqualTo(StockStatus.SOLD_OUT);

        assertThat(opts.get(1).getOptionLabel()).isEqualTo("2(32)");
        assertThat(opts.get(1).getStatus()).isEqualTo(StockStatus.AVAILABLE);

        assertThat(opts.get(2).getOptionLabel()).isEqualTo("3(34)");
        assertThat(opts.get(2).getStatus()).isEqualTo(StockStatus.AVAILABLE);

        assertThat(opts.get(3).getOptionLabel()).isEqualTo("4(36)");
        assertThat(opts.get(3).getStatus()).isEqualTo(StockStatus.SOLD_OUT);
    }
}
