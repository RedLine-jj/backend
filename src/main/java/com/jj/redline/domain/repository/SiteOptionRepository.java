package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.SiteOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteOptionRepository extends JpaRepository<SiteOption, Long>, SiteOptionRepositoryCustom {
}
