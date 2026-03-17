package com.jj.redline.domain.dto.subscription;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@Schema(description = "구독 수 TOP 응답")
public class TopSubscriptionResponse implements Serializable {

    @Schema(description = "모델 ID", example = "1")
    private Long modelId;

    @Schema(description = "모델 이름", example = "Levi's 501")
    private String modelName;

    @Schema(description = "구독 수", example = "142")
    private Long count;
}
