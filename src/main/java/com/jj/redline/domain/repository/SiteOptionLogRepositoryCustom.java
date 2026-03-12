package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.SiteOptionLog;
import com.querydsl.core.Tuple;

import java.time.LocalDate;
import java.util.List;

public interface SiteOptionLogRepositoryCustom {

    List<SiteOptionLog> findLogsWithCursor(Long siteOptionId, Long cursor, int size);

    /** 모델 ID 기준, 일별 사이트 평균 가격 조회 (siteName, date, avgPrice) */
    List<Tuple> findDailyAvgPricesByModelId(Long modelId, LocalDate startDate);

    /** 최근 재입고 목록 조회 (status: false→true 전환 감지) */
    List<Tuple> findRecentRestocks(int limit);
}
