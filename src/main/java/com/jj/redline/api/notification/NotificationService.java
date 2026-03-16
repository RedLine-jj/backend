package com.jj.redline.api.notification;

import com.jj.redline.domain.dto.notification.NotificationResponse;
import com.jj.redline.domain.entity.Model;
import com.jj.redline.domain.entity.RestockNotification;
import com.jj.redline.domain.entity.User;
import com.jj.redline.domain.repository.RestockNotificationRepository;
import com.jj.redline.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final RestockNotificationRepository restockNotificationRepository;
    private final NotificationRedisService notificationRedisService;
    private final UserRepository userRepository;

    public List<NotificationResponse> getNotifications() {
        User user = getCurrentUser();
        return restockNotificationRepository.findByUserOrderByIdDesc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    public long getUnreadCount() {
        User user = getCurrentUser();
        return notificationRedisService.getUnreadCount(user.getId());
    }

    @Transactional
    public void markAsRead(Long id) {
        User user = getCurrentUser();
        RestockNotification notification = restockNotificationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        if (!notification.getReadYn()) {
            notification.markAsRead();
            notificationRedisService.decrement(user.getId());
        }
    }

    @Transactional
    public void markAllAsRead() {
        User user = getCurrentUser();
        int updated = restockNotificationRepository.markAllAsReadByUser(user);
        if (updated > 0) {
            notificationRedisService.reset(user.getId());
        }
    }

    private User getCurrentUser() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private NotificationResponse toResponse(RestockNotification notification) {
        Model model = notification.getModel();
        return new NotificationResponse(
                notification.getId(),
                model.getId(),
                model.getBrand().getBrandName(),
                model.getModelName(),
                model.getImageUrl(),
                notification.getReadYn(),
                notification.getCreatedAt()
        );
    }
}
