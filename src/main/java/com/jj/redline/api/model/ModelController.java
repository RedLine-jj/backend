package com.jj.redline.api.model;

import com.jj.redline.common.ApiResponse;
import com.jj.redline.domain.dto.common.CursorPageResponse;
import com.jj.redline.domain.dto.model.ModelResponse;
import com.jj.redline.domain.entity.ModelType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Tag(name = "모델", description = "모델 목록 조회 (무한스크롤)")
@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;

    @Operation(summary = "모델 목록 조회 (커서 페이징)")
    @GetMapping
    public ApiResponse<CursorPageResponse<ModelResponse>> getModels(
            @Parameter(description = "브랜드 ID 목록 (콤마 구분, 예: 1,5,12)") @RequestParam(required = false) String brandIds,
            @Parameter(description = "모델 타입 목록 (콤마 구분, 예: DENIM_PANTS,DENIM_JACKET)") @RequestParam(required = false) String types,
            @Parameter(description = "커서 (이전 페이지 마지막 ID)") @RequestParam(required = false) Long cursor,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(modelService.getModels(parseBrandIds(brandIds), parseTypes(types), cursor, size));
    }

    @Operation(summary = "모델 상세 조회")
    @GetMapping("/{id}")
    public ApiResponse<ModelResponse> getModel(@PathVariable Long id) {
        return ApiResponse.ok(modelService.getModel(id));
    }


    @Operation(summary = "모델 총 개수 조회")
    @GetMapping("/count")
    public ApiResponse<Long> getModelCount(
            @Parameter(description = "브랜드 ID 목록 (콤마 구분, 예: 1,5,12)") @RequestParam(required = false) String brandIds
    ) {
        return ApiResponse.ok(modelService.getModelCount(parseBrandIds(brandIds)));
    }
    @Operation(summary = "모델 타입 목록 조회")
    @GetMapping("/types")
    public ApiResponse<java.util.List<ModelTypeResponse>> getModelTypes() {
        java.util.List<ModelTypeResponse> types = java.util.Arrays.stream(ModelType.values())
                .map(t -> new ModelTypeResponse(t.name(), t.getLabel()))
                .toList();
        return ApiResponse.ok(types);
    }

    private List<Long> parseBrandIds(String brandIds) {
        if (!StringUtils.hasText(brandIds)) {
            return Collections.emptyList();
        }
        return Arrays.stream(brandIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .toList();
    }

    private List<ModelType> parseTypes(String types) {
        if (!StringUtils.hasText(types)) {
            return Collections.emptyList();
        }
        return Arrays.stream(types.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(ModelType::valueOf)
                .toList();
    }

}
