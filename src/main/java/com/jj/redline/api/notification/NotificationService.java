package com.jj.redline.api.notification;

import com.jj.redline.common.auth.CurrentUserService;
import com.jj.redline.domain.dto.notification.NotificationResponse;
import com.jj.redline.domain.entity.Model;
import com.jj.redline.domain.entity.RestockNotification;
import com.jj.redline.domain.entity.User;
import com.jj.redline.domain.repository.RestockNotificationRepository;
import com.jj.redline.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final RestockNotificationRepository restockNotificationRepository;
    private final NotificationRedisService notificationRedisService;
    private final CurrentUserService currentUserService;
    private final SseEmitterService sseEmitterService;
    private final NotificationResponseMapper notificationResponseMapper;

    public SseEmitter connectStream() {
        User user = getCurrentUser();
        return sseEmitterService.connect(user.getId());
    }

    public List<NotificationResponse> getNotifications() {
        User user = getCurrentUser();
        return restockNotificationRepository.findByUserOrderByIdDesc(user).stream()
                .map(notificationResponseMapper::toResponse)
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
                .orElseThrow(() -> new NotFoundException("알림을 찾을 수 없습니다."));

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
        return currentUserService.getCurrentUser();
    }
}
