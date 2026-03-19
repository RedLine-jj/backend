package com.jj.redline.domain.dto.crawl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private long code;      // [수정] cate_no (예: 263, 858)
    private String name;   // "Denim Jackets", "Denim Pants"
}
