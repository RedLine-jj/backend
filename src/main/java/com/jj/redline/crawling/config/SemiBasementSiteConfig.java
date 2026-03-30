package com.jj.redline.crawling.config;

import com.jj.redline.domain.dto.crawl.CrawlSiteConfig;
import com.jj.redline.domain.enums.Site;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SemiBasementSiteConfig implements CrawlSiteConfig {

    @Override
    public String getDomain() {
        return "semibasement.com";
    }

    @Override
    public Site getSite() {
        return Site.SEMI_BASEMENT;
    }

    @Override
    public List<CategoryToCrawl> getCategories() {
        return List.of(
                new CategoryToCrawl("89", "Denim Pants"),
                new CategoryToCrawl("93", "Denim Jackets")
        );
    }

    @Override
    public String getListUrlTemplate() {
        return "https://semibasement.com/%s/?page=%d&sort=recent";
    }

    @Override
    public String getDetailUrlTemplate() {
        return "https://semibasement.com/%s/?idx=%s";
    }
}
