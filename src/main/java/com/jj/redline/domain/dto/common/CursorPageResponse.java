package com.jj.redline.domain.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "무한스크롤 커서 페이징 응답")
public class CursorPageResponse<T> {

    @Schema(description = "조회된 항목 목록")
    private List<T> content;

    @Schema(description = "다음 페이지 커서 (마지막 페이지면 null)", example = "42")
    private Long nextCursor;

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasNext;
}
