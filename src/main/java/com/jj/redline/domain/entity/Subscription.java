package com.jj.redline.domain.entity;

import com.jj.redline.domain.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_subscription",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "model_id"}))
public class Subscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    public static Subscription of(User user, Model model) {
        Subscription subscription = new Subscription();
        subscription.user = user;
        subscription.model = model;
        subscription.setAuditId(user.getUserId());
        return subscription;
    }
}
