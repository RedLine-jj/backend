package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.QBrand;
import com.jj.redline.domain.entity.QModel;
import com.jj.redline.domain.entity.QSite;
import com.jj.redline.domain.entity.QSiteOption;
import com.jj.redline.domain.entity.SiteOption;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SiteOptionRepositoryImpl implements SiteOptionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<SiteOption> findSiteOptionsWithCursor(Long siteId, Long modelId, Boolean status, Long cursor, int size) {
        QSiteOption siteOption = QSiteOption.siteOption;
        QSite site = QSite.site;
        QModel model = QModel.model;
        QBrand brand = QBrand.brand;

        return queryFactory
                .selectFrom(siteOption)
                .join(siteOption.site, site).fetchJoin()
                .join(siteOption.model, model).fetchJoin()
                .join(model.brand, brand).fetchJoin()
                .where(
                        siteIdEq(siteOption, siteId),
                        modelIdEq(siteOption, modelId),
                        statusEq(siteOption, status),
                        cursorLt(siteOption, cursor)
                )
                .orderBy(siteOption.id.desc())
                .limit(size + 1)
                .fetch();
    }

    private BooleanExpression siteIdEq(QSiteOption siteOption, Long siteId) {
        return siteId != null ? siteOption.site.id.eq(siteId) : null;
    }

    private BooleanExpression modelIdEq(QSiteOption siteOption, Long modelId) {
        return modelId != null ? siteOption.model.id.eq(modelId) : null;
    }

    private BooleanExpression statusEq(QSiteOption siteOption, Boolean status) {
        return status != null ? siteOption.status.eq(status) : null;
    }

    private BooleanExpression cursorLt(QSiteOption siteOption, Long cursor) {
        return cursor != null ? siteOption.id.lt(cursor) : null;
    }
}
