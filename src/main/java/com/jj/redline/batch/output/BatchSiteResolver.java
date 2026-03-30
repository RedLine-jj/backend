package com.jj.redline.batch.output;

import com.jj.redline.domain.dto.CrawlSite;
import com.jj.redline.domain.entity.Site;
import com.jj.redline.domain.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchSiteResolver {

    private final SiteRepository siteRepository;
    private final BatchCacheService batchCacheService;

    public Site resolve(CrawlSite crawlSite) {
        String siteName = crawlSite.name();
        return siteRepository.findBySiteName(siteName)
                .orElseGet(() -> {
                    log.info("[DbSnapshotWriter] tb_site insert: siteName={}", siteName);
                    batchCacheService.evict("sites");
                    return siteRepository.save(Site.of(siteName));
                });
    }
}
