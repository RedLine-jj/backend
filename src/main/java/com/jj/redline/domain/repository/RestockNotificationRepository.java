package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.RestockNotification;
import com.jj.redline.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RestockNotificationRepository extends JpaRepository<RestockNotification, Long> {

    List<RestockNotification> findByUserOrderByIdDesc(User user);

    Optional<RestockNotification> findByIdAndUser(Long id, User user);

    long countByUserAndReadYn(User user, Boolean readYn);

    @Modifying
    @Query("UPDATE RestockNotification n SET n.readYn = true WHERE n.user = :user AND n.readYn = false")
    int markAllAsReadByUser(User user);
}
