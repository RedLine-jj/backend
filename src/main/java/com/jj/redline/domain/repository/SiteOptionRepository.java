package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.Model;
import com.jj.redline.domain.entity.Site;
import com.jj.redline.domain.entity.SiteOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SiteOptionRepository extends JpaRepository<SiteOption, Long>, SiteOptionRepositoryCustom {


    @Query("SELECT so.model.id, MIN(so.price) FROM SiteOption so WHERE so.model.id IN :modelIds AND so.status = true GROUP BY so.model.id")
    List<Object[]> findMinPriceByModelIds(@Param("modelIds") List<Long> modelIds);
    
    Optional<SiteOption> findBySiteAndModelAndOptionLabel(Site site, Model model, String optionLabel);

    @Query("SELECT so.model.id, MIN(so.price) FROM SiteOption so WHERE so.model.id IN :modelIds AND so.status = true GROUP BY so.model.id")
    List<Object[]> findMinPriceByModelIds(@Param("modelIds") List<Long> modelIds);

    Optional<SiteOption> findBySiteAndModelAndOptionLabel(Site site, Model model, String optionLabel);
}
