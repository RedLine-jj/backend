package com.jj.redline.batch.output;

import com.jj.redline.domain.dto.ProductSnapshot;
import com.jj.redline.domain.entity.Brand;
import com.jj.redline.domain.entity.Model;
import com.jj.redline.domain.entity.ModelType;
import com.jj.redline.domain.entity.Site;
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

    private final BatchSiteResolver batchSiteResolver;
    private final BatchBrandResolver batchBrandResolver;
    private final BatchModelTypeResolver batchModelTypeResolver;
    private final BatchModelResolver batchModelResolver;
    private final SiteOptionPersistenceService siteOptionPersistenceService;
    private final RestockEventPublisher restockEventPublisher;

    @Override
    @Transactional
    public void write(Chunk<? extends ProductSnapshot> chunk) throws Exception {
        for (ProductSnapshot snapshot : chunk.getItems()) {
            if (snapshot.getSite() == null) continue;
            Site site = batchSiteResolver.resolve(snapshot.getSite());

            String brandName = snapshot.getBrand();
            if (brandName == null || brandName.isBlank()) continue;
            Brand brand = batchBrandResolver.resolve(brandName);

            String modelName = snapshot.getName();
            if (modelName == null || modelName.isBlank()) continue;
            ModelType modelType = batchModelTypeResolver.resolve(snapshot);
            Model model = batchModelResolver.resolve(brand, modelName, snapshot.getImageUrl(), modelType, site);
            if (model == null) continue;

            SiteOptionPersistenceService.SiteOptionUpdateResult result =
                    siteOptionPersistenceService.upsert(snapshot, site, model);
            if (result.optionCount() == 0) continue;

            if (result.restocked()) {
                restockEventPublisher.publish(model.getId());
            }
            log.info("[DbSnapshotWriter] done: model={}, options={}", modelName, result.optionCount());
        }
    }
}
