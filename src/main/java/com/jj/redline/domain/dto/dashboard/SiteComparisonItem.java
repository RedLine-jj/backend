package com.jj.redline.domain.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "사이트별 옵션 가격 항목")
public class SiteComparisonItem {

    @Schema(description = "사이트 이름", example = "쿠팡")
    private String siteName;

    @Schema(description = "옵션별 가격 목록", example = "[]")
    private List<OptionPriceItem> options;
}
