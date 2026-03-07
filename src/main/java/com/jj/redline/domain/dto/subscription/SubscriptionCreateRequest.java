package com.jj.redline.domain.dto.subscription;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(description = "구독 생성 요청")
public class SubscriptionCreateRequest {

    @NotNull
    @Schema(description = "구독할 사이트 옵션 ID", example = "1")
    private Long siteOptionId;
}
