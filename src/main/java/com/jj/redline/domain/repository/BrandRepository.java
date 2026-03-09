package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    List<Brand> findAllByOrderByBrandNameAsc();

    Optional<Brand> findByBrandName(String brandName);
}
