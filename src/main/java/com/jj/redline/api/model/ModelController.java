package com.jj.redline.api.model;

import com.jj.redline.common.ApiResponse;
import com.jj.redline.domain.dto.common.CursorPageResponse;
import com.jj.redline.domain.dto.model.ModelResponse;
import com.jj.redline.domain.entity.ModelType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "모델", description = "모델 목록 조회 (무한스크롤)")
@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;

    @Operation(summary = "모델 목록 조회 (커서 페이징)")
    @GetMapping
    public ApiResponse<CursorPageResponse<ModelResponse>> getModels(
            @Parameter(description = "브랜드 ID (선택)") @RequestParam(required = false) Long brandId,
            @Parameter(description = "커서 (이전 페이지 마지막 ID)") @RequestParam(required = false) Long cursor,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(modelService.getModels(brandId, cursor, size));
    }

    @Operation(summary = "모델 타입 목록 조회")
    @GetMapping("/types")
    public ApiResponse<java.util.List<ModelTypeResponse>> getModelTypes() {
        java.util.List<ModelTypeResponse> types = java.util.Arrays.stream(ModelType.values())
                .map(t -> new ModelTypeResponse(t.name(), t.getLabel()))
                .toList();
        return ApiResponse.ok(types);
    }
}
