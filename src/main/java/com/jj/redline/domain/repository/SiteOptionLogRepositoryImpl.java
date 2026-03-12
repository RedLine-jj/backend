package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.QModel;
import com.jj.redline.domain.entity.QSite;
import com.jj.redline.domain.entity.QSiteOption;
import com.jj.redline.domain.entity.QSiteOptionLog;
import com.jj.redline.domain.entity.SiteOptionLog;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
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

    @Override
    public List<Tuple> findDailyAvgPricesByModelId(Long modelId, LocalDate startDate) {
        QSiteOptionLog log = QSiteOptionLog.siteOptionLog;
        QSiteOption so = QSiteOption.siteOption;
        QSite site = QSite.site;

        DateTemplate<LocalDate> dateExpr = Expressions.dateTemplate(
                LocalDate.class, "DATE({0})", log.capturedAt
        );

        return queryFactory
                .select(site.siteName, dateExpr, log.price.avg())
                .from(log)
                .join(log.siteOption, so)
                .join(so.site, site)
                .where(
                        so.model.id.eq(modelId),
                        log.capturedAt.goe(startDate.atStartOfDay()),
                        log.price.isNotNull()
                )
                .groupBy(site.siteName, dateExpr)
                .orderBy(site.siteName.asc(), dateExpr.asc())
                .fetch();
    }

    @Override
    public List<Tuple> findRecentRestocks(int limit) {
        QSiteOptionLog log = QSiteOptionLog.siteOptionLog;
        QSiteOptionLog prevLog = new QSiteOptionLog("prevLog");
        QSiteOptionLog maxPrev = new QSiteOptionLog("maxPrev");
        QSiteOption so = QSiteOption.siteOption;
        QModel model = QModel.model;
        QSite site = QSite.site;

        return queryFactory
                .select(model.id, model.modelName, site.siteName, log.capturedAt)
                .from(log)
                .join(log.siteOption, so)
                .join(so.model, model)
                .join(so.site, site)
                .where(
                        log.status.isTrue(),
                        JPAExpressions
                                .selectOne()
                                .from(prevLog)
                                .where(
                                        prevLog.siteOption.id.eq(log.siteOption.id),
                                        prevLog.status.isFalse(),
                                        prevLog.id.eq(
                                                JPAExpressions
                                                        .select(maxPrev.id.max())
                                                        .from(maxPrev)
                                                        .where(
                                                                maxPrev.siteOption.id.eq(log.siteOption.id),
                                                                maxPrev.id.lt(log.id)
                                                        )
                                        )
                                )
                                .exists()
                )
                .orderBy(log.capturedAt.desc())
                .limit(limit)
                .fetch();
    }

    private BooleanExpression cursorLt(QSiteOptionLog siteOptionLog, Long cursor) {
        return cursor != null ? siteOptionLog.id.lt(cursor) : null;
    }
}
