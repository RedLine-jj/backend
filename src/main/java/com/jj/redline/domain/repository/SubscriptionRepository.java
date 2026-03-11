package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.Model;
import com.jj.redline.domain.entity.Subscription;
import com.jj.redline.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long>, SubscriptionRepositoryCustom {

    long countByUser(User user);

    boolean existsByUserAndModel(User user, Model model);

    Optional<Subscription> findByIdAndUser(Long id, User user);
}
