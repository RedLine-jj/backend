package com.jj.redline.crawling.config;

import com.jj.redline.domain.dto.crawl.CrawlSiteConfig;
import com.jj.redline.domain.enums.Site;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NestStoreSiteConfig implements CrawlSiteConfig {

    @Override
    public String getDomain() {
        return "neststore.co.kr";
    }

    @Override
    public Site getSite() {
        return Site.NEST_STORE;
    }

    @Override
    public List<CategoryToCrawl> getCategories() {
        return List.of(
                new CategoryToCrawl("230", "Denim Jackets"),
                new CategoryToCrawl("34", "Denim Pants")
        );
    }

    @Override
    public String getListUrlTemplate() {
        return "https://neststore.co.kr/product/list.html?cate_no=%s&page=%d";
    }
}
