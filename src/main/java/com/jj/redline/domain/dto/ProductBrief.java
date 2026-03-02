package com.jj.redline.domain.dto;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBrief {
    private String site;               // "MODEMAN"
    private CategoryDto category;      // {code, name}

    private String productKey;         // 목록에서 얻는 상품 키(예: "8128")
    private String url;                // 상세 URL(절대경로)
    private String name;               // 목록의 상품명(가능하면)
    private String imageUrl;           // 썸네일(절대경로)
    private Integer price;             // 대표 가격(없을 수도 있어 Integer)
    private OffsetDateTime capturedAt; // 목록에서 이 상품을 수집한 시각
}
