package com.jj.redline.domain.entity;

import com.jj.redline.domain.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_site")
public class Site extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site_name", nullable = false, length = 100)
    private String siteName;

    @Column(name = "site_link", length = 500)
    private String siteLink;

    public static Site of(String siteName) {
        Site site = new Site();
        site.siteName = siteName;
        site.setAuditId("redline");
        return site;
    }
}
