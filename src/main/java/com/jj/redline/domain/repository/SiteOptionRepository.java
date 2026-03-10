package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.Model;
import com.jj.redline.domain.entity.Site;
import com.jj.redline.domain.entity.SiteOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SiteOptionRepository extends JpaRepository<SiteOption, Long>, SiteOptionRepositoryCustom {

    Optional<SiteOption> findBySiteAndModelAndOptionLabel(Site site, Model model, String optionLabel);
}
