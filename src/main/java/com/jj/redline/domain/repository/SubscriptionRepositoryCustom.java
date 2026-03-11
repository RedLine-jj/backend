package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.Subscription;
import com.jj.redline.domain.entity.User;

import java.util.List;

public interface SubscriptionRepositoryCustom {

    List<Subscription> findByUserWithCursor(User user, Long cursor, int size);
}
