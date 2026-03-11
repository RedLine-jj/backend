package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.QBrand;
import com.jj.redline.domain.entity.QModel;
import com.jj.redline.domain.entity.QSubscription;
import com.jj.redline.domain.entity.Subscription;
import com.jj.redline.domain.entity.User;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SubscriptionRepositoryImpl implements SubscriptionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Subscription> findByUserWithCursor(User user, Long cursor, int size) {
        QSubscription subscription = QSubscription.subscription;
        QModel model = QModel.model;
        QBrand brand = QBrand.brand;

        return queryFactory
                .selectFrom(subscription)
                .join(subscription.model, model).fetchJoin()
                .join(model.brand, brand).fetchJoin()
                .where(
                        subscription.user.eq(user),
                        cursorLt(subscription, cursor)
                )
                .orderBy(subscription.id.desc())
                .limit(size + 1)
                .fetch();
    }

    private BooleanExpression cursorLt(QSubscription subscription, Long cursor) {
        return cursor != null ? subscription.id.lt(cursor) : null;
    }
}
