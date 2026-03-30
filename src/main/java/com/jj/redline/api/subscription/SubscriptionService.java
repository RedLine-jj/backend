package com.jj.redline.api.subscription;

import com.jj.redline.common.auth.CurrentUserService;
import com.jj.redline.common.pagination.CursorPaginationSupport;
import com.jj.redline.domain.dto.common.CursorPageResponse;
import com.jj.redline.domain.dto.subscription.SubscriptionCreateRequest;
import com.jj.redline.domain.dto.subscription.SubscriptionResponse;
import com.jj.redline.domain.dto.subscription.TopSubscriptionResponse;
import com.jj.redline.domain.entity.Model;
import com.jj.redline.domain.entity.Subscription;
import com.jj.redline.domain.entity.User;
import com.jj.redline.domain.repository.ModelRepository;
import com.jj.redline.domain.repository.SubscriptionRepository;
import com.jj.redline.exception.BadRequestException;
import com.jj.redline.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final ModelRepository modelRepository;
    private final CurrentUserService currentUserService;
    private final SubscriptionResponseMapper subscriptionResponseMapper;

    public CursorPageResponse<SubscriptionResponse> getSubscriptions(Long cursor, int size) {
        User user = getCurrentUser();
        List<Subscription> subscriptions = subscriptionRepository.findByUserWithCursor(user, cursor, size);
        CursorPaginationSupport.CursorSlice<Subscription> slice =
                CursorPaginationSupport.slice(subscriptions, size, Subscription::getId);
        List<SubscriptionResponse> content = slice.content().stream()
                .map(subscriptionResponseMapper::toResponse)
                .toList();

        return new CursorPageResponse<>(content, slice.nextCursor(), slice.hasNext());
    }

    public long getSubscriptionCount() {
        User user = getCurrentUser();
        return subscriptionRepository.countByUser(user);
    }

    @CacheEvict(value = "subscriptions:top", allEntries = true)
    @Transactional
    public void createSubscription(SubscriptionCreateRequest request) {
        User user = getCurrentUser();
        Model model = modelRepository.findById(request.getModelId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 모델입니다."));

        if (subscriptionRepository.existsByUserAndModel(user, model)) {
            throw new BadRequestException("이미 구독 중인 모델입니다.");
        }

        subscriptionRepository.save(Subscription.of(user, model));
    }

    @CacheEvict(value = "subscriptions:top", allEntries = true)
    @Transactional
    public void deleteSubscription(Long id) {
        User user = getCurrentUser();
        Subscription subscription = subscriptionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("구독을 찾을 수 없습니다."));

        subscriptionRepository.delete(subscription);
    }

    @Cacheable(value = "subscriptions:top")
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
        return currentUserService.getCurrentUser();
    }
}
