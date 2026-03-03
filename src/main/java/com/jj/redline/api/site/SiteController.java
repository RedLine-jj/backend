package com.jj.redline.api.site;

import com.jj.redline.common.ApiResponse;
import com.jj.redline.domain.dto.site.SiteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "사이트", description = "사이트 목록 조회")
@RestController
@RequestMapping("/api/sites")
public class SiteController {

    @Operation(summary = "사이트 목록 조회")
    @GetMapping
    public ApiResponse<List<SiteResponse>> getSites() {
        return ApiResponse.ok(null, null);
    }
}
