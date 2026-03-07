package com.jj.redline.batch.processor;

import com.jj.redline.common.util.TimeUtil;
import com.jj.redline.crawling.modeman.ModeManHttpClient;
import com.jj.redline.crawling.modeman.detail.ModeManJsonLdParser;
import com.jj.redline.domain.dto.CategoryDto;
import com.jj.redline.domain.dto.ProductBrief;
import com.jj.redline.domain.dto.ProductSnapshot;
import com.jj.redline.domain.dto.Site;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@StepScope // JobParameter를 주입받기 위해 StepScope로 지정
@RequiredArgsConstructor
public class ModeManDetailCrawlingProcessor implements ItemProcessor<ProductBrief, ProductSnapshot> {

    private final ModeManHttpClient modeManHttpClient;
    private final ModeManJsonLdParser modeManJsonLdParser;

    // Job Parameter에서 카테고리 정보를 주입받습니다.
    @Value("#{jobParameters['categoryCode']}")
    private long categoryCode;

    @Value("#{jobParameters['categoryName']}")
    private String categoryName;

    @Override
    public ProductSnapshot process(ProductBrief item) throws Exception {
        // 1. ProductBrief에서 상품 상세 페이지 URL 가져오기
        String productUrl = item.getUrl();

        // 2. ModeManHttpClient를 사용해 해당 URL의 HTML 내용 가져오기
        String html = modeManHttpClient.get(productUrl);

        // 3. Parser에 전달할 인자들 준비
        final Site site = Site.MODEMAN;
        final CategoryDto category = new CategoryDto(categoryCode, categoryName);
        final OffsetDateTime capturedAt = TimeUtil.now(); // 크롤링 시각

        // 4. ModeManJsonLdParser를 사용해 HTML에서 ProductSnapshot 파싱하기
        ProductSnapshot snapshot = modeManJsonLdParser.parse(html, site, category, capturedAt, item);

        // 5. 완성된 ProductSnapshot 반환
        return snapshot;
    }
}