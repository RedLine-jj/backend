package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelRepository extends JpaRepository<Model, Long>, ModelRepositoryCustom {
    long countByBrandId(Long brandId);
}

