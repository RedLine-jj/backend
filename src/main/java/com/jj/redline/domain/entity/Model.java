package com.jj.redline.domain.entity;

import com.jj.redline.domain.entity.base.BaseEntity;
import com.jj.redline.domain.enums.ModelType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_model")
public class Model extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(name = "model_name", nullable = false, length = 150)
    private String modelName;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50)
    private ModelType type;

    public static Model of(Brand brand, String modelName, String imageUrl, ModelType type) {
        Model model = new Model();
        model.brand = brand;
        model.modelName = modelName;
        model.imageUrl = imageUrl;
        model.type = type;
        model.setAuditId("redline");
        return model;
    }
}
