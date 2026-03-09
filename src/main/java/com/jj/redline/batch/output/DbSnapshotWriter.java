package com.jj.redline.batch.output;

import com.jj.redline.domain.dto.ProductSnapshot;
import com.jj.redline.domain.entity.Brand;
import com.jj.redline.domain.entity.Site;
import com.jj.redline.domain.repository.BrandRepository;
import com.jj.redline.domain.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DbSnapshotWriter implements ItemWriter<ProductSnapshot> {

    private final SiteRepository siteRepository;
    private final BrandRepository brandRepository;

    @Override
    @Transactional
    public void write(Chunk<? extends ProductSnapshot> chunk) throws Exception {
        for (ProductSnapshot snapshot : chunk.getItems()) {
            // Step 1: tb_site upsert
            String siteName = snapshot.getSite() != null ? snapshot.getSite().name() : null;
            if (siteName == null) continue;
            Site site = siteRepository.findBySiteName(siteName)
                    .orElseGet(() -> {
                        log.info("[DbSnapshotWriter] tb_site insert: siteName={}", siteName);
                        return siteRepository.save(Site.of(siteName));
                    });

            // Step 2: tb_brand upsert
            String brandName = snapshot.getBrand();
            if (brandName == null || brandName.isBlank()) continue;
            Brand brand = brandRepository.findByBrandName(brandName)
                    .orElseGet(() -> {
                        log.info("[DbSnapshotWriter] tb_brand insert: brandName={}", brandName);
                        return brandRepository.save(Brand.of(brandName));
                    });

            log.info("[DbSnapshotWriter] site={}, brand={}, name={}",
                    site.getSiteName(), brand.getBrandName(), snapshot.getName());
        }
    }
}
