package com.jj.redline.api.siteoption;

import com.jj.redline.common.ApiResponse;
import com.jj.redline.domain.dto.common.CursorPageResponse;
import com.jj.redline.domain.dto.siteoption.SiteOptionDetailResponse;
import com.jj.redline.domain.dto.siteoption.SiteOptionLogResponse;
import com.jj.redline.domain.dto.siteoption.SiteOptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사이트 옵션", description = "사이트 옵션 목록/상세/이력 조회")
@RestController
@RequestMapping("/api/site-options")
public class SiteOptionController {

    @Operation(summary = "사이트 옵션 목록 조회 (커서 페이징, 필터 선택)")
    @GetMapping
    public ApiResponse<CursorPageResponse<SiteOptionResponse>> getSiteOptions(
            @Parameter(description = "사이트 ID (선택)") @RequestParam(required = false) Long siteId,
            @Parameter(description = "모델 ID (선택)") @RequestParam(required = false) Long modelId,
            @Parameter(description = "상태 (선택, true=재고있음)") @RequestParam(required = false) Boolean status,
            @Parameter(description = "커서") @RequestParam(required = false) Long cursor,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok((CursorPageResponse<SiteOptionResponse>) null);
    }

    @Operation(summary = "사이트 옵션 상세 조회")
    @GetMapping("/{id}")
    public ApiResponse<SiteOptionDetailResponse> getSiteOption(
            @Parameter(description = "옵션 ID") @PathVariable Long id
    ) {
        return ApiResponse.ok((SiteOptionDetailResponse) null);
    }

    @Operation(summary = "사이트 옵션 이력 조회 (커서 페이징)")
    @GetMapping("/{id}/logs")
    public ApiResponse<CursorPageResponse<SiteOptionLogResponse>> getSiteOptionLogs(
            @Parameter(description = "옵션 ID") @PathVariable Long id,
            @Parameter(description = "커서") @RequestParam(required = false) Long cursor,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok((CursorPageResponse<SiteOptionLogResponse>) null);
    }
}
