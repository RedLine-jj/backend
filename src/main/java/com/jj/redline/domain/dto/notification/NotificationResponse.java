package com.jj.redline.domain.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "알림 응답")
public class NotificationResponse {

    @Schema(description = "알림 ID", example = "1")
    private Long id;

    @Schema(description = "모델 ID", example = "1")
    private Long modelId;

    @Schema(description = "브랜드 이름", example = "Levi's")
    private String brandName;

    @Schema(description = "모델 이름", example = "501 Original")
    private String modelName;

    @Schema(description = "모델 이미지 URL")
    private String imageUrl;

    @Schema(description = "읽음 여부")
    private Boolean readYn;

    @Schema(description = "알림 생성 시각")
    private LocalDateTime createdAt;
}
