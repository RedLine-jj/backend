package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.SiteOptionLog;

import java.util.List;

public interface SiteOptionLogRepositoryCustom {

    List<SiteOptionLog> findLogsWithCursor(Long siteOptionId, Long cursor, int size);
}
