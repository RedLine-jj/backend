package com.jj.redline.api.notification;

import com.jj.redline.domain.dto.notification.NotificationResponse;
import com.jj.redline.domain.entity.Model;
import com.jj.redline.domain.entity.RestockNotification;
import org.springframework.stereotype.Component;

@Component
public class NotificationResponseMapper {

    public NotificationResponse toResponse(RestockNotification notification) {
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
