package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SiteRepository extends JpaRepository<Site, Long> {

    List<Site> findAllByOrderBySiteNameAsc();

    Optional<Site> findBySiteName(String siteName);
}
