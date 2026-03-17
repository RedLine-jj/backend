package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    List<Brand> findAllByOrderByBrandNameAsc();

    Optional<Brand> findByBrandName(String brandName);

    @Query("SELECT b FROM Brand b WHERE UPPER(REPLACE(REPLACE(REPLACE(REPLACE(b.brandName, ' ', ''), '&', ''), '.', ''), ',', '')) = :normalized")
    Optional<Brand> findByNormalizedBrandName(@Param("normalized") String normalized);
}
