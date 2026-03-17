package com.jj.redline.api.notification;

import com.jj.redline.domain.entity.Model;
import com.jj.redline.domain.entity.RestockNotification;
import com.jj.redline.domain.entity.Subscription;
import com.jj.redline.domain.entity.User;
import com.jj.redline.domain.repository.ModelRepository;
import com.jj.redline.domain.repository.RestockNotificationRepository;
import com.jj.redline.domain.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestockSubscriber implements MessageListener {

    private final ModelRepository modelRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final RestockNotificationRepository restockNotificationRepository;
    private final NotificationRedisService notificationRedisService;
    private final SseEmitterService sseEmitterService;

    @Override
    @CacheEvict(value = "restocks:recent", allEntries = true)
    @Transactional
    public void onMessage(Message message, byte[] pattern) {
        Long modelId = Long.parseLong(new String(message.getBody()));
        log.info("[RestockSubscriber] 재입고 이벤트 수신: modelId={}", modelId);

        Model model = modelRepository.findById(modelId).orElse(null);
        if (model == null) {
            log.warn("[RestockSubscriber] 모델 없음: modelId={}", modelId);
            return;
        }

        List<Subscription> subscriptions = subscriptionRepository.findByModel(model);
        if (subscriptions.isEmpty()) {
            log.info("[RestockSubscriber] 구독자 없음: modelId={}", modelId);
            return;
        }

        String modelName = model.getModelName();
        String brandName = model.getBrand().getBrandName();

        for (Subscription subscription : subscriptions) {
            User user = subscription.getUser();

            restockNotificationRepository.save(RestockNotification.of(user, model));
            notificationRedisService.increment(user.getId());

            if (sseEmitterService.isConnected(user.getId())) {
                sseEmitterService.send(user.getId(), "restock", Map.of(
                        "modelId", modelId,
                        "modelName", modelName,
                        "brandName", brandName
                ));
            }
        }

        log.info("[RestockSubscriber] 알림 생성 완료: modelId={}, 구독자={}명", modelId, subscriptions.size());
    }
}
