package com.jj.redline.crawling.config;

import com.jj.redline.domain.dto.crawl.CrawlSiteConfig;
import com.jj.redline.domain.enums.Site;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ModeManSiteConfig implements CrawlSiteConfig {

    @Override
    public String getDomain() {
        return "mode-man.com";
    }

    @Override
    public Site getSite() {
        return Site.MODEMAN;
    }

    @Override
    public List<CategoryToCrawl> getCategories() {
        return List.of(
                new CategoryToCrawl("263", "Denim Jackets"),
                new CategoryToCrawl("858", "Denim Pants")
        );
    }

    @Override
    public String getListUrlTemplate() {
        return "https://mode-man.com/product/list.html?cate_no=%s&page=%d";
    }
}
