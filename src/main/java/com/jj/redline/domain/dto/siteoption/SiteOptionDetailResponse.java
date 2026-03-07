package com.jj.redline.domain.dto.siteoption;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "사이트 옵션 상세 응답")
public class SiteOptionDetailResponse {

    @Schema(description = "옵션 ID", example = "1")
    private Long id;

    @Schema(description = "사이트 이름", example = "쿠팡")
    private String siteName;

    @Schema(description = "사이트 링크", example = "https://www.coupang.com")
    private String siteLink;

    @Schema(description = "브랜드 이름", example = "삼성")
    private String brandName;

    @Schema(description = "모델 이름", example = "갤럭시 S24")
    private String modelName;

    @Schema(description = "모델 이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "옵션 라벨", example = "256GB 블랙")
    private String optionLabel;

    @Schema(description = "가격", example = "1200000")
    private Integer price;

    @Schema(description = "재고 상태 (true=재고있음)", example = "true")
    private Boolean status;

    @Schema(description = "상품 URL", example = "https://www.coupang.com/vp/products/...")
    private String url;

    @Schema(description = "마지막 수집 시각", example = "2026-03-03T10:00:00")
    private LocalDateTime lastCapturedAt;
}
