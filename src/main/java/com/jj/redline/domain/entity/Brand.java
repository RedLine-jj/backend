package com.jj.redline.domain.entity;

import com.jj.redline.domain.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_brand")
public class Brand extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "brand_name", nullable = false, length = 100)
    private String brandName;

    @Column(name = "brand_name_ko", length = 100)
    private String brandNameKo;

    public static Brand of(String brandName) {
        Brand brand = new Brand();
        brand.brandName = brandName;
        brand.setAuditId("redline");
        return brand;
    }
}
