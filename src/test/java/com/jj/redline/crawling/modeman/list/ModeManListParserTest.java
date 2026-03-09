package com.jj.redline.crawling.modeman.list;

import com.jj.redline.domain.dto.ProductBrief;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ModeManListParserTest {

    @Test
    void parse_success_twoProducts_withEcDataPrice() {
        ModeManListParser parser = new ModeManListParser();

        String html = """
            <html><body>
              <ul class="p">
                <li id="anchorBoxId_8128">
                  <div class="img">
                    <a href="/product/pm-301-indigo-classic-wide-jeans/8128/category/212/display/1/">
                      <img src="//mode-man.com/web/product/big/202208/289dc4318d4dbec52e8faac6176bd333.jpg"
                           alt="PM-301 Indigo Classic Wide Jeans"/>
                    </a>
                  </div>
                  <p class="nm">PM-301 Indigo Classic Wide Jeans</p>
                  <p class="b"><a>PHIGVEL MAKERS CO.</a></p>
                  <div class="dsc" ec-data-price="378000"></div>
                </li>

                <li id="anchorBoxId_6737">
                  <div class="img">
                    <a href="https://mode-man.com/product/00-1040-high-waist-denim-jasmin-used-washing-for-women/6737/category/212/display/1/">
                      <img src="https://mode-man.com/web/product/big/202401/sample.jpg"
                           alt="00-1040 High Waist Denim"/>
                    </a>
                  </div>
                  <p class="b"><a>BRAND2</a></p>
                  <div class="dsc" ec-data-price="198000"></div>
                </li>
              </ul>
            </body></html>
        """;

        List<ProductBrief> briefs = parser.parse(html);

        assertThat(briefs).hasSize(2);

        ProductBrief b1 = briefs.get(0);
        assertThat(b1.getUrl()).isEqualTo("https://mode-man.com/product/pm-301-indigo-classic-wide-jeans/8128/category/212/display/1/");
        assertThat(b1.getImageUrl()).isEqualTo("https://mode-man.com/web/product/big/202208/289dc4318d4dbec52e8faac6176bd333.jpg");
        assertThat(b1.getName()).isEqualTo("PM-301 Indigo Classic Wide Jeans");
        assertThat(b1.getBrand()).isEqualTo("PHIGVEL MAKERS CO.");
        assertThat(b1.getPrice()).isEqualTo(378000L);

        ProductBrief b2 = briefs.get(1);
        assertThat(b2.getUrl()).isEqualTo("https://mode-man.com/product/00-1040-high-waist-denim-jasmin-used-washing-for-women/6737/category/212/display/1/");
        assertThat(b2.getImageUrl()).isEqualTo("https://mode-man.com/web/product/big/202401/sample.jpg");
        assertThat(b2.getBrand()).isEqualTo("BRAND2");
        assertThat(b2.getPrice()).isEqualTo(198000L);
    }

    @Test
    void parse_dedupe_sameUrl_keepsFirst() {
        ModeManListParser parser = new ModeManListParser();

        String html = """
            <html><body>
              <ul class="p">
                <li id="anchorBoxId_1">
                  <a href="/product/abc/111/category/212/display/1/"><img alt="A"/></a>
                  <div ec-data-price="1000"></div>
                </li>
                <li id="anchorBoxId_2">
                  <a href="/product/abc/111/category/212/display/1/"><img alt="A DUP"/></a>
                  <div ec-data-price="9999"></div>
                </li>
              </ul>
            </body></html>
        """;

        List<ProductBrief> briefs = parser.parse(html);

        assertThat(briefs).hasSize(1);
        assertThat(briefs.get(0).getUrl()).isEqualTo("https://mode-man.com/product/abc/111/category/212/display/1/");
        assertThat(briefs.get(0).getPrice()).isEqualTo(1000L);
    }
}
