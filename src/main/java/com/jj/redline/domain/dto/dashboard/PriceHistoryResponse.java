package com.jj.redline.domain.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "가격 추이 응답")
public class PriceHistoryResponse {

    @Schema(description = "사이트별 가격 추이 목록")
    private List<SitePriceHistory> sites;
}
