package com.jj.redline.batch.output;

import com.jj.redline.domain.entity.Brand;
import com.jj.redline.domain.repository.BrandAliasRepository;
import com.jj.redline.domain.repository.BrandRepository;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchBrandResolver {

    private final BrandRepository brandRepository;
    private final BrandAliasRepository brandAliasRepository;
    private final BatchCacheService batchCacheService;

    public Brand resolve(String brandName) {
        String normalized = brandName.toUpperCase(Locale.ROOT)
                .replace(" ", "").replace("&", "").replace(".", "").replace(",", "");

        return brandRepository.findByBrandName(brandName)
                .or(() -> brandAliasRepository.findByAliasName(brandName).map(alias -> alias.getBrand()))
                .or(() -> brandRepository.findByNormalizedBrandName(normalized))
                .orElseGet(() -> {
                    log.info("[DbSnapshotWriter] tb_brand insert: brandName={}", brandName);
                    batchCacheService.evict("brands");
                    return brandRepository.save(Brand.of(brandName));
                });
    }
}
