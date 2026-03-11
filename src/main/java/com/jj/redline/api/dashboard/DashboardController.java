package com.jj.redline.api.dashboard;

import com.jj.redline.common.ApiResponse;
import com.jj.redline.domain.dto.dashboard.PriceComparisonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "대시보드", description = "가격 비교 대시보드")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "모델별 사이트×옵션 가격 비교")
    @GetMapping("/price-comparison")
    public ApiResponse<PriceComparisonResponse> getPriceComparison(
            @Parameter(description = "모델 ID", required = true) @RequestParam Long modelId
    ) {
        return ApiResponse.ok(dashboardService.getPriceComparison(modelId));
    }
}
