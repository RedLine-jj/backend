package com.jj.redline.domain.dto.crawl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jj.redline.domain.enums.StockStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOption {
    @JsonIgnore
    private String optionId;     // [추가] 옵션 고유 ID (item_code)
    private String optionLabel;  // 표시 라벨 (예: "1(30)", "30", "4")
    private StockStatus status;  // AVAILABLE / SOLD_OUT
}
