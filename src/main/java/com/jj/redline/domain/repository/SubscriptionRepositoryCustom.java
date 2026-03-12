package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.Subscription;
import com.jj.redline.domain.entity.User;
import com.querydsl.core.Tuple;

import java.util.List;

public interface SubscriptionRepositoryCustom {

    List<Subscription> findByUserWithCursor(User user, Long cursor, int size);

    /** 구독 수 기준 TOP N 모델 조회 (modelId, modelName, count) */
    List<Tuple> findTopSubscribedModels(int limit);
}
