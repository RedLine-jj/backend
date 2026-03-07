package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.SiteOption;

import java.util.List;

public interface SiteOptionRepositoryCustom {

    List<SiteOption> findSiteOptionsWithCursor(Long siteId, Long modelId, Boolean status, Long cursor, int size);
}
