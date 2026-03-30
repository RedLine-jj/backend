package com.jj.redline.api.subscription;

import com.jj.redline.domain.dto.subscription.SubscriptionResponse;
import com.jj.redline.domain.entity.Model;
import com.jj.redline.domain.entity.Subscription;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionResponseMapper {

    public SubscriptionResponse toResponse(Subscription subscription) {
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
