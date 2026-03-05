package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.QSiteOptionLog;
import com.jj.redline.domain.entity.SiteOptionLog;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SiteOptionLogRepositoryImpl implements SiteOptionLogRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<SiteOptionLog> findLogsWithCursor(Long siteOptionId, Long cursor, int size) {
        QSiteOptionLog siteOptionLog = QSiteOptionLog.siteOptionLog;

        return queryFactory
                .selectFrom(siteOptionLog)
                .where(
                        siteOptionLog.siteOption.id.eq(siteOptionId),
                        cursorLt(siteOptionLog, cursor)
                )
                .orderBy(siteOptionLog.id.desc())
                .limit(size + 1)
                .fetch();
    }

    private BooleanExpression cursorLt(QSiteOptionLog siteOptionLog, Long cursor) {
        return cursor != null ? siteOptionLog.id.lt(cursor) : null;
    }
}
