package com.jj.redline.domain.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "옵션별 가격 항목")
public class OptionPriceItem {

    @Schema(description = "옵션 라벨", example = "256GB 블랙")
    private String optionLabel;

    @Schema(description = "가격", example = "1200000")
    private Integer price;

    @Schema(description = "재고 상태 (true=재고있음)", example = "true")
    private Boolean status;

    @Schema(description = "상품 URL", example = "https://www.coupang.com/vp/products/...")
    private String url;
}
