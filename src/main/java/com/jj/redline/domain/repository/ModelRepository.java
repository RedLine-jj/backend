package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.Brand;
import com.jj.redline.domain.entity.Model;
import com.jj.redline.domain.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ModelRepository extends JpaRepository<Model, Long>, ModelRepositoryCustom {
    long countByBrandIdIn(List<Long> brandIds);

    Optional<Model> findByBrandAndModelName(Brand brand, String modelName);

    List<Model> findAllByBrand(Brand brand);

    @Query("SELECT DISTINCT m FROM Model m WHERE m.brand = :brand AND m.id IN " +
           "(SELECT so.model.id FROM SiteOption so WHERE so.site <> :site)")
    List<Model> findByBrandWithOtherSiteOptions(@Param("brand") Brand brand, @Param("site") Site site);
}
