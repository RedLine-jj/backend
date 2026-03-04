package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.SiteOptionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteOptionLogRepository extends JpaRepository<SiteOptionLog, Long>, SiteOptionLogRepositoryCustom {
}
