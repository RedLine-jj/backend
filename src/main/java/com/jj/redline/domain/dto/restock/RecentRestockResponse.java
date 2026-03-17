package com.jj.redline.domain.dto.restock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "최근 재입고 응답")
public class RecentRestockResponse implements Serializable {

    @Schema(description = "모델 ID", example = "1")
    private Long modelId;

    @Schema(description = "모델 이름", example = "Levi's 501")
    private String modelName;

    @Schema(description = "사이트 이름", example = "END Clothing")
    private String siteName;

    @Schema(description = "재입고 시각", example = "2026-03-11T12:30:00")
    private LocalDateTime restockedAt;
}
