package com.jj.redline.api.brand;

import com.jj.redline.domain.dto.brand.BrandResponse;
import com.jj.redline.domain.entity.Brand;
import com.jj.redline.domain.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandService {

    private final BrandRepository brandRepository;

    @Cacheable(value = "brands")
    public List<BrandResponse> getBrands() {
        List<BrandResponse> result = brandRepository.findAllByOrderByBrandNameAsc().stream()
                .map(brand -> new BrandResponse(brand.getId(), brand.getBrandName(), brand.getBrandNameKo()))
                .toList();
        log.debug("getBrands result: count={}", result.size());
        return result;
    }
}
