package com.jj.redline.batch.output;

import com.jj.redline.domain.dto.ProductOption;
import com.jj.redline.domain.dto.ProductSnapshot;
import com.jj.redline.domain.dto.StockStatus;
import com.jj.redline.domain.entity.Brand;
import com.jj.redline.domain.entity.Model;
import com.jj.redline.domain.entity.ModelType;
import com.jj.redline.domain.entity.Site;
import com.jj.redline.domain.entity.SiteOption;
import com.jj.redline.domain.entity.SiteOptionLog;
import com.jj.redline.domain.repository.BrandRepository;
import com.jj.redline.domain.repository.ModelRepository;
import com.jj.redline.domain.repository.SiteOptionLogRepository;
import com.jj.redline.domain.repository.SiteOptionRepository;
import com.jj.redline.domain.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class DbSnapshotWriter implements ItemWriter<ProductSnapshot> {

    private final SiteRepository siteRepository;
    private final BrandRepository brandRepository;
    private final ModelRepository modelRepository;
    private final SiteOptionRepository siteOptionRepository;
    private final SiteOptionLogRepository siteOptionLogRepository;
    private final StringRedisTemplate redisTemplate;
    private final ChannelTopic restockTopic;
    private final CacheManager cacheManager;

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
                        evictCache("sites");
                        return siteRepository.save(Site.of(siteName));
                    });

            // Step 2: tb_brand upsert
            String brandName = snapshot.getBrand();
            if (brandName == null || brandName.isBlank()) continue;
            Brand brand = brandRepository.findByBrandName(brandName)
                    .orElseGet(() -> {
                        log.info("[DbSnapshotWriter] tb_brand insert: brandName={}", brandName);
                        evictCache("brands");
                        return brandRepository.save(Brand.of(brandName));
                    });

            // Step 3: tb_model upsert
            String modelName = snapshot.getName();
            if (modelName == null || modelName.isBlank()) continue;
            ModelType modelType = resolveModelType(snapshot);
            Model model = modelRepository.findByBrandAndModelName(brand, modelName)
                    .orElseGet(() -> {
                        log.info("[DbSnapshotWriter] tb_model insert: modelName={}", modelName);
                        return modelRepository.save(Model.of(brand, modelName, snapshot.getImageUrl(), modelType));
                    });

            // Step 4: tb_site_option upsert + Step 5: 변경 시 tb_site_option_log insert
            List<ProductOption> options = snapshot.getOptions();
            if (options == null || options.isEmpty()) continue;
            LocalDateTime capturedAt = snapshot.getCapturedAt() != null
                    ? snapshot.getCapturedAt().toLocalDateTime() : LocalDateTime.now();
            Integer price = snapshot.getPrice() != null ? snapshot.getPrice().intValue() : null;

            boolean restocked = false;
            for (ProductOption option : options) {
                Boolean status = option.getStatus() == StockStatus.AVAILABLE;
                SiteOption siteOption = siteOptionRepository
                        .findBySiteAndModelAndOptionLabel(site, model, option.getOptionLabel())
                        .orElse(null);

                if (siteOption == null) {
                    siteOption = siteOptionRepository.save(
                            SiteOption.of(site, model, option.getOptionLabel(),
                                    price, snapshot.getUrl(), status, capturedAt));
                    siteOptionLogRepository.save(
                            SiteOptionLog.of(siteOption, price, status, capturedAt));
                } else {
                    boolean changed = !Objects.equals(siteOption.getPrice(), price)
                            || !Objects.equals(siteOption.getStatus(), status);
                    if (changed) {
                        log.info("[DbSnapshotWriter] 변경 감지: option={}, price={}→{}, status={}→{}",
                                option.getOptionLabel(), siteOption.getPrice(), price,
                                siteOption.getStatus(), status);
                        siteOptionLogRepository.save(
                                SiteOptionLog.of(siteOption, price, status, capturedAt));

                        if (!Boolean.TRUE.equals(siteOption.getStatus()) && Boolean.TRUE.equals(status)) {
                            restocked = true;
                        }
                    }
                    siteOption.update(price, status, capturedAt);
                }
            }

            if (restocked) {
                log.info("[DbSnapshotWriter] 재입고 감지 → Redis PUBLISH: modelId={}", model.getId());
                redisTemplate.convertAndSend(restockTopic.getTopic(), String.valueOf(model.getId()));
            }
            log.info("[DbSnapshotWriter] done: model={}, options={}", modelName, options.size());
        }
    }

    private ModelType resolveModelType(ProductSnapshot snapshot) {
        if (snapshot.getCategory() == null) return null;
        return switch ((int) snapshot.getCategory().getCode()) {
            case 263 -> ModelType.DENIM_JACKET;
            case 858 -> ModelType.DENIM_PANTS;
            default -> null;
        };
    }

    private void evictCache(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) cache.clear();
    }
}
