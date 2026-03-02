package com.jj.redline.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private int code;      // cate_no (예: 263, 858)
    private String name;   // "Denim Jackets", "Denim Pents"
}