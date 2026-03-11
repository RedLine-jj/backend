package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.Brand;
import com.jj.redline.domain.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModelRepository extends JpaRepository<Model, Long>, ModelRepositoryCustom {
    long countByBrandIdIn(List<Long> brandIds);

    Optional<Model> findByBrandAndModelName(Brand brand, String modelName);
}

