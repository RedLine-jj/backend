package com.jj.redline.api.brand;

import com.jj.redline.domain.dto.brand.BrandResponse;
import com.jj.redline.domain.entity.Brand;
import com.jj.redline.domain.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandService {

    private final BrandRepository brandRepository;

    public List<BrandResponse> getBrands() {
        return brandRepository.findAllByOrderByBrandNameAsc().stream()
                .map(brand -> new BrandResponse(brand.getId(), brand.getBrandName(), brand.getBrandNameKo()))
                .toList();
    }
}
