package com.jj.redline.domain.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "가격 비교 응답")
public class PriceComparisonResponse {

    @Schema(description = "모델 이름", example = "갤럭시 S24")
    private String modelName;

    @Schema(description = "모델 이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "사이트별 옵션 가격 목록", example = "[]")
    private List<SiteComparisonItem> sites;
}
