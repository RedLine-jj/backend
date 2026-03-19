package com.jj.redline.batch.output;

import com.jj.redline.common.ai.AiModelMatcher;
import com.jj.redline.domain.dto.crawl.ProductOption;
import com.jj.redline.domain.dto.crawl.ProductSnapshot;
import com.jj.redline.domain.enums.StockStatus;
import com.jj.redline.domain.entity.Brand;
import com.jj.redline.domain.entity.Model;
import com.jj.redline.domain.entity.ModelAlias;
import com.jj.redline.domain.enums.ModelType;
import com.jj.redline.domain.entity.Site;
import com.jj.redline.domain.entity.SiteOption;
import com.jj.redline.domain.entity.SiteOptionLog;
import com.jj.redline.domain.repository.BrandAliasRepository;
import com.jj.redline.domain.repository.BrandRepository;
import com.jj.redline.domain.repository.ModelAliasRepository;
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
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DbSnapshotWriter implements ItemWriter<ProductSnapshot> {

    private final SiteRepository siteRepository;
    private final BrandRepository brandRepository;
    private final BrandAliasRepository brandAliasRepository;
    private final ModelRepository modelRepository;
    private final ModelAliasRepository modelAliasRepository;
    private final AiModelMatcher aiModelMatcher;
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
            Brand brand = resolveBrand(brandName);

            // Step 3: tb_model upsert
            String modelName = snapshot.getName();
            if (modelName == null || modelName.isBlank()) continue;
            ModelType modelType = resolveModelType(snapshot);
            Model model = resolveModel(brand, modelName, snapshot.getImageUrl(), modelType, site);
            if (model == null) continue;

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

    private Brand resolveBrand(String brandName) {
        String normalized = brandName.toUpperCase()
                .replace(" ", "").replace("&", "").replace(".", "").replace(",", "");

        return brandRepository.findByBrandName(brandName)
                .or(() -> brandAliasRepository.findByAliasName(brandName).map(alias -> alias.getBrand()))
                .or(() -> brandRepository.findByNormalizedBrandName(normalized))
                .orElseGet(() -> {
                    log.info("[DbSnapshotWriter] tb_brand insert: brandName={}", brandName);
                    evictCache("brands");
                    return brandRepository.save(Brand.of(brandName));
                });
    }

    private ModelType resolveModelType(ProductSnapshot snapshot) {
        if (snapshot.getCategory() == null) return null;
        return switch ((int) snapshot.getCategory().getCode()) {
            case 263 -> ModelType.DENIM_JACKET;
            case 858 -> ModelType.DENIM_PANTS;
            case 230 -> ModelType.DENIM_JACKET;
            case 34 -> ModelType.DENIM_PANTS;
            case 89 -> ModelType.DENIM_PANTS;
            case 93 -> ModelType.DENIM_JACKET;
            default -> null;
        };
    }

    private Model resolveModel(Brand brand, String modelName, String imageUrl, ModelType modelType, Site site) {
        Optional<Model> exact = modelRepository.findByBrandAndModelName(brand, modelName);
        if (exact.isPresent()) {
            return exact.get();
        }

        Optional<ModelAlias> alias = modelAliasRepository.findByAliasName(modelName);
        if (alias.isPresent() && alias.get().getModel().getBrand().getId().equals(brand.getId())) {
            return alias.get().getModel();
        }

        List<String> existingNames = modelRepository.findByBrandWithOtherSiteOptions(brand, site)
                .stream()
                .map(Model::getModelName)
                .toList();

        if (existingNames.isEmpty()) {
            log.info("[DbSnapshotWriter] tb_model insert: modelName={}", modelName);
            return modelRepository.save(Model.of(brand, modelName, imageUrl, modelType));
        }

        AiModelMatcher.MatchResult matchResult = aiModelMatcher.findMatch(brand.getBrandName(), modelName, existingNames);

        return switch (matchResult.status()) {
            case MATCHED -> {
                Optional<Model> matched = modelRepository.findByBrandAndModelName(brand, matchResult.matchedName());
                if (matched.isPresent()) {
                    modelAliasRepository.save(ModelAlias.of(modelName, matched.get()));
                    log.info("[DbSnapshotWriter] AI model match: '{}' → '{}' (confidence: {}%)",
                            modelName, matchResult.matchedName(), matchResult.confidence());
                    yield matched.get();
                }
                log.info("[DbSnapshotWriter] tb_model insert: modelName={}", modelName);
                yield modelRepository.save(Model.of(brand, modelName, imageUrl, modelType));
            }
            case NO_MATCH -> {
                log.info("[DbSnapshotWriter] tb_model insert: modelName={}", modelName);
                yield modelRepository.save(Model.of(brand, modelName, imageUrl, modelType));
            }
            case ERROR -> {
                log.warn("[DbSnapshotWriter] AI unavailable, skipping: modelName={}", modelName);
                yield null;
            }
        };
    }

    private void evictCache(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) cache.clear();
    }
}
