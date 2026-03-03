package com.jj.redline.domain.dto.subscription;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "구독 응답")
public class SubscriptionResponse {

    @Schema(description = "구독 ID", example = "1")
    private Long id;

    @Schema(description = "사이트 옵션 ID", example = "1")
    private Long siteOptionId;

    @Schema(description = "사이트 이름", example = "쿠팡")
    private String siteName;

    @Schema(description = "모델 이름", example = "갤럭시 S24")
    private String modelName;

    @Schema(description = "옵션 라벨", example = "256GB 블랙")
    private String optionLabel;

    @Schema(description = "가격", example = "1200000")
    private Integer price;

    @Schema(description = "옵션 재고 상태 (true=재고있음)", example = "true")
    private Boolean optionStatus;

    @Schema(description = "구독 생성 시각", example = "2026-03-03T10:00:00")
    private LocalDateTime createdAt;
}
