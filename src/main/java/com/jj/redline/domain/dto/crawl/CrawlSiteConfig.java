package com.jj.redline.domain.dto.crawl;

import com.jj.redline.domain.enums.Site;
import java.util.List;

public interface CrawlSiteConfig {
    String getDomain();

    Site getSite();

    List<CategoryToCrawl> getCategories();

    String getListUrlTemplate();

    default String getDetailUrlTemplate() { return null; }

    record CategoryToCrawl(String code, String name) {}
}
