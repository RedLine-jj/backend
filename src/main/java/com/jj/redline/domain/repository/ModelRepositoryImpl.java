package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.Model;
import com.jj.redline.domain.entity.QBrand;
import com.jj.redline.domain.entity.QModel;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ModelRepositoryImpl implements ModelRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Model> findModelsWithCursor(List<Long> brandIds, Long cursor, int size) {
        QModel model = QModel.model;
        QBrand brand = QBrand.brand;

        return queryFactory
                .selectFrom(model)
                .join(model.brand, brand).fetchJoin()
                .where(
                        brandIdsIn(model, brandIds),
                        cursorLt(model, cursor)
                )
                .orderBy(model.id.desc())
                .limit(size + 1)
                .fetch();
    }

    private BooleanExpression brandIdsIn(QModel model, List<Long> brandIds) {
        return brandIds != null && !brandIds.isEmpty() ? model.brand.id.in(brandIds) : null;
    }

    private BooleanExpression cursorLt(QModel model, Long cursor) {
        return cursor != null ? model.id.lt(cursor) : null;
    }
}
