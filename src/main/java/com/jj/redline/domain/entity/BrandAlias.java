package com.jj.redline.domain.entity;

import com.jj.redline.domain.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_brand_alias")
public class BrandAlias extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alias_name", nullable = false, length = 100, unique = true)
    private String aliasName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    public static BrandAlias of(String aliasName, Brand brand) {
        BrandAlias alias = new BrandAlias();
        alias.aliasName = aliasName;
        alias.brand = brand;
        alias.setAuditId("redline");
        return alias;
    }
}
