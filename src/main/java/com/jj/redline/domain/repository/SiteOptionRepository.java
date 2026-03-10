package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.SiteOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SiteOptionRepository extends JpaRepository<SiteOption, Long>, SiteOptionRepositoryCustom {

    @Query("SELECT so.model.id, MIN(so.price) FROM SiteOption so WHERE so.model.id IN :modelIds AND so.status = true GROUP BY so.model.id")
    List<Object[]> findMinPriceByModelIds(@Param("modelIds") List<Long> modelIds);
}
