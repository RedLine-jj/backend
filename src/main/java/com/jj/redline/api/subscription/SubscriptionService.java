package com.jj.redline.api.subscription;

import com.jj.redline.domain.dto.common.CursorPageResponse;
import com.jj.redline.domain.dto.subscription.SubscriptionCreateRequest;
import com.jj.redline.domain.dto.subscription.SubscriptionResponse;
import com.jj.redline.domain.dto.subscription.TopSubscriptionResponse;
import com.jj.redline.domain.entity.Model;
import com.jj.redline.domain.entity.Subscription;
import com.jj.redline.domain.entity.User;
import com.jj.redline.domain.repository.ModelRepository;
import com.jj.redline.domain.repository.SubscriptionRepository;
import com.jj.redline.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.Tuple;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final ModelRepository modelRepository;

    public CursorPageResponse<SubscriptionResponse> getSubscriptions(Long cursor, int size) {
        User user = getCurrentUser();
        List<Subscription> subscriptions = subscriptionRepository.findByUserWithCursor(user, cursor, size);

        boolean hasNext = subscriptions.size() > size;
        if (hasNext) {
            subscriptions = subscriptions.subList(0, size);
        }
        Long nextCursor = hasNext ? subscriptions.get(subscriptions.size() - 1).getId() : null;

        List<SubscriptionResponse> content = subscriptions.stream()
                .map(this::toResponse)
                .toList();

        return new CursorPageResponse<>(content, nextCursor, hasNext);
    }

    public long getSubscriptionCount() {
        User user = getCurrentUser();
        return subscriptionRepository.countByUser(user);
    }

    @Transactional
    public void createSubscription(SubscriptionCreateRequest request) {
        User user = getCurrentUser();
        Model model = modelRepository.findById(request.getModelId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모델입니다."));

        if (subscriptionRepository.existsByUserAndModel(user, model)) {
            throw new IllegalArgumentException("이미 구독 중인 모델입니다.");
        }

        subscriptionRepository.save(Subscription.of(user, model));
    }

    @Transactional
    public void deleteSubscription(Long id) {
        User user = getCurrentUser();
        Subscription subscription = subscriptionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다."));

        subscriptionRepository.delete(subscription);
    }

    public List<TopSubscriptionResponse> getTopSubscriptions() {
        List<Tuple> rows = subscriptionRepository.findTopSubscribedModels(10);

        List<TopSubscriptionResponse> result = rows.stream()
                .map(row -> new TopSubscriptionResponse(
                        row.get(0, Long.class),
                        row.get(1, String.class),
                        row.get(2, Long.class)
                ))
                .toList();

        log.debug("getTopSubscriptions: size={}", result.size());
        return result;
    }

    private User getCurrentUser() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private SubscriptionResponse toResponse(Subscription subscription) {
        Model model = subscription.getModel();
        return new SubscriptionResponse(
                subscription.getId(),
                model.getId(),
                model.getBrand().getBrandName(),
                model.getModelName(),
                model.getImageUrl(),
                subscription.getCreatedAt()
        );
    }
}
