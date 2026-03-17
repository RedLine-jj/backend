package com.jj.redline.batch.processor;

import com.jj.redline.common.util.TimeUtil;
import com.jj.redline.crawling.semibasement.SemiBasementHttpClient;
import com.jj.redline.crawling.semibasement.detail.SemiBasementDetailParser;
import com.jj.redline.domain.dto.ProductBrief;
import com.jj.redline.domain.dto.ProductSnapshot;
import com.jj.redline.domain.dto.Site;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@StepScope
@RequiredArgsConstructor
public class SemiBasementDetailProcessor implements ItemProcessor<ProductBrief, ProductSnapshot> {

    private final SemiBasementHttpClient httpClient;
    private final SemiBasementDetailParser detailParser;

    @Override
    public ProductSnapshot process(ProductBrief brief) {
        int idx = Integer.parseInt(brief.getProductKey());
        String omsJson = httpClient.getProductDetails(List.of(idx), brief.getUrl());
        return detailParser.parse(omsJson, Site.SEMI_BASEMENT, brief.getCategory(), TimeUtil.nowUtc(), brief);
    }
}
