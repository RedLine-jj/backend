package com.jj.redline.domain.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "사이트별 가격 추이")
public class SitePriceHistory {

    @Schema(description = "사이트 이름", example = "모드맨")
    private String siteName;

    @Schema(description = "현재 가격 (최신 평균)", example = "87000")
    private Integer currentPrice;

    @Schema(description = "가격 변동 (최신 vs 7일전)", example = "-2700")
    private Integer priceChange;

    @Schema(description = "기간 내 최저가", example = "83300")
    private Integer minPrice;

    @Schema(description = "기간 내 최고가", example = "91100")
    private Integer maxPrice;

    @Schema(description = "일별 가격 이력")
    private List<DailyPrice> history;
}
