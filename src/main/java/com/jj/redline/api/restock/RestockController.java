package com.jj.redline.api.restock;

import com.jj.redline.common.ApiResponse;
import com.jj.redline.domain.dto.restock.RecentRestockResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "재입고", description = "최근 재입고 조회")
@RestController
@RequestMapping("/api/restocks")
@RequiredArgsConstructor
public class RestockController {

    private final RestockService restockService;

    @Operation(summary = "최근 재입고 목록 조회 (최대 10개)")
    @GetMapping("/recent")
    public ApiResponse<List<RecentRestockResponse>> getRecentRestocks() {
        return ApiResponse.ok(restockService.getRecentRestocks());
    }
}
