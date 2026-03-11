package com.jj.redline.domain.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "일별 가격")
public class DailyPrice {

    @Schema(description = "날짜", example = "2026-02-10")
    private String date;

    @Schema(description = "평균 가격", example = "87000")
    private Integer price;
}
