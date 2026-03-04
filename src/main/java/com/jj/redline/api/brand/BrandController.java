package com.jj.redline.api.brand;

import com.jj.redline.common.ApiResponse;
import com.jj.redline.domain.dto.brand.BrandResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "브랜드", description = "브랜드 목록 조회")
@RestController
@RequestMapping("/api/brands")
public class BrandController {

    @Operation(summary = "브랜드 목록 조회")
    @GetMapping
    public ApiResponse<List<BrandResponse>> getBrands() {
        return (ApiResponse<List<BrandResponse>>) (ApiResponse<?>) ApiResponse.ok(null);
    }
}
