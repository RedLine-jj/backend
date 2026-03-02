package com.jj.redline.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOption {
    private String optionLabel;  // 표시 라벨 (예: "1(30)", "30", "4")
    private String status;  // AVAILABLE / SOLD_OUT
}
