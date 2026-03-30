package com.jj.redline.domain.dto.crawl;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ListParseResult {
    List<ProductBrief> productBriefs;
}
