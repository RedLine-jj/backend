package com.jj.redline.crawling.modeman.list;

import com.jj.redline.domain.dto.ProductBrief;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ListParseResult {
    List<ProductBrief> productBriefs;
}
