package com.jj.redline.domain.entity;

import com.jj.redline.domain.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_restock_notification")
public class RestockNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    @Column(name = "read_yn", nullable = false)
    private Boolean readYn = false;

    public static RestockNotification of(User user, Model model) {
        RestockNotification notification = new RestockNotification();
        notification.user = user;
        notification.model = model;
        notification.readYn = false;
        notification.setAuditId("redline");
        return notification;
    }

    public void markAsRead() {
        this.readYn = true;
    }
}
