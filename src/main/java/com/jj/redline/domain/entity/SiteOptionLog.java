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
@Table(name = "tb_site_option_log")
public class SiteOptionLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_option_id", nullable = false)
    private SiteOption siteOption;

    @Column(name = "captured_at")
    private LocalDateTime capturedAt;

    @Column(name = "option_label", length = 200)
    private String optionLabel;

    @Column(name = "price")
    private Integer price;

    @Column(name = "status")
    private Boolean status;

    public static SiteOptionLog of(SiteOption siteOption, Integer price, Boolean status, LocalDateTime capturedAt) {
        SiteOptionLog log = new SiteOptionLog();
        log.siteOption = siteOption;
        log.optionLabel = siteOption.getOptionLabel();
        log.price = price;
        log.status = status;
        log.capturedAt = capturedAt;
        log.setAuditId("redline");
        return log;
    }
}
