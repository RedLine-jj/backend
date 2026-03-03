package com.jj.redline.domain.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "무한스크롤 커서 페이징 요청")
public class CursorPageRequest {

    @Schema(description = "마지막으로 받은 항목의 ID (첫 요청 시 null)", example = "null")
    private Long cursor;

    @Schema(description = "한 번에 가져올 항목 수", example = "20", defaultValue = "20")
    @Builder.Default
    private int size = 20;
}
