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

    @Schema(description = "모델 ID", example = "1")
    private Long modelId;

    @Schema(description = "브랜드 이름", example = "Levi's")
    private String brandName;

    @Schema(description = "모델 이름", example = "501 Original")
    private String modelName;

    @Schema(description = "모델 이미지 URL")
    private String imageUrl;

    @Schema(description = "구독 생성 시각", example = "2026-03-03T10:00:00")
    private LocalDateTime createdAt;
}
