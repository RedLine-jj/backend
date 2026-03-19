package com.jj.redline.batch.processor;

import com.jj.redline.common.util.TimeUtil;
import com.jj.redline.crawling.cafe24.Cafe24HttpClient;
import com.jj.redline.crawling.cafe24.Cafe24JsonLdParser;
import com.jj.redline.domain.dto.crawl.CrawlSiteConfig;
import com.jj.redline.domain.dto.crawl.CategoryDto;
import com.jj.redline.domain.dto.crawl.ProductBrief;
import com.jj.redline.domain.dto.crawl.ProductSnapshot;
import com.jj.redline.domain.enums.Site;
import org.springframework.batch.item.ItemProcessor;

import java.time.OffsetDateTime;

public class Cafe24DetailProcessor implements ItemProcessor<ProductBrief, ProductSnapshot> {

    private final Cafe24HttpClient cafe24HttpClient;
    private final Cafe24JsonLdParser cafe24JsonLdParser;
    private final CrawlSiteConfig siteConfig;

    public Cafe24DetailProcessor(Cafe24HttpClient cafe24HttpClient, Cafe24JsonLdParser cafe24JsonLdParser, CrawlSiteConfig siteConfig) {
        this.cafe24HttpClient = cafe24HttpClient;
        this.cafe24JsonLdParser = cafe24JsonLdParser;
        this.siteConfig = siteConfig;
    }

    @Override
    public ProductSnapshot process(ProductBrief item) {
        String productUrl = item.getUrl();
        String html = cafe24HttpClient.get(productUrl);

        final Site site = siteConfig.getSite();
        final CategoryDto category = item.getCategory();
        final OffsetDateTime capturedAt = TimeUtil.nowUtc();

        return cafe24JsonLdParser.parse(html, site, category, capturedAt, item);
    }
}
