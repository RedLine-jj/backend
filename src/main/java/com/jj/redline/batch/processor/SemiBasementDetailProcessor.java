package com.jj.redline.batch.processor;

import com.jj.redline.common.util.TimeUtil;
import com.jj.redline.crawling.imweb.ImwebHttpClient;
import com.jj.redline.crawling.imweb.ImwebDetailParser;
import com.jj.redline.domain.dto.crawl.ProductBrief;
import com.jj.redline.domain.dto.crawl.ProductSnapshot;
import com.jj.redline.domain.enums.Site;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@StepScope
@RequiredArgsConstructor
public class SemiBasementDetailProcessor implements ItemProcessor<ProductBrief, ProductSnapshot> {

    private final ImwebHttpClient httpClient;
    private final ImwebDetailParser detailParser;

    @Override
    public ProductSnapshot process(ProductBrief brief) {
        int idx = Integer.parseInt(brief.getProductKey());
        String omsJson = httpClient.getProductDetails(List.of(idx), brief.getUrl());
        return detailParser.parse(omsJson, Site.SEMI_BASEMENT, brief.getCategory(), TimeUtil.nowUtc(), brief);
    }
}
