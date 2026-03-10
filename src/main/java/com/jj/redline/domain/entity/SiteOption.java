package com.jj.redline.domain.entity;

import com.jj.redline.domain.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_site_option")
public class SiteOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    @Column(name = "option_label", length = 200)
    private String optionLabel;

    @Column(name = "price")
    private Integer price;

    @Column(name = "url", length = 1000)
    private String url;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "last_captured_at")
    private LocalDateTime lastCapturedAt;

    public static SiteOption of(Site site, Model model, String optionLabel,
                                Integer price, String url, Boolean status, LocalDateTime lastCapturedAt) {
        SiteOption so = new SiteOption();
        so.site = site;
        so.model = model;
        so.optionLabel = optionLabel;
        so.price = price;
        so.url = url;
        so.status = status;
        so.lastCapturedAt = lastCapturedAt;
        so.setAuditId("redline");
        return so;
    }

    public void update(Integer price, Boolean status, LocalDateTime lastCapturedAt) {
        this.price = price;
        this.status = status;
        this.lastCapturedAt = lastCapturedAt;
    }
}
