package com.jj.redline.api.subscription;

import com.jj.redline.common.ApiResponse;
import com.jj.redline.domain.dto.common.CursorPageResponse;
import com.jj.redline.domain.dto.subscription.SubscriptionCreateRequest;
import com.jj.redline.domain.dto.subscription.SubscriptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "구독", description = "모델 구독 관리 (로그인 필수)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "내 구독 목록 조회 (커서 페이징)")
    @GetMapping
    public ApiResponse<CursorPageResponse<SubscriptionResponse>> getSubscriptions(
            @Parameter(description = "커서") @RequestParam(required = false) Long cursor,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(subscriptionService.getSubscriptions(cursor, size));
    }

    @Operation(summary = "내 구독 수 조회")
    @GetMapping("/count")
    public ApiResponse<Long> getSubscriptionCount() {
        return ApiResponse.ok(subscriptionService.getSubscriptionCount());
    }

    @Operation(summary = "구독 추가")
    @PostMapping
    public ApiResponse<Void> createSubscription(@Valid @RequestBody SubscriptionCreateRequest request) {
        subscriptionService.createSubscription(request);
        return ApiResponse.ok();
    }

    @Operation(summary = "구독 삭제")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSubscription(
            @Parameter(description = "구독 ID") @PathVariable Long id
    ) {
        subscriptionService.deleteSubscription(id);
        return ApiResponse.ok();
    }
}
